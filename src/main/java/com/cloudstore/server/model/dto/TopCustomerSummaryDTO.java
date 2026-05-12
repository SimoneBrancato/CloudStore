package com.cloudstore.server.model.dto;
import java.time.LocalDateTime;

/**
    * Data Transfer Object summarizing information about a top-performing customer.
**/
public class TopCustomerSummaryDTO {
    private String customerName; // The name of the customer
    private int orderCount; // Total number of orders placed by the customer
    private double totalSpent; // Total amount spent by the customer
    private LocalDateTime lastOrderDate; // The date and time of the customer's last order

    /**
        * Default constructor.
    **/
    public TopCustomerSummaryDTO() {}

    /**
        * Constructor to initialize all fields.
        * @param customerName The name of the customer.
        * @param orderCount Total number of orders placed by the customer.
        * @param totalSpent Total amount spent by the customer.
        * @param lastOrderDate The date and time of the customer's last order.
    **/
    public TopCustomerSummaryDTO(String customerName, int orderCount, double totalSpent, LocalDateTime lastOrderDate) {
        this.customerName = customerName;
        this.orderCount = orderCount;
        this.totalSpent = totalSpent;
        this.lastOrderDate = lastOrderDate;
    }

    /**
        * Gets the the name of the customer.
        * @return The customerName.
    **/
    public String getCustomerName() { return customerName; }

    /**
        * Sets the the name of the customer.
        * @param customerName The customerName.
    **/
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    /**
        * Gets the total number of orders placed by the customer.
        * @return The orderCount.
    **/
    public int getOrderCount() { return orderCount; }

    /**
        * Sets the total number of orders placed by the customer.
        * @param orderCount The orderCount.
    **/
    public void setOrderCount(int orderCount) { this.orderCount = orderCount; }
    /**
        * Gets the total amount spent by the customer.
        * @return The totalSpent.
    **/
    public double getTotalSpent() { return totalSpent; }

    /**
        * Sets the total amount spent by the customer.
        * @param totalSpent The totalSpent.
    **/
    public void setTotalSpent(double totalSpent) { this.totalSpent = totalSpent; }
    
    /**
        * Gets the the date and time of the customer's last order.
        * @return The lastOrderDate.
    **/
    public LocalDateTime getLastOrderDate() { return lastOrderDate; }

    /**
        * Sets the the date and time of the customer's last order.
        * @param lastOrderDate The lastOrderDate.
    **/
    public void setLastOrderDate(LocalDateTime lastOrderDate) { this.lastOrderDate = lastOrderDate; }
}
