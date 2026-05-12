package com.cloudstore.server.service.impl;

import com.cloudstore.server.dao.impl.ProductDAOImpl;
import com.cloudstore.server.dao.impl.TransactionDAOImpl;
import com.cloudstore.server.dao.interfaces.ProductDAO;
import com.cloudstore.server.dao.interfaces.TransactionDAO;
import com.cloudstore.server.model.domain.CartOrderResult;
import com.cloudstore.server.model.domain.CheckoutContext;
import com.cloudstore.server.model.entities.Product;
import com.cloudstore.server.model.entities.Transaction;
import com.cloudstore.server.service.exception.ServiceException;
import com.cloudstore.server.service.interfaces.CartService;
import com.cloudstore.server.service.interfaces.TransactionService;
import com.cloudstore.server.service.interfaces.UserService;
import com.cloudstore.server.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class CartServiceImpl implements CartService {

    // DAOs and services used for cart operations
    private final ProductDAO productDAO;

    // TransactionDAO for handling transaction persistence
    private final TransactionDAO transactionDAO;
    
    // Service for retrieving transaction history and calculating discounts
    private final TransactionService transactionService;

    // Service for retrieving user information and categories
    private final UserService userService;

    // Database connection instance for managing transactions and ensuring atomicity
    private final DatabaseConnection dbConnection;

    /** 
        * Constructor for CartServiceImpl.
        * Initializes DAOs and services, and sets up the database connection.
        * @throws ServiceException If initialization fails due to database connection issues or other problems.
    **/
    public CartServiceImpl() throws ServiceException {
        try {
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            this.productDAO = new ProductDAOImpl();
            this.transactionDAO = new TransactionDAOImpl();
            this.transactionService = new TransactionServiceImpl();
            this.userService = new UserServiceImpl();
            this.dbConnection = dbConnection;
        } catch (SQLException e) {
            throw new ServiceException("Unable to initialize CartService", e);
        }
    }

    /** 
        * Retrieves the checkout context for a customer based on their cart items.
        * @param customerName The name of the customer.
        * @param items A map of product IDs to quantities.
        * @return A map containing the checkout context.
        * @throws ServiceException If an error occurs while retrieving the checkout context.
    **/
    @Override
    public CheckoutContext getCheckoutContext(String customerName, Map<Integer, Integer> items) throws ServiceException {
        Map<Integer, Integer> normalizedItems = normalizeItemsMap(items);
        
        String customerCategory = userService.resolveCustomerCategory(customerName);
        float discount = calculateDiscountForCart(normalizedItems);
        int discountSourceSize = getDiscountSourceSize(normalizedItems);

        return new CheckoutContext(
            customerName,
            customerCategory,
            discount,
            discount > 0 ? 1 : 0,
            discountSourceSize > 0 ? "recent_product_average_discount" : "no_product_history",
            discountSourceSize,
            5
        );
    }

    /** 
        * Processes a single order, ensuring atomicity and consistency.
        * @param dto The transaction data transfer object.
        * @return The processed transaction DTO.
        * @throws ServiceException If an error occurs while processing the order.
    **/
    @Override
    public Transaction processSingleOrder(Transaction transaction) throws ServiceException {
        validateOrder(transaction);

        LocalDateTime orderDate = transaction.date() == null ? LocalDateTime.now() : transaction.date();
        float normalizedDiscount = Math.max(0.0f, Math.min(transaction.Discount(), 1.0f));
        int discountApplied = normalizedDiscount > 0 ? 1 : 0;

        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int productId = transaction.ProductID() != null ? transaction.ProductID().id() : -1;
                if (productId == -1) throw new ServiceException("Product not specified");

                Product product = productDAO.findByIdForUpdate(conn, productId)
                    .orElseThrow(() -> new ServiceException("Product not found with ID: " + productId));

                if (product.stock() < transaction.TotalItems()) {
                    throw new ServiceException(String.format(
                        "Insufficient stock for '%s': available %d, requested %d",
                        product.name(), product.stock(), transaction.TotalItems()));
                }

                double grossTotal = product.price() * transaction.TotalItems();
                double netTotal = grossTotal * (1 - normalizedDiscount);
                
                Transaction toSave = new Transaction(
                    transaction.id(), orderDate, transaction.CustomerName(), product.name(),
                    transaction.TotalItems(), netTotal, transaction.PaymentMethod(), transaction.City(),
                    discountApplied, transaction.CustomerCategory(), normalizedDiscount, product
                );

                Transaction saved = transactionDAO.save(conn, toSave);
                boolean stockUpdated = productDAO.updateStock(conn, productId, product.stock() - transaction.TotalItems());

                if (!stockUpdated) {
                    throw new ServiceException("Stock update failed for product ID: " + productId);
                }

                conn.commit();
                return saved;
            } catch (Exception e) {
                try {
                    if (!conn.isClosed()) {
                        conn.rollback();
                    }
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServiceException("Error during atomic order processing", e);
        }
    }

    /** 
        * Processes a cart order, ensuring atomicity and consistency across multiple items.
        * @param customerName The name of the customer placing the order.
        * @param paymentMethod The payment method used for the order.
        * @param city The city where the order is being placed.
        * @param items A map of product IDs to quantities representing the cart contents.
        * @return A map containing the results of processing the cart order, including transactions, totals, and discounts.
        * @throws ServiceException If an error occurs while processing the cart order.
    **/
    @Override
    public CartOrderResult processCartOrder(String customerName, String paymentMethod,
                                            String city, Map<Integer, Integer> items) throws ServiceException {
        Map<Integer, Integer> normalizedItems = normalizeItemsMap(items);
        
        validateCartOrder(customerName, normalizedItems);

        String customerCategory = userService.resolveCustomerCategory(customerName);
        float discount = calculateDiscountForCart(normalizedItems);
        LocalDateTime now = LocalDateTime.now();

        List<Map.Entry<Integer, Integer>> sortedItems = new ArrayList<>(normalizedItems.entrySet());
        sortedItems.sort(Comparator.comparingInt(Map.Entry::getKey));

        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                List<Transaction> createdTransactions = new ArrayList<>();
                double cartTotal = 0.0;
                int totalItems = 0;

                for (Map.Entry<Integer, Integer> entry : sortedItems) {
                    int productId = entry.getKey();
                    int quantity = entry.getValue();

                    Product product = productDAO.findByIdForUpdate(conn, productId)
                            .orElseThrow(() -> new ServiceException("Product not found with ID: " + productId));

                    if (product.stock() < quantity) {
                        throw new ServiceException(String.format(
                                "Insufficient stock for '%s': available %d, requested %d",
                                product.name(), product.stock(), quantity));
                    }

                    double lineTotal = product.price() * quantity * (1 - discount);
                    Transaction tx = createTransaction(customerName, paymentMethod, city,
                                                             customerCategory, discount, now,
                                                             product, quantity, lineTotal);

                    Transaction saved = transactionDAO.save(conn, tx);
                    boolean stockUpdated = productDAO.updateStock(conn, productId, product.stock() - quantity);

                    if (!stockUpdated) {
                        throw new ServiceException("Stock update failed for product ID: " + productId);
                    }

                    createdTransactions.add(saved);
                    cartTotal += saved.TotalCost();
                    totalItems += saved.TotalItems();
                }

                conn.commit();

                return new CartOrderResult(createdTransactions, totalItems, cartTotal, createdTransactions.size());
            } catch (Exception e) {
                try {
                    if (!conn.isClosed()) {
                        conn.rollback();
                    }
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServiceException("Error during atomic cart processing", e);
        }
    }

    /** 
        * Normalizes the items map by ensuring all keys and values are valid integers and filtering out invalid entries.
        * @param items The original map of product IDs to quantities.
        * @return A normalized map containing only valid product IDs and positive quantities.
    **/
    private Map<Integer, Integer> normalizeItemsMap(Map<Integer, Integer> items) {
        Map<Integer, Integer> normalized = new HashMap<>();
        if (items == null) {
            return normalized;
        }
        
        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            try {
                Integer key = entry.getKey();
                Integer value = entry.getValue();
                if (key != null && value != null && value > 0) {
                    normalized.put(key, value);
                }
            } catch (ClassCastException e) {
                try {
                    Object rawKey = entry.getKey();
                    if (rawKey instanceof String) {
                        Integer key = Integer.parseInt((String) rawKey);
                        Integer value = entry.getValue();
                        if (value != null && value > 0) {
                            normalized.put(key, value);
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return normalized;
    }

    /** 
        * Calculates the average discount for the cart based on recent transactions of the products in the cart.
        * @param items A map of product IDs to quantities representing the cart contents.
        * @return The calculated average discount for the cart, normalized between 0 and 1.
        * @throws ServiceException If an error occurs while retrieving transaction history or calculating discounts.
    **/
    private float calculateDiscountForCart(Map<Integer, Integer> items) throws ServiceException {
        if (items == null || items.isEmpty()) {
            return 0.0f;
        }

        double weightedDiscountSum = 0.0;
        int weightedQuantity = 0;

        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            int productId = entry.getKey();
            int quantity = Math.max(0, entry.getValue());

            if (quantity <= 0) {
                continue;
            }

            List<Transaction> productTransactions = transactionService
                    .findRecentByProduct(productId, 5);

            if (productTransactions.isEmpty()) {
                continue;
            }

            double productAverage = productTransactions.stream()
                    .mapToDouble(tx -> Math.max(0.0f, Math.min(tx.Discount(), 1.0f)))
                    .average()
                    .orElse(0.0);

            weightedDiscountSum += productAverage * quantity;
            weightedQuantity += quantity;
        }

        return weightedQuantity > 0
                ? (float) Math.max(0.0, Math.min(weightedDiscountSum / weightedQuantity, 1.0))
                : 0.0f;
    }

    /** 
        * Retrieves the size of the discount source for the cart based on recent transactions of the products in the cart.
        * @param items A map of product IDs to quantities representing the cart contents.
        * @return The total number of recent transactions across all products in the cart, which serves as the sample size for discount calculation.
        * @throws ServiceException If an error occurs while retrieving transaction history.
    **/
    private int getDiscountSourceSize(Map<Integer, Integer> items) throws ServiceException {
        if (items == null || items.isEmpty()) {
            return 0;
        }

        int totalSize = 0;
        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            int productId = entry.getKey();
            List<Transaction> transactions = transactionService
                    .findRecentByProduct(productId, 5);
            totalSize += transactions.size();
        }
        return totalSize;
    }

    /** 
        * Creates a TransactionDTO instance based on the provided parameters.
        * @param customerName The name of the customer.
        * @param paymentMethod The payment method used.
        * @param city The city of the customer.
        * @param customerCategory The category of the customer.
        * @param discount The discount applied.
        * @param now The current date and time.
        * @param product The product for which the transaction is being created.
        * @param quantity The quantity of the product.
        * @param lineTotal The total cost for the line item.
        * @return A new TransactionDTO instance initialized with the provided values.
    **/
    private Transaction createTransaction(String customerName, String paymentMethod, String city,
                                               String customerCategory, float discount, LocalDateTime now,
                                               Product product, int quantity, double lineTotal) {
        return new Transaction(
            0, now, customerName, product.name(), quantity, lineTotal, paymentMethod, city,
            discount > 0 ? 1 : 0, customerCategory, discount, product
        );
    }
    
    /** 
        * Validates the transaction data transfer object for a single order.
        * @param dto The TransactionDTO to validate.
        * @throws ServiceException If the DTO is invalid, such as missing product details, non-positive item quantity, or empty customer name.
    **/
    private void validateOrder(Transaction tx) throws ServiceException {
        if (tx.ProductID() == null) {
            throw new ServiceException("Transaction must specify a product");
        }
        if (tx.TotalItems() <= 0) {
            throw new ServiceException("Number of items must be greater than zero");
        }
        if (tx.CustomerName() == null || tx.CustomerName().isBlank()) {
            throw new ServiceException("Customer name cannot be empty");
        }
    }

    /** 
        * Validates the cart order details before processing.
        * @param customerName The name of the customer placing the order.
        * @param items A map of product IDs to quantities representing the cart contents.
        * @throws ServiceException If the customer name is empty, the cart is empty, or any item has a non-positive quantity.
    **/
    private void validateCartOrder(String customerName, Map<Integer, Integer> items) throws ServiceException {
        if (customerName == null || customerName.isBlank()) {
            throw new ServiceException("Customer name cannot be empty");
        }
        if (items == null || items.isEmpty()) {
            throw new ServiceException("Cart is empty");
        }
        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            if (entry.getValue() <= 0) {
                throw new ServiceException("Quantity must be greater than zero for product ID: " + entry.getKey());
            }
        }
    }
}