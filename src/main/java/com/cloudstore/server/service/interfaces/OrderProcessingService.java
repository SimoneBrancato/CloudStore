package com.cloudstore.server.service.interfaces;

import com.cloudstore.server.model.domain.OrderSubmissionResult;
import com.cloudstore.server.model.dto.TransactionDTO;
import com.cloudstore.server.service.exception.ServiceException;

import java.util.Map;

/**
 * Application service responsible for accepting and dispatching orders
 * to the asynchronous processing pipeline.
 * 
 * This abstraction decouples the Remote Facade from any specific
 * messaging infrastructure (RabbitMQ, Kafka, etc.).
 **/
public interface OrderProcessingService {

    /** * Submits a single order to the processing pipeline.
     * @param dto The TransactionDTO containing the details of the single order.
     * @return An OrderSubmissionResult detailing the status of the operation.
     * @throws ServiceException If validation or submission fails.
     **/
    OrderSubmissionResult submitSingleOrder(TransactionDTO dto) throws ServiceException;

    /** * Submits a cart order to the processing pipeline.
     * @param customerName The name of the customer placing the order.
     * @param paymentMethod The chosen payment method.
     * @param city The delivery city.
     * @param items A map representing the product IDs and their respective quantities.
     * @return An OrderSubmissionResult detailing the status of the operation.
     * @throws ServiceException If validation or submission fails.
     **/
    OrderSubmissionResult submitCartOrder(String customerName, String paymentMethod,
                                          String city, Map<Integer, Integer> items) throws ServiceException;
}