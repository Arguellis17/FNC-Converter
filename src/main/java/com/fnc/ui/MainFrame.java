package com.fnc.ui;

import com.fnc.engine.GrammarService;
import com.fnc.engine.GrammarValidator;
import com.fnc.model.Grammar;
import com.fnc.model.TransformationStep;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Ventana principal del aplicativo (menú + paneles).
 * Ofrece modo automático (proceso completo) y modo paso a paso
 * (botón "Ejecutar el paso a paso" + "Continuar"), ambos sobre el
 * pipeline de {@link GrammarService}.
 */
public class MainFrame extends BorderPane {

    private final GrammarInputPanel inputPanel;
    private final TransformationPanel transformationPanel;
    private final ResultPanel resultPanel;
    private final Label lblStatus;

    /** Gramática sobre la que se aplica la siguiente etapa (modo paso a paso). */
    private Grammar workingGrammar;

    /** Pasos calculados de la sesión paso a paso en curso (null si no hay). */
    private List<TransformationStep> pendingSteps;
    /** Índice del último paso ya mostrado (-1 si no hay sesión activa). */
    private int currentStepIndex = -1;

    public MainFrame() {
        inputPanel = new GrammarInputPanel();
        transformationPanel = new TransformationPanel();
        resultPanel = new ResultPanel();
        lblStatus = new Label("Ingrese una gramática para comenzar.");
        lblStatus.getStyleClass().add("status-bar");

        inputPanel.setOnValidGrammar(() -> {
            try {
                workingGrammar = inputPanel.parseGrammar();
                setStatus("Gramática válida. Puede ejecutar el proceso completo o paso a paso.");
            } catch (IllegalArgumentException ex) {
                setStatus("Error: " + ex.getMessage());
            }
        });

        setTop(new VBox(buildMenuBar(), buildToolBar()));
        setLeft(inputPanel);
        inputPanel.setPrefWidth(340);

        VBox center = new VBox();
        center.getChildren().addAll(transformationPanel, new Separator(), resultPanel);
        transformationPanel.setPrefHeight(380);
        resultPanel.setPrefHeight(220);
        setCenter(center);

        setBottom(lblStatus);
        BorderPane.setMargin(lblStatus, new Insets(4, 10, 4, 10));
    }

    private MenuBar buildMenuBar() {
        Menu menuArchivo = new Menu("Archivo");

        MenuItem itemNueva = new MenuItem("Nueva gramática");
        itemNueva.setOnAction(e -> newGrammar());

        MenuItem itemSalir = new MenuItem("Salir");
        itemSalir.setOnAction(e -> System.exit(0));

        menuArchivo.getItems().addAll(itemNueva, itemSalir);

        Menu menuAyuda = new Menu("Ayuda");
        MenuItem itemAcerca = new MenuItem("Acerca de");
        itemAcerca.setOnAction(e -> showAbout());
        menuAyuda.getItems().add(itemAcerca);

        return new MenuBar(menuArchivo, menuAyuda);
    }

    private ToolBar buildToolBar() {
        Button btnFull = new Button("▶ Proceso completo");
        btnFull.getStyleClass().add("primary-button");
        btnFull.setOnAction(e -> runFullProcess());

        Button btnStepByStep = new Button("Ejecutar el paso a paso");
        btnStepByStep.setOnAction(e -> startStepByStep());

        Button btnContinue = new Button("Continuar");
        btnContinue.setOnAction(e -> continueStepByStep());

        return new ToolBar(btnFull, new Separator(), btnStepByStep, btnContinue);
    }

    /** Ejecuta todas las etapas de una vez sobre la gramática del formulario. */
    private void runFullProcess() {
        Grammar grammar = parseAndValidate();
        if (grammar == null) {
            return;
        }

        resetStepByStep();

        List<TransformationStep> steps = new GrammarService().convertFull(grammar);
        Grammar current = steps.get(steps.size() - 1).getGrammarAfter();

        workingGrammar = current;
        transformationPanel.setSteps(steps);
        resultPanel.setGrammar(current);
        setStatus("Proceso completo: " + steps.size()
                + " pasos. Gramática final lista.");
    }

    /**
     * Inicia el modo paso a paso: calcula todos los pasos y muestra
     * únicamente el primero. Cada click en "Continuar" revela el siguiente.
     */
    private void startStepByStep() {
        Grammar grammar = parseAndValidate();
        if (grammar == null) {
            return;
        }

        pendingSteps = new GrammarService().convertFull(grammar);
        currentStepIndex = 0;

        transformationPanel.clear();
        resultPanel.clear();
        revealCurrentStep();
    }

    /** Revela el siguiente paso de la sesión paso a paso en curso. */
    private void continueStepByStep() {
        if (pendingSteps == null || currentStepIndex < 0) {
            setStatus("Pulse primero \"Ejecutar el paso a paso\".");
            return;
        }
        if (currentStepIndex + 1 >= pendingSteps.size()) {
            setStatus("Ya se mostró el último paso. Gramática final lista.");
            return;
        }
        currentStepIndex++;
        revealCurrentStep();
    }

    /** Muestra el paso actual y actualiza la gramática de trabajo y el resultado. */
    private void revealCurrentStep() {
        TransformationStep step = pendingSteps.get(currentStepIndex);
        transformationPanel.addStep(step);
        workingGrammar = step.getGrammarAfter();
        resultPanel.setGrammar(workingGrammar);
        setStatus("Paso " + (currentStepIndex + 1) + " de " + pendingSteps.size()
                + ": " + step.getStepName() + ". Pulse \"Continuar\".");
    }

    /** Reinicia la sesión paso a paso. */
    private void resetStepByStep() {
        pendingSteps = null;
        currentStepIndex = -1;
    }

    /**
     * Parsea y valida la gramática del formulario.
     * @return la gramática válida, o null si hay errores (ya reportados).
     */
    private Grammar parseAndValidate() {
        Grammar grammar;
        try {
            grammar = inputPanel.parseGrammar();
        } catch (IllegalArgumentException ex) {
            inputPanel.showValidationResult(false,
                    List.of("Error: " + ex.getMessage()), List.of());
            setStatus("Corrija la gramática antes de convertir.");
            return null;
        }

        GrammarValidator validator = new GrammarValidator();
        if (!validator.validate(grammar)) {
            inputPanel.showValidationResult(false,
                    validator.getErrors(), validator.getWarnings());
            setStatus("La gramática tiene errores. El proceso no puede comenzar.");
            return null;
        }
        inputPanel.showValidationResult(true, List.of(), validator.getWarnings());
        return grammar;
    }

    /** Limpia todo para ingresar una nueva gramática (RF20). */
    private void newGrammar() {
        inputPanel.clearAll();
        transformationPanel.clear();
        resultPanel.clear();
        workingGrammar = null;
        resetStepByStep();
        setStatus("Ingrese una gramática para comenzar.");
    }

    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de");
        alert.setHeaderText("FNC Converter");
        alert.setContentText("Aplicativo para depuración y conversión de Gramáticas "
                + "Libres de Contexto a Forma Normal de Chomsky.\nTeoría de la Computación - Microproyecto #1.");
        alert.showAndWait();
    }

    private void setStatus(String message) {
        lblStatus.setText(message);
    }
}
