package service.facade;

import model.dto.PermissionDTO;
import model.dto.ProductDTO;
import model.dto.TransactionDTO;
import model.dto.UserDTO;
import model.dto.auth.LoginResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Esempio di utilizzo del CloudStoreFacade
 */
public class CloudStoreFacadeExample {

    public static void main(String[] args) {
        try {
            // Inizializzazione
            CloudStoreFacade facade = new CloudStoreFacade();
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            System.out.println("=== CloudStore Facade - Examples ===\n");

            // ── 1. Dashboard stats ─────────────────────────────────────────────
            System.out.println("1. Dashboard stats:");
            System.out.println(mapper.writeValueAsString(facade.getDashboardStats()));
            System.out.println();

            // ── 2. User profile with order history ─────────────────────────────────
            System.out.println("2. User profile with order history:");
            System.out.println(mapper.writeValueAsString(facade.getUserProfile("Mario Rossi")));
            System.out.println();

            // ── 3. Process a new order ────────────────────────────────────────────
            System.out.println("3. Process a new order:");
            
            // Creazione manuale del DTO invece di usare JSON
            TransactionDTO order = new TransactionDTO();
            order.setId(0);
            order.setCustomerName("Mario Rossi");
            order.setProduct("Laptop");
            order.setTotalItems(1);
            order.setTotalCost(999.99);
            order.setPaymentMethod("Credit Card");
            order.setCity("Roma");
            order.setDiscountApplied(0);
            order.setCustomerCategory("Regular");
            order.setDiscount(0.0f);
            
            ProductDTO productDetails = new ProductDTO();
            productDetails.setId(1);
            productDetails.setName("Laptop");
            productDetails.setDescription("Electronics");
            productDetails.setPrice(999.99);
            productDetails.setStock(10);
            order.setProductDetails(productDetails);
            
            System.out.println("Processed order: " + mapper.writeValueAsString(facade.processOrder(order)));
            System.out.println();

            // ── 4. Register a new user ──────────────────────────────────────────────
            System.out.println("4. Register a new user:");
            
            // Verifica e creazione permesso se necessario
            if (facade.countPermissions() == 0) {
                PermissionDTO permission = new PermissionDTO();
                permission.setId(0);
                permission.setCategory("USER");
                facade.savePermission(permission);
            }
            
            int validPermissionId = facade.getFirstAvailablePermissionId();
            
            UserDTO newUser = new UserDTO();
            newUser.setNickname("luigi.verdi");
            newUser.setName("Luigi");
            newUser.setSurname("Verdi");
            newUser.setEmail("luigi.verdi@example.com");
            newUser.setPassword("SecurePass123");
            
            PermissionDTO userPermission = new PermissionDTO();
            userPermission.setId(validPermissionId);
            userPermission.setCategory("USER");
            newUser.setPermission(userPermission);
            
            System.out.println("Registered user: " + mapper.writeValueAsString(facade.registerUser(newUser)));
            
            LoginResult userLogin = facade.authenticateUser("luigi.verdi", "SecurePass123");
            System.out.println("User token obtained for luigi.verdi: " + 
                               userLogin.getToken().substring(0, Math.min(20, userLogin.getToken().length())) + "...");
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
                    .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime now = LocalDateTime.now();
            System.out.println("Total: EUR " + facade.calculateTotalSales(startOfMonth, now));

            // ── 8. Product categories ─────────────────────────────────────────────
            System.out.println("\n8. Product categories:");
            System.out.println(mapper.writeValueAsString(facade.getAllProductCategories()));
            System.out.println();

            // ── 9. Checkout context for a customer ─────────────────────────────────
            System.out.println("9. Checkout context:");
            Map<Integer, Integer> items = new HashMap<>();
            items.put(1, 2);  // productId 1, quantity 2
            items.put(2, 1);  // productId 2, quantity 1
            
            Map<String, Object> checkoutContext = facade.getCheckoutContext("Mario Rossi", items);
            System.out.println(mapper.writeValueAsString(checkoutContext));
            System.out.println();

            // ── 10. Process cart order ────────────────────────────────────────────
            System.out.println("10. Process cart order:");
            Map<String, Object> cartResult = facade.processCartOrder(
                "Mario Rossi",
                "PayPal",
                "Milano",
                items
            );
            System.out.println(mapper.writeValueAsString(cartResult));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}