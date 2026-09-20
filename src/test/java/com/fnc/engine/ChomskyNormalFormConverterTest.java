package com.fnc.engine;

import com.fnc.model.Grammar;
import com.fnc.model.Production;
import com.fnc.model.TransformationStep;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias para {@link ChomskyNormalFormConverter}.
 * Casos basados en Hopcroft, Motwani, Ullman (Introduction to Automata Theory).
 */
class ChomskyNormalFormConverterTest {

    /** Gramática ya en FNC debe quedar intacta. */
    @Test
    void alreadyInCnf() {
        Grammar grammar = new Grammar(
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
        TransformationStep step = converter.convert(grammar);
        Grammar result = step.getGrammarAfter();

        assertTrue(result.getProductions().stream()
                        .allMatch(p -> p.getRightSide().size() <= 2),
                "Todas las producciones deben ser binarias o terminales");
        assertTrue(converter.getProductionsRemoved().isEmpty(),
                "No debe eliminarse ninguna producción");
    }

    /**
     * S → Aa: el terminal 'a' en producción larga se sustituye
     * por una variable nueva X → a.
     */
    @Test
    void terminalReplacement() {
        Grammar grammar = new Grammar(
            Set.of("S", "A"),
            Set.of("a", "b"),
            List.of(
                new Production("S", List.of("A", "a")),
                new Production("A", List.of("b"))
            ),
            "S"
        );

        ChomskyNormalFormConverter converter = new ChomskyNormalFormConverter();
        Grammar result = converter.convert(grammar).getGrammarAfter();

        assertTrue(result.getProductions().stream()
                        .anyMatch(p -> p.getRightSide().size() == 1
                                && p.getRightSide().get(0).equals("a")
                                && !p.getLeftSide().equals("A")),
                "Debe crearse una variable nueva para el terminal 'a'");
    }

    /**
     * S → ABC se descompone en binarias con variable auxiliar Y.
     */
    @Test
    void longProductionDecomposition() {
        Grammar grammar = new Grammar(
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
        Grammar result = converter.convert(grammar).getGrammarAfter();

        assertTrue(result.getProductions().stream()
                        .anyMatch(p -> p.getLeftSide().equals("S")
                                && p.getRightSide().size() == 2),
                "La producción de S debe quedar binaria");
        assertTrue(result.getProductions().stream()
                        .anyMatch(p -> p.getLeftSide().startsWith("Y")),
                "Debe crearse una variable Y auxiliar");
    }

    /** Producción mixta S → aAb debe quedar en FNC. */
    @Test
    void mixedProduction() {
        Grammar grammar = new Grammar(
            Set.of("S", "A"),
            Set.of("a", "b"),
            List.of(
                new Production("S", List.of("a", "A", "b")),
                new Production("A", List.of("a", "b"))
            ),
            "S"
        );

        ChomskyNormalFormConverter converter = new ChomskyNormalFormConverter();
        Grammar result = converter.convert(grammar).getGrammarAfter();

        assertTrue(result.getProductions().stream()
                        .allMatch(p -> p.getRightSide().size() <= 2),
                "Todas las producciones deben ser FNC válidas");
    }

    /** Gramática compleja: S → AB | a, A → a | AC, B → b, C → c. */
    @Test
    void complexGrammar() {
        Grammar grammar = new Grammar(
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
        Grammar result = converter.convert(grammar).getGrammarAfter();

        assertTrue(result.getProductions().stream()
                        .allMatch(p -> p.getRightSide().size() <= 2
                                || (p.getRightSide().size() == 1
                                    && grammar.getTerminals()
                                            .contains(p.getRightSide().get(0)))),
                "Todas las producciones deben ser FNC válidas");
        assertTrue(result.getProductions().stream()
                        .anyMatch(p -> p.getRightSide().equals(List.of("a"))),
                "Las producciones terminales deben conservarse");
    }
}
