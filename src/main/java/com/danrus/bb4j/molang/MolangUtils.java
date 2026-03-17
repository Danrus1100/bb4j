package com.danrus.bb4j.molang;

import java.util.regex.Pattern;

public class MolangUtils {
    
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?f?$");
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*$");
    
    public static boolean isNumber(String value) {
        if (value == null || value.isEmpty()) return false;
        return NUMBER_PATTERN.matcher(value.trim()).matches();
    }
    
    public static boolean isVariable(String value) {
        if (value == null || value.isEmpty()) return false;
        return VARIABLE_PATTERN.matcher(value.trim()).matches();
    }
    
    public static boolean isExpression(String value) {
        if (value == null || value.isEmpty()) return false;
        String trimmed = value.trim();
        
        if (isNumber(trimmed)) return false;
        if (isVariable(trimmed)) return false;
        
        return trimmed.contains("+") || trimmed.contains("-") || 
               trimmed.contains("*") || trimmed.contains("/") ||
               trimmed.contains("(") || trimmed.contains("?");
    }
    
    public static Double parseNumber(String value) {
        if (value == null || !isNumber(value)) return null;
        try {
            String cleaned = value.trim().replace("f", "");
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public static String negate(String value) {
        if (value == null || value.isEmpty()) return value;
        return MolangInverter.invert(value);
    }
    
    public static String simplify(String expression) {
        if (expression == null || !isExpression(expression)) return expression;
        
        MolangEvaluator eval = new MolangEvaluator();
        try {
            double result = eval.evaluate(expression);
            if (result == (long) result) {
                return String.valueOf((long) result);
            }
            return String.valueOf(result);
        } catch (Exception e) {
            return expression;
        }
    }
    
    public static String[] extractVariables(String expression) {
        if (expression == null) return new String[0];
        
        java.util.regex.Matcher matcher = VARIABLE_PATTERN.matcher(expression);
        java.util.Set<String> vars = new java.util.HashSet<>();
        
        while (matcher.find()) {
            String var = matcher.group();
            if (!isNumber(var)) {
                vars.add(var);
            }
        }
        
        return vars.toArray(new String[0]);
    }
    
    public static boolean containsFunction(String expression, String functionName) {
        if (expression == null || functionName == null) return false;
        String pattern = functionName + "\\s*\\(";
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(expression).find();
    }
    
    public static String formatNumber(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(Math.round(value * 100000.0) / 100000.0);
    }
}
