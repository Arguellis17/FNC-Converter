package com.fnc.engine;

import com.fnc.model.Grammar;
import com.fnc.model.Production;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Simple test for GrammarValidator.
 * Run this class to verify validation logic.
 */
public class GrammarValidatorTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== GrammarValidator Tests ===\n");

        testValidGrammar();
        testMissingVariables();
        testMissingTerminals();
        testMissingStartSymbol();
        testInvalidStartSymbol();
        testUndeclaredSymbolInProduction();
        testDuplicateProductions();
        testOverlapVariablesTerminals();

        System.out.println("\n=== Results ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
    }

    static void testValidGrammar() {
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

        GrammarValidator v = new GrammarValidator();
        boolean valid = v.validate(g);
        check("Valid grammar passes", valid);
    }

    static void testMissingVariables() {
        Grammar g = new Grammar(
            Set.of(),
            Set.of("a", "b"),
            List.of(new Production("S", List.of("a"))),
            "S"
        );

        GrammarValidator v = new GrammarValidator();
        boolean valid = v.validate(g);
        check("Missing variables fails", !valid);
    }

    static void testMissingTerminals() {
        Grammar g = new Grammar(
            Set.of("S"),
            Set.of(),
            List.of(new Production("S", List.of("S"))),
            "S"
        );

        GrammarValidator v = new GrammarValidator();
        boolean valid = v.validate(g);
        check("Missing terminals fails", !valid);
    }

    static void testMissingStartSymbol() {
        Grammar g = new Grammar(
            Set.of("S", "A"),
            Set.of("a"),
            List.of(new Production("S", List.of("a"))),
            null
        );

        GrammarValidator v = new GrammarValidator();
        boolean valid = v.validate(g);
        check("Missing start symbol fails", !valid);
    }

    static void testInvalidStartSymbol() {
        Grammar g = new Grammar(
            Set.of("S", "A"),
            Set.of("a"),
            List.of(new Production("S", List.of("a"))),
            "X"  // X is not in variables
        );

        GrammarValidator v = new GrammarValidator();
        boolean valid = v.validate(g);
        check("Invalid start symbol fails", !valid);
    }

    static void testUndeclaredSymbolInProduction() {
        Grammar g = new Grammar(
            Set.of("S"),
            Set.of("a"),
            List.of(new Production("S", List.of("a", "B"))),  // B not declared
            "S"
        );

        GrammarValidator v = new GrammarValidator();
        boolean valid = v.validate(g);
        check("Undeclared symbol in production fails", !valid);
    }

    static void testDuplicateProductions() {
        Grammar g = new Grammar(
            Set.of("S"),
            Set.of("a"),
            List.of(
                new Production("S", List.of("a")),
                new Production("S", List.of("a"))  // duplicate
            ),
            "S"
        );

        GrammarValidator v = new GrammarValidator();
        boolean valid = v.validate(g);
        check("Duplicate productions warns", valid && !v.getWarnings().isEmpty());
    }

    static void testOverlapVariablesTerminals() {
        Grammar g = new Grammar(
            Set.of("S", "a"),  // 'a' is both variable and terminal
            Set.of("a", "b"),
            List.of(new Production("S", List.of("a"))),
            "S"
        );

        GrammarValidator v = new GrammarValidator();
        boolean valid = v.validate(g);
        check("Overlap variables-terminals fails", !valid);
    }

    static void check(String name, boolean condition) {
        if (condition) {
            System.out.println("✓ " + name);
            passed++;
        } else {
            System.out.println("✗ " + name);
            failed++;
        }
    }
}
