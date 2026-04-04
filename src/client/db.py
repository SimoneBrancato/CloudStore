import json
import os
import requests
import sys


class CloudStoreDB:
    def __init__(self, token=None):
        self.token = token
        backend_host = os.getenv('BACKEND_HOST', 'server')
        backend_port = os.getenv('BACKEND_PORT', '9999')
        self.backend_url = f"http://{backend_host}:{backend_port}"
        print(f"Backend URL: {self.backend_url}", file=sys.stderr)
        
        self._test_connection()
    
    def _test_connection(self):
        try:
            response = requests.get(f"{self.backend_url}/", timeout=5)
            print(f"Backend connection test: status={response.status_code}, response={response.text}", file=sys.stderr)
            if response.status_code == 200:
                print("Backend is reachable", file=sys.stderr)
            else:
                print(f"Backend returned status {response.status_code}", file=sys.stderr)
        except requests.exceptions.ConnectionError as e:
            print(f"CRITICAL: Cannot connect to backend at {self.backend_url}", file=sys.stderr)
            print(f"Error: {e}", file=sys.stderr)
            print("Make sure the backend container is running and port 9999 is exposed", file=sys.stderr)
        except Exception as e:
            print(f"Backend test failed: {e}", file=sys.stderr)
    
    def _call_facade(self, method_name, *args, token=None):
        request = {"method": method_name, "args": args}
        print(f"Sending request: {request}", file=sys.stderr)
        
        headers = {"Content-Type": "application/json"}
        if token:
            headers["Authorization"] = f"Bearer {token}"
            
        try:
            response = requests.post(
                self.backend_url,
                json=request,
                timeout=30,
                headers=headers
            )
            print(f"Response status: {response.status_code}", file=sys.stderr)
            
            if response.status_code != 200:
                print(f"HTTP Error: {response.status_code} - {response.text}", file=sys.stderr)
                raise RuntimeError(f"HTTP {response.status_code}: {response.text}")
            
            result = response.json()
            print(f"Parsed response: {result}", file=sys.stderr)
            
            if not result.get("ok"):
                raise RuntimeError(result.get("error", "Unknown error"))
            
            return result.get("data")
            
        except requests.exceptions.ConnectionError as e:
            print(f"Connection error: {e}", file=sys.stderr)
            raise RuntimeError(f"Cannot connect to backend at {self.backend_url}")
        except requests.exceptions.Timeout as e:
            print(f"Timeout error: {e}", file=sys.stderr)
            raise RuntimeError(f"Backend request timeout")
        except requests.exceptions.RequestException as e:
            print(f"HTTP error: {e}", file=sys.stderr)
            raise RuntimeError(f"Backend request failed: {e}")
        except json.JSONDecodeError as e:
            print(f"JSON decode error: {e}", file=sys.stderr)
            raise RuntimeError(f"Invalid response from backend")
    
    def _call_authenticated_facade(self, method_name, *args):
        if not self.token:
            raise RuntimeError("Authentication required: missing token")
        return self._call_facade(method_name, *args, token=self.token)
    
    #  AUTH 
    def authenticate_user(self, nickname, password):
        return self._call_facade("authenticateUser", nickname, password)
    
    #  PRODUCT 
    def list_products(self):
        return self._call_facade("getAllProducts")
    
    def list_product_categories(self):
        return self._call_facade("getAllProductCategories")
    
    def list_products_by_category(self, category):
        if not category or category == "All":
            return self.list_products()
        return self._call_facade("findProductsByCategory", category)
    
    def save_product(self, name, category, price, stock):
        product = {
            "id": 0,
            "name": name,
            "description": category,
            "price": float(price),
            "stock": int(stock)
        }
        return self._call_authenticated_facade("saveProduct", product)
    
    def delete_product(self, product_id):
        return self._call_authenticated_facade("deleteProduct", int(product_id))
    
    def update_product_stock(self, product_id, new_stock):
        return self._call_authenticated_facade("updateProductStock", int(product_id), int(new_stock))
    
    def update_seller_product_stock(self, product_id, new_stock):
        """Update product stock for sellers."""
        return self._call_authenticated_facade("updateSellerProductStock", int(product_id), int(new_stock))
    
    def low_stock_products(self, threshold):
        return self._call_authenticated_facade("findLowStockProducts", int(threshold))
    
    #  USER 
    def list_users(self):
        return self._call_authenticated_facade("getAllUsers")
    
    def register_user(self, nickname, name, surname, email, password, permission_id):
        user = {
            "nickname": nickname,
            "name": name,
            "surname": surname,
            "email": email,
            "password": password,
            "permission": {"id": int(permission_id)}
        }
        return self._call_facade("registerUser", user)
    
    #  PERMISSION 
    def list_permissions(self):
        return self._call_authenticated_facade("getAllPermissions")
    
    def save_permission(self, category):
        permission = {"id": 0, "category": category}
        return self._call_authenticated_facade("savePermission", permission)
    
    def get_first_available_permission_id(self):
        return self._call_authenticated_facade("getFirstAvailablePermissionId")
    
    #  TRANSACTION 
    def list_transactions(self, limit=50):
        return self._call_authenticated_facade("findRecentTransactions", int(limit))
    
    def get_customer_checkout_context(self, customer_name, items=None):
        normalized_items = {}
        for item in items or []:
            product_id = int(item.get("product_id", 0))
            quantity = int(item.get("quantity", 0))
            if product_id > 0 and quantity > 0:
                normalized_items[product_id] = quantity
        return self._call_authenticated_facade("getCheckoutContext", customer_name, normalized_items)
    
    def process_cart_order(self, customer_name, payment_method, city, items):
        normalized_items = {}
        for item in items:
            product_id = int(item.get("product_id", 0))
            quantity = int(item.get("quantity", 0))
            if product_id > 0 and quantity > 0:
                normalized_items[product_id] = quantity
        return self._call_authenticated_facade("processCartOrder", customer_name, payment_method, city, normalized_items)
    
    def dashboard_stats(self):
        raw = self._call_authenticated_facade("getDashboardStats")
        return {
            "total_products": raw.get("totalProducts", 0),
            "total_users": raw.get("totalUsers", 0),
            "total_transactions": raw.get("totalTransactions", 0),
            "total_permissions": raw.get("totalPermissions", 0),
            "monthly_sales": float(raw.get("monthlySales", 0.0)),
            "monthly_transactions": raw.get("monthlyTransactions", 0),
            "low_stock_products": raw.get("lowStockProducts", 0),
        }
    
    def user_profile(self, nickname):
        raw = self._call_authenticated_facade("getUserProfile", nickname)
        if not raw:
            return None
        return {
            "user": raw.get("user"),
            "orders": raw.get("orderHistory", []),
            "total_orders": raw.get("totalOrders", 0),
            "total_spent": float(raw.get("totalSpent", 0.0)),
        }
    
    def process_order(self, customer_name, product_id, total_items, payment_method, city, customer_category, discount):
        products = self.list_products()
        target = next((p for p in products if int(p["id"]) == int(product_id)), None)
        if not target:
            raise ValueError(f"Product not found with ID: {product_id}")
        
        transaction = {
            "id": 0,
            "customerName": customer_name,
            "product": target["name"],
            "totalItems": int(total_items),
            "totalCost": float(target["price"]) * int(total_items),
            "paymentMethod": payment_method,
            "city": city,
            "discountApplied": 1 if float(discount) > 0 else 0,
            "customerCategory": customer_category,
            "discount": float(discount),
            "productDetails": {"id": int(product_id)}
        }
        return self._call_authenticated_facade("processOrder", transaction)
    
    #  SELLER 
    def get_seller_dashboard_stats(self):
        """Get dashboard statistics for the authenticated seller."""
        raw = self._call_authenticated_facade("getSellerDashboardStats")
        return {
            "total_sales": float(raw.get("totalSales", 0.0)),
            "total_orders": raw.get("totalOrders", 0),
            "total_revenue": float(raw.get("totalRevenue", 0.0)),
            "average_order_value": float(raw.get("averageOrderValue", 0.0)),
            "products_sold": raw.get("productsSold", 0),
            "low_stock_products": raw.get("lowStockProducts", 0),
        }
    
    def get_seller_products(self):
        """Get all products associated with the authenticated seller."""
        return self._call_authenticated_facade("getSellerProducts")
    
    def get_seller_sales_orders(self, limit=50):
        """Get recent sales orders for the authenticated seller's products."""
        return self._call_authenticated_facade("getSellerSalesOrders", int(limit))
    
    def get_seller_top_customers(self, limit=10):
        """Get top customers by total spending for the authenticated seller."""
        return self._call_authenticated_facade("getSellerTopCustomers", int(limit))
    
    def fetch_one(self, query, params=()):
        if query.strip().lower().startswith("select 1"):
            return {"ok": 1}
        raise RuntimeError("Direct queries are disabled: use the Facade")