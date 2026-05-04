from pydantic import BaseModel

class OrderHistoryPayload(BaseModel):
    product_id: int | None = None
    name: str | None = None
    category: str | None = None
    price: float | None = None
    quantity: int | None = None
    total_cost: float | None = None
    date: str | None = None