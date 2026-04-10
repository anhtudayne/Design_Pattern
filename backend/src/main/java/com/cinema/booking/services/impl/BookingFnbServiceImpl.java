package com.cinema.booking.services.impl;

import com.cinema.booking.dtos.BookingFnbCreateDTO;
import com.cinema.booking.entities.Booking;
import com.cinema.booking.entities.FnBLine;
import com.cinema.booking.entities.FnbItem;
import com.cinema.booking.repositories.BookingRepository;
import com.cinema.booking.repositories.FnbItemRepository;
import com.cinema.booking.repositories.FnBLineRepository;
import com.cinema.booking.services.BookingFnbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingFnbServiceImpl implements BookingFnbService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FnbItemRepository fnbItemRepository;

    @Autowired
    private FnBLineRepository fnBLineRepository;

    @Override
    public List<FnBLine> getAllBookingFnbItems() {
        return fnBLineRepository.findAll();
    }

    @Override
    public List<FnBLine> getBookingFnbItemsByBookingId(Integer bookingId) {
        return fnBLineRepository.findByBooking_BookingId(bookingId);
    }

    @Override
    @Transactional
    public List<FnBLine> createBookingFnbItems(BookingFnbCreateDTO createDTO) {
        Booking booking = bookingRepository.findById(createDTO.getBookingId())
                .orElseThrow(() -> new RuntimeException("Đặt vé không tồn tại với mã: " + createDTO.getBookingId()));

        return createDTO.getItems().stream().map(itemDTO -> {
            FnbItem fnbItem = fnbItemRepository.findById(itemDTO.getItemId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm F&B không tồn tại: " + itemDTO.getItemId()));

            FnBLine item = FnBLine.builder()
                    .booking(booking)
                    .item(fnbItem)
                    .quantity(itemDTO.getQuantity())
                    .unitPrice(fnbItem.getPrice())
                    .build();
            return fnBLineRepository.save(item);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteBookingFnbItemsByBookingId(Integer bookingId) {
        List<FnBLine> items = fnBLineRepository.findByBooking_BookingId(bookingId);
        fnBLineRepository.deleteAll(items);
    }
}
