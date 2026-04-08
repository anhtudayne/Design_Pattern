package com.cinema.booking.services;

import com.cinema.booking.dtos.VoucherDTO;
import java.util.List;
import java.util.Optional;

public interface VoucherService {
    void createVoucher(VoucherDTO voucher);
    void updateVoucher(VoucherDTO voucher);
    void deleteVoucher(String code);
    Optional<VoucherDTO> getVoucher(String code);
    List<VoucherDTO> getAllVouchers();
}
