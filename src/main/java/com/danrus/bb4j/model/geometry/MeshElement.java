package com.danrus.bb4j.model.geometry;

import java.util.List;
import java.util.Map;

/**
 * A free-form polygonal {@link Element}: a set of named {@code vertices} (each an
 * {@code [x, y, z]} point) plus {@link MeshFaceData polygonal faces} that
 * reference those vertices and carry per-vertex UV coordinates.
 *
 * <p>Note that mesh faces are stored separately from the cube {@link Face} map of
 * the base class; the reader/writer pick the right representation from the element
 * {@code type}.
 */
public class MeshElement extends Element {
    private Map<String, Double[]> vertices;
    private List<MeshFaceData> meshFaces;

    public MeshElement() {
        super(Element.MESH);
    }

    public Map<String, Double[]> getVertices() {
        return vertices;
    }

    public void setVertices(Map<String, Double[]> vertices) {
        this.vertices = vertices;
    }

    public List<MeshFaceData> getMeshFaces() {
        return meshFaces;
    }

    public void setMeshFaces(List<MeshFaceData> meshFaces) {
        this.meshFaces = meshFaces;
    }

    public static class MeshFaceData {
        private String id;
        private List<String> vertices;
        private Map<String, Double[]> uvByVertex;
        private String texture;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<String> getVertices() {
            return vertices;
        }

        public void setVertices(List<String> vertices) {
            this.vertices = vertices;
        }

        public Map<String, Double[]> getUvByVertex() {
            return uvByVertex;
        }

        public void setUvByVertex(Map<String, Double[]> uvByVertex) {
            this.uvByVertex = uvByVertex;
        }

        public String getTexture() {
            return texture;
        }

        public void setTexture(String texture) {
            this.texture = texture;
        }
    }
}
