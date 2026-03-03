import { createTheme } from '@mui/material/styles';

export const darkTheme = createTheme({
  palette: {
    mode: 'dark',
    background: {
      default: '#0A1929', // A very deep, sleek blue/black
      paper: 'rgba(19, 47, 76, 0.4)', // This is the start of our glass effect!
    },
    primary: {
      main: '#3399FF', // A sharp, neon-ish blue for accents
    },
  },
  shape: {
    borderRadius: 16, // Rounded corners on all our cards
  },
});