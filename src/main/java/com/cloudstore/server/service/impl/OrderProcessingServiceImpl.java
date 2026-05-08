package com.cloudstore.server.service.impl;

import com.cloudstore.server.messaging.MessagePipeline;
import com.cloudstore.server.messaging.exception.MessagingException;
import com.cloudstore.server.model.domain.OrderSubmissionResult;
import com.cloudstore.server.model.dto.TransactionDTO;
import com.cloudstore.server.service.exception.ServiceException;
import com.cloudstore.server.service.interfaces.OrderProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of OrderProcessingService that validates order requests
 * and publishes them to the asynchronous messaging pipeline.
 **/
public class OrderProcessingServiceImpl implements OrderProcessingService {

    private static final Logger log = LoggerFactory.getLogger(OrderProcessingServiceImpl.class);
    private final MessagePipeline messagePipeline;

    /** * Default constructor for OrderProcessingServiceImpl.
     * Initializes the MessagePipeline and registers a shutdown hook.
     **/
    public OrderProcessingServiceImpl() {
        try {
            this.messagePipeline = new MessagePipeline();
        } catch (ServiceException e) {
            throw new MessagingException("Failed to initialize MessagePipeline", e);
        }
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (this.messagePipeline != null) {
                    this.messagePipeline.close();
                }
            } catch (Exception e) {
                log.error("Failed to gracefully close MessagePipeline during JVM shutdown", e);
            }
        }));
    }

    /** * Constructor for OrderProcessingServiceImpl with dependency injection.
     * @param messagePipeline The MessagePipeline instance to use for messaging operations.
     **/
    public OrderProcessingServiceImpl(MessagePipeline messagePipeline) {
        this.messagePipeline = messagePipeline;
    }

    /** * Submits a single order to the asynchronous pipeline.
     * @param dto The TransactionDTO containing the details of the single order.
     * @return An OrderSubmissionResult indicating the outcome.
     * @throws ServiceException If the DTO is null or submission fails.
     **/
    @Override
    public OrderSubmissionResult submitSingleOrder(TransactionDTO dto) throws ServiceException {
        if (dto == null) {
            throw new ServiceException("Transaction data cannot be null");
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "SINGLE_ORDER");
        payload.put("dto", dto);
        
        String messageId = UUID.randomUUID().toString();
        
        messagePipeline.publishOrderMessage("order.single", messageId, payload);
        
        return new OrderSubmissionResult("accepted", "Order queued for processing");
    }

    /** * Submits a cart order to the asynchronous pipeline.
     * @param customerName The name of the customer placing the order.
     * @param paymentMethod The chosen payment method.
     * @param city The delivery city.
     * @param items A map representing the product IDs and their respective quantities.
     * @return An OrderSubmissionResult indicating the outcome.
     * @throws ServiceException If parameters are invalid or submission fails.
     **/
    @Override
    public OrderSubmissionResult submitCartOrder(String customerName, String paymentMethod,
                                                  String city, Map<Integer, Integer> items) throws ServiceException {
        if (customerName == null || customerName.isBlank()) {
            throw new ServiceException("Customer name cannot be empty");
        }
        if (items == null || items.isEmpty()) {
            throw new ServiceException("Cart is empty");
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "CART_ORDER");
        payload.put("customerName", customerName);
        payload.put("paymentMethod", paymentMethod);
        payload.put("city", city);
        payload.put("items", items);
        
        String messageId = UUID.randomUUID().toString();
        
        messagePipeline.publishOrderMessage("order.cart", messageId, payload);
        
        return new OrderSubmissionResult("accepted", "Cart order queued for processing");
    }
}