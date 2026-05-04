import os
import sys
import json
import httpx
from typing import Any

from models.AdviceRequest import AdviceRequest
from models.AdviceResponse import AdviceResponse

""" Rank products based on cart and order history affinity, discount, and price.
    
    Args:
        req (AdviceRequest): The advice request containing customer and product information.
    Returns:
        list[Any]: A list of ranked products from the catalog.
"""
def _rank_products(req: AdviceRequest) -> list[Any]:
    cart_categories = {item.category.lower() for item in req.cart_items if item.category}
    history_categories = {item.category.lower() for item in req.order_history if item.category}
    affinity_categories = cart_categories | history_categories

    discounted = max(req.discount, 0.0)
    return sorted(
        [p for p in req.catalog if p.stock > 0 and p.id not in {c.id for c in req.cart_items}],
        key=lambda p: (
            0 if p.category.lower() in affinity_categories else 1,
            -(p.price * discounted),
            p.price,
        ),
    )

""" Generate shopping advice based on the request, using LLM if configured, otherwise falling back to rule-based logic.
    
    Args:
        req (AdviceRequest): The advice request containing customer and product information.
    Returns:
        AdviceResponse: The generated advice response with suggestions.
"""
def _build_rule_based_advice(req: AdviceRequest) -> AdviceResponse:
    ranked = _rank_products(req)
    top = ranked[:3]
    suggestions: list[dict[str, Any]] = []
    for product in top:
        estimated_final = max(product.price * (1.0 - req.discount), 0.0)
        estimated_saving = max(product.price - estimated_final, 0.0)
        suggestions.append(
            {
                "productId": product.id,
                "name": product.name,
                "category": product.category,
                "price": round(product.price, 2),
                "estimatedPriceWithDiscount": round(estimated_final, 2),
                "estimatedSaving": round(estimated_saving, 2),
                "reason": "Picked using order history, cart affinity, and discount.",
            }
        )

    message = (
        f"Sconto transazioni ({req.discount * 100:.1f}%): "
        "scelte concise basate su ordini precedenti, carrello e risparmio. "
        "Aggiungi 1-2 prodotti economici per massimizzare lo sconto."
    )

    return AdviceResponse(message=message, suggestions=suggestions, source="rule_based")

""" Main function to build advice using LLM if configured, otherwise returns None to indicate fallback to rule-based logic.

    Args:
        req (AdviceRequest): The advice request containing customer and product information.
    Returns:
        AdviceResponse | None: The generated advice response from LLM, or None if LLM is not configured or fails.
"""
async def _build_llm_advice(req: AdviceRequest) -> AdviceResponse | None:
    model = os.getenv("OLLAMA_MODEL")
    base_url = os.getenv("OLLAMA_BASE_URL")

    if not model or not base_url:
        return None

    cart_summary = [
        {
            "id": p.id,
            "name": p.name,
            "category": p.category,
            "price": p.price,
            "quantity": p.quantity,
        }
        for p in req.cart_items
    ]

    ranked_catalog = _rank_products(req)
    catalog_summary = [
        {
            "id": p.id,
            "name": p.name,
            "category": p.category,
            "price": p.price,
            "stock": p.stock,
        }
        for p in ranked_catalog[:15]
    ]

    system_prompt = (
        "You are a shopping assistant for CloudStore. "
        "Provide short recommendations based on transaction discounts, stock, and category affinity. "
        "Limit your text reply to 3-4 short sentences, no prompt echo. "
        "You MUST return a valid JSON object with exactly two keys: "
        "'message' (your text response) and 'suggested_product_ids' (a list of integers, containing up to 3 product IDs from the catalog you recommend)."
    )

    user_prompt = {
        "customer_name": req.customer_name,
        "customer_category": req.customer_category,
        "discount": req.discount,
        "customer_message": req.prompt,
        "cart": cart_summary,
        "order_history": [
            {
                "product_id": h.product_id,
                "name": h.name,
                "category": h.category,
                "price": h.price,
                "quantity": h.quantity,
                "date": h.date,
            }
            for h in req.order_history[:10]
        ],
        "catalog": catalog_summary,
    }

    full_prompt = f"{system_prompt}\n\nCustomer context:\n{json.dumps(user_prompt)}"
    body = {
        "model": model,
        "prompt": full_prompt,
        "stream": False,
        "format": "json",
        "keep_alive": "20m",
        "options": {
            "temperature": 0.4,
            "num_predict": 250,
            "num_ctx": 2048,
        },
    }

    try:
        async with httpx.AsyncClient(timeout=90.0) as client:
            response = await client.post(f"{base_url.rstrip('/')}/api/generate", json=body)
            response.raise_for_status()
            payload = response.json()

        text = str(payload.get("response", "")).strip()
        if not text:
            return None

        try:
            llm_json = json.loads(text)
            message = llm_json.get("message", "").strip()
            suggested_ids = llm_json.get("suggested_product_ids", [])
        except json.JSONDecodeError:
            return None

        if not message:
            return None

        catalog_by_id = {p.id: p for p in req.catalog}
        suggestions: list[dict[str, Any]] = []
        
        if isinstance(suggested_ids, list):
            for pid in suggested_ids[:3]:
                product = catalog_by_id.get(pid)
                if product:
                    estimated_final = max(product.price * (1.0 - req.discount), 0.0)
                    estimated_saving = max(product.price - estimated_final, 0.0)
                    suggestions.append({
                        "productId": product.id,
                        "name": product.name,
                        "category": product.category,
                        "price": round(product.price, 2),
                        "estimatedPriceWithDiscount": round(estimated_final, 2),
                        "estimatedSaving": round(estimated_saving, 2),
                        "reason": "Suggested by AI Assistant"
                    })

        if not suggestions:
            rb = _build_rule_based_advice(req)
            suggestions = rb.suggestions

        return AdviceResponse(message=message, suggestions=suggestions, source="ollama")
    except Exception as e:
        print(f"[llm-assistant] ollama fallback activated: {repr(e)}", file=sys.stderr)
        return None