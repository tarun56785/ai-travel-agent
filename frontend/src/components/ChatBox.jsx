import React, { useState } from 'react';
import SendIcon from '@mui/icons-material/Send';
import SmartToyIcon from '@mui/icons-material/SmartToy';
import PersonIcon from '@mui/icons-material/Person';
import { useAppStore } from '../store/useAppStore'; // 1. Import our Water Tower!
import { Box, TextField, IconButton, Paper, Typography, Avatar, InputAdornment } from '@mui/material';

// A small dictionary of coordinates for our demo
const CITY_COORDS = {
  "New York": [40.7128, -74.0060],
  "Paris": [48.8566, 2.3522],
  "Tokyo": [35.6762, 139.6503],
  "London": [51.5074, -0.1278]
};

export default function ChatBox() {
  const [messages, setMessages] = useState([
    { text: "Hello! I am your AI Travel Concierge. Where would you like to go?", sender: "ai" }
  ]);
  const [input, setInput] = useState("");
  
  // 2. Grab the map controls from our Water Tower
  const { setMapCenter, getToken } = useAppStore();

  const handleSend = async () => {
    const jwtToken = getToken();
    if (!jwtToken) {
      setMessages((prev) => [...prev, { text: "You are logged out.", sender: "ai" }]);
      return;
    }
    if (input.trim() === "") return;

    setMessages((prev) => [...prev, { text: input, sender: "user" }]);
    setInput("");
    setMessages((prev) => [...prev, { text: "", sender: "ai" }]);

    try {
      const response = await fetch(`http://localhost:8081/api/secure/chat?message=${encodeURIComponent(input)}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${jwtToken}`,
          'Accept': 'text/event-stream'
        }
      });

      if (!response.ok) {
        const errorText = await response.text();
        const authError = response.headers.get('WWW-Authenticate');
        throw new Error(`The Bouncer blocked us! Status: ${response.status} - ${errorText} ${authError ? `(${authError})` : ''}`);
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let fullAiText = "";
      const triggeredCities = new Set();

      while (true) {
        const { value, done } = await reader.read();
        if (done) break; 
        
        const chunk = decoder.decode(value);
        const cleanText = chunk.replace(/data:/g, '').replace(/\n/g, '');
        fullAiText += cleanText;

        Object.keys(CITY_COORDS).forEach(city => {
          if (fullAiText.includes(city) && !triggeredCities.has(city)) {
            setMapCenter(CITY_COORDS[city], 12); 
            triggeredCities.add(city);
          }
        });

        // Update the screen with the new word
        setMessages((prev) => {
          const updated = [...prev];
          updated[updated.length - 1] = { text: fullAiText, sender: "ai" };
          return updated;
        });
      }
    } catch (error) {
      console.error("Connection error:", error);
    }
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%', width: '100%', overflow: 'hidden' }}>
      <Box sx={{ 
        flexGrow: 1, 
        overflowY: 'auto', 
        mb: 2, 
        display: 'flex', 
        flexDirection: 'column', 
        gap: 2,
        pr: 1.5,
        '&::-webkit-scrollbar': { width: '8px' },
        '&::-webkit-scrollbar-track': { background: 'transparent' },
        '&::-webkit-scrollbar-thumb': { background: 'rgba(255,255,255,0.15)', borderRadius: '10px' },
        '&::-webkit-scrollbar-thumb:hover': { background: 'rgba(255,255,255,0.3)' },
      }}>
        {messages.map((msg, index) => (
          <Box key={index} sx={{ display: 'flex', gap: 2, flexDirection: msg.sender === 'user' ? 'row-reverse' : 'row' }}>
            <Avatar sx={{ bgcolor: msg.sender === 'user' ? 'primary.main' : 'secondary.main' }}>
              {msg.sender === 'user' ? <PersonIcon /> : <SmartToyIcon />}
            </Avatar>
            <Paper sx={{ p: 2, maxWidth: '85%', bgcolor: msg.sender === 'user' ? 'primary.dark' : 'background.paper', borderRadius: 3, borderTopRightRadius: msg.sender === 'user' ? 0 : 3, borderTopLeftRadius: msg.sender === 'ai' ? 0 : 3 }}>
              <Typography variant="body1">{msg.text}</Typography>
            </Paper>
          </Box>
        ))}
      </Box>

      {/* The Input Box */}
      <Paper sx={{ p: 1, display: 'flex', alignItems: 'center', bgcolor: 'rgba(0,0,0,0.2)', borderRadius: 4, flexShrink: 0 }}>
        <TextField 
          fullWidth 
          variant="standard" 
          placeholder="Type your travel request..." 
          value={input} 
          onChange={(e) => setInput(e.target.value)} 
          onKeyPress={(e) => e.key === 'Enter' && handleSend()} 
          InputProps={{ 
            disableUnderline: true, 
            sx: { ml: 2, mr: 1 },
            endAdornment: (
              <InputAdornment position="end">
                <IconButton color="primary" onClick={handleSend}>
                  <SendIcon />
                </IconButton>
              </InputAdornment>
            )
          }} 
        />
      </Paper>
    </Box>
  );
}