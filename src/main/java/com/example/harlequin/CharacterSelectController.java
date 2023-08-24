package com.example.harlequin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class CharacterSelectController {

    @FXML
    public void selectCharacter(MouseEvent event) {
        try {
            AnchorPane gameRoot = FXMLLoader.load(getClass().getResource("game.fxml"));
            Stage stage = (Stage) ((StackPane) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(gameRoot, 1920, 1080);
            stage.setScene(scene);
            stage.show();

            gameRoot.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
