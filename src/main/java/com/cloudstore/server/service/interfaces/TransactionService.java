package com.cloudstore.server.service.interfaces;

import com.cloudstore.server.model.entities.Transaction;
import com.cloudstore.server.model.domain.TopCustomerSummary;
import com.cloudstore.server.service.exception.ServiceException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionService {

    Optional<Transaction> findById(long id) throws ServiceException;
    
    List<Transaction> findByCustomer(String customerName) throws ServiceException;
    
    List<Transaction> findByProduct(int productId) throws ServiceException;
    
    List<Transaction> findRecentByProduct(int productId, int limit) throws ServiceException;
    
    List<Transaction> findByDateRange(LocalDateTime start, LocalDateTime end) throws ServiceException;
    
    List<Transaction> findByPaymentMethod(String paymentMethod) throws ServiceException;
    
    List<Transaction> findByCity(String city) throws ServiceException;
    
    List<Transaction> findAll() throws ServiceException;
    
    Transaction save(Transaction transaction) throws ServiceException;
    
    boolean delete(long id) throws ServiceException;
    
    double calculateTotalSales(LocalDateTime start, LocalDateTime end) throws ServiceException;
    
    int countByDateRange(LocalDateTime start, LocalDateTime end) throws ServiceException;
    
    List<Transaction> findRecentTransactions(int limit) throws ServiceException;
    
    boolean exists(long id) throws ServiceException;
    
    int count() throws ServiceException;

    int countDistinctProductsSold() throws ServiceException;

    List<TopCustomerSummary> findTopCustomers(int limit) throws ServiceException;
}