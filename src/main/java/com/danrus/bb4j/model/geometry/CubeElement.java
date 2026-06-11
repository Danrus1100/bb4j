package com.danrus.bb4j.model.geometry;

/**
 * A box-shaped {@link Element} defined by its {@code from} and {@code to}
 * corners, with up to six named {@link Face}s (north/south/east/west/up/down).
 */
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
