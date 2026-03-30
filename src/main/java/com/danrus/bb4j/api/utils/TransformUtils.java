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
            return new Transform();
        }

        Double length = animation.getLength();
        if (length != null && length > 0) {
            if (animation.getLoop() != null && animation.getLoop() > 0) {
                time = time % length;
            } else {
                time = Math.min(time, length);
            }
        }
        
        Animator animator = animation.getAnimators().get(targetUuid);
        if (animator == null || animator.getKeyframes() == null) {
            return new Transform();
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
        
        return transform;
    }
    
    private Keyframe[] interpolateKeyframes(List<Keyframe> keyframes, double time) {
        if (keyframes == null || keyframes.isEmpty()) return null;
        
        Keyframe before = null;
        Keyframe after = null;
        int beforeIndex = -1;
        
        for (int i = 0; i < keyframes.size(); i++) {
            Keyframe kf = keyframes.get(i);
            double kfTime = kf.getTime() != null ? kf.getTime() : 0;
            if (kfTime <= time) {
                before = kf;
                beforeIndex = i;
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
        
        if (interp != null && interp.isStepped()) {
            t = 0;
            return interpolateKeyframeData(before, after, t);
        } else if (interp != null && (interp.isCatmullrom() || interp.isBezier())) {
            Keyframe beforePlus = beforeIndex > 0 ? keyframes.get(beforeIndex - 1) : null;
            Keyframe afterPlus = (beforeIndex + 2) < keyframes.size() ? keyframes.get(beforeIndex + 2) : null;
            return interpolateKeyframeDataAdvanced(beforePlus, before, after, afterPlus, t, interp.getValue());
        }
        
        return interpolateKeyframeData(before, after, t);
    }
    
    private Keyframe[] interpolateKeyframeDataAdvanced(Keyframe beforePlus, Keyframe before, Keyframe after, Keyframe afterPlus, double alpha, String interpType) {
        DataPoint dpBefore = before.getDataPoints() != null && !before.getDataPoints().isEmpty() ? before.getDataPoints().get(0) : null;
        DataPoint dpAfter = after.getDataPoints() != null && !after.getDataPoints().isEmpty() ? after.getDataPoints().get(0) : null;
        
        if (dpBefore == null && dpAfter == null) return null;
        
        Keyframe result = new Keyframe();
        double time = before.getTime() + (after.getTime() - before.getTime()) * alpha;
        result.setTime(time);
        result.setChannel(before.getChannel());
        
        DataPoint interpolated = new DataPoint();
        boolean isCatmull = Interpolation.CATMULLROM.equals(interpType);
        
        if (dpBefore != null) {
            if (dpBefore.getX() != null) {
                Double val = isCatmull ? getCatmullromLerp(beforePlus, before, after, afterPlus, alpha, "x") 
                                       : getBezierLerp(before, after, alpha, "x");
                if (val != null) interpolated.setX(String.valueOf(val));
            }
            if (dpBefore.getY() != null) {
                Double val = isCatmull ? getCatmullromLerp(beforePlus, before, after, afterPlus, alpha, "y") 
                                       : getBezierLerp(before, after, alpha, "y");
                if (val != null) interpolated.setY(String.valueOf(val));
            }
            if (dpBefore.getZ() != null) {
                Double val = isCatmull ? getCatmullromLerp(beforePlus, before, after, afterPlus, alpha, "z") 
                                       : getBezierLerp(before, after, alpha, "z");
                if (val != null) interpolated.setZ(String.valueOf(val));
            }
            if (dpBefore.getW() != null) {
                Double v1 = evaluateMolangOrNumber(dpBefore.getW());
                Double v2 = dpAfter != null && dpAfter.getW() != null ? evaluateMolangOrNumber(dpAfter.getW()) : v1;
                if (v1 != null && v2 != null) {
                    interpolated.setW(String.valueOf(lerp(v1, v2, alpha)));
                } else if (v1 != null) {
                    interpolated.setW(String.valueOf(v1));
                }
            }
        }
        
        result.setDataPoints(List.of(interpolated));
        return new Keyframe[]{result};
    }
    
    private Double getCatmullromLerp(Keyframe beforePlus, Keyframe before, Keyframe after, Keyframe afterPlus, double alpha, String axis) {
        List<double[]> points = new ArrayList<>();
        
        if (beforePlus != null) {
            Double val = getAxisValue(beforePlus, axis);
            if (val != null) points.add(new double[]{beforePlus.getTime(), val});
        }
        if (before != null) {
            Double val = getAxisValue(before, axis);
            if (val != null) points.add(new double[]{before.getTime(), val});
        }
        if (after != null) {
            Double val = getAxisValue(after, axis);
            if (val != null) points.add(new double[]{after.getTime(), val});
        }
        if (afterPlus != null) {
            Double val = getAxisValue(afterPlus, axis);
            if (val != null) points.add(new double[]{afterPlus.getTime(), val});
        }
        
        if (points.size() < 2) return getAxisValue(before, axis);
        
        double t = (alpha + (beforePlus != null ? 1 : 0)) / (points.size() - 1);
        
        int p = (int) (points.size() - 1) * (int) t;
        int intPoint = (int) Math.floor(t * (points.size() - 1));
        if (intPoint < 0) intPoint = 0;
        if (intPoint > points.size() - 2) intPoint = points.size() - 2;
        
        double weight = (t * (points.size() - 1)) - intPoint;
        
        double[] p0 = points.get(intPoint == 0 ? intPoint : intPoint - 1);
        double[] p1 = points.get(intPoint);
        double[] p2 = points.get(intPoint > points.size() - 2 ? points.size() - 1 : intPoint + 1);
        double[] p3 = points.get(intPoint > points.size() - 3 ? points.size() - 1 : intPoint + 2);
        
        return catmullRom(weight, p0[1], p1[1], p2[1], p3[1]);
    }
    
    private double catmullRom(double weight, double p0, double p1, double p2, double p3) {
        double weight2 = weight * weight;
        double weight3 = weight2 * weight;
        return 0.5 * (
            (2 * p1) +
            (-p0 + p2) * weight +
            (2 * p0 - 5 * p1 + 4 * p2 - p3) * weight2 +
            (-p0 + 3 * p1 - 3 * p2 + p3) * weight3
        );
    }
    
    private Double getBezierLerp(Keyframe before, Keyframe after, double alpha, String axis) {
        int axisNum = axis.equals("x") ? 0 : axis.equals("y") ? 1 : 2;
        Double valBefore = getAxisValue(before, axis);
        Double valAfter = getAxisValue(after, axis);
        
        if (valBefore == null || valAfter == null) return valBefore;
        
        double timeGap = after.getTime() - before.getTime();
        
        Double[] rightTime = before.getBezierRightTime();
        double timeHandleBefore = rightTime != null && rightTime.length > axisNum && rightTime[axisNum] != null ? rightTime[axisNum] : 0;
        timeHandleBefore = Math.max(0, Math.min(timeGap, timeHandleBefore));
        
        Double[] leftTime = after.getBezierLeftTime();
        double timeHandleAfter = leftTime != null && leftTime.length > axisNum && leftTime[axisNum] != null ? leftTime[axisNum] : 0;
        timeHandleAfter = Math.max(-timeGap, Math.min(0, timeHandleAfter));
        
        Double[] rightVal = before.getBezierRightValue();
        double valHandleBefore = rightVal != null && rightVal.length > axisNum && rightVal[axisNum] != null ? rightVal[axisNum] : 0;
        
        Double[] leftVal = after.getBezierLeftValue();
        double valHandleAfter = leftVal != null && leftVal.length > axisNum && leftVal[axisNum] != null ? leftVal[axisNum] : 0;
        
        double p0x = before.getTime();
        double p0y = valBefore;
        
        double p1x = before.getTime() + timeHandleBefore;
        double p1y = valBefore + valHandleBefore;
        
        double p2x = after.getTime() + timeHandleAfter;
        double p2y = valAfter + valHandleAfter;
        
        double p3x = after.getTime();
        double p3y = valAfter;
        
        double targetTime = before.getTime() + (after.getTime() - before.getTime()) * alpha;
        
        // Approximate bezier curve using steps
        int steps = 50;
        double bestT = alpha;
        double minDiff = Double.MAX_VALUE;
        
        for (int i = 0; i <= steps; i++) {
            double currT = (double) i / steps;
            double currX = cubicBezier(currT, p0x, p1x, p2x, p3x);
            double diff = Math.abs(currX - targetTime);
            if (diff < minDiff) {
                minDiff = diff;
                bestT = currT;
            }
        }
        
        return cubicBezier(bestT, p0y, p1y, p2y, p3y);
    }
    
    private double cubicBezier(double t, double p0, double p1, double p2, double p3) {
        double u = 1 - t;
        double tt = t * t;
        double uu = u * u;
        double uuu = uu * u;
        double ttt = tt * t;
        
        double p = uuu * p0;
        p += 3 * uu * t * p1;
        p += 3 * u * tt * p2;
        p += ttt * p3;
        
        return p;
    }
    
    private Double getAxisValue(Keyframe kf, String axis) {
        if (kf == null || kf.getDataPoints() == null || kf.getDataPoints().isEmpty()) return null;
        DataPoint dp = kf.getDataPoints().get(0);
        String valStr = axis.equals("x") ? dp.getX() : axis.equals("y") ? dp.getY() : dp.getZ();
        return evaluateMolangOrNumber(valStr);
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
        
        boolean isRotation = "rotation".equals(before.getChannel());

        if (dpBefore != null) {
            if (dpBefore.getX() != null) {
                Double v1 = evaluateMolangOrNumber(dpBefore.getX());
                Double v2 = dpAfter != null && dpAfter.getX() != null ? evaluateMolangOrNumber(dpAfter.getX()) : v1;
                if (v1 != null && v2 != null) {
                    interpolated.setX(String.valueOf(isRotation ? lerpAngle(v1, v2, t) : lerp(v1, v2, t)));
                } else if (v1 != null) {
                    interpolated.setX(String.valueOf(v1));
                }
            }
            if (dpBefore.getY() != null) {
                Double v1 = evaluateMolangOrNumber(dpBefore.getY());
                Double v2 = dpAfter != null && dpAfter.getY() != null ? evaluateMolangOrNumber(dpAfter.getY()) : v1;
                if (v1 != null && v2 != null) {
                    interpolated.setY(String.valueOf(isRotation ? lerpAngle(v1, v2, t) : lerp(v1, v2, t)));
                } else if (v1 != null) {
                    interpolated.setY(String.valueOf(v1));
                }
            }
            if (dpBefore.getZ() != null) {
                Double v1 = evaluateMolangOrNumber(dpBefore.getZ());
                Double v2 = dpAfter != null && dpAfter.getZ() != null ? evaluateMolangOrNumber(dpAfter.getZ()) : v1;
                if (v1 != null && v2 != null) {
                    interpolated.setZ(String.valueOf(isRotation ? lerpAngle(v1, v2, t) : lerp(v1, v2, t)));
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
            BbModelDocument.Group documentGroup = findDocumentGroupByUuid(groupUuid);
            if (group != null && group.getRotation() != null) {
                transform = new Transform();
                if (group.getRotation().length >= 1) transform.setRotX(group.getRotation()[0]);
                if (group.getRotation().length >= 2) transform.setRotY(group.getRotation()[1]);
                if (group.getRotation().length >= 3) transform.setRotZ(group.getRotation()[2]);
            } else if (documentGroup != null && documentGroup.getRotation() != null) {
                transform = new Transform();
                if (documentGroup.getRotation().length >= 1) transform.setRotX(documentGroup.getRotation()[0]);
                if (documentGroup.getRotation().length >= 2) transform.setRotY(documentGroup.getRotation()[1]);
                if (documentGroup.getRotation().length >= 3) transform.setRotZ(documentGroup.getRotation()[2]);
            }
        }
        
        return transform;
    }
    
    public Map<String, Transform> getBlendedTransforms(List<AnimationBlendState> states) {
        Map<String, Transform> result = new HashMap<>();
        if (states == null || states.isEmpty()) return result;
        
        Set<String> allUuids = new HashSet<>();
        for (AnimationBlendState state : states) {
            if (state.getWeight() <= 0) continue;
            Animation animation = animationUtils.getAnimationByName(state.getAnimationName());
            if (animation != null && animation.getAnimators() != null) {
                allUuids.addAll(animation.getAnimators().keySet());
            }
        }
        
        for (String uuid : allUuids) {
            double x = 0, y = 0, z = 0;
            double rotX = 0, rotY = 0, rotZ = 0;
            double scaleX = 1, scaleY = 1, scaleZ = 1;
            double totalWeight = 0;
            
            for (AnimationBlendState state : states) {
                if (state.getWeight() <= 0) continue;
                Animation animation = animationUtils.getAnimationByName(state.getAnimationName());
                if (animation == null || animation.getAnimators() == null || !animation.getAnimators().containsKey(uuid)) continue;
                
                Transform t = getTransformAtTime(uuid, animation, state.getTime());
                if (t == null) continue;
                
                double w = state.getWeight();
                if (totalWeight == 0) {
                    x = t.getX(); y = t.getY(); z = t.getZ();
                    rotX = t.getRotX(); rotY = t.getRotY(); rotZ = t.getRotZ();
                    scaleX = t.getScaleX(); scaleY = t.getScaleY(); scaleZ = t.getScaleZ();
                    totalWeight = w;
                } else {
                    double factor = w / (totalWeight + w);
                    x = lerp(x, t.getX(), factor);
                    y = lerp(y, t.getY(), factor);
                    z = lerp(z, t.getZ(), factor);
                    rotX = lerpAngle(rotX, t.getRotX(), factor);
                    rotY = lerpAngle(rotY, t.getRotY(), factor);
                    rotZ = lerpAngle(rotZ, t.getRotZ(), factor);
                    scaleX = lerp(scaleX, t.getScaleX(), factor);
                    scaleY = lerp(scaleY, t.getScaleY(), factor);
                    scaleZ = lerp(scaleZ, t.getScaleZ(), factor);
                    totalWeight += w;
                }
            }
            
            if (totalWeight > 0) {
                if (totalWeight < 1.0) {
                    double factor = 1.0 - totalWeight;
                    x = lerp(x, 0.0, factor);
                    y = lerp(y, 0.0, factor);
                    z = lerp(z, 0.0, factor);
                    rotX = lerpAngle(rotX, 0.0, factor);
                    rotY = lerpAngle(rotY, 0.0, factor);
                    rotZ = lerpAngle(rotZ, 0.0, factor);
                    scaleX = lerp(scaleX, 1.0, factor);
                    scaleY = lerp(scaleY, 1.0, factor);
                    scaleZ = lerp(scaleZ, 1.0, factor);
                }
                Transform blended = new Transform();
                blended.setX(x); blended.setY(y); blended.setZ(z);
                blended.setRotX(rotX); blended.setRotY(rotY); blended.setRotZ(rotZ);
                blended.setScaleX(scaleX); blended.setScaleY(scaleY); blended.setScaleZ(scaleZ);
                result.put(uuid, blended);
            }
        }
        
        return result;
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
                    
                    Double[] rotation = element.getRotation();
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
        BbModelDocument.Group documentGroup = findDocumentGroupByUuid(targetUuid);
        if (group != null || documentGroup != null) {
            if (group != null && group.getRotation() != null && group.getRotation().length >= 3) {
                transform.setRotX(group.getRotation()[0]);
                transform.setRotY(group.getRotation()[1]);
                transform.setRotZ(group.getRotation()[2]);
            } else if (documentGroup != null && documentGroup.getRotation() != null && documentGroup.getRotation().length >= 3) {
                transform.setRotX(documentGroup.getRotation()[0]);
                transform.setRotY(documentGroup.getRotation()[1]);
                transform.setRotZ(documentGroup.getRotation()[2]);
            }
            if (group != null && group.getTranslation() != null && group.getTranslation().length >= 3) {
                transform.setX(group.getTranslation()[0]);
                transform.setY(group.getTranslation()[1]);
                transform.setZ(group.getTranslation()[2]);
            }
            if (group != null && group.getScale() != null && group.getScale().length >= 3) {
                transform.setScaleX(group.getScale()[0]);
                transform.setScaleY(group.getScale()[1]);
                transform.setScaleZ(group.getScale()[2]);
            }
        }
        
        return transform;
    }

    private BbModelDocument.Group findDocumentGroupByUuid(String uuid) {
        if (uuid == null || document.getGroups() == null) {
            return null;
        }
        for (BbModelDocument.Group group : document.getGroups()) {
            if (uuid.equals(group.getUuid())) {
                return group;
            }
        }
        return null;
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
        
        public double getX() { return x != null ? x : 0.0; }
        public void setX(Double x) { this.x = x; }
        
        public double getY() { return y != null ? y : 0.0; }
        public void setY(Double y) { this.y = y; }
        
        public double getZ() { return z != null ? z : 0.0; }
        public void setZ(Double z) { this.z = z; }
        
        public double getRotX() { return rotX != null ? rotX : 0.0; }
        public void setRotX(Double rotX) { this.rotX = rotX; }
        
        public double getRotY() { return rotY != null ? rotY : 0.0; }
        public void setRotY(Double rotY) { this.rotY = rotY; }
        
        public double getRotZ() { return rotZ != null ? rotZ : 0.0; }
        public void setRotZ(Double rotZ) { this.rotZ = rotZ; }
        
        public double getScaleX() { return scaleX != null ? scaleX : 1.0; }
        public void setScaleX(Double scaleX) { this.scaleX = scaleX; }
        
        public double getScaleY() { return scaleY != null ? scaleY : 1.0; }
        public void setScaleY(Double scaleY) { this.scaleY = scaleY; }
        
        public double getScaleZ() { return scaleZ != null ? scaleZ : 1.0; }
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
