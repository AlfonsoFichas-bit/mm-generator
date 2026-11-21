module com.buga.mmgenerator {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires kotlin.stdlib;
    requires atlantafx.base;
    requires com.google.gson;
    requires java.desktop;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;

    opens com.buga.mmgenerator to javafx.fxml, com.google.gson;
    opens com.buga.mmgenerator.model to com.google.gson;
    exports com.buga.mmgenerator;
}