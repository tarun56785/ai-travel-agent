import React, { useEffect } from 'react';
import { Paper, Typography, Box, Divider, IconButton } from '@mui/material';
import FlightTakeoffIcon from '@mui/icons-material/FlightTakeoff';
import RefreshIcon from '@mui/icons-material/Refresh';
import { useAppStore } from '../store/useAppStore';

export default function SavedTripsCard() {
  const { savedTrips, fetchSavedTrips } = useAppStore();

  const userEmail = "traveler@example.com";
  
  useEffect(() => {
    fetchSavedTrips(userEmail);
  }, [fetchSavedTrips]);

  return (
    <Paper 
      sx={{ 
        position: 'absolute', 
        top: 20, 
        right: 20, 
        width: 320, 
        maxHeight: 400,
        overflowY: 'auto',
        p: 2, 
        backdropFilter: 'blur(12px)', // Frosted glass effect
        bgcolor: 'rgba(19, 47, 76, 0.6)',
        border: '1px solid rgba(255,255,255,0.2)',
        borderRadius: 3,
        zIndex: 1000 // Ensure it floats ABOVE the Leaflet map!
      }}
    >
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
        <Typography variant="h6" color="primary.main" fontWeight="bold">
          Saved Itineraries
        </Typography>
        <IconButton size="small" color="primary" onClick={() => fetchSavedTrips(userEmail)}>
          <RefreshIcon />
        </IconButton>
      </Box>
      <Divider sx={{ borderColor: 'rgba(255,255,255,0.1)', mb: 2 }} />

      {savedTrips.length === 0 ? (
        <Typography variant="body2" color="text.secondary">No saved trips yet. Ask the AI to finalize a trip!</Typography>
      ) : (
        savedTrips.map((trip, index) => (
          <Box key={index} sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2, p: 1.5, bgcolor: 'rgba(0,0,0,0.3)', borderRadius: 2 }}>
            <FlightTakeoffIcon color="secondary" />
            <Box>
              <Typography variant="body1" fontWeight="bold">{trip.destination}</Typography>
              <Typography variant="caption" color="text.secondary">Status: {trip.status}</Typography>
            </Box>
          </Box>
        ))
      )}
    </Paper>
  );
}