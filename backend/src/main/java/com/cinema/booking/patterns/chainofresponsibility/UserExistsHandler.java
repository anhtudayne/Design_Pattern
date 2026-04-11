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

    @Override
    protected void doHandle(CheckoutValidationContext context) {
        User user = userRepository.findById(context.getUserId())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        if (!(user instanceof Customer)) {
            throw new RuntimeException("Chỉ có Customer mới có thể đặt vé.");
        }

        context.setUser(user);
    }
}
