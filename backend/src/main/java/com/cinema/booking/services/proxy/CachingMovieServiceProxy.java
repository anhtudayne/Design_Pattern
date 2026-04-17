package com.cinema.booking.services.proxy;

import com.cinema.booking.dtos.MovieDTO;
import com.cinema.booking.entities.Movie.MovieStatus;
import com.cinema.booking.services.MovieService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proxy Pattern — Caching Proxy cho MovieService.
 *
 * Mục đích: Trang chủ Customer liên tục gọi API lấy danh sách phim đang chiếu.
 * Danh sách này ít thay đổi (chỉ khi Admin CRUD phim), nhưng query phải join
 * nhiều bảng (movies, genres, movie_casts, cast_members).
 *
 * Proxy đứng trước MovieServiceImpl:
 *   - READ (getAllMovies, getMoviesByStatus, getMovieById): kiểm tra cache trước
 *     + Cache HIT → trả về ngay, không query DB
 *     + Cache MISS → gọi realService, lưu cache, trả kết quả
 *   - WRITE (createMovie, updateMovie, deleteMovie): delegate + invalidate cache
 *
 * Annotation @Primary khiến Spring ưu tiên inject Proxy khi có nhiều bean cùng
 * implement MovieService. MovieServiceImpl được đặt tên bean "movieServiceImpl".
 */
@Service
@Primary
@Slf4j
public class CachingMovieServiceProxy implements MovieService {

    private final MovieService realService;

    /** Cache lưu trữ dữ liệu đã query, dùng ConcurrentHashMap để thread-safe */
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /** Thời gian sống của cache: 5 phút */
    private static final long TTL_MS = 5 * 60 * 1000;

    public CachingMovieServiceProxy(@Qualifier("movieServiceImpl") MovieService realService) {
        this.realService = realService;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  READ operations — Kiểm tra cache trước, cache miss thì gọi real service
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public List<MovieDTO> getAllMovies() {
        String key = "all_movies";
        CacheEntry entry = cache.get(key);

        if (entry != null && !entry.isExpired()) {
            log.debug("[Proxy] Cache HIT cho key: {}", key);
            return castList(entry.getData());
        }

        log.debug("[Proxy] Cache MISS cho key: {} → gọi DB", key);
        List<MovieDTO> result = realService.getAllMovies();
        cache.put(key, new CacheEntry(result));
        return result;
    }

    @Override
    public List<MovieDTO> getMoviesByStatus(MovieStatus status) {
        String key = "movies_" + status.name();
        CacheEntry entry = cache.get(key);

        if (entry != null && !entry.isExpired()) {
            log.debug("[Proxy] Cache HIT cho key: {}", key);
            return castList(entry.getData());
        }

        log.debug("[Proxy] Cache MISS cho key: {} → gọi DB", key);
        List<MovieDTO> result = realService.getMoviesByStatus(status);
        cache.put(key, new CacheEntry(result));
        return result;
    }

    @Override
    public MovieDTO getMovieById(Integer id) {
        String key = "movie_" + id;
        CacheEntry entry = cache.get(key);

        if (entry != null && !entry.isExpired()) {
            log.debug("[Proxy] Cache HIT cho key: {}", key);
            return (MovieDTO) entry.getData();
        }

        log.debug("[Proxy] Cache MISS cho key: {} → gọi DB", key);
        MovieDTO result = realService.getMovieById(id);
        cache.put(key, new CacheEntry(result));
        return result;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  WRITE operations — Delegate sang real service + invalidate cache
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public MovieDTO createMovie(MovieDTO movieDTO) {
        MovieDTO result = realService.createMovie(movieDTO);
        invalidateCache();
        log.info("[Proxy] Tạo phim mới → Cache đã được xóa toàn bộ");
        return result;
    }

    @Override
    public MovieDTO updateMovie(Integer id, MovieDTO movieDTO) {
        MovieDTO result = realService.updateMovie(id, movieDTO);
        invalidateCache();
        log.info("[Proxy] Cập nhật phim ID {} → Cache đã được xóa toàn bộ", id);
        return result;
    }

    @Override
    public void deleteMovie(Integer id) {
        realService.deleteMovie(id);
        invalidateCache();
        log.info("[Proxy] Xóa phim ID {} → Cache đã được xóa toàn bộ", id);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Internal helpers
    // ═══════════════════════════════════════════════════════════════════

    /** Xóa toàn bộ cache khi dữ liệu phim thay đổi */
    private void invalidateCache() {
        cache.clear();
    }

    /** Helper để cast Object → List<MovieDTO> an toàn */
    @SuppressWarnings("unchecked")
    private List<MovieDTO> castList(Object data) {
        return (List<MovieDTO>) data;
    }

    // ─────────────────────────────────────────────────────────────
    //  Inner class: CacheEntry — lưu data + thời điểm tạo
    // ─────────────────────────────────────────────────────────────

    /**
     * Đơn vị lưu trữ cache: chứa dữ liệu và timestamp tạo.
     * isExpired() kiểm tra xem entry đã quá TTL chưa.
     */
    private static class CacheEntry {
        private final Object data;
        private final long createdAt;

        CacheEntry(Object data) {
            this.data = data;
            this.createdAt = System.currentTimeMillis();
        }

        Object getData() {
            return data;
        }

        boolean isExpired() {
            return (System.currentTimeMillis() - createdAt) > TTL_MS;
        }
    }
}
