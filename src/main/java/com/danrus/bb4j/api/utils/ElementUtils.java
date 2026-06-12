package com.danrus.bb4j.api.utils;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.geometry.Element;
import com.danrus.bb4j.model.geometry.MeshElement;

import java.util.*;
import java.util.stream.Collectors;

public class ElementUtils {

    private final BbModelDocument document;
    /** Lazily built {@code uuid -> element} index; see {@link #getElementByUuid}. */
    private Map<String, Element> elementIndex;

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

    /**
     * Looks up an element by uuid using a lazily built index. The index is cached
     * on this instance, so obtain a fresh {@code ElementUtils} after adding or
     * removing elements.
     */
    public Element getElementByUuid(String uuid) {
        if (uuid == null || document.getElements() == null) {
            return null;
        }
        if (elementIndex == null) {
            elementIndex = new HashMap<>();
            for (Element e : document.getElements()) {
                if (e.getUuid() != null) {
                    elementIndex.putIfAbsent(e.getUuid(), e);
                }
            }
        }
        return elementIndex.get(uuid);
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
        if (groupUuid == null) {
            return Collections.emptyList();
        }
        // The outliner already knows how to resolve a group's element references;
        // reuse it rather than re-walking the tree here.
        Set<String> elementUuids =
            new HashSet<>(OutlinerUtils.forDocument(document).getElementUuidsInGroup(groupUuid));
        if (elementUuids.isEmpty()) {
            return Collections.emptyList();
        }
        return getAllElements().stream()
            .filter(e -> elementUuids.contains(e.getUuid()))
            .collect(Collectors.toList());
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
    
    /**
     * Sum of each element's axis-aligned bounding-box volume. Cubes use their
     * {@code from}/{@code to} extents; meshes use the bounding box of their
     * vertices (so mesh models are no longer counted as zero volume).
     */
    public double getTotalVolume() {
        double total = 0;
        for (Element element : getAllElements()) {
            Bounds b = new Bounds();
            accumulate(element, b);
            total += b.volume();
        }
        return total;
    }

    /**
     * Axis-aligned bounding box of the whole model as
     * {@code [minX, minY, minZ, maxX, maxY, maxZ]}, or all zeros when there is no
     * positioned geometry. Cube extents come from {@code from}/{@code to}; mesh
     * extents come from each vertex offset by the element {@code origin}. Element
     * rotation and {@code inflate} are not applied.
     */
    public double[] getModelBounds() {
        Bounds bounds = new Bounds();
        for (Element element : getAllElements()) {
            accumulate(element, bounds);
        }
        return bounds.toArray();
    }

    /** Adds an element's corners (cube) or vertices (mesh) to {@code bounds}. */
    private static void accumulate(Element element, Bounds bounds) {
        if (element instanceof MeshElement mesh && mesh.getVertices() != null
                && !mesh.getVertices().isEmpty()) {
            Double[] origin = element.getOrigin();
            double ox = component(origin, 0), oy = component(origin, 1), oz = component(origin, 2);
            for (Double[] v : mesh.getVertices().values()) {
                if (isPoint(v)) {
                    bounds.include(ox + v[0], oy + v[1], oz + v[2]);
                }
            }
            return;
        }
        Double[] from = element.getFrom();
        Double[] to = element.getTo();
        if (isPoint(from) && isPoint(to)) {
            bounds.include(from[0], from[1], from[2]);
            bounds.include(to[0], to[1], to[2]);
        }
    }

    private static boolean isPoint(Double[] v) {
        return v != null && v.length >= 3 && v[0] != null && v[1] != null && v[2] != null
                && Double.isFinite(v[0]) && Double.isFinite(v[1]) && Double.isFinite(v[2]);
    }

    private static double component(Double[] v, int i) {
        return v != null && v.length > i && v[i] != null ? v[i] : 0.0;
    }

    /** Mutable axis-aligned bounding-box accumulator. */
    private static final class Bounds {
        private double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        private double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

        void include(double x, double y, double z) {
            minX = Math.min(minX, x); maxX = Math.max(maxX, x);
            minY = Math.min(minY, y); maxY = Math.max(maxY, y);
            minZ = Math.min(minZ, z); maxZ = Math.max(maxZ, z);
        }

        boolean isEmpty() {
            return minX > maxX;
        }

        double volume() {
            return isEmpty() ? 0 : (maxX - minX) * (maxY - minY) * (maxZ - minZ);
        }

        double[] toArray() {
            return isEmpty()
                ? new double[]{0, 0, 0, 0, 0, 0}
                : new double[]{minX, minY, minZ, maxX, maxY, maxZ};
        }
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
