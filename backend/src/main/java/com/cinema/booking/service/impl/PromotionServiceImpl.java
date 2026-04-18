package com.cinema.booking.service.impl;

import com.cinema.booking.dto.PromotionDTO;
import com.cinema.booking.entity.Promotion;
import com.cinema.booking.repository.PromotionRepository;
import com.cinema.booking.service.PromotionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionServiceImpl(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    @Override
    public List<PromotionDTO> findAll() {
        return promotionRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public PromotionDTO create(PromotionDTO dto) {
        if (dto.getCode() == null || dto.getCode().isBlank()) {
            throw new RuntimeException("Mã khuyến mãi không được để trống.");
        }
        if (dto.getDiscountType() == null) {
            throw new RuntimeException("Loại giảm giá là bắt buộc.");
        }
        if (dto.getDiscountValue() == null) {
            throw new RuntimeException("Giá trị giảm là bắt buộc.");
        }
        if (dto.getValidTo() == null) {
            throw new RuntimeException("Thời hạn hiệu lực là bắt buộc.");
        }
        String code = dto.getCode().trim().toUpperCase();
        if (promotionRepository.findByCode(code).isPresent()) {
            throw new RuntimeException("Mã '" + code + "' đã tồn tại.");
        }
        Promotion p = Promotion.builder()
                .code(code)
                .discountType(dto.getDiscountType())
                .discountValue(dto.getDiscountValue())
                .validTo(dto.getValidTo())
                .build();
        return toDto(promotionRepository.save(p));
    }

    @Override
    @Transactional
    public PromotionDTO update(Integer id, PromotionDTO dto) {
        Promotion p = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi."));
        if (dto.getDiscountType() != null) {
            p.setDiscountType(dto.getDiscountType());
        }
        if (dto.getDiscountValue() != null) {
            p.setDiscountValue(dto.getDiscountValue());
        }
        if (dto.getValidTo() != null) {
            p.setValidTo(dto.getValidTo());
        }
        return toDto(promotionRepository.save(p));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        promotionRepository.deleteById(id);
    }

    private PromotionDTO toDto(Promotion p) {
        return PromotionDTO.builder()
                .id(p.getId())
                .code(p.getCode())
                .discountType(p.getDiscountType())
                .discountValue(p.getDiscountValue())
                .validTo(p.getValidTo())
                .build();
    }
}
