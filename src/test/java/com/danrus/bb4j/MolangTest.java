package com.danrus.bb4j;

import com.danrus.bb4j.molang.MolangEvaluator;
import com.danrus.bb4j.molang.MolangInverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MolangTest {

    // -------------------------------------------------------------------------
    // MolangEvaluator tests
    // -------------------------------------------------------------------------

    @Test
    void plainNumbers() {
        MolangEvaluator eval = new MolangEvaluator();
        assertEquals(0.0, eval.evaluate("0"), 1e-9);
        assertEquals(42.0, eval.evaluate("42"), 1e-9);
        assertEquals(-5.0, eval.evaluate("-5"), 1e-9);
        assertEquals(3.14, eval.evaluate("3.14"), 1e-9);
    }

    @Test
    void basicArithmetic() {
        MolangEvaluator eval = new MolangEvaluator();
        assertEquals(5.0, eval.evaluate("2 + 3"), 1e-9);
        assertEquals(5.0, eval.evaluate("10 / 2"), 1e-9);
        assertEquals(7.0, eval.evaluate("2 * 3 + 1"), 1e-9);
        assertEquals(-5.0, eval.evaluate("-5"), 1e-9);
    }

    @Test
    void mathPrefixMustBeRecognized() {
        MolangEvaluator eval = new MolangEvaluator();
        assertNotEquals(0.0, eval.evaluate("math.sin(90)"),
                "math. prefix must be recognized: math.sin(90) should be non-zero");
    }

    @Test
    void mathSin() {
        MolangEvaluator eval = new MolangEvaluator();
        // sin takes degrees; sin(90°) = 1
        assertEquals(1.0, eval.evaluate("math.sin(90)"), 1e-9);
    }

    @Test
    void mathCos() {
        MolangEvaluator eval = new MolangEvaluator();
        assertEquals(1.0, eval.evaluate("math.cos(0)"), 1e-9);
    }

    @Test
    void mathAbs() {
        MolangEvaluator eval = new MolangEvaluator();
        assertEquals(4.0, eval.evaluate("math.abs(-4)"), 1e-9);
    }

    @Test
    void mathFloor() {
        MolangEvaluator eval = new MolangEvaluator();
        assertEquals(3.0, eval.evaluate("math.floor(3.7)"), 1e-9);
    }

    @Test
    void mathSqrt() {
        MolangEvaluator eval = new MolangEvaluator();
        assertEquals(3.0, eval.evaluate("math.sqrt(9)"), 1e-9);
    }

    @Test
    void mathPow() {
        MolangEvaluator eval = new MolangEvaluator();
        assertEquals(8.0, eval.evaluate("math.pow(2, 3)"), 1e-9);
    }

    @Test
    void mathClamp() {
        MolangEvaluator eval = new MolangEvaluator();
        assertEquals(3.0, eval.evaluate("math.clamp(5, 0, 3)"), 1e-9);
    }

    @Test
    void mathMax() {
        MolangEvaluator eval = new MolangEvaluator();
        assertEquals(3.0, eval.evaluate("math.max(1, 2, 3)"), 1e-9);
    }

    @Test
    void mathNestedCall() {
        MolangEvaluator eval = new MolangEvaluator();
        // math.min(5, 10) = 5; math.max(5, 2) = 5
        assertEquals(5.0, eval.evaluate("math.max(math.min(5, 10), 2)"), 1e-9);
    }

    @Test
    void variableEvaluation() {
        MolangEvaluator eval = new MolangEvaluator();
        eval.setVariable("variable.x", 4.0);
        assertEquals(8.0, eval.evaluate("variable.x * 2"), 1e-9);
    }

    @Test
    void defaultContextDeltaTime() {
        MolangEvaluator eval = MolangEvaluator.withDefaultContext();
        assertEquals(0.016, eval.evaluate("query.delta_time"), 1e-9);
    }

    @Test
    void constantPi() {
        MolangEvaluator eval = new MolangEvaluator();
        assertEquals(Math.PI, eval.evaluate("pi"), 1e-9);
    }

    // -------------------------------------------------------------------------
    // MolangInverter tests
    // -------------------------------------------------------------------------

    @Test
    void invertIntegerPreservesFormat() {
        // Fix C: "1" must become "-1", not "-1.0"
        assertEquals("-1", MolangInverter.invert("1"));
    }

    @Test
    void invertNegativeIntegerPreservesFormat() {
        assertEquals("1", MolangInverter.invert("-1"));
    }

    @Test
    void invertDoublePreservesFormat() {
        assertEquals("-1.5", MolangInverter.invert("1.5"));
    }

    @Test
    void invertZeroIsPassthrough() {
        assertEquals("0", MolangInverter.invert("0"));
    }

    @Test
    void invertNullIsNull() {
        assertNull(MolangInverter.invert(null));
    }

    @Test
    void invertEmptyIsEmpty() {
        assertEquals("", MolangInverter.invert(""));
    }

    @Test
    void invertSimpleSum() {
        // "a + b"  ->  "-a - b"
        assertEquals("-a - b", MolangInverter.invert("a + b"));
    }

    @Test
    void invertIdempotenceVariable() {
        String expr = "variable.x";
        assertEquals(expr, MolangInverter.invert(MolangInverter.invert(expr)));
    }

    @Test
    void invertIdempotenceDouble() {
        assertEquals("1.5", MolangInverter.invert(MolangInverter.invert("1.5")));
    }
}
