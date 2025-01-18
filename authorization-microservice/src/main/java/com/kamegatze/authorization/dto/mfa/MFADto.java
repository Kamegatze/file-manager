package com.kamegatze.authorization.dto.mfa;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for qr code and code for 2fa")
public record MFADto(
        @Schema(description = "QR code", example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAA.....")
        String qrCode,
        @Schema(description = "Character code", example = "L6JXNF2J7QICON7N")
        String code
) {
}
