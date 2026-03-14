package com.cloudstore.service.facade;

import com.cloudstore.model.dto.PermissionDTO;
import com.cloudstore.model.dto.TransactionDTO;
import com.cloudstore.model.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;

public class CloudStoreFacadeExample {

    public static void main(String[] args) {
        try {
            CloudStoreFacade facade = new CloudStoreFacade();
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            System.out.println("=== CloudStore Remote Facade - Examples ===\n");

            // ── 1. Aggregate dashboard (cross-domain) ─────────────────────────────
            System.out.println("1. Dashboard stats (cross-domain):");
            System.out.println(mapper.writeValueAsString(facade.getDashboardStats()));
            System.out.println();

            // ── 2. User profile with order history (cross-domain) ─────────────────
            System.out.println("2. User profile with order history:");
            System.out.println(mapper.writeValueAsString(facade.getUserProfile("Mario Rossi")));
            System.out.println();

            // ── 3. processOrder: check stock -> create transaction -> update stock ─
            System.out.println("3. Process a new order (cross-domain):");
            String orderJson = """
                    {
                        "id": 0,
                        "customerName": "Mario Rossi",
                        "product": "Laptop",
                        "totalItems": 1,
                        "totalCost": 999.99,
                        "paymentMethod": "Credit Card",
                        "city": "Roma",
                        "discountApplied": 0,
                        "customerCategory": "Regular",
                        "discount": 0.0,
                        "productDetails": { "id": 1 }
                    }
                    """;
            TransactionDTO order = mapper.readValue(orderJson, TransactionDTO.class);
            System.out.println("Processed order: " + mapper.writeValueAsString(facade.processOrder(order)));
            System.out.println();

            // ── 4. User registration with validation ──────────────────────────────
            System.out.println("4. Register a new user (with validation):");
            if (facade.countPermissions() == 0) {
                PermissionDTO permission = new PermissionDTO();
                permission.setId(0);
                permission.setCategory("user");
                facade.savePermission(permission);
            }
            int validPermissionId = facade.getFirstAvailablePermissionId();
            String newUserJson = String.format("""
                    {
                        "nickname": "luigi.verdii",
                        "name": "Luigi",
                        "surname": "Verdi",
                        "email": "luigi.verdii@example.com",
                        "password": "SecurePass123",
                        "permission": { "id": %d }
                    }
                    """, validPermissionId);
            UserDTO newUser = mapper.readValue(newUserJson, UserDTO.class);
            System.out.println("Registered user: " + mapper.writeValueAsString(facade.registerUser(newUser)));
            System.out.println();

            // ── 5. Low-stock products ──────────────────────────────────────────────
            System.out.println("5. Low-stock products (< 10):");
            System.out.println(mapper.writeValueAsString(facade.findLowStockProducts(10)));
            System.out.println();

            // ── 6. Last 5 transactions ────────────────────────────────────────────
            System.out.println("6. Last 5 transactions:");
            System.out.println(mapper.writeValueAsString(facade.findRecentTransactions(5)));
            System.out.println();

            // ── 7. Total sales for current month ─────────────────────────────────
            System.out.println("7. Total sales for current month:");
            LocalDateTime startOfMonth = LocalDateTime.now()
                    .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            System.out.println("Total: EUR " + facade.calculateTotalSales(startOfMonth, LocalDateTime.now()));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

