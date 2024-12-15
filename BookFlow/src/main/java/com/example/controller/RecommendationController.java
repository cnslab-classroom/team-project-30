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
    }

    public void showRecommendationWindowWithGenres(ArrayList<String> genres) {
        // Add logic to pass genres to the ListView recommendation process
        ListView<String> recommendationListView = new ListView<>();
        handleRecommendation(recommendationListView, genres);

        // Populate the UI with the recommendations
        AnchorPane recommendationPane = new AnchorPane();
        AnchorPane.setTopAnchor(recommendationListView, 20.0);
        AnchorPane.setLeftAnchor(recommendationListView, 20.0);
        recommendationPane.getChildren().add(recommendationListView);

        Scene scene = new Scene(recommendationPane, 600, 600);
        recommendationStage = new Stage();
        recommendationStage.setTitle("추천 도서");
        recommendationStage.setScene(scene);
        recommendationStage.initModality(Modality.APPLICATION_MODAL);
        recommendationStage.setResizable(false);
        recommendationStage.show();
    }

    private void handleRecommendation(ListView<String> recommendationListView, ArrayList<String> genres) {
        String query;

        if (genres.isEmpty()) {
            // Fallback: Recommend books with high ratings
            query = "SELECT title, rating FROM book ORDER BY rating DESC LIMIT 10";
        } else {
            // Recommend books based on genres in purchase history
            StringBuilder genrePlaceholders = new StringBuilder();
            for (int i = 0; i < genres.size(); i++) {
                genrePlaceholders.append("?");
                if (i < genres.size() - 1) genrePlaceholders.append(", ");
            }
            query = "SELECT DISTINCT title, rating FROM book WHERE genre IN (" 
                    + genrePlaceholders + ") ORDER BY rating DESC LIMIT 10";
        }

        try (Connection connection = DBConnection.getConnection("bookflow_db");
             PreparedStatement statement = connection.prepareStatement(query)) {

            if (!genres.isEmpty()) {
                for (int i = 0; i < genres.size(); i++) {
                    statement.setString(i + 1, genres.get(i));
                }
            }

            ResultSet rs = statement.executeQuery();
            ArrayList<String> recommendations = new ArrayList<>();
            while (rs.next()) {
                String title = rs.getString("title");
                double rating = rs.getDouble("rating");
                recommendations.add(title + " (평점: " + rating + ")");
            }

            recommendationListView.getItems().clear();
            if (recommendations.isEmpty()) {
                recommendationListView.getItems().add("추천할 책이 없습니다.");
            } else {
                recommendationListView.getItems().addAll(recommendations);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("오류", "추천 도서를 로드하는 데 오류가 발생했습니다.");
        }
    }

    public void showRecommendationWindow() {
        // Default: Fetch and recommend books without any genres
        showRecommendationWindowWithGenres(new ArrayList<>());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
