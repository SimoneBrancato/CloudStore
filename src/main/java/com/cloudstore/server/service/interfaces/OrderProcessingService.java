package com.cloudstore.server.service.interfaces;

import com.cloudstore.server.model.domain.OrderSubmissionResult;
import com.cloudstore.server.model.dto.TransactionDTO;
import com.cloudstore.server.service.exception.ServiceException;

import java.util.Map;

public interface OrderProcessingService {

    OrderSubmissionResult submitSingleOrder(TransactionDTO dto) throws ServiceException;

    OrderSubmissionResult submitCartOrder(String customerName, String paymentMethod,
                                          String city, Map<Integer, Integer> items) throws ServiceException;
}