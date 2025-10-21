# final-customer-service

> SpringBucks Customer Service - Microservice with Feign client, Circuit Breaker, Bulkhead, message routing, and distributed tracing

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.2-blue.svg)](https://spring.io/projects/spring-cloud)
[![Zipkin](https://img.shields.io/badge/Zipkin-Enabled-blue.svg)](https://zipkin.io/)
[![Feign](https://img.shields.io/badge/Feign-Client-green.svg)](https://github.com/OpenFeign/feign)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Customer-facing microservice demonstrating Spring Cloud OpenFeign integration, Resilience4j Circuit Breaker + Bulkhead patterns, RabbitMQ routing key filtering, and Zipkin distributed tracing. Supports multi-instance deployment with dynamic port configuration.

## Features

- **Declarative REST Client**: Spring Cloud OpenFeign for service-to-service communication
- **Circuit Breaker**: Resilience4j circuit breaker for `order` and `menu` endpoints
- **Bulkhead Pattern**: Concurrent call isolation (order: 1 thread, menu: 5 threads)
- **Routing Key Filtering**: RabbitMQ consumer with customer-specific routing keys
- **Distributed Tracing**: HTTP-based Zipkin integration (Micrometer Tracing)
- **Multi-Instance Support**: Environment variable-based port configuration
- **Service Discovery**: Automatic Consul registration and discovery
- **Docker Ready**: Containerized deployment with Docker Compose

## Tech Stack

- **Spring Boot 3.4.5** + **Spring Cloud 2024.0.2**
- **Spring Cloud OpenFeign** + **Apache HttpClient 5**
- **Resilience4j Spring Boot 3** (Circuit Breaker + Bulkhead)
- **Spring Cloud Stream** + **RabbitMQ** (Message consumer)
- **Micrometer Tracing + Brave Bridge**
- **Zipkin** (HTTP reporter)
- **Spring Cloud Consul Discovery**
- **Joda Money 2.0.2**
- **Lombok** + **Java 21**

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                final-customer-service                        │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌─────────────────────────────────────────────────────┐   │
│  │          CustomerController (REST API)              │   │
│  │  POST /customer/order  → Create order via Feign    │   │
│  └─────────────────────────────────────────────────────┘   │
│                         ↓ ↓ ↓                               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │         WaiterServiceClient (Feign Client)           │  │
│  │  @CircuitBreaker(name = "order")                     │  │
│  │  @Bulkhead(name = "order", type = SEMAPHORE)        │  │
│  └──────────────────────────────────────────────────────┘  │
│                         ↓                                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │       Consul Discovery (waiter-service lookup)       │  │
│  │       http://waiter-service:8080/order/              │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │   NotificationListener (RabbitMQ Consumer)           │  │
│  │   Queue: notifyOrders.customer-service-{port}        │  │
│  │   Routing Key: spring-{port}                         │  │
│  │   → Only receives own customer's notifications      │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                               │
│  Resilience4j Protection:                                    │
│  ✓ Circuit Breaker: 50% failure threshold, 5s wait          │
│  ✓ Bulkhead (order): 1 concurrent call max, 5s wait         │
│  ✓ Bulkhead (menu): 5 concurrent calls max                  │
└─────────────────────────────────────────────────────────────┘
```

## Getting Started

### Prerequisites

- **JDK 21** or higher
- **Maven 3.8+** (or use included wrapper)
- **Docker** + **Docker Compose** (for complete system deployment)
- **Running waiter-service** (for Feign client to call)

### Quick Start (Docker Compose - Recommended)

**Step 1: Build All Docker Images**

```bash
# Navigate to Chapter 16 directory
cd "Chapter 16 服務鏈路追蹤"

# Build waiter-service first
cd final-waiter-service
mvn clean package -DskipTests

# Build customer-service
cd ../final-customer-service
mvn clean package -DskipTests
# Output: springbucks/final-customer-service:0.0.1-SNAPSHOT

# Build barista-service
cd ../final-barista-service
mvn clean package -DskipTests

cd ..
```

**Step 2: Start Complete System**

```bash
# Start all 9 containers
docker-compose up -d

# Wait for services to be ready (~30 seconds)
docker-compose ps

# Expected containers:
# - final-customer-service-8090 (Up - port 8090)
# - final-customer-service-9090 (Up - port 9090)
# - final-waiter-service         (Up - port 8080)
# - final-barista-service        (Up)
# - consul, rabbitmq, zipkin, mariadb, redis
```

**Step 3: Verify Multi-Instance Deployment**

```bash
# Check Consul for registered customer-service instances
curl http://localhost:8500/v1/catalog/service/customer-service | jq

# Expected: 2 instances with different ports (8090, 9090)

# Test instance 1 (port 8090)
curl http://localhost:8090/actuator/health

# Test instance 2 (port 9090)
curl http://localhost:9090/actuator/health
```

### Standalone Execution (Development)

**Prerequisites: Start Infrastructure + waiter-service**

```bash
# Start infrastructure (Consul, RabbitMQ, Zipkin)
docker run -d --name consul -p 8500:8500 consul:1.4.5
docker run -d --name rabbitmq \
  -e RABBITMQ_DEFAULT_USER=spring \
  -e RABBITMQ_DEFAULT_PASS=spring \
  -p 5672:5672 -p 15672:15672 \
  rabbitmq:4.1.4-management
docker run -d --name zipkin -p 9411:9411 openzipkin/zipkin:3-arm64

# Start waiter-service (required for Feign client)
cd ../final-waiter-service
./mvnw spring-boot:run
```

**Run customer-service (Default Port 8090)**

```bash
./mvnw spring-boot:run
```

**Run Multiple Instances (Multi-Instance Testing)**

```bash
# Instance 1 (port 8090 - default)
./mvnw spring-boot:run

# Instance 2 (port 9090 - in another terminal)
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=9090"
```

## API Documentation

### Customer Order Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/customer/order` | Create order via Feign (calls waiter-service) |

**Example: Create Order**

```bash
# From instance 1 (port 8090)
curl -X POST http://localhost:8090/customer/order \
  -H "Content-Type: application/json"

# Expected response:
{
  "id": 1,
  "customer": "spring-8090",  ← Matches instance port
  "items": [
    {
      "name": "capuccino",
      "price": 125.00
    }
  ],
  "state": "PAID",
  "waiter": "springbucks-<uuid>",
  "discount": 95,
  "total": 118.75
}
```

## Configuration Highlights

### Feign Client Configuration

```properties
# Timeout settings
feign.client.config.default.connect-timeout=500
feign.client.config.default.read-timeout=500
```

**Feign Client Interface**:

```java
@FeignClient(name = "waiter-service")
public interface WaiterServiceClient {
    
    @CircuitBreaker(name = "order", fallbackMethod = "createOrderFallback")
    @Bulkhead(name = "order", type = Bulkhead.Type.SEMAPHORE)
    @PostMapping("/order/")
    CoffeeOrder createOrder(@RequestBody NewOrderRequest order);
    
    @CircuitBreaker(name = "menu", fallbackMethod = "getMenuFallback")
    @Bulkhead(name = "menu")
    @GetMapping("/coffee/")
    List<Coffee> getMenu();
}
```

### Resilience4j Circuit Breaker

```properties
# Circuit Breaker for 'order' endpoint
resilience4j.circuitbreaker.instances.order.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.order.wait-duration-in-open-state=5000
resilience4j.circuitbreaker.instances.order.ring-buffer-size-in-closed-state=5
resilience4j.circuitbreaker.instances.order.ring-buffer-size-in-half-open-state=3

# Circuit Breaker for 'menu' endpoint
resilience4j.circuitbreaker.instances.menu.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.menu.wait-duration-in-open-state=5000
```

**Circuit Breaker States**:
- **CLOSED**: Normal operation (≤50% failure rate)
- **OPEN**: All calls fail fast (after 50% failure rate)
- **HALF_OPEN**: Testing recovery (after 5s wait in OPEN state)

### Bulkhead (Semaphore-based)

```properties
# Order endpoint: Only 1 concurrent call allowed
resilience4j.bulkhead.instances.order.max-concurrent-calls=1
resilience4j.bulkhead.instances.order.max-wait-duration=5

# Menu endpoint: 5 concurrent calls allowed
resilience4j.bulkhead.instances.menu.max-concurrent-calls=5
resilience4j.bulkhead.instances.menu.max-wait-duration=5
```

**Bulkhead Purpose**: Limit concurrent calls to prevent resource exhaustion

### Routing Key Filtering

```properties
# Customer name = spring-{port}
customer.name=spring-${server.port}

# Consumer binding with routing key filter
spring.cloud.stream.rabbit.bindings.notifyOrders-in-0.consumer.binding-routing-key=${customer.name}
spring.cloud.stream.rabbit.bindings.notifyOrders-in-0.consumer.durable-subscription=true

# Example:
# Instance 8090: routing key = "spring-8090"
# Instance 9090: routing key = "spring-9090"
# Each instance ONLY receives its own notifications!
```

**Message Flow**:

```
waiter-service:
  StreamBridge.send(
    "notifyOrders-out-0", 
    message,
    headers: { customer: "spring-8090" }  ← Routing key
  )
      ↓
RabbitMQ Exchange (notifyOrders):
  Route by key "spring-8090"
      ↓
Queue: notifyOrders.customer-service-8090
      ↓
customer-service-8090 (ONLY this instance receives the message!)
```

### Zipkin Tracing (HTTP Reporter)

```properties
# HTTP-based tracing reporter
management.zipkin.tracing.endpoint=http://zipkin-final-spring-course:9411/api/v2/spans
management.tracing.sampling.probability=1.0  # 100% sampling
```

## Multi-Instance Testing

### Scenario 1: Instance 1 (Port 8090) Creates Order

```bash
# Step 1: Create order from instance 1
curl -X POST http://localhost:8090/customer/order \
  -H "Content-Type: application/json"

# Expected logs (customer-service-8090):
# Create order: 1
# Order is PAID: CoffeeOrder(id=1, customer=spring-8090, ...)
# Order 1 is READY, I'll take it.  ← Only 8090 receives this!

# Instance 9090 logs: (NOTHING - correct!)
```

### Scenario 2: Instance 2 (Port 9090) Creates Order

```bash
# Step 2: Create order from instance 2
curl -X POST http://localhost:9090/customer/order \
  -H "Content-Type: application/json"

# Expected logs (customer-service-9090):
# Create order: 2
# Order is PAID: CoffeeOrder(id=2, customer=spring-9090, ...)
# Order 2 is READY, I'll take it.  ← Only 9090 receives this!

# Instance 8090 logs: (NOTHING - correct!)
```

**Routing Key Verification**:

```bash
# Check RabbitMQ Management UI
open http://localhost:15672

# Navigate to: Queues tab
# You should see:
# - notifyOrders.customer-service-8090  (binding key: spring-8090)
# - notifyOrders.customer-service-9090  (binding key: spring-9090)
```

## Monitoring & Observability

### Distributed Tracing

```bash
# View trace in Zipkin UI
open http://localhost:9411

# Trace flow example:
# customer-service-8090 (POST /customer/order)  [Span 1]
#   → waiter-service (POST /order/)              [Span 2 - Feign call]
#     → waiter-service (PUT /order/{id})         [Span 3 - Update state]
#       → RabbitMQ (newOrders)                   [Span 4-6]
#         → barista-service (process)            [Span 7-9]
#           → RabbitMQ (finishedOrders)          [Span 10-12]
#             → waiter-service (update)          [Span 13-14]
#               → RabbitMQ (notifyOrders)        [Span 15-17 - routing key!]
#                 → customer-service-8090 (notify) [Span 18 - only 8090!]

# Total spans: 20 (includes all HTTP, RabbitMQ, and internal processing)
```

### Circuit Breaker Monitoring

```bash
# Check circuit breaker state
curl http://localhost:8090/actuator/health | jq '.components.circuitBreakers'

# Circuit breaker metrics
curl http://localhost:8090/actuator/metrics/resilience4j.circuitbreaker.state

# Trigger circuit breaker (stop waiter-service, then call order endpoint 5+ times)
for i in {1..6}; do
  curl -X POST http://localhost:8090/customer/order
done

# Check state again (should transition to OPEN)
curl http://localhost:8090/actuator/health | jq '.components.circuitBreakers'
```

### Bulkhead Monitoring

```bash
# Bulkhead metrics
curl http://localhost:8090/actuator/metrics/resilience4j.bulkhead.available.concurrent.calls

# Test bulkhead (order endpoint allows only 1 concurrent call)
# In terminal 1:
curl -X POST http://localhost:8090/customer/order  # Call 1 - allowed

# In terminal 2 (within 5s):
curl -X POST http://localhost:8090/customer/order  # Call 2 - rejected!
# Expected: BulkheadFullException
```

## Performance & Best Practices

### Feign Client Optimization

```java
// Use Apache HttpClient 5 for connection pooling
@Bean
public Client feignClient() {
    return new ApacheHttp5Client(httpClient());
}

@Bean
public CloseableHttpClient httpClient() {
    return HttpClients.custom()
        .setMaxConnTotal(200)
        .setMaxConnPerRoute(50)
        .setDefaultRequestConfig(RequestConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(500))
            .setResponseTimeout(Timeout.ofMilliseconds(500))
            .build())
        .build();
}
```

### Circuit Breaker Tuning

| Environment | Failure Threshold | Wait Duration | Reasoning |
|-------------|-------------------|---------------|-----------|
| **Development** | 50% | 5s | Current setting (easy testing) |
| **Production** | 70% | 30s | Allow temporary failures, longer recovery |
| **Critical Services** | 30% | 10s | Faster failure detection |

### Bulkhead Recommendations

| Endpoint Type | Max Concurrent Calls | Reasoning |
|---------------|----------------------|-----------|
| **Order Creation** | 1-5 | Prevent database overload |
| **Menu Queries** | 10-50 | Read-only, can handle more |
| **Admin APIs** | 1-2 | Protect sensitive operations |

## Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| **Feign call fails** | Check waiter-service is running and registered in Consul |
| **No notification received** | Verify routing key matches customer name |
| **Circuit breaker always OPEN** | Check waiter-service health and reduce failure threshold |
| **Bulkhead rejects requests** | Increase `max-concurrent-calls` or reduce timeout |

### Logs Analysis

```bash
# View full logs
docker logs -f final-spring-course-final-customer-service-8090-1

# Filter Feign calls
docker logs final-spring-course-final-customer-service-8090-1 | grep "Feign"

# Filter notifications
docker logs final-spring-course-final-customer-service-8090-1 | grep "Order .* is READY"
```

## Comparison with Other Projects

| Project | Feign | Circuit Breaker | Bulkhead | Multi-Instance | Routing Key |
|---------|-------|-----------------|----------|----------------|-------------|
| **final-customer** | ✅ | ✅ (order, menu) | ✅ (order, menu) | ✅ Port-based | ✅ Dynamic |
| **lazy-customer** | ✅ | ✅ (order, menu) | ✅ (order, menu) | ✅ Port-based | ✅ Dynamic |
| **simple-customer** | ❌ | ❌ | ❌ | ❌ | ❌ |

## References

- [Spring Cloud OpenFeign](https://spring.io/projects/spring-cloud-openfeign)
- [Resilience4j Circuit Breaker](https://resilience4j.readme.io/docs/circuitbreaker)
- [Resilience4j Bulkhead](https://resilience4j.readme.io/docs/bulkhead)
- [Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream)
- [RabbitMQ Routing](https://www.rabbitmq.com/tutorials/tutorial-four-java.html)

## License

This project is licensed under the MIT License.

## Contact

We focus on Agile Project Management, IoT application development, and Domain-Driven Design (DDD), combining advanced technologies with practical experience to create flexible software solutions.

- **Facebook**: [風清雲談](https://www.facebook.com/profile.php?id=61576838896062)
- **LinkedIn**: [linkedin.com/in/chu-kuo-lung](https://www.linkedin.com/in/chu-kuo-lung)
- **YouTube**: [雲談風清](https://www.youtube.com/channel/UCXDqLTdCMiCJ1j8xGRfwEig)
- **Blog**: [風清雲談](https://blog.fengqing.tw/)
- **Email**: [fengqing.tw@gmail.com](mailto:fengqing.tw@gmail.com)

---

**Last Updated**: 2025-10-21  
**Maintainer**: FengQing Team
