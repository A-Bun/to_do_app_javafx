package com.todo;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class SettingsController implements Initializable{
    private SQLController db;

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        db = App.getSQLController();

        final Stage stage = App.getRoot();
        stage.setOnCloseRequest((WindowEvent we) -> {
            we.consume();

            try {
                if(!db.switchCollections("Lists")) {
                    stage.close();
                    return;
                }
                App.setRoot("AllListsView");

                // update the close request to actually close this window again (instead of preventing it)
                stage.setOnCloseRequest((WindowEvent wex) -> {
                    db.closeConnection();
                });
            }
            catch (IOException e) {
                System.err.println("Scene switch failed...");
            }
        });

        if(!db.switchCollections("Settings")) {
            stage.close();
        }
    }
}
