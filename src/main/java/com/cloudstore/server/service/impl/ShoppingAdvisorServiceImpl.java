package com.cloudstore.server.service.impl;

import com.cloudstore.server.model.domain.CheckoutContext;
import com.cloudstore.server.model.entities.Product;
import com.cloudstore.server.model.entities.Transaction;
import com.cloudstore.server.service.exception.ServiceException;
import com.cloudstore.server.service.interfaces.ShoppingAdvisorService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShoppingAdvisorServiceImpl implements ShoppingAdvisorService {

    private static final ObjectMapper MAPPER = new ObjectMapper(); // Can be reused for multiple requests
    private final HttpClient httpClient; // Reusable HttpClient instance
    private final String llmServiceUrl; // Base URL for the LLM service, injected via environment variable
    
    // Constructor that initializes the HttpClient and retrieves the LLM service URL
    public ShoppingAdvisorServiceImpl() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4))
                .build();

        this.llmServiceUrl = System.getenv("LLM_SERVICE_URL");
        if (this.llmServiceUrl == null || this.llmServiceUrl.isBlank()) {
            throw new IllegalArgumentException("CRITICAL: LLM_SERVICE_URL environment variable is not set.");
        }
    }

    /**
         * Retrieves shopping advice for a customer based on their cart items, a prompt, and their order history.
         * @param customerName The name of the customer for whom to retrieve advice.
         * @param prompt An optional prompt providing context for the advice request.
         * @param items A map of product IDs to quantities representing the customer's current cart items.
         * @param catalog A list of all products available in the store, used to enrich the advice with product details.
         * @param checkoutContext The current checkout context, including customer category and discount information.
         * @param orderHistory A list of the customer's past transactions, used to provide personalized advice based on purchase history.
         * @return A map containing the advice returned by the LLM service, which may include product recommendations, discounts, or other shopping tips.
         * @throws ServiceException If an error occurs while communicating with the LLM service or processing the response.
    **/
    @Override
    public Map<String, Object> getAdvice(String customerName,
                                         String prompt,
                                         Map<Integer, Integer> items,
                                         List<Product> catalog,
                                         CheckoutContext checkoutContext,
                                         List<Transaction> orderHistory) throws ServiceException {
        try {
            Map<String, Object> payload = buildPayload(customerName, prompt, items, catalog, checkoutContext, orderHistory);
            String requestBody = MAPPER.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(llmServiceUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ServiceException("LLM service error: HTTP " + response.statusCode());
            }

            return MAPPER.readValue(response.body(), new TypeReference<>() {});
        } catch (IOException e) {
            throw new ServiceException("Invalid response from LLM service", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException("LLM request interrupted", e);
        }
    }

    /**
         * Builds the payload for the LLM request based on the provided parameters.
         * @param customerName The name of the customer for whom to retrieve advice.
         * @param prompt An optional prompt providing context for the advice request.
         * @param items A map of product IDs to quantities representing the customer's current cart items.
         * @param catalog A list of all products available in the store, used to enrich the advice with product details.
         * @param checkoutContext The current checkout context, including customer category and discount information.
         * @param orderHistory A list of the customer's past transactions, used to provide personalized advice based on purchase history.
         * @return A map containing the payload for the LLM request.
    **/
    private Map<String, Object> buildPayload(String customerName,
                                             String prompt,
                                             Map<Integer, Integer> items,
                                             List<Product> catalog,
                                             CheckoutContext checkoutContext,
                                             List<Transaction> orderHistory) {
        Map<Integer, Product> byId = new HashMap<>();
        for (Product product : catalog) {
            byId.put(product.id(), product);
        }

        List<Map<String, Object>> cartItems = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            Product p = byId.get(entry.getKey());
            if (p == null) {
                continue;
            }
            Map<String, Object> row = new HashMap<>();
            row.put("id", p.id());
            row.put("name", p.name());
            row.put("category", p.category());
            row.put("price", p.price());
            row.put("stock", p.stock());
            row.put("quantity", entry.getValue());
            cartItems.add(row);
        }

        List<Map<String, Object>> catalogItems = new ArrayList<>();
        for (Product p : catalog) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", p.id());
            row.put("name", p.name());
            row.put("category", p.category());
            row.put("price", p.price());
            row.put("stock", p.stock());
            catalogItems.add(row);
        }

        List<Map<String, Object>> historyItems = new ArrayList<>();
        List<Transaction> safeHistory = orderHistory != null ? orderHistory : List.of();
        List<Transaction> limitedHistory = safeHistory.stream()
                .sorted((a, b) -> {
                    LocalDateTime ad = a.date();
                    LocalDateTime bd = b.date();
                    if (ad == null && bd == null) {
                        return 0;
                    }
                    if (ad == null) {
                        return 1;
                    }
                    if (bd == null) {
                        return -1;
                    }
                    return bd.compareTo(ad);
                })
                .limit(10)
                .collect(Collectors.toList());

        for (Transaction tx : limitedHistory) {
            Map<String, Object> row = new HashMap<>();
            Product product = tx.ProductID();
            if (product != null) {
                row.put("product_id", product.id());
                row.put("name", product.name());
                row.put("category", product.category());
                row.put("price", product.price());
            } else {
                row.put("name", tx.Product());
            }
            row.put("quantity", tx.TotalItems());
            row.put("total_cost", tx.TotalCost());
            row.put("date", tx.date() != null ? tx.date().toString() : null);
            historyItems.add(row);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("customer_name", customerName);
        payload.put("customer_category", checkoutContext.customerCategory());
        payload.put("discount", checkoutContext.discount());
        payload.put("prompt", prompt != null ? prompt : "");
        payload.put("cart_items", cartItems);
        payload.put("catalog", catalogItems);
        payload.put("order_history", historyItems);

        return payload;
    }
}
