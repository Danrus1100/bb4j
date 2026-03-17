package com.danrus.bb4j.api.utils;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.animation.Animation;
import com.danrus.bb4j.model.geometry.Element;
import com.danrus.bb4j.model.geometry.Face;
import com.danrus.bb4j.model.outliner.OutlinerNode;
import com.danrus.bb4j.model.outliner.OutlinerGroupNode;
import com.danrus.bb4j.api.utils.TransformUtils.Transform;

import java.util.*;

public class RenderUtils {
    
    private final BbModelDocument document;
    private final TransformUtils transformUtils;
    private final AnimationUtils animationUtils;
    private final TextureUtils textureUtils;
    private final OutlinerUtils outlinerUtils;
    
    private RenderUtils(BbModelDocument document) {
        this.document = document;
        this.transformUtils = TransformUtils.forDocument(document);
        this.animationUtils = AnimationUtils.forDocument(document);
        this.textureUtils = TextureUtils.forDocument(document);
        this.outlinerUtils = OutlinerUtils.forDocument(document);
    }
    
    public static RenderUtils forDocument(BbModelDocument document) {
        return new RenderUtils(document);
    }
    
    public List<RenderableMesh> getAllMeshes() {
        return getMeshesAtAnimationTime((Animation) null, 0);
    }
    
    public List<RenderableMesh> getMeshesAtAnimationTime(String animationName, double time) {
        Animation animation = animationUtils.getAnimationByName(animationName);
        return getMeshesAtAnimationTime(animation, time);
    }
    
    public List<RenderableMesh> getMeshesAtAnimationTime(Animation animation, double time) {
        List<RenderableMesh> meshes = new ArrayList<>();
        
        if (document.getOutliner() == null) {
            return meshes;
        }
        
        Map<String, Transform> transforms = new HashMap<>();
        if (animation != null) {
            transforms = transformUtils.getAllTransformsAtTime(animation, time);
        }
        
        processOutlinerNode(document.getOutliner(), transforms, new double[]{0, 0, 0}, new double[]{0, 0, 0}, meshes);
        
        return meshes;
    }
    
    private void processOutlinerNode(List<OutlinerNode> nodes, Map<String, Transform> transforms, double[] parentPos, double[] parentRot, List<RenderableMesh> meshes) {
        if (nodes == null) return;
        
        for (OutlinerNode node : nodes) {
            double[] pos = parentPos.clone();
            double[] rot = parentRot.clone();
            
            Transform transform = transforms.get(node.getUuid());
            if (transform != null) {
                if (transform.getX() != null) pos[0] += transform.getX();
                if (transform.getY() != null) pos[1] += transform.getY();
                if (transform.getZ() != null) pos[2] += transform.getZ();
                
                if (transform.getRotX() != null) rot[0] += transform.getRotX();
                if (transform.getRotY() != null) rot[1] += transform.getRotY();
                if (transform.getRotZ() != null) rot[2] += transform.getRotZ();
            }
            
            if (node instanceof OutlinerGroupNode) {
                OutlinerGroupNode group = (OutlinerGroupNode) node;
                
                Transform staticTransform = transformUtils.getStaticTransform(group.getUuid());
                if (staticTransform != null) {
                    if (staticTransform.getX() != null) pos[0] += staticTransform.getX();
                    if (staticTransform.getY() != null) pos[1] += staticTransform.getY();
                    if (staticTransform.getZ() != null) pos[2] += staticTransform.getZ();
                    
                    if (staticTransform.getRotX() != null) rot[0] += staticTransform.getRotX();
                    if (staticTransform.getRotY() != null) rot[1] += staticTransform.getRotY();
                    if (staticTransform.getRotZ() != null) rot[2] += staticTransform.getRotZ();
                }
                
                if (group.getChildren() != null) {
                    processOutlinerNode(group.getChildren(), transforms, pos, rot, meshes);
                }
            } else if (node.getUuid() != null && (node.getChildren() == null || node.getChildren().isEmpty())) {
                Element element = findElementByUuid(node.getUuid());
                if (element != null) {
                    RenderableMesh mesh = createMesh(element, pos, rot);
                    if (mesh != null) {
                        meshes.add(mesh);
                    }
                }
            }
        }
    }
    
    private Element findElementByUuid(String uuid) {
        if (document.getElements() == null) return null;
        return document.getElements().stream()
            .filter(e -> uuid.equals(e.getUuid()))
            .findFirst()
            .orElse(null);
    }
    
    private RenderableMesh createMesh(Element element, double[] position, double[] rotation) {
        if (element.getFrom() == null || element.getTo() == null) return null;
        
        double[] from = toDoubleArray(element.getFrom());
        double[] to = toDoubleArray(element.getTo());
        
        double[] center = new double[]{
            (from[0] + to[0]) / 2,
            (from[1] + to[1]) / 2,
            (from[2] + to[2]) / 2
        };
        
        double[] size = new double[]{
            to[0] - from[0],
            to[1] - from[1],
            to[2] - from[2]
        };
        
        String textureUuid = null;
        if (element.getFaces() != null && !element.getFaces().isEmpty()) {
            Face firstFace = element.getFaces().values().iterator().next();
            textureUuid = firstFace.getTexture();
        }
        
        RenderableMesh mesh = new RenderableMesh();
        mesh.setElementUuid(element.getUuid());
        mesh.setElementName(element.getName());
        mesh.setPosition(position);
        mesh.setRotation(rotation);
        mesh.setLocalCenter(center);
        mesh.setSize(size);
        mesh.setTextureUuid(textureUuid);
        mesh.setFaces(new ArrayList<>());
        
        if (element.getFaces() != null) {
            for (Map.Entry<String, Face> entry : element.getFaces().entrySet()) {
                RenderableFace face = createFace(entry.getKey(), entry.getValue(), from, to, center);
                if (face != null) {
                    mesh.getFaces().add(face);
                }
            }
        }
        
        return mesh;
    }
    
    private RenderableFace createFace(String faceName, Face face, double[] from, double[] to, double[] center) {
        if (face.getUv() == null) return null;
        
        RenderableFace rf = new RenderableFace();
        rf.setName(faceName);
        rf.setUv(face.getUv().getUv().clone());
        rf.setTextureUuid(face.getTexture());
        rf.setTintIndex(face.getTintindex());
        
        double[] v1, v2, v3, v4;
        
        switch (faceName) {
            case "north" -> {
                v1 = new double[]{from[0], to[1], from[2]};
                v2 = new double[]{to[0], to[1], from[2]};
                v3 = new double[]{to[0], from[1], from[2]};
                v4 = new double[]{from[0], from[1], from[2]};
            }
            case "south" -> {
                v1 = new double[]{to[0], to[1], to[2]};
                v2 = new double[]{from[0], to[1], to[2]};
                v3 = new double[]{from[0], from[1], to[2]};
                v4 = new double[]{to[0], from[1], to[2]};
            }
            case "east" -> {
                v1 = new double[]{to[0], to[1], from[2]};
                v2 = new double[]{to[0], to[1], to[2]};
                v3 = new double[]{to[0], from[1], to[2]};
                v4 = new double[]{to[0], from[1], from[2]};
            }
            case "west" -> {
                v1 = new double[]{from[0], to[1], to[2]};
                v2 = new double[]{from[0], to[1], from[2]};
                v3 = new double[]{from[0], from[1], from[2]};
                v4 = new double[]{from[0], from[1], to[2]};
            }
            case "up" -> {
                v1 = new double[]{from[0], to[1], to[2]};
                v2 = new double[]{to[0], to[1], to[2]};
                v3 = new double[]{to[0], to[1], from[2]};
                v4 = new double[]{from[0], to[1], from[2]};
            }
            case "down" -> {
                v1 = new double[]{from[0], from[1], from[2]};
                v2 = new double[]{to[0], from[1], from[2]};
                v3 = new double[]{to[0], from[1], to[2]};
                v4 = new double[]{from[0], from[1], to[2]};
            }
            default -> {
                return null;
            }
        }
        
        rf.setVertices(new double[][]{v1, v2, v3, v4});
        
        double[] normal = calculateNormal(v1, v2, v3);
        rf.setNormal(normal);
        
        return rf;
    }
    
    private double[] calculateNormal(double[] v1, double[] v2, double[] v3) {
        double[] u = new double[]{v2[0] - v1[0], v2[1] - v1[1], v2[2] - v1[2]};
        double[] v = new double[]{v3[0] - v1[0], v3[1] - v1[1], v3[2] - v1[2]};
        
        double[] normal = new double[]{
            u[1] * v[2] - u[2] * v[1],
            u[2] * v[0] - u[0] * v[2],
            u[0] * v[1] - u[1] * v[0]
        };
        
        double len = Math.sqrt(normal[0] * normal[0] + normal[1] * normal[1] + normal[2] * normal[2]);
        if (len > 0) {
            normal[0] /= len;
            normal[1] /= len;
            normal[2] /= len;
        }
        
        return normal;
    }
    
    private double[] toDoubleArray(Double[] arr) {
        if (arr == null) return new double[]{0, 0, 0};
        double[] result = new double[Math.min(arr.length, 3)];
        for (int i = 0; i < result.length; i++) {
            result[i] = arr[i] != null ? arr[i] : 0;
        }
        return result;
    }
    
    public static class RenderableMesh {
        private String elementUuid;
        private String elementName;
        private double[] position;
        private double[] rotation;
        private double[] localCenter;
        private double[] size;
        private String textureUuid;
        private List<RenderableFace> faces;
        
        public String getElementUuid() { return elementUuid; }
        public void setElementUuid(String elementUuid) { this.elementUuid = elementUuid; }
        
        public String getElementName() { return elementName; }
        public void setElementName(String elementName) { this.elementName = elementName; }
        
        public double[] getPosition() { return position; }
        public void setPosition(double[] position) { this.position = position; }
        
        public double[] getRotation() { return rotation; }
        public void setRotation(double[] rotation) { this.rotation = rotation; }
        
        public double[] getLocalCenter() { return localCenter; }
        public void setLocalCenter(double[] localCenter) { this.localCenter = localCenter; }
        
        public double[] getSize() { return size; }
        public void setSize(double[] size) { this.size = size; }
        
        public String getTextureUuid() { return textureUuid; }
        public void setTextureUuid(String textureUuid) { this.textureUuid = textureUuid; }
        
        public List<RenderableFace> getFaces() { return faces; }
        public void setFaces(List<RenderableFace> faces) { this.faces = faces; }
        
        public int getVertexCount() {
            return faces.stream().mapToInt(f -> f.getVertices().length).sum();
        }
    }
    
    public static class RenderableFace {
        private String name;
        private double[][] vertices;
        private double[] normal;
        private double[] uv;
        private String textureUuid;
        private Integer tintIndex;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public double[][] getVertices() { return vertices; }
        public void setVertices(double[][] vertices) { this.vertices = vertices; }
        
        public double[] getNormal() { return normal; }
        public void setNormal(double[] normal) { this.normal = normal; }
        
        public double[] getUv() { return uv; }
        public void setUv(double[] uv) { this.uv = uv; }
        
        public String getTextureUuid() { return textureUuid; }
        public void setTextureUuid(String textureUuid) { this.textureUuid = textureUuid; }
        
        public Integer getTintIndex() { return tintIndex; }
        public void setTintIndex(Integer tintIndex) { this.tintIndex = tintIndex; }
    }
}
