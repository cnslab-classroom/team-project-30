package com.example.controller;

import javafx.scene.Scene;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import com.example.util.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Node;
import org.springframework.web.client.RestTemplate;

public class MainController {
    private Stage stage;
    private int clientNumber;
    private String serverUrl = "http://localhost:8080";
    private HBox headerBox;
    private Button infoButton;  // infoButton을 변수로 선언
    private Button bucketButton;
    private ListView<String> resultListView;
    
 // ComboBox와 TextField를 클래스의 인스턴스 변수로 선언
    private ComboBox<String> genreComboBox;
    private ComboBox<String> rateComboBox;
    private ComboBox<String> searchComboBox;
    private TextField searchField;

    public MainController() {
        initialize();
    }
    
    public void testServerConnection() {
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(serverUrl + "/test", String.class);
        System.out.println("Server Response: " + response);
    }

    public void setClientNumber(int clientNumber) {
        this.clientNumber = clientNumber;
        loadClientInfo();
    }

    public void updateBalance() {
        loadClientInfo();
        headerBox.layout();
    }

    private void loadClientInfo() {
        String query = "SELECT clientname, balance FROM client WHERE clientnumber = ?";
        try (Connection connection = DBConnection.getConnection("bookflow_db");
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, clientNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String clientName = resultSet.getString("clientname");
                    String balance = resultSet.getString("balance");

                    // infoButton만 텍스트 갱신
                    updateHeaderButtonText(clientName, balance);

                    // 레이아웃 강제 갱신
                    headerBox.layout();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateHeaderButtonText(String clientName, String balance) {
        try {
            // balance 문자열을 숫자로 변환
            double balanceValue = Double.parseDouble(balance);
            // 숫자 형식으로 포맷
            DecimalFormat formatter = new DecimalFormat("#,###");
            String formattedBalance = formatter.format(balanceValue);
            // infoButton 텍스트를 갱신
            infoButton.setText(clientName + "    " + formattedBalance + " 원");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            // 잔액 변환 실패 시에도 텍스트 갱신
            infoButton.setText(clientName + "    " + balance + " 원");
        }
    }
    
    private String buildSearchQuery() {
        String genre = genreComboBox.getValue();
        String rate = rateComboBox.getValue();
        String searchType = searchComboBox.getValue();
        String keyword = searchField.getText();

        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM book WHERE 1=1");

        if (!genre.equals("장르")) {
            queryBuilder.append(" AND genre = '").append(genre).append("'");
        }

        if (!"평점".equals(rate)) {
            double minRating = 0;
            double maxRating = 5;
            switch (rate) {
                case "1": minRating = 0; maxRating = 2; break;
                case "2": minRating = 2; maxRating = 3; break;
                case "3": minRating = 3; maxRating = 4; break;
                case "4": minRating = 4; maxRating = 5; break;
                case "5": minRating = 5; maxRating = 5; break;
            }
            queryBuilder.append(" AND rating BETWEEN ").append(minRating).append(" AND ").append(maxRating);
        }

        if (!keyword.isEmpty()) {
            if (searchType.equals("제목")) {
                queryBuilder.append(" AND title LIKE '%").append(keyword).append("%'");
            } else if (searchType.equals("저자")) {
                queryBuilder.append(" AND author LIKE '%").append(keyword).append("%'");
            }
        }

        return queryBuilder.toString();
    }
    
    private void initialize() {
        AnchorPane mainPane = new AnchorPane();
        mainPane.setPrefSize(1200, 750);

        // 헤더 박스
        headerBox = createHeaderBox();
        mainPane.getChildren().add(headerBox);
        AnchorPane.setTopAnchor(headerBox, 0.0);
        AnchorPane.setLeftAnchor(headerBox, 0.0);
        AnchorPane.setRightAnchor(headerBox, 0.0);

        // 추천 박스
        VBox suggestBox = createSuggestBox();
        mainPane.getChildren().add(suggestBox);
        AnchorPane.setTopAnchor(suggestBox, 52.0);
        AnchorPane.setLeftAnchor(suggestBox, 0.0);
        AnchorPane.setRightAnchor(suggestBox, 1000.0);
        AnchorPane.setBottomAnchor(suggestBox, 0.0);

        HBox searchBox = createSearchBox();
        mainPane.getChildren().add(searchBox);
        AnchorPane.setTopAnchor(searchBox, 52.0);
        AnchorPane.setLeftAnchor(searchBox, 200.0);
        AnchorPane.setRightAnchor(searchBox, 0.0);
        
        VBox resultBox = createResultBox();
        mainPane.getChildren().add(resultBox);
        AnchorPane.setTopAnchor(resultBox, 108.0);
        AnchorPane.setLeftAnchor(resultBox, 210.0);
        AnchorPane.setRightAnchor(resultBox, 10.0);
        AnchorPane.setBottomAnchor(resultBox, 10.0);
        
        // infoButton 클릭 시 InfoController에서 새 창 열기
        infoButton.setOnAction(e -> {
            InfoController infoController = new InfoController(clientNumber);
            infoController.setBalanceUpdateListener(() -> updateBalance());
            infoController.showInfoWindow();
        });
        
        // 장바구니 버튼 클릭 시 장바구니 창 열기
        bucketButton.setOnAction(e -> {
            Stage bucketStage = new Stage();
            bucketStage.setTitle("장바구니");
            
            // 빈 레이아웃 설정
            VBox bucketLayout = new VBox();
            bucketLayout.setPadding(new Insets(10));
            bucketLayout.setSpacing(10);
            
            // ListView 초기화
            ListView<String> bucketListView = new ListView<>();
            bucketListView.setPrefHeight(400);

            // 장바구니 데이터 로드
            loadBucketData(bucketListView);
            
            HBox deleteBox = new HBox();
            deleteBox.setSpacing(10);
            deleteBox.setAlignment(Pos.TOP_RIGHT);
            Button deleteAllButton = new Button("삭제");
            
            // 전체 삭제 버튼 클릭 시
            deleteAllButton.setOnAction(event -> {
                try (Connection connection = DBConnection.getConnection("bookflow_db")) {
                    String query = "DELETE FROM bucket WHERE clientnumber = ?";
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setInt(1, clientNumber); // 클라이언트 번호 설정
                        int rowsAffected = statement.executeUpdate();
                        if (rowsAffected > 0) {
                            // 삭제 성공 메시지 또는 업데이트된 장바구니 내용 표시
                            System.out.println("장바구니의 모든 항목이 삭제되었습니다.");
                            loadBucketData(bucketListView); // 장바구니 데이터를 다시 로드
                        } else {
                            System.out.println("삭제할 항목이 없습니다.");
                        }
                    }
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            });

            deleteBox.getChildren().addAll(deleteAllButton);
            
            deleteAllButton.setPrefWidth(200);

            // 장바구니 씬 설정
            Scene bucketScene = new Scene(bucketLayout, 800, 600);
            bucketStage.setScene(bucketScene);
            bucketStage.setResizable(false);

            // 모달 설정 (기존 창 조작 불가)
            bucketStage.initModality(Modality.APPLICATION_MODAL);
            
            // 장바구니 레이아웃에 ListView 추가
            bucketLayout.getChildren().addAll(bucketListView, deleteBox);

            bucketStage.showAndWait(); // showAndWait을 사용하여 창 닫힘 후에 코드 실행           
        });

        Scene scene = new Scene(mainPane);
        stage = new Stage();
        stage.setTitle("BookFlow");
        stage.setScene(scene);
        stage.setResizable(false);
    }
    
    private void loadBucketData(ListView<String> bucketListView) {
        bucketListView.getItems().clear(); // 기존 목록 비우기

        try (Connection connection = DBConnection.getConnection("bookflow_db")) {
            String query = "SELECT b.book_id, b.title, bt.quantity, b.price FROM bucket bt " +
                           "JOIN book b ON bt.book_id = b.book_id WHERE bt.clientnumber = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, clientNumber); // 클라이언트 번호 설정
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    int bookId = rs.getInt("book_id");  // 책 ID
                    String bookTitle = rs.getString("title");  // 책 제목
                    int quantity = rs.getInt("quantity");  // 수량
                    double price = rs.getDouble("price");  // 가격
                    double total = quantity * price;  // 총액

                    // 항목 내용 만들기 (bookId 포함)
                    String itemText = String.format("%s - %d | 수량: %d | 가격: %, .0f 원 | 총액: %, .0f 원",
                            bookTitle, bookId, quantity, price, total);
                    
                    bucketListView.getItems().add(itemText); // ListView에 항목 추가
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private HBox createHeaderBox() {
        HBox headerBox = new HBox();
        headerBox.setSpacing(15);
        headerBox.setPadding(new Insets(10));
        headerBox.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #cccccc;");

        Image logo = new Image("file:src/main/resources/images/logo.png");
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(170);
        logoView.setFitHeight(50);
        logoView.setPreserveRatio(true);

        Region spacer = new Region();

        bucketButton = new Button("장바구니");
        Button buyLogButton = new Button("구매내역");
        infoButton = new Button("아이디, 잔액");  // infoButton을 변수로 선언

        bucketButton.setPrefHeight(30);
        bucketButton.setPrefWidth(150);
        buyLogButton.setPrefHeight(30);
        buyLogButton.setPrefWidth(150);
        infoButton.setPrefHeight(30);
        infoButton.setPrefWidth(300);

        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(logoView, spacer, bucketButton, buyLogButton, infoButton);

        return headerBox;
    }

    private VBox createSuggestBox() {
        VBox suggestBox = new VBox();
        suggestBox.setPadding(new Insets(10));

        Button sampleButton = new Button("추천");

        suggestBox.getChildren().add(sampleButton);
        suggestBox.setStyle("-fx-background-color: #e0e0e0;");
        return suggestBox;
    }
    
    private VBox createResultBox() {
        VBox resultBox = new VBox();
        resultBox.setAlignment(Pos.TOP_RIGHT);
        resultBox.setSpacing(10);
        resultBox.setPadding(new Insets(10));

        // ListView를 초기화
        resultListView = new ListView<>(); // 클래스 필드에 할당
        resultBox.setStyle("-fx-background-color: #e0e0e0;");

        // 새로고침 버튼
        Button refreshButton = new Button("새로고침");
        refreshButton.setOnAction(e -> {
            String query = buildSearchQuery();
            executeSearchQuery(query);
        });

        // 장바구니 수량 입력 필드
        TextField bucketField = new TextField();
        bucketField.setPromptText("수량");

        // 장바구니 추가 버튼
        Button addBucketButton = new Button("장바구니 추가");
        addBucketButton.setOnAction(e -> {
            // 선택된 책 정보 가져오기
            String selectedBookText = resultListView.getSelectionModel().getSelectedItem();
            
            if (selectedBookText == null) {
                // 책을 선택하지 않으면 경고 메시지 표시
                showAlert("오류", "책을 선택해주세요.");
                return;
            }

            // 수량 입력 값 가져오기
            String quantityText = bucketField.getText();
            if (quantityText.isEmpty()) {
                // 수량이 입력되지 않으면 경고 메시지 표시
                showAlert("오류", "수량을 입력해주세요.");
                return;
            }

            // 수량이 숫자인지 확인
            int quantity = 0;
            try {
                quantity = Integer.parseInt(quantityText);
                if (quantity <= 0) {
                    showAlert("오류", "수량은 1 이상이어야 합니다.");
                    return;
                }
            } catch (NumberFormatException ex) {
                showAlert("오류", "수량은 유효한 숫자만 입력 가능합니다.");
                return;
            }

            // 선택한 책 정보 파싱 (가격과 재고 추출)
            String[] parts = selectedBookText.split(";");
            String title = parts[0];
            int stock = Integer.parseInt(parts[5].replace(",", "")); // 재고 정보

            if (quantity > stock) {
                // 수량이 재고보다 많으면 오류 메시지 표시
                showAlert("오류", "재고보다 많은 수량을 입력할 수 없습니다.");
            } else {
                // 장바구니에 추가
                addToCart(title, quantity);
            }
        });

        resultBox.getChildren().addAll(refreshButton, resultListView, bucketField, addBucketButton);

        return resultBox;
    }
    
    // 장바구니에 책 추가하는 메서드
    private void addToCart(String bookTitle, int quantity) {
        // 먼저 책 제목을 통해 book_id를 조회해야 함
        String getBookIdQuery = "SELECT book_id, price FROM book WHERE title = ?";
        try (Connection connection = DBConnection.getConnection("bookflow_db");
             PreparedStatement statement = connection.prepareStatement(getBookIdQuery)) {
            
            statement.setString(1, bookTitle);  // 책 제목을 파라미터로 설정
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int bookId = resultSet.getInt("book_id");  // 책 ID
                    double bookPrice = resultSet.getDouble("price");  // 책 가격
                    
                    // 책을 찾았다면, bucket 테이블에 추가
                    addBookToBucket(bookTitle, bookId, quantity, bookPrice);  // bookTitle을 추가
                } else {
                    // 책을 찾을 수 없으면 알림
                    showAlert("오류", "책을 찾을 수 없습니다.");
                }
            }
        } catch (Exception e) {
            showAlert("오류", "장바구니에 책을 추가하는데 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }
    
    // bucket 테이블에 책을 추가하는 메서드
    private void addBookToBucket(String bookTitle, int bookId, int quantity, double price) {
        String insertQuery = "INSERT INTO bucket (clientnumber, book_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection("bookflow_db");
             PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            
            statement.setInt(1, clientNumber);  // 현재 클라이언트 번호
            statement.setInt(2, bookId);  // 책 ID
            statement.setInt(3, quantity);  // 수량
            statement.setDouble(4, price);  // 가격

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("책 '" + bookTitle + "'이/가 " + quantity + "개 장바구니에 추가되었습니다.");
            } else {
                showAlert("오류", "장바구니에 추가하는데 실패했습니다.");
            }
        } catch (Exception e) {
            showAlert("오류", "장바구니에 책을 추가하는데 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }
    
    // 알림을 표시하는 메서드 (Alert Dialog 사용)
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private HBox createSearchBox() {
        HBox searchBox = new HBox();
        searchBox.setPadding(new Insets(10));

        // ComboBox와 TextField를 클래스 인스턴스 변수로 초기화
        genreComboBox = new ComboBox<>();
        genreComboBox.getItems().addAll("장르", "판타지", "SF", "로맨스", "미스터리", "공포", "역사", "청소년", "자기계발", "과학", "철학", "종교", "예술", "에세이", "여행", "요리", "교육");
        genreComboBox.setValue("장르");
        genreComboBox.setPrefWidth(100);

        rateComboBox = new ComboBox<>();
        rateComboBox.getItems().addAll("평점", "1", "2", "3", "4", "5");
        rateComboBox.setValue("평점");
        rateComboBox.setPrefWidth(100);

        searchComboBox = new ComboBox<>();
        searchComboBox.getItems().addAll("제목", "저자");
        searchComboBox.setValue("제목");
        searchComboBox.setPrefWidth(100);

        searchField = new TextField();
        searchField.setPromptText("입력");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button searchButton = new Button("검색");
        searchButton.setPrefWidth(100);

        // 검색 버튼 클릭 시 동작 정의
        searchButton.setOnAction(event -> {
            String query = buildSearchQuery();
            executeSearchQuery(query);
        });

        searchBox.getChildren().addAll(genreComboBox, rateComboBox, searchComboBox, searchField, searchButton);
        searchBox.setStyle("-fx-background-color: #87CEEB;");

        searchBox.setMargin(genreComboBox, new Insets(0, 5, 0, 0));
        searchBox.setMargin(rateComboBox, new Insets(0, 5, 0, 0));
        searchBox.setMargin(searchField, new Insets(0, 5, 0, 0));

        return searchBox;
    }
    
    private void executeSearchQuery(String query) {
        try (Connection connection = DBConnection.getConnection("bookflow_db");
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            // ListView 초기화 (기존 검색 결과 제거)
            resultListView.getItems().clear();

            // 검색 결과를 저장
            while (resultSet.next()) {
                String bookTitle = resultSet.getString("title");
                String bookAuthor = resultSet.getString("author");
                String bookGenre = resultSet.getString("genre");
                int bookPrice = resultSet.getInt("price"); // 가격 추가
                double bookRating = resultSet.getDouble("rating");
                int bookStock = resultSet.getInt("stock");

                // 하나의 결과 문자열로 저장
                String resultText = String.format("%s;%s;%s;%,d;%.1f;%,d",
                        bookTitle, bookAuthor, bookGenre, bookPrice, bookRating, bookStock);

                resultListView.getItems().add(resultText);
            }

            // ListView에 셀 팩토리 적용
            resultListView.setCellFactory(listView -> new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        // 데이터를 분리 (세미콜론으로 구분된 데이터)
                        String[] parts = item.split(";");
                        String title = parts[0];
                        String author = parts[1];
                        String genre = parts[2];
                        String price = parts[3];
                        String rating = parts[4];
                        String stock = parts[5];

                        // HBox 레이아웃 생성
                        HBox hBox = new HBox(10); // 10px 간격
                        hBox.setPadding(new Insets(5, 10, 5, 10));
                        hBox.setAlignment(Pos.CENTER_LEFT); // 왼쪽 정렬

                        // 제목 Label (고정 폭 설정)
                        Label titleLabel = new Label(title);
                        titleLabel.setPrefWidth(300); // 제목 영역 고정 폭
                        titleLabel.setMaxWidth(300);
                        titleLabel.setEllipsisString("...");
                        titleLabel.setWrapText(false);
                        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                        Tooltip tooltip = new Tooltip(title);
                        Tooltip.install(titleLabel, tooltip);

                        // 나머지 정보 Label
                        Label infoLabel = new Label(String.format("저자: %s  장르: %s  가격: %s원  평점: %s  재고: %s",
                                author, genre, price, rating, stock));
                        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

                        // 재고 강조 (0 이하일 경우 빨간색)
                        if (Integer.parseInt(stock.replace(",", "")) <= 0) {
                            infoLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                        }

                        // 레이아웃 추가
                        hBox.getChildren().addAll(titleLabel, infoLabel);

                        // 그래픽으로 설정
                        setGraphic(hBox);
                    }
                }
            });

        } catch (Exception e) {
            showAlert("오류", "클라이언트 정보를 불러오는 데 실패했습니다.");
            e.printStackTrace();
        }
    }

    public void show() {
        stage.show();
    }
}
