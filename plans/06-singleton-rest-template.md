# Plan chi tiet — Singleton (RestTemplate bean cho MoMo)

**Muc tieu:** Khong tao `new RestTemplate()` trong `MomoServiceImpl`; dung bean do Spring quan ly (singleton scope mac dinh), de test va cau hinh.

**File hien co:** `MomoServiceImpl.java`, `config/*`

---

## Buoc 0 — Xac nhan cach dung RestTemplate

1. Mo `MomoServiceImpl` — tim field `RestTemplate`.
2. Grep toan project `new RestTemplate()` — dam bao khong cho nao khac tao thu cong.

---

## Buoc 1 — Tao `RestTemplateConfig`

1. Tao file `backend/src/main/java/com/cinema/booking/config/RestTemplateConfig.java`.
2. Noi dung toi thieu:
   - `@Configuration`
   - `@Bean` method tra ve `RestTemplate`
3. (Tuy chon) Cau hinh timeout (connect/read), `BufferingClientHttpRequestFactory` neu can log body — v1 co the giu default.

---

## Buoc 2 — Inject vao `MomoServiceImpl`

1. Xoa `private final RestTemplate restTemplate = new RestTemplate();`
2. Constructor injection:

```java
private final RestTemplate restTemplate;

public MomoServiceImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
}
```

3. Giu nguyen `createPayment` / `verifySignature`.

---

## Buoc 3 — Kiem tra context

1. Chay ung dung — dam bao khong loi `No qualifying bean`.
2. Neu sau nay co nhieu `RestTemplate` beans — dat `@Qualifier("momoRestTemplate")` cho ro.

---

## Buoc 4 — Test (tuy chon)

1. Unit test `MomoServiceImpl` voi `MockRestServiceServer` hoac mock `RestTemplate`.

---

## Checklist hoan thanh

- [ ] `RestTemplateConfig.java` ton tai voi `@Bean RestTemplate`
- [ ] `MomoServiceImpl` khong `new RestTemplate()`
- [ ] Ung dung chay OK
- [ ] (Tuy chon) Test goi MoMo dev
