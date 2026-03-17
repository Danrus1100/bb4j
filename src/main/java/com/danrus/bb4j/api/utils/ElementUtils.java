package com.danrus.bb4j.api.utils;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.geometry.Element;
import com.danrus.bb4j.model.outliner.OutlinerNode;
import com.danrus.bb4j.model.outliner.OutlinerGroupNode;

import java.util.*;
import java.util.stream.Collectors;

public class ElementUtils {
    
    private final BbModelDocument document;
    
    private ElementUtils(BbModelDocument document) {
        this.document = document;
    }
    
    public static ElementUtils forDocument(BbModelDocument document) {
        return new ElementUtils(document);
    }
    
    public List<Element> getAllElements() {
        return document.getElements() != null 
            ? new ArrayList<>(document.getElements()) 
            : Collections.emptyList();
    }
    
    public Element getElementByUuid(String uuid) {
        if (uuid == null || document.getElements() == null) {
            return null;
        }
        return document.getElements().stream()
            .filter(e -> uuid.equals(e.getUuid()))
            .findFirst()
            .orElse(null);
    }
    
    public Element getElementByName(String name) {
        if (name == null || document.getElements() == null) {
            return null;
        }
        return document.getElements().stream()
            .filter(e -> name.equals(e.getName()))
            .findFirst()
            .orElse(null);
    }
    
    public List<Element> getElementsByNamePrefix(String prefix) {
        if (prefix == null || document.getElements() == null) {
            return Collections.emptyList();
        }
        return document.getElements().stream()
            .filter(e -> e.getName() != null && e.getName().startsWith(prefix))
            .collect(Collectors.toList());
    }
    
    public List<Element> getCubes() {
        if (document.getElements() == null) {
            return Collections.emptyList();
        }
        return document.getElements().stream()
            .filter(Element::isCube)
            .collect(Collectors.toList());
    }
    
    public List<Element> getMeshes() {
        if (document.getElements() == null) {
            return Collections.emptyList();
        }
        return document.getElements().stream()
            .filter(Element::isMesh)
            .collect(Collectors.toList());
    }
    
    public List<Element> getElementsInGroup(String groupUuid) {
        if (groupUuid == null || document.getOutliner() == null) {
            return Collections.emptyList();
        }
        
        List<String> elementUuids = new ArrayList<>();
        findElementUuidsInGroup(document.getOutliner(), groupUuid, elementUuids);
        
        return getAllElements().stream()
            .filter(e -> elementUuids.contains(e.getUuid()))
            .collect(Collectors.toList());
    }
    
    private void findElementUuidsInGroup(List<OutlinerNode> nodes, String targetGroupUuid, List<String> elementUuids) {
        if (nodes == null) return;
        
        for (OutlinerNode node : nodes) {
            if (targetGroupUuid.equals(node.getUuid())) {
                collectElementUuids(node, elementUuids);
                return;
            }
            if (node.getChildren() != null) {
                findElementUuidsInGroup(node.getChildren(), targetGroupUuid, elementUuids);
            }
        }
    }
    
    private void collectElementUuids(OutlinerNode node, List<String> elementUuids) {
        if (node instanceof OutlinerGroupNode) {
            if (node.getChildren() != null) {
                for (OutlinerNode child : node.getChildren()) {
                    collectElementUuids(child, elementUuids);
                }
            }
        } else if (node.getUuid() != null && (node.getChildren() == null || node.getChildren().isEmpty())) {
            elementUuids.add(node.getUuid());
        }
    }
    
    public Map<String, List<Element>> getElementsGroupedByType() {
        Map<String, List<Element>> result = new HashMap<>();
        result.put("cube", getCubes());
        result.put("mesh", getMeshes());
        return result;
    }
    
    public int getTotalElementCount() {
        return getAllElements().size();
    }
    
    public double getTotalVolume() {
        return getAllElements().stream()
            .mapToDouble(e -> e.getWidth() * e.getHeight() * e.getDepth())
            .sum();
    }
    
    public double[] getModelBounds() {
        if (document.getElements() == null || document.getElements().isEmpty()) {
            return new double[]{0, 0, 0, 0, 0, 0};
        }
        
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE, maxZ = Double.MIN_VALUE;
        
        for (Element element : document.getElements()) {
            Double[] from = element.getFrom();
            Double[] to = element.getTo();
            
            if (from != null && to != null) {
                minX = Math.min(minX, from[0]);
                minY = Math.min(minY, from[1]);
                minZ = Math.min(minZ, from[2]);
                maxX = Math.max(maxX, to[0]);
                maxY = Math.max(maxY, to[1]);
                maxZ = Math.max(maxZ, to[2]);
            }
        }
        
        return new double[]{minX, minY, minZ, maxX, maxY, maxZ};
    }
    
    public double[] getModelCenter() {
        double[] bounds = getModelBounds();
        return new double[]{
            (bounds[0] + bounds[3]) / 2,
            (bounds[1] + bounds[4]) / 2,
            (bounds[2] + bounds[5]) / 2
        };
    }
    
    public double getModelWidth() {
        double[] bounds = getModelBounds();
        return bounds[3] - bounds[0];
    }
    
    public double getModelHeight() {
        double[] bounds = getModelBounds();
        return bounds[4] - bounds[1];
    }
    
    public double getModelDepth() {
        double[] bounds = getModelBounds();
        return bounds[5] - bounds[2];
    }
}
