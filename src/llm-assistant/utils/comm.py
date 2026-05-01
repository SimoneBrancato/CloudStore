import os
import sys
import httpx
from typing import Any

from models.AdviceRequest import AdviceRequest
from models.AdviceResponse import AdviceResponse


""" Builds a rule-based advice response based on the customer's cart items, order history, and discount. 

 Args:
    req (AdviceRequest): The advice request containing customer information, cart items, order history, catalog, and discount.
Returns:
    AdviceResponse: The generated advice response with a message and product suggestions.
"""
def _build_rule_based_advice(req: AdviceRequest) -> AdviceResponse:
    cart_categories = {item.category.lower() for item in req.cart_items if item.category}
    history_categories = {item.category.lower() for item in req.order_history if item.category}
    affinity_categories = cart_categories | history_categories

    discounted = max(req.discount, 0.0)
    ranked = sorted(
        [p for p in req.catalog if p.stock > 0 and p.id not in {c.id for c in req.cart_items}],
        key=lambda p: (
            0 if p.category.lower() in affinity_categories else 1,
            -(p.price * discounted),
            p.price,
        ),
    )

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

""" Shortens the LLM response to 3-4 sentences and removes any prompt echo.
Args:
    text (str): The original LLM response text.
Returns:
    str: The shortened and cleaned response text.
"""
def _shorten_response(text: str) -> str:
    cleaned = " ".join(text.replace("\n", " ").split()).strip()
    if "you asked" in cleaned.lower():
        cleaned = cleaned.replace("You asked:", "").replace("you asked:", "").strip()
    if not cleaned:
        return cleaned
    sentences = []
    for chunk in cleaned.replace("!", ".").replace("?", ".").split("."):
        chunk = chunk.strip()
        if chunk:
            sentences.append(chunk)
        if len(sentences) >= 4:
            break
    if not sentences:
        return cleaned
    return ". ".join(sentences).strip() + "."


""" Builds advice using an LLM by sending a request to the Ollama API.

Args:
    req (AdviceRequest): The advice request containing customer information, cart items, order history, catalog, and discount.
Returns:
    AdviceResponse | None: The generated advice response from the LLM.
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

    catalog_summary = [
        {
            "id": p.id,
            "name": p.name,
            "category": p.category,
            "price": p.price,
            "stock": p.stock,
        }
        for p in req.catalog[:15]
    ]

    system_prompt = (
        "You are a shopping assistant for CloudStore. "
        "Provide short recommendations based on transaction discounts, stock, and category affinity. "
        "Limit your reply to 3-4 short sentences, no bullet lists, no prompt echo. "
        "If possible, mention up to 3 concrete products from the provided catalog IDs."
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

    full_prompt = f"{system_prompt}\n\nCustomer context:\n{user_prompt}"
    body = {
        "model": model,
        "prompt": full_prompt,
        "stream": False,
        "keep_alive": "20m",
        "options": {
            "temperature": 0.4,
            "num_predict": 180,
            "num_ctx": 2048,
        },
    }

    try:
        async with httpx.AsyncClient(timeout=24.0) as client:
            response = await client.post(f"{base_url.rstrip('/')}/api/generate", json=body)
            response.raise_for_status()
            payload = response.json()

        text = str(payload.get("response", "")).strip()
        if not text:
            return None

        text = _shorten_response(text)

        rb = _build_rule_based_advice(req)
        return AdviceResponse(message=text, suggestions=rb.suggestions, source="ollama")
    except Exception as e:
        print(f"[llm-assistant] ollama fallback activated: {e}", file=sys.stderr)
        return None