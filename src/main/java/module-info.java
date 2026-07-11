module com.taskbarplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires jaudiotagger;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;

    opens com.taskbarplayer to javafx.fxml;
    opens com.taskbarplayer.controller to javafx.fxml;
    exports com.taskbarplayer;
}