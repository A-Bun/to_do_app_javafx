package com.todo;

import java.io.IOException;

import javafx.fxml.FXML;

public class SecondaryController {

    @FXML
    @SuppressWarnings("unused")
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}