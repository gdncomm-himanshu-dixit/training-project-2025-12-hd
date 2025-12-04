# Ecommerce Microservices Architecture

## 1. High-level Overview

### Services

#### API Gateway (Spring Cloud Gateway)
- **Responsibilities**: 
  - Authentication (JWT validation)
  - Authorization
  - Routing
  - Rate limiting
  - Header sanitization
  - Token blacklist checking
  - Request/response logging
- **Purpose**: Exposes public endpoints to frontend

#### Member Service (Spring Boot + PostgreSQL)
- **Responsibilities**: 
  - User registration
  - Login
  - User profile management
  - JWT issuance
  - User management
- **Database**: `member_db` (PostgreSQL)

#### Product Service (Spring Boot + MongoDB)
- **Responsibilities**: 
  - Product CRUD operations
  - Search functionality (text + filters)
  - Product details
  - Catalog management
- **Database**: `product` (MongoDB), Collection: `products`

#### Cart Service (Spring Boot + MongoDB)
- **Responsibilities**: 
  - Cart operations (add/view/remove)
  - User validation via MemberFeignClient
  - Short-term cart storage
- **Database**: `cart` (MongoDB), Collection: `carts`

#### Redis
- **Responsibilities**: 
  - Rate limiter backing store
  - Token blacklist (logout/invalidated tokens)
  - Caching (optional)

---

## 2. Tech Choices & Rationale

- **Spring Boot microservices**: Modern, production-ready framework
- **PostgreSQL for Member Service**: Strong relational needs for user data
- **MongoDB for Product & Cart Services**: Document model fits product records and cart structure
- **Spring Cloud Gateway**: Central authentication and authorization
- **JWT**: Stateless authentication mechanism
- **Feign**: Inter-service communication with typed interfaces
- **Redis**: Short-lived caches and token blacklist storage

---

## 3. Data Flow & JWT Lifecycle

### Member Issues JWT

1. **User Registration**: `POST /api/v1/member/register` (Member Service)

2. **User Login**: `POST /api/v1/member/login`
   - Member Service validates credentials
   - Returns JWT signed with shared secret/key
   - Includes `userId` claim (UUID) and optional roles

3. **Frontend Storage**: 
   - JWT stored in HttpOnly cookie or client storage
   - Calls backend via Gateway with header: `Authorization: Bearer <token>`

### Gateway JWT Authentication Filter

The Gateway's `JwtAuthFilter` performs the following:

1. **Verifies token signature** using the same secret/key
2. **Checks expiry** timestamp
3. **Checks token blacklist** in Redis
4. **Extracts claims**: `userId` and `username`
5. **Header Sanitization**:
   - Removes untrusted incoming identity headers (`X-USERID`, `X-USERNAME`)
   - Injects trusted identity headers:
     - `X-USERID: <UUID>`
     - `X-USERNAME: <name>`
6. **Forwards request** to downstream service

### Downstream Service Authentication

- Cart/Product services read `X-USERID` header
- Cart Service requires this header
- Treat Gateway-injected headers as authoritative identity

---

## 4. JWT Format & Claims

### Header
```json
{
  "alg": "HS256"
}
```
*Note: Use RS256 for public/private key pairs*

### Claims
- **`sub`** (subject): Username or email
- **`userId`**: UUID (string), required for downstream microservices
- **`iat`**, **`exp`**: IssuedAt, expiry timestamps
- **`roles`** or **`scope`** (optional): User permissions

### Security Notes
- Use a long random secret for HS256
- For RS256: Use private key on issuer and public key in Gateway for verification

---

## 5. Routing & Expected Headers

### Gateway Rules

The Gateway accepts requests from frontend and enforces header rules.

#### Required Headers for Protected Endpoints (Cart, Checkout)
- `Authorization: Bearer <token>`
- `X-USERID: <uuid>` (Gateway-injected)
- `X-USERNAME` (Optional, Gateway-injected)

### Security Best Practice
Downstream services should **NOT** trust incoming user headers and must rely **only** on Gateway-injected headers.

---

## 6. Logout & Token Revocation

### Blacklist Approach (Selected Implementation)

**On Logout:**
1. Store the token's `jti` (JWT ID) or token string in Redis
2. Set TTL = remaining token lifetime
3. Gateway checks Redis on each request to ensure token is not revoked

**Benefits:**
- Redis provides fast lookup
- Suitable for high-traffic systems

### Implementation Details

- Add `jti` (unique ID) claim in JWT or use the whole token string as Redis key
- **Redis Key Pattern**: `blacklist:<jti>`
- **Redis Value**: Reason/timestamp
- **TTL**: `exp - now` (remaining token lifetime)

---

## 7. Rate Limiting

### Configuration
- Implement Spring Cloud Gateway `RequestRateLimiter` backed by Redis
- Configure `replenishRate` and `burstCapacity` per route

### Per-Service Rates
- **Cart Service**: Lower rate (resource-intensive operations)
- **Product Search**: Higher rate (read-heavy operations)

---

## 8. Feign Usage

### Inter-Service Communication

- **ProductService** and **CartService** expose REST controllers
- **CartService** uses `@FeignClient` to call:
  - **MemberService**: User validation (`existsById`)
  - **ProductService**: Fetch product details

### Resilience
- Configure Feign timeout settings
- Implement circuit-breaker (Resilience4j) to avoid cascading failures

---

## 9. API List

### Member Service
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/member/register` | User registration |
| `POST` | `/api/v1/member/login` | User login, returns JWT |
| `GET` | `/api/v1/member/exists/{id}` | Check if user exists |

### Product Service
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/product/search?keyword=&page=&size=` | Search products |
| `GET` | `/api/v1/product/{id}` | Get product details |
| `POST` | `/api/v1/product` | Create product (internal) |

### Cart Service
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/v1/cart/add` | Add item to cart | ✅ Protected |
| `GET` | `/api/v1/cart/view` | View cart contents | ✅ Protected |
| `DELETE` | `/api/v1/cart/remove/{productId}` | Remove item from cart | ✅ Protected |

### API Gateway
**Routes:**
- `/api/v1/product/**`
- `/api/v1/cart/**`
- `/api/v1/member/**`

**Features:**
- JWT Authentication Filter
- Redis Rate Limiter
- Header sanitization
- Token blacklist verification

---

## Architecture Diagram

```
┌─────────────┐
│   Frontend  │
└──────┬──────┘
       │ JWT in Authorization header
       ▼
┌─────────────────────────────────────┐
│       API Gateway                   │
│  - JWT Validation                   │
│  - Header Injection (X-USERID)      │
│  - Rate Limiting                    │
│  - Token Blacklist Check            │
└──────┬──────────────────────────────┘
       │
   ┌───┴────┬──────────┬──────────┐
   ▼        ▼          ▼          ▼
┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐
│ Member │ │Product │ │  Cart  │ │ Redis  │
│Service │ │Service │ │Service │ │        │
└────┬───┘ └────────┘ └───┬────┘ └────────┘
     │                    │
     ▼                    ▼
┌──────────┐         ┌──────────┐
│PostgreSQL│         │ MongoDB  │
└──────────┘         └──────────┘
```