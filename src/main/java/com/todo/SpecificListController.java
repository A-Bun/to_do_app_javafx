package com.todo;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.ResourceBundle;

import org.bson.types.ObjectId;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
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
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SpecificListController implements Initializable {
    @FXML private BorderPane border_pane;
    @FXML private ScrollPane center_content;
    @FXML private Label specific_list_label;
    @FXML private Button all_lists_button;
    @FXML private Button specific_list_edit;
    @FXML private Button undo_button;
    @FXML private Button redo_button;
    @FXML private Button save_button;
    @FXML private HBox top_content;
    @FXML private VBox list_container;
    private ArrayList<ListItem> list_items;
    private SQLController db;
    private boolean editing = false;
    private final int undo_limit = 50;
    private ListState initial_state;
    private final Deque<ListState> undo_stack = new ArrayDeque<>();
    private final Deque<ListState> redo_stack = new ArrayDeque<>();

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        border_pane.setLeft(null);
        border_pane.setBottom(null);
        border_pane.setCenter(center_content);

        all_lists_button.getStyleClass().add("all_lists_border");
        specific_list_edit.getStyleClass().add("edit_border");
        undo_button.setDisable(true);
        redo_button.setDisable(true);
        
        String current_list = App.getSpecificList();
        specific_list_label.setText(current_list);

        db = App.getSQLController();
        System.out.println("Specific List: " + current_list);
        
        list_items = db.getList(current_list);

        initial_state = new ListState(current_list, list_items);
        // initial_state.printState();

        if(list_items.isEmpty()) {
            /* Start the page in Edit mode to see the Add Item button */

            editing = true;
            initiateEditMode();
            specific_list_edit.setDisable(true);
        }
        else {
            for (int i = 0; i < list_items.size(); i++) {
                ListItem item = list_items.get(i);

                Button current_item = new Button(item.getName());
                current_item.getStyleClass().add("container_sub_child");
                // current_item.setMinWidth(current_item_box.getWidth());
                current_item.setOnAction((ActionEvent e) -> {
                    toggleItemStatus(current_item);
                });

                if(item.getStatus()) {
                    current_item.getStyleClass().add("checked_item");
                }
                
                ArrayList<Node> new_nodes = new ArrayList<>();
                new_nodes.add(current_item);

                createContainerChild(new_nodes, 0);
            }
            
            toggleListStatus();
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

    private HBox createContainerChild(ArrayList<Node> sub_children, int placement) {
        HBox current_item_box = new HBox();
        current_item_box.setFillHeight(true);
        current_item_box.getStyleClass().add("container_sub");
        current_item_box.setAlignment(Pos.CENTER_LEFT);

        for(Node sub_child : sub_children) {
            current_item_box.getChildren().add(sub_child);
        }

        list_container.getChildren().add(list_container.getChildren().size() - placement, current_item_box);

        return current_item_box;
    }

    private void initiateEditMode() {
        System.out.println("Edit Mode On");

        save_button.setDisable(true);

        /* Add an "Add Item" button to the end of the list_container (similar to the Create New List button) */
        Button add_item_button = new Button("Add New Item to List");
        add_item_button.getStyleClass().add("container_sub_child");
        add_item_button.setOnAction((ActionEvent e) -> {
            System.out.println("adding new item...");
            specific_list_edit.setDisable(true);
            undo_button.setDisable(true);
            redo_button.setDisable(true);
            
            Button new_item_button = new Button();
            new_item_button.getStyleClass().add("container_sub_child");
            new_item_button.setOnAction((ActionEvent ae) -> {
                toggleItemStatus(new_item_button);
            });

            new_item_button.setContextMenu(addNewMenuItem(new ContextMenu(), "Rename", (ActionEvent ae) -> {
                specific_list_edit.setDisable(true);
                undo_button.setDisable(true);
                redo_button.setDisable(true);
                TextField rename_item = new TextField(new_item_button.getText());
                rename_item.getStyleClass().add("container_sub_child_tf");
                rename_item.setOnAction((ActionEvent sae) -> {
                    String trimmed_name = rename_item.getText().trim();
                    if(!trimmed_name.isBlank()) {
                        new_item_button.setText(trimmed_name);
                        HBox child_hbox = (HBox)rename_item.getParent();
                        String item_id = list_items.get(list_container.getChildren().indexOf(child_hbox)).getId();
                        updateListItem(item_id, new_item_button.getText(), trimmed_name);
                        child_hbox.getChildren().indexOf(rename_item);
                        child_hbox.getChildren().remove(rename_item);
                        child_hbox.getChildren().add(new_item_button);
                        updateStack();
                        if(!stillRenaming()) {
                            specific_list_edit.setDisable(false);
                            enableUndoRedo();
                        }
                    }});
                HBox child_hbox = (HBox)new_item_button.getParent();
                child_hbox.getChildren().remove(new_item_button);
                child_hbox.getChildren().add(rename_item);
            }));

            Button delete_button = new Button();
            delete_button.setText("X");
            delete_button.getStyleClass().add("delete_button");
            delete_button.setOnAction((ActionEvent ae) -> {
                Node parent = delete_button.getParent();
                String item_id = list_items.get(list_container.getChildren().indexOf(parent)).getId();
                deleteListItem(item_id, new_item_button.getText());
                list_container.getChildren().remove(parent);
                updateStack();
                if(!stillRenaming()) {
                    specific_list_edit.setDisable(false);
                    enableUndoRedo();
                }

                if(list_container.getChildren().size() <= 1) {
                    specific_list_edit.setDisable(true);
                }
                toggleListStatus();
            });

            ArrayList<Node> new_nodes = new ArrayList<>();
            new_nodes.add(delete_button);

            HBox new_hbox = createContainerChild(new_nodes, 1);
            
            TextField new_item = new TextField();
            new_item.getStyleClass().add("container_sub_child_tf");
            new_item.setOnAction((ActionEvent ae) -> {
                String trimmed_name = new_item.getText().trim();
                if(!trimmed_name.isBlank()) {
                    new_item_button.setText(trimmed_name);
                    HBox parent_hbox = (HBox)new_item.getParent();
                    parent_hbox.getChildren().remove(new_item);
                    parent_hbox.getChildren().add(new_item_button);
                    if(!stillRenaming()) {
                        specific_list_edit.setDisable(false);
                        enableUndoRedo();
                    }
                    addListItem(trimmed_name, false);
                    updateStack();
                    toggleListStatus();
                }});
            new_hbox.getChildren().add(new_item);
        });
        list_container.getChildren().add(add_item_button);

        /* loop over current list items and add new ones as TextFields second to last in list_container (before Add Item button) */
        // specific_list_edit.getStyleClass().remove(specific_list_edit.getStyleClass().size()-1);
        specific_list_edit.getStyleClass().add("edit_button_on");
        specific_list_label.setContextMenu(addNewMenuItem(new ContextMenu(), "Rename", (ActionEvent e) -> {
                specific_list_edit.setDisable(true);
                undo_button.setDisable(true);
                redo_button.setDisable(true);
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
                        updateStack();
                        if(!stillRenaming()) {
                            specific_list_edit.setDisable(false);
                            enableUndoRedo();
                        }
                    }});
                top_content.getChildren().remove(specific_list_label);
                top_content.getChildren().add(1, rename_item);
                HBox.setMargin(rename_item, specific_list_label.getInsets());
            }));

        for (int i = 0; i < list_container.getChildren().size()-1; i++) {
            HBox child_list_hbox = (HBox)list_container.getChildren().get(i);

            Button item = (Button)child_list_hbox.getChildren().get(0);

            Button delete_button = new Button();
            delete_button.setText("X");
            delete_button.getStyleClass().add("delete_button");
            delete_button.setOnAction((ActionEvent e) -> {
                String item_id = list_items.get(list_container.getChildren().indexOf(child_list_hbox)).getId();
                deleteListItem(item_id, item.getText());
                list_container.getChildren().remove(child_list_hbox);
                updateStack();
                if(!stillRenaming()) {
                    specific_list_edit.setDisable(false);
                    enableUndoRedo();
                }

                if(list_container.getChildren().size() <= 1) {
                    specific_list_edit.setDisable(true);
                }
                toggleListStatus();
            });
            child_list_hbox.getChildren().add(0, delete_button);

            item.setContextMenu(addNewMenuItem(new ContextMenu(), "Rename", (ActionEvent e) -> {
                    specific_list_edit.setDisable(true);
                    undo_button.setDisable(true);
                    redo_button.setDisable(true);
                    TextField rename_item = new TextField(item.getText());
                    rename_item.getStyleClass().add("container_sub_child_tf");
                    rename_item.setOnAction((ActionEvent ae) -> {
                        String trimmed_name = rename_item.getText().trim();
                        if(!trimmed_name.isBlank()) {
                            String item_id = list_items.get(list_container.getChildren().indexOf(child_list_hbox)).getId();
                            updateListItem(item_id, item.getText(), trimmed_name);
                            item.setText(trimmed_name);
                            child_list_hbox.getChildren().remove(rename_item);
                            child_list_hbox.getChildren().add(item);
                            updateStack();
                            if(!stillRenaming()) {
                                specific_list_edit.setDisable(false);
                                enableUndoRedo();
                            }
                        }});
                    child_list_hbox.getChildren().remove(item);
                    child_list_hbox.getChildren().add(rename_item);
                }));
        }
    }

    private void concludeEditMode() {
        if(stillRenaming()) {
            System.out.println("Renaming Action Incomplete - Edit Mode is still on...");
            editing = true;
            return;
        }

        System.out.println("Edit Mode Off");

        save_button.setDisable(false);
        specific_list_edit.setDisable(false);
        enableUndoRedo();

        specific_list_edit.getStyleClass().remove("edit_button_on");
        specific_list_label.getContextMenu().getItems().clear();
        list_container.getChildren().remove(list_container.getChildren().size()-1);

        for (int i = 0; i < list_container.getChildren().size(); i++) {
            HBox child_list_hbox = (HBox)list_container.getChildren().get(i);
            child_list_hbox.getChildren().remove(0);

            Button item = (Button)child_list_hbox.getChildren().get(0);

            item.getContextMenu().getItems().clear();
        }
    }

    private boolean stillRenaming() {
        boolean result = false;
        if(top_content.getChildren().get(1).getClass().getSimpleName().equals("TextField")) {
            result = true;
        }
        
        for (int i = 0; i < list_container.getChildren().size()-1; i++) {
            HBox child_list_hbox = (HBox)list_container.getChildren().get(i);

            if(child_list_hbox.getChildren().get(0).getClass().getSimpleName().equals("TextField")) {
                result = true;
                break;
            }
        }

        return result;
    }

    private void toggleItemStatus(Button item_button) {
        boolean check;

        if(item_button.getStyleClass().contains("checked_item")) {
            item_button.getStyleClass().remove("checked_item");
            check = false;
        }
        else {
            item_button.getStyleClass().add("checked_item");
            check = true;
        }

        Node parent = item_button.getParent();
        String item_id = list_items.get(list_container.getChildren().indexOf(parent)).getId();
        updateListItem(item_id, item_button.getText(), check);
        updateStack();

        toggleListStatus();
    }

    private void toggleListStatus() {
        int child_count = list_container.getChildren().size();
        int child_id = 0;
        boolean all_checked = true;

        if(editing) {
            child_count--;
            child_id = 1;
        }

        for(int i = 0; i < child_count; i++) {
            HBox child_hbox = (HBox)list_container.getChildren().get(i);

            if(!child_hbox.getChildren().get(child_id).getStyleClass().contains("checked_item")) {
                all_checked = false;
            }
        }

        if(all_checked && !specific_list_label.getStyleClass().contains("checked_item")) {
            specific_list_label.getStyleClass().add("checked_item");
        }
        else if (!all_checked) {
            specific_list_label.getStyleClass().remove("checked_item");
        }
    }

    private void enableUndoRedo() {
        if(!undo_stack.isEmpty()) {
            undo_button.setDisable(false);
        }

        if(!redo_stack.isEmpty()) {
            redo_button.setDisable(false);
        }
    }

    private ContextMenu addNewMenuItem(ContextMenu menu, String menu_item_name, EventHandler<ActionEvent> menu_item_event) {
        MenuItem menu_item = new MenuItem(menu_item_name);
        menu_item.setOnAction(menu_item_event);
        menu.getItems().add(menu_item);
        return menu;
    }

    private ListItem getListItem(String item_id, String item_name) {
        ListItem list_item = null;
        
        for(int i = 0; i < list_items.size(); i++) {
            if(list_items.get(i).getName().equals(item_name) && list_items.get(i).getId().equals(item_id)) {
                list_item = list_items.get(i);
            }
        }

        return list_item;
    }

    private void addListItem(String item_name, boolean item_status) {
        ObjectId item_id = new ObjectId();
        ListItem new_list_item = new ListItem(item_id.toString(), item_name, item_status);
        list_items.add(new_list_item);

        System.out.println("Added new list item \"" + item_name + "\"");
    }

    private void deleteListItem(String item_id, String item_name) {
        ListItem item_to_delete = getListItem(item_id, item_name);
        list_items.remove(item_to_delete);

        System.out.println("Deleted list item \"" + item_name + "\"");
    }

    private void updateListItem(String item_id, String item_name, String new_item_name) {
        ListItem item_to_update = getListItem(item_id, item_name);
        if(new_item_name != null && !new_item_name.equals(item_name))
        {
            item_to_update.setName(new_item_name);
        
            System.out.println("Updated list item name to \"" + new_item_name + "\"");
        }
    }

    private void updateListItem(String item_id, String item_name, boolean item_status) {
        ListItem item_to_update = getListItem(item_id, item_name);

        if(item_status != item_to_update.getStatus()) {
            item_to_update.setStatus(item_status);
        }
        
        System.out.println("Updated status of list item \"" + item_name + "\"");
    }

    private void updateStack() {
        if(undo_stack.size() >= undo_limit) {
            System.out.println("   Removing bottom entry of Undo stack...");
            initial_state = undo_stack.pollLast();
        }

        ListState curr_state = new ListState(specific_list_label.getText(), list_items);

        undo_stack.offerFirst(curr_state);

        if(!redo_stack.isEmpty()) {
            System.out.println("   Clearing Redo stack...");
            redo_stack.clear();
            redo_button.setDisable(true);
        }

        // System.out.println("   Updating Undo stack... New Size = " + undo_stack.size());

        enableUndoRedo();
    }

    private void updateListState(ListState state) {
        specific_list_label.setText(state.getName().trim());

        ArrayList<ListItem> updated_state_items = new ArrayList<>();

        for (ListItem item : state.getItems()) {
            updated_state_items.add(new ListItem(item.getId(), item.getName(), item.getStatus()));
        }

        list_items = updated_state_items;

        int button_cnt = list_container.getChildren().size();
        int child_id = 0;
        int loop_cnt = list_items.size();

        if(editing) {
            button_cnt--;
            child_id++;
        }

        if(button_cnt > loop_cnt) {
            loop_cnt = button_cnt;
        }

        for (int i = 0; i < loop_cnt; i++) {
            if(i > button_cnt-1) { /* add new button */
                ListItem item = list_items.get(i);
                ArrayList<Node> new_nodes = new ArrayList<>();

                Button item_button = new Button(item.getName());
                item_button.getStyleClass().add("container_sub_child");
                // item_button.setMinWidth(item_button_box.getWidth());
                item_button.setOnAction((ActionEvent e) -> {
                    toggleItemStatus(item_button);
                });

                if(item.getStatus()) {
                    item_button.getStyleClass().add("checked_item");
                }
                
                new_nodes.add(item_button);

                if(editing) {
                    item_button.setContextMenu(addNewMenuItem(new ContextMenu(), "Rename", (ActionEvent ae) -> {
                        specific_list_edit.setDisable(true);
                        undo_button.setDisable(true);
                        redo_button.setDisable(true);
                        TextField rename_item = new TextField(item_button.getText());
                        rename_item.getStyleClass().add("container_sub_child_tf");
                        rename_item.setOnAction((ActionEvent sae) -> {
                            String trimmed_name = rename_item.getText().trim();
                            if(!trimmed_name.isBlank()) {
                                item_button.setText(trimmed_name);
                                HBox child_hbox = (HBox)rename_item.getParent();
                                ListItem item_to_rename = list_items.get(list_container.getChildren().indexOf(child_hbox));
                                updateListItem(item_to_rename.getId(), item_to_rename.getName(), trimmed_name);
                                child_hbox.getChildren().remove(rename_item);
                                child_hbox.getChildren().add(item_button);
                                updateStack();
                                if(!stillRenaming()) {
                                    specific_list_edit.setDisable(false);
                                    enableUndoRedo();
                                }
                            }});
                        HBox child_hbox = (HBox)item_button.getParent();
                        child_hbox.getChildren().remove(item_button);
                        child_hbox.getChildren().add(rename_item);
                    }));

                    Button delete_button = new Button();
                    delete_button.setText("X");
                    delete_button.getStyleClass().add("delete_button");
                    delete_button.setOnAction((ActionEvent e) -> {
                        ListItem item_to_delete = list_items.get(list_container.getChildren().indexOf(delete_button.getParent()));
                        deleteListItem(item_to_delete.getId(), item_to_delete.getName());
                        list_container.getChildren().remove(delete_button.getParent());
                        updateStack();
                        if(!stillRenaming()) {
                            specific_list_edit.setDisable(false);
                            enableUndoRedo();
                        }

                        if(list_container.getChildren().size() <= 1) {
                            specific_list_edit.setDisable(true);
                        }
                        toggleListStatus();
                    });

                    new_nodes.add(0, delete_button);
                    createContainerChild(new_nodes, 1);
                }
                else {
                    createContainerChild(new_nodes, 0);
                }
            }
            else if(i > list_items.size()-1) { /* delete the button */
               list_container.getChildren().remove(i);
            }
            else { /* update the button text */
                ListItem item = list_items.get(i);

                HBox child_hbox = (HBox)list_container.getChildren().get(i);
                Button item_button = ((Button)child_hbox.getChildren().get(child_id));
                
                item_button.setText(item.getName().trim());   
                
                if(item_button.getStyleClass().contains("checked_item") && !item.getStatus()) {
                    item_button.getStyleClass().remove("checked_item");
                }
                else if (!item_button.getStyleClass().contains("checked_item") && item.getStatus()) {
                    item_button.getStyleClass().add("checked_item");
                }
            }
        }
        
        toggleListStatus();
        
        // System.out.println("   Updating List State...");
    }

    @FXML
    @SuppressWarnings("unused")
    private void undo() {
        ListState state_to_use;

        redo_stack.offerFirst(undo_stack.pollFirst());

        // System.out.println("   Undo-ing... State pushed to Redo stack... Undo stack count: " + undo_stack.size());

        // System.out.println("Here is the state just pushed onto the Redo stack:");
        // redo_state.printState();

        if(!undo_stack.isEmpty()) {
            state_to_use = undo_stack.peekFirst();
        }
        else {
            state_to_use = initial_state;
        }

        updateListState(state_to_use);

        if(undo_stack.isEmpty()) {
            undo_button.setDisable(true);
        }

        enableUndoRedo();
    }

    @FXML
    @SuppressWarnings("unused")
    private void redo() {
        ListState state_to_use;

        undo_stack.offerFirst(redo_stack.peekFirst());

        // System.out.println("   Redo-ing... State pushed to Undo stack... Redo stack count: " + redo_stack.size());

        // System.out.println("Here is the state just pushed onto the Undo stack:");
        // undo_state.printState();

        if(!redo_stack.isEmpty()) {
            state_to_use = redo_stack.pollFirst();
        }
        else {
            state_to_use = initial_state;
        }

        updateListState(state_to_use);

        if(redo_stack.isEmpty()) {
            redo_button.setDisable(true);
        }

        enableUndoRedo();
    }

    @FXML
    @SuppressWarnings("unused")
    private void saveDialog() { 
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        // dialog.initOwner(App.getRoot()); -- might not need this (custom funtion that returns stage from App)
        dialog.setResizable(false);
        dialog.setTitle("Save Changes?");
        
        // create outer dialog box
        VBox dialog_vbox = new VBox(20);
        dialog_vbox.setAlignment(Pos.CENTER);
        dialog_vbox.getStyleClass().add("full_page");
        dialog_vbox.setPadding(new Insets(20));

        // create details of dialog box
        Label dialog_label = new Label("Are you sure you want to save your changes?");
        dialog_label.setTextAlignment(TextAlignment.CENTER);
        dialog_label.getStyleClass().add("header_label");

        HBox dialog_hbox = new HBox(20);
        dialog_hbox.setAlignment(Pos.CENTER);
        dialog_hbox.setFillHeight(true);

        Button yes_button = new Button("Yes, Save");
        yes_button.getStyleClass().add("container_sub_child");
        yes_button.setStyle("-fx-border-width: 1px; -fx-border-color: white;");
        yes_button.setOnAction((ActionEvent e) -> {
            try {
                saveList();
                dialog.close();
            } catch (IOException ex) {
                System.err.println("Save Failed.");
            }
        });

        Button no_button = new Button("No, Go Back");
        no_button.getStyleClass().add("container_sub_child");
        no_button.setStyle("-fx-border-width: 1px; -fx-border-color: white;");
        no_button.setOnAction((ActionEvent e) -> {
            System.out.println("Save Cancelled.");
            dialog.close();
        });

        dialog_hbox.getChildren().add(yes_button);
        dialog_hbox.getChildren().add(no_button);

        dialog_vbox.getChildren().add(dialog_label);
        dialog_vbox.getChildren().add(dialog_hbox);

        // display dialog box
        Scene dialog_scene = new Scene(dialog_vbox, 300, 200);
        dialog_scene.getStylesheets().add(App.class.getResource("styles/Base_Style.css").toExternalForm());
        dialog.setScene(dialog_scene);
        dialog.show();
    }

    @FXML
    @SuppressWarnings("unused")
    private void saveList() throws IOException { 
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