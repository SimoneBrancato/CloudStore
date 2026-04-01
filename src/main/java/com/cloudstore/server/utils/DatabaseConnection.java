package com.cloudstore.server.utils;

import java.sql.*;

public class DatabaseConnection {
    private static DatabaseConnection instance; // Singleton instance
    private static String connectionUrl; // Database connection URL
    private static String username; // Database username
    private static String password; // Database password
    
    /**
        * Private constructor for the DatabaseConnection class.
    **/
    private DatabaseConnection() {
        String host = getEnvOrDefault("DB_HOST", "mysql");
        String port = getEnvOrDefault("DB_PORT", "3306");
        String database = getEnvOrDefault("DB_NAME", "cloudstore_db");
        
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
    
    /**
        * Retrieves the value of an environment variable or returns a default value if it's not set.
        * @param key The environment variable name.
        * @param defaultValue The default value to return if the environment variable is not set.
        * @return The value of the environment variable or the default value.
    **/
    private String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isEmpty()) {
            System.err.println("Env " + key + " not set, using default: " + defaultValue);
            return defaultValue;
        }
        System.err.println("Env " + key + " = " + value);
        return value;
    }
    
    /**
        * Retrieves the singleton instance of the DatabaseConnection class.
        * @return The singleton instance of the DatabaseConnection class.
        * @throws SQLException If an error occurs while initializing the database connection.
    **/
    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
        * Establishes and returns a connection to the database.
        * @return A Connection object representing the connection to the database.
        * @throws SQLException If an error occurs while connecting to the database.
    **/
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