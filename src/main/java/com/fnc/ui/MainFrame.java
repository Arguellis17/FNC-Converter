package com.fnc.ui;

import com.fnc.engine.ChomskyNormalFormConverter;
import com.fnc.engine.GrammarService;
import com.fnc.engine.GrammarValidator;
import com.fnc.engine.NullProductionEliminator;
import com.fnc.engine.UnitProductionEliminator;
import com.fnc.engine.UnreachableVariableEliminator;
import com.fnc.engine.UselessVariableEliminator;
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
import java.util.function.Function;

/**
 * Ventana principal del aplicativo (menú + paneles).
 * Ofrece modo paso a paso (una etapa a la vez) y modo automático
 * (proceso completo), en el orden del microproyecto:
 * inútiles -> inalcanzables -> unitarias -> nulas -> FNC.
 */
public class MainFrame extends BorderPane {

    private final GrammarInputPanel inputPanel;
    private final TransformationPanel transformationPanel;
    private final ResultPanel resultPanel;
    private final Label lblStatus;

    /** Gramática sobre la que se aplica la siguiente etapa (modo paso a paso). */
    private Grammar workingGrammar;

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

        Button btnUseless = new Button("1. Inútiles");
        btnUseless.setOnAction(e -> runSingleStep("inútiles",
                g -> new UselessVariableEliminator().eliminate(g)));

        Button btnUnreachable = new Button("2. Inalcanzables");
        btnUnreachable.setOnAction(e -> runSingleStep("inalcanzables",
                g -> new UnreachableVariableEliminator().eliminate(g)));

        Button btnUnit = new Button("3. Unitarias");
        btnUnit.setOnAction(e -> runSingleStep("unitarias",
                g -> new UnitProductionEliminator().eliminate(g)));

        Button btnNull = new Button("4. Nulas");
        btnNull.setOnAction(e -> runSingleStep("nulas",
                g -> new NullProductionEliminator().eliminate(g)));

        Button btnCnf = new Button("5. FNC");
        btnCnf.setOnAction(e -> runSingleStep("FNC",
                g -> new ChomskyNormalFormConverter().convert(g)));

        return new ToolBar(btnFull, new Separator(),
                btnUseless, btnUnreachable, btnUnit, btnNull, btnCnf);
    }

    /** Ejecuta las 5 etapas en orden sobre la gramática del formulario. */
    private void runFullProcess() {
        Grammar grammar;
        try {
            grammar = inputPanel.parseGrammar();
        } catch (IllegalArgumentException ex) {
            inputPanel.showValidationResult(false,
                    List.of("Error: " + ex.getMessage()), List.of());
            setStatus("Corrija la gramática antes de convertir.");
            return;
        }

        GrammarValidator validator = new GrammarValidator();
        if (!validator.validate(grammar)) {
            inputPanel.showValidationResult(false,
                    validator.getErrors(), validator.getWarnings());
            setStatus("La gramática tiene errores. El proceso no puede comenzar.");
            return;
        }
        inputPanel.showValidationResult(true, List.of(), validator.getWarnings());

        List<TransformationStep> steps = new GrammarService().convertFull(grammar);
        Grammar current = steps.get(steps.size() - 1).getGrammarAfter();

        workingGrammar = current;
        transformationPanel.setSteps(steps);
        resultPanel.setGrammar(current);
        setStatus("Proceso completo: " + steps.size()
                + " pasos. Gramática final lista.");
    }

    /**
     * Ejecuta una sola etapa sobre la gramática de trabajo actual
     * (modo paso a paso). Si aún no hay gramática de trabajo, parte
     * de la gramática del formulario previamente validada.
     */
    private void runSingleStep(String stageName, Function<Grammar, TransformationStep> stage) {
        Grammar base = workingGrammar;
        if (base == null) {
            if (!inputPanel.validateInput()) {
                setStatus("Valide primero una gramática correcta.");
                return;
            }
            try {
                base = inputPanel.parseGrammar();
            } catch (IllegalArgumentException ex) {
                setStatus("Error: " + ex.getMessage());
                return;
            }
        }
        TransformationStep step = stage.apply(base);
        workingGrammar = step.getGrammarAfter();
        transformationPanel.addStep(step);
        resultPanel.setGrammar(workingGrammar);
        setStatus("Etapa aplicada: " + step.getStepName() + ".");
    }

    /** Limpia todo para ingresar una nueva gramática (RF20). */
    private void newGrammar() {
        inputPanel.clearAll();
        transformationPanel.clear();
        resultPanel.clear();
        workingGrammar = null;
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
