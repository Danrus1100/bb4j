package com.danrus.bb4j;

import com.danrus.bb4j.molang.MolangEvaluator;
import com.danrus.bb4j.molang.MolangInverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MolangEvaluatorTest {

    @Test
    void testSimpleMath() {
        MolangEvaluator eval = new MolangEvaluator();
        
        assertEquals(2.0, eval.evaluate("1 + 1"), 0.001);
        assertEquals(1.0, eval.evaluate("2 - 1"), 0.001);
        assertEquals(6.0, eval.evaluate("2 * 3"), 0.001);
        assertEquals(2.0, eval.evaluate("6 / 3"), 0.001);
    }

    @Test
    void testMathFunctions() {
        MolangEvaluator eval = new MolangEvaluator();
        
        assertEquals(0.0, eval.evaluate("sin(0)"), 0.001);
        assertEquals(1.0, eval.evaluate("cos(0)"), 0.001);
        assertEquals(0.0, eval.evaluate("abs(0)"), 0.001);
        assertEquals(5.0, eval.evaluate("abs(-5)"), 0.001);
    }

    @Test
    void testLerp() {
        MolangEvaluator eval = new MolangEvaluator();
        
        assertEquals(5.0, eval.evaluate("lerp(0, 10, 0.5)"), 0.001);
        assertEquals(0.0, eval.evaluate("lerp(0, 10, 0)"), 0.001);
        assertEquals(10.0, eval.evaluate("lerp(0, 10, 1)"), 0.001);
    }

    @Test
    void testClamp() {
        MolangEvaluator eval = new MolangEvaluator();
        
        assertEquals(5.0, eval.evaluate("clamp(5, 0, 10)"), 0.001);
        assertEquals(0.0, eval.evaluate("clamp(-1, 0, 10)"), 0.001);
        assertEquals(10.0, eval.evaluate("clamp(15, 0, 10)"), 0.001);
    }

    @Test
    void testVariables() {
        MolangEvaluator eval = new MolangEvaluator();
        eval.setVariable("var.test", 5.0);
        
        assertEquals(5.0, eval.evaluate("var.test"), 0.001);
        assertEquals(10.0, eval.evaluate("var.test + var.test"), 0.001);
    }

    @Test
    void testComplexExpression() {
        MolangEvaluator eval = new MolangEvaluator();
        
        assertEquals(5.0, eval.evaluate("clamp(sin(0) * 10 + 5, 0, 10)"), 0.001);
    }

    @Test
    void testInverter() {
        MolangInverter inverter = new MolangInverter();
        
        String inverted = inverter.invert("var.test = 5");
        assertNotNull(inverted);
    }
}
