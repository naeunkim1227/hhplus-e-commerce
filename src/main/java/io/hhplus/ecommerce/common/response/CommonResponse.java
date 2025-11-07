package io.hhplus.ecommerce.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "공통 API 응답")
public class CommonResponse<T> {

    @Schema(description = "성공 여부", example = "true")
    private final boolean success;

    @Schema(description = "응답 코드", example = "OK")
    private final String code;

    @Schema(description = "응답 메시지", example = "요청이 성공했습니다")
    private final String message;

    @Schema(description = "HTTP 상태 코드", example = "200")
    private final int status;

    @Schema(description = "응답 데이터")
    private final T data;

    @Schema(description = "에러 상세 정보")
    private final CommonErrorResponse error;

    @Schema(description = "응답 시간", example = "2025-10-28T12:00:00")
    private final LocalDateTime timestamp;

    @Schema(description = "추적 ID", example = "d7f3a8c2")
    private final String traceId;

    private CommonResponse(boolean success, String code, String message, int status, T data, CommonErrorResponse error, String traceId) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.status = status;
        this.data = data;
        this.error = error;
        this.timestamp = LocalDateTime.now();
        this.traceId = traceId != null ? traceId : generateTraceId();
    }

    // 성공 응답 - 데이터 없음
    public static <T> CommonResponse<T> success() {
        return new CommonResponse<>(true, "OK", "요청이 성공했습니다", 200, null, null, null);
    }

    // 성공 응답 - 데이터 있음
    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(true, "OK", "요청이 성공했습니다", 200, data, null, null);
    }

    // 성공 응답 - 상태 코드 + 데이터
    public static <T> CommonResponse<T> success(int status, T data) {
        return new CommonResponse<>(true, "OK", "요청이 성공했습니다", status, data, null, null);
    }

    // 성공 응답 - 커스텀 메시지
    public static <T> CommonResponse<T> success(String message, T data) {
        return new CommonResponse<>(true, "OK", message, 200, data, null, null);
    }

    // 성공 응답 - 상태 코드 + 커스텀 메시지
    public static <T> CommonResponse<T> success(int status, String message, T data) {
        return new CommonResponse<>(true, "OK", message, status, data, null, null);
    }

    // 에러 응답
    public static <T> CommonResponse<T> error(int status, CommonErrorResponse error) {
        return new CommonResponse<>(false, error.getCode(), error.getMessage(), status, null, error, null);
    }

    // 에러 응답 - traceId 포함
    public static <T> CommonResponse<T> error(int status, CommonErrorResponse error, String traceId) {
        return new CommonResponse<>(false, error.getCode(), error.getMessage(), status, null, error, traceId);
    }

    private static String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}