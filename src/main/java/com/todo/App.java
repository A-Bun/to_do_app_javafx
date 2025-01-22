package com.todo;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private final static SQLController sqlctrl = new SQLController();

    @Override
    public void start(Stage stage) throws IOException {
        sqlctrl.openConnection();
        scene = new Scene(loadFXML("AllListsView"), 800, 600);
        scene.getStylesheets().add(App.class.getResource("styles/Base_Style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("To-Do Tracker");
        stage.resizableProperty().set(false); // TO-DO: Make window and font size resizable
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    static SQLController getSQLController() {
        return sqlctrl;
    }

    public static void main(String[] args) {
        launch();
    }

}