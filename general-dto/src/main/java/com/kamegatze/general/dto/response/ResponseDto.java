package com.kamegatze.general.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response entity")
public class ResponseDto {

    @Schema(description = "Description status operation after execution", example = "User was created")
    private String message;

    @Schema(description = "Status response request", example = "CREATED")
    private HttpStatus status;
}
