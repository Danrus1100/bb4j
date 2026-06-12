package com.danrus.bb4j.api.utils;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.outliner.OutlinerNode;
import com.danrus.bb4j.model.outliner.OutlinerGroupNode;
import com.danrus.bb4j.model.outliner.OutlinerElementRefNode;

import java.util.*;
import java.util.stream.Collectors;

public class OutlinerUtils {

    private final BbModelDocument document;
    /** Lazily built {@code uuid -> group} index; see {@link #getGroupByUuid}. */
    private Map<String, OutlinerGroupNode> groupIndex;

    private OutlinerUtils(BbModelDocument document) {
        this.document = document;
    }

    public static OutlinerUtils forDocument(BbModelDocument document) {
        return new OutlinerUtils(document);
    }

    public List<OutlinerNode> getRootNodes() {
        return document.getOutliner() != null
            ? new ArrayList<>(document.getOutliner())
            : Collections.emptyList();
    }

    public List<OutlinerGroupNode> getAllGroups() {
        List<OutlinerGroupNode> groups = new ArrayList<>();
        OutlinerWalk.preOrder(document.getOutliner(), node -> {
            if (node instanceof OutlinerGroupNode group) {
                groups.add(group);
            }
        });
        return groups;
    }

    /**
     * Looks up a group by uuid using a lazily built index. The index is cached on
     * this instance, so obtain a fresh {@code OutlinerUtils} after mutating the
     * outliner tree.
     */
    public OutlinerGroupNode getGroupByUuid(String uuid) {
        if (uuid == null || document.getOutliner() == null) {
            return null;
        }
        if (groupIndex == null) {
            groupIndex = new HashMap<>();
            for (OutlinerGroupNode group : getAllGroups()) {
                if (group.getUuid() != null) {
                    groupIndex.putIfAbsent(group.getUuid(), group);
                }
            }
        }
        return groupIndex.get(uuid);
    }

    public OutlinerGroupNode getGroupByName(String name) {
        if (name == null) {
            return null;
        }
        return getAllGroups().stream()
            .filter(g -> name.equals(g.getName()))
            .findFirst()
            .orElse(null);
    }

    public List<OutlinerNode> getAllDescendants(OutlinerGroupNode group) {
        List<OutlinerNode> result = new ArrayList<>();
        if (group != null) {
            OutlinerWalk.preOrder(group.getChildren(), result::add);
        }
        return result;
    }

    public List<OutlinerElementRefNode> getAllElementRefs() {
        List<OutlinerElementRefNode> refs = new ArrayList<>();
        OutlinerWalk.preOrder(document.getOutliner(), node -> {
            if (node instanceof OutlinerElementRefNode ref) {
                refs.add(ref);
            }
        });
        return refs;
    }

    public List<String> getElementUuidsInGroup(String groupUuid) {
        OutlinerGroupNode group = getGroupByUuid(groupUuid);
        if (group == null) return Collections.emptyList();

        List<String> uuids = new ArrayList<>();
        OutlinerWalk.preOrder(group.getChildren(), node -> {
            if (node instanceof OutlinerElementRefNode ref && ref.getElementUuid() != null) {
                uuids.add(ref.getElementUuid());
            }
        });
        return uuids;
    }
    
    public Map<String, List<String>> getGroupHierarchy() {
        Map<String, List<String>> hierarchy = new HashMap<>();
        
        for (OutlinerGroupNode group : getAllGroups()) {
            List<String> children = new ArrayList<>();
            if (group.getChildren() != null) {
                for (OutlinerNode child : group.getChildren()) {
                    children.add(child.getUuid());
                }
            }
            hierarchy.put(group.getUuid(), children);
        }
        
        return hierarchy;
    }
    
    public int getTotalGroupCount() {
        return getAllGroups().size();
    }
    
    public int getMaxNestingDepth() {
        if (document.getOutliner() == null) return 0;
        
        int maxDepth = 0;
        for (OutlinerNode node : document.getOutliner()) {
            maxDepth = Math.max(maxDepth, calculateDepth(node, 1));
        }
        return maxDepth;
    }
    
    private int calculateDepth(OutlinerNode node, int currentDepth) {
        if (node == null || node.getChildren() == null || node.getChildren().isEmpty()) {
            return currentDepth;
        }
        
        int maxChildDepth = currentDepth;
        for (OutlinerNode child : node.getChildren()) {
            maxChildDepth = Math.max(maxChildDepth, calculateDepth(child, currentDepth + 1));
        }
        return maxChildDepth;
    }
    
    public List<OutlinerNode> getPathToNode(String targetUuid) {
        List<OutlinerNode> path = new ArrayList<>();
        if (document.getOutliner() != null) {
            findPath(document.getOutliner(), targetUuid, path);
        }
        return path;
    }
    
    private boolean findPath(List<OutlinerNode> nodes, String targetUuid, List<OutlinerNode> path) {
        if (nodes == null) return false;
        
        for (OutlinerNode node : nodes) {
            path.add(node);
            
            if (targetUuid.equals(node.getUuid())) {
                return true;
            }
            
            if (node.getChildren() != null && findPath(node.getChildren(), targetUuid, path)) {
                return true;
            }
            
            path.remove(path.size() - 1);
        }
        
        return false;
    }
    
    public String getGroupPath(String groupUuid) {
        List<OutlinerNode> path = getPathToNode(groupUuid);
        if (path.isEmpty()) return null;
        
        return path.stream()
            .map(n -> n.getName() != null ? n.getName() : n.getUuid())
            .collect(Collectors.joining("/"));
    }
}
