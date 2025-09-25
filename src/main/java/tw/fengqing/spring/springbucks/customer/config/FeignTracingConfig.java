package tw.fengqing.spring.springbucks.customer.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 追蹤配置
 * 用於手動注入追蹤標頭到 Feign 請求中
 * 解決 Micrometer Tracing 在 Spring Cloud 2024.0.2 中 Feign 追蹤不足的問題
 */
@Configuration
public class FeignTracingConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * 手動注入追蹤標頭的請求攔截器
     */
    @Bean
    public RequestInterceptor feignTracingInterceptor(Tracer tracer, Propagator propagator) {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 獲取當前 span
                Span currentSpan = tracer.currentSpan();
                if (currentSpan != null) {
                    // 使用 propagator 注入追蹤標頭
                    propagator.inject(tracer.currentTraceContext().context(), 
                                    template, 
                                    (template1, key, value) -> template1.header(key, value));
                }
            }
        };
    }
}
