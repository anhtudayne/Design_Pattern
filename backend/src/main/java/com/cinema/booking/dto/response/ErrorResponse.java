package com.cinema.booking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response DTO for all API endpoints.
 * Ensures consistent error format across the application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response structure")
public class ErrorResponse {
    
    @Schema(description = "HTTP status code", example = "400")
    private int status;
    
    @Schema(description = "Error type/category", example = "Bad Request")
    private String error;
    
    @Schema(description = "Human-readable error message", example = "Invalid input provided")
    private String message;
    
    @Schema(description = "Timestamp when error occurred", example = "2024-01-15T10:30:00")
    private LocalDateTime timestamp;
    
    @Schema(description = "Request path that caused the error", example = "/api/payment/checkout")
    private String path;
    
    @Schema(description = "Additional error details (validation errors, field errors, etc.)")
    private Map<String, String> details;
    
    public ErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}
