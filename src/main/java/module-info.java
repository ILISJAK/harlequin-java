module com.example.harlequin {
    requires javafx.controls;
    requires javafx.fxml;
            
        requires org.controlsfx.controls;
            requires com.dlsc.formsfx;
                            requires com.almasb.fxgl.all;
    
    opens com.example.harlequin to javafx.fxml;
    exports com.example.harlequin;
}