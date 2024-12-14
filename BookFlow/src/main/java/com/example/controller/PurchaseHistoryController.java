package com.example.controller;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;

import com.example.util.DBConnection;

public class PurchaseHistoryController {

    private Stage historyStage;
    private int clientNumber;

    public PurchaseHistoryController(int clientNumber) {
        this.clientNumber = clientNumber;
        initialize();
    }

    private void initialize() {
        AnchorPane historyPane = new AnchorPane();

        // ListView for purchase history
        ListView<String> purchaseListView = new ListView<>();
        Button returnButton = new Button("반품 요청");

        // Load purchase history data
        loadPurchaseHistory(purchaseListView);

        // Set positions for elements
        AnchorPane.setTopAnchor(purchaseListView, 20.0);
        AnchorPane.setLeftAnchor(purchaseListView, 20.0);
        AnchorPane.setTopAnchor(returnButton, 250.0);
        AnchorPane.setLeftAnchor(returnButton, 20.0);

        // Return Button Action
        returnButton.setOnAction(event -> handleReturnRequest(purchaseListView));

        // Add components to AnchorPane
        historyPane.getChildren().addAll(purchaseListView, returnButton);

        // Create Scene and Stage for Purchase History
        Scene scene = new Scene(historyPane, 600, 350);
        historyStage = new Stage();
        historyStage.setTitle("구매 내역");
        historyStage.setScene(scene);
        historyStage.initModality(Modality.APPLICATION_MODAL);
        historyStage.setResizable(false);
    }

    private void loadPurchaseHistory(ListView<String> purchaseListView) {
        purchaseListView.getItems().clear();

        // Query to get purchase history from the database
        String query = "SELECT b.title, p.purchase_date, p.amount FROM purchase_history p " +
                "JOIN book b ON p.book_id = b.book_id WHERE p.clientnumber = ?";
        try (Connection connection = DBConnection.getConnection("bookflow_db")) {
            // 1. Insert order into order_history
            String insertOrderQuery = "INSERT INTO order_history (clientnumber, book_id, quantity, total_price) " +
                    "SELECT bt.clientnumber, bt.book_id, bt.quantity, (bt.quantity * b.price) " +
                    "FROM bucket bt " +
                    "JOIN book b ON bt.book_id = b.book_id " +
                    "WHERE bt.clientnumber = ?";
            try (PreparedStatement orderStatement = connection.prepareStatement(insertOrderQuery)) {
                orderStatement.setInt(1, clientNumber);
                orderStatement.executeUpdate();
            }

            // 2. Clear bucket for the client
            String clearBucketQuery = "DELETE FROM bucket WHERE clientnumber = ?";
            try (PreparedStatement clearStatement = connection.prepareStatement(clearBucketQuery)) {
                clearStatement.setInt(1, clientNumber);
                clearStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("오류", "구매 처리 중 오류가 발생했습니다.");
        }

    }

    private void handleReturnRequest(ListView<String> purchaseListView) {
        String selectedItem = purchaseListView.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            showAlert("오류", "반품할 항목을 선택해주세요.");
            return;
        }

        // Simulate return request (e.g., by removing it from the database or flagging
        // it)
        showAlert("반품 요청", "반품 요청이 접수되었습니다.");

        // Optionally, you can implement more sophisticated return logic here
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showPurchaseHistoryWindow() {
        historyStage.show();
    }
}
