package com.example.controller;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;

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

  private ArrayList<String> purchasedGenres = new ArrayList<>();

private void loadPurchaseHistory(ListView<String> purchaseListView) {
    purchaseListView.getItems().clear();
    purchasedGenres.clear(); // Clear previous genres
    
    // Query to get purchase history along with genres
    String query = "SELECT b.title, b.genre, oh.order_date, oh.quantity " +
                   "FROM order_history oh " +
                   "JOIN book b ON oh.book_id = b.book_id " +
                   "WHERE oh.clientnumber = ?";
    
    try (Connection connection = DBConnection.getConnection("bookflow_db");
         PreparedStatement statement = connection.prepareStatement(query)) {
        statement.setInt(1, clientNumber);
        ResultSet rs = statement.executeQuery();
        
        while (rs.next()) {
            String title = rs.getString("title");
            String genre = rs.getString("genre");
            purchasedGenres.add(genre); // Collect genres
            String purchaseInfo = title + " | " + rs.getString("order_date") + " | " + rs.getInt("quantity");
            purchaseListView.getItems().add(purchaseInfo);
        }
    } catch (SQLException e) {
        e.printStackTrace();
        showAlert("notice", "구매 내역의 양이 충분하지 않은 분들에게는 평점이 높은 작품들로 자동 추천됩니다");
    }
}

// Getter for genres
public ArrayList<String> getPurchasedGenres() {
    return purchasedGenres;
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
