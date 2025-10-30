package io.hhplus.ecommerce.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "공통 API 응답")
public class ApiResponse<T> {

    @Schema(description = "상태 코드", example = "200")
    private final int status;

    @Schema(description = "응답 데이터")
    private final T data;

    @Schema(description = "에러 상세 정보")
    private final ErrorResponse error;

    @Schema(description = "응답 시간", example = "2025-10-28T12:00:00")
    private final LocalDateTime timestamp;

    private ApiResponse(int status, T data, ErrorResponse error) {
        this.status = status;
        this.data = data;
        this.error = error;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, data, null);
    }

    public static <T> ApiResponse<T> success(int status, T data) {
        return new ApiResponse<>(status, data, null);
    }

    public static <T> ApiResponse<T> error(int status, ErrorResponse error) {
        return new ApiResponse<>(status, null, error);
    }
}