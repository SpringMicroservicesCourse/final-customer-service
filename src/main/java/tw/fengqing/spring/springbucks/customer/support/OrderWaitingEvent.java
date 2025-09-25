package tw.fengqing.spring.springbucks.customer.support;

import tw.fengqing.spring.springbucks.customer.model.CoffeeOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.ApplicationEvent;

@Data
@EqualsAndHashCode(callSuper = false)
public class OrderWaitingEvent extends ApplicationEvent {
    private CoffeeOrder order;

    public OrderWaitingEvent(CoffeeOrder order) {
        super(order);
        this.order = order;
    }
}
