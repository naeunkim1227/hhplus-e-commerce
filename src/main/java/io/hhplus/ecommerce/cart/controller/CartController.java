package io.hhplus.ecommerce.cart.controller;

import io.hhplus.ecommerce.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "장바구니 API", description = "장바구니 관련 API")
@RestController
@RequestMapping("/cart")
public class CartController {

    @Operation(summary = "장바구니 조회", description = "현재 사용자의 장바구니 내역을 조회합니다.")
    @GetMapping
    public ApiResponse<Map<String, Object>> getCart(
            @Parameter(description = "사용자 ID", example = "1")
            @RequestParam Long userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "장바구니 조회 API - Mock");
        data.put("userId", userId);
        return ApiResponse.success(data);
    }

    @Operation(summary = "장바구니 담기", description = "장바구니에 상품을 추가합니다.")
    @PostMapping("/items")
    public ApiResponse<Map<String, Object>> addCartItem(
            @RequestBody Map<String, Object> request) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "장바구니 담기 API - Mock");
        data.put("request", request);
        return ApiResponse.success(HttpStatus.CREATED.value(), data);
    }

    @Operation(summary = "장바구니 상품 수량 변경", description = "장바구니 상품의 수량을 변경합니다.")
    @PatchMapping("/items/{itemId}")
    public ApiResponse<Map<String, Object>> updateCartItem(
            @Parameter(description = "장바구니 아이템 ID", example = "1")
            @PathVariable Long itemId,
            @RequestBody Map<String, Object> request) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "장바구니 상품 수량 변경 API - Mock");
        data.put("itemId", itemId);
        data.put("request", request);
        return ApiResponse.success(data);
    }

    @Operation(summary = "장바구니 상품 삭제", description = "장바구니에서 상품을 삭제합니다.")
    @DeleteMapping("/items/{itemId}")
    public ApiResponse<Map<String, Object>> deleteCartItem(
            @Parameter(description = "장바구니 아이템 ID", example = "1")
            @PathVariable Long itemId) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "장바구니 상품 삭제 API - Mock");
        data.put("itemId", itemId);
        return ApiResponse.success(HttpStatus.NO_CONTENT.value(), data);
    }
}