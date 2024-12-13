package com.example.controller;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.text.DecimalFormat;

import com.example.util.DBConnection;

public class InfoController {

    private Stage infoStage;
    private int clientNumber;
    private BalanceUpdateListener listener; // 콜백 리스너

    public interface BalanceUpdateListener {
        void onBalanceUpdated(); // 잔액이 업데이트되었을 때 호출되는 메소드
    }

    public InfoController(int clientNumber) {
        this.clientNumber = clientNumber;
        initialize();
    }

    public void setBalanceUpdateListener(BalanceUpdateListener listener) {
        this.listener = listener; // MainController에서 리스너를 등록
    }

    private void initialize() {
        AnchorPane infoPane = new AnchorPane();

        // Labels for client details
        Label clientNameLabel = new Label("이름 :  ");
        Label joinDateLabel = new Label("가입일 :  ");
        Label balanceLabel = new Label("잔액 :  ");
        Label chargeLabel = new Label("잔액 충전");
        TextField chargeField = new TextField();
        Button chargeButton = new Button("충전");
        Label clientNumLabel = new Label("Client No." + clientNumber);

        chargeField.setPromptText("금액");
        chargeField.setPrefWidth(130);

        // Load client details from the client_db
        loadClientDetails(clientNameLabel, joinDateLabel, balanceLabel);

        // Add Labels to AnchorPane
        infoPane.getChildren().addAll(clientNameLabel, joinDateLabel, balanceLabel, chargeLabel, chargeField, chargeButton, clientNumLabel);

        // Set positions for Labels
        AnchorPane.setTopAnchor(clientNameLabel, 20.0);
        AnchorPane.setLeftAnchor(clientNameLabel, 20.0);

        AnchorPane.setTopAnchor(joinDateLabel, 60.0);
        AnchorPane.setLeftAnchor(joinDateLabel, 20.0);

        AnchorPane.setTopAnchor(balanceLabel, 100.0);
        AnchorPane.setLeftAnchor(balanceLabel, 20.0);

        AnchorPane.setTopAnchor(chargeLabel, 145.0);
        AnchorPane.setLeftAnchor(chargeLabel, 20.0);

        AnchorPane.setTopAnchor(chargeField, 165.0);
        AnchorPane.setLeftAnchor(chargeField, 20.0);

        AnchorPane.setTopAnchor(chargeButton, 165.0);
        AnchorPane.setLeftAnchor(chargeButton, 150.0);

        AnchorPane.setTopAnchor(clientNumLabel, 230.0);
        AnchorPane.setLeftAnchor(clientNumLabel, 10.0);

        // Add action for charge button
        chargeButton.setOnAction(event -> {
            String chargeAmountStr = chargeField.getText();
            try {
                // Parse input to integer
                int chargeAmount = Integer.parseInt(chargeAmountStr);
                if (chargeAmount <= 0) {
                    showAlert("잘못된 입력", "충전 금액은 0보다 커야 합니다.");
                    return;
                }

                // Update balance in the client_db
                updateBalance(chargeAmount);

                // Reload the balance label
                loadClientDetails(clientNameLabel, joinDateLabel, balanceLabel);

                // Clear input field
                chargeField.clear();

            } catch (NumberFormatException e) {
                showAlert("잘못된 입력", "충전 금액은 숫자로 입력해야 합니다.");
            }
        });

        // Create and set the Scene
        Scene scene = new Scene(infoPane, 600, 250); // Scene 크기를 600x250으로 설정
        infoStage = new Stage();
        infoStage.setTitle("회원 정보");
        infoStage.setScene(scene);

        // Modal window setting
        infoStage.initModality(Modality.APPLICATION_MODAL); // 모달 창으로 설정

        // Fixed window size
        infoStage.setResizable(false); // 창 크기 변경 불가
    }

    private void loadClientDetails(Label clientNameLabel, Label joinDateLabel, Label balanceLabel) {
        // client_db에서 클라이언트 정보 로드
        String query = "SELECT clientname, join_date, balance FROM client WHERE clientnumber = ?";
        try (Connection connection = DBConnection.getConnection("bookflow_db");
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Set the client number parameter
            statement.setInt(1, clientNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Retrieve client details
                    String clientName = resultSet.getString("clientname");
                    String joinDate = resultSet.getString("join_date");
                    int balance = resultSet.getInt("balance");

                    // Format the balance using DecimalFormat
                    DecimalFormat formatter = new DecimalFormat("#,###");
                    String formattedBalance = formatter.format(balance);

                    // Set Labels with client details
                    clientNameLabel.setText("이름 :  " + clientName);
                    joinDateLabel.setText("가입일 :  " + joinDate);
                    balanceLabel.setText("잔액 :  " + formattedBalance + " 원");
                }
            }
        } catch (SQLException e) {
            showAlert("오류", "데이터베이스 연결 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void updateBalance(int chargeAmount) {
        // client_db에서 잔액 업데이트
        String query = "UPDATE client SET balance = balance + ? WHERE clientnumber = ?";
        try (Connection connection = DBConnection.getConnection("bookflow_db");
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Set the parameters
            statement.setInt(1, chargeAmount);
            statement.setInt(2, clientNumber);

            // Execute update
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Balance updated successfully.");

                // 콜백 호출 (잔액 업데이트 후 MainController에 알림)
                if (listener != null) {
                    listener.onBalanceUpdated();
                }
            } else {
                showAlert("오류", "잔액을 업데이트하지 못했습니다.");
            }
        } catch (SQLException e) {
            showAlert("오류", "데이터베이스 연결 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showInfoWindow() {
        infoStage.show(); // Show the info window
    }
}
