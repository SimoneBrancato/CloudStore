package com.cloudstore.model.entities;

public record Product (
    int id, 
    String name, 
    String category,
    double price, 
    int stock) {}