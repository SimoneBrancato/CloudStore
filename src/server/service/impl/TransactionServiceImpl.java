package service.impl;

import dao.impl.TransactionDAOImpl;
import dao.interfaces.TransactionDAO;
import model.dto.TransactionDTO;
import model.entities.Transaction;
import service.exception.ServiceException;
import service.interfaces.TransactionService;
import service.mapper.DTOMapper;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TransactionServiceImpl implements TransactionService {

    // Dependency on TransactionDAO to perform database operations related to transactions
    private final TransactionDAO transactionDAO;

    /** 
        * Default constructor for TransactionServiceImpl that initializes the TransactionDAO with a default implementation.
        * @throws ServiceException If initialization fails due to database connection issues or other problems.
    **/
    public TransactionServiceImpl() throws ServiceException {
        try {
            this.transactionDAO = new TransactionDAOImpl();
        } catch (SQLException e) {
            throw new ServiceException("Unable to initialize TransactionService", e);
        }
    }

    /** 
        * Constructor for TransactionServiceImpl that accepts a TransactionDAO instance.
        * @param transactionDAO The TransactionDAO instance to use for database operations.
    **/
    public TransactionServiceImpl(TransactionDAO transactionDAO) {
        this.transactionDAO = transactionDAO;
    }

    /** 
        * Retrieves a transaction by its unique identifier.
        * @param id The unique identifier of the transaction to retrieve.
        * @return An Optional containing the TransactionDTO if found, or empty if not found.
        * @throws ServiceException If an error occurs while retrieving the transaction from the database.
    **/
    @Override
    public Optional<TransactionDTO> findById(long id) throws ServiceException {
        try {
            return transactionDAO.findById(id).map(DTOMapper::toDTO);
        } catch (SQLException e) {
            throw new ServiceException("Error retrieving transaction ID: " + id, e);
        }
    }

    /** 
        * Retrieves transactions by the customer's name.
        * @param customerName The name of the customer whose transactions to retrieve.
        * @return A list of TransactionDTOs representing the found transactions.
        * @throws ServiceException If an error occurs while searching for transactions by customer name.
    **/
    @Override
    public List<TransactionDTO> findByCustomer(String customerName) throws ServiceException {
        try {
            return transactionDAO.findByCustomer(customerName).stream()
                    .map(DTOMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new ServiceException("Error searching transactions by customer: " + customerName, e);
        }
    }

    /** 
        * Retrieves transactions by the total cost.
        * @param totalCost The total cost of the transactions to retrieve.
        * @return A list of TransactionDTOs representing the found transactions.
        * @throws ServiceException If an error occurs while searching for transactions by total cost.
    **/
    @Override
    public List<TransactionDTO> findByProduct(int productId) throws ServiceException {
        try {
            return transactionDAO.findByProduct(productId).stream()
                    .map(DTOMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new ServiceException("Error searching transactions by product ID: " + productId, e);
        }
    }

    /** 
        * Retrieves transactions by the total cost.
        * @param totalCost The total cost of the transactions to retrieve.
        * @return A list of TransactionDTOs representing the found transactions.
        * @throws ServiceException If an error occurs while searching for transactions by total cost.
    **/
    @Override
    public List<TransactionDTO> findRecentByProduct(int productId, int limit) throws ServiceException {
        try {
            return transactionDAO.findRecentByProduct(productId, limit).stream()
                    .map(DTOMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new ServiceException("Error searching recent transactions for product ID: " + productId, e);
        }
    }

    /** 
        * Retrieves transactions by a date range.
        * @param start The start date and time of the range.
        * @param end The end date and time of the range.
        * @return A list of TransactionDTOs representing the found transactions.
        * @throws ServiceException If an error occurs while searching for transactions by date range, or if the date range is invalid.
    **/
    @Override
    public List<TransactionDTO> findByDateRange(LocalDateTime start, LocalDateTime end) throws ServiceException {
        if (start.isAfter(end)) {
            throw new ServiceException("Start date cannot be after end date");
        }
        try {
            return transactionDAO.findByDateRange(start, end).stream()
                    .map(DTOMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new ServiceException("Error searching transactions by date range", e);
        }
    }

    /** 
        * Retrieves transactions by their payment method.
        * @param paymentMethod The payment method of the transactions to retrieve.
        * @return A list of TransactionDTOs representing the found transactions.
        * @throws ServiceException If an error occurs while searching for transactions by payment method.
    **/
    @Override
    public List<TransactionDTO> findByPaymentMethod(String paymentMethod) throws ServiceException {
        try {
            return transactionDAO.findByPaymentMethod(paymentMethod).stream()
                    .map(DTOMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new ServiceException("Error searching transactions by payment method", e);
        }
    }

    /** 
        * Retrieves transactions by their city.
        * @param city The city of the transactions to retrieve.
        * @return A list of TransactionDTOs representing the found transactions.
        * @throws ServiceException If an error occurs while searching for transactions by city.
    **/
    @Override
    public List<TransactionDTO> findByCity(String city) throws ServiceException {
        try {
            return transactionDAO.findByCity(city).stream()
                    .map(DTOMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new ServiceException("Error searching transactions by city: " + city, e);
        }
    }

    /** 
        * Retrieves all transactions from the database.
        * @return A list of TransactionDTOs representing all transactions.
        * @throws ServiceException If an error occurs while retrieving transactions from the database.
    **/
    @Override
    public List<TransactionDTO> findAll() throws ServiceException {
        try {
            return transactionDAO.findAll().stream()
                    .map(DTOMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new ServiceException("Error retrieving transactions", e);
        }
    }

    /** 
        * Saves a transaction to the database after validating the input data.
        * @param dto The TransactionDTO containing the transaction data to save.
        * @return The saved TransactionDTO with any generated fields populated (e.g., ID).
        * @throws ServiceException If validation fails or if an error occurs while saving the transaction to the database.
    **/
    @Override
    public TransactionDTO save(TransactionDTO dto) throws ServiceException {
        validate(dto);
        try {
            Transaction saved = transactionDAO.save(DTOMapper.toEntity(dto));
            return DTOMapper.toDTO(saved);
        } catch (SQLException e) {
            throw new ServiceException("Error saving transaction", e);
        }
    }

    /** 
        * Deletes a transaction by its unique identifier.
        * @param id The unique identifier of the transaction to delete.
        * @return true if the transaction was successfully deleted, false otherwise.
        * @throws ServiceException If an error occurs while deleting the transaction from the database.
    **/
    @Override
    public boolean delete(long id) throws ServiceException {
        try {
            return transactionDAO.delete(id);
        } catch (SQLException e) {
            throw new ServiceException("Error deleting transaction ID: " + id, e);
        }
    }

    /** 
        * Calculates the total sales amount for transactions within a specified date range.
        * @param start The start date and time of the range.
        * @param end The end date and time of the range.
        * @return The total sales amount for transactions within the specified date range.
        * @throws ServiceException If an error occurs while calculating total sales, or if the date range is invalid.
    **/
    @Override
    public double calculateTotalSales(LocalDateTime start, LocalDateTime end) throws ServiceException {
        if (start.isAfter(end)) {
            throw new ServiceException("Start date cannot be after end date");
        }
        try {
            return transactionDAO.calculateTotalSales(start, end);
        } catch (SQLException e) {
            throw new ServiceException("Error calculating total sales", e);
        }
    }

    /** 
        * Counts the number of transactions that occurred within a specified date range.
        * @param start The start date and time of the range.
        * @param end The end date and time of the range.
        * @return The count of transactions that occurred within the specified date range.
        * @throws ServiceException If an error occurs while counting transactions, or if the date range is invalid.
    **/
    @Override
    public int countByDateRange(LocalDateTime start, LocalDateTime end) throws ServiceException {
        if (start.isAfter(end)) {
            throw new ServiceException("Start date cannot be after end date");
        }
        try {
            return transactionDAO.countByDateRange(start, end);
        } catch (SQLException e) {
            throw new ServiceException("Error counting transactions by date range", e);
        }
    }

    /** 
        * Retrieves a list of recent transactions, limited by the specified number.
        * @param limit The maximum number of recent transactions to retrieve.
        * @return A list of TransactionDTOs representing the recent transactions.
        * @throws ServiceException If an error occurs while retrieving recent transactions, or if the limit is invalid.
    **/
    @Override
    public List<TransactionDTO> findRecentTransactions(int limit) throws ServiceException {
        if (limit <= 0) {
            throw new ServiceException("Limit must be a positive number");
        }
        try {
            return transactionDAO.findRecentTransactions(limit).stream()
                    .map(DTOMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new ServiceException("Error retrieving recent transactions", e);
        }
    }

    /** 
        * Checks if a transaction with the specified ID exists in the database.
        * @param id The unique identifier of the transaction to check.
        * @return true if the transaction exists, false otherwise.
        * @throws ServiceException If an error occurs while checking transaction existence in the database, such as SQL exceptions.
    **/
    @Override
    public boolean exists(long id) throws ServiceException {
        try {
            return transactionDAO.exists(id);
        } catch (SQLException e) {
            throw new ServiceException("Error checking transaction existence", e);
        }
    }

    /** 
        * Counts the total number of transactions in the database.
        * @return The total count of transactions.
        * @throws ServiceException If an error occurs while counting transactions in the database, such as SQL exceptions.
    **/
    @Override
    public int count() throws ServiceException {
        try {
            return transactionDAO.count();
        } catch (SQLException e) {
            throw new ServiceException("Error counting transactions", e);
        }
    }

    /** 
        * Validates the fields of a TransactionDTO before saving it to the database.
        * @param dto The TransactionDTO to validate.
        * @throws ServiceException If any validation rules are violated, such as missing required fields or invalid values.
    **/
    private void validate(TransactionDTO dto) throws ServiceException {
        if (dto.getCustomerName() == null || dto.getCustomerName().isBlank()) {
            throw new ServiceException("Customer name cannot be empty");
        }
        if (dto.getTotalItems() <= 0) {
            throw new ServiceException("Number of items must be greater than zero");
        }
        if (dto.getTotalCost() < 0) {
            throw new ServiceException("Total cost cannot be negative");
        }
        if (dto.getProductDetails() == null) {
            throw new ServiceException("Transaction must be associated with a product");
        }
    }
}
