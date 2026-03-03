package com.concierge.ai_travel_agent.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TravelTools {

    // 1. We grab your secret API key and URL from the Vault (application.yml)
    @Value("${openweather.api-key}")
    private String apiKey;

    @Value("${openweather.url}")
    private String apiUrl;

    @Value("${amadeus.client-id}")
    private String amadeusClientId;

    @Value("${amadeus.client-secret}")
    private String amadeusClientSecret;

    @Value("${amadeus.auth-url}")
    private String amadeusAuthUrl;

    @Value("${amadeus.search-url}")
    private String amadeusSearchUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // 1. Inject the KafkaTemplate
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Tool(description = "Get the current weather conditions for a specific city")
    // MAGIC: This tells Spring to save the result in Redis under the "weather" bucket, using the city name as the key.
    public String getWeather(@ToolParam(description = "The name of the city, e.g., Paris, Tokyo") String city) {
        System.out.println("TOOL: AI is fetching LIVE weather for: " + city);

        try {
            if (apiKey == null || apiUrl == null) {
                return "Configuration Error: OpenWeatherMap API key or URL is null. Check application.yaml.";
            }

            // Trim the key to remove any accidental whitespace from application.yaml
            String cleanKey = apiKey.trim();
            String requestUrl = apiUrl + "?q={city}&APPID={apiKey}&units=imperial";

            // 4. Send the GET request and catch the JSON response in a Map
            Map<String, Object> response = restTemplate.getForObject(requestUrl, Map.class, city, cleanKey);

            if (response != null && response.containsKey("main") && response.containsKey("weather")) {
                // 5. Dig into the JSON to extract the temperature and description
                Map<String, Object> main = (Map<String, Object>) response.get("main");
                List<Map<String, Object>> weatherList = (List<Map<String, Object>>) response.get("weather");

                double temp = Double.parseDouble(main.get("temp").toString());
                String description = weatherList.get(0).get("description").toString();

                String liveWeather = "The live weather in " + city + " is currently " + temp + "°F with " + description + ".";
                System.out.println("TOOL: Successfully fetched: " + liveWeather);

                // 6. Hand the real data back to the AI so it can formulate its sentence!
                return liveWeather;
            }
        } catch (HttpClientErrorException e) {
            System.err.println("TOOL: OpenWeatherMap API Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return "I couldn't reach the weather service. The API returned: " + e.getStatusText();
        } catch (Exception e) {
            System.err.println("TOOL: Failed to fetch live weather (Key might still be activating!): " + e.getMessage());
            // TEMPORARY DEBUG: Return the actual error to the chat so we can see it without console logs
            return "I am sorry, but I couldn't reach the live weather service. Debug Error: " + e.toString();
        }

        return "Weather data is currently unavailable for " + city + ".";
    }

    // The Live Flight Search Tool
    @Tool(description = "Search for real-time flight prices and offers between two cities.")
    public String searchFlights(
            @ToolParam(description = "The 3-letter IATA airport code for the departure city (e.g., JFK, LHR, CDG)") String origin,
            @ToolParam(description = "The 3-letter IATA airport code for the destination city (e.g., HND, SYD, DFW)") String destination,
            @ToolParam(description = "The departure date in strict YYYY-MM-DD format (e.g., 2026-03-15)") String departureDate
    ) {
        System.out.println("TOOL: AI is searching LIVE flights from " + origin + " to " + destination + " on " + departureDate);

        // 1. Get the VIP Wristband
        String token = getAmadeusAccessToken();
        if (token == null) return "I could not authenticate with the airline database.";

        // 2. Ask for the flights using the wristband (asking for 1 adult, max 2 results to keep it fast)
        String url = amadeusSearchUrl + "?originLocationCode=" + origin + "&destinationLocationCode=" + destination + "&departureDate=" + departureDate + "&adults=1&max=2";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token); // Attach the VIP Wristband!
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            Map<String, Object> responseBody = responseEntity.getBody();

            if (responseBody != null && responseBody.containsKey("data")) {
                List<Map<String, Object>> flights = (List<Map<String, Object>>) responseBody.get("data");

                if (flights.isEmpty()) return "No flights found for " + origin + " to " + destination + " on " + departureDate + ".";

                // 3. Extract the price of the first flight found
                Map<String, Object> firstFlight = flights.get(0);
                Map<String, Object> priceMap = (Map<String, Object>) firstFlight.get("price");
                String total = priceMap.get("total").toString();
                String currency = priceMap.get("currency").toString();

                String liveFlightResult = "Found a live flight from " + origin + " to " + destination + " for " + total + " " + currency + ".";
                System.out.println("TOOL: Successfully fetched: " + liveFlightResult);

                return liveFlightResult;
            }
        } catch (Exception e) {
            System.err.println("TOOL: Flight search failed: " + e.getMessage());
            return "I am sorry, but the live flight system returned an error for that route.";
        }

        return "Flight data is currently unavailable.";
    }

    // Helper Method: The B2B Handshake to get the VIP Wristband
    private String getAmadeusAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=client_credentials&client_id=" + amadeusClientId + "&client_secret=" + amadeusClientSecret;
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(amadeusAuthUrl, request, Map.class);
            return response.get("access_token").toString();
        } catch (Exception e) {
            System.err.println("AMADEUS AUTH FAILED: " + e.getMessage());
            return null;
        }
    }

    @Tool(description = "Finalize the trip and send the itinerary to the background fulfillment worker")
    public String finalizeTrip(
            @ToolParam(description = "The user's email address") String email,
            @ToolParam(description = "The destination city") String destination,
            @ToolParam(description = "The flight details") String flightDetails) {

        System.out.println("WAITER: AI decided to finalize the trip for " + email + ". Pinning ticket to Kafka...");

        // 2. Create the ticket string (In a real app, we would convert the TripFinalizedEvent to JSON)
        String ticket = "Email: " + email + " | Dest: " + destination + " | Flight: " + flightDetails;

        // 3. Pin the ticket to the "trip-events" wheel (Topic)
        kafkaTemplate.send("trip-events", ticket);

        return "Trip successfully queued for finalization. The background worker is generating the PDF and email now.";
    }
}
