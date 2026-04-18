package com.cinema.booking.service.impl;

import com.cinema.booking.dto.request.BookingFnbCreateDTO;
import com.cinema.booking.entity.Booking;
import com.cinema.booking.entity.FnBLine;
import com.cinema.booking.entity.FnbItem;
import com.cinema.booking.repository.BookingRepository;
import com.cinema.booking.repository.FnbItemRepository;
import com.cinema.booking.repository.FnBLineRepository;
import com.cinema.booking.service.BookingFnbService;
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
                    .fnbItem(fnbItem)
                    .quantity(itemDTO.getQuantity())
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
