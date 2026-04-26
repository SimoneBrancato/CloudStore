package com.cloudstore.server.model.dto;

public class ProductDTO {
    private int id; // The unique identifier for the product
    private String name; // The name of the product
    private String category; // The category of the product
    private double price; // The price of the product
    private int stock; // The available stock quantity of the product
    
    public ProductDTO() {} // Default constructor for frameworks that require it
    
    /**
        * Constructor to initialize all fields.
        * @param id The unique identifier for the product.
        * @param name The name of the product.
        * @param category The category of the product.
        * @param price The price of the product.
        * @param stock The available stock quantity of the product.
    **/
    public ProductDTO(int id, String name, String category, double price, int stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
    }
    
    /**
        * Gets the unique identifier for the product.
        * @return The product ID.
    **/
    public int getId() {
        return id;
    }
    
    /**
        * Sets the unique identifier for the product.
        * @param id The product ID.
    **/
    public void setId(int id) {
        this.id = id;
    }
    
    /**
        * Gets the name of the product.
        * @return The product name.
    **/
    public String getName() {
        return name;
    }
    
    /**
        * Sets the name of the product.
        * @param name The product name.
    **/
    public void setName(String name) {
        this.name = name;
    }
    
    /**
        * Gets the category of the product.
        * @return The product category.
    **/
    public String getCategory() {
        return category;
    }
    
    /**
        * Sets the category of the product.
        * @param category The product category.
    **/
    public void setCategory(String category) {
        this.category = category;
    }
    
    /**
        * Gets the price of the product.
        * @return The product price.
    **/
    public double getPrice() {
        return price;
    }
    
    /**
        * Sets the price of the product.
        * @param price The product price.
    **/
    public void setPrice(double price) {
        this.price = price;
    }
    
    /**
        * Gets the available stock quantity of the product.
        * @return The stock quantity.
    **/
    public int getStock() {
        return stock;
    }
    
    /**
        * Sets the available stock quantity of the product.
        * @param stock The stock quantity.
    **/
    public void setStock(int stock) {
        this.stock = stock;
    }
}
