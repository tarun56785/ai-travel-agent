package com.concierge.ai_travel_agent.graphqlcontroller;

import com.concierge.ai_travel_agent.entity.Trip;
import com.concierge.ai_travel_agent.repositoy.TripRepository;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@AllArgsConstructor
public class TripGraphQLController {

    private final TripRepository tripRepository;

    // MAGIC: @QueryMapping tells Spring, "When the frontend asks for 'getUserTrips'"
    @QueryMapping
    public List<Trip> getUserTrips(@Argument String email) {
        System.out.println("GRAPHQL: React asked for trips belonging to: " + email);
        return tripRepository.findByCustomerEmail(email);
    }
}
