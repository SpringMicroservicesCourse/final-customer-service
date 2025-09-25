# SpringBucks 客戶微服務 ⚡

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.2-blue.svg)](https://spring.io/projects/spring-cloud)
[![Docker](https://img.shields.io/badge/Docker-Containerized-blue.svg)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 專案介紹

本專案為 SpringBucks 咖啡店系統的客戶微服務，負責處理客戶訂單、與服務員服務的整合、以及分散式系統的鏈路追蹤。此服務展示了現代微服務架構中客戶端服務的設計模式，包含服務發現、負載均衡、熔斷機制等核心功能。

**核心功能：**
- **客戶訂單管理**：處理客戶下單、訂單查詢、訂單狀態追蹤
- **服務整合**：透過 OpenFeign 與服務員服務進行整合
- **負載均衡**：支援多實例部署，自動負載分散
- **熔斷保護**：整合 Resilience4j 提供服務熔斷與重試機制
- **鏈路追蹤**：整合 Zipkin 進行分散式鏈路追蹤
- **健康監控**：提供完整的服務健康檢查與監控指標

> 💡 **為什麼選擇此微服務架構？**
> - 展示客戶端微服務的完整設計模式
> - 整合現代化的服務治理與監控工具
> - 支援高可用性與容錯機制
> - 提供完整的分散式系統追蹤能力

### 🎯 專案特色

- **微服務整合**：使用 OpenFeign 實現聲明式 HTTP 客戶端
- **負載均衡**：支援多實例部署，自動負載分散
- **熔斷保護**：整合 Resilience4j 提供服務熔斷與重試機制
- **鏈路追蹤**：整合 Zipkin 進行分散式系統的請求追蹤
- **容器化部署**：支援 Docker 打包與部署，便於環境一致性
- **監控整合**：支援 Prometheus 指標收集與健康檢查

## 技術棧

### 核心框架
- **Spring Boot 3.4.5** - 主框架，提供自動配置與生產就緒功能
- **Spring Cloud 2024.0.2** - 微服務框架，提供服務整合與治理功能
- **Spring Cloud OpenFeign** - 聲明式 HTTP 客戶端
- **Resilience4j** - 熔斷器與重試機制

### 微服務與監控
- **Consul** - 服務註冊與發現中心
- **Zipkin** - 分散式鏈路追蹤系統
- **Micrometer** - 應用程式指標收集
- **Spring Cloud Stream** - 訊息驅動微服務框架

### 開發工具與輔助
- **Lombok** - 減少樣板程式碼
- **Apache HttpClient** - HTTP 客戶端連線池管理
- **AspectJ** - 面向切面程式設計
- **Docker** - 容器化部署
- **Maven** - 專案建構與依賴管理

## 專案結構

```
final-customer-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── tw/fengqing/spring/springbucks/customer/
│   │   │       ├── CustomerServiceApplication.java          # 主要應用程式入口
│   │   │       ├── controller/                             # 控制器層
│   │   │       │   └── CustomerController.java           # 客戶控制器
│   │   │       ├── model/                                 # 資料模型
│   │   │       │   ├── Coffee.java                      # 咖啡實體
│   │   │       │   ├── CoffeeOrder.java                 # 咖啡訂單實體
│   │   │       │   └── OrderState.java                  # 訂單狀態枚舉
│   │   │       ├── service/                              # 業務邏輯層
│   │   │       │   └── CustomerService.java            # 客戶業務邏輯
│   │   │       ├── support/                             # 支援類別
│   │   │       │   └── CustomConnectionKeepAliveStrategy.java # 自定義連線策略
│   │   │       └── config/                              # 配置類別
│   │   │           └── FeignTracingConfig.java         # Feign 鏈路追蹤配置
│   │   └── resources/
│   │       ├── application.properties                    # 應用程式配置
│   │       └── bootstrap.properties                      # 啟動配置
│   └── test/
├── Dockerfile                                            # Docker 建構檔案
├── pom.xml                                              # Maven 專案配置
└── README.md                                            # 專案說明文件
```

## 快速開始

### 前置需求
- **Java 21** - 最新 LTS 版本的 Java
- **Maven 3.6+** - 專案建構工具
- **Docker** - 容器化部署（選用）
- **Consul** - 服務註冊中心（或使用 Docker 容器）
- **RabbitMQ** - 訊息佇列系統（或使用 Docker 容器）
- **Zipkin** - 鏈路追蹤系統（或使用 Docker 容器）

### 安裝與執行

1. **克隆此倉庫：**
```bash
git clone https://github.com/username/springbucks-microservices.git
```

2. **進入專案目錄：**
```bash
cd Chapter\ 16\ 服務鏈路追蹤/final-customer-service
```

3. **編譯專案：**
```bash
mvn clean compile
```

4. **執行應用程式：**
```bash
mvn spring-boot:run
```

### Docker 部署

1. **建構 Docker 映像檔：**
```bash
mvn clean package dockerfile:build
```

2. **執行 Docker 容器：**
```bash
# 預設端口 8090
docker run -p 8090:8090 springbucks/final-customer-service:0.0.1-SNAPSHOT

# 自定義端口 9090
docker run -p 9090:9090 -e SERVER_PORT=9090 springbucks/final-customer-service:0.0.1-SNAPSHOT
```

3. **使用 Docker Compose 啟動完整環境：**
```bash
cd Chapter\ 16\ 服務鏈路追蹤
docker-compose up -d
```

## 進階說明

### 環境變數
```properties
# 服務端口配置
SERVER_PORT=8090

# Consul 配置
SPRING_CLOUD_CONSUL_HOST=localhost
SPRING_CLOUD_CONSUL_PORT=8500

# Zipkin 配置
MANAGEMENT_TRACING_ENDPOINT=http://localhost:9411/api/v2/spans

# HTTP 客戶端配置
HTTP_CLIENT_MAX_CONN_TOTAL=200
HTTP_CLIENT_MAX_CONN_PER_ROUTE=20
```

### 設定檔說明
```properties
# application.properties 主要設定
spring.application.name=customer-service
server.port=${SERVER_PORT:8090}

# 服務發現配置
spring.cloud.consul.discovery.service-name=${spring.application.name}
spring.cloud.consul.discovery.health-check-interval=10s

# Feign 配置
feign.client.config.default.connect-timeout=5000
feign.client.config.default.read-timeout=10000
```

## API 端點

### 客戶訂單管理
- `GET /customer/coffee` - 查詢所有咖啡產品
- `GET /customer/coffee/{id}` - 查詢特定咖啡產品
- `POST /customer/order` - 建立新訂單
- `GET /customer/order/{id}` - 查詢特定訂單

### 監控端點
- `GET /actuator/health` - 健康檢查
- `GET /actuator/metrics` - 應用程式指標
- `GET /actuator/prometheus` - Prometheus 指標

## 服務整合

### 與服務員服務整合
本服務透過 OpenFeign 與服務員服務進行整合：

```java
@FeignClient(name = "waiter-service")
public interface WaiterServiceClient {
    @GetMapping("/coffee")
    List<Coffee> getAllCoffee();
    
    @GetMapping("/coffee/{id}")
    Coffee getCoffeeById(@PathVariable Long id);
    
    @PostMapping("/order")
    CoffeeOrder createOrder(@RequestBody CoffeeOrder order);
}
```

### 熔斷機制配置
```java
@Bean
public CircuitBreakerConfig circuitBreakerConfig() {
    return CircuitBreakerConfig.custom()
        .failureRateThreshold(50)
        .waitDurationInOpenState(Duration.ofMillis(1000))
        .slidingWindowSize(2)
        .build();
}
```

## 參考資源

- [Spring Boot 官方文件](https://spring.io/projects/spring-boot)
- [Spring Cloud OpenFeign 官方文件](https://spring.io/projects/spring-cloud)
- [Resilience4j 官方文件](https://resilience4j.readme.io/)
- [Consul 官方文件](https://www.consul.io/docs)
- [Zipkin 官方文件](https://zipkin.io/)

## 注意事項與最佳實踐

### ⚠️ 重要提醒

| 項目 | 說明 | 建議做法 |
|------|------|----------|
| 服務整合 | OpenFeign 超時設定 | 設定適當的連線與讀取超時時間 |
| 熔斷機制 | Resilience4j 配置 | 根據業務需求調整熔斷參數 |
| 負載均衡 | 多實例部署 | 確保服務實例的健康檢查 |
| 鏈路追蹤 | Zipkin 採樣率 | 生產環境調整採樣率以降低效能影響 |

### 🔒 最佳實踐指南

- **服務整合**：使用 OpenFeign 實現聲明式 HTTP 客戶端，提升程式碼可讀性
- **熔斷保護**：整合 Resilience4j 提供服務熔斷與重試機制，提升系統穩定性
- **連線管理**：使用連線池管理 HTTP 連線，提升效能並避免連線洩漏
- **監控告警**：整合 Prometheus 和 Grafana 進行系統監控
- **容器化**：使用 Docker 確保環境一致性，便於部署和擴展

## 授權說明

本專案採用 MIT 授權條款，詳見 LICENSE 檔案。

## 關於我們

我們主要專注在敏捷專案管理、物聯網（IoT）應用開發和領域驅動設計（DDD）。喜歡把先進技術和實務經驗結合，打造好用又靈活的軟體解決方案。

## 聯繫我們

- **FB 粉絲頁**：[風清雲談 | Facebook](https://www.facebook.com/profile.php?id=61576838896062)
- **LinkedIn**：[linkedin.com/in/chu-kuo-lung](https://www.linkedin.com/in/chu-kuo-lung)
- **YouTube 頻道**：[雲談風清 - YouTube](https://www.youtube.com/channel/UCXDqLTdCMiCJ1j8xGRfwEig)
- **風清雲談 部落格**：[風清雲談](https://blog.fengqing.tw/)
- **電子郵件**：[fengqing.tw@gmail.com](mailto:fengqing.tw@gmail.com)

---

**📅 最後更新：2025-01-27**  
**👨‍💻 維護者：風清雲談團隊**
