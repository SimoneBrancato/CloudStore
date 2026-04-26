package com.cloudstore.server.model.dto;
import java.util.List;

/**
 * Data Transfer Object representing a user's profile information and history.
**/
public class UserProfileDTO {
    private UserDTO user; // The user entity details
    private List<TransactionDTO> orderHistory; // List of previous transactions for this user
    private int totalOrders; // Total number of orders placed
    private double totalSpent; // Total lifetime spend for this user

    /**
     * Default constructor for frameworks that require it.
    **/
    public UserProfileDTO() {}

    /**
     * Constructor to initialize all fields.
     * @param user The user entity details.
     * @param orderHistory List of previous transactions for this user.
     * @param totalOrders Total number of orders placed.
     * @param totalSpent Total lifetime spend for this user.
    **/
    public UserProfileDTO(UserDTO user, List<TransactionDTO> orderHistory, int totalOrders, double totalSpent) {
        this.user = user;
        this.orderHistory = orderHistory;
        this.totalOrders = totalOrders;
        this.totalSpent = totalSpent;
    }

    /**
     * Gets the the user entity details.
     * @return The user.
    **/
    public UserDTO getUser() { return user; }

    /**
     * Sets the the user entity details.
     * @param user The user.
    **/
    public void setUser(UserDTO user) { this.user = user; }
    /**
     * Gets the list of previous transactions for this user.
     * @return The orderHistory.
    **/
    public List<TransactionDTO> getOrderHistory() { return orderHistory; }

    /**
     * Sets the list of previous transactions for this user.
     * @param orderHistory The orderHistory.
    **/
    public void setOrderHistory(List<TransactionDTO> orderHistory) { this.orderHistory = orderHistory; }
    /**
     * Gets the total number of orders placed.
     * @return The totalOrders.
    **/
    public int getTotalOrders() { return totalOrders; }

    /**
     * Sets the total number of orders placed.
     * @param totalOrders The totalOrders.
    **/
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
    /**
     * Gets the total lifetime spend for this user.
     * @return The totalSpent.
    **/
    public double getTotalSpent() { return totalSpent; }

    /**
     * Sets the total lifetime spend for this user.
     * @param totalSpent The totalSpent.
    **/
    public void setTotalSpent(double totalSpent) { this.totalSpent = totalSpent; }
}
