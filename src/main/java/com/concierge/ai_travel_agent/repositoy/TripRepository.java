package com.concierge.ai_travel_agent.repositoy;

import com.concierge.ai_travel_agent.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByCustomerEmail(String email);
}
