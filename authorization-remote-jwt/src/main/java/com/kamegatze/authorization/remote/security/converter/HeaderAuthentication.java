package com.kamegatze.authorization.remote.security.converter;

public enum HeaderAuthentication {
    AUTHORIZATION("Authorization"),
    AUTHORIZATION_REFRESH("AuthorizationRefresh");

    HeaderAuthentication(String value) {
    }
}
