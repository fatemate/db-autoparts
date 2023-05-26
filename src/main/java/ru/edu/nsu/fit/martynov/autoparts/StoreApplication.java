package ru.edu.nsu.fit.martynov.autoparts;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ru.edu.nsu.fit.martynov.autoparts.db.DataBaseController;

import java.io.IOException;
import java.sql.SQLException;

public class StoreApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException, SQLException {
        FXMLLoader fxmlLoader = new FXMLLoader(StoreApplication.class.getResource("app-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
        StoreController controller = fxmlLoader.getController();
        stage.setTitle("Auto parts Store");
        stage.getIcons().add(new Image(StoreApplication.class.getResource("favicon.png").openStream()));
        controller.init();
        controller.setLoginPageErrorMessage("Incorret login or password.\nTry again!");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        //DataBaseController.getInstance().init();
    }

    public static void main(String[] args) {
        launch();
    }
}