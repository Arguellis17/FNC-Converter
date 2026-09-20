package com.fnc.ui;

import com.fnc.model.Grammar;
import com.fnc.model.Production;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Formulario para ingresar una Gramática Libre de Contexto G = (V, T, P, S).
 * Permite registrar variables, terminales, símbolo inicial y producciones
 * (RF01-RF05), validar la gramática y cargarla en el proceso de conversión.
 */
public class GrammarInputPanel extends VBox {

    private final TextField txtVariables;
    private final TextField txtTerminals;
    private final TextField txtStartSymbol;
    private final TextArea txtProductions;
    private final TextArea txtValidation;
    private final Button btnValidate;
    private final Button btnExample;
    private final Button btnClear;

    /** Callback que se ejecuta cuando la gramática fue validada con éxito. */
    private Runnable onValidGrammar;

    public GrammarInputPanel() {
        super(8);
        setPadding(new Insets(10));
        getStyleClass().add("panel");

        Label title = new Label("Entrada de la gramática G = (V, T, P, S)");
        title.getStyleClass().add("panel-title");

        txtVariables = new TextField();
        txtVariables.setPromptText("Variables separadas por coma. Ej: S, A, B");

        txtTerminals = new TextField();
        txtTerminals.setPromptText("Terminales separados por coma. Ej: a, b");

        txtStartSymbol = new TextField();
        txtStartSymbol.setPromptText("Símbolo inicial. Ej: S");

        txtProductions = new TextArea();
        txtProductions.setPromptText(
                "Una producción por línea (cada caracter es un símbolo). Ej:\nS -> AB | a\nA -> aA | ε");
        txtProductions.setPrefRowCount(10);
        txtProductions.getStyleClass().add("mono");
        VBox.setVgrow(txtProductions, Priority.ALWAYS);

        txtValidation = new TextArea();
        txtValidation.setEditable(false);
        txtValidation.setPrefRowCount(4);
        txtValidation.setPromptText("Aquí se mostrará el resultado de la validación.");
        txtValidation.getStyleClass().add("mono");

        btnValidate = new Button("Validar");
        btnValidate.setOnAction(e -> validateInput());

        btnExample = new Button("Ejemplo");
        btnExample.setOnAction(e -> loadExample());

        btnClear = new Button("Limpiar");
        btnClear.setOnAction(e -> clearAll());

        HBox buttons = new HBox(8, btnValidate, btnExample, btnClear);

        getChildren().addAll(
                title,
                new Label("Variables (V):"), txtVariables,
                new Label("Terminales (T):"), txtTerminals,
                new Label("Símbolo inicial (S):"), txtStartSymbol,
                new Label("Producciones (P):"), txtProductions,
                buttons,
                new Label("Validación:"), txtValidation);
    }

    public void setOnValidGrammar(Runnable onValidGrammar) {
        this.onValidGrammar = onValidGrammar;
    }

    /**
     * Valida el contenido del formulario y muestra el resultado.
     * @return true si la gramática es válida.
     */
    public boolean validateInput() {
        try {
            Grammar grammar = parseGrammar();
            com.fnc.engine.GrammarValidator validator =
                    new com.fnc.engine.GrammarValidator();
            boolean valid = validator.validate(grammar);
            showValidationResult(valid, validator.getErrors(), validator.getWarnings());
            if (valid && onValidGrammar != null) {
                onValidGrammar.run();
            }
            return valid;
        } catch (IllegalArgumentException ex) {
            showValidationResult(false, List.of("Error: " + ex.getMessage()), List.of());
            return false;
        }
    }

    public void showValidationResult(boolean valid, List<String> errors, List<String> warnings) {
        StringBuilder sb = new StringBuilder();
        if (valid && warnings.isEmpty()) {
            sb.append("Gramática válida. Lista para convertir.");
        } else {
            errors.forEach(err -> sb.append(err).append("\n"));
            warnings.forEach(warn -> sb.append(warn).append("\n"));
        }
        txtValidation.setText(sb.toString().trim());
        txtValidation.getStyleClass().removeAll("validation-ok", "validation-error");
        txtValidation.getStyleClass().add(valid ? "validation-ok" : "validation-error");
    }

    /**
     * Construye la gramática a partir del formulario.
     * @throws IllegalArgumentException si el formato es incorrecto.
     */
    public Grammar parseGrammar() {
        Set<String> variables = parseSymbolSet(txtVariables.getText(), "variables");
        Set<String> terminals = parseSymbolSet(txtTerminals.getText(), "terminales");

        String start = txtStartSymbol.getText() == null
                ? "" : txtStartSymbol.getText().trim();
        if (start.isEmpty()) {
            throw new IllegalArgumentException("Debe indicar el símbolo inicial.");
        }

        List<Production> productions = parseProductions(txtProductions.getText());
        if (productions.isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar al menos una producción.");
        }

        return new Grammar(variables, terminals, productions, start);
    }

    /**
     * Parsea un conjunto de símbolos. Cada caracter es un símbolo, por lo que
     * "S, A, B" y "SAB" son equivalentes. Las comas y espacios son opcionales.
     */
    private Set<String> parseSymbolSet(String text, String setName) {
        if (text == null || text.replaceAll("[,\\s]", "").isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe ingresar el conjunto de " + setName + ".");
        }
        return text.replaceAll("[,\\s]", "").codePoints()
                .mapToObj(cp -> new String(Character.toChars(cp)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Parsea producciones con formato "A -> XY | a" (una por línea).
     * Cada caracter es un símbolo, así "B1C" equivale a "B 1 C".
     * Acepta "->" o "→", alternativas con "|" y "ε" como producción vacía.
     */
    private List<Production> parseProductions(String text) {
        List<Production> result = new ArrayList<>();
        if (text == null) {
            return result;
        }
        String[] lines = text.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] sides = line.split("->|→", 2);
            if (sides.length != 2) {
                throw new IllegalArgumentException(
                        "Línea " + (i + 1) + " sin formato 'Variable -> ...': " + line);
            }
            String left = sides[0].trim();
            if (left.isEmpty() || left.contains(" ")) {
                throw new IllegalArgumentException(
                        "Línea " + (i + 1) + " con lado izquierdo inválido: " + line);
            }
            String[] alternatives = sides[1].split("\\|", -1);
            for (String alt : alternatives) {
                String body = alt.replaceAll("\\s+", "");
                List<String> rightSide;
                if (body.isEmpty() || body.equals("ε") || body.equalsIgnoreCase("epsilon")) {
                    rightSide = List.of();
                } else {
                    rightSide = body.codePoints()
                            .mapToObj(cp -> new String(Character.toChars(cp)))
                            .collect(Collectors.toList());
                }
                result.add(new Production(left, rightSide));
            }
        }
        return result;
    }

    /** Carga una gramática de ejemplo para probar el aplicativo. */
    public void loadExample() {
        txtVariables.setText("S, A, B");
        txtTerminals.setText("a, b");
        txtStartSymbol.setText("S");
        txtProductions.setText("S -> A B | B\nA -> a A | a | ε\nB -> b B | b");
        txtValidation.clear();
    }

    /** Limpia todos los campos del formulario (RF20). */
    public void clearAll() {
        txtVariables.clear();
        txtTerminals.clear();
        txtStartSymbol.clear();
        txtProductions.clear();
        txtValidation.clear();
    }
}
