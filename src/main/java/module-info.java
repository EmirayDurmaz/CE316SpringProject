module org.example.integratedassignmentenviroment {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.iae to javafx.fxml;
    exports com.example.iae;
}