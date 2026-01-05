package io.github.alikhanzhomartov.gateway.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {
    private String requestId;
    private String path;
    private Integer status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
}
