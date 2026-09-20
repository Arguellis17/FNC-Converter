package com.fnc.engine;

import com.fnc.model.Grammar;
import com.fnc.model.Production;
import com.fnc.model.TransformationStep;

import java.util.List;
import java.util.Set;

/**
 * Tests for UselessVariableEliminator.
 * Tests based on examples from:
 * - Hopcroft, Motwani, Ullman (Introduction to Automata Theory)
 */
public class UselessVariableEliminatorTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== UselessVariableEliminator Tests ===\n");

        testSimpleNonGenerating();
        testIndirectNonGenerating();
        testChainOfNonGenerating();
        testNoNonGenerating();
        testMixedProductions();

        System.out.println("\n=== Results ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
    }

    /**
     * Test: S → A, A → a, B → b (B is non-generating if not reachable from S,
     * but here we only check generating, not reachable)
     *
     * Actually: S → A, A → a, B → c
     * All variables are generating because they can reach terminals
     */
    static void testSimpleNonGenerating() {
        System.out.println("Test 1: Simple non-generating variable");

        Grammar g = new Grammar(
            Set.of("S", "A", "B"),
            Set.of("a", "b"),
            List.of(
                new Production("S", List.of("A")),
                new Production("A", List.of("a")),
                new Production("B", List.of())  // B → ε (non-generating for terminals)
            ),
            "S"
        );

        UselessVariableEliminator eliminator = new UselessVariableEliminator();
        TransformationStep step = eliminator.eliminate(g);

        Grammar result = step.getGrammarAfter();

        // B should be non-generating (only has ε-production)
        boolean bNonGenerating = !eliminator.getGeneratingVariables().contains("B");

        // B's production should be removed
        boolean hasBProduction = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("B"));

        check("B is non-generating", bNonGenerating);
        check("B's production removed", !hasBProduction);
    }

    /**
     * Test: S → A, A → B, B → C, C → a
     * All are generating through the chain
     */
    static void testIndirectNonGenerating() {
        System.out.println("\nTest 2: Indirect generating (chain)");

        Grammar g = new Grammar(
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
        TransformationStep step = eliminator.eliminate(g);

        // All variables should be generating
        check("S is generating", eliminator.getGeneratingVariables().contains("S"));
        check("A is generating", eliminator.getGeneratingVariables().contains("A"));
        check("B is generating", eliminator.getGeneratingVariables().contains("B"));
        check("C is generating", eliminator.getGeneratingVariables().contains("C"));
    }

    /**
     * Test: S → A, A → B, B → C (C has no productions → non-generating)
     * This makes B, A, S all non-generating
     */
    static void testChainOfNonGenerating() {
        System.out.println("\nTest 3: Chain of non-generating variables");

        Grammar g = new Grammar(
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
        TransformationStep step = eliminator.eliminate(g);

        Grammar result = step.getGrammarAfter();

        // All variables should be non-generating (can't reach terminals)
        check("S is non-generating", !eliminator.getGeneratingVariables().contains("S"));
        check("A is non-generating", !eliminator.getGeneratingVariables().contains("A"));
        check("B is non-generating", !eliminator.getGeneratingVariables().contains("B"));
        check("C is non-generating", !eliminator.getGeneratingVariables().contains("C"));

        // All productions should be removed
        check("All productions removed", result.getProductions().isEmpty());
    }

    /**
     * Test: Grammar where all variables are generating
     */
    static void testNoNonGenerating() {
        System.out.println("\nTest 4: No non-generating variables");

        Grammar g = new Grammar(
            Set.of("S", "A"),
            Set.of("a", "b"),
            List.of(
                new Production("S", List.of("A", "a")),
                new Production("A", List.of("b"))
            ),
            "S"
        );

        UselessVariableEliminator eliminator = new UselessVariableEliminator();
        TransformationStep step = eliminator.eliminate(g);

        check("S is generating", eliminator.getGeneratingVariables().contains("S"));
        check("A is generating", eliminator.getGeneratingVariables().contains("A"));
        check("No productions removed", eliminator.getProductionsRemoved().isEmpty());
    }

    /**
     * Test: S → A | B, A → a, B → C (C is non-generating)
     * B becomes non-generating, but A and S remain generating
     */
    static void testMixedProductions() {
        System.out.println("\nTest 5: Mixed generating/non-generating");

        Grammar g = new Grammar(
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
        TransformationStep step = eliminator.eliminate(g);

        Grammar result = step.getGrammarAfter();

        // A and S should be generating, B and C should not
        check("S is generating", eliminator.getGeneratingVariables().contains("S"));
        check("A is generating", eliminator.getGeneratingVariables().contains("A"));
        check("B is non-generating", !eliminator.getGeneratingVariables().contains("B"));
        check("C is non-generating", !eliminator.getGeneratingVariables().contains("C"));

        // S → A and A → a should remain, S → B and B → C should be removed
        boolean hasSA = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("S") &&
                          p.getRightSide().equals(List.of("A")));
        boolean hasAa = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("A") &&
                          p.getRightSide().equals(List.of("a")));
        boolean hasSB = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("S") &&
                          p.getRightSide().equals(List.of("B")));

        check("S → A preserved", hasSA);
        check("A → a preserved", hasAa);
        check("S → B removed", !hasSB);
    }

    static void check(String name, boolean condition) {
        if (condition) {
            System.out.println("  ✓ " + name);
            passed++;
        } else {
            System.out.println("  ✗ " + name);
            failed++;
        }
    }
}
