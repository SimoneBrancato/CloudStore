package com.cloudstore.server.service.mapper;

import com.cloudstore.server.model.dto.*;
import com.cloudstore.server.model.entities.*;
import com.cloudstore.server.model.domain.*;

import java.util.List;

public class DTOMapper {
    
    /**
        * Converts a Permission entity to a PermissionDTO.
        * @param permission The Permission entity to convert.
        * @return A PermissionDTO representing the given Permission entity.
    **/
    public static PermissionDTO toDTO(Permission permission) {
        if (permission == null) return null;
        return new PermissionDTO(permission.id(), permission.category());
    }
    
    /**
        * Converts a PermissionDTO to a Permission entity.
        * @param dto The PermissionDTO to convert.
        * @return A Permission entity representing the given PermissionDTO.
    **/
    public static Permission toEntity(PermissionDTO dto) {
        if (dto == null) return null;
        return new Permission(dto.getId(), dto.getCategory());
    }
    
    /**
        * Converts a Product entity to a ProductDTO.
        * @param product The Product entity to convert.
        * @return A ProductDTO representing the given Product entity.
    **/
    public static ProductDTO toDTO(Product product) {
        if (product == null) return null;
        return new ProductDTO(
            product.id(),
            product.name(),
            product.category(),
            product.price(),
            product.stock()
        );
    }
    
    /**
        * Converts a ProductDTO to a Product entity.
        * @param dto The ProductDTO to convert.
        * @return A Product entity representing the given ProductDTO.
    **/
    public static Product toEntity(ProductDTO dto) {
        if (dto == null) return null;
        return new Product(
            dto.getId(),
            dto.getName(),
            dto.getCategory(),
            dto.getPrice(),
            dto.getStock()
        );
    }
    
    /**
        * Converts a User entity to a UserDTO.
        * @param user The User entity to convert.
        * @return A UserDTO representing the given User entity.
    **/
    public static UserDTO toDTO(User user) {
        if (user == null) return null;
        return new UserDTO(
            user.nickname(),
            user.name(),
            user.surname(),
            user.email(),
            user.password(),
            toDTO(user.PermissionID())
        );
    }
    
    /**
        * Converts a UserDTO to a User entity.
        * @param dto The UserDTO to convert.
        * @return A User entity representing the given UserDTO.
    **/
    public static User toEntity(UserDTO dto) {
        if (dto == null) return null;
        return new User(
            dto.getNickname(),
            dto.getName(),
            dto.getSurname(),
            dto.getEmail(),
            dto.getPassword(),
            toEntity(dto.getPermission())
        );
    }
    
    /**
        * Converts a Transaction entity to a TransactionDTO.
        * @param transaction The Transaction entity to convert.
        * @return A TransactionDTO representing the given Transaction entity.
    **/
    public static TransactionDTO toDTO(Transaction transaction) {
        if (transaction == null) return null;
        return new TransactionDTO(
            transaction.id(),
            transaction.date(),
            transaction.CustomerName(),
            transaction.Product(),
            transaction.TotalItems(),
            transaction.TotalCost(),
            transaction.PaymentMethod(),
            transaction.City(),
            transaction.DiscountApplied(),
            transaction.CustomerCategory(),
            transaction.Discount(),
            toDTO(transaction.ProductID())
        );
    }
    
    /**
        * Converts a TransactionDTO to a Transaction entity.
        * @param dto The TransactionDTO to convert.
        * @return A Transaction entity representing the given TransactionDTO.
    **/
    public static Transaction toEntity(TransactionDTO dto) {
        if (dto == null) return null;
        return new Transaction(
            dto.getId(),
            dto.getDate(),
            dto.getCustomerName(),
            dto.getProduct(),
            dto.getTotalItems(),
            dto.getTotalCost(),
            dto.getPaymentMethod(),
            dto.getCity(),
            dto.getDiscountApplied(),
            dto.getCustomerCategory(),
            dto.getDiscount(),
            toEntity(dto.getProductDetails())
        );
    }

    /**
        * Converts a CartOrderResult entity to a CartOrderResultDTO.
        * @param entity The CartOrderResult entity to convert.
        * @return A CartOrderResultDTO representing the given entity.
    **/
    public static CartOrderResultDTO toDTO(CartOrderResult entity) {
        if (entity == null) return null;
        List<TransactionDTO> transactionDTOs = entity.transactions() != null
                ? entity.transactions().stream().map(DTOMapper::toDTO).toList()
                : List.of();
        return new CartOrderResultDTO(transactionDTOs, entity.totalItems(), entity.cartTotal(), entity.lines());
    }

    /**
        * Converts a CheckoutContext entity to a CheckoutContextDTO.
        * @param entity The CheckoutContext entity to convert.
        * @return A CheckoutContextDTO representing the given entity.
    **/
    public static CheckoutContextDTO toDTO(CheckoutContext entity) {
        if (entity == null) return null;
        return new CheckoutContextDTO(
            entity.customerName(), entity.customerCategory(), entity.discount(),
            entity.discountApplied(), entity.discountSource(), entity.sampleSize(), entity.sampleWindow()
        );
    }

    /**
        * Converts a DashboardStats entity to a DashboardStatsDTO.
        * @param entity The DashboardStats entity to convert.
        * @return A DashboardStatsDTO representing the given entity.
    **/
    public static DashboardStatsDTO toDTO(DashboardStats entity) {
        if (entity == null) return null;
        return new DashboardStatsDTO(
            entity.totalProducts(), entity.totalUsers(), entity.totalTransactions(),
            entity.totalPermissions(), entity.monthlySales(), entity.monthlyTransactions(), entity.lowStockProducts()
        );
    }

    /**
        * Converts a SellerDashboardStats entity to a SellerDashboardStatsDTO.
        * @param entity The SellerDashboardStats entity to convert.
        * @return A SellerDashboardStatsDTO representing the given entity.
    **/
    public static SellerDashboardStatsDTO toDTO(SellerDashboardStats entity) {
        if (entity == null) return null;
        return new SellerDashboardStatsDTO(
            entity.totalRevenue(), entity.totalOrders(), entity.averageOrderValue(),
            entity.productsSold(), entity.totalSales(), entity.lowStockProducts()
        );
    }

    /**
        * Converts a UserProfile entity to a UserProfileDTO.
        * @param entity The UserProfile entity to convert.
        * @return A UserProfileDTO representing the given entity.
    **/
    public static UserProfileDTO toDTO(UserProfile entity) {
        if (entity == null) return null;
        List<TransactionDTO> orderDTOs = entity.orderHistory() != null
                ? entity.orderHistory().stream().map(DTOMapper::toDTO).toList()
                : List.of();
        return new UserProfileDTO(toDTO(entity.user()), orderDTOs, entity.totalOrders(), entity.totalSpent());
    }

    /**
        * Converts a SalesOrderSummary entity to a SalesOrderSummaryDTO.
        * @param entity The SalesOrderSummary entity to convert.
        * @return A SalesOrderSummaryDTO representing the given entity.
    **/
    public static SalesOrderSummaryDTO toDTO(SalesOrderSummary entity) {
        if (entity == null) return null;
        return new SalesOrderSummaryDTO(
            entity.id(), entity.customerName(), entity.product(),
            entity.totalItems(), entity.totalCost(), entity.paymentMethod(), entity.city()
        );
    }

    /**
        * Converts a TopCustomerSummary domain record to a TopCustomerSummaryDTO.
        * @param entity The TopCustomerSummary to convert.
        * @return A TopCustomerSummaryDTO representing the given entity.
    **/
    public static TopCustomerSummaryDTO toDTO(TopCustomerSummary entity) {
        if (entity == null) return null;
        return new TopCustomerSummaryDTO(
            entity.customerName(), entity.orderCount(), entity.totalSpent(), entity.lastOrderDate()
        );
    }
}
