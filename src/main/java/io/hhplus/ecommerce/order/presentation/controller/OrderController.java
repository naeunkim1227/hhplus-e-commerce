package io.hhplus.ecommerce.order.presentation.controller;

import io.hhplus.ecommerce.common.response.CommonResponse;
import io.hhplus.ecommerce.order.application.dto.command.OrderCreateDirectCommand;
import io.hhplus.ecommerce.order.application.dto.command.OrderCreateFromCartCommand;
import io.hhplus.ecommerce.order.application.dto.result.OrderDto;
import io.hhplus.ecommerce.order.application.usecase.OrderCreateDirectUseCase;
import io.hhplus.ecommerce.order.application.usecase.OrderCreateFromCartUseCase;
import io.hhplus.ecommerce.order.presentation.dto.request.OrderCreateDirectRequest;
import io.hhplus.ecommerce.order.presentation.dto.request.OrderCreateFromCartRequest;
import io.hhplus.ecommerce.order.presentation.dto.response.OrderResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "주문 API", description = "주문 관련 API")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderCreateFromCartUseCase orderCreateFromCartUseCase;
    private final OrderCreateDirectUseCase orderCreateDirectUseCase;


    /**
     * 장바구니에서 선택 구매
     */
    @PostMapping("/carts")
    public CommonResponse<OrderResponse> createOrderCart(
            @Valid  @RequestBody OrderCreateFromCartRequest request) {
        OrderDto orderDto = orderCreateFromCartUseCase.excute(request.toCommand());
        OrderResponse response = OrderResponse.from(orderDto);
        return CommonResponse.success(response);
    }

    /**
     * 구매창에서 바로 단건 구매
     */
    @PostMapping
    public CommonResponse<OrderResponse> createOrderDirect(
            @Valid  @RequestBody OrderCreateDirectRequest request) {
        OrderDto orderDto = orderCreateDirectUseCase.excute(request.toCommand());
        OrderResponse response = OrderResponse.from(orderDto);
        return CommonResponse.success(response);
    }

}