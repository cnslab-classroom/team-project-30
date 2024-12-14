package com.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/bookflow?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    // MySQL 기본 URL

    public static Connection getConnection(String dbName) throws SQLException {
        String dbUrl = "jdbc:mysql://localhost:3306/" + dbName
                + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String username = "dave"; // 사용자명
        String password = "asshole12!"; // 비밀번호

        return DriverManager.getConnection(dbUrl, username, password);
    }

}
