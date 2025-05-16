package com.todo;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class SpecificListController implements Initializable {
    @FXML private BorderPane border_pane;
    @FXML private ScrollPane center_content;
    @FXML private Label specific_list_label;
    @FXML private Button all_lists_button;
    @FXML private Button specific_list_edit;
    @FXML private Button options_button;
    @FXML private Button undo_button;
    @FXML private Button redo_button;
    @FXML private Button save_button;
    @FXML private HBox top_content;
    @FXML private VBox list_container;
    private ArrayList<ListItem> list_items;
    private ArrayList<ListItem> default_order;
    private SQLController db;
    private boolean editing = false;
    private final int undo_limit = 50;
    private ListState original_state;
    private ListState base_state;
    private final Deque<ListState> undo_stack = new ArrayDeque<>();
    private final Deque<ListState> redo_stack = new ArrayDeque<>();
    // private final Scene curr_scene = App.getRoot().getScene();
    private final Image unchecked_image = new Image(App.class.getResource("images/Checkbox-Unchecked.png").toExternalForm());
    private final Image checked_image = new Image(App.class.getResource("images/Checkbox-Checked-V2.png").toExternalForm());

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
        save_button.setDisable(true);
        
        String current_list = App.getSpecificList();
        specific_list_label.setText(current_list);

        db = App.getSQLController();
        System.out.println("Specific List: " + current_list);
        
        list_items = db.getList(current_list);
        default_order = db.getList(current_list);

        original_state = new ListState(current_list, list_items);
        base_state = new ListState(current_list, list_items);
        // base_state.printState();

        initializeOptionsMenu();

        if(list_items.isEmpty()) {
            /* Start the page in Edit mode to see the Add Item button */

            editing = true;
            initiateEditMode();
            specific_list_edit.setDisable(true);
        }
        else {
            for (int i = 0; i < list_items.size(); i++) {
                ListItem item = list_items.get(i);

                ImageView checkbox = new ImageView(unchecked_image);
                checkbox.setPreserveRatio(true);
                checkbox.setFitWidth(18);
                Button current_item = new Button(item.getName());
                current_item.setGraphic(checkbox);
                current_item.setGraphicTextGap(6);
                current_item.getStyleClass().add("container_sub_child");
                // current_item.setMinWidth(current_item_box.getWidth());
                current_item.setOnAction((ActionEvent e) -> {
                    toggleItemStatus(current_item);
                });

                if(item.getStatus()) {
                    current_item.getStyleClass().add("checked_item");
                    checkbox.setImage(checked_image);
                }
                
                ArrayList<Node> new_nodes = new ArrayList<>();
                new_nodes.add(current_item);

                createContainerChild(new_nodes, 0);
            }
            
            toggleListStatus();

            list_container.setOnMouseEntered((MouseEvent e) -> {
                System.out.println("Resizing List Item Buttons");

                // Resize the list item buttons
                for(Object child : list_container.getChildren()) {
                    HBox child_hbox = (HBox)child;
                    Button button = (Button)child_hbox.getChildren().get(0);
                    button.setPrefWidth(child_hbox.getWidth());
                }

                // reset the mouse entered event
                list_container.setOnMouseEntered((MouseEvent ae) -> {});
            });
        }

        border_pane.setOnMouseClicked((MouseEvent me) -> {
            if(me.getButton() == MouseButton.BACK) {
                System.out.println("Exiting List via Mouse Back Button");
                exitDialog();
            }
            me.consume();
        });
    }

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
            add_item_button.setDisable(true);
            undo_button.setDisable(true);
            redo_button.setDisable(true);
            
            ImageView checkbox = new ImageView(unchecked_image);
            checkbox.setPreserveRatio(true);
            checkbox.setFitWidth(18);
            Button new_item_button = new Button();
            new_item_button.getStyleClass().add("container_sub_child");
            new_item_button.setGraphic(checkbox);
            new_item_button.setOnAction((ActionEvent ae) -> {
                toggleItemStatus(new_item_button);
            });
                        
            ArrayList<Node> new_nodes = new ArrayList<>();
            HBox new_hbox = createContainerChild(new_nodes, 1);

            Button reorder_button = reorderLogic(new_hbox, new_item_button);

            new_item_button.setContextMenu(addNewMenuItem(new ContextMenu(), "Rename", (ActionEvent ae) -> {
                specific_list_edit.setDisable(true);
                undo_button.setDisable(true);
                redo_button.setDisable(true);
                TextField rename_item = new TextField(new_item_button.getText());
                rename_item.getStyleClass().add("container_sub_child_tf");
                rename_item.setOnAction((ActionEvent sae) -> {
                    String trimmed_name = rename_item.getText().trim();
                    if(!trimmed_name.isBlank()) {
                        HBox child_hbox = (HBox)rename_item.getParent();
                        String item_id = list_items.get(list_container.getChildren().indexOf(child_hbox)).getId();
                        boolean updated = updateListItem(item_id, new_item_button.getText(), trimmed_name);
                        new_item_button.setText(trimmed_name);
                        child_hbox.getChildren().indexOf(rename_item);
                        child_hbox.getChildren().remove(rename_item);
                        child_hbox.getChildren().addAll(new_item_button, reorder_button);
                        
                        if(updated)
                        {
                            updateStack();
                        }

                        if(!stillRenaming()) {
                            specific_list_edit.setDisable(false);
                            enableUndoRedo();
                        }
                    }});
                HBox child_hbox = (HBox)new_item_button.getParent();
                child_hbox.getChildren().removeAll(new_item_button, reorder_button);
                child_hbox.getChildren().add(rename_item);
                rename_item.setPrefWidth(child_hbox.getWidth());

                // automatically focus this TextField and select all text
                rename_item.requestFocus();
                rename_item.selectAll();
            }));

            Button delete_button = new Button();
            delete_button.setText("X");
            delete_button.getStyleClass().add("delete_button");
            delete_button.setOnAction((ActionEvent ae) -> {
                Node parent = delete_button.getParent();
                int parent_index = list_container.getChildren().indexOf(parent);
                if(parent_index < list_items.size()) {
                    String item_id = list_items.get(parent_index).getId();
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
                }
                else {
                    list_container.getChildren().remove(parent);
                    if(!stillRenaming()) {
                        specific_list_edit.setDisable(false);
                        add_item_button.setDisable(false);
                        enableUndoRedo();
                    }
                }
            });

            new_hbox.getChildren().add(0, delete_button);
            
            TextField new_item = new TextField();
            new_item.getStyleClass().add("container_sub_child_tf");
            new_item.setOnAction((ActionEvent ae) -> {
                String trimmed_name = new_item.getText().trim();
                if(!trimmed_name.isBlank()) {
                    new_item_button.setText(trimmed_name);
                    HBox parent_hbox = (HBox)new_item.getParent();
                    new_item_button.setPrefWidth(parent_hbox.getWidth());
                    parent_hbox.getChildren().remove(new_item);
                    parent_hbox.getChildren().addAll(new_item_button, reorder_button);
                    if(!stillRenaming()) {
                        specific_list_edit.setDisable(false);
                        add_item_button.setDisable(false);
                        enableUndoRedo();
                    }
                    addListItem(trimmed_name, false);
                    updateStack();
                    toggleListStatus();
                }});
            new_hbox.getChildren().add(new_item);
            new_item.setPrefWidth(list_container.getWidth());
            new_item.requestFocus();
            new_item.selectAll();
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
                rename_item.requestFocus();
                rename_item.selectAll();
                HBox.setMargin(rename_item, specific_list_label.getInsets());
            }));

        for (int i = 0; i < list_container.getChildren().size()-1; i++) {
            HBox child_list_hbox = (HBox)list_container.getChildren().get(i);

            Button item = (Button)child_list_hbox.getChildren().get(0);

            Button reorder_button = reorderLogic(child_list_hbox, item);

            Button delete_button = new Button();
            delete_button.setText("X");
            delete_button.getStyleClass().add("delete_button");
            delete_button.setOnAction((ActionEvent e) -> {
                int parent_index = list_container.getChildren().indexOf(child_list_hbox);
                if(parent_index < list_items.size()) {
                    String item_id = list_items.get(parent_index).getId();
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
                }
                else {
                    list_container.getChildren().remove(child_list_hbox);
                    if(!stillRenaming()) {
                        specific_list_edit.setDisable(false);
                        add_item_button.setDisable(false);
                        enableUndoRedo();
                    }
                }
            });
            child_list_hbox.getChildren().add(0, delete_button);
            child_list_hbox.getChildren().add(reorder_button);

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
                            boolean updated = updateListItem(item_id, item.getText(), trimmed_name);
                            item.setText(trimmed_name);
                            child_list_hbox.getChildren().remove(rename_item);
                            child_list_hbox.getChildren().addAll(item, reorder_button);
                            
                            if(updated)
                            {
                                updateStack();
                            }

                            if(!stillRenaming()) {
                                specific_list_edit.setDisable(false);
                                enableUndoRedo();
                            }
                        }});
                    child_list_hbox.getChildren().removeAll(item, reorder_button);
                    child_list_hbox.getChildren().addAll(rename_item);
                    rename_item.setPrefWidth(child_list_hbox.getWidth());

                    rename_item.requestFocus();
                    rename_item.selectAll();
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

        specific_list_edit.setDisable(false);
        enableUndoRedo();
        toggleSave();

        specific_list_edit.getStyleClass().remove("edit_button_on");
        specific_list_label.getContextMenu().getItems().clear();
        list_container.getChildren().remove(list_container.getChildren().size()-1);

        for (int i = 0; i < list_container.getChildren().size(); i++) {
            HBox child_list_hbox = (HBox)list_container.getChildren().get(i);
            child_list_hbox.getChildren().remove(0);
            child_list_hbox.getChildren().remove(child_list_hbox.getChildren().size()-1);

            Button item = (Button)child_list_hbox.getChildren().get(0);

            item.getContextMenu().getItems().clear();
        }
    }

    private boolean stillRenaming() {
        boolean result = false;
        int child_id = 0;

        if(editing) {
            child_id++;
        }

        if(top_content.getChildren().get(1).getClass().getSimpleName().equals("TextField")) {
            result = true;
        }
        
        for (int i = 0; i < list_container.getChildren().size()-1; i++) {
            HBox child_list_hbox = (HBox)list_container.getChildren().get(i);

            if(child_list_hbox.getChildren().get(child_id).getClass().getSimpleName().equals("TextField")) {
                result = true;
                break;
            }
        }

        return result;
    }

    private void toggleItemStatus(Button item_button) {
        boolean check;
        ImageView checkbox = new ImageView();
        checkbox.setPreserveRatio(true);
        checkbox.setFitWidth(18);
        
        Node parent = item_button.getParent();
        ListItem item = list_items.get(list_container.getChildren().indexOf(parent));
        String item_id = item.getId();

        if(item_button.getStyleClass().contains("checked_item")) {
            item_button.getStyleClass().remove("checked_item");
            check = false;
            checkbox.setImage(unchecked_image);
            item_button.setGraphic(checkbox);

            int new_ind = findNewIndex(item);
            reorderList((HBox)parent, list_items.indexOf(item), new_ind);
        }
        else {
            item_button.getStyleClass().add("checked_item");
            check = true;
            checkbox.setImage(checked_image);
            item_button.setGraphic(checkbox);

            int new_ind = findLastUncheckedItem();
            reorderList((HBox)parent, list_items.indexOf(item), new_ind);
        }

        
        boolean updated = updateListItem(item_id, item_button.getText(), check);
        
        if(updated)
        {
            updateStack();
        }

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

    private int findLastCheckedItem() {
        // find index of last checked item
        int last_checked_id = -1;

        for (int i = 0; i < list_items.size(); i++) {
            if(list_items.get(i).getStatus()) {
                last_checked_id = i;
            }
        }

        return last_checked_id;
    }

    private int findLastUncheckedItem() {
        // find index of last unchecked item
        int last_unchecked_id = -1;

        for (int i = 0; i < list_items.size(); i++) {
            if(!list_items.get(i).getStatus()) {
                last_unchecked_id = i;
            }
        }

        return last_unchecked_id;
    }

    private int findNewIndex(ListItem item_to_find) {
        int ind = findLastUncheckedItem();
        System.out.println("ind: " + ind);
        int df_ind = -1;

        if(ind == -1) {
            return 0;
        }

        for (int i = 0; i < default_order.size(); i++) {
            if(default_order.get(i).getName().equals(item_to_find.getName()) && default_order.get(i).getId().equals(item_to_find.getId())) {
                df_ind = i;
                System.out.println("df_ind: " + df_ind);
                break;
            }
        }

        if(df_ind != -1) {
            for (int i = ind; i >= 0; i--) {
                int r_ind = -1;
                ListItem curr_item = list_items.get(i);
                
                for (int j = 0; j < default_order.size(); j++) {
                    if(default_order.get(j).getName().equals(curr_item.getName()) && default_order.get(j).getId().equals(curr_item.getId())) {
                        r_ind = j;
                        break;
                    }
                }

                System.out.println("r_ind: " + r_ind);

                if(r_ind == -1 || r_ind > df_ind) {
                    ind = i;
                    System.out.println("new ind: " + ind);
                }
                else if (r_ind < df_ind) {
                    ind = i+1;
                    break;
                }
            }
        }
        else {
            ind++;
        }

        System.out.println("Final ind: " + ind);

        return ind;
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

    private ListItem getListItem(ArrayList<ListItem> array_to_search, String item_id, String item_name) {
        ListItem list_item = null;
        
        for(int i = 0; i < array_to_search.size(); i++) {
            if(array_to_search.get(i).getName().equals(item_name) && array_to_search.get(i).getId().equals(item_id)) {
                list_item = array_to_search.get(i);
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
        ListItem item_to_delete = getListItem(list_items, item_id, item_name);
        list_items.remove(item_to_delete);

        System.out.println("Deleted list item \"" + item_name + "\"");
    }

    private boolean updateListItem(String item_id, String item_name, String new_item_name) {
        boolean result = false;
        ListItem item_to_update = getListItem(list_items, item_id, item_name);
        if(new_item_name != null && !new_item_name.equals(item_name))
        {
            item_to_update.setName(new_item_name);
        
            result = true;
            System.out.println("Updated list item name to \"" + new_item_name + "\"");
        }

        return result;
    }

    private boolean updateListItem(String item_id, String item_name, boolean item_status) {
        boolean result = false;
        ListItem item_to_update = getListItem(list_items, item_id, item_name);

        if(item_status != item_to_update.getStatus()) {
            item_to_update.setStatus(item_status);
            result = true;
        }
        
        System.out.println("Updated status of list item \"" + item_name + "\"");

        return result;
    }

    private void reorderListItem(String item_id, String item_name, int new_placement_index) {
        ListItem item_to_reorder = getListItem(list_items, item_id, item_name);

        // remove the item from the list items list
        list_items.remove(item_to_reorder);

        // add the item back into the list items list at the new placement index
        list_items.add(new_placement_index, item_to_reorder);

        System.out.println("Reordered list_items ArrayList.");
    }

    private void reorderList(HBox item_to_reorder, int old_index, int new_index) {
        if(old_index == new_index) {
            System.out.println("Reorder indexes match; Cancelling reorder...");
            return;
        }
        ListItem item = list_items.get(old_index);

        System.out.println("Reordering list...");
        list_container.getChildren().remove(old_index);

        list_container.getChildren().add(new_index, item_to_reorder);

        reorderListItem(item.getId(), item.getName(), new_index);
        
        updateStack();

        // for (ListItem listItem : list_items) {
        //     System.out.println(listItem.getName());
        // }
    }

    private Button reorderLogic(HBox ro_hbox, Button ro_button) {
        Image reorder_image = new Image(App.class.getResource("images/Reorder-Icon.png").toExternalForm(), 0, 18, true, false);
        ImageView reorder_iv = new ImageView(reorder_image);
        // reorder_iv.setPreserveRatio(true);
        // reorder_iv.setFitHeight(18);

        Button reorder_button = new Button("", reorder_iv);
        reorder_button.getStyleClass().add("container_sub_child");
        reorder_button.setPadding(new Insets(2));
        reorder_button.setGraphicTextGap(0);
        
        reorder_button.setOnDragDetected((MouseEvent me) -> {
            if(!stillRenaming()) {
                System.out.println("Drag detected...");
                Dragboard dragboard = ro_hbox.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                ro_hbox.getStyleClass().add("drag_highlighted");
                ro_button.getStyleClass().add("drag_highlighted");
                reorder_button.getStyleClass().add("drag_highlighted");
                content.putString("" + list_container.getChildren().indexOf(ro_hbox));
                dragboard.setContent(content);
                dragboard.setDragView(reorder_image, -15, -15);
            }
            me.consume();
        });

        ro_hbox.setOnDragOver((DragEvent de) -> { 
            if(!stillRenaming()) {
                Dragboard dragboard = de.getDragboard();
                if(dragboard.hasString()) {
                    de.acceptTransferModes(TransferMode.MOVE);
                }

                if(de.getSceneY() > 460 && center_content.getVvalue() < center_content.getVmax()) {
                    center_content.setVvalue(center_content.getVvalue() + 0.01);
                    // System.out.println("New scroll position: " + center_content.getVvalue());
                }
                else if(de.getSceneY() < 160 && center_content.getVvalue() > center_content.getVmin()) {
                    center_content.setVvalue(center_content.getVvalue() - 0.01);
                    // System.out.println("New scroll position: " + center_content.getVvalue());
                }
            }
            de.consume();
        });

        list_container.setOnDragOver((DragEvent de) -> { 
            if(!stillRenaming()) {
                Dragboard dragboard = de.getDragboard();
                if(dragboard.hasString()) {
                    de.acceptTransferModes(TransferMode.MOVE);
                }
                
                if(de.getSceneY() > 460 && center_content.getVvalue() < center_content.getVmax()) {
                    center_content.setVvalue(center_content.getVvalue() + 0.01);
                    // System.out.println("New scroll position: " + center_content.getVvalue());
                }
                else if(de.getSceneY() < 160 && center_content.getVvalue() > center_content.getVmin()) {
                    center_content.setVvalue(center_content.getVvalue() - 0.01);
                    // System.out.println("New scroll position: " + center_content.getVvalue());
                }
            }
            de.consume();
        });

        ro_hbox.setOnDragEntered((DragEvent de) -> {
            if(!stillRenaming()) {
                Dragboard dragboard = de.getDragboard();
                if(dragboard.hasString()) {
                    de.acceptTransferModes(TransferMode.MOVE);
                }

                if(de.getGestureSource() != ro_hbox) {
                    // System.out.println("Drag entered possible drop zone...");
                    ro_hbox.getStyleClass().add("drop_highlighted");
                    ro_button.getStyleClass().add("drop_highlighted");
                    reorder_button.getStyleClass().add("drop_highlighted");
                }
                
            }
            de.consume();
        });

        ro_hbox.setOnDragExited((DragEvent de) -> {
            if(!stillRenaming()) {
                if(de.getGestureSource() != ro_hbox) {
                    // System.out.println("Drag exited possible drop zone...");
                    ro_hbox.getStyleClass().remove("drop_highlighted");
                    ro_button.getStyleClass().remove("drop_highlighted");
                    reorder_button.getStyleClass().remove("drop_highlighted");
                }
            }
            de.consume();
        });

        list_container.setOnDragDropped((DragEvent de) -> {
            if(!stillRenaming()) {
                System.out.println("Dragging terminated...");
                Dragboard dragboard = de.getDragboard();

                if(dragboard.hasString()) {
                    // System.out.println("old_index: " + dragboard.getString());
                    // System.out.println(de.getGestureTarget());
                    int orig_idx = Integer.parseInt(dragboard.getString());
                    int new_idx = orig_idx;

                    if(de.getGestureTarget().getClass().getSimpleName().equals("HBox")) {
                        new_idx = list_container.getChildren().indexOf(de.getGestureTarget());
                    }
                    // System.out.println("new_index: " + new_idx);
                    
                    HBox moved_hbox = (HBox)list_container.getChildren().get(orig_idx);
                    moved_hbox.getStyleClass().remove("drag_highlighted");
                    moved_hbox.getChildren().get(1).getStyleClass().remove("drag_highlighted");
                    moved_hbox.getChildren().get(2).getStyleClass().remove("drag_highlighted");
                    de.setDropCompleted(true);
                    reorderList(moved_hbox, orig_idx, new_idx);
                }
                else {
                    System.out.println("Nothing to drop");
                    de.setDropCompleted(false);
                }
            }
            de.consume();
        });


        return reorder_button;
    }

    private void updateStack() {
        if(undo_stack.size() >= undo_limit) {
            System.out.println("   Removing bottom entry of Undo stack...");
            base_state = undo_stack.pollLast();
        }

        ListState curr_state = new ListState(specific_list_label.getText(), list_items);

        undo_stack.offerFirst(curr_state);

        if(!redo_stack.isEmpty()) {
            System.out.println("   Clearing Redo stack...");
            redo_stack.clear();
            redo_button.setDisable(true);
        }

        // System.out.println("   Updating Undo stack... New Size = " + undo_stack.size());

        if(!stillRenaming()) {
            enableUndoRedo();
            toggleSave();
        }
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

                ImageView checkbox = new ImageView(unchecked_image);
                checkbox.setPreserveRatio(true);
                checkbox.setFitWidth(18);
                Button item_button = new Button(item.getName());
                item_button.getStyleClass().add("container_sub_child");
                item_button.setGraphic(checkbox);
                item_button.setGraphicTextGap(6);
                item_button.setOnAction((ActionEvent e) -> {
                    toggleItemStatus(item_button);
                });

                if(item.getStatus()) {
                    item_button.getStyleClass().add("checked_item");
                    checkbox.setImage(checked_image);
                }
                
                new_nodes.add(item_button);

                if(editing) {
                    HBox new_hbox = createContainerChild(new_nodes, 1);
                    
                    Button reorder_button = reorderLogic(new_hbox, item_button);
                    
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
                                boolean updated = updateListItem(item_to_rename.getId(), item_to_rename.getName(), trimmed_name);
                                child_hbox.getChildren().remove(rename_item);
                                child_hbox.getChildren().addAll(item_button, reorder_button);
                                
                                if(updated)
                                {
                                    updateStack();
                                }

                                if(!stillRenaming()) {
                                    specific_list_edit.setDisable(false);
                                    enableUndoRedo();
                                }
                            }});
                        HBox child_hbox = (HBox)item_button.getParent();
                        child_hbox.getChildren().removeAll(item_button, reorder_button);
                        child_hbox.getChildren().add(rename_item);
                        rename_item.setPrefWidth(child_hbox.getWidth());
                        rename_item.requestFocus();
                        rename_item.selectAll();
                    }));

                    Button delete_button = new Button();
                    delete_button.setText("X");
                    delete_button.getStyleClass().add("delete_button");
                    delete_button.setOnAction((ActionEvent e) -> {
                        Node parent = delete_button.getParent();
                        int parent_index = list_container.getChildren().indexOf(parent);
                        if(parent_index < list_items.size()) {
                            ListItem item_to_delete = list_items.get(parent_index);
                            deleteListItem(item_to_delete.getId(), item_to_delete.getName());
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
                        }
                        else {
                            list_container.getChildren().remove(parent);
                            if(!stillRenaming()) {
                                specific_list_edit.setDisable(false);
                                enableUndoRedo();
                            }
                        }
                    });

                    new_hbox.getChildren().add(0, delete_button);
                    new_hbox.getChildren().add(reorder_button);
                }
                else {
                    createContainerChild(new_nodes, 0);
                }
                
                item_button.setPrefWidth(list_container.getWidth());
                
                if(!stillRenaming()) {
                    specific_list_edit.setDisable(false);
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
                    ImageView checkbox = (ImageView)item_button.getGraphic();
                    checkbox.setImage(unchecked_image);
                }
                else if (!item_button.getStyleClass().contains("checked_item") && item.getStatus()) {
                    item_button.getStyleClass().add("checked_item");
                    ImageView checkbox = (ImageView)item_button.getGraphic();
                    checkbox.setImage(checked_image);
                }
            }
        }
        
        toggleListStatus();
        toggleSave();
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
            state_to_use = base_state;
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
            state_to_use = base_state;
        }

        updateListState(state_to_use);

        if(redo_stack.isEmpty()) {
            redo_button.setDisable(true);
        }

        enableUndoRedo();
    }

    private void toggleSave() {
        if(original_state.isEqual(new ListState(specific_list_label.getText(), list_items))) {
            save_button.setDisable(true);
        }
        else if(!editing) {
            save_button.setDisable(false);
        }
    }

    @SuppressWarnings("unused")
    private void optionsDeleteChecked() {
        /*
         * Loop over the list of items and if it is checked, delete it
         */
        System.out.println("Deleting all checked items...");
    }

    private void optionsMarkListCompleted() {
        System.out.println("Marking list as completed...");
    }

    @SuppressWarnings("unused")
    private void optionsResetList() {
        System.out.println("Resetting list...");


    }

    @SuppressWarnings("unused")
    private void optionsSetDefaultOrder() {
        System.out.println("Setting current list order as default list order...");

        ArrayList<ListItem> temp_arr = new ArrayList<>();

        for (ListItem item : list_items) {
            temp_arr.add(new ListItem(item));
        }

        default_order.clear();

        for (ListItem item : temp_arr) {
            default_order.add(new ListItem(item));
        }

        // System.out.println("Default Order:");
        // for (ListItem item : default_order) {
        //     System.out.println("   " + item.getName() + " - " + item.getStatus());
        // }
    }

    private void optionsSortBy(String sort_criteria) {
        System.out.println("Sorting by: " + sort_criteria);
    }

    private void optionsUncheckAllItems() {
        System.out.println("Unchecking all items...");
    }

    @FXML
    private void initializeOptionsMenu() {
        ContextMenu options_menu = new ContextMenu();
        addNewMenuItem(options_menu, "Delete All Checked Items", (ActionEvent ae) -> {
            // call delete all checked items method
            optionsDialog("optionsDeleteChecked");
        });
        addNewMenuItem(options_menu, "Mark List as Completed", (ActionEvent ae) -> {
            // call mark list as completed method
            optionsMarkListCompleted();
        });
        addNewMenuItem(options_menu, "Reset List", (ActionEvent ae) -> {
            // call reset method
            optionsDialog("optionsResetList");
        });

        addNewMenuItem(options_menu, "Set as Default List Order", (ActionEvent ae) -> {
            // call list order method
            optionsDialog("optionsSetDefaultOrder");
        });

        Menu sort_menu = new Menu("Sort By: ");
        ToggleGroup sort_toggle = new ToggleGroup();
        RadioMenuItem sort_default = new RadioMenuItem("Default Order");
        RadioMenuItem sort_alpha = new RadioMenuItem("Name");
        RadioMenuItem sort_status = new RadioMenuItem("Status");

        sort_default.setOnAction((ActionEvent ae) -> {
            // call sort method
            optionsSortBy("Default");
        });
        sort_default.setSelected(true);

        sort_alpha.setOnAction((ActionEvent ae) -> {
            // call sort method
            optionsSortBy("Name");
        });

        sort_status.setOnAction((ActionEvent ae) -> {
            // call sort method
            optionsSortBy("Status");
        });

        sort_default.setToggleGroup(sort_toggle);
        sort_alpha.setToggleGroup(sort_toggle);
        sort_status.setToggleGroup(sort_toggle);

        sort_menu.getItems().addAll(sort_default, sort_alpha, sort_status);

        options_menu.getItems().add(sort_menu);
        addNewMenuItem(options_menu, "Uncheck All Items", (ActionEvent ae) -> {
            // call uncheck all items method
            optionsUncheckAllItems();
        });

        options_menu.setOnShowing((WindowEvent we) -> {
            // determine whether to diable the "Delete All Checked Items" and "Uncheck All Items" options
            if(findLastCheckedItem() == -1) {
                options_menu.getItems().get(0).setDisable(true);
                // Tooltip tooltip_del = new Tooltip("There are no checked items to delete");
                // Tooltip.install(options_menu.getItems().get(0).getStyleableNode(), tooltip_del);

                options_menu.getItems().get(5).setDisable(true);
                // Tooltip tooltip_unc = new Tooltip("There are no checked items to uncheck");
                // Tooltip.install(options_menu.getItems().get(5).getStyleableNode(), tooltip_unc);
            }
            else {
                options_menu.getItems().get(0).setDisable(false);
                options_menu.getItems().get(5).setDisable(false);
            }

            // determine whether to disable the "Mark List as Completed" option
            if(findLastUncheckedItem() == -1) {
                options_menu.getItems().get(1).setDisable(true);
                // Tooltip tooltip = new Tooltip("The list is already marked as completed");
                // Tooltip.install(options_menu.getItems().get(1).getStyleableNode(), tooltip);
            }
            else {
                options_menu.getItems().get(1).setDisable(false);
                // Tooltip tooltip= new Tooltip("Checks all items and marks list as complete");
                // Tooltip.install(options_menu.getItems().get(1).getStyleableNode(), tooltip);
            }
            
            // determine whether to disable the "Reset List" option
            if(original_state.isEqual(new ListState(specific_list_label.getText(), list_items))) {
                options_menu.getItems().get(2).setDisable(true);                
                // Tooltip tooltip = new Tooltip("Cannot reset since list matches most recent saved state");
                // Tooltip.install(options_menu.getItems().get(2).getStyleableNode(), tooltip);
            }
            else {
                options_menu.getItems().get(2).setDisable(false);
                // Tooltip tooltip = new Tooltip("Resets the list to its most recent saved state");
                // Tooltip.install(options_menu.getItems().get(2).getStyleableNode(), tooltip);
            }

            // determine whether to disable the "Set as Default List Order" option
            if(!editing) {
                options_menu.getItems().get(3).setDisable(true);
                // Tooltip tooltip= new Tooltip("This option is only available in Edit mode");
                // Tooltip.install(options_menu.getItems().get(3).getStyleableNode(), tooltip);
            }
            else {
                options_menu.getItems().get(3).setDisable(false);
            }

            options_menu.show(options_button, Side.RIGHT, 0, 0);
        });

        options_button.setContextMenu(options_menu);
    }

    @FXML
    @SuppressWarnings("unused")
    private void optionsMenu() { // TO-DO: Continue updating code to display Options Menu
        options_button.getContextMenu().show(options_button, Side.RIGHT, 0, 0);
        // final Stage dialog = App.getRoot(); // gets this window
        // try {
        //     // when this window's close button is pressed...
        //     dialog.setOnCloseRequest((WindowEvent ex) -> {
        //         // prevent the window from actually closing
        //         ex.consume();

        //         // set this window's scene back to this scene
        //         dialog.setScene(curr_scene);

        //         // update the close request to actually close this window again (instead of preventing it)
        //         dialog.setOnCloseRequest((WindowEvent wex) -> {
        //             db.closeConnection();
        //         });
        //     });

        //     // create options menu scene
        //     Scene options_scene = new Scene((new FXMLLoader((App.class.getResource("SpecificListViewOptions.fxml"))).load()), 800, 600);
        //     options_scene.getStylesheets().add(App.class.getResource("styles/Base_Style.css").toExternalForm());

        //     // set this window's scene to the Options Menu scene
        //     dialog.setScene(options_scene); 
        // }
        // catch (IOException ex) {
        //     System.err.println("Scene Change Failed.");
        // }
        
    }

    @FXML
    private void optionsDialog(String methodToCall) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);
        dialog.setTitle("Are You Sure?");
        
        // create outer dialog box
        VBox dialog_vbox = new VBox(20);
        dialog_vbox.setAlignment(Pos.CENTER);
        dialog_vbox.getStyleClass().add("full_page");
        dialog_vbox.setPadding(new Insets(20));

        // create details of dialog box
        Label dialog_label = new Label("This change cannot be undone. Continue?");
        dialog_label.setTextAlignment(TextAlignment.CENTER);
        dialog_label.getStyleClass().add("header_label");

        HBox dialog_hbox = new HBox(20);
        dialog_hbox.setAlignment(Pos.CENTER);
        dialog_hbox.setFillHeight(true);

        Button yes_button = new Button("Yes, Continue");
        yes_button.getStyleClass().add("container_sub_child");
        yes_button.setStyle("-fx-border-width: 1px; -fx-border-color: white;");
        yes_button.setOnAction((ActionEvent e) -> {
            try {
                dialog.close();
                this.getClass().getDeclaredMethod(methodToCall).invoke(this);
            }
            catch(IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
                System.err.println("Change failed.");
                System.err.println(ex.toString());
            }
        });

        Button no_button = new Button("No, Cancel");
        no_button.getStyleClass().add("container_sub_child");
        no_button.setStyle("-fx-border-width: 1px; -fx-border-color: white;");
        no_button.setOnAction((var e) -> {
            System.out.println("Change cancelled.");
            dialog.close();
        });

        dialog_hbox.getChildren().add(yes_button);
        dialog_hbox.getChildren().add(no_button);

        dialog_vbox.getChildren().add(dialog_label);
        dialog_vbox.getChildren().add(dialog_hbox);

        // display dialog box
        Scene dialog_scene = new Scene(dialog_vbox, 400, 200);
        dialog_scene.getStylesheets().add(App.class.getResource("styles/Base_Style.css").toExternalForm());
        dialog.setScene(dialog_scene);
        dialog.show();
    }

    @FXML
    private void exitDialog() { 
        if(original_state.isEqual(new ListState(specific_list_label.getText(), list_items)))
        {
            try {
                System.out.println("No changes found... Exiting...");
                switchToAllListsView();
            } catch (IOException ex) {
                System.err.println("Exit Failed.");
            }
            return;
        }

        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        // dialog.initOwner(App.getRoot()); -- might not need this (custom funtion that returns stage from App)
        dialog.setResizable(false);
        dialog.setTitle("Exit Without Saving Changes?");
        
        // create outer dialog box
        VBox dialog_vbox = new VBox(20);
        dialog_vbox.setAlignment(Pos.CENTER);
        dialog_vbox.getStyleClass().add("full_page");
        dialog_vbox.setPadding(new Insets(20));

        // create details of dialog box
        Label dialog_label = new Label("Are you sure you want to exit without saving your changes?");
        dialog_label.setTextAlignment(TextAlignment.CENTER);
        dialog_label.getStyleClass().add("header_label");

        HBox dialog_hbox = new HBox(20);
        dialog_hbox.setAlignment(Pos.CENTER);
        dialog_hbox.setFillHeight(true);

        Button yes_button = new Button("Yes, Exit Without Saving");
        yes_button.getStyleClass().add("container_sub_child");
        yes_button.setStyle("-fx-border-width: 1px; -fx-border-color: white;");
        yes_button.setOnAction((ActionEvent e) -> {
            try {
                System.out.println("Exit Granted.");
                dialog.close();
                switchToAllListsView();
            } catch (IOException ex) {
                System.err.println("Exit Failed.");
            }
        });

        Button no_button = new Button("No, Continue Editing");
        no_button.getStyleClass().add("container_sub_child");
        no_button.setStyle("-fx-border-width: 1px; -fx-border-color: white;");
        no_button.setOnAction((ActionEvent e) -> {
            System.out.println("Exit Cancelled.");
            dialog.close();
        });

        dialog_hbox.getChildren().add(yes_button);
        dialog_hbox.getChildren().add(no_button);

        dialog_vbox.getChildren().add(dialog_label);
        dialog_vbox.getChildren().add(dialog_hbox);

        // display dialog box
        Scene dialog_scene = new Scene(dialog_vbox, 400, 200);
        dialog_scene.getStylesheets().add(App.class.getResource("styles/Base_Style.css").toExternalForm());
        dialog.setScene(dialog_scene);
        dialog.show();
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
    private void saveList() throws IOException { 
        System.out.println("app list: " + App.getSpecificList() + "; new list: " + specific_list_label.getText());

        // call the SQLController's updateList() function with App.getSpecificList(), specific_list_label.getText(), and the ArrayList
        db.updateList(App.getSpecificList(), specific_list_label.getText(), list_items);

        // output save message to console
        System.out.println("List Successfully Saved!");

        undo_stack.clear();
        undo_button.setDisable(true);
        redo_stack.clear();
        redo_button.setDisable(true);
        original_state = new ListState(specific_list_label.getText(), list_items);
        base_state = new ListState(specific_list_label.getText(), list_items);
        toggleSave();
    }
}