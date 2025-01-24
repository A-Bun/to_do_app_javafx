package com.todo;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class SpecificListController implements Initializable {
    @FXML private Label SpecificListLabel;
    private SQLController db;

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        String current_list = App.getSpecificList();
        SpecificListLabel.setText(current_list);

        db = App.getSQLController();

        db.doNothing();

        /* Grab specific list's items from DB 
         * Display each as its own button
        */

        System.out.println("Specific List: " + current_list);
    }

    @FXML
    @SuppressWarnings("unused")
    private void switchToAllListsView() throws IOException {
        App.setRoot("AllListsView");
    }
}