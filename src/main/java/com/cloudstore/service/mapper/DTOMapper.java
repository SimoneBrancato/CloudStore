package com.cloudstore.service.mapper;

import com.cloudstore.model.dto.*;
import com.cloudstore.model.entities.*;

public class DTOMapper {
    
    public static PermissionDTO toDTO(Permission permission) {
        if (permission == null) return null;
        return new PermissionDTO(permission.id(), permission.category());
    }
    
    public static Permission toEntity(PermissionDTO dto) {
        if (dto == null) return null;
        return new Permission(dto.getId(), dto.getCategory());
    }
    
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
    
    public static Product toEntity(ProductDTO dto) {
        if (dto == null) return null;
        return new Product(
            dto.getId(),
            dto.getName(),
            dto.getDescription(),
            dto.getPrice(),
            dto.getStock()
        );
    }
    
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
}
