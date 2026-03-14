import streamlit as st
import pandas as pd

from db import CloudStoreDB


st.set_page_config(page_title="CloudStore Control Panel", layout="wide")
st.title("CloudStore Control Panel")
st.caption("Operational interface for project queries and main use cases")


def _show_table(rows):
    if not rows:
        st.info("No data available")
        return
    st.dataframe(pd.DataFrame(rows), use_container_width=True)


def main():
    db = CloudStoreDB()

    try:
        db.fetch_one("SELECT 1 as ok")
        st.success("Database connection active")
    except Exception as e:
        st.error(f"DB connection failed: {e}")
        st.stop()

    tabs = st.tabs([
        "Dashboard",
        "Orders",
        "Products",
        "Users",
        "Permissions",
        "Transactions",
        "User Profile",
    ])

    with tabs[0]:
        st.subheader("Aggregate Statistics")
        stats = db.dashboard_stats()
        c1, c2, c3, c4 = st.columns(4)
        c1.metric("Products", stats["total_products"])
        c2.metric("Users", stats["total_users"])
        c3.metric("Transactions", stats["total_transactions"])
        c4.metric("Permissions", stats["total_permissions"])
        c5, c6, c7 = st.columns(3)
        c5.metric("Monthly sales", f"EUR {stats['monthly_sales']:.2f}")
        c6.metric("Monthly transactions", stats["monthly_transactions"])
        c7.metric("Low-stock products", stats["low_stock_products"])

    with tabs[1]:
        st.subheader("Process Order (atomic)")
        products = db.list_products()
        if not products:
            st.warning("No products available. Add one in the Products tab.")
        else:
            product_map = {
                f"#{p['id']} - {p['name']} (stock {p['stock']}, EUR {float(p['price']):.2f})": p["id"]
                for p in products
            }
            with st.form("process_order_form"):
                customer_name = st.text_input("Customer", value="Mario Rossi")
                selected_product = st.selectbox("Product", list(product_map.keys()))
                total_items = st.number_input("Quantity", min_value=1, value=1)
                payment_method = st.selectbox("Payment method", ["Credit Card", "Cash", "Bank Transfer"])
                city = st.text_input("City", value="Rome")
                customer_category = st.text_input("Customer category", value="Regular")
                discount = st.number_input("Discount (0.0 - 1.0)", min_value=0.0, max_value=1.0, value=0.0)
                submitted = st.form_submit_button("Process order")

                if submitted:
                    try:
                        result = db.process_order(
                            customer_name=customer_name,
                            product_id=product_map[selected_product],
                            total_items=int(total_items),
                            payment_method=payment_method,
                            city=city,
                            customer_category=customer_category,
                            discount=float(discount),
                        )
                        st.success("Order processed successfully")
                        st.json(result)
                    except Exception as e:
                        st.error(str(e))

    with tabs[2]:
        st.subheader("Products")
        col_a, col_b = st.columns([1, 2])

        with col_a:
            st.markdown("### New product")
            with st.form("new_product_form"):
                name = st.text_input("Product name")
                category = st.text_input("Category")
                price = st.number_input("Price", min_value=0.0, value=1.0)
                stock = st.number_input("Stock", min_value=0, value=0)
                save_product = st.form_submit_button("Save product")
                if save_product:
                    try:
                        db.save_product(name, category, float(price), int(stock))
                        st.success("Product saved")
                    except Exception as e:
                        st.error(str(e))

            st.markdown("### Low stock")
            threshold = st.number_input("Threshold", min_value=0, value=10)
            if st.button("Show low stock"):
                _show_table(db.low_stock_products(int(threshold)))

        with col_b:
            st.markdown("### Product list")
            products = db.list_products()
            _show_table(products)

            if products:
                product_ids = [p["id"] for p in products]
                delete_id = st.selectbox("Delete product ID", product_ids)
                if st.button("Delete product"):
                    try:
                        db.delete_product(delete_id)
                        st.success("Product deleted")
                    except Exception as e:
                        st.error(str(e))

    with tabs[3]:
        st.subheader("Users")
        perms = db.list_permissions()
        perm_options = {f"#{p['id']} - {p['category']}": p["id"] for p in perms}

        with st.form("register_user_form"):
            nickname = st.text_input("Nickname")
            name = st.text_input("Name")
            surname = st.text_input("Surname")
            email = st.text_input("Email")
            password = st.text_input("Password", type="password")
            if perm_options:
                selected_perm = st.selectbox("Permission", list(perm_options.keys()))
            else:
                selected_perm = None
                st.warning("No permissions available: create one in the Permissions tab")
            register = st.form_submit_button("Register user")
            if register:
                if selected_perm is None:
                    st.error("Cannot register user without a permission")
                else:
                    try:
                        db.register_user(
                            nickname=nickname,
                            name=name,
                            surname=surname,
                            email=email,
                            password=password,
                            permission_id=perm_options[selected_perm],
                        )
                        st.success("User registered")
                    except Exception as e:
                        st.error(str(e))

        st.markdown("### User list")
        _show_table(db.list_users())

    with tabs[4]:
        st.subheader("Permissions")
        with st.form("new_permission_form"):
            category = st.text_input("Permission category")
            save_perm = st.form_submit_button("Create permission")
            if save_perm:
                try:
                    db.save_permission(category)
                    st.success("Permission created")
                except Exception as e:
                    st.error(str(e))

        st.markdown("### Permission list")
        _show_table(db.list_permissions())

    with tabs[5]:
        st.subheader("Recent transactions")
        limit = st.number_input("Limit", min_value=1, max_value=500, value=50)
        _show_table(db.list_transactions(int(limit)))

    with tabs[6]:
        st.subheader("User Profile")
        nickname = st.text_input("Customer nickname", value="Mario Rossi")
        if st.button("Load profile"):
            profile = db.user_profile(nickname)
            if not profile:
                st.info("User not found")
            else:
                st.markdown("### User data")
                st.json(profile["user"])
                st.markdown("### Statistics")
                c1, c2 = st.columns(2)
                c1.metric("Total orders", profile["total_orders"])
                c2.metric("Total spent", f"EUR {profile['total_spent']:.2f}")
                st.markdown("### Order history")
                _show_table(profile["orders"])


if __name__ == "__main__":
    main()
