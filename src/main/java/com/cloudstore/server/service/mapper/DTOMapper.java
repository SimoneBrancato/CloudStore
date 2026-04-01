package com.cloudstore.server.service.mapper;

import com.cloudstore.server.model.dto.*;
import com.cloudstore.server.model.entities.*;

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
            dto.getDescription(),
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
}
