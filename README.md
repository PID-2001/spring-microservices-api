# 🛒 Spring Boot Microservices — E-Commerce Backend

A backend system for a simple e-commerce application built using **Spring Boot** and **Spring Cloud**. Instead of one big application, the system is broken into four small, independent services that each do one job and talk to each other over HTTP.

> **Who is this README for?** Anyone cloning this repository for the first time who wants to understand what it does, how it works, and how to run it locally.

---

## 📋 Table of Contents

- [What Does This Project Do?](#what-does-this-project-do)
- [Architecture](#architecture)
- [The Four Services](#the-four-services)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [How to Run It](#how-to-run-it)
- [All API Endpoints](#all-api-endpoints)
- [Example Requests](#example-requests)
- [How Services Talk to Each Other](#how-services-talk-to-each-other)
- [Error Responses](#error-responses)
- [Key Design Decisions](#key-design-decisions)
- [What Could Be Added Next](#what-could-be-added-next)
- [Configuration Reference](#configuration-reference)

---

## What Does This Project Do?

This project provides a **REST API backend** for a basic e-commerce system. It supports two core features:

- **Product Management** — create, view, update, search, and delete products
- **Order Management** — place orders, track order status, and cancel orders

When someone places an order, the system automatically looks up the product details, checks that enough stock is available, calculates the total price, and saves the order. All of this happens across multiple independent services working together.

---

## Architecture

```
                        ┌─────────────────────┐
                        │    Eureka Server     │
                        │     port: 8761       │
                        │   (Service Registry) │
                        └──────────┬──────────┘
                                   │  all services register here
              ┌────────────────────┼────────────────────┐
              │                    │                    │
     ┌────────┴────────┐  ┌────────┴────────┐  ┌───────┴─────────┐
     │   API Gateway   │  │   Product API   │  │    Order API    │
     │   port: 8080    │  │   port: 8081    │  │   port: 8082    │
     │  (entry point)  │  │  (product_db)   │  │   (order_db)    │
     └────────┬────────┘  └─────────────────┘  └─────────────────┘
              │
        all client
        requests
        come here
```

**The flow in plain English:**

1. A client (Postman, frontend app, etc.) sends a request to the **API Gateway** on port `8080`
2. The Gateway asks **Eureka** where the target service is running
3. Eureka responds with the address
4. The Gateway forwards the request to the correct service
5. The service processes it and returns a response

The client never needs to know which port each service runs on. It only ever talks to port `8080`.

---

## The Four Services

### 🗂 Eureka Server — `port 8761`

**What it is:** A service registry — the "phone book" of the system.

Every service announces itself here when it starts up. When one service needs to call another, it asks Eureka for the address instead of hardcoding it. This means services can move, scale, or restart without anything breaking.

- Dashboard: `http://localhost:8761`
- Must always start **first** before any other service

---

### 🚪 API Gateway — `port 8080`

**What it is:** The single front door for all incoming requests.

No client ever calls product-api or order-api directly. Everything comes through the gateway which then routes each request to the right service based on the URL path.

```
/api/products/**  →  forwards to  →  product-api
/api/orders/**    →  forwards to  →  order-api
```

Built with Spring Cloud Gateway (reactive/WebFlux). Uses `lb://service-name` to let Eureka handle the actual routing address dynamically.

---

### 📦 Product API — `port 8081`

**What it is:** Manages everything about products.

This service owns the `product_db` database. No other service is allowed to access that database directly — if another service needs product data, it must ask this service through its API.

Supports full CRUD: create, read, update, delete, and search by name.

Key fields: `name`, `description`, `price`, `quantity`, `createdAt`, `updatedAt`

---

### 🧾 Order API — `port 8082`

**What it is:** Manages everything about orders.

This service owns the `order_db` database. When a new order is placed, it calls the Product API (via Feign Client) to:
- Confirm the product exists
- Check there is enough stock available
- Get the current price

It then calculates the total price and saves the order with a `PENDING` status.

Key fields: `productId`, `productName`, `quantity`, `unitPrice`, `totalPrice`, `status`

Order statuses: `PENDING` → `CONFIRMED` → *(done)* or `CANCELLED`

---

## Technology Stack

| Technology | Version | Why It's Used |
|---|---|---|
| Java | 21 | Programming language |
| Spring Boot | 3.5.11 | Core application framework |
| Spring Cloud | 2025.0.1 | Microservices toolkit (gateway, discovery) |
| Spring Cloud Gateway | 4.x | Routes requests between services (reactive) |
| Netflix Eureka | - | Service registration and discovery |
| Spring Data JPA | - | Database access — turns Java classes into SQL |
| OpenFeign | - | Makes HTTP calls between services feel like local method calls |
| MySQL | 8.x | Relational database for both services |
| Lombok | - | Removes boilerplate (getters, setters, constructors) |
| Maven | 3.9.x | Builds the project and manages dependencies |

---

## Project Structure

All services follow the same layered package structure. Here is what each layer is responsible for:

```
service-name/
└── src/main/java/com/pathum/servicename/
    │
    ├── controller/          HTTP layer — receives requests, returns responses
    │                        No business logic here. Just calls the service layer.
    │
    ├── service/             Business logic layer
    │   ├── Interface        Defines what the service can do (the contract)
    │   └── impl/            The actual implementation of that contract
    │
    ├── repository/          Database layer — only talks to MySQL
    │                        Spring Data JPA generates SQL from method names
    │
    ├── entity/              Represents a database table row as a Java class
    │                        Annotated with @Entity, @Table, @Column etc.
    │
    ├── dto/
    │   ├── request/         What the client sends IN — includes validation rules
    │   └── response/        What you send BACK — you decide exactly what's visible
    │
    ├── mapper/              Converts between entity ↔ dto
    │                        Keeps conversion logic in one dedicated place
    │
    ├── exception/           Centralised error handling
    │   ├── CustomException  Thrown when something goes wrong (e.g. not found)
    │   └── GlobalHandler    Catches exceptions and returns clean JSON error responses
    │
    └── client/              Feign interfaces (order-api only)
        └── ProductClient    Declares the HTTP calls to product-api
```

> **Why not just return the entity directly?** Because your database structure is internal. If you expose the entity, any database change breaks your API contract. DTOs give you a stable public interface regardless of what's happening internally.

---

## Prerequisites

Before running this project you need:

| Requirement | Version | Notes |
|---|---|---|
| Java JDK | 21 | Check with `java -version` |
| Maven | 3.9+ | Or use the included `./mvnw` wrapper |
| MySQL | 8.x | MariaDB also works |
| Git | any | For cloning the repo |

---

## How to Run It

### Step 1 — Clone the repository

```bash
git clone <your-repo-url>
cd <repo-folder>
```

### Step 2 — Create the databases

Open your MySQL client and run:

```sql
CREATE DATABASE product_db;
CREATE DATABASE order_db;
```

### Step 3 — Set your database password

In both `product-api/src/main/resources/application.properties` and
`order-api/src/main/resources/application.properties`, update:

```properties
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD_HERE
```

### Step 4 — Start services in this exact order

Open four separate terminals:

```bash
# Terminal 1 — start this first, everything depends on it
cd eureka-server
./mvnw spring-boot:run
```

```bash
# Terminal 2
cd product-api
./mvnw spring-boot:run
```

```bash
# Terminal 3
cd order-api
./mvnw spring-boot:run
```

```bash
# Terminal 4 — start this last
cd api-gateway
./mvnw spring-boot:run
```

### Step 5 — Verify everything is running

Open `http://localhost:8761` in your browser. You should see the Eureka dashboard with all three services listed as UP:

```
APPLICATION      STATUS
───────────────────────
API-GATEWAY      UP (1)
PRODUCT-API      UP (1)
ORDER-API        UP (1)
```

If a service is missing from the list, check its terminal for startup errors.

> **Note:** Tables are created automatically by Hibernate on first startup (`ddl-auto=update`). You do not need to run any SQL scripts manually.

---

## All API Endpoints

All requests go through the API Gateway at `http://localhost:8080`.

### Product Endpoints

| Method | URL | Description | Body Required |
|---|---|---|---|
| `POST` | `/api/products` | Create a new product | Yes |
| `GET` | `/api/products` | Get all products | No |
| `GET` | `/api/products/{id}` | Get one product by ID | No |
| `GET` | `/api/products/search?name=x` | Search products by name | No |
| `PUT` | `/api/products/{id}` | Update a product | Yes |
| `DELETE` | `/api/products/{id}` | Delete a product | No |

### Order Endpoints

| Method | URL | Description | Body Required |
|---|---|---|---|
| `POST` | `/api/orders` | Place a new order | Yes |
| `GET` | `/api/orders` | Get all orders | No |
| `GET` | `/api/orders/{id}` | Get one order by ID | No |
| `GET` | `/api/orders/status/{status}` | Get orders filtered by status | No |
| `PATCH` | `/api/orders/{id}/status?status=x` | Update order status | No |
| `DELETE` | `/api/orders/{id}` | Cancel an order | No |

**Valid status values:** `PENDING`, `CONFIRMED`, `CANCELLED`

---

## Example Requests

You can test these with Postman or any HTTP client.

### Create a Product

```http
POST http://localhost:8080/api/products
Content-Type: application/json

{
    "name": "Gaming Laptop",
    "description": "High performance laptop",
    "price": 999.99,
    "quantity": 10
}
```

**Response `201 Created`:**
```json
{
    "id": 1,
    "name": "Gaming Laptop",
    "description": "High performance laptop",
    "price": 999.99,
    "quantity": 10,
    "createdAt": "2026-02-27T10:00:00",
    "updatedAt": "2026-02-27T10:00:00"
}
```

---

### Place an Order

You need a valid `productId` from a product you already created.

```http
POST http://localhost:8080/api/orders
Content-Type: application/json

{
    "productId": 1,
    "quantity": 2
}
```

**Response `201 Created`:**
```json
{
    "id": 1,
    "productId": 1,
    "productName": "Gaming Laptop",
    "quantity": 2,
    "unitPrice": 999.99,
    "totalPrice": 1999.98,
    "status": "PENDING",
    "createdAt": "2026-02-27T10:01:00",
    "updatedAt": "2026-02-27T10:01:00"
}
```

Notice that `productName`, `unitPrice`, and `totalPrice` are filled in automatically — the order service fetched the product details and calculated the total on your behalf.

---

### Confirm an Order

```http
PATCH http://localhost:8080/api/orders/1/status?status=CONFIRMED
```

### Cancel an Order

```http
DELETE http://localhost:8080/api/orders/1
```

### Search Products by Name

```http
GET http://localhost:8080/api/products/search?name=laptop
```

### Get All Pending Orders

```http
GET http://localhost:8080/api/orders/status/PENDING
```

---

## How Services Talk to Each Other

When you place an order, here is exactly what happens step by step:

```
Client  ──POST /api/orders──►  API Gateway (8080)
                                      │
                                asks Eureka:
                                "where is order-api?"
                                      │
                                Eureka: "port 8082"
                                      │
                                forwards request
                                      ▼
                                Order API (8082)
                                OrderServiceImpl
                                      │
                                calls Feign:
                                productClient.getProductById(1)
                                      │
                                asks Eureka:
                                "where is product-api?"
                                      │
                                Eureka: "port 8081"
                                      │
                                HTTP GET /api/products/1
                                      ▼
                                Product API (8081)
                                returns product details
                                      │
                                      ▼
                                Order API checks stock,
                                calculates total price,
                                saves order to order_db
                                      │
Client  ◄──OrderResponseDto──  Response returned
```

The Feign client that makes this possible looks like this:

```java
@FeignClient(name = "product-api")
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductResponseDto getProductById(@PathVariable Long id);
}
```

Feign turns this interface into a real HTTP call automatically. The `name = "product-api"` must match exactly the `spring.application.name` of the product service so Eureka can find it.

---

## Error Responses

All errors return a consistent JSON format so clients always know what to expect.

### 404 — Resource Not Found
```json
{
    "timestamp": "2026-02-27T10:00:00",
    "status": 404,
    "message": "Product not found with id: 99"
}
```

### 400 — Validation Failed
```json
{
    "timestamp": "2026-02-27T10:00:00",
    "status": 400,
    "message": "Validation failed",
    "errors": {
        "name": "Product name is required",
        "price": "Price must be greater than 0"
    }
}
```

### 400 — Insufficient Stock
```json
{
    "timestamp": "2026-02-27T10:00:00",
    "status": 400,
    "message": "Insufficient stock. Available: 3"
}
```

### 409 — Duplicate Resource
```json
{
    "timestamp": "2026-02-27T10:00:00",
    "status": 409,
    "message": "Product with name 'Gaming Laptop' already exists"
}
```

---

## Key Design Decisions

**Why microservices instead of one application?**
Each service can be developed, deployed, and scaled independently. If the order service goes down, the product service keeps running. Each team can own one service without stepping on anyone else.

**Why does each service have its own database?**
If services shared a database, a schema change in one service could break another. Separate databases mean true independence — each service owns its data completely and shares it only through API calls.

**Why is there an API Gateway?**
Without a gateway, clients would need to know the address of every service. With a gateway, clients talk to one place. It also becomes the natural place to add authentication, rate limiting, and logging later without touching any individual service.

**Why use Eureka for service discovery?**
Without Eureka, every service would need to hardcode the URLs of every other service. With Eureka, services register dynamically and find each other by name. You can run multiple instances of a service and Eureka load balances automatically.

**Why separate Request and Response DTOs?**
Request DTOs define what input is valid (with validation annotations). Response DTOs define exactly what the client sees — you can hide internal fields or sensitive data. Your database structure stays private and changes to it don't break the API contract.

---

## What Could Be Added Next

This project is a solid foundation. Here are the natural next steps:

**Immediately useful:**
- Deduct stock from product-api when an order is placed
- Add Spring Security + JWT authentication at the gateway level
- Add Resilience4J circuit breaker so order-api handles product-api being unavailable gracefully

**Medium complexity:**
- Docker + Docker Compose to run all four services with a single command
- Spring Cloud Config Server to manage all config files in one place
- Zipkin distributed tracing to visualise requests flowing across services

**Advanced:**
- Apache Kafka so services communicate through events instead of direct HTTP calls
- A Payment Service to handle transactions
- A Notification Service to send order confirmation emails
- Kubernetes deployment for production-grade orchestration

---

## Configuration Reference

<details>
<summary>Eureka Server — application.properties</summary>

```properties
spring.application.name=eureka-server
server.port=8761
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
eureka.server.wait-time-in-ms-when-sync-empty=0
```
</details>

<details>
<summary>API Gateway — application.properties</summary>

```properties
spring.application.name=api-gateway
server.port=8080

eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

spring.cloud.gateway.server.webflux.routes[0].id=product-api
spring.cloud.gateway.server.webflux.routes[0].uri=lb://product-api
spring.cloud.gateway.server.webflux.routes[0].predicates[0]=Path=/api/products/**

spring.cloud.gateway.server.webflux.routes[1].id=order-api
spring.cloud.gateway.server.webflux.routes[1].uri=lb://order-api
spring.cloud.gateway.server.webflux.routes[1].predicates[0]=Path=/api/orders/**
```
</details>

<details>
<summary>Product API — application.properties</summary>

```properties
spring.application.name=product-api
server.port=8081

spring.datasource.url=jdbc:mysql://localhost:3306/product_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```
</details>

<details>
<summary>Order API — application.properties</summary>

```properties
spring.application.name=order-api
server.port=8082

spring.datasource.url=jdbc:mysql://localhost:3306/order_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```
</details>

---

## Author

Built by **Pathum** — a hands-on learning project covering Spring Boot microservices, service discovery, API gateway routing, inter-service communication, and layered architecture patterns.
