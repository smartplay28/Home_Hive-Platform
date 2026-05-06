# 🤖 HomeHive — AI/ML Integration Opportunities

> These are **real, implementable** features — not buzzword soup.  
> Each one has a clear data source, a model approach, and a resume story.

---

## 🗺️ Overview — What Data You Already Have

Before building models, map what data you collect:

| Data | Source | ML Use |
|------|--------|--------|
| Order history (service, location, time, price) | `orders` collection | Demand forecasting, pricing |
| Agent skills, location, ratings, completion rate | `service_agents` | Recommendation, matching |
| Customer service history, cart behavior | `customers` | Personalization |
| Customer reviews (text) | (to be added) | Sentiment analysis |
| Checkout → order → completion timestamps | `orders` | ETA prediction |
| Repeat orders by location + time of week | `orders` | Demand heatmaps |

---

## 🟢 TIER 1 — Quick Wins (Implement in 1-2 days each)

### 1. Smart Service Search with Semantic Similarity

**What it does:** Customer types "my fridge stopped cooling" → system maps to "AC & Appliance Repair" instead of requiring exact category selection.

**Why it matters:** Currently users must browse categories. Semantic search = better UX = more conversions.

**How to build:**

```python
# Python microservice (FastAPI)
# Use sentence-transformers (free, runs locally — no API cost)
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity
import numpy as np

model = SentenceTransformer('all-MiniLM-L6-v2')  # 80MB, runs on CPU

# Pre-embed your service catalog (do once at startup)
services = [
    {"id": "ACRepair", "text": "AC repair appliance fixing refrigerator washing machine"},
    {"id": "salon", "text": "haircut salon beauty parlour threading waxing"},
    {"id": "painting", "text": "house painting wall decor interior"},
    {"id": "cleaningpest", "text": "deep cleaning pest control cockroach termite"},
    {"id": "epc", "text": "electrician plumber carpenter leaking pipe wiring"},
    {"id": "gardening", "text": "gardening landscaping lawn mowing plants"},
]

catalog_embeddings = model.encode([s["text"] for s in services])

@app.post("/api/v1/search")
def semantic_search(query: str, top_k: int = 3):
    query_embedding = model.encode([query])
    scores = cosine_similarity(query_embedding, catalog_embeddings)[0]
    top_indices = np.argsort(scores)[::-1][:top_k]
    return [
        {"service": services[i]["id"], "score": float(scores[i])}
        for i in top_indices if scores[i] > 0.3
    ]
```

**Spring Boot integration:**
```java
@Service
public class SearchService {
    @Value("${ai.service.url}")
    private String aiServiceUrl;

    public List<ServiceMatch> semanticSearch(String query) {
        return restTemplate.postForObject(
            aiServiceUrl + "/api/v1/search?query=" + query,
            null, ServiceMatchList.class
        );
    }
}
```

**Frontend:**
```jsx
// Replace static category grid with a search bar
const SearchBar = () => {
  const { data: matches } = useMutation(
    (query) => api.post('/api/v1/search', { query })
  );

  return (
    <input
      placeholder="Describe your problem... e.g. 'my AC is not cooling'"
      onChange={debounce((e) => mutate(e.target.value), 300)}
    />
  );
};
```

**Resume bullet:** *"Implemented semantic service search using sentence-transformers (all-MiniLM-L6-v2), enabling natural language queries like 'my fridge stopped cooling' to map to correct service categories with >90% accuracy."*

---

### 2. Demand Heatmap + Forecasting Dashboard (Admin Panel)

**What it does:** Admin sees a heatmap of where orders are coming from, and the system predicts next-week demand by area so admins know where to onboard more agents.

**How to build:**

```python
# Simple time-series forecasting with Prophet (by Meta — free, works well)
from prophet import Prophet
import pandas as pd

def forecast_demand_by_location(orders_df: pd.DataFrame, location: str, days_ahead: int = 7):
    """
    orders_df: DataFrame with columns [ds (date), y (order count)]
    """
    location_df = orders_df[orders_df["location"] == location][["ds", "y"]]
    
    model = Prophet(
        seasonality_mode='multiplicative',
        weekly_seasonality=True,    # Weekends spike for home services
        daily_seasonality=False
    )
    model.fit(location_df)
    
    future = model.make_future_dataframe(periods=days_ahead)
    forecast = model.predict(future)
    
    return forecast[["ds", "yhat", "yhat_lower", "yhat_upper"]].tail(days_ahead).to_dict("records")
```

**Admin dashboard additions:**
- Choropleth heatmap of Bangalore localities (using Leaflet.js + OpenStreetMap)
- Predicted demand bars for next 7 days per location
- Alert: "Whitefield has 3x predicted demand but only 1 available AC agent"

**Data you already have:** `orders.location` + `orders.createdAt` — that's all you need.

**Resume bullet:** *"Built demand forecasting pipeline using Facebook Prophet to predict weekly service demand per Bangalore locality, enabling proactive agent recruitment decisions in the admin dashboard."*

---

### 3. Dynamic Pricing Engine

**What it does:** Service prices adjust based on real-time demand and supply (fewer agents = higher price, peak hours = surge pricing — like Ola/Uber).

**How to build (rule-based ML hybrid):**

```java
@Service
public class DynamicPricingService {
    
    public double getSurgeMultiplier(String serviceType, String location) {
        // Features
        int pendingOrders = orderRepo.countByLocationAndStatusAndServiceType(
            location, "PENDING_NOT_ASSIGNED", serviceType);
        int availableAgents = agentRepo.countAvailableByLocationAndSkill(location, serviceType);
        int hourOfDay = LocalTime.now().getHour();
        DayOfWeek day = LocalDate.now().getDayOfWeek();
        
        // Simple demand/supply ratio (interpretable, explainable)
        double demandSupplyRatio = pendingOrders == 0 ? 1.0 
            : (double) pendingOrders / Math.max(availableAgents, 1);
        
        // Peak hour multiplier (9-11am, 5-8pm are home service peaks)
        boolean isPeakHour = (hourOfDay >= 9 && hourOfDay <= 11) 
            || (hourOfDay >= 17 && hourOfDay <= 20);
        
        // Weekend multiplier
        boolean isWeekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
        
        double multiplier = 1.0
            + (Math.min(demandSupplyRatio - 1, 1.5) * 0.3)  // max +30% from demand
            + (isPeakHour ? 0.1 : 0)                         // +10% peak hours
            + (isWeekend ? 0.15 : 0);                        // +15% weekends
        
        return Math.min(multiplier, 2.0); // Cap at 2x
    }
    
    public int getDynamicPrice(String serviceId, String location) {
        int basePrice = serviceConfig.getBasePrice(serviceId);
        String serviceType = serviceConfig.getServiceType(serviceId);
        double multiplier = getSurgeMultiplier(serviceType, location);
        return (int) Math.round(basePrice * multiplier);
    }
}
```

**UI:** Show a small "🔥 High Demand — Book Now" badge with a surge meter when multiplier > 1.2.

**Resume bullet:** *"Designed dynamic pricing engine combining demand/supply ratio, peak-hour detection, and day-of-week patterns, implementing Uber-style surge pricing capped at 2x base price."*

---

## 🟡 TIER 2 — Impressive Mid-Tier (1-2 weeks each)

### 4. AI-Powered Agent Recommendation Engine

**What it does:** When a customer checks out, instead of just picking ANY nearby agent, the system recommends the **best-fit** agent based on multiple signals — like a mini recommendation system.

**The current system's flaw:** It sends notifications to ALL nearby agents with the required skill. This is noisy — agents get spammed, and orders go to whoever accepts first (not the best agent).

**Upgraded approach — Scoring Model:**

```python
# Train a simple model: "which agent assignment leads to highest rating?"
# Features: agent rating, completion rate, distance, workload, skill match count
# Target: post-service rating given (1-5 stars)

from sklearn.ensemble import GradientBoostingRegressor
import joblib

def build_agent_ranking_model(historical_data: pd.DataFrame):
    features = [
        'agent_avg_rating',
        'agent_completion_rate',    # completed / (completed + rejected)
        'distance_km',
        'agent_current_workload',   # pending orders count
        'skill_match_count',        # how many skills overlap with order
        'agent_orders_this_week',   # recency of activity
    ]
    X = historical_data[features]
    y = historical_data['post_service_rating']
    
    model = GradientBoostingRegressor(n_estimators=200, max_depth=4)
    model.fit(X, y)
    
    joblib.dump(model, 'agent_ranking_model.pkl')
    return model

def rank_agents_for_order(agents: list, order: dict, model) -> list:
    feature_rows = [extract_features(agent, order) for agent in agents]
    scores = model.predict(feature_rows)
    return sorted(zip(agents, scores), key=lambda x: x[1], reverse=True)
```

**Integration with Spring Boot:**
```java
@PostMapping("/api/v1/orders/checkout")
public ResponseEntity<ApiResponse<OrderSummary>> checkout(@RequestBody CheckoutRequest req) {
    List<ServiceAgent> candidates = agentService.findNearbyAgents(req);
    
    // Call Python ML service to rank candidates
    List<RankedAgent> rankedAgents = mlService.rankAgents(candidates, req);
    
    // Only notify top 3 (not all) — reduces agent notification spam
    rankedAgents.stream().limit(3).forEach(agent ->
        notificationService.notifyAgent(agent, order)
    );
    
    return ResponseEntity.accepted().body(ApiResponse.ok(toSummary(order)));
}
```

**Resume bullet:** *"Built agent recommendation engine using Gradient Boosting trained on historical order-to-rating pairs, replacing random agent selection with ranked notifications — reducing average post-service rating improvement by X%."*

**Interview talking point:** *"Cold start problem: for new agents with no history, we default to a content-based score using only rating=4.0 (platform average), distance, and skill match until they have 10+ completed orders."*

---

### 5. NLP Sentiment Analysis on Reviews

**What it does:** After customers leave text reviews, the system auto-tags them as positive/negative/mixed and extracts specific complaint topics (punctuality, quality, pricing). Admin dashboard shows aggregate sentiment trends.

**How to build:**

```python
# Zero-shot classification — no training data needed!
from transformers import pipeline

classifier = pipeline(
    "zero-shot-classification",
    model="facebook/bart-large-mnli"  # ~1.6GB, run once on startup
)

# Aspect-based sentiment extraction
ASPECTS = ["punctuality", "work quality", "cleanliness", "pricing", "communication"]

def analyze_review(review_text: str) -> dict:
    # Overall sentiment
    sentiment_result = classifier(review_text, ["positive", "negative", "neutral"])
    
    # Which aspects are mentioned?
    aspect_result = classifier(
        review_text, 
        ASPECTS,
        multi_label=True
    )
    
    mentioned_aspects = [
        aspect for aspect, score in zip(aspect_result["labels"], aspect_result["scores"])
        if score > 0.5
    ]
    
    return {
        "sentiment": sentiment_result["labels"][0],
        "confidence": sentiment_result["scores"][0],
        "aspects": mentioned_aspects
    }
```

**Admin Dashboard Widget:**
```
📊 Sentiment Analysis — Last 30 Days
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
😊 Positive    ████████████░  72%
😐 Neutral     ████░          18%
😞 Negative    ██░            10%

⚠️  Top complaint topic: Punctuality (34 mentions)
✅  Top praise topic: Work Quality (89 mentions)
```

**Resume bullet:** *"Integrated zero-shot NLP sentiment analysis using facebook/bart-large-mnli for aspect-based review categorization across 5 service quality dimensions (punctuality, quality, pricing, cleanliness, communication)."*

---

### 6. Fraud / Anomaly Detection

**What it does:** Detect suspicious behavior — fake agents who accept but never complete, customers who order and dispute payments, duplicate account creation.

**How to build:**

```python
from sklearn.ensemble import IsolationForest
import numpy as np

def detect_agent_fraud(agent_metrics: pd.DataFrame) -> list:
    """
    Flags agents with anomalous behavior patterns
    """
    features = [
        'acceptance_rate',          # accepted / received
        'completion_rate',          # completed / accepted
        'avg_response_time_mins',   # time from notification to accept/reject
        'dispute_rate',             # orders disputed by customers
        'rating_variance',          # inconsistent ratings = suspicious
    ]
    
    model = IsolationForest(contamination=0.05, random_state=42)  # 5% anomaly rate
    model.fit(agent_metrics[features])
    
    anomaly_scores = model.decision_function(agent_metrics[features])
    agent_metrics['anomaly_score'] = anomaly_scores
    agent_metrics['is_suspicious'] = model.predict(agent_metrics[features]) == -1
    
    return agent_metrics[agent_metrics['is_suspicious']].to_dict('records')
```

**Admin Action:** Flagged agents get reviewed before their next order assignment is processed.

---

## 🔴 TIER 3 — Advanced (2-4 weeks, very impressive)

### 7. ETA Prediction Model

**What it does:** When an agent accepts an order, predict realistic completion time based on service type, location, agent's past speed, and time of day — not just a hardcoded "2 hours".

**Training data:** `order.agentAcceptedAt` → `order.completedAt` timestamps (start collecting now).

```python
from sklearn.ensemble import RandomForestRegressor

def train_eta_model(orders_with_completion: pd.DataFrame):
    features = [
        'service_type_encoded',      # AC repair, salon, etc.
        'hour_of_day',               # morning vs evening
        'day_of_week',
        'agent_avg_completion_time', # historical average for this agent
        'location_encoded',          # some areas have more traffic
        'num_services_in_order',     # single vs multi-service order
    ]
    X = orders_with_completion[features]
    y = orders_with_completion['actual_duration_minutes']
    
    model = RandomForestRegressor(n_estimators=300, min_samples_leaf=5)
    model.fit(X, y)
    
    return model

# Returns: {"estimated_minutes": 87, "confidence_interval": [70, 110]}
```

**Frontend display:**
```
✅ Agent Assigned: Raj Kumar (⭐4.8)
⏱️  Estimated completion: 1h 27min (between 1h 10min – 1h 50min)
📍 Agent is 3.2km away
```

---

## 🏗️ AI/ML Architecture

```
┌─────────────────────────────────────────────────────┐
│              Spring Boot (Java)                     │
│  - Business logic, auth, orders, websockets         │
│  - Calls AI service for ML predictions              │
└──────────────────┬──────────────────────────────────┘
                   │ HTTP (internal)
┌──────────────────▼──────────────────────────────────┐
│         FastAPI ML Microservice (Python)            │
│  /search      → Semantic search                     │
│  /rank-agents → Agent recommendation                │
│  /sentiment   → Review NLP                          │
│  /eta         → Completion time prediction          │
│  /fraud       → Anomaly detection                   │
└──────────────────┬──────────────────────────────────┘
                   │ Model artifacts
┌──────────────────▼──────────────────────────────────┐
│         Model Storage                               │
│  Local filesystem / S3 / MLflow registry            │
└─────────────────────────────────────────────────────┘
```

**Why a separate Python microservice?**
- Java ML libraries (DL4J, Smile) are immature vs Python ecosystem
- Models update independently from business logic
- Can scale ML service separately (GPU inference if needed)
- Clean separation: Spring Boot is "smart orchestrator", Python is "brain"

---

## 📊 Data Pipeline (Start Collecting Now)

Even before models are trained, **start logging these events** in a new `events` collection:

```java
@Document(collection = "events")
public class AppEvent {
    private String eventType;        // ORDER_PLACED, AGENT_NOTIFIED, AGENT_ACCEPTED, etc.
    private Map<String, Object> data; // flexible payload
    private Instant timestamp;
    private String sessionId;
}

// Log everywhere
eventService.log("SERVICE_VIEWED", Map.of("serviceId", "ac_001", "customerId", customerId));
eventService.log("CART_ADDED", Map.of("serviceId", "ac_001", "customerId", customerId));
eventService.log("ORDER_PLACED", Map.of("orderId", order.getOrderId(), ...));
eventService.log("AGENT_ACCEPTED", Map.of("agentId", agentId, "responseTimeMs", elapsed));
```

This gives you a **behavioral dataset** for training future models.

---

## 📝 Resume Bullets (AI/ML Additions)

- **Built semantic service search** using sentence-transformers, enabling natural language queries ("my fridge is not cooling") to map to correct service categories with cosine similarity scoring
- **Designed agent recommendation engine** using Gradient Boosting on historical rating data, replacing random agent selection with quality-ranked notifications to top 3 best-fit agents
- **Implemented dynamic surge pricing** combining demand/supply ratio, time-of-day, and day-of-week signals, replicating Uber-style pricing with configurable 2x cap
- **Integrated zero-shot NLP review analysis** using facebook/bart-large-mnli for aspect-based sentiment classification (punctuality, quality, pricing) without labeled training data
- **Deployed demand forecasting** with Facebook Prophet on time-series order data to predict next-week demand by Bangalore locality for agent recruitment decisions
- **Architected AI/ML microservice** (FastAPI + Python) serving 5 ML endpoints consumed by Spring Boot via HTTP, enabling independent model versioning and deployment

---

## 🗓️ Implementation Priority

| Priority | Feature | Effort | Resume Impact |
|----------|---------|--------|---------------|
| 🥇 1st | Semantic Search | 1 day | ⭐⭐⭐⭐⭐ |
| 🥈 2nd | Dynamic Pricing | 2 days | ⭐⭐⭐⭐⭐ |
| 🥉 3rd | Demand Heatmap | 2 days | ⭐⭐⭐⭐ |
| 4th | NLP Reviews | 3 days | ⭐⭐⭐⭐⭐ |
| 5th | Agent Ranker | 1 week | ⭐⭐⭐⭐⭐ |
| 6th | ETA Prediction | 2 weeks | ⭐⭐⭐⭐ |
| 7th | Fraud Detection | 1 week | ⭐⭐⭐⭐ |

---

## ❓ Interview Q&A for Each Feature

**Q: How do you handle the cold start problem in your recommendation system?**
> "New agents have no rating history. We use a hybrid approach — content-based scoring using only skill match count and distance until the agent has 10+ completed orders, then gradually blend in the collaborative signal as data accumulates."

**Q: Why sentence-transformers and not OpenAI embeddings?**
> "Cost and data privacy. `all-MiniLM-L6-v2` runs locally at 80MB, costs ₹0, and keeps user search queries on-premise. OpenAI would add per-query cost and send customer data to a third party. For a city-specific service app, local inference is the right call."

**Q: How would you A/B test the dynamic pricing model?**
> "Feature-flag split: 50% of users see dynamic pricing, 50% see static. Track: checkout conversion rate, revenue per order, and agent acceptance rate. Hold for 2 weeks minimum. Roll back if checkout conversion drops >5%."

**Q: What metrics would you use to evaluate the agent recommendation model?**
> "Primary: post-service rating improvement vs random selection (offline evaluation on holdout set). Secondary: order acceptance rate (if top-ranked agents accept more, model is good). Tertiary: time-to-acceptance (faster = better matching)."
