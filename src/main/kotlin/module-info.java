module by.varyvoda.lemer {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires kotlin.stdlib.jdk8;
    requires kotlinx.coroutines.core.jvm;

    opens by.varyvoda.lemer to javafx.fxml;
    exports by.varyvoda.lemer;
}