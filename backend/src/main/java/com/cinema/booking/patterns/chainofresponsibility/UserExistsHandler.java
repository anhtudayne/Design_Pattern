package com.cinema.booking.patterns.chainofresponsibility;

import com.cinema.booking.entities.Customer;
import com.cinema.booking.entities.User;
import com.cinema.booking.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserExistsHandler extends AbstractCheckoutValidationHandler {

    private final UserRepository userRepository;
    private final com.cinema.booking.repositories.CustomerRepository customerRepository;

    @Override
    protected void doHandle(CheckoutValidationContext context) {
        User user = userRepository.findById(context.getUserId())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        if (!(user instanceof Customer)) {
            // Khách vãng lai mua tại quầy. Áp dụng Mẫu Default Object (Khách Vãng Lai).
            user = getOrCreateWalkInGuest();
        }

        context.setUser(user);
    }

    private Customer getOrCreateWalkInGuest() {
        return customerRepository.findAll().stream()
                .filter(c -> "0000000000".equals(c.getPhone()))
                .findFirst()
                .orElseGet(() -> {
                    Customer guest = new Customer();
                    guest.setFullname("Khách Vãng Lai");
                    guest.setPhone("0000000000");
                    guest.setTotalSpending(java.math.BigDecimal.ZERO);
                    guest.setLoyaltyPoints(0);
                    return customerRepository.save(guest);
                });
    }
}
