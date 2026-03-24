package service.impl;

import dao.impl.ProductDAOImpl;
import dao.impl.TransactionDAOImpl;
import dao.interfaces.ProductDAO;
import dao.interfaces.TransactionDAO;
import model.dto.ProductDTO;
import model.dto.TransactionDTO;
import model.entities.Product;
import model.entities.Transaction;
import service.exception.ServiceException;
import service.interfaces.CartService;
import service.interfaces.TransactionService;
import service.interfaces.UserService;
import service.mapper.DTOMapper;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class CartServiceImpl implements CartService {

    private final ProductDAO productDAO;
    private final TransactionDAO transactionDAO;
    private final TransactionService transactionService;
    private final UserService userService;
    private final DatabaseConnection dbConnection;

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

    @Override
    public Map<String, Object> getCheckoutContext(String customerName, Map<Integer, Integer> items) throws ServiceException {
        // Normalizza la mappa per gestire chiavi String
        Map<Integer, Integer> normalizedItems = normalizeItemsMap(items);
        
        String customerCategory = userService.resolveCustomerCategory(customerName);
        float discount = calculateDiscountForCart(normalizedItems);
        int discountSourceSize = getDiscountSourceSize(normalizedItems);

        Map<String, Object> context = new HashMap<>();
        context.put("customerName", customerName);
        context.put("customerCategory", customerCategory);
        context.put("discount", discount);
        context.put("discountApplied", discount > 0 ? 1 : 0);
        context.put("discountSource", discountSourceSize > 0 ? "recent_product_average_discount" : "no_product_history");
        context.put("sampleSize", discountSourceSize);
        context.put("sampleWindow", 5);
        return context;
    }

    @Override
    public TransactionDTO processSingleOrder(TransactionDTO dto) throws ServiceException {
        validateOrder(dto);

        if (dto.getDate() == null) {
            dto.setDate(LocalDateTime.now());
        }

        float normalizedDiscount = Math.max(0.0f, Math.min(dto.getDiscount(), 1.0f));
        dto.setDiscount(normalizedDiscount);
        dto.setDiscountApplied(normalizedDiscount > 0 ? 1 : 0);

        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int productId = dto.getProductDetails().getId();
                Product product = productDAO.findByIdForUpdate(conn, productId)
                    .orElseThrow(() -> new ServiceException("Product not found with ID: " + productId));

                if (product.stock() < dto.getTotalItems()) {
                    throw new ServiceException(String.format(
                        "Insufficient stock for '%s': available %d, requested %d",
                        product.name(), product.stock(), dto.getTotalItems()));
                }

                double grossTotal = product.price() * dto.getTotalItems();
                double netTotal = grossTotal * (1 - normalizedDiscount);
                dto.setProduct(product.name());
                dto.setTotalCost(netTotal);

                Transaction saved = transactionDAO.save(conn, DTOMapper.toEntity(dto));
                boolean stockUpdated = productDAO.updateStock(conn, productId, product.stock() - dto.getTotalItems());

                if (!stockUpdated) {
                    throw new ServiceException("Stock update failed for product ID: " + productId);
                }

                conn.commit();
                return DTOMapper.toDTO(saved);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServiceException("Error during atomic order processing", e);
        }
    }

    @Override
    public Map<String, Object> processCartOrder(String customerName, String paymentMethod,
                                                String city, Map<Integer, Integer> items) throws ServiceException {
        // Normalizza la mappa per gestire chiavi String
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
                List<TransactionDTO> createdTransactions = new ArrayList<>();
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
                    TransactionDTO dto = createTransactionDTO(customerName, paymentMethod, city,
                                                             customerCategory, discount, now,
                                                             product, quantity, lineTotal);

                    Transaction saved = transactionDAO.save(conn, DTOMapper.toEntity(dto));
                    boolean stockUpdated = productDAO.updateStock(conn, productId, product.stock() - quantity);

                    if (!stockUpdated) {
                        throw new ServiceException("Stock update failed for product ID: " + productId);
                    }

                    TransactionDTO savedDto = DTOMapper.toDTO(saved);
                    createdTransactions.add(savedDto);
                    cartTotal += savedDto.getTotalCost();
                    totalItems += savedDto.getTotalItems();
                }

                conn.commit();

                Map<String, Object> result = new HashMap<>();
                result.put("transactions", createdTransactions);
                result.put("totalItems", totalItems);
                result.put("cartTotal", cartTotal);
                result.put("lines", createdTransactions.size());
                return result;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServiceException("Error during atomic cart processing", e);
        }
    }

    /**
     * Normalizza la mappa degli items per gestire sia Integer che String come chiavi
     */
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
                // Se la chiave è una String, prova a convertirla
                try {
                    Object rawKey = entry.getKey();
                    if (rawKey instanceof String) {
                        Integer key = Integer.parseInt((String) rawKey);
                        Integer value = entry.getValue();
                        if (value != null && value > 0) {
                            normalized.put(key, value);
                        }
                    }
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid product ID: " + entry.getKey());
                }
            }
        }
        return normalized;
    }

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

            List<TransactionDTO> productTransactions = transactionService
                    .findRecentByProduct(productId, 5);

            if (productTransactions.isEmpty()) {
                continue;
            }

            double productAverage = productTransactions.stream()
                    .mapToDouble(tx -> Math.max(0.0f, Math.min(tx.getDiscount(), 1.0f)))
                    .average()
                    .orElse(0.0);

            weightedDiscountSum += productAverage * quantity;
            weightedQuantity += quantity;
        }

        return weightedQuantity > 0
                ? (float) Math.max(0.0, Math.min(weightedDiscountSum / weightedQuantity, 1.0))
                : 0.0f;
    }

    private int getDiscountSourceSize(Map<Integer, Integer> items) throws ServiceException {
        if (items == null || items.isEmpty()) {
            return 0;
        }

        int totalSize = 0;
        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            int productId = entry.getKey();
            List<TransactionDTO> transactions = transactionService
                    .findRecentByProduct(productId, 5);
            totalSize += transactions.size();
        }
        return totalSize;
    }

    private TransactionDTO createTransactionDTO(String customerName, String paymentMethod, String city,
                                               String customerCategory, float discount, LocalDateTime now,
                                               Product product, int quantity, double lineTotal) {
        TransactionDTO dto = new TransactionDTO();
        dto.setDate(now);
        dto.setCustomerName(customerName);
        dto.setProduct(product.name());
        dto.setTotalItems(quantity);
        dto.setTotalCost(lineTotal);
        dto.setPaymentMethod(paymentMethod);
        dto.setCity(city);
        dto.setDiscountApplied(discount > 0 ? 1 : 0);
        dto.setCustomerCategory(customerCategory);
        dto.setDiscount(discount);
        dto.setProductDetails(new ProductDTO(product.id(), product.name(), product.category(),
                                            product.price(), product.stock()));
        return dto;
    }

    private void validateOrder(TransactionDTO dto) throws ServiceException {
        if (dto.getProductDetails() == null) {
            throw new ServiceException("Transaction must specify a product");
        }
        if (dto.getTotalItems() <= 0) {
            throw new ServiceException("Number of items must be greater than zero");
        }
        if (dto.getCustomerName() == null || dto.getCustomerName().isBlank()) {
            throw new ServiceException("Customer name cannot be empty");
        }
    }

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