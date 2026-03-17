package com.danrus.bb4j.model.geometry;

public class CubeElement extends Element {

    public CubeElement() {
        super(Element.CUBE);
    }

    public CubeElement(Double[] from, Double[] to) {
        super(Element.CUBE);
        setFrom(from);
        setTo(to);
    }
}
