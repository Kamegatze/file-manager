package com.kamegatze.authorization.remote.security.converter;

public enum HeaderAuthentication {
    AUTHORIZATION("Authorization"),
    AUTHORIZATION_REFRESH("AuthorizationRefresh");

    private final String value;

    HeaderAuthentication(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
