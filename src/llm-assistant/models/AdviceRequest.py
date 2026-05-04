from pydantic import BaseModel, Field

from .ProductPayload import ProductPayload
from .OrderHistoryPayload import OrderHistoryPayload

class AdviceRequest(BaseModel):
    customer_name: str = Field(min_length=1)
    customer_category: str = "Customer"
    discount: float = 0.0
    prompt: str = ""
    cart_items: list[ProductPayload] = Field(default_factory=list)
    catalog: list[ProductPayload] = Field(default_factory=list)
    order_history: list[OrderHistoryPayload] = Field(default_factory=list)
