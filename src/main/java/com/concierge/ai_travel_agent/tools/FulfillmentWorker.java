package com.concierge.ai_travel_agent.tools;

import com.concierge.ai_travel_agent.entity.Trip;
import com.concierge.ai_travel_agent.repositoy.TripRepository;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FulfillmentWorker {

    private final TripRepository tripRepository;

    @KafkaListener(topics = "trip-events", groupId = "concierge-fulfillment-group")
    public void processTripFinalization(String ticket) {
        System.out.println("CHEF (Worker): Picked up a new ticket from Kafka! " + ticket);

        try {
            String[] parts = ticket.split("\\|");
            String email = parts[0].replace("Email: ", "").trim();
            String dest = parts[1].replace("Dest: ", "").trim();
            String flight = parts[2].replace("Flight: ", "").trim();

            Trip newTrip = new Trip(email, dest, flight);
            tripRepository.save(newTrip);

            System.out.println("CHEF (Worker): Successfully saved " + dest + " trip for " + email + " to PostgreSQL database!");
        } catch (Exception e) {
            System.err.println("CHEF (Worker): Failed to parse and save ticket: " + e.getMessage());
        }
    }
}
