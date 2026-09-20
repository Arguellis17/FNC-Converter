package com.fnc.engine;

import com.fnc.model.Grammar;
import com.fnc.model.Production;
import com.fnc.model.TransformationStep;

import java.util.*;

/**
 * Eliminates null (ε) productions from a context-free grammar.
 *
 * A null production is of the form: A → ε
 *
 * Algorithm:
 * 1. Identify all nullable variables (variables that can derive ε)
 * 2. For each production A → X₁X₂...Xₙ, create new productions by
 *    removing nullable variables in all possible combinations
 * 3. Remove original ε-productions (except S → ε if needed)
 *
 * Reference: Introduction to Automata Theory, Languages, and Computation
 *            (Hopcroft, Motwani, Ullman) - Chapter 6
 */
public class NullProductionEliminator {

    private Grammar originalGrammar;
    private Grammar transformedGrammar;
    private final List<String> productionsRemoved;
    private final List<String> productionsAdded;
    private final Set<String> nullableVariables;

    public NullProductionEliminator() {
        this.productionsRemoved = new ArrayList<>();
        this.productionsAdded = new ArrayList<>();
        this.nullableVariables = new LinkedHashSet<>();
    }

    /**
     * Eliminate null productions from the grammar.
     * @param grammar the original grammar
     * @return a TransformationStep with before/after grammars
     */
    public TransformationStep eliminate(Grammar grammar) {
        this.originalGrammar = grammar.copy();
        this.productionsRemoved.clear();
        this.productionsAdded.clear();
        this.nullableVariables.clear();

        // Step 1: Find all nullable variables
        findNullableVariables(grammar);

        // Step 2: Create new grammar without null productions
        transformedGrammar = createGrammarWithoutNullProductions(grammar);

        List<String> details = new ArrayList<>();
        String description;
        if (nullableVariables.isEmpty()) {
            description = "No se identificaron producciones nulas. "
                    + "La gramática no se modifica y se pasa al siguiente paso.";
            details.add("Sin variables anulables: ninguna deriva ε.");
        } else {
            description = "Variables anulables identificadas: "
                    + String.join(", ", nullableVariables)
                    + ". Se generan las producciones equivalentes y se eliminan los vacíos.";
            for (String removed : productionsRemoved) {
                details.add("Eliminando vacío: " + removed);
            }
        }

        return new TransformationStep(
            "Eliminación de producciones nulas",
            description,
            originalGrammar,
            transformedGrammar,
            productionsRemoved,
            productionsAdded,
            details
        );
    }

    /**
     * Find all variables that can derive ε (nullable variables).
     *
     * Algorithm:
     * 1. Mark A as nullable if A → ε exists
     * 2. Repeat until no more changes:
     *    Mark A as nullable if A → X₁X₂...Xₙ and all Xᵢ are nullable
     */
    private void findNullableVariables(Grammar grammar) {
        Set<String> newNullable = new LinkedHashSet<>();

        // Step 1: Find variables with direct ε-productions
        for (Production p : grammar.getProductions()) {
            if (p.isNull()) {
                newNullable.add(p.getLeftSide());
            }
        }

        // Step 2: Iteratively find indirectly nullable variables
        boolean changed = true;
        while (changed) {
            changed = false;

            for (Production p : grammar.getProductions()) {
                // Skip ε-productions
                if (p.isNull()) continue;

                // Check if all symbols on right side are nullable
                boolean allNullable = true;
                for (String symbol : p.getRightSide()) {
                    if (!newNullable.contains(symbol)) {
                        allNullable = false;
                        break;
                    }
                }

                // If all nullable and not already marked, add to nullable set
                if (allNullable && !newNullable.contains(p.getLeftSide())) {
                    newNullable.add(p.getLeftSide());
                    changed = true;
                }
            }
        }

        nullableVariables.addAll(newNullable);
    }

    /**
     * Create a new grammar with all null productions eliminated.
     */
    private Grammar createGrammarWithoutNullProductions(Grammar grammar) {
        Grammar newGrammar = grammar.copy();
        List<Production> newProductions = new ArrayList<>();
        Set<String> processedProductions = new HashSet<>();

        for (Production p : grammar.getProductions()) {
            if (p.isNull()) {
                // Record the removal (except S → ε which we handle separately)
                if (!p.getLeftSide().equals(grammar.getStartSymbol())) {
                    productionsRemoved.add(p.toString());
                }
                continue;
            }

            // Generate all variants by removing nullable variables
            List<Production> variants = generateVariants(p, grammar.getStartSymbol());

            for (Production variant : variants) {
                String key = variant.toString();
                if (!processedProductions.contains(key)) {
                    newProductions.add(variant);
                    processedProductions.add(key);

                    // Record new productions (not the original)
                    if (!variant.equals(p)) {
                        productionsAdded.add(variant.toString());
                    }
                }
            }
        }

        // Add S → ε back if the original grammar had it and S is nullable
        if (nullableVariables.contains(grammar.getStartSymbol())) {
            Production epsilonProd = new Production(grammar.getStartSymbol(), List.of());
            String key = epsilonProd.toString();
            if (!processedProductions.contains(key)) {
                newProductions.add(epsilonProd);
                productionsAdded.add(epsilonProd.toString());
            }
        }

        newGrammar.setProductions(newProductions);
        return newGrammar;
    }

    /**
     * Generate all variants of a production by removing nullable variables.
     *
     * For production A → X₁X₂X₃ where X₂ is nullable:
     * - A → X₁X₂X₃ (original)
     * - A → X₁X₃ (without X₂)
     */
    private List<Production> generateVariants(Production production, String startSymbol) {
        List<Production> variants = new ArrayList<>();
        List<String> rightSide = production.getRightSide();

        // Find positions of nullable symbols
        List<Integer> nullablePositions = new ArrayList<>();
        for (int i = 0; i < rightSide.size(); i++) {
            if (nullableVariables.contains(rightSide.get(i))) {
                nullablePositions.add(i);
            }
        }

        // If no nullable symbols, return original production
        if (nullablePositions.isEmpty()) {
            variants.add(production);
            return variants;
        }

        // Generate all subsets of nullable positions
        int totalCombinations = 1 << nullablePositions.size();

        for (int mask = 0; mask < totalCombinations; mask++) {
            // Skip the case where no nullable is removed (original production)
            if (mask == 0) {
                variants.add(production);
                continue;
            }

            // Create new right side by removing selected nullable symbols
            Set<Integer> positionsToRemove = new HashSet<>();
            for (int i = 0; i < nullablePositions.size(); i++) {
                if ((mask & (1 << i)) != 0) {
                    positionsToRemove.add(nullablePositions.get(i));
                }
            }

            List<String> newRightSide = new ArrayList<>();
            for (int i = 0; i < rightSide.size(); i++) {
                if (!positionsToRemove.contains(i)) {
                    newRightSide.add(rightSide.get(i));
                }
            }

            // Don't create A → ε (that would be a null production)
            if (newRightSide.isEmpty()) {
                continue;
            }

            Production newProd = new Production(production.getLeftSide(), newRightSide);
            variants.add(newProd);
        }

        return variants;
    }

    // Getters
    public Set<String> getNullableVariables() {
        return nullableVariables;
    }

    public List<String> getProductionsRemoved() {
        return productionsRemoved;
    }

    public List<String> getProductionsAdded() {
        return productionsAdded;
    }

    public Grammar getTransformedGrammar() {
        return transformedGrammar;
    }
}
