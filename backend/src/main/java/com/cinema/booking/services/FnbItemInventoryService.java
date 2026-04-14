package com.cinema.booking.services;

import com.cinema.booking.entities.FnbItem;
import com.cinema.booking.dtos.BookingCalculationDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface FnbItemInventoryService {
    Integer getQuantity(Integer itemId);

    Map<Integer, Integer> getQuantityMap(Collection<Integer> itemIds);

    void upsertQuantity(FnbItem item, Integer quantity);

    void reserveItemsOrThrow(List<BookingCalculationDTO.FnbOrderDTO> fnbOrders);

    void releaseItemsForBooking(Integer bookingId);
}
