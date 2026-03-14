package com.cloudstore.model.dto;

import java.time.LocalDateTime;

public class TransactionDTO {
    private long id;
    private LocalDateTime date;
    private String customerName;
    private String product;
    private int totalItems;
    private double totalCost;
    private String paymentMethod;
    private String city;
    private int discountApplied;
    private String customerCategory;
    private float discount;
    private ProductDTO productDetails;
    
    public TransactionDTO() {}
    
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
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public LocalDateTime getDate() {
        return date;
    }
    
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getProduct() {
        return product;
    }
    
    public void setProduct(String product) {
        this.product = product;
    }
    
    public int getTotalItems() {
        return totalItems;
    }
    
    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }
    
    public double getTotalCost() {
        return totalCost;
    }
    
    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public int getDiscountApplied() {
        return discountApplied;
    }
    
    public void setDiscountApplied(int discountApplied) {
        this.discountApplied = discountApplied;
    }
    
    public String getCustomerCategory() {
        return customerCategory;
    }
    
    public void setCustomerCategory(String customerCategory) {
        this.customerCategory = customerCategory;
    }
    
    public float getDiscount() {
        return discount;
    }
    
    public void setDiscount(float discount) {
        this.discount = discount;
    }
    
    public ProductDTO getProductDetails() {
        return productDetails;
    }
    
    public void setProductDetails(ProductDTO productDetails) {
        this.productDetails = productDetails;
    }
}
