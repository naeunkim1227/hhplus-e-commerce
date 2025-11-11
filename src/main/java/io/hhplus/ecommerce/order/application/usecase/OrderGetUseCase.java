package io.hhplus.ecommerce.order.application.usecase;

import io.hhplus.ecommerce.order.application.dto.result.OrderDto;
import io.hhplus.ecommerce.order.domain.entity.Order;
import io.hhplus.ecommerce.order.domain.service.OrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class OrderGetUseCase {

    private final OrderService orderService;

    public OrderDto excute(Long orderId) {
        Order order = orderService.getOrder(orderId);
        return OrderDto.from(order);
    }

}
