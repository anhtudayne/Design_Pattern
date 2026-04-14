# Pattern 03 — Proxy (Uỷ Quyền / Cache)

## 1. Lý thuyết

**Proxy** thuộc nhóm Structural. Ý tưởng: tạo đối tượng đại diện đứng trước đối tượng thật. Mọi lời gọi đi qua proxy — proxy thêm hành vi (cache, log, auth) **mà không sửa class thật**.

```
Client → Proxy → RealObject
```

---

## 2. Vấn đề (Trước khi áp dụng)

Trang chủ hiển thị danh sách phim — `MovieServiceImpl.getAllMovies()` gọi thẳng DB mỗi request:

```java
// ❌ Trước — DB query mỗi lần
public List<MovieDTO> getAllMovies() {
    return movieRepository.findAll().stream()
        .map(this::toDTO).collect(Collectors.toList());
}
```

**Hậu quả:** 1000 user load trang → 1000 DB query với **cùng dữ liệu**. Danh sách phim ít thay đổi nhưng query liên tục — DB quá tải, latency cao.

---

## 3. Giải pháp — Proxy + Redis Cache

`CachingMovieServiceProxy` implements cùng interface `MovieService`. Đánh dấu `@Primary` — Spring tự inject proxy thay cho impl. **Không cần sửa controller hay caller nào.**

---

## 4. Các file trong dự án

| File | Đường dẫn | Vai trò |
|------|-----------|---------|
| `MovieService.java` | `services/` | Interface chung (sẵn có) |
| `MovieServiceImpl.java` | `services/impl/` | Real object — query DB |
| `CachingMovieServiceProxy.java` | `patterns/proxy/` | **Proxy** — Redis cache + delegate |

---

## 5. Code thực tế — `CachingMovieServiceProxy.java`

```java
@Primary   // Spring inject proxy này thay vì impl
@Service
public class CachingMovieServiceProxy implements MovieService {

    private static final String KEY_ALL           = "movie:cache:all";
    private static final String KEY_STATUS_PREFIX = "movie:cache:status:";

    @Qualifier("movieServiceImpl")
    @Autowired
    private MovieService delegate;     // real object

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${cinema.app.redisTtlSeconds:600}")
    private long ttlSeconds;

    @Override
    public List<MovieDTO> getAllMovies() {
        Object cached = redisTemplate.opsForValue().get(KEY_ALL);
        if (cached != null) return (List<MovieDTO>) cached;     // HIT

        List<MovieDTO> result = delegate.getAllMovies();          // MISS → DB
        redisTemplate.opsForValue().set(KEY_ALL, result, ttlSeconds, TimeUnit.SECONDS);
        return result;
    }

    @Override
    public List<MovieDTO> getMoviesByStatus(MovieStatus status) {
        String key = KEY_STATUS_PREFIX + status.name();
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) return (List<MovieDTO>) cached;

        List<MovieDTO> result = delegate.getMoviesByStatus(status);
        redisTemplate.opsForValue().set(key, result, ttlSeconds, TimeUnit.SECONDS);
        return result;
    }

    @Override
    public MovieDTO getMovieById(Integer id) {
        return delegate.getMovieById(id);   // không cache — query trực tiếp
    }

    // WRITE — invalidate cache sau mỗi thay đổi
    @Override
    public MovieDTO createMovie(MovieDTO dto) {
        MovieDTO created = delegate.createMovie(dto);
        invalidateAll();
        return created;
    }

    @Override
    public MovieDTO updateMovie(Integer id, MovieDTO dto) {
        MovieDTO updated = delegate.updateMovie(id, dto);
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

    private void invalidateAll() { redisTemplate.delete(KEY_ALL); }
    private void invalidateAllStatuses() {
        for (MovieStatus s : MovieStatus.values())
            redisTemplate.delete(KEY_STATUS_PREFIX + s.name());
    }
}
```

---

## 6. Luồng hoạt động thực tế

```
GET /api/movies
        │ (controller chỉ biết MovieService interface)
        ▼
CachingMovieServiceProxy.getAllMovies()
        │
        ├── Redis GET "movie:cache:all"
        │       ├─ HIT  → return ngay (< 1ms)
        │       └─ MISS → delegate.getAllMovies() → DB (~50ms)
        │                    └── Redis SET TTL=600s
        └── return result
```

---

## 7. Redis Keys

| Key | Nội dung | TTL |
|-----|---------|-----|
| `movie:cache:all` | `List<MovieDTO>` tất cả phim | 600s |
| `movie:cache:status:NOW_SHOWING` | phim đang chiếu | 600s |
| `movie:cache:status:COMING_SOON` | phim sắp chiếu | 600s |

---

## 8. SOLID

| | Chi tiết |
|-|---------|
| **S** | Proxy chỉ làm cache — không có business logic phim |
| **O** | Thêm logging → tạo `LoggingMovieServiceProxy` mới, không sửa impl |
| **L** | Proxy thay thế impl hoàn toàn — controller không phân biệt |
| **D** | `MovieController` inject `MovieService` (interface), Spring inject proxy |

---

## 9. Thành quả

| Trước | Sau |
|-------|-----|
| 1000 request → 1000 DB query | 1 DB query + 999 Redis hit (< 1ms) |
| Thêm cache → phải sửa `MovieServiceImpl` | Không sửa impl, không sửa controller |
| Không có chiến lược invalidation rõ ràng | Mọi write → tự xóa cache → data luôn fresh |

---

## 10. Test thủ công

```bash
# Lần 1 — MISS, query DB (~50-100ms)
GET /api/movies

# Lần 2 — HIT, Redis (< 5ms)
GET /api/movies

# Xem Redis key tồn tại
redis-cli GET "movie:cache:all"

# Tạo phim → cache bị xóa
POST /api/movies  {...}

# Gọi lại → MISS lại
GET /api/movies
```
