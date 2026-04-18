package com.cinema.booking.services.adapter;

import com.cinema.booking.dtos.MovieDTO;

/**
 * Adapter Interface (Target) — Định nghĩa hợp đồng chuyển đổi dữ liệu
 * từ nguồn bên ngoài (Adaptee) sang định dạng nội bộ (MovieDTO).
 *
 * Mỗi nguồn dữ liệu bên ngoài (TMDb, IMDb, KOFIC...) sẽ có một
 * Concrete Adapter riêng implement interface này.
 */
public interface MovieDataAdapter {

    /**
     * Chuyển đổi dữ liệu phim từ format bên ngoài sang MovieDTO của StarCine.
     *
     * @param source dữ liệu thô từ API bên ngoài
     * @return MovieDTO đã chuẩn hóa, sẵn sàng để gọi movieService.createMovie()
     */
    MovieDTO adapt(ExternalMovieData source);
}
