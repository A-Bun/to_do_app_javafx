module com.todo {
    requires  transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;

    opens com.todo to javafx.fxml;
    exports com.todo;
}
