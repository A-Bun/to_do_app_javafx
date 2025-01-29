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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SpecificListController implements Initializable {
    @FXML private BorderPane border_pane;
    @FXML private ScrollPane center_content;
    @FXML private Label specific_list_label;
    @FXML private Button specific_list_edit;
    @FXML private HBox top_content;
    @FXML private VBox list_container;
    private ArrayList<ListItem> list_items;
    private SQLController db;
    private boolean editing = false;

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
        
        list_items = db.getList(current_list);

        if(list_items.isEmpty()) {
            /* Start the page in Edit mode to see the Add Item button */

            initiateEditMode();
        }

        for (int i = 0; i < list_items.size(); i++) {
            ListItem item = list_items.get(i);

            Button current_item = new Button(item.getName());
            current_item.getStyleClass().add("container_sub_child");
            // current_item.setMinWidth(current_item_box.getWidth());
            current_item.setOnAction((ActionEvent e) -> {
                if(current_item.getStyleClass().contains("checked_item")) {
                    current_item.getStyleClass().remove(current_item.getStyleClass().size()-1);
                }
                else {
                    current_item.getStyleClass().add("checked_item");
                }
            });

            createContainerChild(current_item, 0);
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void switchToAllListsView() throws IOException {
        App.setRoot("AllListsView");
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleEditAction(ActionEvent e) {
        editing = !editing;

        if(editing) {
            initiateEditMode();
        }
        else {
            concludeEditMode();
        }
    }

    private void createContainerChild(Node sub_child, int placement) {
        HBox current_item_box = new HBox();
        current_item_box.setFillHeight(true);
        current_item_box.getStyleClass().add("container_sub");
        current_item_box.setAlignment(Pos.CENTER_LEFT);

        current_item_box.getChildren().add(sub_child);

        list_container.getChildren().add(list_container.getChildren().size() - placement, current_item_box);
    }

    private void initiateEditMode() {
        System.out.println("Edit Mode On");

        /* Add an "Add Item" button to the end of the list_container (similar to the Create New List button) */
        Button add_item_button = new Button("Add New Item to List");
        add_item_button.getStyleClass().add("container_sub_child");
        add_item_button.setOnAction((ActionEvent e) -> {
            // create textfield
            // create container child using textfield placement 1
            System.out.println("adding new item...");
        });
        list_container.getChildren().add(add_item_button);

        /* loop over current list items and add new ones as TextFields second to last in list_container (before Add Item button) */
        // specific_list_edit.getStyleClass().remove(specific_list_edit.getStyleClass().size()-1);
        specific_list_edit.getStyleClass().add("edit_button_on");

        ContextMenu label_menu = new ContextMenu();
        MenuItem label_rename = new MenuItem("Rename");
        label_rename.setOnAction((ActionEvent e) -> {
            TextField rename_item = new TextField(specific_list_label.getText());
            rename_item.getStyleClass().add("container_sub_child_tf");
            rename_item.setMaxWidth(500);
            HBox.setHgrow(rename_item, Priority.ALWAYS);
            rename_item.setOnAction((ActionEvent ae) -> {
            String trimmed_name = rename_item.getText().trim();
            if(!trimmed_name.isBlank()) {
                specific_list_label.setText(trimmed_name);
                top_content.getChildren().remove(rename_item);
                top_content.getChildren().add(1, specific_list_label);
            }});
            top_content.getChildren().remove(specific_list_label);
            top_content.getChildren().add(1, rename_item);
        });
        label_menu.getItems().add(label_rename);
        specific_list_label.setContextMenu(label_menu);

        for (int i = 0; i < list_container.getChildren().size()-1; i++) {
            // get the button
            HBox child_list_hbox = (HBox)list_container.getChildren().get(i);

            Button item = (Button)child_list_hbox.getChildren().get(0);

            ContextMenu item_menu = new ContextMenu();
            MenuItem menu_rename = new MenuItem("Rename");
            menu_rename.setOnAction((ActionEvent e) -> {
                TextField rename_item = new TextField(item.getText());
                rename_item.getStyleClass().add("container_sub_child_tf");
                rename_item.setOnAction((ActionEvent ae) -> {
                    String trimmed_name = rename_item.getText().trim();
                    if(!trimmed_name.isBlank()) {
                        item.setText(trimmed_name);
                        child_list_hbox.getChildren().remove(rename_item);
                        child_list_hbox.getChildren().add(item);
                    }});
                child_list_hbox.getChildren().remove(item);
                child_list_hbox.getChildren().add(rename_item);
            });
            item_menu.getItems().add(menu_rename);
            item.setContextMenu(item_menu);
        }
    }

    private void concludeEditMode() {
        System.out.println("Edit Mode Off");

        /* Remove "Add Item" button from the list_container */
        specific_list_edit.getStyleClass().remove(specific_list_edit.getStyleClass().size()-1);
        specific_list_edit.getStyleClass().add("edit_button");
        list_container.getChildren().remove(list_container.getChildren().size()-1);

        /* loop over current list items, change them back to buttons */
    }

    @FXML
    @SuppressWarnings("unused")
    private void closeDB(ActionEvent e) {
        db.closeConnection();
    }
}