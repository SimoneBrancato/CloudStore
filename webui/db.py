import json
import os
import subprocess
from datetime import datetime


class CloudStoreDB:
    def __init__(self):
        self.java_cmd = os.getenv(
            "FACADE_JAVA_CMD",
            "java -cp /app/target/classes:/app/target/dependency/* com.cloudstore.service.facade.CloudStoreFacadeBridge",
        )

    def _call_facade(self, command, payload=""):
        cmd = self.java_cmd.split() + [command]
        result = subprocess.run(
            cmd,
            input=payload,
            text=True,
            capture_output=True,
            check=False,
        )

        stdout = (result.stdout or "").strip()
        if not stdout:
            raise RuntimeError(result.stderr.strip() or "No response from RemoteFacade")

        try:
            response = json.loads(stdout)
        except json.JSONDecodeError as exc:
            raise RuntimeError(f"Invalid response from RemoteFacade: {stdout}") from exc

        if result.returncode != 0 or not response.get("ok", False):
            raise RuntimeError(response.get("error") or result.stderr.strip() or "Error from RemoteFacade")

        return response.get("data")

    def fetch_one(self, query, params=()):
        if query.strip().lower().startswith("select 1"):
            return {"ok": 1}
        raise RuntimeError("Direct queries are disabled: use the RemoteFacade")

    def list_permissions(self):
        return self._call_facade("list_permissions")

    def save_permission(self, category):
        payload = json.dumps({"id": 0, "category": category})
        return self._call_facade("save_permission", payload)

    def list_products(self):
        return self._call_facade("list_products")

    def save_product(self, name, category, price, stock):
        payload = json.dumps(
            {
                "id": 0,
                "name": name,
                "description": category,
                "price": float(price),
                "stock": int(stock),
            }
        )
        return self._call_facade("save_product", payload)

    def delete_product(self, product_id):
        return self._call_facade("delete_product", str(int(product_id)))

    def update_product_stock(self, product_id, new_stock):
        products = self.list_products()
        target = next((p for p in products if int(p["id"]) == int(product_id)), None)
        if not target:
            raise ValueError(f"Product not found with ID: {product_id}")
        payload = json.dumps(
            {
                "id": int(target["id"]),
                "name": target["name"],
                "description": target.get("description", target.get("category", "")),
                "price": float(target["price"]),
                "stock": int(new_stock),
            }
        )
        return self._call_facade("save_product", payload)

    def low_stock_products(self, threshold):
        return self._call_facade("low_stock", str(int(threshold)))

    def list_users(self):
        return self._call_facade("list_users")

    def register_user(self, nickname, name, surname, email, password, permission_id):
        payload = json.dumps(
            {
                "nickname": nickname,
                "name": name,
                "surname": surname,
                "email": email,
                "password": password,
                "permission": {"id": int(permission_id)},
            }
        )
        return self._call_facade("register_user", payload)

    def list_transactions(self, limit=50):
        return self._call_facade("list_transactions", str(int(limit)))

    def dashboard_stats(self):
        raw = self._call_facade("dashboard_stats")
        return {
            "total_products": raw.get("total_products", raw.get("totalProducts", 0)),
            "total_users": raw.get("total_users", raw.get("totalUsers", 0)),
            "total_transactions": raw.get("total_transactions", raw.get("totalTransactions", 0)),
            "total_permissions": raw.get("total_permissions", raw.get("totalPermissions", 0)),
            "monthly_sales": float(raw.get("monthly_sales", raw.get("monthlySales", 0.0))),
            "monthly_transactions": raw.get("monthly_transactions", raw.get("monthlyTransactions", 0)),
            "low_stock_products": raw.get("low_stock_products", raw.get("lowStockProducts", 0)),
        }

    def user_profile(self, nickname):
        raw = self._call_facade("user_profile", nickname)
        if not raw:
            return None
        return {
            "user": raw.get("user"),
            "orders": raw.get("orders", raw.get("orderHistory", [])),
            "total_orders": raw.get("total_orders", raw.get("totalOrders", 0)),
            "total_spent": float(raw.get("total_spent", raw.get("totalSpent", 0.0))),
        }

    def process_order(
        self,
        customer_name,
        product_id,
        total_items,
        payment_method,
        city,
        customer_category,
        discount,
    ):
        products = self.list_products()
        target = next((p for p in products if int(p["id"]) == int(product_id)), None)
        if not target:
            raise ValueError(f"Product not found with ID: {product_id}")

        payload = json.dumps(
            {
                "id": 0,
                "date": datetime.now().isoformat(),
                "customerName": customer_name,
                "product": target["name"],
                "totalItems": int(total_items),
                "totalCost": float(target["price"]) * int(total_items),
                "paymentMethod": payment_method,
                "city": city,
                "discountApplied": 1 if float(discount) > 0 else 0,
                "customerCategory": customer_category,
                "discount": float(discount),
                "productDetails": {"id": int(product_id)},
            }
        )
        return self._call_facade("process_order", payload)
