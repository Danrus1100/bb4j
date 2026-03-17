package com.danrus.bb4j.model.scalar;

import com.danrus.bb4j.molang.MolangEvaluator;

public class MolangValue extends ScalarValue {
    
    private MolangValue(Object value, ScalarType type) {
        super(value, type);
    }
    
    public static MolangValue of(String molangExpression) {
        return new MolangValue(molangExpression, ScalarType.STRING);
    }
    
    public static MolangValue of(Number value) {
        return new MolangValue(value, ScalarType.NUMBER);
    }
    
    public static MolangValue of(Boolean value) {
        return new MolangValue(value, ScalarType.BOOLEAN);
    }
    
    public static MolangValue parse(Object value) {
        if (value == null) return new MolangValue(null, ScalarType.NULL);
        if (value instanceof Number) return of((Number) value);
        if (value instanceof Boolean) return of((Boolean) value);
        if (value instanceof String) return of((String) value);
        return of(value.toString());
    }
    
    public double evaluate(MolangEvaluator context) {
        if (isNull()) return 0;
        if (isNumber()) return asDouble();
        if (isString()) {
            return context.evaluate(asString());
        }
        return 0;
    }
    
    public double evaluateOrDefault(MolangEvaluator context, double defaultValue) {
        try {
            return evaluate(context);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    public boolean isMolangExpression() {
        if (!isString()) return false;
        String val = asString();
        return val != null && (val.contains(".") || val.contains("+") || val.contains("-") || 
               val.contains("*") || val.contains("/") || val.contains("(") || val.contains("?"));
    }
    
    public boolean isStatic() {
        return isNumber() || isBoolean() || (isString() && !isMolangExpression());
    }
}
