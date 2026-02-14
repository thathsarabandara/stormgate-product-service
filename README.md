# Product Service

Multi-tenant Product Microservice for E-commerce SaaS Platform

## 📋 Overview

The Product Service is responsible for:

- Managing tenant-specific product catalogs
- Creating, updating, deleting products
- Managing categories and product variants
- Handling inventory (stock levels)
- Supporting product search & filtering
- Enforcing subscription-based product limits
- Publishing domain events (ProductCreated, StockUpdated)
- Maintaining strict multi-tenant isolation

## 🏗️ Architecture

```
┌─────────────────────────────────────────┐
│              API Gateway               │
└────────────────────┬────────────────────┘
                     │
              ┌──────▼──────┐
              │ Product Svc │
              └──────┬──────┘
                     │
        ┌────────────┴────────────┐
        │                         │
   ┌────▼──────┐           ┌──────▼──────┐
   │ PostgreSQL│           │   Redis     │
   │  Database │           │   Cache     │
   └────┬──────┘           └──────┬──────┘
        │                         │
        └────────────┬────────────┘
                     │
            ┌────────▼────────┐
            │  Message Broker  │
            │ (Kafka/RabbitMQ) │
            └────────┬────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
   ┌────▼──┐   ┌────▼────┐  ┌────▼────┐
   │Cart   │   │Order    │  │AI Chat  │
   │Service│   │Service  │  │Service  │
   └───────┘   └─────────┘  └─────────┘
```

## 📊 Database Schema

### Products Table

```sql
CREATE TABLE products (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL NOT NULL,
    currency VARCHAR(3) NOT NULL,
    stock INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### Product Categories Table

```sql
CREATE TABLE categories (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT
);
```

### Product Variants Table (Optional)

```sql
CREATE TABLE product_variants (
    id UUID PRIMARY KEY,
    product_id UUID REFERENCES products(id),
    sku VARCHAR(100) UNIQUE,
    variant_name VARCHAR(255),
    price DECIMAL,
    stock INT
);
```

### Product Images Table

```sql
CREATE TABLE product_images (
    id UUID PRIMARY KEY,
    product_id UUID REFERENCES products(id),
    image_url TEXT NOT NULL,
    is_primary BOOLEAN DEFAULT false
);
```

## 🔄 Product State Machine

```
DRAFT
   ├─→ ACTIVE
   │     ├─→ OUT_OF_STOCK
   │     ├─→ ARCHIVED
   │     └─→ INACTIVE
   └─→ ARCHIVED
```

### Valid Transitions

- DRAFT → ACTIVE, ARCHIVED
- ACTIVE → OUT_OF_STOCK, INACTIVE, ARCHIVED
- OUT_OF_STOCK → ACTIVE
- INACTIVE → ACTIVE
- ARCHIVED → No transitions (terminal)

## 🔌 API Endpoints

### Create Product

```
POST /api/v1/products
Headers:
  X-Tenant-Id: <tenant-id>
```

**Request Body:**

```json
{
  "name": "Gaming Laptop",
  "description": "High performance laptop",
  "price": 2500.00,
  "currency": "USD",
  "stock": 10,
  "categoryId": "cat_001"
}
```

**Response (201 Created):**

```json
{
  "id": "prod_123",
  "name": "Gaming Laptop",
  "status": "DRAFT",
  "price": 2500.00,
  "stock": 10,
  "createdAt": "2026-02-01T10:00:00Z"
}
```

### Get Product by ID

```
GET /api/v1/products/{productId}
Headers:
  X-Tenant-Id: <tenant-id>
```

### List Products (with filtering)

```
GET /api/v1/products?status=ACTIVE&page=0&size=10&search=laptop
Headers:
  X-Tenant-Id: <tenant-id>
```

### Update Product

```
PUT /api/v1/products/{productId}
```

### Update Stock

```
PATCH /api/v1/products/{productId}/stock
```

### Delete / Archive Product

```
DELETE /api/v1/products/{productId}
```

## 📡 Event Contracts

### ProductCreatedEvent (Published)

```json
{
  "eventType": "PRODUCT_CREATED",
  "productId": "prod_123",
  "tenantId": "tenant_123",
  "price": 2500.00,
  "timestamp": "2026-02-01T10:00:00Z"
}
```

### StockUpdatedEvent (Published)

```json
{
  "eventType": "STOCK_UPDATED",
  "productId": "prod_123",
  "tenantId": "tenant_123",
  "newStock": 8,
  "timestamp": "2026-02-01T11:00:00Z"
}
```

### OrderCreatedEvent (Consumed)

Used to reduce stock when an order is placed.

## 🚀 Getting Started

### Prerequisites

- Java 21
- Maven 3.8+
- PostgreSQL 15+
- Kafka 7.0+
- Docker & Docker Compose

### Local Development Setup

#### 1. Start Dependencies

```bash
docker-compose up -d
```

Starts:

- PostgreSQL
- Kafka
- Zookeeper
- Redis
- Product Service

#### 2. Build

```bash
mvn clean install
```

#### 3. Run

```bash
mvn spring-boot:run
```

Or run the JAR file directly:

```bash
java -jar target/stormgate-product-service-0.0.1-SNAPSHOT.jar
```

#### 4. Access

- Swagger: http://localhost:8082/swagger-ui.html
- Health: http://localhost:8082/api/v1/health
- Metrics: http://localhost:8082/actuator/health
- Prometheus: http://localhost:8082/actuator/prometheus

## 📦 Project Structure

```
product-service/
├── src/main/java/com/example/stormgate_product_service/
│   ├── StormgateProductServiceApplication.java
│   ├── config/
│   │   ├── KafkaConfig.java
│   │   ├── RedisConfig.java
│   │   └── OpenApiConfig.java
│   ├── controller/
│   │   ├── ProductController.java
│   │   ├── CategoryController.java
│   │   └── HealthController.java
│   ├── service/
│   │   ├── ProductService.java
│   │   ├── CategoryService.java
│   │   ├── InventoryService.java
│   │   ├── ProductStateMachine.java
│   │   ├── ProductEventPublisher.java
│   │   └── OrderEventListener.java
│   ├── domain/
│   │   ├── Product.java
│   │   ├── Category.java
│   │   ├── ProductVariant.java
│   │   ├── ProductImage.java
│   │   └── ProductStatus.java
│   ├── repository/
│   │   ├── ProductRepository.java
│   │   ├── CategoryRepository.java
│   │   ├── ProductVariantRepository.java
│   │   └── ProductImageRepository.java
│   ├── event/
│   │   ├── ProductCreatedEvent.java
│   │   ├── StockUpdatedEvent.java
│   │   └── OrderCreatedEvent.java
│   ├── dto/
│   │   ├── ProductDTO.java
│   │   ├── CreateProductRequest.java
│   │   ├── UpdateProductRequest.java
│   │   ├── StockUpdateRequest.java
│   │   └── CategoryDTO.java
│   ├── mapper/
│   │   ├── ProductMapper.java
│   │   └── CategoryMapper.java
│   └── exception/
│       ├── ProductNotFoundException.java
│       ├── CategoryNotFoundException.java
│       ├── InsufficientStockException.java
│       ├── InvalidProductStatusException.java
│       └── GlobalExceptionHandler.java
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/
│       └── V1__Initialize_Product_Schema.sql
├── src/test/java/com/example/stormgate_product_service/
│   └── service/ProductStateMachineTest.java
├── .github/workflows/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## 🔐 Security

### Multi-Tenant Isolation

- All queries filtered by tenant_id
- X-Tenant-Id header required
- Cross-tenant access blocked

### Authentication & Authorization

- JWT validation (to be implemented)
- Role-based permissions (to be implemented)
- Admin-only product management

## 📈 Monitoring & Observability

### Metrics

- product_count
- stock_updates_total
- product_creation_rate
- out_of_stock_products

### Health Checks

- DB connectivity
- Kafka connectivity
- Redis connectivity

## 🔄 CI/CD Pipeline

### GitHub Actions Flow

- Build & Test
- Static Analysis (SonarCloud)
- Docker Build
- Push to Registry
- Deploy to Cloud
- Smoke Tests

## 🛠️ Troubleshooting

### Check PostgreSQL

```bash
docker exec product-service-postgres psql -U postgres -d product_db -c "\dt"
```

### Check Kafka

```bash
docker exec product-service-kafka kafka-topics --list --bootstrap-server localhost:9092
```

### Check Redis

```bash
docker exec product-service-redis redis-cli ping
```

### View Logs

```bash
docker compose logs product-service -f
```

## 📝 Performance Considerations

- Target Response Time: <250ms
- Indexed fields: tenant_id, status, price
- Redis caching for product listings
- Pagination required for list endpoints
- Async stock updates via Kafka

## 🔗 Related Services

- Cart Service (validates product before adding)
- Order Service (reduces stock)
- Subscription Service (limits number of products per plan)
- AI Chatbot Service (product recommendations)
- Notification Service (low stock alerts)

## 📄 License

Apache License 2.0

## 📧 Support

For issues or questions:

- Open GitHub issue
- Contact: support@company.com
