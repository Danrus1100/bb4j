package com.danrus.bb4j.api;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.animation.*;
import com.danrus.bb4j.model.outliner.OutlinerNode;
import com.danrus.bb4j.model.outliner.OutlinerGroupNode;
import com.danrus.bb4j.molang.MolangEvaluator;
import com.danrus.bb4j.api.utils.AnimationUtils;

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
        
        List<Keyframe> keyframes = animator.getKeyframes();
        
        for (Keyframe keyframe : keyframes) {
            String channel = keyframe.getChannel();
            if (channel == null) continue;
            
            List<DataPoint> dataPoints = keyframe.getDataPoints();
            if (dataPoints == null || dataPoints.isEmpty()) continue;
            
            DataPoint dataPoint = interpolateDataPoint(keyframe.getTime(), dataPoints, time);
            if (dataPoint == null) continue;
            
            switch (channel) {
                case "position" -> {
                    if (dataPoint.getX() != null) transform.setX(evaluateOrUseStatic(dataPoint.getX(), transform.getX()));
                    if (dataPoint.getY() != null) transform.setY(evaluateOrUseStatic(dataPoint.getY(), transform.getY()));
                    if (dataPoint.getZ() != null) transform.setZ(evaluateOrUseStatic(dataPoint.getZ(), transform.getZ()));
                }
                case "rotation" -> {
                    if (dataPoint.getX() != null) transform.setRotX(evaluateOrUseStatic(dataPoint.getX(), transform.getRotX()));
                    if (dataPoint.getY() != null) transform.setRotY(evaluateOrUseStatic(dataPoint.getY(), transform.getRotY()));
                    if (dataPoint.getZ() != null) transform.setRotZ(evaluateOrUseStatic(dataPoint.getZ(), transform.getRotZ()));
                }
                case "scale" -> {
                    if (dataPoint.getX() != null) transform.setScaleX(evaluateOrUseStatic(dataPoint.getX(), transform.getScaleX()));
                    if (dataPoint.getY() != null) transform.setScaleY(evaluateOrUseStatic(dataPoint.getY(), transform.getScaleY()));
                    if (dataPoint.getZ() != null) transform.setScaleZ(evaluateOrUseStatic(dataPoint.getZ(), transform.getScaleZ()));
                }
            }
        }
        
        return transform.hasAnyValue() ? transform : getStaticTransform(targetUuid);
    }
    
    private Double evaluateOrUseStatic(String molangValue, Double current) {
        if (molangValue == null) return current;
        
        if (isNumericString(molangValue)) {
            return parseDouble(molangValue);
        }
        
        if (molangEvaluator != null) {
            try {
                Double result = molangEvaluator.evaluateOrNull(molangValue);
                if (result != null) return result;
            } catch (Exception ignored) {}
        }
        
        return current;
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
    
    private Double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private DataPoint interpolateDataPoint(double keyframeTime, List<DataPoint> dataPoints, double currentTime) {
        if (dataPoints.isEmpty()) return null;
        
        if (dataPoints.size() == 1) {
            return dataPoints.get(0);
        }
        
        if (currentTime <= keyframeTime) {
            return dataPoints.get(0);
        }
        
        return dataPoints.get(dataPoints.size() - 1);
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
