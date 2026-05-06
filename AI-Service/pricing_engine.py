import datetime
import math
from zoneinfo import ZoneInfo  # Python 3.9+ stdlib (no extra deps)

class DynamicPricingEngine:
    def __init__(self):
        # Base multiplier is 1.0 (standard price)
        self.base_multiplier = 1.0
        
    def calculate_price_multiplier(self, service_id: str, location: str, demand_supply_ratio: float) -> dict:
        """
        Calculates the dynamic surge multiplier (surge pricing) based on:
        1. Time of day (peak hours: 8am-10am, 6pm-8pm)
        2. Real-time demand/supply ratio (from backend)
        3. Day of week (weekends have slight premium)
        """
        IST = ZoneInfo("Asia/Kolkata")
        now = datetime.datetime.now(IST)  # Always Bangalore time, regardless of server location
        hour = now.hour
        is_weekend = now.weekday() >= 5
        
        multiplier = self.base_multiplier
        reasons = []
        
        # 1. Time of day surge (Rush hour)
        if (8 <= hour <= 10) or (18 <= hour <= 20):
            multiplier += 0.15
            reasons.append("Peak hours")
        elif (22 <= hour <= 23) or (0 <= hour <= 6):
            # Late night premium
            multiplier += 0.25
            reasons.append("Late night service")
            
        # 2. Weekend surge
        if is_weekend:
            multiplier += 0.10
            reasons.append("Weekend demand")
            
        # 3. Supply/Demand Ratio surge
        # Ratio = Active Orders / Available Agents. 
        # If Ratio > 1.0, there are more orders than agents (High Demand)
        if demand_supply_ratio > 1.5:
            surge = min(0.5, (demand_supply_ratio - 1.0) * 0.2) # Max 50% surge from supply
            multiplier += surge
            reasons.append("High demand in your area")
        elif demand_supply_ratio < 0.5:
            # Low demand discount!
            multiplier -= 0.10
            reasons.append("Low demand discount")
            
        # Hard limits on multiplier to protect customer experience
        multiplier = max(0.8, min(multiplier, 2.0))
        
        return {
            "serviceId": service_id,
            "location": location,
            "finalMultiplier": round(multiplier, 2),
            "surgeReasons": reasons,
            "timestamp": now.isoformat()
        }

# Singleton instance
pricing_engine = DynamicPricingEngine()
