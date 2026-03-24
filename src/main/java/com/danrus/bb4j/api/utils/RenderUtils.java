package com.danrus.bb4j.api.utils;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.animation.Animation;
import com.danrus.bb4j.model.animation.AnimationBlendState;
import com.danrus.bb4j.model.geometry.Element;
import com.danrus.bb4j.model.geometry.Face;
import com.danrus.bb4j.model.geometry.MeshElement;
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
    private final double textureWidth;
    private final double textureHeight;
    
    private RenderUtils(BbModelDocument document) {
        this.document = document;
        this.transformUtils = TransformUtils.forDocument(document);
        this.animationUtils = AnimationUtils.forDocument(document);
        this.textureUtils = TextureUtils.forDocument(document);
        this.outlinerUtils = OutlinerUtils.forDocument(document);
        
        double tw = 16.0;
        double th = 16.0;
        if (document.getResolution() != null) {
            if (document.getResolution().getWidth() != null) tw = document.getResolution().getWidth();
            if (document.getResolution().getHeight() != null) th = document.getResolution().getHeight();
        }
        this.textureWidth = tw;
        this.textureHeight = th;
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
    
    public List<RenderableMesh> getBlendedMeshes(List<AnimationBlendState> states) {
        List<RenderableMesh> meshes = new ArrayList<>();
        if (document.getOutliner() == null) {
            return meshes;
        }
        
        Map<String, Transform> transforms = transformUtils.getBlendedTransforms(states);
        processOutlinerNode(document.getOutliner(), transforms, new double[]{0, 0, 0}, new double[]{0, 0, 0}, new double[]{1, 1, 1}, null, meshes);
        return meshes;
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
        
        processOutlinerNode(document.getOutliner(), transforms, new double[]{0, 0, 0}, new double[]{0, 0, 0}, new double[]{1, 1, 1}, null, meshes);
        
        return meshes;
    }
    

    private void processOutlinerNode(List<OutlinerNode> nodes, Map<String, Transform> transforms, double[] parentPos, double[] parentRot, double[] parentScale, double[] parentOrigin, List<RenderableMesh> meshes) {
        if (nodes == null) return;
        
        for (OutlinerNode node : nodes) {
            double[] pos = parentPos.clone();
            double[] rot = parentRot.clone();
            double[] scale = parentScale.clone();
            
            Transform transform = transforms.get(node.getUuid());
            if (transform != null) {
                if (transform.getX() != null) pos[0] += transform.getX();
                if (transform.getY() != null) pos[1] += transform.getY();
                if (transform.getZ() != null) pos[2] += transform.getZ();
                
                if (transform.getRotX() != null) rot[0] += transform.getRotX();
                if (transform.getRotY() != null) rot[1] += transform.getRotY();
                if (transform.getRotZ() != null) rot[2] += transform.getRotZ();

                if (transform.getScaleX() != null) scale[0] *= transform.getScaleX();
                if (transform.getScaleY() != null) scale[1] *= transform.getScaleY();
                if (transform.getScaleZ() != null) scale[2] *= transform.getScaleZ();
            }

            if (node instanceof OutlinerGroupNode) {
                OutlinerGroupNode group = (OutlinerGroupNode) node;

                double[] groupOrigin = parentOrigin;
                if (document.getGroups() != null) {
                    for (com.danrus.bb4j.model.BbModelDocument.Group g : document.getGroups()) {
                        if (g.getUuid().equals(group.getUuid())) {
                            Double[] originObj = g.getOrigin();
                            if (originObj != null && originObj.length >= 3) {
                                groupOrigin = new double[]{originObj[0], originObj[1], originObj[2]};
                            }
                            break;
                        }
                    }
                }

                Transform staticTransform = transformUtils.getStaticTransform(group.getUuid());
                if (staticTransform != null) {
                    if (staticTransform.getX() != null) pos[0] += staticTransform.getX();
                    if (staticTransform.getY() != null) pos[1] += staticTransform.getY();
                    if (staticTransform.getZ() != null) pos[2] += staticTransform.getZ();

                    if (staticTransform.getRotX() != null) rot[0] += staticTransform.getRotX();
                    if (staticTransform.getRotY() != null) rot[1] += staticTransform.getRotY();
                    if (staticTransform.getRotZ() != null) rot[2] += staticTransform.getRotZ();
                    
                    if (staticTransform.getScaleX() != null) scale[0] *= staticTransform.getScaleX();
                    if (staticTransform.getScaleY() != null) scale[1] *= staticTransform.getScaleY();
                    if (staticTransform.getScaleZ() != null) scale[2] *= staticTransform.getScaleZ();
                }
                
                if (group.getChildren() != null) {
                    processOutlinerNode(group.getChildren(), transforms, pos, rot, scale, groupOrigin, meshes);
                }
            } else if (node.getUuid() != null && (node.getChildren() == null || node.getChildren().isEmpty())) {
                Element element = findElementByUuid(node.getUuid());
                if (element != null) {
                    double[] elementRot = rot.clone();
                    double[] elementScale = scale.clone();
                    Transform staticTransform = transformUtils.getStaticTransform(element.getUuid());
                    if (staticTransform != null) {
                        if (staticTransform.getRotX() != null) elementRot[0] += staticTransform.getRotX();
                        if (staticTransform.getRotY() != null) elementRot[1] += staticTransform.getRotY();
                        if (staticTransform.getRotZ() != null) elementRot[2] += staticTransform.getRotZ();

                        if (staticTransform.getScaleX() != null) elementScale[0] *= staticTransform.getScaleX();
                        if (staticTransform.getScaleY() != null) elementScale[1] *= staticTransform.getScaleY();
                        if (staticTransform.getScaleZ() != null) elementScale[2] *= staticTransform.getScaleZ();
                    }

                    RenderableMesh mesh = createMesh(element, pos, elementRot, elementScale, parentOrigin);
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
    
    private RenderableMesh createMesh(Element element, double[] position, double[] rotation, double[] scale, double[] groupOrigin) {
        if (element instanceof MeshElement meshElement) {
            return createMeshFromMeshElement(meshElement, position, rotation, scale, groupOrigin);
        }

        if (element.getFrom() == null || element.getTo() == null) return null;
        
        double[] from = toDoubleArray(element.getFrom());
        double[] to = toDoubleArray(element.getTo());

        double inflate = element.getInflate() != null ? element.getInflate() : 0.0;
        if (inflate != 0.0) {
            from[0] -= inflate;
            from[1] -= inflate;
            from[2] -= inflate;
            to[0] += inflate;
            to[1] += inflate;
            to[2] += inflate;
        }
        
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
        mesh.setScale(scale);
        mesh.setLocalCenter(center);
        
        // If the element has no origin, use the group origin as the pivot
        double[] elementOrigin = element.getOrigin() != null ? toDoubleArray(element.getOrigin()) : groupOrigin;
        if (elementOrigin == null) {
            elementOrigin = center;
        }
        mesh.setLocalOrigin(elementOrigin);

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

    private RenderableMesh createMeshFromMeshElement(MeshElement element, double[] position, double[] rotation, double[] scale, double[] groupOrigin) {
        if (element.getVertices() == null || element.getVertices().isEmpty() || element.getMeshFaces() == null || element.getMeshFaces().isEmpty()) {
            return null;
        }

        RenderableMesh mesh = new RenderableMesh();
        mesh.setElementUuid(element.getUuid());
        mesh.setElementName(element.getName());
        mesh.setPosition(position);
        mesh.setRotation(rotation);
        mesh.setScale(scale);
        mesh.setLocalCenter(new double[]{0, 0, 0});
        
        double[] elementOrigin = element.getOrigin() != null ? toDoubleArray(element.getOrigin()) : groupOrigin;
        if (elementOrigin == null) {
            elementOrigin = new double[]{0, 0, 0};
        }
        mesh.setLocalOrigin(elementOrigin);
        
        mesh.setSize(new double[]{0, 0, 0});
        mesh.setTextureUuid(null);
        mesh.setFaces(new ArrayList<>());

        for (MeshElement.MeshFaceData faceData : element.getMeshFaces()) {
            List<RenderableFace> faces = createFacesFromMeshData(faceData, element.getVertices(), elementOrigin);
            for (RenderableFace face : faces) {
                mesh.getFaces().add(face);
                if (mesh.getTextureUuid() == null && face.getTextureUuid() != null) {
                    mesh.setTextureUuid(face.getTextureUuid());
                }
            }
        }

        return mesh.getFaces().isEmpty() ? null : mesh;
    }

    private List<RenderableFace> createFacesFromMeshData(MeshElement.MeshFaceData faceData, Map<String, Double[]> vertices, double[] origin) {
        List<RenderableFace> result = new ArrayList<>();
        if (faceData.getVertices() == null || faceData.getVertices().size() < 3) {
            return result;
        }

        List<String> vertexKeys = faceData.getVertices();
        for (int i = 1; i < vertexKeys.size() - 1; i++) {
            String k0 = vertexKeys.get(0);
            String k1 = vertexKeys.get(i);
            String k2 = vertexKeys.get(i + 1);

            Double[] p0 = vertices.get(k0);
            Double[] p1 = vertices.get(k1);
            Double[] p2 = vertices.get(k2);
            if (p0 == null || p1 == null || p2 == null || p0.length < 3 || p1.length < 3 || p2.length < 3) {
                continue;
            }

            double ox = origin != null && origin.length > 0 ? origin[0] : 0.0;
            double oy = origin != null && origin.length > 1 ? origin[1] : 0.0;
            double oz = origin != null && origin.length > 2 ? origin[2] : 0.0;

            double[][] renderVertices = new double[][]{
                new double[]{p0[0] + ox, p0[1] + oy, p0[2] + oz},
                new double[]{p1[0] + ox, p1[1] + oy, p1[2] + oz},
                new double[]{p2[0] + ox, p2[1] + oy, p2[2] + oz},
                new double[]{p2[0] + ox, p2[1] + oy, p2[2] + oz}
            };

            double[][] vertexUvs = new double[][]{
                getMeshUv(faceData, k0),
                getMeshUv(faceData, k1),
                getMeshUv(faceData, k2),
                getMeshUv(faceData, k2)
            };

            RenderableFace face = new RenderableFace();
            String baseName = faceData.getId() != null ? faceData.getId() : "mesh_face";
            face.setName(baseName + "_tri_" + i);
            face.setVertices(renderVertices);
            face.setVertexUvs(vertexUvs);
            face.setUv(new double[]{0, 0, 1, 1});
            face.setTextureUuid(faceData.getTexture());
            face.setTintIndex(null);
            face.setNormal(calculateNormal(renderVertices[0], renderVertices[1], renderVertices[2]));
            face.setLocalCenter(calculateFaceCenter(renderVertices));
            result.add(face);
        }

        return result;
    }

    private double[] getMeshUv(MeshElement.MeshFaceData faceData, String vertexKey) {
        if (faceData.getUvByVertex() == null) {
            return new double[]{0, 0};
        }
        Double[] uv = faceData.getUvByVertex().get(vertexKey);
        if (uv == null || uv.length < 2) {
            return new double[]{0, 0};
        }
        return new double[]{uv[0] / textureWidth, uv[1] / textureHeight};
    }
    
    private RenderableFace createFace(String faceName, Face face, double[] from, double[] to, double[] center) {
        if (face.getUv() == null) return null;
        
        RenderableFace rf = new RenderableFace();
        rf.setName(faceName);
        double[] originalUv = face.getUv().getUv();
        if (originalUv != null && originalUv.length >= 4) {
            rf.setUv(new double[]{
                originalUv[0] / textureWidth,
                originalUv[1] / textureHeight,
                originalUv[2] / textureWidth,
                originalUv[3] / textureHeight
            });
        } else {
            rf.setUv(new double[]{0, 0, 1, 1});
        }
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
        rf.setLocalCenter(calculateFaceCenter(rf.getVertices()));
        
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

    private double[] calculateFaceCenter(double[][] vertices) {
        if (vertices == null || vertices.length == 0) {
            return new double[]{0, 0, 0};
        }

        double centerX = 0.0;
        double centerY = 0.0;
        double centerZ = 0.0;
        for (double[] vertex : vertices) {
            centerX += vertex[0];
            centerY += vertex[1];
            centerZ += vertex[2];
        }

        double inv = 1.0 / vertices.length;
        return new double[]{centerX * inv, centerY * inv, centerZ * inv};
    }
    
    private double[] toDoubleArray(Double[] arr) {
        double[] result = new double[]{0, 0, 0};
        if (arr == null) {
            return result;
        }
        for (int i = 0; i < Math.min(arr.length, 3); i++) {
            result[i] = arr[i] != null ? arr[i] : 0;
        }
        return result;
    }
    
    public static class RenderableMesh {
        private String elementUuid;
        private String elementName;
        private double[] position;
        private double[] rotation;
        private double[] scale;
        private double[] localCenter;
        private double[] localOrigin;
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

        public double[] getScale() { return scale; }
        public void setScale(double[] scale) { this.scale = scale; }
        
        public double[] getLocalCenter() { return localCenter; }
        public void setLocalCenter(double[] localCenter) { this.localCenter = localCenter; }

        public double[] getLocalOrigin() { return localOrigin; }
        public void setLocalOrigin(double[] localOrigin) { this.localOrigin = localOrigin; }
        
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
        private double[][] vertexUvs;
        private double[] localCenter;
        private double[] normal;
        private double[] uv;
        private String textureUuid;
        private Integer tintIndex;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public double[][] getVertices() { return vertices; }
        public void setVertices(double[][] vertices) { this.vertices = vertices; }

        public double[][] getVertexUvs() { return vertexUvs; }
        public void setVertexUvs(double[][] vertexUvs) { this.vertexUvs = vertexUvs; }

        public double[] getLocalCenter() { return localCenter; }
        public void setLocalCenter(double[] localCenter) { this.localCenter = localCenter; }
        
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
