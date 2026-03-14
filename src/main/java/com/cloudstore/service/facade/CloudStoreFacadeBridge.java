package com.cloudstore.service.facade;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.cloudstore.model.dto.PermissionDTO;
import com.cloudstore.model.dto.ProductDTO;
import com.cloudstore.model.dto.TransactionDTO;
import com.cloudstore.model.dto.UserDTO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


public class CloudStoreFacadeBridge {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Map<String, CommandHandler> COMMAND_HANDLERS = buildCommandHandlers();

    @FunctionalInterface
    private interface CommandHandler {
        JsonNode handle(CloudStoreFacade facade, String payload) throws Exception;
    }

    static {
        MAPPER.registerModule(new JavaTimeModule());
    }

    private static Map<String, CommandHandler> buildCommandHandlers() {
        Map<String, CommandHandler> handlers = new HashMap<>();

        handlers.put("dashboard_stats", (facade, payload) -> MAPPER.valueToTree(facade.getDashboardStats()));
        handlers.put("list_permissions", (facade, payload) -> MAPPER.valueToTree(facade.getAllPermissions()));
        handlers.put("save_permission", (facade, payload) -> {
            PermissionDTO dto = MAPPER.readValue(payload, PermissionDTO.class);
            return MAPPER.valueToTree(facade.savePermission(dto));
        });

        handlers.put("list_products", (facade, payload) -> MAPPER.valueToTree(facade.getAllProducts()));
        handlers.put("save_product", (facade, payload) -> {
            ProductDTO dto = MAPPER.readValue(payload, ProductDTO.class);
            return MAPPER.valueToTree(facade.saveProduct(dto));
        });
        handlers.put("delete_product", (facade, payload) -> MAPPER.valueToTree(facade.deleteProduct(Integer.parseInt(payload))));
        handlers.put("low_stock", (facade, payload) -> MAPPER.valueToTree(facade.findLowStockProducts(Integer.parseInt(payload))));

        handlers.put("list_users", (facade, payload) -> MAPPER.valueToTree(facade.getAllUsers()));
        handlers.put("register_user", (facade, payload) -> {
            UserDTO dto = MAPPER.readValue(payload, UserDTO.class);
            return MAPPER.valueToTree(facade.registerUser(dto));
        });

        handlers.put("list_transactions", (facade, payload) -> MAPPER.valueToTree(facade.findRecentTransactions(Integer.parseInt(payload))));
        handlers.put("user_profile", (facade, payload) -> MAPPER.valueToTree(facade.getUserProfile(payload)));
        handlers.put("process_order", (facade, payload) -> {
            TransactionDTO dto = MAPPER.readValue(payload, TransactionDTO.class);
            return MAPPER.valueToTree(facade.processOrder(dto));
        });

        handlers.put("total_sales", (facade, payload) -> {
            JsonNode req = MAPPER.readTree(payload);
            LocalDateTime start = LocalDateTime.parse(req.get("start").asText());
            LocalDateTime end = LocalDateTime.parse(req.get("end").asText());
            return MAPPER.valueToTree(facade.calculateTotalSales(start, end));
        });

        return handlers;
    }

    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                throw new IllegalArgumentException("Missing command");
            }

            String command = args[0];
            String payload = readStdin().trim();

            CloudStoreFacade facade = new CloudStoreFacade();
            JsonNode data = executeCommand(facade, command, payload);

            MAPPER.writeValue(System.out, MAPPER.createObjectNode()
                    .put("ok", true)
                    .set("data", data));
        } catch (Exception e) {
            try {
                MAPPER.writeValue(System.out, MAPPER.createObjectNode()
                        .put("ok", false)
                        .put("error", e.getMessage()));
            } catch (IOException ignored) {
                System.out.print("{\"ok\":false,\"error\":\"Serialization error\"}");
            }
            System.exit(1);
        }
    }

    private static JsonNode executeCommand(CloudStoreFacade facade, String command, String payload) throws Exception {
        CommandHandler handler = COMMAND_HANDLERS.get(command);
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported command: " + command);
        }
        return handler.handle(facade, payload);
    }

    private static String readStdin() throws IOException {
        return new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
    }
}
