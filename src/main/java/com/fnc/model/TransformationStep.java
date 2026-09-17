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

    public TransformationStep(String stepName, String description,
                              Grammar grammarBefore, Grammar grammarAfter,
                              List<String> productionsRemoved,
                              List<String> productionsAdded) {
        this.stepName = stepName;
        this.description = description;
        this.grammarBefore = grammarBefore;
        this.grammarAfter = grammarAfter;
        this.productionsRemoved = productionsRemoved;
        this.productionsAdded = productionsAdded;
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
