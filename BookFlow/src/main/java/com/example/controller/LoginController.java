package com.example.controller;

import com.example.util.DBConnection;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.sql.*;

public class LoginController {

    private AnchorPane loginPane;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Button signupButton;
    private MainController mainController; // MainController 참조 추가
    private int clientNumber; // clientNumber를 이곳에 선언

    public LoginController() {
        initialize();
    }

    private void initialize() {
        loginPane = new AnchorPane();
        loginPane.setPrefSize(300, 200);

        // Username Label and TextField
        Label usernameLabel = new Label("ID");
        usernameLabel.setLayoutX(50);
        usernameLabel.setLayoutY(50);

        usernameField = new TextField();
        usernameField.setLayoutX(120);
        usernameField.setLayoutY(50);
        usernameField.setPrefWidth(120);

        // Password Label and PasswordField
        Label passwordLabel = new Label("PW");
        passwordLabel.setLayoutX(50);
        passwordLabel.setLayoutY(90);

        passwordField = new PasswordField();
        passwordField.setLayoutX(120);
        passwordField.setLayoutY(90);
        passwordField.setPrefWidth(120);

        // Login Button
        loginButton = new Button("Login");
        loginButton.setLayoutX(160); // 위치 조정
        loginButton.setLayoutY(140);

        // Signup Button
        signupButton = new Button("Signup");
        signupButton.setLayoutX(80); // 위치 조정
        signupButton.setLayoutY(140);

        // Add components to the pane
        loginPane.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField, loginButton, signupButton);

        // Button Actions
        loginButton.setOnAction(event -> handleLogin());
        signupButton.setOnAction(event -> openSignupWindow());

        // Add Enter Key Event for Login
        addEnterKeyEvent();
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (authenticate(username, password)) {
            showAlert(Alert.AlertType.INFORMATION, "로그인 성공", "환영합니다!");
            openMainWindow(); // 로그인한 사용자 정보 전달
            closeLoginWindow();
        } else {
            showAlert(Alert.AlertType.ERROR, "로그인 실패", "ID와 PW를 확인해주세요!");
        }
    }

    public boolean authenticate(String username, String password) {
        String query = "SELECT clientnumber FROM client WHERE clientname = ? AND pw = ?";
        try (Connection connection = DBConnection.getConnection("bookflow_db");  // client 데이터베이스 연결
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    clientNumber = resultSet.getInt("clientnumber");

                    if (mainController == null) {
                        mainController = new MainController();
                    }

                    mainController.setClientNumber(clientNumber);
                    return true;
                }
                return false;
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "로그인 오류", "데이터베이스 연결 오류가 발생했습니다.");
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "로그인 오류", "알 수 없는 오류가 발생했습니다.");
            e.printStackTrace();
            return false;
        }
    }

    private void openSignupWindow() {
        // 로그인 창 비활성화
        loginPane.setDisable(true);

        // Signup 창 띄우기
        SignupController signupController = new SignupController(this);
        signupController.show();
    }

    private void openMainWindow() {
        // mainController가 null이면 새로 생성
        if (mainController == null) {
            mainController = new MainController();
        }
        mainController.setClientNumber(clientNumber); // clientNumber를 mainController에 전달
        mainController.show();
    }

    private void addEnterKeyEvent() {
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                loginButton.fire(); // Trigger login button
            }
        });

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                loginButton.fire(); // Trigger login button
            }
        });
    }

    private void closeLoginWindow() {
        Stage stage = (Stage) loginPane.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public AnchorPane getLoginPane() {
        return loginPane;
    }
}
