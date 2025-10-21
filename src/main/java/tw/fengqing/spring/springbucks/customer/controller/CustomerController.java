package tw.fengqing.spring.springbucks.customer.controller;

import tw.fengqing.spring.springbucks.customer.integration.CoffeeOrderService;
import tw.fengqing.spring.springbucks.customer.integration.CoffeeService;
import tw.fengqing.spring.springbucks.customer.model.Coffee;
import tw.fengqing.spring.springbucks.customer.model.CoffeeOrder;
import tw.fengqing.spring.springbucks.customer.model.NewOrderRequest;
import tw.fengqing.spring.springbucks.customer.model.OrderState;
import tw.fengqing.spring.springbucks.customer.model.OrderStateRequest;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import io.vavr.control.Try;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/customer")
@Slf4j
public class CustomerController {
    @Autowired
    private CoffeeService coffeeService;
    @Autowired
    private CoffeeOrderService coffeeOrderService;
    @Value("${customer.name}")
    private String customer;
    private CircuitBreaker circuitBreaker;
    private Bulkhead bulkhead;

    public CustomerController(CircuitBreakerRegistry circuitBreakerRegistry,
                              BulkheadRegistry bulkheadRegistry) {
        circuitBreaker = circuitBreakerRegistry.circuitBreaker("menu");
        bulkhead = bulkheadRegistry.bulkhead("menu");
    }

    @GetMapping("/menu")
    public List<Coffee> readMenu() {
        return Try.ofSupplier(
                Bulkhead.decorateSupplier(bulkhead,
                        CircuitBreaker.decorateSupplier(circuitBreaker,
                                () -> coffeeService.getAll())))
                .recover(CallNotPermittedException.class, Collections.emptyList())
                .recover(BulkheadFullException.class, Collections.emptyList())
                .get();
    }

    @PostMapping("/order")
    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "order", fallbackMethod = "createOrderFallback")
    @io.github.resilience4j.bulkhead.annotation.Bulkhead(name = "order", fallbackMethod = "createOrderFallback")
    public CoffeeOrder createAndPayOrder() {
        log.info("正常處理：創建訂單 for customer: {}", customer);
        NewOrderRequest orderRequest = NewOrderRequest.builder()
                .customer(customer)
                .items(Arrays.asList("capuccino"))
                .build();
        CoffeeOrder order = coffeeOrderService.create(orderRequest);
        log.info("Create order: {}", order != null ? order.getId() : "-");
        order = coffeeOrderService.updateState(order.getId(),
                OrderStateRequest.builder().state(OrderState.PAID).build());
        log.info("Order is PAID: {}", order);
        return order;
    }

    /**
     * Fallback 方法：當熔斷器打開或隔艙已滿時調用
     * 
     * @param e 觸發 fallback 的異常
     * @return null 表示訂單創建失敗
     */
    private CoffeeOrder createOrderFallback(Exception e) {
        log.error("⚠️ 服務降級！原因: {}", e.getClass().getSimpleName());
        log.error("⚠️ 錯誤訊息: {}", e.getMessage());
        
        // 判斷具體異常類型
        if (e instanceof io.github.resilience4j.circuitbreaker.CallNotPermittedException) {
            log.error("⚠️ 熔斷器已打開，服務暫時不可用");
        } else if (e instanceof io.github.resilience4j.bulkhead.BulkheadFullException) {
            log.error("⚠️ 隔艙已滿，系統繁忙請稍後再試");
        } else {
            log.error("⚠️ 服務調用失敗: {}", e.getMessage());
        }
        
        // 返回 null 表示訂單創建失敗
        return null;
    }
}