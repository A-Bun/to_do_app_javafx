package com.todo;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

public class SettingsController implements Initializable{
    private SQLController db;

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        db = App.getSQLController();

        App.getRoot(); // DELETE THIS

        System.out.println("Undo Limit: " + db.GetSetting("Undo Limit"));

        /* Loop over settings documents from DB and manually display each one */
    }

    private void switchToAllListsView() throws IOException {
        App.setRoot("AllListsView");
    }

    @FXML
    @SuppressWarnings("unused")
    private void exitDialog() {
        try {
            System.out.println("Exit Granted.");
            switchToAllListsView();
        } catch (IOException ex) {
            System.err.println("Exit Failed.");
        }
    }
}
