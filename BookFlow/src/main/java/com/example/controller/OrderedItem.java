package com.example.controller;

public class OrderedItem {
    private int orderId;
    private String title;
    private int bookId;
    private int quantity;
    private double totalPrice;
    private String orderDate;

    public OrderedItem(int orderId, String title, int bookId, int quantity, double totalPrice, String orderDate) {
        this.orderId = orderId;
        this.title = title;
        this.bookId = bookId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
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