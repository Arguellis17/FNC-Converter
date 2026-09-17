package com.fnc.engine;

import com.fnc.model.Grammar;
import com.fnc.model.Production;
import com.fnc.model.TransformationStep;

import java.util.List;
import java.util.Set;

/**
 * Tests for NullProductionEliminator.
 * Tests based on examples from:
 * - Hopcroft, Motwani, Ullman (Introduction to Automata Theory)
 */
public class NullProductionEliminatorTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== NullProductionEliminator Tests ===\n");

        testSimpleNullProduction();
        testIndirectNullProduction();
        testMultipleNullables();
        testNoNullProductions();
        testStartSymbolEpsilon();

        System.out.println("\n=== Results ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
    }

    /**
     * Test: S → Aa, A → ε
     * After: S → Aa | a, A → (removed)
     */
    static void testSimpleNullProduction() {
        System.out.println("Test 1: Simple null production");

        Grammar g = new Grammar(
            Set.of("S", "A"),
            Set.of("a"),
            List.of(
                new Production("S", List.of("A", "a")),
                new Production("A", List.of())  // A → ε
            ),
            "S"
        );

        NullProductionEliminator eliminator = new NullProductionEliminator();
        TransformationStep step = eliminator.eliminate(g);

        // Check nullable variables
        boolean nullableOk = eliminator.getNullableVariables().contains("A");

        // Check that S → a was added (A removed)
        Grammar result = step.getGrammarAfter();
        boolean hasSa = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("S") &&
                          p.getRightSide().equals(List.of("a")));
        boolean hasSAa = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("S") &&
                          p.getRightSide().equals(List.of("A", "a")));

        check("Nullable A identified", nullableOk);
        check("Original S → Aa preserved", hasSAa);
        check("New S → a added", hasSa);
    }

    /**
     * Test: S → AB, A → a | ε, B → b | ε
     * A and B are nullable, so we get more combinations
     */
    static void testMultipleNullables() {
        System.out.println("\nTest 2: Multiple nullable variables");

        Grammar g = new Grammar(
            Set.of("S", "A", "B"),
            Set.of("a", "b"),
            List.of(
                new Production("S", List.of("A", "B")),
                new Production("A", List.of("a")),
                new Production("A", List.of()),  // A → ε
                new Production("B", List.of("b")),
                new Production("B", List.of())   // B → ε
            ),
            "S"
        );

        NullProductionEliminator eliminator = new NullProductionEliminator();
        TransformationStep step = eliminator.eliminate(g);

        Grammar result = step.getGrammarAfter();

        // Should have: S → AB, S → A, S → B, S → (none, because S→ε not added)
        boolean hasSAB = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("S") &&
                          p.getRightSide().equals(List.of("A", "B")));
        boolean hasSA = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("S") &&
                          p.getRightSide().equals(List.of("A")));
        boolean hasSB = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("S") &&
                          p.getRightSide().equals(List.of("B")));

        check("S → AB preserved", hasSAB);
        check("S → A added (B removed)", hasSA);
        check("S → B added (A removed)", hasSB);
    }

    /**
     * Test: Grammar with no null productions should remain unchanged
     */
    static void testNoNullProductions() {
        System.out.println("\nTest 3: No null productions");

        Grammar g = new Grammar(
            Set.of("S", "A"),
            Set.of("a", "b"),
            List.of(
                new Production("S", List.of("A", "a")),
                new Production("A", List.of("b"))
            ),
            "S"
        );

        NullProductionEliminator eliminator = new NullProductionEliminator();
        TransformationStep step = eliminator.eliminate(g);

        check("No nullable variables", eliminator.getNullableVariables().isEmpty());
        check("No productions removed", eliminator.getProductionsRemoved().isEmpty());
    }

    /**
     * Test: Indirect nullable - S → AB, A → B, B → ε
     * A becomes nullable because A → B and B → ε
     */
    static void testIndirectNullProduction() {
        System.out.println("\nTest 4: Indirect nullable (chain)");

        Grammar g = new Grammar(
            Set.of("S", "A", "B"),
            Set.of("a"),
            List.of(
                new Production("S", List.of("A", "a")),
                new Production("A", List.of("B")),
                new Production("B", List.of())  // B → ε
            ),
            "S"
        );

        NullProductionEliminator eliminator = new NullProductionEliminator();
        TransformationStep step = eliminator.eliminate(g);

        // A should be nullable because A → B and B → ε
        boolean aNullable = eliminator.getNullableVariables().contains("A");

        Grammar result = step.getGrammarAfter();
        boolean hasSa = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("S") &&
                          p.getRightSide().equals(List.of("a")));

        check("A is indirectly nullable", aNullable);
        check("S → a added (A removed)", hasSa);
    }

    /**
     * Test: If S → ε exists, it should be preserved in the result
     */
    static void testStartSymbolEpsilon() {
        System.out.println("\nTest 5: Start symbol with ε");

        Grammar g = new Grammar(
            Set.of("S", "A"),
            Set.of("a"),
            List.of(
                new Production("S", List.of("A")),
                new Production("S", List.of()),  // S → ε
                new Production("A", List.of("a"))
            ),
            "S"
        );

        NullProductionEliminator eliminator = new NullProductionEliminator();
        TransformationStep step = eliminator.eliminate(g);

        Grammar result = step.getGrammarAfter();

        // S → ε should be preserved
        boolean hasSEpsilon = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("S") && p.getRightSide().isEmpty());

        check("Start symbol S is nullable", eliminator.getNullableVariables().contains("S"));
        check("S → ε preserved in result", hasSEpsilon);
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
