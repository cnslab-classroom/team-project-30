package com.example;


import com.example.controller.LoginController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // LoginController를 초기화
        LoginController loginController = new LoginController();

        // Stage 설정
        Scene scene = new Scene(loginController.getLoginPane(), 300, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("BookFlow Login");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
