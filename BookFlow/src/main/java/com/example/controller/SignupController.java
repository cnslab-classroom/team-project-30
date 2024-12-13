package com.example.controller;

import com.example.util.DBConnection;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.sql.*;

public class SignupController {
    private AnchorPane signupPane;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button signupButton;
    private LoginController loginController;

    public SignupController(LoginController loginController) {
        this.loginController = loginController;  // LoginController를 전달받음
        initialize();
    }

    private void initialize() {
        signupPane = new AnchorPane();
        signupPane.setPrefSize(300, 200);

        // Username Label and TextField
        Label usernameLabel = new Label("ID:");
        usernameLabel.setLayoutX(50);
        usernameLabel.setLayoutY(50);

        usernameField = new TextField();
        usernameField.setLayoutX(120);
        usernameField.setLayoutY(50);
        usernameField.setPrefWidth(120);

        // Password Label and PasswordField
        Label passwordLabel = new Label("PW:");
        passwordLabel.setLayoutX(50);
        passwordLabel.setLayoutY(90);

        passwordField = new PasswordField();
        passwordField.setLayoutX(120);
        passwordField.setLayoutY(90);
        passwordField.setPrefWidth(120);

        // Signup Button
        signupButton = new Button("Signup");
        signupButton.setLayoutX(120);
        signupButton.setLayoutY(140);

        // Add components to the pane
        signupPane.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField, signupButton);

        // Button Action
        signupButton.setOnAction(event -> handleSignup());
    }

    private void handleSignup() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "입력 오류", "ID와 PW를 모두 입력해주세요!");
            return;
        }

        if (isUsernameTaken(username)) {
            showAlert(Alert.AlertType.ERROR, "중복 ID", "이미 사용 중인 ID입니다!");
            return;
        }

        if (createAccount(username, password)) {
            showAlert(Alert.AlertType.INFORMATION, "회원가입 성공", "계정이 성공적으로 생성되었습니다!");
            closeSignupWindow();
        } else {
            showAlert(Alert.AlertType.ERROR, "회원가입 실패", "계정 생성에 실패했습니다. 다시 시도해주세요!");
        }
    }

    private boolean isUsernameTaken(String username) {
        String query = "SELECT clientname FROM client WHERE clientname = ?";
        try (Connection connection = DBConnection.getConnection("bookflow_db"); 
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();  // 중복된 username이 있으면 true
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true; // 에러가 발생하면 true를 반환해 중복으로 간주
        }
    }

    private boolean createAccount(String username, String password) {
        String query = "INSERT INTO client (clientname, pw) VALUES (?, ?)";
        try (Connection connection = DBConnection.getConnection("bookflow_db");
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            return statement.executeUpdate() > 0; // 성공적으로 추가되면 true 반환
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void closeSignupWindow() {
        Stage stage = (Stage) signupPane.getScene().getWindow();
        stage.close();

        // 회원가입 창이 닫힐 때 로그인 창을 다시 활성화
        loginController.getLoginPane().setDisable(false);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void show() {
        // 로그인 창 비활성화
        loginController.getLoginPane().setDisable(true);

        // 회원가입 창 띄우기
        Stage stage = new Stage();
        stage.setTitle("Signup");
        stage.setScene(new Scene(signupPane));
        stage.show();
    }
}
