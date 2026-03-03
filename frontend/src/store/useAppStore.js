import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export const useAppStore = create((set, get) => ({
  // --- Map State ---
  mapCenter: [20.0, 0.0], 
  mapZoom: 3, 
  destinations: [], 
  setMapCenter: (coords, zoom) => set({ mapCenter: coords, mapZoom: zoom }),
  addDestination: (dest) => set((state) => ({ destinations: [...state.destinations, dest] })),

  // --- NEW: Keycloak Security State ---
  keycloak: null,
  isAuthenticated: false,
  
  // We save the entire Keycloak instance to the tower!
  setKeycloak: (kc, authenticated) => set({ keycloak: kc, isAuthenticated: authenticated }),
  
  // A helper function to always grab the freshest token right before we send an API request
  getToken: () => {
    const kc = get().keycloak;
    return kc ? kc.token : null;
  },

  // --- GraphQL State ---
  savedTrips: [],
  fetchSavedTrips: async (email) => {
    const token = get().getToken(); // Use the new helper!
    if (!token) return;

    try {
      const response = await fetch('http://localhost:8081/graphql', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}` 
        },
        body: JSON.stringify({
          query: `query { getUserTrips(email: "${email}") { destination status } }`
        })
      });
      const result = await response.json();
      if (result.data && result.data.getUserTrips) {
        set({ savedTrips: result.data.getUserTrips });
      }
    } catch (error) {
      console.error("GraphQL Fetch Error:", error);
    }
  }
}));