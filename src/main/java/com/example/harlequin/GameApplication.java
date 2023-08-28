package com.example.harlequin;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class GameApplication extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        showMainMenu();
    }

    public void showMainMenu() throws Exception {
        System.out.println("Starting game...");
        Parent root = FXMLLoader.load(getClass().getResource("menu.fxml"));
        primaryStage.setTitle("Vampire Survivors Clone");

        Scene scene = new Scene(root, 1920, 1080);

        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);  // Set the stage to fullscreen
        primaryStage.show();

        System.out.println("Main menu loaded.");
    }

    public static void main(String[] args) {
        Font.loadFont(GameApplication.class.getResource("/com/example/harlequin/font/Pixeled.ttf").toExternalForm(), 20);
        System.out.println("Launching game application...");
        launch(args);
    }
}