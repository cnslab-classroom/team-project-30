package com.example.controller;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.text.DecimalFormat;

import com.example.util.DBConnection;

public class CartController {

    private Stage cartStage;
    private int clientNumber;

    public CartController(int clientNumber) {
        this.clientNumber = clientNumber;
        initialize();
    }

    private void initialize() {
        AnchorPane cartPane = new AnchorPane();

        // ListView for cart items
        ListView<String> cartListView = new ListView<>();
        Label totalPriceLabel = new Label("총 가격: 0 원");
        Button purchaseButton = new Button("구매하기");

        // Load cart data
        loadCartData(cartListView, totalPriceLabel);

        // Set positions for elements
        AnchorPane.setTopAnchor(cartListView, 20.0);
        AnchorPane.setLeftAnchor(cartListView, 20.0);
        AnchorPane.setTopAnchor(totalPriceLabel, 250.0);
        AnchorPane.setLeftAnchor(totalPriceLabel, 20.0);
        AnchorPane.setTopAnchor(purchaseButton, 280.0);
        AnchorPane.setLeftAnchor(purchaseButton, 20.0);

        // Purchase Button Action
        purchaseButton.setOnAction(event -> handlePurchase(cartListView));

        // Add components to AnchorPane
        cartPane.getChildren().addAll(cartListView, totalPriceLabel, purchaseButton);

        // Create Scene and Stage for Cart
        Scene scene = new Scene(cartPane, 600, 350);
        cartStage = new Stage();
        cartStage.setTitle("장바구니");
        cartStage.setScene(scene);
        cartStage.initModality(Modality.APPLICATION_MODAL);
        cartStage.setResizable(false);
    }

    private void loadCartData(ListView<String> cartListView, Label totalPriceLabel) {
        cartListView.getItems().clear();
        double totalPrice = 0;

        // Query to get cart items from the database
        String query = "SELECT b.title, bt.quantity, b.price FROM bucket bt " +
                "JOIN book b ON bt.book_id = b.book_id WHERE bt.clientnumber = ?";
        try (Connection connection = DBConnection.getConnection("bookflow_db");
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, clientNumber);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                String title = rs.getString("title");
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price");
                double total = quantity * price;

                // Add item to ListView
                String itemText = String.format("%s - 수량: %d | 가격: %, .0f 원 | 총액: %, .0f 원", title, quantity, price,
                        total);
                cartListView.getItems().add(itemText);

                // Calculate total price
                totalPrice += total;
            }

            // Update total price label
            DecimalFormat formatter = new DecimalFormat("#,###");
            totalPriceLabel.setText("총 가격: " + formatter.format(totalPrice) + " 원");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("오류", "장바구니 데이터를 로드하는 데 오류가 발생했습니다.");
        }
    }

    private void handlePurchase(ListView<String> cartListView) {
        // Handle the purchase logic
        try (Connection connection = DBConnection.getConnection("bookflow_db")) {
            String query = "DELETE FROM bucket WHERE clientnumber = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, clientNumber);
                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    cartListView.getItems().clear(); // Clear the cart
                    showAlert("구매 성공", "구매가 완료되었습니다.");
                } else {
                    showAlert("오류", "구매를 처리할 수 없습니다.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("오류", "구매 처리 중 오류가 발생했습니다.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showCartWindow() {
        cartStage.show();
    }
}
