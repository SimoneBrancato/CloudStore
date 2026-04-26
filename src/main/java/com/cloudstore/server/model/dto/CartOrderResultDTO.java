package com.cloudstore.server.model.dto;
import java.util.List;

/**
 * Data Transfer Object for representing the result of a multi-product cart order.
**/
public class CartOrderResultDTO {
    private List<TransactionDTO> transactions; // List of individual transactions created
    private int totalItems; // Total number of items in the order
    private double cartTotal; // Total monetary value of the order
    private int lines; // Number of distinct lines in the order

    /**
     * Default constructor for frameworks that require it.
    **/
    public CartOrderResultDTO() {}

    /**
     * Constructor to initialize all fields.
     * @param transactions List of individual transactions created.
     * @param totalItems Total number of items in the order.
     * @param cartTotal Total monetary value of the order.
     * @param lines Number of distinct lines in the order.
    **/
    public CartOrderResultDTO(List<TransactionDTO> transactions, int totalItems, double cartTotal, int lines) {
        this.transactions = transactions;
        this.totalItems = totalItems;
        this.cartTotal = cartTotal;
        this.lines = lines;
    }

    /**
     * Gets the list of individual transactions created.
     * @return The transactions.
    **/
    public List<TransactionDTO> getTransactions() { return transactions; }

    /**
     * Sets the list of individual transactions created.
     * @param transactions The transactions.
    **/
    public void setTransactions(List<TransactionDTO> transactions) { this.transactions = transactions; }
    /**
     * Gets the total number of items in the order.
     * @return The totalItems.
    **/
    public int getTotalItems() { return totalItems; }

    /**
     * Sets the total number of items in the order.
     * @param totalItems The totalItems.
    **/
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
    /**
     * Gets the total monetary value of the order.
     * @return The cartTotal.
    **/
    public double getCartTotal() { return cartTotal; }

    /**
     * Sets the total monetary value of the order.
     * @param cartTotal The cartTotal.
    **/
    public void setCartTotal(double cartTotal) { this.cartTotal = cartTotal; }
    /**
     * Gets the number of distinct lines in the order.
     * @return The lines.
    **/
    public int getLines() { return lines; }

    /**
     * Sets the number of distinct lines in the order.
     * @param lines The lines.
    **/
    public void setLines(int lines) { this.lines = lines; }
}
