package com.cloudstore.utils;

import java.sql.*;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private static String connectionUrl;
    private static String username;
    private static String password;
    
    private DatabaseConnection() {
        String host = getEnvOrDefault("DB_HOST", "localhost");
        String port = getEnvOrDefault("DB_PORT", "3307");
        String database = getEnvOrDefault("DB_NAME", "cloudstore_db");
        username = getEnvOrDefault("DB_USER", "emi");
        password = getEnvOrDefault("DB_PASSWORD", "tramonta");
        
        connectionUrl = String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
            host, port, database
        );
    }
    
    private String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
    
    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(connectionUrl, username, password);
    }
}