package com.example.harlequin;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GameApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Starting game...");
        Parent root = FXMLLoader.load(getClass().getResource("menu.fxml"));
        primaryStage.setTitle("Vampire Survivors Clone");

        Scene scene = new Scene(root, 1920, 1080);

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);  // Ensure window is not resizable
        primaryStage.show();

        System.out.println("Main menu loaded.");
    }


    public static void main(String[] args) {
        System.out.println("Launching game application...");
        launch(args);
    }
}
