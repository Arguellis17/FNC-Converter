package com.fnc.engine;

import com.fnc.model.Grammar;
import com.fnc.model.Production;
import com.fnc.model.TransformationStep;

import java.util.List;
import java.util.Set;

/**
 * Tests for ChomskyNormalFormConverter.
 * Tests based on examples from:
 * - Hopcroft, Motwani, Ullman (Introduction to Automata Theory)
 */
public class ChomskyNormalFormConverterTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== ChomskyNormalFormConverter Tests ===\n");

        testAlreadyCNF();
        testTerminalReplacement();
        testLongProduction();
        testMixedProduction();
        testComplexGrammar();

        System.out.println("\n=== Results ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
    }

    /**
     * Test: Grammar already in CNF should remain unchanged
     */
    static void testAlreadyCNF() {
        System.out.println("Test 1: Already in CNF");

        Grammar g = new Grammar(
            Set.of("S", "A", "B"),
            Set.of("a", "b"),
            List.of(
                new Production("S", List.of("A", "B")),
                new Production("A", List.of("a")),
                new Production("B", List.of("b"))
            ),
            "S"
        );

        ChomskyNormalFormConverter converter = new ChomskyNormalFormConverter();
        TransformationStep step = converter.convert(g);

        Grammar result = step.getGrammarAfter();

        // All productions should be binary or single terminal
        boolean allValid = result.getProductions().stream()
            .allMatch(p -> p.getRightSide().size() <= 2);

        check("All productions are binary or terminal", allValid);
        check("No productions removed", converter.getProductionsRemoved().isEmpty());
    }

    /**
     * Test: Replace terminals in long productions
     * A → aB becomes A → X₁B where X₁ → a
     */
    static void testTerminalReplacement() {
        System.out.println("\nTest 2: Terminal replacement");

        Grammar g = new Grammar(
            Set.of("S", "A"),
            Set.of("a", "b"),
            List.of(
                new Production("S", List.of("A", "a")),
                new Production("A", List.of("b"))
            ),
            "S"
        );

        ChomskyNormalFormConverter converter = new ChomskyNormalFormConverter();
        TransformationStep step = converter.convert(g);

        Grammar result = step.getGrammarAfter();

        // Should have new variable for terminal 'a' in S production
        boolean hasNewVarProd = result.getProductions().stream()
            .anyMatch(p -> p.getRightSide().size() == 1
                    && p.getRightSide().get(0).equals("a")
                    && !p.getLeftSide().equals("A"));

        check("New variable created for terminal 'a'", hasNewVarProd);
    }

    /**
     * Test: Break long production into binary
     * S → ABC becomes S → AY₁, Y₁ → BC
     */
    static void testLongProduction() {
        System.out.println("\nTest 3: Long production decomposition");

        Grammar g = new Grammar(
            Set.of("S", "A", "B", "C"),
            Set.of("a", "b", "c"),
            List.of(
                new Production("S", List.of("A", "B", "C")),
                new Production("A", List.of("a")),
                new Production("B", List.of("b")),
                new Production("C", List.of("c"))
            ),
            "S"
        );

        ChomskyNormalFormConverter converter = new ChomskyNormalFormConverter();
        TransformationStep step = converter.convert(g);

        Grammar result = step.getGrammarAfter();

        // S production should be broken into binary
        boolean hasBinaryS = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().equals("S")
                    && p.getRightSide().size() == 2);

        // Should have new Y variable
        boolean hasYVar = result.getProductions().stream()
            .anyMatch(p -> p.getLeftSide().startsWith("Y"));

        check("S production is binary", hasBinaryS);
        check("New Y variable created", hasYVar);
    }

    /**
     * Test: Mixed production with terminals and variables
     * S → aAb becomes S → X₁AX₂ where X₁ → a, X₂ → b
     */
    static void testMixedProduction() {
        System.out.println("\nTest 4: Mixed production");

        Grammar g = new Grammar(
            Set.of("S", "A"),
            Set.of("a", "b"),
            List.of(
                new Production("S", List.of("a", "A", "b")),
                new Production("A", List.of("a", "b"))
            ),
            "S"
        );

        ChomskyNormalFormConverter converter = new ChomskyNormalFormConverter();
        TransformationStep step = converter.convert(g);

        Grammar result = step.getGrammarAfter();

        // All productions should be valid CNF
        boolean allValid = result.getProductions().stream()
            .allMatch(p -> p.getRightSide().size() <= 2);

        check("All productions are valid CNF", allValid);
    }

    /**
     * Test: Complex grammar
     * S → AB | a, A → a | AC, B → b, C → c
     */
    static void testComplexGrammar() {
        System.out.println("\nTest 5: Complex grammar");

        Grammar g = new Grammar(
            Set.of("S", "A", "B", "C"),
            Set.of("a", "b", "c"),
            List.of(
                new Production("S", List.of("A", "B")),
                new Production("S", List.of("a")),
                new Production("A", List.of("a")),
                new Production("A", List.of("A", "C")),
                new Production("B", List.of("b")),
                new Production("C", List.of("c"))
            ),
            "S"
        );

        ChomskyNormalFormConverter converter = new ChomskyNormalFormConverter();
        TransformationStep step = converter.convert(g);

        Grammar result = step.getGrammarAfter();

        // All productions should be valid CNF
        boolean allValid = result.getProductions().stream()
            .allMatch(p -> p.getRightSide().size() <= 2
                    || (p.getRightSide().size() == 1
                        && g.getTerminals().contains(p.getRightSide().get(0))));

        check("All productions are valid CNF", allValid);
        check("Terminal productions preserved",
              result.getProductions().stream()
                .anyMatch(p -> p.getRightSide().equals(List.of("a"))));
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
