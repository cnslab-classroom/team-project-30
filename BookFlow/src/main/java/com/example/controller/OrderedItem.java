package com.example.controller;

public class OrderedItem {
    private int orderId;
    private String title;
    private int bookId;
    private int quantity;
    private double totalPrice;
    private String orderDate;

    // 기존 생성자
    public OrderedItem(int orderId, String title, int bookId, int quantity, double totalPrice, String orderDate) {
        this.orderId = orderId;
        this.title = title;
        this.bookId = bookId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
    }

    // 주문 ID 반환하는 메서드 추가
    public int getOrderId() {
        return orderId;
    }

    // 나머지 getter 메서드들
    public String getTitle() {
        return title;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public int getBookId() {
        return bookId;
    }

    @Override
    public String toString() {
        return String.format("주문 번호: %d | 책: %s | 수량: %d | 총 가격: %.2f원 | 주문 날짜: %s",
                orderId, title, quantity, totalPrice, orderDate);
    }
}
