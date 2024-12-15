package com.example.controller;

import com.example.util.DBConnection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

import java.sql.*;

public class ReviewController {

    // 리뷰 클래스 정의
    public static class Review {
        private String clientName;
        private String reviewContent;
        private Double rating;
        private Timestamp reviewDate;

        public Review(String clientName, String reviewContent, double rating, Timestamp reviewDate) {
            this.clientName = clientName;
            this.reviewContent = reviewContent;
            this.rating = rating;
            this.reviewDate = reviewDate;
        }

        // Getter methods
        public String getClientName() {
            return clientName;
        }

        public String getReviewContent() {
            return reviewContent;
        }

        public double getRating() {
            return rating;
        }

        public Timestamp getReviewDate() {
            return reviewDate;
        }

        @Override
        public String toString() {
            return String.format("작성자: %s, 평점: %.1f, 리뷰: %s, 날짜: %s",
                    clientName, rating, reviewContent, reviewDate.toString());
        }
    }

    // 책에 대한 모든 리뷰를 가져오는 메서드
    public ObservableList<Review> getReviewsForBook(int bookId) {
        List<Review> reviews = new ArrayList<>();

        // 데이터베이스 연결 및 쿼리 실행
        String query = "SELECT client.clientname, review.review_content, review.rating, review.review_date " +
                "FROM review " +
                "JOIN client ON review.clientnumber = client.clientnumber " +
                "WHERE review.book_id = ?";

        try (Connection connection = DBConnection.getConnection("bookflow_db");
                PreparedStatement statement = connection.prepareStatement(query)) {

            // bookId 파라미터 설정
            statement.setInt(1, bookId);

            try (ResultSet resultSet = statement.executeQuery()) {
                // 결과를 List에 담기
                while (resultSet.next()) {
                    String clientName = resultSet.getString("clientname");
                    String reviewContent = resultSet.getString("review_content");
                    double rating = resultSet.getDouble("rating");
                    Timestamp reviewDate = resultSet.getTimestamp("review_date");

                    // Review 객체 생성 후 리스트에 추가
                    reviews.add(new Review(clientName, reviewContent, rating, reviewDate));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ObservableList<Review> observableList = FXCollections.observableArrayList(reviews);

        return observableList;
    }

    // 특정 책에 대한 특정 사용자의 리뷰 유무 확인
    public boolean isReviewExist(int bookId, int clientNumber) {
        String query = "SELECT COUNT(*) FROM review WHERE book_id = ? AND clientnumber = ?";
        
        try (Connection connection = DBConnection.getConnection("bookflow_db");
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, bookId);
            statement.setInt(2, clientNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0; // 리뷰가 있으면 true
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // 리뷰가 없으면 false
    }

    // 리뷰를 추가하는 메서드
    public boolean addReview(int bookId, int clientNumber, String reviewContent, double rating) {
        String query = "INSERT INTO review (book_id, clientnumber, review_content, rating) VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection("bookflow_db");
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, bookId);
            statement.setInt(2, clientNumber);
            statement.setString(3, reviewContent);
            statement.setDouble(4, rating);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0; // 성공적으로 삽입되면 true
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // 오류가 발생하면 false
    }
}
