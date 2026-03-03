import React from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css'; // CRITICAL: Without this, the map looks scrambled!
import { useAppStore } from '../store/useAppStore';

// This is a special helper component that forces the map to fly to new coordinates smoothly
function MapUpdater({ center, zoom }) {
  const map = useMap();
  map.flyTo(center, zoom, { duration: 2 }); // 2-second smooth flying animation
  return null;
}

export default function InteractiveMap() {
  // The Map drinks directly from our Zustand water tower!
  const { mapCenter, mapZoom, destinations } = useAppStore();

  return (
    <MapContainer center={mapCenter} zoom={mapZoom} style={{ height: '100%', width: '100%' }} zoomControl={false}>
      <TileLayer
        url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
        attribution='&copy; <a href="https://carto.com/">CARTO</a>'
      />
      
      {/* This invisible component watches the state and moves the camera */}
      <MapUpdater center={mapCenter} zoom={mapZoom} />
      
      {/* Loop through our destinations and drop pins */}
      {destinations.map((dest, index) => (
        <Marker key={index} position={dest.coords}>
          <Popup>
            <strong style={{ color: 'black' }}>{dest.name}</strong>
          </Popup>
        </Marker>
      ))}
      
    </MapContainer>
  );
}