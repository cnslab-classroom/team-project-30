package com.example.util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/";  // MySQL 기본 URL
    // 데이터베이스 이름을 매개변수로 받아서 연결하는 메서드
    public static Connection getConnection(String dbName) throws SQLException {
        String dbUrl = URL + dbName;  // 데이터베이스 이름을 URL에 추가
        // 실제 사용 중인 MySQL 사용자 이름과 비밀번호로 변경해야 합니다.
        String username = "root";  // 실제 MySQL 사용자 이름
        String password = "yunotest";  // 실제 MySQL 비밀번호
        // 데이터베이스 연결
        return DriverManager.getConnection(dbUrl, username, password);  
    }
}