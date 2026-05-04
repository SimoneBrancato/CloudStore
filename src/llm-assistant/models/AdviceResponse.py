from pydantic import BaseModel
from typing import Any

class AdviceResponse(BaseModel):
    message: str
    suggestions: list[dict[str, Any]]
    source: str