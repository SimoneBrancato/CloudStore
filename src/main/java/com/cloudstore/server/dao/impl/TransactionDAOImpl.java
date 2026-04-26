package com.cloudstore.server.dao.impl;

import com.cloudstore.server.dao.interfaces.TransactionDAO;
import com.cloudstore.server.model.entities.Transaction;
import com.cloudstore.server.model.entities.Product;
import com.cloudstore.server.model.domain.TopCustomerSummary;
import com.cloudstore.server.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionDAOImpl implements TransactionDAO {
    
    private final DatabaseConnection dbConnection; // Database connection instance

    // Constructor initializes the database connection
    public TransactionDAOImpl() throws SQLException {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
        * Finds a transaction by its ID.
        * @param id the transaction ID
        * @return an Optional containing the transaction if found, otherwise empty
        * @throws SQLException if a database error occurs
    **/
    @Override
    public Optional<Transaction> findById(long id) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT t.*, p.* " +
                 "FROM transactions t " +
                 "JOIN products p ON t.Product_Id = p.Product_Id " +
                 "WHERE t.Transaction_ID = ?"
             )) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTransaction(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
        * Finds transactions by customer name.
        * @param customerName the name of the customer
        * @return a list of transactions associated with the customer
        * @throws SQLException if a database error occurs
    **/
    @Override
    public List<Transaction> findByCustomer(String customerName) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT t.*, p.* " +
                 "FROM transactions t " +
                 "JOIN products p ON t.Product_Id = p.Product_Id " +
                 "WHERE t.Customer_Name = ? " +
                 "ORDER BY t.Date DESC"
             )) {
            
            stmt.setString(1, customerName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return transactions;
    }

    /**
        * Finds transactions by product ID.
        * @param productId the ID of the product
        * @return a list of transactions associated with the product
        * @throws SQLException if a database error occurs
    **/
    @Override
    public List<Transaction> findByProduct(int productId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT t.*, p.* " +
                 "FROM transactions t " +
                 "JOIN products p ON t.Product_Id = p.Product_Id " +
                 "WHERE t.Product_Id = ? " +
                 "ORDER BY t.Date DESC"
             )) {
            
            stmt.setInt(1, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return transactions;
    }

    /**
        * Finds recent transactions for a specific product.
        * @param productId the ID of the product
        * @param limit the maximum number of transactions to return
        * @return a list of recent transactions associated with the product
        * @throws SQLException if a database error occurs
    **/
    @Override
    public List<Transaction> findRecentByProduct(int productId, int limit) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT t.*, p.* " +
                 "FROM transactions t " +
                 "JOIN products p ON t.Product_Id = p.Product_Id " +
                 "WHERE t.Product_Id = ? " +
                 "ORDER BY t.Date DESC " +
                 "LIMIT ?"
             )) {

            stmt.setInt(1, productId);
            stmt.setInt(2, Math.max(1, limit));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return transactions;
    }

    /**
        * Finds transactions within a specified date range.
        * @param start the start date and time
        * @param end the end date and time
        * @return a list of transactions that occurred within the date range
        * @throws SQLException if a database error occurs
    **/
    @Override
    public List<Transaction> findByDateRange(LocalDateTime start, LocalDateTime end) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT t.*, p.* " +
                 "FROM transactions t " +
                 "JOIN products p ON t.Product_Id = p.Product_Id " +
                 "WHERE t.Date BETWEEN ? AND ? " +
                 "ORDER BY t.Date"
             )) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(start));
            stmt.setTimestamp(2, Timestamp.valueOf(end));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return transactions;
    }
    
    /**
        * Finds transactions by payment method.
        * @param paymentMethod the payment method
        * @return a list of transactions associated with the payment method
        * @throws SQLException if a database error occurs
    **/
    @Override
    public List<Transaction> findByPaymentMethod(String paymentMethod) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT t.*, p.* " +
                 "FROM transactions t " +
                 "JOIN products p ON t.Product_Id = p.Product_Id " +
                 "WHERE t.Payment_Method = ? " +
                 "ORDER BY t.Date DESC"
             )) {
            
            stmt.setString(1, paymentMethod);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return transactions;
    }
    
    /**
        * Finds transactions by city.
        * @param city the city
        * @return a list of transactions associated with the city
        * @throws SQLException if a database error occurs
    **/
    @Override
    public List<Transaction> findByCity(String city) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT t.*, p.* " +
                 "FROM transactions t " +
                 "JOIN products p ON t.Product_Id = p.Product_Id " +
                 "WHERE t.City = ? " +
                 "ORDER BY t.Date DESC"
             )) {
            
            stmt.setString(1, city);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return transactions;
    }
    
    /**
        * Retrieves all transactions from the database.
        * @return a list of all transactions
        * @throws SQLException if a database error occurs
    **/
    @Override
    public List<Transaction> findAll() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT t.*, p.* " +
                 "FROM transactions t " +
                 "JOIN products p ON t.Product_Id = p.Product_Id " +
                 "ORDER BY t.Date DESC"
             );
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }
    
    /**
        * Saves a transaction to the database. If the transaction has an ID of 0, a new ID will be generated.
        * @param transaction the transaction to save
        * @return the saved transaction with an assigned ID
        * @throws SQLException if a database error occurs
    **/
    @Override
    public Transaction save(Transaction transaction) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            return save(conn, transaction);
        }
    }

    /**
        * Saves a transaction to the database using an existing connection. This method is used for transactions that are part of a larger operation.
        * @param conn the database connection to use
        * @param transaction the transaction to save
        * @return the saved transaction with an assigned ID
        * @throws SQLException if a database error occurs
    **/
    @Override
    public Transaction save(Connection conn, Transaction transaction) throws SQLException {
        final int maxRetries = 5;
        int attempts = 0;

        while (true) {
            attempts++;
            long id = transaction.id();
            if (id == 0) {
                id = getNextId(conn);
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO transactions (" +
                "Transaction_ID, Date, Customer_Name, Product, Total_Items, Total_Cost, " +
                "Payment_Method, City, Discount_Applied, Customer_Category, Product_Id, Discount" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            )) {
                stmt.setLong(1, id);
                stmt.setTimestamp(2, Timestamp.valueOf(transaction.date()));
                stmt.setString(3, transaction.CustomerName());
                stmt.setString(4, transaction.Product());
                stmt.setInt(5, transaction.TotalItems());
                stmt.setDouble(6, transaction.TotalCost());
                stmt.setString(7, transaction.PaymentMethod());
                stmt.setString(8, transaction.City());
                stmt.setInt(9, transaction.DiscountApplied());
                stmt.setString(10, transaction.CustomerCategory());
                stmt.setInt(11, transaction.ProductID().id());
                stmt.setFloat(12, transaction.Discount());

                stmt.executeUpdate();
                return new Transaction(id, transaction.date(), transaction.CustomerName(),
                        transaction.Product(), transaction.TotalItems(), transaction.TotalCost(),
                        transaction.PaymentMethod(), transaction.City(), transaction.DiscountApplied(),
                        transaction.CustomerCategory(), transaction.Discount(), transaction.ProductID());
            } catch (SQLIntegrityConstraintViolationException e) {
                if (transaction.id() != 0 || attempts >= maxRetries) {
                    throw e;
                }
            }
        }
    }

    /**
        * Retrieves the next available transaction ID by finding the maximum existing ID and adding 1. This method is used to generate unique IDs for new transactions.
        * @param conn the database connection to use
        * @return the next available transaction ID
        * @throws SQLException if a database error occurs
    **/
    private long getNextId(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
            "SELECT COALESCE(MAX(Transaction_ID), 0) + 1 FROM transactions"
        );
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 1L;
        }
    }
    
    /**
        * Deletes a transaction from the database.
        * @param id the ID of the transaction to delete
        * @return true if the transaction was deleted, false otherwise
        * @throws SQLException if a database error occurs
    **/
    @Override
    public boolean delete(long id) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM transactions WHERE Transaction_ID = ?"
             )) {
            
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
        * Checks if a transaction with the specified ID exists in the database.
        * @param id the ID of the transaction to check
        * @return true if the transaction exists, false otherwise
        * @throws SQLException if a database error occurs
    **/
    @Override
    public boolean exists(long id) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM transactions WHERE Transaction_ID = ?"
             )) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
        * Calculates the total sales for a given date range.
        * @param start the start date of the range
        * @param end the end date of the range
        * @return the total sales amount
        * @throws SQLException if a database error occurs
    **/
    @Override
    public double calculateTotalSales(LocalDateTime start, LocalDateTime end) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COALESCE(SUM(Total_Cost), 0) FROM transactions WHERE Date BETWEEN ? AND ?"
             )) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(start));
            stmt.setTimestamp(2, Timestamp.valueOf(end));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }
    
    /**
        * Counts the number of transactions within a given date range.
        * @param start the start date of the range
        * @param end the end date of the range
        * @return the count of transactions
        * @throws SQLException if a database error occurs
    **/
    @Override
    public int countByDateRange(LocalDateTime start, LocalDateTime end) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM transactions WHERE Date BETWEEN ? AND ?"
             )) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(start));
            stmt.setTimestamp(2, Timestamp.valueOf(end));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    
    /**
        * Finds the most recent transactions up to a specified limit.
        * @param limit the maximum number of transactions to retrieve
        * @return a list of recent transactions
        * @throws SQLException if a database error occurs
    **/
    @Override
    public List<Transaction> findRecentTransactions(int limit) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT t.*, p.* " +
                 "FROM transactions t " +
                 "JOIN products p ON t.Product_Id = p.Product_Id " +
                 "ORDER BY t.Date DESC LIMIT ?"
             )) {
            
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return transactions;
    }
    
    /**
        * Counts the total number of transactions in the database.
        * @return the total count of transactions
        * @throws SQLException if a database error occurs
    **/
    @Override
    public int count() throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM transactions");
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public int countDistinctProductsSold() throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(DISTINCT Product_Id) FROM transactions");
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public List<TopCustomerSummary> findTopCustomers(int limit) throws SQLException {
        List<TopCustomerSummary> topCustomers = new ArrayList<>();
        String sql = "SELECT Customer_Name, COUNT(*) as orderCount, SUM(Total_Cost) as totalSpent, MAX(Date) as lastOrderDate " +
                     "FROM transactions GROUP BY Customer_Name ORDER BY totalSpent DESC LIMIT ?";
                     
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    topCustomers.add(new TopCustomerSummary(
                        rs.getString("Customer_Name"),
                        rs.getInt("orderCount"),
                        rs.getDouble("totalSpent"),
                        rs.getTimestamp("lastOrderDate").toLocalDateTime()
                    ));
                }
            }
        }
        return topCustomers;
    }
    
    /**
        * Maps a ResultSet row to a Transaction object. This method is used to convert database query results into Transaction instances.
        * @param rs the ResultSet containing the transaction data
        * @return a Transaction object representing the data in the ResultSet
        * @throws SQLException if a database error occurs while accessing the ResultSet
    **/
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Product product = new Product(
            rs.getInt("Product_Id"),
            rs.getString("Product_Name"),
            rs.getString("Category"),
            rs.getDouble("Price"),
            rs.getInt("Stock_Quantity")
        );
        
        return new Transaction(
            rs.getLong("Transaction_ID"),
            rs.getTimestamp("Date").toLocalDateTime(),
            rs.getString("Customer_Name"),
            rs.getString("Product"),
            rs.getInt("Total_Items"),
            rs.getDouble("Total_Cost"),
            rs.getString("Payment_Method"),
            rs.getString("City"),
            rs.getInt("Discount_Applied"),
            rs.getString("Customer_Category"),
            rs.getFloat("Discount"),
            product
        );
    }
}