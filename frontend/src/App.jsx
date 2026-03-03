import React, { useEffect, useState, useRef } from 'react';
import { ThemeProvider, CssBaseline, Box, Typography, CircularProgress } from '@mui/material';
import { darkTheme } from './theme/AppTheme';
import ChatBox from './components/ChatBox';
import InteractiveMap from './components/InteractiveMap';
import SavedTripsCard from './components/SavedTripsCard';
import { useAppStore } from './store/useAppStore';
import keycloak from './keycloak';

function App() {
  const { isAuthenticated, setKeycloak } = useAppStore();
  const [isInitializing, setIsInitializing] = useState(true);
  const isRun = useRef(false);

  useEffect(() => {
    if (isRun.current) return;
    isRun.current = true;

    // When the app starts, we check if they already have a valid session with Keycloak
    keycloak.init({ onLoad: 'check-sso', pkceMethod: 'S256' })
      .then((authenticated) => {
        setKeycloak(keycloak, authenticated);
        setIsInitializing(false);
        
        // If they are not logged in, immediately redirect them to the Keycloak server!
        if (!authenticated) {
          keycloak.login();
        }
      })
      .catch(console.error);
  }, [setKeycloak]);

  // Show a loading spinner while Keycloak figures out who the user is
  if (isInitializing || !isAuthenticated) {
    return (
      <Box sx={{ height: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: '#0A1929' }}>
        <CircularProgress />
      </Box>
    );
  }

  // If they ARE authenticated, show the beautiful dashboard!
  return (
    <ThemeProvider theme={darkTheme}>
      <CssBaseline /> 
      <Box sx={{ display: 'flex', height: '100vh', width: '100vw', overflow: 'hidden' }}>
        <Box sx={{ width: '450px', flexShrink: 0, p: 3, display: 'flex', flexDirection: 'column', borderRight: '1px solid rgba(255,255,255,0.1)' }}>
          <Typography variant="h5" sx={{ fontWeight: 'bold', mb: 2, color: 'primary.main' }}>
            AI Concierge
          </Typography>
          <ChatBox />
        </Box>
        <Box sx={{ flexGrow: 1, position: 'relative', bgcolor: '#000' }}>
          <Box sx={{ height: '100%', width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
             <InteractiveMap />
          </Box>
          <SavedTripsCard />
        </Box>
      </Box>
    </ThemeProvider>
  );
}

export default App;