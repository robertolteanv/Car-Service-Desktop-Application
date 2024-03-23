module org.example.cardealer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires java.sql.rowset;
    requires java.naming;



    opens org.example.cardealer to javafx.fxml;
    exports org.example.cardealer;
}