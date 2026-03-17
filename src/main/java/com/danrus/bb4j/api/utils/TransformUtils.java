package com.danrus.bb4j.api.utils;

import com.danrus.bb4j.api.utils.AnimationUtils;
import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.animation.*;
import com.danrus.bb4j.model.outliner.OutlinerNode;
import com.danrus.bb4j.model.outliner.OutlinerGroupNode;
import com.danrus.bb4j.molang.MolangEvaluator;

import java.util.*;

public class TransformUtils {
    
    private final BbModelDocument document;
    private final AnimationUtils animationUtils;
    private MolangEvaluator molangEvaluator;
    
    private TransformUtils(BbModelDocument document) {
        this.document = document;
        this.animationUtils = AnimationUtils.forDocument(document);
    }
    
    public static TransformUtils forDocument(BbModelDocument document) {
        return new TransformUtils(document);
    }
    
    public void setMolangContext(MolangEvaluator evaluator) {
        this.molangEvaluator = evaluator;
    }
    
    public void setMolangVariables(Map<String, Double> variables) {
        if (this.molangEvaluator == null) {
            this.molangEvaluator = MolangEvaluator.withDefaultContext();
        }
        this.molangEvaluator.setVariables(variables);
    }
    
    public void setMolangVariable(String name, double value) {
        if (this.molangEvaluator == null) {
            this.molangEvaluator = MolangEvaluator.withDefaultContext();
        }
        this.molangEvaluator.setVariable(name, value);
    }
    
    public Transform getTransformAtTime(String targetUuid, Animation animation, double time) {
        if (animation == null || animation.getAnimators() == null) {
            return getStaticTransform(targetUuid);
        }
        
        Animator animator = animation.getAnimators().get(targetUuid);
        if (animator == null || animator.getKeyframes() == null) {
            return getStaticTransform(targetUuid);
        }
        
        Transform transform = new Transform();
        
        Map<String, List<Keyframe>> keyframesByChannel = new HashMap<>();
        for (Keyframe kf : animator.getKeyframes()) {
            if (kf.getChannel() == null) continue;
            keyframesByChannel.computeIfAbsent(kf.getChannel(), k -> new ArrayList<>()).add(kf);
        }
        
        for (Map.Entry<String, List<Keyframe>> entry : keyframesByChannel.entrySet()) {
            List<Keyframe> keyframes = entry.getValue();
            keyframes.sort(Comparator.comparing(kf -> kf.getTime() != null ? kf.getTime() : 0.0));
            
            Keyframe[] interpolated = interpolateKeyframes(keyframes, time);
            
            if (interpolated == null) continue;
            
            for (Keyframe kf : interpolated) {
                if (kf == null || kf.getDataPoints() == null || kf.getDataPoints().isEmpty()) continue;
                
                DataPoint dp = kf.getDataPoints().get(0);
                if (dp == null) continue;
                
                switch (entry.getKey()) {
                    case "position" -> {
                        if (dp.getX() != null) transform.setX(evaluateMolangOrNumber(dp.getX()));
                        if (dp.getY() != null) transform.setY(evaluateMolangOrNumber(dp.getY()));
                        if (dp.getZ() != null) transform.setZ(evaluateMolangOrNumber(dp.getZ()));
                    }
                    case "rotation" -> {
                        if (dp.getX() != null) transform.setRotX(evaluateMolangOrNumber(dp.getX()));
                        if (dp.getY() != null) transform.setRotY(evaluateMolangOrNumber(dp.getY()));
                        if (dp.getZ() != null) transform.setRotZ(evaluateMolangOrNumber(dp.getZ()));
                    }
                    case "scale" -> {
                        if (dp.getX() != null) transform.setScaleX(evaluateMolangOrNumber(dp.getX()));
                        if (dp.getY() != null) transform.setScaleY(evaluateMolangOrNumber(dp.getY()));
                        if (dp.getZ() != null) transform.setScaleZ(evaluateMolangOrNumber(dp.getZ()));
                    }
                }
            }
        }
        
        return transform.hasAnyValue() ? transform : getStaticTransform(targetUuid);
    }
    
    private Keyframe[] interpolateKeyframes(List<Keyframe> keyframes, double time) {
        if (keyframes == null || keyframes.isEmpty()) return null;
        
        Keyframe before = null;
        Keyframe after = null;
        
        for (Keyframe kf : keyframes) {
            double kfTime = kf.getTime() != null ? kf.getTime() : 0;
            if (kfTime <= time) {
                before = kf;
            } else {
                after = kf;
                break;
            }
        }
        
        if (before == null && after == null) return null;
        if (before == null) return new Keyframe[]{after};
        if (after == null) return new Keyframe[]{before};
        
        double t = (time - before.getTime()) / (after.getTime() - before.getTime());
        t = Math.max(0, Math.min(1, t));
        
        Interpolation interp = before.getInterpolation();
        
        if (interp != null && interp.isBezier()) {
            t = applyBezier(t, before, after);
        } else if (interp != null && interp.isStepped()) {
            t = 0;
        }
        
        return interpolateKeyframeData(before, after, t);
    }
    
    private double applyBezier(double t, Keyframe before, Keyframe after) {
        Double[] left = before.getBezierRightValue();
        Double[] right = after.getBezierLeftValue();
        
        if (left == null || right == null || left.length < 2 || right.length < 2) {
            return t;
        }
        
        double cp1x = left[0] / 100.0;
        double cp1y = left[1] / 100.0;
        double cp2x = 1.0 + right[0] / 100.0;
        double cp2y = 1.0 + right[1] / 100.0;
        
        return cubicBezierY(t, cp1x, cp1y, cp2x, cp2y);
    }
    
    private double cubicBezierY(double t, double x1, double y1, double x2, double y2) {
        double cx = 3.0 * x1;
        double bx = 3.0 * (x2 - x1) - cx;
        double ax = 1.0 - cx - bx;
        
        double cy = 3.0 * y1;
        double by = 3.0 * (y2 - y1) - cy;
        double ay = 1.0 - cy - by;
        
        return ((ay * t + by) * t + cy) * t;
    }
    
    private Keyframe[] interpolateKeyframeData(Keyframe before, Keyframe after, double t) {
        DataPoint dpBefore = before.getDataPoints() != null && !before.getDataPoints().isEmpty() 
            ? before.getDataPoints().get(0) : null;
        DataPoint dpAfter = after.getDataPoints() != null && !after.getDataPoints().isEmpty() 
            ? after.getDataPoints().get(0) : null;
        
        if (dpBefore == null && dpAfter == null) return null;
        
        Keyframe result = new Keyframe();
        result.setTime(t);
        result.setChannel(before.getChannel());
        
        DataPoint interpolated = new DataPoint();
        
        if (dpBefore != null) {
            if (dpBefore.getX() != null) {
                Double v1 = evaluateMolangOrNumber(dpBefore.getX());
                Double v2 = dpAfter != null && dpAfter.getX() != null ? evaluateMolangOrNumber(dpAfter.getX()) : v1;
                if (v1 != null && v2 != null) {
                    interpolated.setX(String.valueOf(lerp(v1, v2, t)));
                } else if (v1 != null) {
                    interpolated.setX(String.valueOf(v1));
                }
            }
            if (dpBefore.getY() != null) {
                Double v1 = evaluateMolangOrNumber(dpBefore.getY());
                Double v2 = dpAfter != null && dpAfter.getY() != null ? evaluateMolangOrNumber(dpAfter.getY()) : v1;
                if (v1 != null && v2 != null) {
                    interpolated.setY(String.valueOf(lerpAngle(v1, v2, t)));
                } else if (v1 != null) {
                    interpolated.setY(String.valueOf(v1));
                }
            }
            if (dpBefore.getZ() != null) {
                Double v1 = evaluateMolangOrNumber(dpBefore.getZ());
                Double v2 = dpAfter != null && dpAfter.getZ() != null ? evaluateMolangOrNumber(dpAfter.getZ()) : v1;
                if (v1 != null && v2 != null) {
                    interpolated.setZ(String.valueOf(lerpAngle(v1, v2, t)));
                } else if (v1 != null) {
                    interpolated.setZ(String.valueOf(v1));
                }
            }
            if (dpBefore.getW() != null) {
                Double v1 = evaluateMolangOrNumber(dpBefore.getW());
                Double v2 = dpAfter != null && dpAfter.getW() != null ? evaluateMolangOrNumber(dpAfter.getW()) : v1;
                if (v1 != null && v2 != null) {
                    interpolated.setW(String.valueOf(lerp(v1, v2, t)));
                } else if (v1 != null) {
                    interpolated.setW(String.valueOf(v1));
                }
            }
        }
        
        result.setDataPoints(List.of(interpolated));
        return new Keyframe[]{result};
    }
    
    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
    
    private double lerpAngle(double from, double to, double t) {
        double diff = to - from;
        while (diff > 180) diff -= 360;
        while (diff < -180) diff += 360;
        return from + diff * t;
    }
    
    private Double evaluateMolangOrNumber(String value) {
        if (value == null) return null;
        
        if (isNumericString(value)) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        if (molangEvaluator != null) {
            try {
                return molangEvaluator.evaluateOrNull(value);
            } catch (Exception ignored) {}
        }
        
        return null;
    }
    
    private boolean isNumericString(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public Transform getTransformAtTime(String targetUuid, String animationName, double time) {
        Animation animation = animationUtils.getAnimationByName(animationName);
        return getTransformAtTime(targetUuid, animation, time);
    }
    
    public Transform getElementTransformAtTime(String elementUuid, Animation animation, double time) {
        return getTransformAtTime(elementUuid, animation, time);
    }
    
    public Transform getGroupTransformAtTime(String groupUuid, Animation animation, double time) {
        Transform transform = getTransformAtTime(groupUuid, animation, time);
        
        if (transform == null || !transform.hasAnyValue()) {
            OutlinerGroupNode group = findGroupByUuid(groupUuid);
            if (group != null && group.getRotation() != null) {
                transform = new Transform();
                if (group.getRotation().length >= 1) transform.setRotX((double) group.getRotation()[0]);
                if (group.getRotation().length >= 2) transform.setRotY((double) group.getRotation()[1]);
                if (group.getRotation().length >= 3) transform.setRotZ((double) group.getRotation()[2]);
            }
        }
        
        return transform;
    }
    
    public Map<String, Transform> getAllTransformsAtTime(Animation animation, double time) {
        Map<String, Transform> result = new HashMap<>();
        
        if (animation == null || animation.getAnimators() == null) {
            return result;
        }
        
        for (String uuid : animation.getAnimators().keySet()) {
            Transform transform = getTransformAtTime(uuid, animation, time);
            if (transform != null) {
                result.put(uuid, transform);
            }
        }
        
        return result;
    }
    
    public Transform getStaticTransform(String targetUuid) {
        Transform transform = new Transform();
        
        if (document.getElements() != null) {
            for (var element : document.getElements()) {
                if (targetUuid.equals(element.getUuid())) {
                    Double[] from = element.getFrom();
                    Double[] to = element.getTo();
                    if (from != null && to != null && from.length >= 3 && to.length >= 3) {
                        transform.setX((from[0] + to[0]) / 2);
                        transform.setY((from[1] + to[1]) / 2);
                        transform.setZ((from[2] + to[2]) / 2);
                    }
                    
                    Integer[] rotation = element.getRotation();
                    if (rotation != null && rotation.length >= 3) {
                        transform.setRotX((double) rotation[0]);
                        transform.setRotY((double) rotation[1]);
                        transform.setRotZ((double) rotation[2]);
                    }
                    
                    Double[] scale = element.getScale();
                    if (scale != null && scale.length >= 3) {
                        transform.setScaleX(scale[0]);
                        transform.setScaleY(scale[1]);
                        transform.setScaleZ(scale[2]);
                    }
                    
                    return transform;
                }
            }
        }
        
        OutlinerGroupNode group = findGroupByUuid(targetUuid);
        if (group != null) {
            if (group.getRotation() != null && group.getRotation().length >= 3) {
                transform.setRotX((double) group.getRotation()[0]);
                transform.setRotY((double) group.getRotation()[1]);
                transform.setRotZ((double) group.getRotation()[2]);
            }
            if (group.getTranslation() != null && group.getTranslation().length >= 3) {
                transform.setX(group.getTranslation()[0]);
                transform.setY(group.getTranslation()[1]);
                transform.setZ(group.getTranslation()[2]);
            }
            if (group.getScale() != null && group.getScale().length >= 3) {
                transform.setScaleX(group.getScale()[0]);
                transform.setScaleY(group.getScale()[1]);
                transform.setScaleZ(group.getScale()[2]);
            }
        }
        
        return transform;
    }
    
    private OutlinerGroupNode findGroupByUuid(String uuid) {
        if (uuid == null || document.getOutliner() == null) {
            return null;
        }
        return findGroupByUuid(document.getOutliner(), uuid);
    }
    
    private OutlinerGroupNode findGroupByUuid(List<OutlinerNode> nodes, String uuid) {
        if (nodes == null) return null;
        
        for (OutlinerNode node : nodes) {
            if (uuid.equals(node.getUuid()) && node instanceof OutlinerGroupNode) {
                return (OutlinerGroupNode) node;
            }
            if (node.getChildren() != null) {
                OutlinerGroupNode found = findGroupByUuid(node.getChildren(), uuid);
                if (found != null) return found;
            }
        }
        return null;
    }
    
    public static class Transform {
        private Double x, y, z;
        private Double rotX, rotY, rotZ;
        private Double scaleX, scaleY, scaleZ;
        
        public Double getX() { return x != null ? x : 0.0; }
        public void setX(Double x) { this.x = x; }
        
        public Double getY() { return y != null ? y : 0.0; }
        public void setY(Double y) { this.y = y; }
        
        public Double getZ() { return z != null ? z : 0.0; }
        public void setZ(Double z) { this.z = z; }
        
        public Double getRotX() { return rotX != null ? rotX : 0.0; }
        public void setRotX(Double rotX) { this.rotX = rotX; }
        
        public Double getRotY() { return rotY != null ? rotY : 0.0; }
        public void setRotY(Double rotY) { this.rotY = rotY; }
        
        public Double getRotZ() { return rotZ != null ? rotZ : 0.0; }
        public void setRotZ(Double rotZ) { this.rotZ = rotZ; }
        
        public Double getScaleX() { return scaleX != null ? scaleX : 1.0; }
        public void setScaleX(Double scaleX) { this.scaleX = scaleX; }
        
        public Double getScaleY() { return scaleY != null ? scaleY : 1.0; }
        public void setScaleY(Double scaleY) { this.scaleY = scaleY; }
        
        public Double getScaleZ() { return scaleZ != null ? scaleZ : 1.0; }
        public void setScaleZ(Double scaleZ) { this.scaleZ = scaleZ; }
        
        public boolean hasAnyValue() {
            return x != null || y != null || z != null ||
                   rotX != null || rotY != null || rotZ != null ||
                   scaleX != null || scaleY != null || scaleZ != null;
        }
        
        public double[] toTranslationArray() {
            return new double[]{getX(), getY(), getZ()};
        }
        
        public double[] toRotationArray() {
            return new double[]{getRotX(), getRotY(), getRotZ()};
        }
        
        public double[] toScaleArray() {
            return new double[]{getScaleX(), getScaleY(), getScaleZ()};
        }
    }
}
