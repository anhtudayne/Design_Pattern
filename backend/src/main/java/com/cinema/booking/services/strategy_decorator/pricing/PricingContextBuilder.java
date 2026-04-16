package com.cinema.booking.services.strategy_decorator.pricing;

import com.cinema.booking.dtos.BookingCalculationDTO;
import com.cinema.booking.entities.User;
import com.cinema.booking.entities.FnbItem;
import com.cinema.booking.entities.Showtime;
import com.cinema.booking.entities.Promotion;
import com.cinema.booking.entities.Seat;
import com.cinema.booking.repositories.UserRepository;
import com.cinema.booking.repositories.FnbItemRepository;
import com.cinema.booking.repositories.SeatRepository;
import com.cinema.booking.repositories.TicketRepository;
import com.cinema.booking.security.UserDetailsImpl;
import com.cinema.booking.services.strategy_decorator.pricing.validation.PricingValidationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds orchestration {@link PricingContext} after CoR validation, reusing entities loaded by handlers.
 */
@Component
@RequiredArgsConstructor
public class PricingContextBuilder {

    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final FnbItemRepository fnbItemRepository;
    private final UserRepository userRepository;

    public PricingContext build(PricingValidationContext validationCtx, BookingCalculationDTO request) {
        Showtime showtime = validationCtx.getShowtime();
        Promotion promotion = validationCtx.getPromotion();

        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());
        List<PricingContext.ResolvedFnbItem> resolvedFnbs = resolveFnbItems(request.getFnbs(), showtime);
        User user = resolveCurrentUser();

        int bookedSeatsCount = ticketRepository.findByShowtime_ShowtimeId(request.getShowtimeId()).size();
        
        // Note: although capacity might be static in some models, calculating from seat repository 
        // linked to room is consistent with current Room entity structure.
        int totalSeatsCount = showtime.getRoom() != null
                ? seatRepository.findByRoom_RoomId(showtime.getRoom().getRoomId()).size()
                : 0;

        return PricingContext.builder()
                .showtime(showtime)
                .seats(seats)
                .resolvedFnbs(resolvedFnbs)
                .promotion(promotion)
                .user(user)
                .bookingTime(LocalDateTime.now())
                .bookedSeatsCount(bookedSeatsCount)
                .totalSeatsCount(totalSeatsCount)
                .build();
    }

    private List<PricingContext.ResolvedFnbItem> resolveFnbItems(
            List<BookingCalculationDTO.FnbOrderDTO> fnbOrders, Showtime showtime) {
        if (fnbOrders == null || fnbOrders.isEmpty()) {
            return new ArrayList<>();
        }
        List<PricingContext.ResolvedFnbItem> result = new ArrayList<>();
        for (BookingCalculationDTO.FnbOrderDTO order : fnbOrders) {
            FnbItem item = fnbItemRepository.findById(order.getItemId())
                    .orElseThrow(() -> new RuntimeException("F&B product not found: " + order.getItemId()));
            
            // Note: item.getCinema() check removed because Cinema FK was removed from FnbItem entity to match diagram.
            result.add(new PricingContext.ResolvedFnbItem(
                    item.getFnbItemId(), item.getName(), item.getPrice(), order.getQuantity()));
        }
        return result;
    }

    private User resolveCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
                return null;
            }
            Integer userId = ((UserDetailsImpl) auth.getPrincipal()).getId();
            return userRepository.findById(userId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
