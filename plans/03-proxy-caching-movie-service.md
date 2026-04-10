# Plan chi tiet — Proxy (Caching MovieService)

**Muc tieu:** Giam tai DB cho cac API doc phim thuong xuyen (`getAllMovies`, `getMoviesByStatus`) bang Redis, van implement `MovieService`.

**File hien co:** `MovieServiceImpl.java`, `MovieService.java`, `RedisTemplate` da co san.

**Package moi de xuat:** `com.cinema.booking.patterns.proxy`

---

## Buoc 0 — Xac dinh pham vi cache

1. Cache **chi** cac method doc nang va it thay doi:
   - `getAllMovies()`
   - `getMoviesByStatus(MovieStatus status)`
2. **Khong** cache (hoac invalidate ngay) khi:
   - `createMovie`, `updateMovie`, `deleteMovie`
3. `getMovieById`: tuy chon — co the cache theo key `movie:id:{id}` hoac bo qua v1.

---

## Buoc 1 — Thiet ke key va TTL

1. Key de xuat:
   - `movie:cache:all`
   - `movie:cache:status:{NOW_SHOWING|COMING_SOON|STOPPED}`
2. TTL: dung property `cinema.app.redisTtlSeconds` hoac tao `cinema.app.movieCacheTtlSeconds` trong `application.properties`.
3. Serialization: dung cung `RedisTemplate<String, Object>` nhu project; luu `List<MovieDTO>` hoac JSON string — thong nhat mot kieu.

---

## Buoc 2 — Implement `CachingMovieServiceProxy`

1. Class `CachingMovieServiceProxy implements MovieService`.
2. Inject:
   - `MovieService` that — dat ten bean: dung `@Qualifier("movieServiceImpl")` hoac cau hinh `@Primary` can than de tranh vong lap.
3. Trong method doc:
   - Thu `GET` cache
   - Miss → goi delegate → `SET` cache → return
4. Trong method ghi:
   - Goi delegate
   - `DELETE` cac key lien quan (all + tat ca status + optional id)

---

## Buoc 3 — Cau hinh Spring Bean

1. Dam bao chi co **mot** `MovieService` duoc inject vao controller:
   - Cach A: `@Primary` tren `CachingMovieServiceProxy`, impl dat `@Qualifier`.
   - Cach B: impl la `@Service("movieServiceDelegate")` va chi proxy la bean `MovieService` — can refactor ten bean.
2. Kiem tra `MovieController` / `PublicController` dang `@Autowired MovieService` — sau cau hinh phai nhan proxy.

---

## Buoc 4 — Invalidate dung luc

1. Sau `createMovie`: xoa `movie:cache:all` + `movie:cache:status:{newStatus}`.
2. Sau `updateMovie`: xoa `all`, xoa ca key status cu va moi (can doc entity truoc update hoac xoa toan bo prefix `movie:cache:status:*` neu don gian).
3. Sau `deleteMovie`: tuong tu update.

---

## Buoc 5 — Kiem thu

1. Goi `GET /api/movies` hai lan — lan 2 it hit DB hon (quan sat log hoac bat SQL logging).
2. `POST /api/movies` — cache bi xoa, lan doc sau rebuild.
3. Dam bao khong loi serialize `MovieDTO` / enum `MovieStatus`.

---

## Rui ro

- `RedisTemplate.keys("movie:cache:status:*")` tren production co the cham — uu tien xoa cu the neu biet status.
- Race condition: chap nhan TTL ngan; hoac cache-aside don gian nhu tren.

---

## Checklist hoan thanh

- [ ] Proxy implement day du method `MovieService`
- [ ] Controller dung proxy transparently
- [ ] Invalidate sau CUD
- [ ] Build/test pass
