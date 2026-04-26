package com.cloudstore.server.model.dto;

/**
 * Data Transfer Object providing a summary of a sales order for seller views.
**/
public class SalesOrderSummaryDTO {
    private long id; // The unique identifier for the transaction
    private String customerName; // The name of the customer
    private String product; // The name of the product
    private int totalItems; // Total items purchased
    private double totalCost; // Total cost of the transaction
    private String paymentMethod; // Method of payment used
    private String city; // City of the transaction

    /**
     * Default constructor for frameworks that require it.
    **/
    public SalesOrderSummaryDTO() {}

    /**
     * Constructor to initialize all fields.
     * @param id The unique identifier for the transaction.
     * @param customerName The name of the customer.
     * @param product The name of the product.
     * @param totalItems Total items purchased.
     * @param totalCost Total cost of the transaction.
     * @param paymentMethod Method of payment used.
     * @param city City of the transaction.
    **/
    public SalesOrderSummaryDTO(long id, String customerName, String product, int totalItems, double totalCost, String paymentMethod, String city) {
        this.id = id;
        this.customerName = customerName;
        this.product = product;
        this.totalItems = totalItems;
        this.totalCost = totalCost;
        this.paymentMethod = paymentMethod;
        this.city = city;
    }

    /**
     * Gets the the unique identifier for the transaction.
     * @return The id.
    **/
    public long getId() { return id; }

    /**
     * Sets the the unique identifier for the transaction.
     * @param id The id.
    **/
    public void setId(long id) { this.id = id; }
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
     * Gets the the name of the product.
     * @return The product.
    **/
    public String getProduct() { return product; }

    /**
     * Sets the the name of the product.
     * @param product The product.
    **/
    public void setProduct(String product) { this.product = product; }
    /**
     * Gets the total items purchased.
     * @return The totalItems.
    **/
    public int getTotalItems() { return totalItems; }

    /**
     * Sets the total items purchased.
     * @param totalItems The totalItems.
    **/
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
    /**
     * Gets the total cost of the transaction.
     * @return The totalCost.
    **/
    public double getTotalCost() { return totalCost; }

    /**
     * Sets the total cost of the transaction.
     * @param totalCost The totalCost.
    **/
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }
    /**
     * Gets the method of payment used.
     * @return The paymentMethod.
    **/
    public String getPaymentMethod() { return paymentMethod; }

    /**
     * Sets the method of payment used.
     * @param paymentMethod The paymentMethod.
    **/
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    /**
     * Gets the city of the transaction.
     * @return The city.
    **/
    public String getCity() { return city; }

    /**
     * Sets the city of the transaction.
     * @param city The city.
    **/
    public void setCity(String city) { this.city = city; }
}
