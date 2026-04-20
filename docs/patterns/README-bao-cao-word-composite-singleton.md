# Báo cáo bố cục mẫu 08 — Composite và Singleton

Nội dung báo cáo ngắn (bố cục giống [08-dynamic-pricing-engine-report-bo-cuc-mau.md](08-dynamic-pricing-engine-report-bo-cuc-mau.md)) có bản **Markdown** trong repo; file Word chỉ là tùy chọn nếu cần nộp `.docx`.

## File Markdown (chính)

| File | Nội dung |
|------|-----------|
| [composite-dashboard-stats-report-bo-cuc-mau.md](composite-dashboard-stats-report-bo-cuc-mau.md) | Composite — dashboard thống kê admin |
| [singleton-resttemplate-report-bo-cuc-mau.md](singleton-resttemplate-report-bo-cuc-mau.md) | Singleton — bean `RestTemplate` / MoMo |

Tài liệu kỹ thuật dài: [composite-dashboard-stats-package-vi.md](composite-dashboard-stats-package-vi.md), [singleton-resttemplate-package-vi.md](singleton-resttemplate-package-vi.md).

## Xuất Word tùy chọn

Nếu cần `.docx` (cùng nội dung tương đương):

```bash
python3 -m venv .venv_docx
.venv_docx/bin/pip install python-docx
.venv_docx/bin/python scripts/generate_pattern_reports_docx.py
```

Script: [scripts/generate_pattern_reports_docx.py](../../scripts/generate_pattern_reports_docx.py) — ghi `docs/patterns/composite-dashboard-stats-report-bo-cuc-mau.docx` và `singleton-resttemplate-report-bo-cuc-mau.docx`.

Nên giữ `.venv_docx/` ngoài Git (đã có trong `.gitignore` nếu repo đã cập nhật).
