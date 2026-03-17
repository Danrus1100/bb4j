package com.danrus.bb4j.molang;

import java.util.regex.Pattern;

public class MolangInverter {
    private static final Pattern STRING_NUM_REGEX = Pattern.compile("^-?\\d+(\\.\\d+f?)?$");
    private static final String BRACKET_OPEN = "{([";
    private static final String BRACKET_CLOSE = "})]";

    public static String invert(String molang) {
        if (molang == null || molang.isEmpty() || "0".equals(molang)) {
            return molang;
        }
        
        if (isStringNumber(molang)) {
            double val = Double.parseDouble(molang);
            return String.valueOf(-val);
        }
        
        boolean invert = true;
        int bracketDepth = 0;
        String lastOperator = null;
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < molang.length(); i++) {
            char c = molang.charAt(i);
            
            if (bracketDepth == 0) {
                String operator = null;
                boolean hadInput = true;
                
                if (c == '-' && (lastOperator == null || (!"*".equals(lastOperator) && !"/".equals(lastOperator)))) {
                    if (!invert && lastOperator == null) {
                        result.append('+');
                    }
                    invert = false;
                    continue;
                } else if (c == ' ' || c == '\n') {
                    hadInput = false;
                } else if (c == '+' && (lastOperator == null || (!"*".equals(lastOperator) && !"/".equals(lastOperator)))) {
                    result.append('-');
                    invert = false;
                    continue;
                } else if (c == '?' || c == ':') {
                    invert = true;
                    operator = String.valueOf(c);
                } else if (invert) {
                    result.append('-');
                    invert = false;
                } else if ("+-*/&|".indexOf(c) >= 0) {
                    operator = String.valueOf(c);
                }
                
                if (hadInput) {
                    lastOperator = operator;
                }
            }
            
            if (BRACKET_OPEN.indexOf(c) >= 0) {
                bracketDepth++;
            } else if (BRACKET_CLOSE.indexOf(c) >= 0) {
                bracketDepth--;
            }
            
            result.append(c);
        }
        
        return result.toString();
    }

    private static boolean isStringNumber(String string) {
        return STRING_NUM_REGEX.matcher(string).matches();
    }

    public static double evaluate(String molang) {
        return 0;
    }
}
