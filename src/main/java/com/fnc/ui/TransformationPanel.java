package com.fnc.ui;

import com.fnc.model.Grammar;
import com.fnc.model.GrammarFormatter;
import com.fnc.model.TransformationStep;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Panel que muestra el historial paso a paso de las transformaciones (RF18).
 * Lista cada etapa y detalla: gramática antes/después y producciones
 * eliminadas/agregadas.
 */
public class TransformationPanel extends SplitPane {

    private final ListView<String> stepList;
    private final TextArea txtDetail;
    private final Label lblCount;
    private final List<TransformationStep> steps = new ArrayList<>();
    private final ObservableList<String> stepNames = FXCollections.observableArrayList();

    public TransformationPanel() {
        stepList = new ListView<>(stepNames);
        stepList.setPrefWidth(220);
        stepList.getSelectionModel().selectedIndexProperty().addListener(
                (obs, oldIdx, newIdx) -> showStep(newIdx.intValue()));

        txtDetail = new TextArea();
        txtDetail.setEditable(false);
        txtDetail.setPromptText("Aquí se mostrará el detalle de cada paso de la transformación.");
        txtDetail.getStyleClass().add("mono");

        VBox left = new VBox(6);
        left.setPadding(new Insets(10));
        lblCount = new Label("Pasos: 0");
        lblCount.getStyleClass().add("panel-title");
        left.getChildren().addAll(lblCount, stepList);
        VBox.setVgrow(stepList, Priority.ALWAYS);

        VBox right = new VBox(6);
        right.setPadding(new Insets(10));
        Label detailTitle = new Label("Detalle del paso");
        detailTitle.getStyleClass().add("panel-title");
        right.getChildren().addAll(detailTitle, txtDetail);
        VBox.setVgrow(txtDetail, Priority.ALWAYS);

        getItems().addAll(left, right);
        setDividerPositions(0.3);
    }

    /** Reemplaza el historial completo de pasos. */
    public void setSteps(List<TransformationStep> newSteps) {
        steps.clear();
        stepNames.clear();
        if (newSteps != null) {
            steps.addAll(newSteps);
            for (int i = 0; i < newSteps.size(); i++) {
                stepNames.add((i + 1) + ". " + newSteps.get(i).getStepName());
            }
        }
        lblCount.setText("Pasos: " + steps.size());
        if (!steps.isEmpty()) {
            stepList.getSelectionModel().select(0);
        } else {
            txtDetail.clear();
        }
    }

    /** Agrega un paso al final del historial. */
    public void addStep(TransformationStep step) {
        steps.add(step);
        stepNames.add(steps.size() + ". " + step.getStepName());
        lblCount.setText("Pasos: " + steps.size());
        stepList.getSelectionModel().select(steps.size() - 1);
    }

    public List<TransformationStep> getSteps() {
        return List.copyOf(steps);
    }

    /** Limpia el historial de pasos. */
    public void clear() {
        setSteps(List.of());
    }

    private void showStep(int index) {
        if (index < 0 || index >= steps.size()) {
            return;
        }
        TransformationStep step = steps.get(index);
        StringBuilder sb = new StringBuilder();
        sb.append(index + 1).append(") ").append(step.getStepName()).append("\n");
        sb.append(step.getDescription()).append("\n");

        if (!step.getDetails().isEmpty()) {
            sb.append("\n");
            for (String detail : step.getDetails()) {
                sb.append("  • ").append(detail).append("\n");
            }
        }

        sb.append("\n--- Sigma antes ---\n");
        if (step.hasChanges()) {
            sb.append(GrammarFormatter.formatWithStruck(
                    step.getGrammarBefore(),
                    new java.util.HashSet<>(step.getProductionsRemoved())));
        } else {
            sb.append(GrammarFormatter.formatFull(step.getGrammarBefore()));
        }

        if (!step.getProductionsAdded().isEmpty()) {
            sb.append("\n\nProducciones agregadas:\n");
            GrammarFormatter.groupProductionTexts(step.getProductionsAdded())
                    .forEach(p -> sb.append("  + ").append(p).append("\n"));
            sb.setLength(sb.length() - 1);
        }

        sb.append("\n\n--- Sigma después ---\n");
        sb.append(GrammarFormatter.formatFull(step.getGrammarAfter()));

        txtDetail.setText(sb.toString());
        txtDetail.setScrollTop(0);
    }

    private String formatGrammar(Grammar grammar) {
        if (grammar == null) {
            return "(sin gramática)";
        }
        return GrammarFormatter.formatFull(grammar);
    }
}
