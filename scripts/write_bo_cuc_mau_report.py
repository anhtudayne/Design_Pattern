# -*- coding: utf-8 -*-
"""One-off: write UTF-8 markdown for08-dynamic-pricing-engine-report-bo-cuc-mau.md"""
from pathlib import Path

def main() -> None:
    lines = [
        "# Dynamic Pricing Engine \u2014 Tr\u00ecnh b\u00e0y theo b\u1ed1 c\u1ee5c m\u1eabu",
        "",
        "> **Lu\u1ed3ng:** `POST /api/booking/calculate` \u2014 t\u00ednh gi\u00e1 tr\u01b0\u1edbc khi thanh to\u00e1n  ",
        "> **T\u00e0i li\u1ec7u k\u1ef9 thu\u1eadt chi ti\u1ebft:** [08-dynamic-pricing-engine.md](08-dynamic-pricing-engine.md)",
        "",
        "---",
        "",
        "## C\u00e1c pattern \u00e1p d\u1ee5ng",
        "",
        (
            "Trong h\u1ec7 th\u1ed1ng \u0111\u1eb7t v\u00e9, nh\u00f3m x\u1eed l\u00fd **t\u00ednh gi\u00e1 \u0111\u1ed9ng (dynamic pricing)** s\u1eed d\u1ee5ng "
            "**n\u0103m pattern b\u1ed5 tr\u1ee3 cho nhau** tr\u00ean c\u00f9ng m\u1ed9t \u0111\u01b0\u1eddng \u0111i production: "
            "**Chain of Responsibility** (chu\u1ed7i ki\u1ec3m tra \u0111i\u1ec1u ki\u1ec7n tr\u01b0\u1edbc khi t\u00ednh), "
            "**Proxy** (b\u1ecdc engine t\u00ednh gi\u00e1 \u0111\u1ec3 th\u00eam cache Redis), "
            "**Specification** (\u0111\u00f3ng g\u00f3i \u0111i\u1ec1u ki\u1ec7n nghi\u1ec7p v\u1ee5 d\u1ea1ng predicate cho ph\u1ee5 ph\u00ed th\u1eddi gian), "
            "**Strategy** (t\u00e1ch ba c\u00f4ng th\u1ee9c: ti\u1ec1n v\u00e9, F&B, ph\u1ee5 ph\u00ed th\u1eddi gian), v\u00e0 "
            "**Decorator** (chu\u1ed7i gi\u1ea3m gi\u00e1 c\u00f3 th\u1ec3 k\u1ebft h\u1ee3p theo runtime). "
            "C\u00e1c pattern n\u00e0y kh\u00f4ng \u0111\u1ee9ng ri\u00eang l\u1ebb: CoR ch\u1ea1y tr\u01b0\u1edbc \u0111\u1ec3 \u0111\u1ea3m b\u1ea3o d\u1eef li\u1ec7u h\u1ee3p l\u1ec7 v\u00e0 t\u00e1i d\u00f9ng k\u1ebft qu\u1ea3 truy v\u1ea5n; "
            "Proxy ch\u1eb7n m\u1ecdi l\u1eddi g\u1ecdi t\u00ednh gi\u00e1 \u0111\u1ec3 tr\u00e1nh t\u00ednh l\u1eb7p; b\u00ean trong engine, Strategy v\u00e0 Specification ph\u1ed1i h\u1ee3p "
            "(strategy g\u1ecdi predicate khi c\u1ea7n); cu\u1ed1i c\u00f9ng Decorator c\u1ed9ng d\u1ed3n c\u00e1c kho\u1ea3n gi\u1ea3m sau khi \u0111\u00e3 c\u00f3 subtotal."
        ),
        "",
        "---",
        "",
        "## L\u00fd do s\u1eed d\u1ee5ng",
        "",
        (
            "Lu\u1ed3ng t\u00ednh gi\u00e1 trong \u1ee9ng d\u1ee5ng r\u1ea1p chi\u1ebfu phim c\u1ea7n v\u1eeba **an to\u00e0n nghi\u1ec7p v\u1ee5** "
            "(su\u1ea5t chi\u1ebfu c\u00f2n h\u1ee3p l\u1ec7, gh\u1ebf c\u00f2n tr\u1ed1ng, m\u00e3 khuy\u1ebfn m\u00e3i c\u00f2n h\u1ea1n v\u00e0 c\u00f2n l\u01b0\u1ee3t), v\u1eeba **linh ho\u1ea1t c\u00f4ng th\u1ee9c** "
            "(v\u00e9 + F&B + ph\u1ee5 ph\u00ed cu\u1ed1i tu\u1ea7n/ng\u00e0y l\u1ec5 + gi\u1ea3m theo h\u1ea1ng th\u00e0nh vi\u00ean + gi\u1ea3m theo promo), v\u1eeba **ch\u1ecbu t\u1ea3i khi ng\u01b0\u1eddi d\u00f9ng xem gi\u00e1 nhi\u1ec1u l\u1ea7n** "
            "v\u1edbi c\u00f9ng b\u1ed9 tham s\u1ed1, trong khi ph\u1ea7n c\u00f2n l\u1ea1i c\u1ee7a nghi\u1ec7p v\u1ee5 (m\u1ed9t \u0111i\u1ec3m API th\u1ed1ng nh\u1ea5t, m\u1ed9t DTO k\u1ebft qu\u1ea3 chi ti\u1ebft) "
            "v\u1eabn ph\u1ea3i gi\u1eef nguy\u00ean h\u1ee3p \u0111\u1ed3ng v\u1edbi client."
        ),
        "",
        (
            "**Chain of Responsibility** gi\u1ea3i quy\u1ebft vi\u1ec7c kh\u00f4ng th\u1ec3 nh\u1ed3i h\u00e0ng ch\u1ee5c \u0111i\u1ec1u ki\u1ec7n ki\u1ec3m tra v\u00e0o m\u1ed9t method duy nh\u1ea5t: "
            "m\u1ed7i handler \u0111\u1ea3m nhi\u1ec7m m\u1ed9t quy t\u1eafc, n\u1ed1i th\u00e0nh chu\u1ed7i c\u00f3 th\u1ee9 t\u1ef1, fail s\u1edbm khi vi ph\u1ea1m, \u0111\u1ed3ng th\u1eddi ghi `showtime` v\u00e0 `promotion` v\u00e0o context "
            "\u0111\u1ec3 t\u1ea7ng sau kh\u00f4ng ph\u1ea3i truy v\u1ea5n l\u1ea1i c\u01a1 s\u1edf d\u1eef li\u1ec7u."
        ),
        "",
        (
            "**Proxy** gi\u1ea3i quy\u1ebft b\u00e0i to\u00e1n preview gi\u00e1 l\u1eb7p l\u1ea1i: m\u1ed9t l\u1edbp b\u1ecdc c\u00f9ng giao di\u1ec7n `IPricingEngine` ki\u1ec3m tra cache theo kh\u00f3a gh\u00e9p t\u1eeb su\u1ea5t chi\u1ebfu, gh\u1ebf, m\u00e3 promo v\u00e0 kh\u00e1ch; "
            "cache hit th\u00ec tr\u1ea3 ngay, cache miss m\u1edbi \u1ee7y quy\u1ec1n xu\u1ed1ng engine th\u1eadt \u2014 caller (`BookingServiceImpl`) kh\u00f4ng c\u1ea7n bi\u1ebft c\u00f3 cache hay kh\u00f4ng."
        ),
        "",
        (
            "**Specification** t\u00e1ch c\u00e1c \u0111i\u1ec1u ki\u1ec7n \u201ccu\u1ed1i tu\u1ea7n\u201d, \u201cng\u00e0y l\u1ec5\u201d, \u201c\u0111\u1eb7t s\u1edbm\u201d, \u201cl\u1ea5p \u0111\u1ea7y cao\u201d kh\u1ecfi code if-else r\u1ea3i r\u00e1c trong strategy: "
            "m\u1ed7i \u0111i\u1ec1u ki\u1ec7n l\u00e0 m\u1ed9t predicate c\u00f3 th\u1ec3 ki\u1ec3m th\u1eed \u0111\u1ed9c l\u1eadp v\u00e0 gh\u00e9p n\u1ed1i, gi\u00fap tr\u00e1nh s\u00f3t quy t\u1eafc (v\u00ed d\u1ee5 ng\u00e0y l\u1ec5) "
            "v\u00e0 t\u1eadp trung danh s\u00e1ch ng\u00e0y l\u1ec5 m\u1ed9t n\u01a1i."
        ),
        "",
        (
            "**Strategy** cho ph\u00e9p ba c\u00e1ch t\u00ednh ti\u1ec1n kh\u00e1c nhau (v\u00e9, F&B, ph\u1ee5 ph\u00ed th\u1eddi gian) c\u00f9ng chung m\u1ed9t h\u1ee3p \u0111\u1ed3ng `calculate(context)` m\u00e0 kh\u00f4ng switch theo lo\u1ea1i trong m\u1ed9t method kh\u1ed5ng l\u1ed3; "
            "\u0111\u1ed3ng th\u1eddi F&B \u0111\u01b0\u1ee3c t\u00ednh tr\u00ean d\u1eef li\u1ec7u \u0111\u00e3 resolve s\u1eb5n t\u1eeb service, tr\u00e1nh N+1 truy v\u1ea5n trong engine."
        ),
        "",
        (
            "**Decorator** gi\u1ea3i quy\u1ebft vi\u1ec7c c\u00f3 nhi\u1ec1u lo\u1ea1i gi\u1ea3m gi\u00e1 c\u00f3 th\u1ec3 \u0111\u1ed3ng th\u1eddi t\u1ed3n t\u1ea1i (promo + h\u1ea1ng th\u00e0nh vi\u00ean) v\u1edbi t\u1eadp ph\u1ee5 thu\u1ed9c runtime: "
            "thay v\u00ec b\u00f9ng n\u1ed5 l\u1edbp k\u1ebf th\u1eeba ki\u1ec3u \u201cch\u1ec9 promo / ch\u1ec9 member / c\u1ea3 hai\u201d, chu\u1ed7i decorator \u0111\u01b0\u1ee3c d\u1ef1ng \u0111\u1ed9ng quanh `NoDiscount`, "
            "m\u1ed7i l\u1edbp ch\u1ec9 c\u1ed9ng th\u00eam m\u1ed9t lo\u1ea1i chi\u1ebft kh\u1ea5u v\u00e0 \u1ee7y quy\u1ec1n ph\u1ea7n c\u00f2n l\u1ea1i cho l\u1edbp b\u1ecdc b\u00ean trong; ph\u1ea7n validate promo \u0111\u00e3 \u0111\u01b0\u1ee3c t\u00e1ch sang CoR "
            "n\u00ean t\u1ea7ng gi\u1ea3m gi\u00e1 ch\u1ec9 t\u00ednh thu\u1ea7n, \u0111\u00fang ph\u00e2n t\u1ea7ng tr\u00e1ch nhi\u1ec7m."
        ),
        "",
        "---",
        "",
        "## \u01afu \u0111i\u1ec3m",
        "",
        (
            "\u00c1p n\u0103m pattern tr\u00ean mang l\u1ea1i kh\u1ea3 n\u0103ng m\u1edf r\u1ed9ng theo h\u01b0\u1edbng **\u0111\u00f3ng v\u1edbi thay \u0111\u1ed5i, m\u1edf v\u1edbi m\u1edf r\u1ed9ng**: "
            "khi b\u1ed5 sung quy t\u1eafc validate m\u1edbi c\u00f3 th\u1ec3 th\u00eam m\u1ed9t handler v\u00e0 n\u1ed1i v\u00e0o c\u1ea5u h\u00ecnh chu\u1ed7i; khi b\u1ed5 sung c\u00e1ch t\u00ednh gi\u00e1 m\u1edbi c\u00f3 th\u1ec3 th\u00eam m\u1ed9t strategy; "
            "khi b\u1ed5 sung lo\u1ea1i gi\u1ea3m gi\u00e1 m\u1edbi c\u00f3 th\u1ec3 th\u00eam m\u1ed9t decorator; khi b\u1ed5 sung \u0111i\u1ec1u ki\u1ec7n kinh doanh m\u1edbi c\u00f3 th\u1ec3 b\u1ed5 sung predicate trong l\u1edbp \u0111i\u1ec1u ki\u1ec7n "
            "\u2014 **\u00edt ph\u1ea3i s\u1eeda** c\u00e1c l\u1edbp \u0111i\u1ec1u ph\u1ed1i v\u00e0 engine \u0111ang \u1ed5n \u0111\u1ecbnh."
        ),
        "",
        (
            "M\u00e3 ngu\u1ed3n **d\u1ec5 ki\u1ec3m th\u1eed v\u00e0 b\u1ea3o tr\u00ec** h\u01a1n v\u00ec t\u1eebng m\u1eaft x\u00edch c\u00f3 ph\u1ea1m vi h\u1eb9p: handler ch\u1ec9 lo m\u1ed9t rule, strategy ch\u1ec9 lo m\u1ed9t c\u00f4ng th\u1ee9c, "
            "decorator ch\u1ec9 lo m\u1ed9t kho\u1ea3n chi\u1ebft kh\u1ea5u, specification ch\u1ec9 lo m\u1ed9t \u0111i\u1ec1u ki\u1ec7n; proxy v\u00e0 engine th\u1eadt c\u00f3 th\u1ec3 thay th\u1ebf l\u1eabn nhau qua interface. "
            "Lu\u1ed3ng cache c\u00f3 th\u1ec3 b\u1eadt ho\u1eb7c ch\u1ec9nh TTL m\u00e0 kh\u00f4ng l\u00e0m l\u1eabn logic t\u00ednh gi\u00e1 c\u1ed1t l\u00f5i."
        ),
        "",
        (
            "Proxy c\u00f2n gi\u00fap **gi\u1ea3m t\u1ea3i** cho c\u01a1 s\u1edf d\u1eef li\u1ec7u v\u00e0 CPU khi ng\u01b0\u1eddi d\u00f9ng l\u1eb7p l\u1ea1i c\u00f9ng m\u1ed9t b\u1ea3n xem gi\u00e1. CoR gi\u00fap **gi\u1ea3m truy v\u1ea5n d\u01b0 th\u1eeba** nh\u1edd t\u00e1i d\u00f9ng th\u1ef1c th\u1ec3 \u0111\u00e3 load. "
            "Specification gi\u00fap **gi\u1ea3m r\u1ee7i ro c\u1ea5u h\u00ecnh logic nghi\u1ec7p v\u1ee5** (thi\u1ebfu ng\u00e0y l\u1ec5, \u0111i\u1ec1u ki\u1ec7n tr\u00f9ng l\u1eb7p) v\u00ec quy t\u1eafc \u0111\u01b0\u1ee3c t\u1eadp trung v\u00e0 ki\u1ec3m th\u1eed t\u00e1ch bi\u1ec7t. "
            "Decorator gi\u00fap **tr\u00e1nh nh\u1ea7m gi\u1eefa validate v\u00e0 chi\u1ebft kh\u1ea5u**, gi\u1ea3m bug \u201cvalidate promo \u1edf hai n\u01a1i\u201d hay \u201cqu\u00ean gi\u1ea3m h\u1ea1ng th\u00e0nh vi\u00ean\u201d so v\u1edbi c\u00e1ch nh\u1ed3i to\u00e0n b\u1ed9 v\u00e0o m\u1ed9t method."
        ),
        "",
        (
            "T\u1ed5ng th\u1ec3, \u0111i\u1ec3m g\u1ecdi API v\u1eabn **th\u1ed1ng nh\u1ea5t** (`calculatePrice`), ph\u1ea3n h\u1ed3i v\u1eabn **\u0111\u1ea7y \u0111\u1ee7 breakdown** "
            "(v\u00e9, F&B, ph\u1ee5 ph\u00ed th\u1eddi gian, gi\u1ea3m th\u00e0nh vi\u00ean, t\u1ed5ng gi\u1ea3m, nh\u00e3n chi\u1ebfn l\u01b0\u1ee3c, t\u1ed5ng cu\u1ed1i), trong khi b\u00ean trong \u0111\u01b0\u1ee3c chia th\u00e0nh c\u00e1c t\u1ea7ng pattern "
            "**r\u00f5 vai tr\u00f2, \u00edt ph\u1ee5 thu\u1ed9c ch\u00e9o**, ph\u00f9 h\u1ee3p ph\u00e1t tri\u1ec3n v\u00e0 v\u1eadn h\u00e0nh l\u00e2u d\u00e0i."
        ),
        "",
    ]
    out = Path(__file__).resolve().parents[1] / "docs" / "patterns" / "08-dynamic-pricing-engine-report-bo-cuc-mau.md"
    out.write_text("\n".join(lines), encoding="utf-8")
    print("Wrote", out)

if __name__ == "__main__":
    main()
