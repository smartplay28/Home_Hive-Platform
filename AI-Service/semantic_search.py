import numpy as np
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity
import json

class SemanticSearchEngine:
    def __init__(self):
        # Using a fast, lightweight, and highly capable pre-trained model for sentence embeddings
        print("Loading SentenceTransformer model... (this may take a few seconds on first run)")
        self.model = SentenceTransformer('all-MiniLM-L6-v2')
        
        # Define the taxonomy of HomeHive services
        self.services = [
            {"id": "ACRepair", "name": "AC Repair & Maintenance", "description": "Fixing air conditioning units, gas refilling, split and window AC servicing."},
            {"id": "Plumbing", "name": "Plumbing Services", "description": "Fixing leaking pipes, blocked drains, installing taps, toilets, and water heaters."},
            {"id": "Electrician", "name": "Electrician", "description": "Wiring repair, fixing short circuits, installing fans, lights, switches, and MCB boards."},
            {"id": "Cleaning", "name": "Deep Home Cleaning", "description": "Full home deep cleaning, bathroom cleaning, sofa cleaning, and carpet vacuuming."},
            {"id": "PestControl", "name": "Pest Control", "description": "Extermination of cockroaches, bed bugs, termites, mosquitoes, and rodents."},
            {"id": "Salon", "name": "Salon for Women/Men", "description": "Haircut, styling, facial, massage, waxing, threading, and pedicure at home."},
            {"id": "Carpentry", "name": "Carpentry", "description": "Furniture repair, assembling beds, fixing doors, and making wooden cabinets."}
        ]
        
        # Pre-compute embeddings for all service descriptions for fast inference
        print("Pre-computing service embeddings...")
        corpus = [f"{s['name']}: {s['description']}" for s in self.services]
        self.service_embeddings = self.model.encode(corpus)
        print("Semantic Search Engine initialized successfully.")

    def search(self, query: str, top_k: int = 3):
        """
        Takes a natural language query (e.g. "my sink is leaking water everywhere")
        and returns the top-k matching services using cosine similarity on BERT embeddings.
        """
        if not query.strip():
            return []

        # Encode the user query
        query_embedding = self.model.encode([query])
        
        # Calculate cosine similarity against all known services
        similarities = cosine_similarity(query_embedding, self.service_embeddings)[0]
        
        # Get indices of top k scores
        top_indices = np.argsort(similarities)[::-1][:top_k]
        
        results = []
        for idx in top_indices:
            score = float(similarities[idx])
            # Only return matches with a reasonable confidence threshold
            if score > 0.15:
                results.append({
                    "serviceId": self.services[idx]["id"],
                    "serviceName": self.services[idx]["name"],
                    "confidence": score
                })
                
        return results

# Singleton instance
search_engine = SemanticSearchEngine()
