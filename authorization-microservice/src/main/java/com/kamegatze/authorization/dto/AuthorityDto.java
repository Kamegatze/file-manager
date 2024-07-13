package com.kamegatze.authorization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Contain authority user system")
public class AuthorityDto {
    @Schema(description = "authority user", example = "AUTHORITY_READ")
    String authority;
}
