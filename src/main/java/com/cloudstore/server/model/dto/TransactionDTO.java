package model.dto;

import java.time.LocalDateTime;

public class TransactionDTO {
    private long id;    // The unique identifier for the transaction
    private LocalDateTime date; // The date and time when the transaction occurred
    private String customerName; // The name of the customer who made the transaction
    private String product; // The name of the product involved in the transaction
    private int totalItems; // The total number of items purchased in the transaction
    private double totalCost; // The total cost of the transaction
    private String paymentMethod; // The method of payment used for the transaction (e.g., credit card, cash, etc.)
    private String city; // The city where the transaction took place
    private int discountApplied; // The percentage of discount applied to the transaction (e.g., 10 for 10% discount)
    private String customerCategory; // The category of the customer (e.g., regular, premium, etc.)
    private float discount; // The actual discount amount applied to the transaction
    private ProductDTO productDetails; // Detailed information about the product involved in the transaction
     
    public TransactionDTO() {} // Default constructor for frameworks that require it
    
    /**
        * Constructor to initialize all fields.
        * @param id The unique identifier for the transaction.
        * @param date The date and time when the transaction occurred.
        * @param customerName The name of the customer who made the transaction.
        * @param product The name of the product involved in the transaction.
        * @param totalItems The total number of items purchased in the transaction.
        * @param totalCost The total cost of the transaction.
        * @param paymentMethod The method of payment used for the transaction.
        * @param city The city where the transaction took place.
        * @param discountApplied The percentage of discount applied to the transaction.
        * @param customerCategory The category of the customer.
        * @param discount The actual discount amount applied to the transaction.
        * @param productDetails Detailed information about the product involved in the transaction.
    **/
    public TransactionDTO(long id, LocalDateTime date, String customerName, String product,
                         int totalItems, double totalCost, String paymentMethod, String city,
                         int discountApplied, String customerCategory, float discount, 
                         ProductDTO productDetails) {
        this.id = id;
        this.date = date;
        this.customerName = customerName;
        this.product = product;
        this.totalItems = totalItems;
        this.totalCost = totalCost;
        this.paymentMethod = paymentMethod;
        this.city = city;
        this.discountApplied = discountApplied;
        this.customerCategory = customerCategory;
        this.discount = discount;
        this.productDetails = productDetails;
    }
    
    /**
        * Gets the unique identifier for the transaction.
        * @return The transaction ID.
    **/
    public long getId() {
        return id;
    }
    
    /**
        * Sets the unique identifier for the transaction.
        * @param id The transaction ID.
    **/
    public void setId(long id) {
        this.id = id;
    }
    
    /**
        * Gets the date and time when the transaction occurred.
        * @return The transaction date.
    **/
    public LocalDateTime getDate() {
        return date;
    }
    
    /**
        * Sets the date and time when the transaction occurred.
        * @param date The transaction date.
    **/
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    
    /**
        * Gets the name of the customer who made the transaction.
        * @return The customer name.
    **/

    public String getCustomerName() {
        return customerName;
    }
    
    /**
        * Sets the name of the customer who made the transaction.
        * @param customerName The customer name.
    **/
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    /**
        * Gets the name of the product involved in the transaction.
        * @return The product name.
    **/
    public String getProduct() {
        return product;
    }
    
    /**
        * Sets the name of the product involved in the transaction.
        * @param product The product name.
    **/
    public void setProduct(String product) {
        this.product = product;
    }
    
    /**
        * Gets the total number of items purchased in the transaction.
        * @return The total number of items.
    **/
    public int getTotalItems() {
        return totalItems;
    }
    
    /**
        * Sets the total number of items purchased in the transaction.
        * @param totalItems The total number of items.
    **/
    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }
    
    /**
        * Gets the total cost of the transaction.
        * @return The total cost.
    **/
    public double getTotalCost() {
        return totalCost;
    }

    /**
        * Sets the total cost of the transaction.
        * @param totalCost The total cost.
    **/
    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }
    
    /**
        * Gets the method of payment used for the transaction.
        * @return The payment method.
    **/
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    /**
        * Sets the method of payment used for the transaction.
        * @param paymentMethod The payment method.
    **/
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    /**
        * Gets the city where the transaction took place.
        * @return The city.
    **/
    public String getCity() {
        return city;
    }
    
    /**
        * Sets the city where the transaction took place.
        * @param city The city.
    **/
    public void setCity(String city) {
        this.city = city;
    }
    
    /**
        * Gets the percentage of discount applied to the transaction.
        * @return The discount percentage.
    **/
    public int getDiscountApplied() {
        return discountApplied;
    }
    
    /**
        * Sets the percentage of discount applied to the transaction.
        * @param discountApplied The discount percentage.
    **/
    public void setDiscountApplied(int discountApplied) {
        this.discountApplied = discountApplied;
    }
    
    /**
        * Gets the category of the customer.
        * @return The customer category.
    **/
    public String getCustomerCategory() {
        return customerCategory;
    }
    
    /**
        * Sets the category of the customer.
        * @param customerCategory The customer category.
    **/
    public void setCustomerCategory(String customerCategory) {
        this.customerCategory = customerCategory;
    }
    
    /**
        * Gets the actual discount amount applied to the transaction.
        * @return The discount amount.
    **/
    public float getDiscount() {
        return discount;
    }
    
    /**
        * Sets the actual discount amount applied to the transaction.
        * @param discount The discount amount.
    **/
    public void setDiscount(float discount) {
        this.discount = discount;
    }
    
    /**
        * Gets detailed information about the product involved in the transaction.
        * @return The product details.
    **/
    public ProductDTO getProductDetails() {
        return productDetails;
    }
    
    /**
        * Sets detailed information about the product involved in the transaction.
        * @param productDetails The product details.
    **/
    public void setProductDetails(ProductDTO productDetails) {
        this.productDetails = productDetails;
    }
}
