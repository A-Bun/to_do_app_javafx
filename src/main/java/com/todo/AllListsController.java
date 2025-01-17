package com.todo;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AllListsController implements Initializable{
    SQLController db = new SQLController();
    @FXML private VBox list_container;
    @FXML private Button all_lists_edit;
    @FXML private Button global_settings;
    private Boolean editing = false;
    final private int container_cnt = 2;
    private int cntr;

    @FXML
    @Override
    // implementing Initializable allows the fxml to be dynamically updated
    public void initialize(URL location, ResourceBundle resources) {
        // db.openConnection();
        // Map<String, ArrayList<String>> all_lists = db.getAllLists();
        // db.closeConnection();

        /* TO-DO: Get stored lists into scene
         *      - For each key in map.keySet(), create an hbox and a button
         *      - The button text should match the current key's name
         *      - The user should still have the ability to create a new list, but it won't be saved
         */

        // all_lists.split("");
        HBox new_list_trigger_box = new HBox();
        Button new_list_trigger = new Button("+ Create New List");
        new_list_trigger.getStyleClass().add("container_sub_child");
        // new_list_trigger.setStyle("-fx-border-width:0px;");

        Image settings = new Image(App.class.getResource("images/whiteGears.png").toExternalForm());
        ImageView iv1 = new ImageView();
        iv1.setImage(settings);
        iv1.setPreserveRatio(true);
        iv1.setSmooth(true);
        iv1.setFitWidth(40);
        global_settings.setGraphic(iv1);

        global_settings.setOnAction((ActionEvent e) -> {
            System.out.println("Global: " + global_settings.getWidth());
            db.openConnection();
        });
        
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
                if(!list_name.getText().isBlank()) {
                    Button new_list = new Button();
                    new_list.getStyleClass().add("container_sub_child");
                    new_list.setMinWidth(new_list_box.getWidth());
                    new_list.setOnAction((ActionEvent sae) -> {
                        try {
                            switchToSpecificListView();
                        } catch (IOException e1) {
                    }});
                    all_lists_edit.setDisable(false);
                    new_list_trigger.setDisable(false);
                    new_list.setText(list_name.getText().trim());
                    createNewList(new_list_box, new_list);
                    new_list_box.getChildren().remove(list_name);
                    new_list_box.getChildren().remove(remove_new_list);
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
        });

        new_list_trigger_box.getChildren().add(new_list_trigger);
        list_container.getChildren().add(new_list_trigger_box);
    }
    
    @FXML
    @SuppressWarnings("unused")
    private void switchToSpecificListView() throws IOException {
        App.setRoot("SpecificListView");
    }

    @FXML
    private void createNewList(HBox list_box, Button list_button) {
        list_box.getChildren().add(list_button);
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

    private void initiateEditMode() {
        System.out.println("Edit Mode On");

        list_container.getChildren().get(list_container.getChildren().size()-1).setDisable(true);
        all_lists_edit.getStyleClass().remove(all_lists_edit.getStyleClass().size()-1);
        all_lists_edit.getStyleClass().add("edit_button_on");

        for (cntr = 1; cntr < list_container.getChildren().size()-1; cntr++) {
            HBox child_list_hbox = (HBox)list_container.getChildren().get(cntr);

            String list_name = ((Button)child_list_hbox.getChildren().get(0)).getText();
            child_list_hbox.getChildren().remove(0);

            Button delete_button = new Button();
            delete_button.setText("X");
            delete_button.getStyleClass().add("delete_button");
            child_list_hbox.getChildren().add(delete_button);
            delete_button.setOnAction((ActionEvent e) -> {
                list_container.getChildren().remove(child_list_hbox);
                if(list_container.getChildren().size() <= container_cnt) {
                    editing = !editing;
                    concludeEditMode();
                }
            });

            TextField rename_list = new TextField(list_name.trim());
            rename_list.getStyleClass().add("container_sub_child_tf");
            child_list_hbox.getChildren().add(rename_list);
        }
    }

    private void concludeEditMode() {
        for (cntr = 1; cntr < list_container.getChildren().size()-1; cntr++) {
            HBox child_list_hbox = (HBox)list_container.getChildren().get(cntr);
            TextField renamed_list = (TextField)child_list_hbox.getChildren().get(child_list_hbox.getChildren().size()-1);

            if(renamed_list.getText().isBlank()) {
                System.out.println("Invalid List Name- Edit Mode is still on...");
                editing = true;
                return;
            }
        }
        
        System.out.println("Edit Mode Off");
        all_lists_edit.getStyleClass().remove(all_lists_edit.getStyleClass().size()-1);
        all_lists_edit.getStyleClass().add("edit_button");

        for (cntr = 1; cntr < list_container.getChildren().size()-1; cntr++) {
            HBox child_list_hbox = (HBox)list_container.getChildren().get(cntr);

            Button list_button = (Button)child_list_hbox.getChildren().get(0);
            TextField renamed_list = (TextField)child_list_hbox.getChildren().get(child_list_hbox.getChildren().size()-1);

            list_button.setText(renamed_list.getText());
            list_button.setMinWidth(child_list_hbox.getWidth());
            list_button.getStyleClass().remove(list_button.getStyleClass().size()-1);
            list_button.getStyleClass().add("container_sub_child");
            list_button.setOnAction((ActionEvent e) -> {
                try {
                    switchToSpecificListView();
                } catch (IOException e1) {
            }});

            child_list_hbox.getChildren().remove(renamed_list);
        }

        list_container.getChildren().get(list_container.getChildren().size()-1).setDisable(false);

        if(list_container.getChildren().size() <= container_cnt) {
            all_lists_edit.setDisable(true);
        }
    }

}
