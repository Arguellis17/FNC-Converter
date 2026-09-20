package com.fnc.engine;

import com.fnc.model.Grammar;
import com.fnc.model.Production;
import com.fnc.model.TransformationStep;

import java.util.*;

/**
 * Converts a context-free grammar to Chomsky Normal Form (CNF).
 *
 * A grammar is in CNF if all productions are of the form:
 * - A → BC (two variables)
 * - A → a (single terminal)
 *
 * Algorithm:
 * 1. Replace terminals in long productions with new variables
 *    - For terminal 'a' in A → XaY, create X_a → a and replace a with X_a
 * 2. Break long productions into binary productions
 *    - A → X₁X₂...Xₙ becomes:
 *      A → X₁Y₁, Y₁ → X₂Y₂, ..., Yₙ₋₂ → Xₙ₋₁Xₙ
 *
 * Reference: Introduction to Automata Theory, Languages, and Computation
 *            (Hopcroft, Motwani, Ullman) - Chapter 6
 */
public class ChomskyNormalFormConverter {

    private Grammar originalGrammar;
    private Grammar transformedGrammar;
    private final List<String> productionsRemoved;
    private final List<String> productionsAdded;
    private int newVariableCounter;

    public ChomskyNormalFormConverter() {
        this.productionsRemoved = new ArrayList<>();
        this.productionsAdded = new ArrayList<>();
        this.newVariableCounter = 0;
    }

    /**
     * Convert the grammar to Chomsky Normal Form.
     * @param grammar the grammar (should have no ε, unit, or useless variables)
     * @return a TransformationStep with before/after grammars
     */
    public TransformationStep convert(Grammar grammar) {
        this.originalGrammar = grammar.copy();
        this.productionsRemoved.clear();
        this.productionsAdded.clear();
        this.newVariableCounter = 0;

        // Step 1: Replace terminals in long productions
        Grammar afterTerminalReplacement = replaceTerminalsInLongProductions(grammar);

        // Step 2: Break long productions into binary form
        transformedGrammar = breakLongProductions(afterTerminalReplacement);

        // Step 3: Validate CNF
        validateCNF(transformedGrammar);

        List<String> details = new ArrayList<>();
        String description;
        if (productionsRemoved.isEmpty() && productionsAdded.isEmpty()) {
            description = "La gramática ya está en Forma Normal de Chomsky "
                    + "(solo A -> BC o A -> a). No se modifica nada.";
            details.add("Sin cambios: todas las producciones ya cumplen FNC.");
        } else {
            description = "Se sustituyeron los terminales en producciones largas por nuevas "
                    + "variables y se redujeron todas las producciones a la forma binaria "
                    + "requerida por la FNC: A -> BC o A -> a.";
            details.add("Producciones transformadas: " + productionsRemoved.size()
                    + ". Nuevas variables auxiliares creadas en las agregadas.");
        }

        return new TransformationStep(
            "Conversión a Forma Normal de Chomsky",
            description,
            originalGrammar,
            transformedGrammar,
            productionsRemoved,
            productionsAdded,
            details
        );
    }

    /**
     * Replace terminals in productions with length > 1 with new variables.
     *
     * Example:
     * - A → aB becomes A → X_aB where X_a → a
     * - A → ab becomes A → X_aX_b where X_a → a, X_b → b
     */
    private Grammar replaceTerminalsInLongProductions(Grammar grammar) {
        Grammar newGrammar = grammar.copy();
        Map<String, String> terminalToVariable = new HashMap<>();
        List<Production> newProductions = new ArrayList<>();

        // First, collect all existing productions
        for (Production p : grammar.getProductions()) {
            // Skip if it's already a valid CNF form (single terminal or two variables)
            if (p.getRightSide().size() <= 1) {
                newProductions.add(p);
                continue;
            }

            // Check if this production has terminals mixed with variables
            boolean hasMixedSymbols = false;
            for (String symbol : p.getRightSide()) {
                if (grammar.getTerminals().contains(symbol)) {
                    hasMixedSymbols = true;
                    break;
                }
            }

            if (!hasMixedSymbols) {
                newProductions.add(p);
                continue;
            }

            // Create new right side with variables instead of terminals
            List<String> newRightSide = new ArrayList<>();
            for (String symbol : p.getRightSide()) {
                if (grammar.getTerminals().contains(symbol)) {
                    // Get or create variable for this terminal
                    String varName = terminalToVariable.computeIfAbsent(symbol, t -> {
                        String newVar = generateNewVariable(grammar, newGrammar);
                        return newVar;
                    });
                    newRightSide.add(varName);
                } else {
                    newRightSide.add(symbol);
                }
            }

            // Add the modified production
            Production newProd = new Production(p.getLeftSide(), newRightSide);
            newProductions.add(newProd);

            if (!newProd.equals(p)) {
                productionsRemoved.add(p.toString());
                productionsAdded.add(newProd.toString());
            }
        }

        // Add new productions for terminal variables
        for (Map.Entry<String, String> entry : terminalToVariable.entrySet()) {
            String terminal = entry.getKey();
            String variable = entry.getValue();

            Production termProd = new Production(variable, List.of(terminal));
            newProductions.add(termProd);
            productionsAdded.add(termProd.toString());
        }

        newGrammar.setProductions(newProductions);

        // Add new variables to the grammar
        Set<String> newVariables = new LinkedHashSet<>(grammar.getVariables());
        newVariables.addAll(terminalToVariable.values());
        newGrammar.setVariables(newVariables);

        return newGrammar;
    }

    /**
     * Break long productions (> 2 symbols) into binary form.
     *
     * Example:
     * - A → BCDE becomes:
     *   A → BY₁, Y₁ → CY₂, Y₂ → DE
     */
    private Grammar breakLongProductions(Grammar grammar) {
        Grammar newGrammar = grammar.copy();
        List<Production> newProductions = new ArrayList<>();
        Set<String> newVariables = new LinkedHashSet<>(grammar.getVariables());

        for (Production p : grammar.getProductions()) {
            // Already binary or terminal production
            if (p.getRightSide().size() <= 2) {
                newProductions.add(p);
                continue;
            }

            // Break long production into binary chain
            List<Production> binaryProductions = decomposeLongProduction(p, newVariables);
            newProductions.addAll(binaryProductions);

            productionsRemoved.add(p.toString());
            for (Production bp : binaryProductions) {
                productionsAdded.add(bp.toString());
            }
        }

        newGrammar.setProductions(newProductions);
        newGrammar.setVariables(newVariables);

        return newGrammar;
    }

    /**
     * Decompose a long production into binary productions.
     *
     * A → X₁X₂X₃X₄ becomes:
     * A → X₁Y₁
     * Y₁ → X₂Y₂
     * Y₂ → X₃X₄
     */
    private List<Production> decomposeLongProduction(Production production, Set<String> variables) {
        List<Production> result = new ArrayList<>();
        List<String> rightSide = production.getRightSide();

        if (rightSide.size() <= 2) {
            result.add(production);
            return result;
        }

        String leftSide = production.getLeftSide();

        // First pair: A → X₁Y₁
        String firstSymbol = rightSide.get(0);
        String newYVar = generateNewVariableFromString("Y", variables);
        variables.add(newYVar);

        List<String> firstRight = List.of(firstSymbol, newYVar);
        result.add(new Production(leftSide, firstRight));

        // Middle pairs: Yᵢ → Xᵢ₊₁Yᵢ₊₁
        for (int i = 1; i < rightSide.size() - 2; i++) {
            String currentSymbol = rightSide.get(i);
            String nextYVar = generateNewVariableFromString("Y", variables);
            variables.add(nextYVar);

            List<String> middleRight = List.of(currentSymbol, nextYVar);
            result.add(new Production(newYVar, middleRight));

            newYVar = nextYVar;
        }

        // Last pair: Yₙ₋₂ → Xₙ₋₁Xₙ
        String secondToLast = rightSide.get(rightSide.size() - 2);
        String last = rightSide.get(rightSide.size() - 1);

        List<String> lastRight = List.of(secondToLast, last);
        result.add(new Production(newYVar, lastRight));

        return result;
    }

    /**
     * Generate a new unique variable name.
     */
    private String generateNewVariable(Grammar originalGrammar, Grammar currentGrammar) {
        String name;
        do {
            newVariableCounter++;
            name = "X" + newVariableCounter;
        } while (originalGrammar.getVariables().contains(name)
                || currentGrammar.getVariables().contains(name));

        return name;
    }

    /**
     * Generate a new unique variable name with a prefix.
     */
    private String generateNewVariableFromString(String prefix, Set<String> existingVariables) {
        String name;
        int counter = 1;
        do {
            name = prefix + counter;
            counter++;
        } while (existingVariables.contains(name));

        return name;
    }

    /**
     * Validate that the grammar is in CNF.
     */
    private void validateCNF(Grammar grammar) {
        for (Production p : grammar.getProductions()) {
            // Valid CNF: A → BC or A → a
            boolean isBinary = p.getRightSide().size() == 2;
            boolean isTerminal = p.getRightSide().size() == 1
                    && grammar.getTerminals().contains(p.getRightSide().get(0));
            boolean isEpsilon = p.getRightSide().isEmpty();

            if (!isBinary && !isTerminal && !isEpsilon) {
                System.err.println("WARNING: Production " + p + " is not in CNF form!");
            }
        }
    }

    // Getters
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
