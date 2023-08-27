package com.example.harlequin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MenuController {

    @FXML
    private Button startButton;

    @FXML
    private Button exitButton;

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