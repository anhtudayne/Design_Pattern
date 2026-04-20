package com.cinema.booking.pattern.strategy.pricing;

import com.cinema.booking.dto.BookingCalculationDTO;
import com.cinema.booking.entity.Customer;
import com.cinema.booking.entity.FnbItem;
import com.cinema.booking.entity.Promotion;
import com.cinema.booking.entity.Seat;
import com.cinema.booking.entity.Showtime;
import com.cinema.booking.repository.CustomerRepository;
import com.cinema.booking.repository.FnbItemRepository;
import com.cinema.booking.repository.SeatRepository;
import com.cinema.booking.repository.TicketRepository;
import com.cinema.booking.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Component
@RequiredArgsConstructor
public class PricingContextBuilder {

    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final FnbItemRepository fnbItemRepository;
    private final CustomerRepository customerRepository;

    public PricingContext build(com.cinema.booking.pattern.chain.PricingValidationContext validationCtx, BookingCalculationDTO request) {
        if (validationCtx.getShowtime() == null) {
            throw new IllegalArgumentException("Showtime must be populated by validation chain before building PricingContext");
        }
        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new IllegalArgumentException("Seat IDs must be validated by validation chain before building PricingContext");
        }

        Showtime showtime = validationCtx.getShowtime();
        Promotion promotion = validationCtx.getPromotion();

        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());
        List<PricingContext.FnbItemQuantity> fnbItems = resolveFnbItems(request.getFnbs());
        Customer customer = resolveCurrentCustomer();

        int bookedSeatsCount = ticketRepository.findByShowtime_ShowtimeId(request.getShowtimeId()).size();
        int totalSeatsCount = showtime.getRoom() != null
                ? seatRepository.findByRoom_RoomId(showtime.getRoom().getRoomId()).size()
                : 0;

        return PricingContext.builder()
                .showtime(showtime)
                .seats(seats)
                .fnbItems(fnbItems)
                .promotion(promotion)
                .customer(customer)
                .bookingTime(LocalDateTime.now())
                .bookedSeatsCount(bookedSeatsCount)
                .totalSeatsCount(totalSeatsCount)
                .build();
    }

    private List<PricingContext.FnbItemQuantity> resolveFnbItems(
            List<BookingCalculationDTO.FnbOrderDTO> fnbOrders) {
        if (fnbOrders == null || fnbOrders.isEmpty()) {
            return new ArrayList<>();
        }
        List<PricingContext.FnbItemQuantity> result = new ArrayList<>();
        for (BookingCalculationDTO.FnbOrderDTO order : fnbOrders) {
            FnbItem item = fnbItemRepository.findById(order.getItemId())
                    .orElseThrow(() -> new RuntimeException("F&B product not found: " + order.getItemId()));
            result.add(PricingContext.FnbItemQuantity.builder()
                    .fnbItem(item)
                    .quantity(order.getQuantity())
                    .build());
        }
        return result;
    }

    private Customer resolveCurrentCustomer() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
                return null;
            }
            Integer userId = ((UserDetailsImpl) auth.getPrincipal()).getId();
            return customerRepository.findById(userId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
