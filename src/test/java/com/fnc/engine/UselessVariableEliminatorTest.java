package com.fnc.engine;

import com.fnc.model.Grammar;
import com.fnc.model.Production;
import com.fnc.model.TransformationStep;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias para {@link UselessVariableEliminator}.
 * Casos basados en Hopcroft, Motwani, Ullman (Introduction to Automata Theory).
 */
class UselessVariableEliminatorTest {

    /** B solo tiene B → ε, por lo tanto no es generadora. */
    @Test
    void simpleNonGenerating() {
        Grammar grammar = new Grammar(
            Set.of("S", "A", "B"),
            Set.of("a", "b"),
            List.of(
                new Production("S", List.of("A")),
                new Production("A", List.of("a")),
                new Production("B", List.of()) // B → ε (no generadora)
            ),
            "S"
        );

        UselessVariableEliminator eliminator = new UselessVariableEliminator();
        TransformationStep step = eliminator.eliminate(grammar);
        Grammar result = step.getGrammarAfter();

        assertFalse(eliminator.getGeneratingVariables().contains("B"),
                "B no debe ser generadora");
        assertFalse(result.getProductions().stream()
                        .anyMatch(p -> p.getLeftSide().equals("B")),
                "Las producciones de B deben eliminarse");
    }

    /** Cadena S → A → B → C → a: todas son generadoras. */
    @Test
    void indirectGeneratingChain() {
        Grammar grammar = new Grammar(
            Set.of("S", "A", "B", "C"),
            Set.of("a"),
            List.of(
                new Production("S", List.of("A")),
                new Production("A", List.of("B")),
                new Production("B", List.of("C")),
                new Production("C", List.of("a"))
            ),
            "S"
        );

        UselessVariableEliminator eliminator = new UselessVariableEliminator();
        eliminator.eliminate(grammar);

        assertTrue(eliminator.getGeneratingVariables().contains("S"), "S es generadora");
        assertTrue(eliminator.getGeneratingVariables().contains("A"), "A es generadora");
        assertTrue(eliminator.getGeneratingVariables().contains("B"), "B es generadora");
        assertTrue(eliminator.getGeneratingVariables().contains("C"), "C es generadora");
    }

    /** C no tiene producciones: S, A, B y C son no generadoras. */
    @Test
    void chainOfNonGenerating() {
        Grammar grammar = new Grammar(
            Set.of("S", "A", "B", "C"),
            Set.of("a"),
            List.of(
                new Production("S", List.of("A")),
                new Production("A", List.of("B")),
                new Production("B", List.of("C"))
                // C has no productions → non-generating
            ),
            "S"
        );

        UselessVariableEliminator eliminator = new UselessVariableEliminator();
        TransformationStep step = eliminator.eliminate(grammar);
        Grammar result = step.getGrammarAfter();

        assertFalse(eliminator.getGeneratingVariables().contains("S"), "S no es generadora");
        assertFalse(eliminator.getGeneratingVariables().contains("A"), "A no es generadora");
        assertFalse(eliminator.getGeneratingVariables().contains("B"), "B no es generadora");
        assertFalse(eliminator.getGeneratingVariables().contains("C"), "C no es generadora");
        assertTrue(result.getProductions().isEmpty(),
                "Todas las producciones deben eliminarse");
    }

    /** Gramática donde todas las variables son generadoras. */
    @Test
    void noNonGenerating() {
        Grammar grammar = new Grammar(
            Set.of("S", "A"),
            Set.of("a", "b"),
            List.of(
                new Production("S", List.of("A", "a")),
                new Production("A", List.of("b"))
            ),
            "S"
        );

        UselessVariableEliminator eliminator = new UselessVariableEliminator();
        eliminator.eliminate(grammar);

        assertTrue(eliminator.getGeneratingVariables().contains("S"), "S es generadora");
        assertTrue(eliminator.getGeneratingVariables().contains("A"), "A es generadora");
        assertTrue(eliminator.getProductionsRemoved().isEmpty(),
                "No debe eliminarse ninguna producción");
    }

    /**
     * S → A | B, A → a, B → C (C no generadora).
     * B se vuelve no generadora; S y A siguen generadoras.
     */
    @Test
    void mixedProductions() {
        Grammar grammar = new Grammar(
            Set.of("S", "A", "B", "C"),
            Set.of("a"),
            List.of(
                new Production("S", List.of("A")),
                new Production("S", List.of("B")),
                new Production("A", List.of("a")),
                new Production("B", List.of("C"))
                // C has no productions → non-generating
            ),
            "S"
        );

        UselessVariableEliminator eliminator = new UselessVariableEliminator();
        TransformationStep step = eliminator.eliminate(grammar);
        Grammar result = step.getGrammarAfter();

        assertTrue(eliminator.getGeneratingVariables().contains("S"), "S es generadora");
        assertTrue(eliminator.getGeneratingVariables().contains("A"), "A es generadora");
        assertFalse(eliminator.getGeneratingVariables().contains("B"), "B no es generadora");
        assertFalse(eliminator.getGeneratingVariables().contains("C"), "C no es generadora");

        assertTrue(hasProduction(result, "S", List.of("A")), "S → A debe conservarse");
        assertTrue(hasProduction(result, "A", List.of("a")), "A → a debe conservarse");
        assertFalse(hasProduction(result, "S", List.of("B")), "S → B debe eliminarse");
    }

    private static boolean hasProduction(Grammar grammar, String left, List<String> right) {
        return grammar.getProductions().stream()
                .anyMatch(p -> p.getLeftSide().equals(left)
                        && p.getRightSide().equals(right));
    }
}
