package com.fnc.engine;

import com.fnc.model.Grammar;
import com.fnc.model.Production;
import com.fnc.model.TransformationStep;

import java.util.List;
import java.util.Set;

/**
 * Tests for UnreachableVariableEliminator.
 * Tests based on examples from:
 * - Hopcroft, Motwani, Ullman (Introduction to Automata Theory)
 */
public class UnreachableVariableEliminatorTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== UnreachableVariableEliminator Tests ===\n");

        testSimpleUnreachable();
        testIndirectUnreachable();
        testChainOfUnreachable();
        testNoUnreachable();
        testMultiplePaths();

        System.out.println("\n=== Results ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
    }

    /**
     * Test: S → a, A → b (A is unreachable from S)
     */
    static void testSimpleUnreachable() {
        System.out.println("Test 1: Simple unreachable variable");

        Grammar g = new Grammar(
            Set.of("S", "A"),
            Set.of("a", "b"),
            List.of(
                new Production("S", List.of("a")),
                new Production("A", List.of("b"))
            ),
            "S"
        );

        UnreachableVariableEliminator eliminator = new UnreachableVariableEliminator();
        TransformationStep step = eliminator.eliminate(g);

        Grammar result = step.getGrammarAfter();

        // A should be unreachable
        boolean aUnreachable = !eliminator.getReachableVariables().contains("A");

        // A's production should be removed
        boolean hasAProduction = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("A"));

        check("A is unreachable", aUnreachable);
        check("A's production removed", !hasAProduction);
    }

    /**
     * Test: S → A, A → B, B → a (all reachable through chain)
     */
    static void testIndirectUnreachable() {
        System.out.println("\nTest 2: All reachable through chain");

        Grammar g = new Grammar(
            Set.of("S", "A", "B"),
            Set.of("a"),
            List.of(
                new Production("S", List.of("A")),
                new Production("A", List.of("B")),
                new Production("B", List.of("a"))
            ),
            "S"
        );

        UnreachableVariableEliminator eliminator = new UnreachableVariableEliminator();
        TransformationStep step = eliminator.eliminate(g);

        // All variables should be reachable
        check("S is reachable", eliminator.getReachableVariables().contains("S"));
        check("A is reachable", eliminator.getReachableVariables().contains("A"));
        check("B is reachable", eliminator.getReachableVariables().contains("B"));
    }

    /**
     * Test: S → A, A → a, B → C (B and C unreachable)
     */
    static void testChainOfUnreachable() {
        System.out.println("\nTest 3: Chain of unreachable variables");

        Grammar g = new Grammar(
            Set.of("S", "A", "B", "C"),
            Set.of("a"),
            List.of(
                new Production("S", List.of("A")),
                new Production("A", List.of("a")),
                new Production("B", List.of("C")),
                new Production("C", List.of("a"))
            ),
            "S"
        );

        UnreachableVariableEliminator eliminator = new UnreachableVariableEliminator();
        TransformationStep step = eliminator.eliminate(g);

        Grammar result = step.getGrammarAfter();

        // B and C should be unreachable
        check("B is unreachable", !eliminator.getReachableVariables().contains("B"));
        check("C is unreachable", !eliminator.getReachableVariables().contains("C"));

        // B and C's productions should be removed
        boolean hasBProduction = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("B"));
        boolean hasCProduction = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("C"));

        check("B's production removed", !hasBProduction);
        check("C's production removed", !hasCProduction);
    }

    /**
     * Test: Grammar where all variables are reachable
     */
    static void testNoUnreachable() {
        System.out.println("\nTest 4: No unreachable variables");

        Grammar g = new Grammar(
            Set.of("S", "A"),
            Set.of("a", "b"),
            List.of(
                new Production("S", List.of("A", "a")),
                new Production("A", List.of("b"))
            ),
            "S"
        );

        UnreachableVariableEliminator eliminator = new UnreachableVariableEliminator();
        TransformationStep step = eliminator.eliminate(g);

        check("S is reachable", eliminator.getReachableVariables().contains("S"));
        check("A is reachable", eliminator.getReachableVariables().contains("A"));
        check("No productions removed", eliminator.getProductionsRemoved().isEmpty());
    }

    /**
     * Test: S → A | B, A → a, B → b (both A and B reachable)
     */
    static void testMultiplePaths() {
        System.out.println("\nTest 5: Multiple paths from S");

        Grammar g = new Grammar(
            Set.of("S", "A", "B"),
            Set.of("a", "b"),
            List.of(
                new Production("S", List.of("A")),
                new Production("S", List.of("B")),
                new Production("A", List.of("a")),
                new Production("B", List.of("b"))
            ),
            "S"
        );

        UnreachableVariableEliminator eliminator = new UnreachableVariableEliminator();
        TransformationStep step = eliminator.eliminate(g);

        // Both A and B should be reachable
        check("A is reachable", eliminator.getReachableVariables().contains("A"));
        check("B is reachable", eliminator.getReachableVariables().contains("B"));

        // All productions should remain
        Grammar result = step.getGrammarAfter();
        check("All productions preserved",
              result.getProductions().size() == g.getProductions().size());
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
