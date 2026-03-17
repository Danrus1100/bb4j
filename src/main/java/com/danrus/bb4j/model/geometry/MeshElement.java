package com.danrus.bb4j.model.geometry;

import java.util.List;

public class MeshElement extends Element {
    private List<Float[]> vertices;
    private List<Integer[]> meshFaces;

    public MeshElement() {
        super(Element.MESH);
    }

    public List<Float[]> getVertices() {
        return vertices;
    }

    public void setVertices(List<Float[]> vertices) {
        this.vertices = vertices;
    }

    public List<Integer[]> getMeshFaces() {
        return meshFaces;
    }

    public void setMeshFaces(List<Integer[]> meshFaces) {
        this.meshFaces = meshFaces;
    }
}
