package com.concierge.ai_travel_agent.dto;

public record TripFinalizedEvent(String customerEmail, String destination, String flightDetails) {
}
