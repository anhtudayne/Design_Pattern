package com.cinema.booking.patterns.specification;

import com.cinema.booking.entities.Booking;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class BookingSpecificationBuilder {

    public static Specification<Booking> searchBookings(String query) {
        return (root, criteriaQuery, cb) -> {
            if (query == null || query.trim().isEmpty()) {
                return cb.conjunction();
            }

            String st = query.trim();
            String lowerSt = st.toLowerCase();
            List<Predicate> preds = new ArrayList<>();

            // 1. Tìm theo ID (Số nguyên)
            if (st.matches("\\d+")) {
                try {
                    preds.add(cb.equal(root.get("bookingId"), Integer.valueOf(st)));
                } catch (Exception ignored) {}
            }

            // 2. Tìm theo Mã Booking Code (Like)
            preds.add(cb.like(cb.lower(root.get("bookingCode")), "%" + lowerSt + "%"));

            // 3. Thông tin khách hàng (SĐT / Email)
            try {
                var customerJoin = root.join("customer", JoinType.LEFT);
                
                // Tìm theo Số điện thoại
                preds.add(cb.like(customerJoin.get("phone"), "%" + st + "%"));

                // Tìm theo Email (Chỉ join UserAccount nếu từ khóa có khả năng là email hoặc không phải số đơn thuần)
                if (st.contains("@") || !st.matches("\\d+")) {
                    var accountJoin = customerJoin.join("userAccount", JoinType.LEFT);
                    preds.add(cb.like(cb.lower(accountJoin.get("email")), "%" + lowerSt + "%"));
                }
            } catch (Exception e) {
                // Log cảnh báo nếu có lỗi join nhưng không làm sập luồng tìm kiếm ID/Code
                System.err.println("Booking search specification warning: " + e.getMessage());
            }

            return cb.or(preds.toArray(new Predicate[0]));
        };
    }
}
