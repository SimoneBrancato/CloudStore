package com.cloudstore.server.service.interfaces;

import com.cloudstore.server.model.domain.CartOrderResult;
import com.cloudstore.server.model.domain.CheckoutContext;
import com.cloudstore.server.model.entities.Transaction;
import com.cloudstore.server.service.exception.ServiceException;

import java.util.Map;

public interface CartService {
    CheckoutContext getCheckoutContext(String customerName, Map<Integer, Integer> items) throws ServiceException;

    Transaction processSingleOrder(Transaction transaction) throws ServiceException;
    
    CartOrderResult processCartOrder(String customerName, String paymentMethod, 
                                     String city, Map<Integer, Integer> items) throws ServiceException;
}