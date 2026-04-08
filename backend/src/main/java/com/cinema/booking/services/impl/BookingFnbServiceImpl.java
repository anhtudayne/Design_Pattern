package com.cinema.booking.services.impl;

import com.cinema.booking.dtos.BookingFnbCreateDTO;
import com.cinema.booking.entities.Booking;
import com.cinema.booking.entities.BookingFnbItem;
import com.cinema.booking.entities.FnbItem;
import com.cinema.booking.repositories.BookingFnbItemRepository;
import com.cinema.booking.repositories.BookingRepository;
import com.cinema.booking.repositories.FnbItemRepository;
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
    private BookingFnbItemRepository bookingFnbItemRepository;

    @Override
    public List<BookingFnbItem> getAllBookingFnbItems() {
        return bookingFnbItemRepository.findAll();
    }

    @Override
    public List<BookingFnbItem> getBookingFnbItemsByBookingId(Integer bookingId) {
        return bookingFnbItemRepository.findByBooking_BookingId(bookingId);
    }

    @Override
    @Transactional
    public List<BookingFnbItem> createBookingFnbItems(BookingFnbCreateDTO createDTO) {
        Booking booking = bookingRepository.findById(createDTO.getBookingId())
                .orElseThrow(() -> new RuntimeException("Đặt vé không tồn tại với mã: " + createDTO.getBookingId()));

        return createDTO.getItems().stream().map(itemDTO -> {
            FnbItem fnbItem = fnbItemRepository.findById(itemDTO.getItemId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm F&B không tồn tại: " + itemDTO.getItemId()));

            BookingFnbItem item = BookingFnbItem.builder()
                    .booking(booking)
                    .item(fnbItem)
                    .quantity(itemDTO.getQuantity())
                    .price(fnbItem.getPrice())
                    .build();
            return bookingFnbItemRepository.save(item);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteBookingFnbItemsByBookingId(Integer bookingId) {
        List<BookingFnbItem> items = bookingFnbItemRepository.findByBooking_BookingId(bookingId);
        bookingFnbItemRepository.deleteAll(items);
    }
}
