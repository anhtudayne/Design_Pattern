# Huong Dan Chay Du An (Cho May Moi)

Tai lieu nay dung cho truong hop vua clone source ve va chay local tu dau.

## 1) Yeu cau cai dat

- Git
- Docker + Docker Compose
- Java 17
- Node.js 18+ (khuyen nghi 20+)
- npm 9+

## 2) Clone va vao project

```bash
git clone <repo-url>
cd Design_Pattern
```

## 3) Cau hinh bien moi truong

Du an su dung 2 file env rieng:

- `backend/.env`
- `frontend/.env`

Khong commit file `.env` len GitHub. Neu chua co gia tri that, xin tu team dev.

### 3.1 Backend env

Tao file `backend/.env` (hoac copy tu mau neu team co file `.env.example`):

```env
DB_URL=jdbc:mysql://localhost:3306/film_booking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh
DB_USERNAME=root
DB_PASSWORD=root

FRONTEND_URL=http://localhost:5173
JWT_SECRET=replace-with-strong-secret

CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_USERNAME=
REDIS_PASSWORD=
REDIS_TTL_SECONDS=600

DEV_MOMO_ENDPOINT=https://test-payment.momo.vn/v2/gateway/api/create
DEV_ACCESS_KEY=your_momo_access_key
DEV_PARTNER_CODE=your_momo_partner_code
DEV_SECRET_KEY=your_momo_secret_key
MOMO_RETURN_URL=http://localhost:5173/payment/result
MOMO_NOTIFY_URL=https://<your-ngrok-or-public-host>/api/payment/momo/ipn
MOMO_DEV_PAYMENT_OPTION_ALL_PAYMENT_SUCCESS=true
```

> Luu y:
> - Cac bien tren duoc map tu `backend/src/main/resources/application.properties`.
> - Co the dung dich vu cloud/sandbox do team cap, hoac tam thoi de placeholder de test luong co ban.

### 3.2 Frontend env

Tao file `frontend/.env`:

```env
VITE_GOOGLE_CLIENT_ID=your_google_client_id
```

## 4) Chay MySQL va Redis bang Docker

Tu thu muc goc du an:

```bash
docker compose up -d
```

`docker-compose.yml` se khoi tao:

- MySQL: `localhost:3306`
- Redis: `localhost:6379`

Va tu dong import:

- `database_schema.sql`
- `mock_data.sql`

Kiem tra container:

```bash
docker compose ps
```

Neu muon reset data tu dau:

```bash
docker compose down -v
docker compose up -d
```

## 5) Chay backend

```bash
cd backend
./mvnw spring-boot:run
```

Backend mac dinh chay tai:

- API: `http://localhost:8080`

## 6) Chay frontend

Mo terminal moi:

```bash
cd frontend
npm install
npm run dev
```

Frontend mac dinh:

- Web: `http://localhost:5173`

## 7) Kiem tra nhanh sau khi chay

- Mo `http://localhost:5173` de kiem tra giao dien.
- Goi thu API metadata (vi du): `http://localhost:8080/api/metadata/...`
- Neu loi CORS, kiem tra `FRONTEND_URL` trong `backend/.env`.

## 8) Loi thuong gap

- **Port 3306/6379/8080/5173 dang bi trung**
  - Dung process dang chiem port hoac doi port tuong ung.
- **Backend khong ket noi duoc DB**
  - Kiem tra lai `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
  - Dam bao container MySQL da `healthy` (`docker compose ps`).
- **Loi Google login**
  - Kiem tra `VITE_GOOGLE_CLIENT_ID`.
- **Loi thanh toan MoMo/IPN local**
  - Can URL public (vd: ngrok) cho `MOMO_NOTIFY_URL`.

## 9) Quy tac bao mat khi push code

- Khong push `backend/.env` va `frontend/.env`.
- Chi push code + file huong dan + file `.env.example` (neu co).
- Secret da lo phai rotate ngay (DB password, JWT secret, API keys).
