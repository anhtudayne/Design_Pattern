package com.cinema.booking.services.impl;

import com.cinema.booking.dtos.BookingCalculationDTO;
import com.cinema.booking.entities.Booking;
import com.cinema.booking.entities.FnBLine;
import com.cinema.booking.entities.FnbItem;
import com.cinema.booking.entities.FnbItemInventory;
import com.cinema.booking.repositories.BookingRepository;
import com.cinema.booking.repositories.FnBLineRepository;
import com.cinema.booking.repositories.FnbItemInventoryRepository;
import com.cinema.booking.services.FnbItemInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FnbItemInventoryServiceImpl implements FnbItemInventoryService {

    private final FnbItemInventoryRepository inventoryRepository;
    private final BookingRepository bookingRepository;
    private final FnBLineRepository fnBLineRepository;

    @Override
    @Transactional(readOnly = true)
    public Integer getQuantity(Integer itemId) {
        return inventoryRepository.findByItem_ItemId(itemId)
                .map(FnbItemInventory::getQuantity)
                .orElse(0);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, Integer> getQuantityMap(Collection<Integer> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return inventoryRepository.findAll().stream()
                .filter(inv -> inv.getItem() != null && inv.getItem().getItemId() != null)
                .filter(inv -> itemIds.contains(inv.getItem().getItemId()))
                .collect(Collectors.toMap(inv -> inv.getItem().getItemId(), FnbItemInventory::getQuantity, (a, b) -> b));
    }

    @Override
    @Transactional
    public void upsertQuantity(FnbItem item, Integer quantity) {
        int safeQty = quantity == null ? 0 : quantity;
        FnbItemInventory inventory = inventoryRepository.findByItem_ItemId(item.getItemId())
                .orElseGet(() -> FnbItemInventory.builder().item(item).build());
        inventory.setQuantity(safeQty);
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public void reserveItemsOrThrow(List<BookingCalculationDTO.FnbOrderDTO> fnbOrders) {
        if (fnbOrders == null || fnbOrders.isEmpty()) {
            return;
        }

        for (BookingCalculationDTO.FnbOrderDTO order : fnbOrders) {
            Integer itemId = order.getItemId();
            int requestQty = order.getQuantity() == null ? 0 : order.getQuantity();
            if (requestQty <= 0) {
                continue;
            }

            FnbItemInventory inventory = inventoryRepository.findByItemIdForUpdate(itemId)
                    .orElseThrow(() -> new RuntimeException("Chưa cấu hình tồn kho cho F&B item: " + itemId));
            int current = inventory.getQuantity() == null ? 0 : inventory.getQuantity();
            if (current < requestQty) {
                throw new RuntimeException("Sản phẩm F&B không đủ tồn kho. itemId=" + itemId + ", còn lại=" + current);
            }
            inventory.setQuantity(current - requestQty);
            inventoryRepository.save(inventory);
        }
    }

    @Override
    @Transactional
    public void releaseItemsForBooking(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return;
        }

        List<FnBLine> lines = fnBLineRepository.findByBooking_BookingId(bookingId);
        for (FnBLine line : lines) {
            if (line.getItem() == null || line.getItem().getItemId() == null) {
                continue;
            }
            int qty = line.getQuantity() == null ? 0 : line.getQuantity();
            if (qty <= 0) {
                continue;
            }
            FnbItemInventory inventory = inventoryRepository.findByItemIdForUpdate(line.getItem().getItemId())
                    .orElse(null);
            if (inventory == null) {
                continue;
            }
            int current = inventory.getQuantity() == null ? 0 : inventory.getQuantity();
            inventory.setQuantity(current + qty);
            inventoryRepository.save(inventory);
        }
    }
}
