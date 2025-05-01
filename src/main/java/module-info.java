module org.example.integratedassignmentenviroment {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires org.json;

    opens com.example.iae to javafx.fxml;
    exports com.example.iae;
}