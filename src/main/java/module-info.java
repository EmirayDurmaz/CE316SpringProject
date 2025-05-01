module org.example.integratedassignmentenviroment {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens org.example.iae to javafx.fxml;
    exports org.example.iae;
}