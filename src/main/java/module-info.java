module topic8.os_project_topic8 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens topic8.os_project_topic8 to javafx.fxml;
    exports topic8.os_project_topic8;
}