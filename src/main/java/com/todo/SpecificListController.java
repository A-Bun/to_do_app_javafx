package com.todo;

import java.io.IOException;

import javafx.fxml.FXML;

public class SpecificListController {

    @FXML
    @SuppressWarnings("unused")
    private void switchToAllListsView() throws IOException {
        App.setRoot("AllListsView");
    }
}