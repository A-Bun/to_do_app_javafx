package com.todo;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AllListsController implements Initializable{
    SQLController db;
    @FXML private VBox list_container;
    @FXML private Button all_lists_edit;
    @FXML private Button global_settings;
    private Boolean editing = false;
    final private int container_cnt = 1;
    private int cntr;
    private final ArrayList<ListItem> blank_list = new ArrayList<>();
    private final ArrayList<String> old_list_names = new ArrayList<>();
    private final Map<String, Boolean> list_statuses = new HashMap<>();

    @FXML
    @Override
    // implementing Initializable allows the fxml to be initialized with additional objects once
    public void initialize(URL location, ResourceBundle resources) {
        db = App.getSQLController();

        // Map<String, ArrayList<ListItem>> all_lists = db.getAllLists();

        // Object[] list_array = all_lists.keySet().toArray();
        ArrayList<String> list_array = db.getAllListNames();

        if(list_array.isEmpty()) {
            all_lists_edit.setDisable(true);
        }
        else {
            list_container.setOnMouseEntered((MouseEvent e) -> {
                if(!editing) {
                    System.out.println("Resizing List Buttons");

                    // Resize the list buttons
                    for(int i = 0; i < list_container.getChildren().size()-1; i++) {
                        HBox child_hbox = (HBox)list_container.getChildren().get(i);
                        Button button = (Button)child_hbox.getChildren().get(0);
                        button.setPrefWidth(child_hbox.getWidth());
                    }

                    // reset the mouse entered event
                    list_container.setOnMouseEntered((MouseEvent ae) -> {});
                }
            });
        }

        // for (Object list : list_array) {
        for (int i = 0; i < list_array.size(); i++) {
            String list_name = list_array.get(i);
            HBox current_list_box = new HBox();
            current_list_box.setFillHeight(true);
            current_list_box.getStyleClass().add("container_sub");
            current_list_box.setAlignment(Pos.CENTER_LEFT);
            
            Button current_list = new Button(list_name);
            // Button current_list = new Button(list_array.get(i));
            current_list.getStyleClass().add("container_sub_child");

            ArrayList<ListItem> full_list = db.getList(list_name);

            if(!full_list.isEmpty()) {
                current_list.getStyleClass().add("checked_item");

                for(ListItem item : full_list) {
                    list_statuses.put(list_name, true);

                    if(!item.getStatus()) {
                        current_list.getStyleClass().remove("checked_item");
                        list_statuses.put(list_name, false);
                        break;
                    }
                }
            }
            else {
                list_statuses.put(list_name, false);
            }

            // current_list.setMinWidth(current_list_box.getWidth());
            current_list.setOnAction((ActionEvent e) -> {
                try {
                    switchToSpecificListView(current_list.getText());
                } catch (IOException e1) {
            }});
            current_list_box.getChildren().add(current_list);

            list_container.getChildren().add(list_container.getChildren().size(), current_list_box);
        }

        HBox new_list_trigger_box = new HBox();
        Button new_list_trigger = new Button("+ Create New List");
        new_list_trigger.getStyleClass().add("container_sub_child");
        // new_list_trigger.setStyle("-fx-border-width:0px;");

        Image settings = new Image(App.class.getResource("images/White-Gears.png").toExternalForm());
        ImageView iv1 = new ImageView();
        iv1.setImage(settings);
        iv1.setPreserveRatio(true);
        iv1.setSmooth(true);
        iv1.setFitWidth(40);
        global_settings.setGraphic(iv1);
        
        new_list_trigger.setOnAction((ActionEvent e) -> {
            all_lists_edit.setDisable(true);
            new_list_trigger.setDisable(true);
            HBox new_list_box = new HBox();
            new_list_box.setFillHeight(true);
            new_list_box.getStyleClass().add("container_sub");
            new_list_box.setAlignment(Pos.CENTER_LEFT);
            Button remove_new_list = new Button();
            remove_new_list.getStyleClass().add("delete_button");
            remove_new_list.setText("X");
            new_list_box.getChildren().add(remove_new_list);

            TextField list_name = new TextField();
            list_name.setPromptText("Enter new list title here...");
            list_name.getStyleClass().add("container_sub_child_tf");
            list_name.setOnAction((ActionEvent ae) -> {
                String trimmed_name = list_name.getText().trim();

                if(trimmed_name.equals("Repop")){
                    all_lists_edit.setDisable(false);
                    new_list_trigger.setDisable(false);
                    list_container.getChildren().remove(new_list_box);
                    Repop();
                }
                else if(!trimmed_name.isBlank() && !db.Exists(trimmed_name)) {
                    db.addList(trimmed_name, blank_list);
                    list_statuses.put(trimmed_name, false);
                    try {
                        switchToSpecificListView(trimmed_name);
                    } 
                    catch (IOException e1) { }
                }
                else if (db.Exists(trimmed_name)) {
                    System.err.println("List \"" + trimmed_name + "\" already exists. Use a different name.");
                }
            });

            remove_new_list.setOnAction((ActionEvent ae) -> {
                list_container.getChildren().remove(new_list_box);

                if(list_container.getChildren().size() > container_cnt) {
                    all_lists_edit.setDisable(false);
                }

                new_list_trigger.setDisable(false);
            });

            new_list_box.getChildren().add(list_name);

            list_container.getChildren().add(list_container.getChildren().size()-1, new_list_box);

            // automatically focus this TextField
            list_name.requestFocus();
        });

        new_list_trigger_box.getChildren().add(new_list_trigger);
        list_container.getChildren().add(new_list_trigger_box);
    }
    
    @FXML
    @SuppressWarnings("unused")
    private void switchToSpecificListView(String list_to_view) throws IOException {
        App.setSpecificList(list_to_view);
        App.setRoot("SpecificListView");
    }

    // @FXML
    // private void createNewList(HBox list_box, Button list_button) {
    //     list_box.getChildren().add(list_button);
    // }

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

    private void initiateEditMode() {
        System.out.println("Edit Mode On");

        list_container.getChildren().get(list_container.getChildren().size()-1).setDisable(true);
        all_lists_edit.getStyleClass().add("edit_button_on");

        for (cntr = 0; cntr < list_container.getChildren().size()-1; cntr++) {
            HBox child_list_hbox = (HBox)list_container.getChildren().get(cntr);

            String list_name = ((Button)child_list_hbox.getChildren().get(0)).getText().trim();
            old_list_names.add(list_name);
            child_list_hbox.getChildren().remove(0);

            Button delete_button = new Button();
            delete_button.setText("X");
            delete_button.getStyleClass().add("delete_button");
            child_list_hbox.getChildren().add(delete_button);
            delete_button.setOnAction((ActionEvent e) -> {
                old_list_names.remove(list_name);
                list_statuses.remove(list_name);
                db.deleteList(list_name);
                list_container.getChildren().remove(child_list_hbox);
                if(list_container.getChildren().size() <= container_cnt) {
                    editing = !editing;
                    concludeEditMode();
                }
            });

            TextField rename_list = new TextField(list_name);
            rename_list.getStyleClass().add("container_sub_child_tf");
            child_list_hbox.getChildren().add(rename_list);
        }
    }

    private void concludeEditMode() {
        for (cntr = 0; cntr < list_container.getChildren().size()-1; cntr++) {
            HBox child_list_hbox = (HBox)list_container.getChildren().get(cntr);
            TextField renamed_list = (TextField)child_list_hbox.getChildren().get(child_list_hbox.getChildren().size()-1);

            if(renamed_list.getText().isBlank()) {
                System.out.println("Invalid List Name - Edit Mode is still on...");
                editing = true;
                return;
            }
        }
        
        System.out.println("Edit Mode Off");
        all_lists_edit.getStyleClass().remove("edit_button_on");

        for (cntr = 0; cntr < list_container.getChildren().size()-1; cntr++) {
            HBox child_list_hbox = (HBox)list_container.getChildren().get(cntr);

            Button list_button = (Button)child_list_hbox.getChildren().get(0);
            TextField renamed_list = (TextField)child_list_hbox.getChildren().get(child_list_hbox.getChildren().size()-1);

            list_button.setText(renamed_list.getText().trim());
            list_button.setMinWidth(child_list_hbox.getWidth());
            list_button.getStyleClass().remove("delete_button");
            list_button.getStyleClass().add("container_sub_child");

            list_button.setOnAction((ActionEvent e) -> {
                try {
                    switchToSpecificListView(list_button.getText());
                } catch (IOException e1) {
            }});

            db.updateList(old_list_names.get(cntr), list_button.getText(), null);

            if(list_statuses.get(old_list_names.get(cntr)) == true ) {
                list_statuses.remove(old_list_names.get(cntr));
                list_statuses.put(list_button.getText(), true);
                list_button.getStyleClass().add("checked_item");
            }
            else if(list_statuses.get(old_list_names.get(cntr)) == false ) {
                list_statuses.remove(old_list_names.get(cntr));
                list_statuses.put(list_button.getText(), false);
            }

            child_list_hbox.getChildren().remove(renamed_list);
        }

        list_container.getChildren().get(list_container.getChildren().size()-1).setDisable(false);
        old_list_names.clear();

        if(list_container.getChildren().size() <= container_cnt) {
            all_lists_edit.setDisable(true);
        }
    }

    public void Settings() throws IOException {
        System.out.println("Global: " + global_settings.getWidth());
        System.out.println("Displaying Settings Menu...");
        App.setRoot("SettingsView");
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

    /* TO BE DELETED */
    public void Repop() {
        db.RepopulateTestData();
    }

}
