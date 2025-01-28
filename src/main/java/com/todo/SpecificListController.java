package com.todo;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SpecificListController implements Initializable {
    @FXML private BorderPane border_pane;
    @FXML private ScrollPane center_content;
    @FXML private Label specific_list_label;
    @FXML private VBox list_container;
    private ArrayList<ListItem> list_items;
    private SQLController db;

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        border_pane.setLeft(null);
        border_pane.setBottom(null);
        border_pane.setCenter(center_content);
        
        String current_list = App.getSpecificList();
        specific_list_label.setText(current_list);

        db = App.getSQLController();
        System.out.println("Specific List: " + current_list);

        /* Grab specific list's items from DB 
         * Display each as its own button
        */
        list_items = db.getList(current_list);

        if(list_items.isEmpty()) {
            /* Start the page in Edit mode to see the Add Item button */

            initiateEditMode();
            concludeEditMode();
        }

        for (int i = 0; i < list_items.size(); i++) {
            ListItem item = list_items.get(i);

            Button current_item = new Button(item.getName());
            current_item.getStyleClass().add("container_sub_child");
            // current_item.setMinWidth(current_item_box.getWidth());
            current_item.setOnAction((ActionEvent e) -> {
                db.doNothing();
            });

            createContainerChild(current_item);
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void switchToAllListsView() throws IOException {
        App.setRoot("AllListsView");
    }

    private void createContainerChild(Node sub_child) {
        HBox current_item_box = new HBox();
        current_item_box.setFillHeight(true);
        current_item_box.getStyleClass().add("container_sub");
        current_item_box.setAlignment(Pos.CENTER_LEFT);

        current_item_box.getChildren().add(sub_child);

        list_container.getChildren().add(list_container.getChildren().size(), current_item_box);
    }

    private void initiateEditMode() {
        /* Add an "Add Item" button to the end of the list_container (similar to the Create New List button) */

        /* loop over current list items and add new ones as TextFields second to last in list_container (before Add Item button) */
    }

    private void concludeEditMode() {
        /* Remove "Add Item" button from the list_container */

        /* loop over current list items, change them back to buttons */
    }
}