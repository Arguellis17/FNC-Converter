package com.fnc.model;

import java.util.List;

/**
 * Represents a single transformation step in the grammar conversion process.
 * Stores the grammar before and after the transformation, along with
 * a description of what was done.
 */
public class TransformationStep {
    private final String stepName;
    private final String description;
    private final Grammar grammarBefore;
    private final Grammar grammarAfter;
    private final List<String> productionsRemoved;
    private final List<String> productionsAdded;
    /**
     * Líneas de detalle elemento por elemento (ej: "Se elimina la
     * variable inútil: F", "Eliminando vacío: B -> ε"). Si el paso no
     * encontró nada que transformar, contiene el aviso correspondiente.
     */
    private final List<String> details;

    public TransformationStep(String stepName, String description,
                              Grammar grammarBefore, Grammar grammarAfter,
                              List<String> productionsRemoved,
                              List<String> productionsAdded) {
        this(stepName, description, grammarBefore, grammarAfter,
                productionsRemoved, productionsAdded, List.of());
    }

    public TransformationStep(String stepName, String description,
                              Grammar grammarBefore, Grammar grammarAfter,
                              List<String> productionsRemoved,
                              List<String> productionsAdded,
                              List<String> details) {
        this.stepName = stepName;
        this.description = description;
        this.grammarBefore = grammarBefore;
        this.grammarAfter = grammarAfter;
        this.productionsRemoved = productionsRemoved;
        this.productionsAdded = productionsAdded;
        this.details = details == null ? List.of() : List.copyOf(details);
    }

    // Getters
    public String getStepName() {
        return stepName;
    }

    public String getDescription() {
        return description;
    }

    public Grammar getGrammarBefore() {
        return grammarBefore;
    }

    public Grammar getGrammarAfter() {
        return grammarAfter;
    }

    public List<String> getProductionsRemoved() {
        return productionsRemoved;
    }

    public List<String> getProductionsAdded() {
        return productionsAdded;
    }

    public List<String> getDetails() {
        return details;
    }

    /**
     * Indica si el paso realmente transformó algo.
     */
    public boolean hasChanges() {
        return !productionsRemoved.isEmpty() || !productionsAdded.isEmpty();
    }

    /**
     * Format the step as a readable string for display.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(stepName).append(" ===\n");
        sb.append(description).append("\n\n");

        if (!productionsRemoved.isEmpty()) {
            sb.append("Producciones eliminadas:\n");
            productionsRemoved.forEach(p -> sb.append("  - ").append(p).append("\n"));
        }

        if (!productionsAdded.isEmpty()) {
            sb.append("Producciones agregadas:\n");
            productionsAdded.forEach(p -> sb.append("  + ").append(p).append("\n"));
        }

        return sb.toString();
    }
}
