package com.cinema.booking.patterns.chainofresponsibility;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CheckoutValidationConfig {

    @Bean
    public CheckoutValidationHandler checkoutValidationChain(
            MaxSeatsHandler maxSeatsHandler,
            UserExistsHandler userExistsHandler,
            ShowtimeExistsHandler showtimeExistsHandler,
            SeatsNotSoldHandler seatsNotSoldHandler) {
        
        maxSeatsHandler.setNext(userExistsHandler);
        userExistsHandler.setNext(showtimeExistsHandler);
        showtimeExistsHandler.setNext(seatsNotSoldHandler);
        
        return maxSeatsHandler;
    }
}
