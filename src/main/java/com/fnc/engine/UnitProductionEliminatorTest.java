package com.fnc.engine;

import com.fnc.model.Grammar;
import com.fnc.model.Production;
import com.fnc.model.TransformationStep;

import java.util.List;
import java.util.Set;

/**
 * Tests for UnitProductionEliminator.
 * Tests based on examples from:
 * - Hopcroft, Motwani, Ullman (Introduction to Automata Theory)
 */
public class UnitProductionEliminatorTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== UnitProductionEliminator Tests ===\n");

        testSimpleUnitProduction();
        testChainOfUnitProductions();
        testCycleDetection();
        testNoUnitProductions();
        testMultipleTargets();

        System.out.println("\n=== Results ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
    }

    /**
     * Test: S → A, A → a
     * After: S → a, A → a
     */
    static void testSimpleUnitProduction() {
        System.out.println("Test 1: Simple unit production");

        Grammar g = new Grammar(
            Set.of("S", "A"),
            Set.of("a"),
            List.of(
                new Production("S", List.of("A")),
                new Production("A", List.of("a"))
            ),
            "S"
        );

        UnitProductionEliminator eliminator = new UnitProductionEliminator();
        TransformationStep step = eliminator.eliminate(g);

        Grammar result = step.getGrammarAfter();

        // S → a should be added
        boolean hasSa = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("S") &&
                          p.getRightSide().equals(List.of("a")));
        // A → a should remain
        boolean hasAa = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("A") &&
                          p.getRightSide().equals(List.of("a")));
        // S → A should be removed
        boolean hasSA = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("S") &&
                          p.getRightSide().equals(List.of("A")));

        check("S → a added", hasSa);
        check("A → a preserved", hasAa);
        check("S → A removed", !hasSA);
    }

    /**
     * Test: S → A, A → B, B → a
     * Chain: S ⇒ A ⇒ B ⇒ a
     * After: S → a, A → a, B → a
     */
    static void testChainOfUnitProductions() {
        System.out.println("\nTest 2: Chain of unit productions");

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

        UnitProductionEliminator eliminator = new UnitProductionEliminator();
        TransformationStep step = eliminator.eliminate(g);

        Grammar result = step.getGrammarAfter();

        boolean hasSa = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("S") &&
                          p.getRightSide().equals(List.of("a")));
        boolean hasAa = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("A") &&
                          p.getRightSide().equals(List.of("a")));
        boolean hasBa = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("B") &&
                          p.getRightSide().equals(List.of("a")));

        check("S → a added (chain S→A→B→a)", hasSa);
        check("A → a added (chain A→B→a)", hasAa);
        check("B → a preserved", hasBa);
    }

    /**
     * Test: S → A, A → S, A → a
     * Cycle: S ⇔ A
     * After: S → a, A → a
     */
    static void testCycleDetection() {
        System.out.println("\nTest 3: Cycle detection (S ⇔ A)");

        Grammar g = new Grammar(
            Set.of("S", "A"),
            Set.of("a"),
            List.of(
                new Production("S", List.of("A")),
                new Production("A", List.of("S")),
                new Production("A", List.of("a"))
            ),
            "S"
        );

        UnitProductionEliminator eliminator = new UnitProductionEliminator();
        TransformationStep step = eliminator.eliminate(g);

        Grammar result = step.getGrammarAfter();

        // Both S and A should have a → a
        boolean hasSa = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("S") &&
                          p.getRightSide().equals(List.of("a")));
        boolean hasAa = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("A") &&
                          p.getRightSide().equals(List.of("a")));

        check("S → a added (via cycle)", hasSa);
        check("A → a preserved", hasAa);
    }

    /**
     * Test: Grammar with no unit productions should remain unchanged
     */
    static void testNoUnitProductions() {
        System.out.println("\nTest 4: No unit productions");

        Grammar g = new Grammar(
            Set.of("S", "A"),
            Set.of("a", "b"),
            List.of(
                new Production("S", List.of("A", "a")),
                new Production("A", List.of("b"))
            ),
            "S"
        );

        UnitProductionEliminator eliminator = new UnitProductionEliminator();
        TransformationStep step = eliminator.eliminate(g);

        check("No productions removed", eliminator.getProductionsRemoved().isEmpty());
        check("No productions added", eliminator.getProductionsAdded().isEmpty());
    }

    /**
     * Test: S → A | B, A → a, B → b
     * After: S → a | b, A → a, B → b
     */
    static void testMultipleTargets() {
        System.out.println("\nTest 5: Multiple unit production targets");

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

        UnitProductionEliminator eliminator = new UnitProductionEliminator();
        TransformationStep step = eliminator.eliminate(g);

        Grammar result = step.getGrammarAfter();

        boolean hasSa = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("S") &&
                          p.getRightSide().equals(List.of("a")));
        boolean hasSb = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("S") &&
                          p.getRightSide().equals(List.of("b")));

        check("S → a added (from A)", hasSa);
        check("S → b added (from B)", hasSb);
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
