package com.cloudstore.server.service.interfaces;

import com.cloudstore.server.model.entities.Product;
import com.cloudstore.server.service.exception.ServiceException;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    Optional<Product> findById(int id) throws ServiceException;

    List<Product> findByName(String name) throws ServiceException;

    List<Product> findByCategory(String category) throws ServiceException;

    List<Product> findAll() throws ServiceException;

    List<String> findAllCategories() throws ServiceException;

    Product save(Product product) throws ServiceException;

    boolean delete(int id) throws ServiceException;

    boolean updateStock(int productId, int newQuantity) throws ServiceException;

    List<Product> findLowStockProducts(int threshold) throws ServiceException;

    boolean exists(int id) throws ServiceException;

    int count() throws ServiceException;
    
}