package org.example.cardealer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.getIcons().add(new Image("file:src/main/resources/org/example/cardealer/icon.jpg"));
        //stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        stage.setResizable(false);
        //stage.setTitle("Hello!");
        stage.setTitle("Vericu Service and Tuning");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}