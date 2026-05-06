import os
from fastapi import FastAPI, HTTPException, Security, Depends
from fastapi.security.api_key import APIKeyHeader
from pydantic import BaseModel
from typing import List

# Import our ML modules
from semantic_search import search_engine
from pricing_engine import pricing_engine

app = FastAPI(
    title="HomeHive AI/ML Microservice",
    description="Provides Semantic Search and Dynamic Pricing to the HomeHive backend.",
    version="1.0.0"
)

# ─── API Key Security ───────────────────────────────────────────────────────
# Set AI_SERVICE_API_KEY in your environment. The Spring Boot backend must
# send this in the X-Api-Key header on every request.
API_KEY = os.getenv("AI_SERVICE_API_KEY")
api_key_header = APIKeyHeader(name="X-Api-Key", auto_error=False)

async def require_api_key(key: str = Security(api_key_header)):
    if not API_KEY:
        # No key configured — skip auth (useful for local dev without env var)
        return
    if key != API_KEY:
        raise HTTPException(status_code=403, detail="Invalid or missing API key.")

# ─── Pydantic Models ────────────────────────────────────────────────────────

class SearchRequest(BaseModel):
    query: str
    top_k: int = 3

class SearchResult(BaseModel):
    serviceId: str
    serviceName: str
    confidence: float

class PricingRequest(BaseModel):
    serviceId: str
    location: str
    demandSupplyRatio: float = 1.0

# ─── Endpoints ─────────────────────────────────────────────────────────────

@app.get("/health")
def health_check():
    return {"status": "up", "service": "HomeHive-AI"}

@app.post("/api/v1/ai/search", response_model=List[SearchResult], dependencies=[Depends(require_api_key)])
def semantic_search(request: SearchRequest):
    """
    Takes a natural language user query and maps it to HomeHive services.
    E.g. "I want to install a ceiling fan" -> Electrician (confidence 0.89)
    """
    try:
        results = search_engine.search(request.query, request.top_k)
        return results
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/v1/ai/pricing", dependencies=[Depends(require_api_key)])
def get_dynamic_price(request: PricingRequest):
    """
    Calculates the current surge multiplier based on time, location, and network demand.
    Backend calls this before showing the final price to the customer.
    """
    try:
        result = pricing_engine.calculate_price_multiplier(
            request.serviceId, 
            request.location, 
            request.demandSupplyRatio
        )
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    # Run server on port 8000
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
