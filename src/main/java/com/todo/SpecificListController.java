package com.todo;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
    @FXML private Button all_lists_button;
    @FXML private Button specific_list_edit;
    @FXML private Button save_button;
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

        all_lists_button.getStyleClass().add("all_lists_border");
        specific_list_edit.getStyleClass().add("edit_border");
        
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
                toggleItemStatus(current_item);
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

        save_button.setDisable(true);

        /* Add an "Add Item" button to the end of the list_container (similar to the Create New List button) */
        Button add_item_button = new Button("Add New Item to List");
        add_item_button.getStyleClass().add("container_sub_child");
        add_item_button.setOnAction((ActionEvent e) -> {
            // create textfield
            // create container child using textfield placement 1
            System.out.println("adding new item...");
            
            Button new_item_button = new Button();
            new_item_button.getStyleClass().add("container_sub_child");
            new_item_button.setOnAction((ActionEvent ae) -> {
                toggleItemStatus(new_item_button);
            });
            
            TextField new_item = new TextField();
            new_item.getStyleClass().add("container_sub_child_tf");
            new_item.setOnAction((ActionEvent ae) -> {
                String trimmed_name = new_item.getText().trim();
                if(!trimmed_name.isBlank()) {
                    new_item_button.setText(trimmed_name);
                    HBox new_hbox = (HBox)new_item.getParent();
                    new_hbox.getChildren().remove(new_item);
                    new_hbox.getChildren().add(new_item_button);
                    toggleListStatus();
                }});
            createContainerChild(new_item, 1);
        });
        list_container.getChildren().add(add_item_button);

        /* loop over current list items and add new ones as TextFields second to last in list_container (before Add Item button) */
        // specific_list_edit.getStyleClass().remove(specific_list_edit.getStyleClass().size()-1);
        specific_list_edit.getStyleClass().add("edit_button_on");
        specific_list_label.setContextMenu(addNewMenuItem(new ContextMenu(), "Rename", (ActionEvent e) -> {
                TextField rename_item = new TextField(specific_list_label.getText());
                rename_item.getStyleClass().add("list_header_tf");
                rename_item.setMaxWidth(500);
                rename_item.setPrefHeight(all_lists_button.getHeight());
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
                HBox.setMargin(rename_item, specific_list_label.getInsets());
            }));

        for (int i = 0; i < list_container.getChildren().size()-1; i++) {
            // get the button
            HBox child_list_hbox = (HBox)list_container.getChildren().get(i);

            Button item = (Button)child_list_hbox.getChildren().get(0);

            item.setContextMenu(addNewMenuItem(new ContextMenu(), "Rename", (ActionEvent e) -> {
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
                }));
        }
    }

    private void concludeEditMode() {
        System.out.println("Edit Mode Off");

        save_button.setDisable(false);

        /* Remove "Add Item" button from the list_container */
        specific_list_edit.getStyleClass().remove("edit_button_on");
        list_container.getChildren().remove(list_container.getChildren().size()-1);

        /* loop over current list items, change them back to buttons */
    }

    private void toggleItemStatus(Button item_button) {
        if(item_button.getStyleClass().contains("checked_item")) {
            item_button.getStyleClass().remove("checked_item");
        }
        else {
            item_button.getStyleClass().add("checked_item");
        }

        toggleListStatus();
    }

    private void toggleListStatus() {
        int child_count = list_container.getChildren().size();
        int child_id = 0;
        boolean all_checked = true;

        if(editing) {
            child_count--;
            // child_id++; // add back with delete button in edit mode
        }

        for(int i = 0; i < child_count; i++) {
            HBox child_hbox = (HBox)list_container.getChildren().get(i);

            if(!child_hbox.getChildren().get(child_id).getStyleClass().contains("checked_item")) {
                all_checked = false;
            }
        }

        if(all_checked) {
            specific_list_label.getStyleClass().add("checked_item");
        }
        else {
            specific_list_label.getStyleClass().remove("checked_item");
        }
    }

    private ContextMenu addNewMenuItem(ContextMenu menu, String menu_item_name, EventHandler<ActionEvent> menu_item_event) {
        MenuItem menu_item = new MenuItem(menu_item_name);
        menu_item.setOnAction(menu_item_event);
        menu.getItems().add(menu_item);
        return menu;
    }

    @FXML
    @SuppressWarnings("unused")
    private void saveList() {
        // clear the existing ArrayList of ListItems
        list_items.clear();

        // loop over list_container's children
        for (int i = 0; i < list_container.getChildren().size(); i++) {
            HBox child_hbox = (HBox)list_container.getChildren().get(i);
            Button item = (Button)child_hbox.getChildren().get(0);
            boolean is_checked = false;

            if(item.getStyleClass().contains("checked_item")) {
                is_checked = true;
            }
            
            // for the current button, create a new ListItem that uses the button's text as the name and the button's checked_item style as the status
            ListItem list_item = new ListItem(item.getText(), is_checked);

            // store that new ListItem in the ArrayList
            list_items.add(list_item);
        }

        System.out.println("app list: " + App.getSpecificList() + "; new list: " + specific_list_label.getText());

        // call the SQLController's updateList() function with App.getSpecificList(), specific_list_label.getText(), and the ArrayList
        db.updateList(App.getSpecificList(), specific_list_label.getText(), list_items);

        // output save message to console
        System.out.println("List Successfully Saved!");
    }

    @FXML
    @SuppressWarnings("unused")
    private void closeDB(ActionEvent e) {
        db.closeConnection();
    }
}