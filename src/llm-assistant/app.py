from fastapi import FastAPI

from models.AdviceRequest import AdviceRequest
from models.AdviceResponse import AdviceResponse
from utils import _build_llm_advice, _build_rule_based_advice

app = FastAPI(title="CloudStore LLM Assistant", version="1.0.0")

@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}

@app.post("/chat", response_model=AdviceResponse)
async def chat(req: AdviceRequest) -> AdviceResponse:
    llm_response = await _build_llm_advice(req)
    if llm_response is not None:
        return llm_response
    return _build_rule_based_advice(req)
