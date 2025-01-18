package com.kamegatze.authorization.services;

public interface MFATokenService {
    String generateSecretKey();

    String getQRCode(final String secret);

    boolean verifyTotp(final String code, final String secret);
}
