package com.danrus.bb4j.molang;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MolangEvaluator {
    
    private final Map<String, Double> variables;
    private final Map<String, Double> temporaries;
    
    private static final Pattern VARIABLE_PATTERN = Pattern.compile(
        "([a-zA-Z_][a-zA-Z0-9_]*(?:\\.[a-zA-Z_][a-zA-Z0-9_]*)*)"
    );
    
    public MolangEvaluator() {
        this.variables = new HashMap<>();
        this.temporaries = new HashMap<>();
    }
    
    public MolangEvaluator(Map<String, Double> variables) {
        this.variables = new HashMap<>(variables);
        this.temporaries = new HashMap<>();
    }
    
    public void setVariable(String name, double value) {
        variables.put(name.toLowerCase(), value);
    }
    
    public void setVariables(Map<String, Double> vars) {
        vars.forEach((k, v) -> variables.put(k.toLowerCase(), v));
    }
    
    public void clearVariables() {
        variables.clear();
    }
    
    public double evaluate(String expression) {
        if (expression == null || expression.isEmpty()) {
            return 0;
        }
        
        expression = expression.trim();
        
        if (isNumber(expression)) {
            return parseNumber(expression);
        }
        
        if (expression.startsWith("(") && expression.endsWith(")")) {
            return evaluate(expression.substring(1, expression.length() - 1));
        }
        
        return evaluateExpression(expression);
    }
    
    public Double evaluateOrNull(String expression) {
        try {
            return evaluate(expression);
        } catch (Exception e) {
            return null;
        }
    }
    
    private double evaluateExpression(String expr) {
        expr = expr.trim();
        
        if (expr.startsWith("-") && !expr.startsWith("--")) {
            return -evaluate(expr.substring(1));
        }
        
        if (expr.startsWith("+")) {
            return evaluate(expr.substring(1));
        }
        
        int parenDepth = 0;
        int lastOp = -1;
        int lastLowOp = -1;
        
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            
            if (c == ')' || c == ']' || c == '}') parenDepth++;
            else if (c == '(' || c == '[' || c == '{') parenDepth--;
            else if (parenDepth == 0) {
                if ("?:".indexOf(c) >= 0 && lastLowOp == -1) {
                    lastLowOp = i;
                }
                if ("+-".indexOf(c) >= 0 && lastOp == -1 && i > 0) {
                    char prev = expr.charAt(i - 1);
                    if (!Character.isDigit(prev) && prev != '.') {
                        lastOp = i;
                    }
                }
                if ("*/".indexOf(c) >= 0 && lastOp == -1) {
                    lastOp = i;
                }
            }
        }
        
        if (lastLowOp != -1) {
            String cond = expr.substring(0, lastLowOp);
            String trueVal = expr.substring(lastLowOp + 1);
            String falseVal = "";
            
            int colonIdx = trueVal.indexOf(':');
            if (colonIdx != -1) {
                falseVal = trueVal.substring(colonIdx + 1);
                trueVal = trueVal.substring(0, colonIdx);
            }
            
            return evaluate(cond) != 0 ? evaluate(trueVal) : evaluate(falseVal);
        }
        
        if (lastOp != -1) {
            char op = expr.charAt(lastOp);
            String left = expr.substring(0, lastOp);
            String right = expr.substring(lastOp + 1);
            
            switch (op) {
                case '+': return evaluate(left) + evaluate(right);
                case '-': return evaluate(left) - evaluate(right);
                case '*': return evaluate(left) * evaluate(right);
                case '/': return evaluate(right) != 0 ? evaluate(left) / evaluate(right) : 0;
            }
        }
        
        return evaluateFunction(expr);
    }
    
    private double evaluateFunction(String expr) {
        int openParen = expr.indexOf('(');
        if (openParen > 0) {
            String funcName = expr.substring(0, openParen).trim().toLowerCase();
            String argsStr = expr.substring(openParen + 1);
            if (argsStr.endsWith(")")) {
                argsStr = argsStr.substring(0, argsStr.length() - 1);
            }
            
            String[] args = splitArgs(argsStr);
            return executeFunction(funcName, args);
        }
        
        return getVariableValue(expr);
    }
    
    private String[] splitArgs(String argsStr) {
        List<String> args = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        
        for (int i = 0; i < argsStr.length(); i++) {
            char c = argsStr.charAt(i);
            if (c == '(' || c == '[' || c == '{') depth++;
            else if (c == ')' || c == ']' || c == '}') depth--;
            else if (c == ',' && depth == 0) {
                args.add(current.toString().trim());
                current = new StringBuilder();
                continue;
            }
            current.append(c);
        }
        
        if (current.length() > 0) {
            args.add(current.toString().trim());
        }
        
        return args.toArray(new String[0]);
    }
    
    private double executeFunction(String funcName, String[] args) {
        double[] evalArgs = new double[args.length];
        for (int i = 0; i < args.length; i++) {
            evalArgs[i] = evaluate(args[i]);
        }
        
        switch (funcName) {
            case "sin":
                return evalArgs.length > 0 ? Math.sin(Math.toRadians(evalArgs[0])) : 0;
            case "cos":
                return evalArgs.length > 0 ? Math.cos(Math.toRadians(evalArgs[0])) : 0;
            case "tan":
                return evalArgs.length > 0 ? Math.tan(Math.toRadians(evalArgs[0])) : 0;
            case "asin":
                return evalArgs.length > 0 ? Math.toDegrees(Math.asin(evalArgs[0])) : 0;
            case "acos":
                return evalArgs.length > 0 ? Math.toDegrees(Math.acos(evalArgs[0])) : 0;
            case "atan":
                return evalArgs.length > 0 ? Math.toDegrees(Math.atan(evalArgs[0])) : 0;
            case "atan2":
                return evalArgs.length >= 2 ? Math.toDegrees(Math.atan2(evalArgs[0], evalArgs[1])) : 0;
            case "abs":
                return evalArgs.length > 0 ? Math.abs(evalArgs[0]) : 0;
            case "ceil":
                return evalArgs.length > 0 ? Math.ceil(evalArgs[0]) : 0;
            case "floor":
                return evalArgs.length > 0 ? Math.floor(evalArgs[0]) : 0;
            case "round":
                return evalArgs.length > 0 ? Math.round(evalArgs[0]) : 0;
            case "sqrt":
                return evalArgs.length > 0 ? Math.sqrt(evalArgs[0]) : 0;
            case "pow":
                return evalArgs.length >= 2 ? Math.pow(evalArgs[0], evalArgs[1]) : 0;
            case "min":
                if (evalArgs.length == 0) return 0;
                double min = evalArgs[0];
                for (int i = 1; i < evalArgs.length; i++) min = Math.min(min, evalArgs[i]);
                return min;
            case "max":
                if (evalArgs.length == 0) return 0;
                double max = evalArgs[0];
                for (int i = 1; i < evalArgs.length; i++) max = Math.max(max, evalArgs[i]);
                return max;
            case "clamp":
                if (evalArgs.length < 3) return 0;
                return Math.max(evalArgs[1], Math.min(evalArgs[0], evalArgs[2]));
            case "lerp":
                if (evalArgs.length < 3) return 0;
                return evalArgs[0] + (evalArgs[1] - evalArgs[0]) * evalArgs[2];
            case "lerprot":
                if (evalArgs.length < 3) return 0;
                return lerpAngle(evalArgs[0], evalArgs[1], evalArgs[2]);
            case "random":
                return Math.random();
            case "random_range":
                if (evalArgs.length < 2) return 0;
                return evalArgs[0] + Math.random() * (evalArgs[1] - evalArgs[0]);
            case "mod":
                return evalArgs.length >= 2 ? evalArgs[0] % evalArgs[1] : 0;
            case "trunc":
                return evalArgs.length > 0 ? (long) evalArgs[0] : 0;
            default:
                return getVariableValue(funcName + "(" + String.join(",", args) + ")");
        }
    }
    
    private double lerpAngle(double from, double to, double factor) {
        double diff = to - from;
        while (diff > 180) diff -= 360;
        while (diff < -180) diff += 360;
        return from + diff * factor;
    }
    
    private double getVariableValue(String name) {
        name = name.trim();
        
        if (isNumber(name)) {
            return parseNumber(name);
        }
        
        String lowerName = name.toLowerCase();
        
        if (lowerName.equals("true")) return 1;
        if (lowerName.equals("false")) return 0;
        if (lowerName.equals("pi")) return Math.PI;
        if (lowerName.equals("e")) return Math.E;
        
        Double value = variables.get(lowerName);
        if (value != null) return value;
        
        value = temporaries.get(lowerName);
        if (value != null) return value;
        
        for (Map.Entry<String, Double> entry : variables.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        
        if (name.contains(".")) {
            String[] parts = name.split("\\.", 2);
            value = variables.get(parts[0].toLowerCase());
            if (value != null) {
                return value;
            }
        }
        
        return 0;
    }
    
    private boolean isNumber(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private double parseNumber(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public Map<String, Double> getVariables() {
        return new HashMap<>(variables);
    }
    
    public static MolangEvaluator withDefaultContext() {
        MolangEvaluator eval = new MolangEvaluator();
        eval.setVariables(getDefaultVariables());
        return eval;
    }
    
    public static Map<String, Double> getDefaultVariables() {
        Map<String, Double> vars = new HashMap<>();
        
        vars.put("query.life_time", 0.0);
        vars.put("query.delta_time", 0.016);
        vars.put("query.anim_time", 0.0);
        vars.put("query.key_frames_time", 0.0);
        vars.put("query.key_frame_factor", 1.0);
        vars.put("query.frame_time", 0.016);
        
        vars.put("variable.cycle", 0.0);
        vars.put("variable.rotation_x", 0.0);
        vars.put("variable.rotation_y", 0.0);
        vars.put("variable.rotation_z", 0.0);
        vars.put("variable.scale_x", 1.0);
        vars.put("variable.scale_y", 1.0);
        vars.put("variable.scale_z", 1.0);
        
        return vars;
    }
}
