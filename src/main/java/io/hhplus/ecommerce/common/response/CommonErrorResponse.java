package io.hhplus.ecommerce.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "에러 응답")
public class CommonErrorResponse {

    @Schema(description = "에러 코드", example = "PRODUCT_NOT_FOUND")
    private String code;

    @Schema(description = "에러 메시지", example = "상품을 찾을 수 없습니다")
    private String message;

    @Schema(description = "에러 상세 정보")
    private Map<String, Object> details;

    public static CommonErrorResponse of(String code, String message) {
        return CommonErrorResponse.builder()
                .code(code)
                .message(message)
                .build();
    }

    public static CommonErrorResponse of(String code, String message, Map<String, Object> details) {
        return CommonErrorResponse.builder()
                .code(code)
                .message(message)
                .details(details)
                .build();
    }
}