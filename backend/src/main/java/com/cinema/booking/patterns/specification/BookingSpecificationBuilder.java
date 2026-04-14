package com.cinema.booking.patterns.specification;

import com.cinema.booking.entities.Booking;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.JoinType;
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

            // 1. Search by Booking ID
            try {
                if (searchText.matches("\\d+")) {
                    Integer id = Integer.parseInt(searchText);
                    predicates.add(criteriaBuilder.equal(root.get("bookingId"), id));
                }
            } catch (NumberFormatException ignored) {}

            // Use LEFT JOIN to avoid filtering out Bookings without registered Customer/UserAccount (e.g., Guest checkout)
            var customerJoin = root.join("customer", JoinType.LEFT);
            var accountJoin = customerJoin.join("userAccount", JoinType.LEFT);

            // 2. Search by Email
            if (searchText.contains("@")) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(accountJoin.get("email")), "%" + searchText + "%"));
            } 
            
            // 3. Search by Phone
            if (searchText.matches("\\d{9,11}")) {
                predicates.add(criteriaBuilder.like(customerJoin.get("phone"), "%" + searchText + "%"));
            }

            // 4. Fallback: search both fields if not specific
            if (!searchText.contains("@") && !searchText.matches("\\d+")) {
                 predicates.add(criteriaBuilder.like(criteriaBuilder.lower(accountJoin.get("email")), "%" + searchText + "%"));
                 predicates.add(criteriaBuilder.like(customerJoin.get("phone"), "%" + searchText + "%"));
            }
            
            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }
}
