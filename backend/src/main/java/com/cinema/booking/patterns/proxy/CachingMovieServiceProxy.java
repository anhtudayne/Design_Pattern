package com.cinema.booking.patterns.proxy;

import com.cinema.booking.dtos.MovieDTO;
import com.cinema.booking.entities.Movie.MovieStatus;
import com.cinema.booking.services.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Proxy pattern — wraps the real MovieServiceImpl to add Redis caching.
 * Marked @Primary so all callers receive this proxy instead of the impl.
 * Cache is invalidated on every write (create / update / delete).
 */
@Primary
@Service
public class CachingMovieServiceProxy implements MovieService {

    private static final String KEY_ALL = "movie:cache:all";
    private static final String KEY_STATUS_PREFIX = "movie:cache:status:";

    @Qualifier("movieServiceImpl")
    @Autowired
    private MovieService delegate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${cinema.app.redisTtlSeconds:600}")
    private long ttlSeconds;

    // ─── READ ────────────────────────────────────────────────────────────────

    @Override
    @SuppressWarnings("unchecked")
    public List<MovieDTO> getAllMovies() {
        Object cached = redisTemplate.opsForValue().get(KEY_ALL);
        if (cached != null) {
            return (List<MovieDTO>) cached;
        }
        List<MovieDTO> result = delegate.getAllMovies();
        redisTemplate.opsForValue().set(KEY_ALL, result, ttlSeconds, TimeUnit.SECONDS);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MovieDTO> getMoviesByStatus(MovieStatus status) {
        String key = KEY_STATUS_PREFIX + status.name();
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return (List<MovieDTO>) cached;
        }
        List<MovieDTO> result = delegate.getMoviesByStatus(status);
        redisTemplate.opsForValue().set(key, result, ttlSeconds, TimeUnit.SECONDS);
        return result;
    }

    @Override
    public MovieDTO getMovieById(Integer id) {
        // Not cached in v1 — delegate directly
        return delegate.getMovieById(id);
    }

    // ─── WRITE (invalidate cache after each mutation) ─────────────────────

    @Override
    public MovieDTO createMovie(MovieDTO movieDTO) {
        MovieDTO created = delegate.createMovie(movieDTO);
        invalidateAll();
        if (created.getStatus() != null) {
            invalidateStatus(created.getStatus());
        }
        return created;
    }

    @Override
    public MovieDTO updateMovie(Integer id, MovieDTO movieDTO) {
        MovieDTO updated = delegate.updateMovie(id, movieDTO);
        invalidateAll();
        invalidateAllStatuses();
        return updated;
    }

    @Override
    public void deleteMovie(Integer id) {
        delegate.deleteMovie(id);
        invalidateAll();
        invalidateAllStatuses();
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private void invalidateAll() {
        redisTemplate.delete(KEY_ALL);
    }

    private void invalidateStatus(MovieStatus status) {
        redisTemplate.delete(KEY_STATUS_PREFIX + status.name());
    }

    private void invalidateAllStatuses() {
        for (MovieStatus status : MovieStatus.values()) {
            invalidateStatus(status);
        }
    }
}
