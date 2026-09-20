package com.fnc.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Formatea gramáticas en la notación formal usada en clase:
 *
 * <pre>
 * G={{A, B}, {a, b}, S, Sigma}
 * Sigma=
 *   S -> A B | B
 *   A -> a
 * </pre>
 *
 * Como los TextArea de JavaFX son texto plano (sin rich-text), el
 * "tachado" de producciones eliminadas se simula con el caracter
 * Unicode de tachado combinado U+0336 (ej: F&#x336;1&#x336;).
 */
public final class GrammarFormatter {

    /** Caracter combinado U+0336 que tacha el caracter anterior. */
    private static final char STRIKE = '̶';

    private GrammarFormatter() {
    }

    /**
     * Tacha un texto para texto plano (ej: "F1" -> "F&#x336;1&#x336;").
     */
    public static String strike(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            sb.append(text.charAt(i)).append(STRIKE);
        }
        return sb.toString();
    }

    /**
     * Cabecera formal: G={{V}, {T}, S, Sigma}.
     */
    public static String formatHeader(Grammar grammar) {
        if (grammar == null) {
            return "G={} (sin gramática)";
        }
        return "G={{" + String.join(", ", grammar.getVariables()) + "}, {"
                + String.join(", ", grammar.getTerminals()) + "}, "
                + grammar.getStartSymbol() + ", Sigma}";
    }

    /**
     * Producciones agrupadas por variable: "S -> A B | B".
     * Una alternativa vacía se muestra como "ε".
     */
    public static List<String> formatProductionLines(Grammar grammar) {
        List<String> lines = new ArrayList<>();
        if (grammar == null) {
            lines.add("(sin gramática)");
            return lines;
        }
        Map<String, List<String>> grouped = new LinkedHashMap<>();
        for (String v : grammar.getVariables()) {
            grouped.put(v, new ArrayList<>());
        }
        for (Production p : grammar.getProductions()) {
            grouped.computeIfAbsent(p.getLeftSide(), k -> new ArrayList<>())
                    .add(formatRightSide(p.getRightSide()));
        }
        for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            lines.add(entry.getKey() + " -> " + String.join(" | ", entry.getValue()));
        }
        if (lines.isEmpty()) {
            lines.add("(sin producciones)");
        }
        return lines;
    }

    private static String formatRightSide(List<String> rightSide) {
        if (rightSide == null || rightSide.isEmpty()) {
            return "ε";
        }
        return String.join(" ", rightSide);
    }

    /**
     * Gramática completa: cabecera + bloque Sigma.
     */
    public static String formatFull(Grammar grammar) {
        StringBuilder sb = new StringBuilder();
        sb.append(formatHeader(grammar)).append("\n");
        sb.append("Sigma=\n");
        for (String line : formatProductionLines(grammar)) {
            sb.append("  ").append(line).append("\n");
        }
        return sb.toString().trim();
    }

    /**
     * Bloque Sigma donde las producciones cuyo texto esté en
     * {@code struckProductionTexts} aparecen tachadas.
     * El texto de comparación es {@code Production.toString()}.
     */
    public static String formatWithStruck(Grammar grammar, Set<String> struckProductionTexts) {
        StringBuilder sb = new StringBuilder();
        sb.append(formatHeader(grammar)).append("\n");
        sb.append("Sigma=\n");
        if (grammar == null || grammar.getProductions().isEmpty()) {
            sb.append("  (sin producciones)");
            return sb.toString();
        }
        for (Production p : grammar.getProductions()) {
            String line = "  " + p.getLeftSide() + " -> " + formatRightSide(p.getRightSide());
            if (struckProductionTexts != null && struckProductionTexts.contains(p.toString())) {
                line = "  " + strike(line.trim());
            }
            sb.append(line).append("\n");
        }
        return sb.toString().trim();
    }
}
