module ru.edu.nsu.fit.martynov.autoparts {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens ru.edu.nsu.fit.martynov.autoparts to javafx.fxml;
    exports ru.edu.nsu.fit.martynov.autoparts;
    exports ru.edu.nsu.fit.martynov.autoparts.db;
    opens ru.edu.nsu.fit.martynov.autoparts.db to javafx.fxml;
}