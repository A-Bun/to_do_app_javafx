module com.todo {
    requires  transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;

    opens com.todo to javafx.fxml;
    exports com.todo;
}
