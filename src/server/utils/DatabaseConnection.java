package utils;

import java.sql.*;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private static String connectionUrl;
    private static String username;
    private static String password;
    
    private DatabaseConnection() {
        // Usa le variabili dal tuo .env
        String host = getEnvOrDefault("DB_HOST", "mysql");
        String port = getEnvOrDefault("DB_PORT", "3306");
        String database = getEnvOrDefault("DB_NAME", "cloudstore_db");
        
        // Usa l'utente emi (che esiste nel tuo DB)
        username = getEnvOrDefault("DB_USER", "emi");
        password = getEnvOrDefault("DB_PASSWORD", "tramonta");
        
        System.err.println("=== DatabaseConnection Debug ===");
        System.err.println("DB_HOST: " + host);
        System.err.println("DB_PORT: " + port);
        System.err.println("DB_NAME: " + database);
        System.err.println("DB_USER: " + username);
        System.err.println("DB_PASSWORD: " + (password != null ? "***" : "null"));
        
        connectionUrl = String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
            host, port, database
        );
        System.err.println("Connection URL: " + connectionUrl);
    }
    
    private String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isEmpty()) {
            System.err.println("Env " + key + " not set, using default: " + defaultValue);
            return defaultValue;
        }
        System.err.println("Env " + key + " = " + value);
        return value;
    }
    
    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    public Connection getConnection() throws SQLException {
        System.err.println("Attempting to connect to database...");
        try {
            Connection conn = DriverManager.getConnection(connectionUrl, username, password);
            System.err.println("Connection successful!");
            return conn;
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            throw e;
        }
    }
}