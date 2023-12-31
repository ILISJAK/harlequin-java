package com.example.harlequin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.IOException;

public class MenuController {

    @FXML
    private Button startButton;

    @FXML
    private Button exitButton;

    @FXML
    public void initialize() {
        Font customFont = Font.loadFont(getClass().getResource("/com/example/harlequin/font/Pixeled.ttf").toExternalForm(), 20);

        if (customFont == null) {
            System.err.println("Custom font is null!");
        } else {
            startButton.setFont(customFont);
            exitButton.setFont(customFont);
        }
    }

    @FXML
    public void startGame(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("characterSelect.fxml"));
            startButton.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void exitGame(ActionEvent event) {
        System.exit(0);
    }
}
