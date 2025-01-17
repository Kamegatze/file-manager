package com.kamegatze.general.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response delete entity")
public class ResponseDtoByDelete {

    @Schema(description = "Description status operation after execution", example = "File was delete")
    private String message;

    @Schema(description = "Status response request", example = "ACCEPTED")
    private HttpStatus status;

    @Schema(description = "id delete entity", example = "8910b499-34f7-44de-9e19-c05d64368641")
    private UUID deleteId;
}
