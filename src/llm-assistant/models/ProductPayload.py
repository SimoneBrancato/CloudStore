from pydantic import BaseModel

class ProductPayload(BaseModel):
    id: int
    name: str
    category: str
    price: float
    stock: int = 0
    quantity: int = 0