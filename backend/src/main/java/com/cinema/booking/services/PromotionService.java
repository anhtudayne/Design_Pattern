package com.cinema.booking.services;

import com.cinema.booking.dtos.PromotionDTO;

import java.util.List;

public interface PromotionService {

    List<PromotionDTO> findAll();

    PromotionDTO create(PromotionDTO dto);

    PromotionDTO update(Integer id, PromotionDTO dto);

    void delete(Integer id);
}
