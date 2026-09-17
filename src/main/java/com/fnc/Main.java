package com.fnc;

import com.fnc.model.Grammar;
import com.fnc.model.Production;
import com.fnc.model.TransformationStep;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Main entry point for the FNC Converter application.
 * Context-Free Grammar to Chomsky Normal Form converter
 * with step-by-step visualization.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("FNC Converter - Gramática a Forma Normal de Chomsky");

        Label label = new Label("FNC Converter - En desarrollo");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
