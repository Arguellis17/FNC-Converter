package com.fnc.engine;

import com.fnc.model.Grammar;
import com.fnc.model.Production;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates that a grammar is correctly defined before transformation.
 * Checks: variables, terminals, start symbol, production structure.
 */
public class GrammarValidator {

    private final List<String> errors;

    public GrammarValidator() {
        this.errors = new ArrayList<>();
    }

    /**
     * Validate the entire grammar.
     * @return true if valid, false otherwise (check getErrors())
     */
    public boolean validate(Grammar grammar) {
        errors.clear();

        validateVariables(grammar);
        validateTerminals(grammar);
        validateStartSymbol(grammar);
        validateProductions(grammar);

        return errors.isEmpty();
    }

    private void validateVariables(Grammar grammar) {
        if (grammar.getVariables().isEmpty()) {
            errors.add("Error: No se han declarado variables (símbolos no terminales).");
        }
    }

    private void validateTerminals(Grammar grammar) {
        if (grammar.getTerminals().isEmpty()) {
            errors.add("Error: No se han declarado terminales.");
        }
    }

    private void validateStartSymbol(Grammar grammar) {
        if (grammar.getStartSymbol() == null || grammar.getStartSymbol().isEmpty()) {
            errors.add("Error: No se ha definido el símbolo inicial.");
        } else if (!grammar.getVariables().contains(grammar.getStartSymbol())) {
            errors.add("Error: el símbolo inicial " + grammar.getStartSymbol()
                    + " no pertenece al conjunto de variables.");
        }
    }

    private void validateProductions(Grammar grammar) {
        if (grammar.getProductions().isEmpty()) {
            errors.add("Error: No se han declarado producciones.");
            return;
        }

        for (Production p : grammar.getProductions()) {
            // Validate left side is a declared variable
            if (!grammar.getVariables().contains(p.getLeftSide())) {
                errors.add("Error: la variable " + p.getLeftSide()
                        + " en el lado izquierdo no fue declarada.");
            }

            // Validate all symbols on right side are declared
            for (String symbol : p.getRightSide()) {
                if (symbol.equals("ε")) continue; // epsilon is allowed

                if (!grammar.getVariables().contains(symbol)
                        && !grammar.getTerminals().contains(symbol)) {
                    errors.add("Error: el símbolo " + symbol
                            + " utilizado en la producción " + p
                            + " no fue declarado.");
                }
            }
        }
    }

    public List<String> getErrors() {
        return errors;
    }
}
