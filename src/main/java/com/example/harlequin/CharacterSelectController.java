package com.example.harlequin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class CharacterSelectController {

    @FXML
    public void selectCharacter(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("game.fxml"));
            ((StackPane) event.getSource()).getScene().setRoot(root);
            root.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
