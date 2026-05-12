package com.cloudstore.server.model.dto;

/**
    * Data Transfer Object containing context information for the checkout process.
**/
public class CheckoutContextDTO {
    private String customerName; // The name of the customer checking out
    private String customerCategory; // The category/level of the customer
    private float discount; // The discount percentage applied
    private int discountApplied; // Flag indicating if a discount was applied (1 for true, 0 for false)
    private String discountSource; // The source or reason for the discount
    private int sampleSize; // The sample size used for discount calculation
    private int sampleWindow; // The historical window used for calculation

    /**
        * Default constructor.
    **/
    public CheckoutContextDTO() {}

    /**
        * Constructor to initialize all fields.
        * @param customerName The name of the customer checking out.
        * @param customerCategory The category/level of the customer.
        * @param discount The discount percentage applied.
        * @param discountApplied Flag indicating if a discount was applied (1 for true, 0 for false).
        * @param discountSource The source or reason for the discount.
        * @param sampleSize The sample size used for discount calculation.
        * @param sampleWindow The historical window used for calculation.
    **/
    public CheckoutContextDTO(String customerName, String customerCategory, float discount, int discountApplied, String discountSource, int sampleSize, int sampleWindow) {
        this.customerName = customerName;
        this.customerCategory = customerCategory;
        this.discount = discount;
        this.discountApplied = discountApplied;
        this.discountSource = discountSource;
        this.sampleSize = sampleSize;
        this.sampleWindow = sampleWindow;
    }

    /**
        * Gets the the name of the customer checking out.
        * @return The customerName.
    **/
    public String getCustomerName() { return customerName; }

    /**
        * Sets the the name of the customer checking out.
        * @param customerName The customerName.
    **/
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    /**
        * Gets the the category/level of the customer.
        * @return The customerCategory.
    **/
    public String getCustomerCategory() { return customerCategory; }

    /**
        * Sets the the category/level of the customer.
        * @param customerCategory The customerCategory.
    **/
    public void setCustomerCategory(String customerCategory) { this.customerCategory = customerCategory; }
    /**
        * Gets the the discount percentage applied.
        * @return The discount.
    **/
    public float getDiscount() { return discount; }

    /**
        * Sets the the discount percentage applied.
        * @param discount The discount.
    **/
    public void setDiscount(float discount) { this.discount = discount; }
    /**
        * Gets the flag indicating if a discount was applied (1 for true, 0 for false).
        * @return The discountApplied.
    **/
    public int getDiscountApplied() { return discountApplied; }

    /**
        * Sets the flag indicating if a discount was applied (1 for true, 0 for false).
        * @param discountApplied The discountApplied.
    **/
    public void setDiscountApplied(int discountApplied) { this.discountApplied = discountApplied; }
    /**
        * Gets the the source or reason for the discount.
        * @return The discountSource.
    **/
    public String getDiscountSource() { return discountSource; }

    /**
        * Sets the the source or reason for the discount.
        * @param discountSource The discountSource.
    **/
    public void setDiscountSource(String discountSource) { this.discountSource = discountSource; }
    /**
        * Gets the the sample size used for discount calculation.
        * @return The sampleSize.
    **/
    public int getSampleSize() { return sampleSize; }

    /**
        * Sets the the sample size used for discount calculation.
        * @param sampleSize The sampleSize.
    **/
    public void setSampleSize(int sampleSize) { this.sampleSize = sampleSize; }
    /**
        * Gets the the historical window used for calculation.
        * @return The sampleWindow.
    **/
    public int getSampleWindow() { return sampleWindow; }

    /**
        * Sets the the historical window used for calculation.
        * @param sampleWindow The sampleWindow.
    **/
    public void setSampleWindow(int sampleWindow) { this.sampleWindow = sampleWindow; }
}
