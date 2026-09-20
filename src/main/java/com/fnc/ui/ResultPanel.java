package com.fnc.ui;

import com.fnc.model.Grammar;
import com.fnc.model.Production;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Panel que presenta la gramática final en Forma Normal de Chomsky (RF19)
 * y la valida automáticamente (RNF12): cada producción debe ser A -> BC o A -> a.
 */
public class ResultPanel extends VBox {

    private final TextArea txtResult;
    private final Label lblStatus;

    public ResultPanel() {
        super(6);
        setPadding(new Insets(10));
        getStyleClass().add("panel");

        Label title = new Label("Gramática final en Forma Normal de Chomsky");
        title.getStyleClass().add("panel-title");

        txtResult = new TextArea();
        txtResult.setEditable(false);
        txtResult.setPromptText("La gramática final aparecerá aquí después de la conversión.");
        txtResult.getStyleClass().add("mono");
        VBox.setVgrow(txtResult, Priority.ALWAYS);

        lblStatus = new Label("Sin resultado.");

        getChildren().addAll(title, txtResult, lblStatus);
    }

    /** Muestra la gramática final y su validación FNC. */
    public void setGrammar(Grammar grammar) {
        if (grammar == null) {
            clear();
            return;
        }
        txtResult.setText(grammar.toString());
        List<String> invalid = findNonCnfProductions(grammar);
        if (invalid.isEmpty()) {
            lblStatus.setText("La gramática final está en Forma Normal de Chomsky.");
            lblStatus.getStyleClass().removeAll("status-ok", "status-error");
            lblStatus.getStyleClass().add("status-ok");
        } else {
            lblStatus.setText("Advertencia: " + invalid.size()
                    + " producción(es) no cumplen FNC: " + String.join("; ", invalid));
            lblStatus.getStyleClass().removeAll("status-ok", "status-error");
            lblStatus.getStyleClass().add("status-error");
        }
    }

    public void clear() {
        txtResult.clear();
        lblStatus.setText("Sin resultado.");
        lblStatus.getStyleClass().removeAll("status-ok", "status-error");
    }

    /**
     * Verifica que cada producción sea A -> BC (dos variables) o A -> a
     * (un terminal). Se acepta S -> ε como caso especial.
     */
    private List<String> findNonCnfProductions(Grammar grammar) {
        List<String> invalid = new ArrayList<>();
        for (Production p : grammar.getProductions()) {
            List<String> right = p.getRightSide();
            if (right.isEmpty()) {
                // Solo se acepta S -> ε como caso especial.
                if (!p.getLeftSide().equals(grammar.getStartSymbol())) {
                    invalid.add(p.toString());
                }
            } else if (right.size() == 1) {
                if (!grammar.getTerminals().contains(right.get(0))) {
                    invalid.add(p.toString());
                }
            } else if (right.size() == 2) {
                if (!grammar.getVariables().contains(right.get(0))
                        || !grammar.getVariables().contains(right.get(1))) {
                    invalid.add(p.toString());
                }
            } else {
                invalid.add(p.toString());
            }
        }
        return invalid;
    }
}
