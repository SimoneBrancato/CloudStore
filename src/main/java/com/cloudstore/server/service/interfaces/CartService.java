package com.cloudstore.server.service.interfaces;

import com.cloudstore.server.model.dto.TransactionDTO;
import com.cloudstore.server.service.exception.ServiceException;

import java.util.Map;

public interface CartService {
    Map<String, Object> getCheckoutContext(String customerName, Map<Integer, Integer> items) throws ServiceException;

    TransactionDTO processSingleOrder(TransactionDTO dto) throws ServiceException;
    
    Map<String, Object> processCartOrder(String customerName, String paymentMethod, 
                                         String city, Map<Integer, Integer> items) throws ServiceException;
}