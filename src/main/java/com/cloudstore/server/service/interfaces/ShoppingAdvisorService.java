package com.cloudstore.server.service.interfaces;

import com.cloudstore.server.model.domain.CheckoutContext;
import com.cloudstore.server.model.entities.Product;
import com.cloudstore.server.model.entities.Transaction;
import com.cloudstore.server.service.exception.ServiceException;

import java.util.List;
import java.util.Map;

public interface ShoppingAdvisorService {
    Map<String, Object> getAdvice(String customerName,
                                  String prompt,
                                  Map<Integer, Integer> items,
                                  List<Product> catalog,
                                  CheckoutContext checkoutContext,
                                  List<Transaction> orderHistory) throws ServiceException;
}
