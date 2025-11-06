package io.hhplus.ecommerce.cart.presentation.controller;

import io.hhplus.ecommerce.cart.application.dto.result.CartItemDto;
import io.hhplus.ecommerce.cart.application.dto.command.CartItemAddCommand;
import io.hhplus.ecommerce.cart.application.dto.command.CartItemDeleteCommand;
import io.hhplus.ecommerce.cart.application.dto.command.CartItemUpdateCommand;
import io.hhplus.ecommerce.cart.application.usecase.*;
import io.hhplus.ecommerce.cart.presentation.dto.request.CartItemAddRequest;
import io.hhplus.ecommerce.cart.presentation.dto.request.CartItemDeleteRequest;
import io.hhplus.ecommerce.cart.presentation.dto.request.CartItemUpdateRequest;
import io.hhplus.ecommerce.cart.presentation.dto.response.CartItemResponse;
import io.hhplus.ecommerce.cart.presentation.dto.response.CartResponse;
import io.hhplus.ecommerce.common.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "장바구니 API", description = "장바구니 관련 API")
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartAddUseCase cartAddUseCase;
    private final CartGetUseCase cartGetUseCase;
    private final CartUpdateUseCase cartUpdateUseCase;
    private final CartDeleteUseCase cartDeleteUseCase;
    private final CartClearUseCase cartClearUseCase;

    @Operation(summary = "장바구니 조회", description = "현재 사용자의 장바구니 내역을 조회합니다.")
    @GetMapping
    public CommonResponse<CartResponse> getCart(
            @Parameter(description = "사용자 ID", example = "1")
            @RequestParam Long userId) {
        List<CartItemDto> cartItemDtos = cartGetUseCase.execute(userId);
        CartResponse response = CartResponse.from(userId,cartItemDtos);
        return CommonResponse.success(response);
    }

    @Operation(summary = "장바구니 담기", description = "장바구니에 상품을 추가합니다.")
    @PostMapping("/items")
    public CommonResponse<CartItemResponse> addCartItem(@Valid @RequestBody CartItemAddRequest request) {
        CartItemAddCommand command = request.toCommand();
        CartItemDto cartItemDto = cartAddUseCase.execute(command);
        CartItemResponse response = CartItemResponse.from(cartItemDto);

        return CommonResponse.success(response);
    }

    @Operation(summary = "장바구니 상품 수량 변경", description = "장바구니 상품의 수량을 변경합니다.")
    @PatchMapping("/items/{itemId}")
    public CommonResponse<CartItemResponse> updateCartItem(
            @Valid @RequestBody CartItemUpdateRequest request) {
        CartItemUpdateCommand command = request.toCommand();
        CartItemDto cartItemDto = cartUpdateUseCase.excute(command);
        CartItemResponse response = CartItemResponse.from(cartItemDto);
        return CommonResponse.success(response);
    }

    @Operation(summary = "장바구니 상품 삭제", description = "장바구니에서 상품을 삭제합니다.")
    @DeleteMapping("/items/{itemId}")
    public CommonResponse<Map<String, Object>> deleteCartItem(CartItemDeleteRequest request) {
        CartItemDeleteCommand command = request.toCommand();
        cartDeleteUseCase.excute(command);
        return CommonResponse.success();
    }

    @Operation(summary = "장바구니 상품 전체 삭제", description = "장바구니에서 상품을 삭제합니다.")
    @DeleteMapping("/items")
    public CommonResponse<Map<String, Object>> clearCartItem(CartItemDeleteRequest request) {
        CartItemDeleteCommand command = request.toCommand();
        cartClearUseCase.excute(command);
        return CommonResponse.success();
    }


}