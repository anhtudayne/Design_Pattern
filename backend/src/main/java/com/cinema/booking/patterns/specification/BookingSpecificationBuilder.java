package com.cinema.booking.patterns.specification;

import com.cinema.booking.entities.Booking;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class BookingSpecificationBuilder {

    public static Specification<Booking> searchBookings(String query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (query == null || query.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();
            String searchText = query.trim().toLowerCase();

            // 1. Search by Booking ID (if query is a number and not looking like a phone number)
            try {
                if (searchText.matches("\\d+")) {
                    // Try parsing as ID
                    Integer id = Integer.parseInt(searchText);
                    predicates.add(criteriaBuilder.equal(root.get("bookingId"), id));
                }
            } catch (NumberFormatException ignored) {}

            var customerJoin = root.join("customer");
            var accountJoin = customerJoin.join("userAccount"); // To get email

            // If it contains @, search by email
            if (searchText.contains("@")) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(accountJoin.get("email")), "%" + searchText + "%"));
            } else if (searchText.matches("\\d{9,11}")) {
                // If it is 9-11 digits, it is likely a phone number. Add phone number predicate
                predicates.add(criteriaBuilder.like(customerJoin.get("phone"), "%" + searchText + "%"));
            } else {
                 // Generic search by customer's phone or email
                 predicates.add(criteriaBuilder.like(criteriaBuilder.lower(accountJoin.get("email")), "%" + searchText + "%"));
                 predicates.add(criteriaBuilder.like(customerJoin.get("phone"), "%" + searchText + "%"));
            }
            
            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }
}
