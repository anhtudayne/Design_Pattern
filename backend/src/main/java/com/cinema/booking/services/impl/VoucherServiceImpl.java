package com.cinema.booking.services.impl;

import com.cinema.booking.dtos.VoucherDTO;
import com.cinema.booking.services.VoucherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class VoucherServiceImpl implements VoucherService {

    /** Local mapper for {@code LinkedHashMap} → {@link VoucherDTO}; no global {@code ObjectMapper} bean in Boot 4. */
    private static final ObjectMapper VOUCHER_MAP_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String VOUCHER_KEY_PREFIX = "voucher:";

    /**
     * Redis may return {@link VoucherDTO} (new entries with Jackson type id) or
     * {@link java.util.LinkedHashMap} (legacy payloads without {@code @class}) — normalize to DTO.
     */
    private VoucherDTO toVoucherDto(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof VoucherDTO dto) {
            return dto;
        }
        return VOUCHER_MAP_MAPPER.convertValue(raw, VoucherDTO.class);
    }

    @Override
    public void createVoucher(VoucherDTO voucher) {
        String key = VOUCHER_KEY_PREFIX + voucher.getCode();
        redisTemplate.opsForValue().set(key, voucher, voucher.getTtlSeconds() != null ? voucher.getTtlSeconds() : 3600, TimeUnit.SECONDS);
    }

    @Override
    public void updateVoucher(VoucherDTO voucher) {
        String key = VOUCHER_KEY_PREFIX + voucher.getCode();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            Long ttl = redisTemplate.getExpire(key);
            redisTemplate.opsForValue().set(key, voucher, ttl, TimeUnit.SECONDS);
        } else {
            throw new RuntimeException("Voucher không tồn tại hoặc đã hết hạn.");
        }
    }

    @Override
    public void deleteVoucher(String code) {
        redisTemplate.delete(VOUCHER_KEY_PREFIX + code);
    }

    @Override
    public Optional<VoucherDTO> getVoucher(String code) {
        return Optional.ofNullable(toVoucherDto(redisTemplate.opsForValue().get(VOUCHER_KEY_PREFIX + code)));
    }

    @Override
    public List<VoucherDTO> getAllVouchers() {
        Set<String> keys = redisTemplate.keys(VOUCHER_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }

        return keys.stream()
                .map(key -> toVoucherDto(redisTemplate.opsForValue().get(key)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
