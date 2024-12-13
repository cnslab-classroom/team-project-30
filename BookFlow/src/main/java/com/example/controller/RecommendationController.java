package com.example.controller;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;

import com.example.util.DBConnection;

public class RecommendationController {

    private Stage recommendationStage;
    private int clientNumber;

    public RecommendationController(int clientNumber) {
        this.clientNumber = clientNumber;
        initialize();
    }

    private void initialize() {
        AnchorPane recommendationPane = new AnchorPane();

        // ComboBox for genre and rating
        ComboBox<String> genreComboBox = new ComboBox<>();
        ComboBox<String> ratingComboBox = new ComboBox<>();
        Button recommendButton = new Button("추천 받기");
        ListView<String> recommendationListView = new ListView<>();

        genreComboBox.getItems().addAll("장르", "판타지", "로맨스", "SF", "미스터리", "공포", "역사");
        ratingComboBox.getItems().addAll("평점", "1", "2", "3", "4", "5");

        // Set default values
        genreComboBox.setValue("장르");
        ratingComboBox.setValue("평점");

        // Set positions for elements
        AnchorPane.setTopAnchor(genreComboBox, 20.0);
        AnchorPane.setLeftAnchor(genreComboBox, 20.0);
        AnchorPane.setTopAnchor(ratingComboBox, 20.0);
        AnchorPane.setLeftAnchor(ratingComboBox, 200.0);
        AnchorPane.setTopAnchor(recommendButton, 60.0);
        AnchorPane.setLeftAnchor(recommendButton, 20.0);
        AnchorPane.setTopAnchor(recommendationListView, 100.0);
        AnchorPane.setLeftAnchor(recommendationListView, 20.0);

        // Recommend Button Action
        recommendButton
                .setOnAction(event -> handleRecommendation(genreComboBox, ratingComboBox, recommendationListView));

        // Add components to AnchorPane
        recommendationPane.getChildren().addAll(genreComboBox, ratingComboBox, recommendButton, recommendationListView);

        // Create Scene and Stage for Recommendations
        Scene scene = new Scene(recommendationPane, 600, 350);
        recommendationStage = new Stage();
        recommendationStage.setTitle("추천 도서");
        recommendationStage.setScene(scene);
        recommendationStage.initModality(Modality.APPLICATION_MODAL);
        recommendationStage.setResizable(false);
    }

    private void handleRecommendation(ComboBox<String> genreComboBox, ComboBox<String> ratingComboBox,
            ListView<String> recommendationListView) {
        String genre = genreComboBox.getValue();
        String rating = ratingComboBox.getValue();

        // Generate SQL query based on selected filters
        String query = "";

        // Genre-based recommendation
        if (!genre.equals("장르")) {
            query = "SELECT title FROM book WHERE genre = ?";
        }
        // Rating-based recommendation
        else if (!rating.equals("평점")) {
            query = "SELECT title FROM book WHERE rating >= ?";
        }

        // Execute query and display recommendations
        try (Connection connection = DBConnection.getConnection("bookflow_db");
                PreparedStatement statement = connection.prepareStatement(query)) {
            if (!genre.equals("장르")) {
                statement.setString(1, genre);
            } else {
                statement.setString(1, rating);
            }

            ResultSet rs = statement.executeQuery();

            // Add recommended books to ListView
            ArrayList<String> recommendations = new ArrayList<>();
            while (rs.next()) {
                recommendations.add(rs.getString("title"));
            }

            recommendationListView.getItems().clear();
            recommendationListView.getItems().addAll(recommendations);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("오류", "추천 도서를 로드하는 데 오류가 발생했습니다.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showRecommendationWindow() {
        recommendationStage.show();
    }
}
