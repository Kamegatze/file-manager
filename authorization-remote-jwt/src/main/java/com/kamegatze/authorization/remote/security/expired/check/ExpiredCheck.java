package com.kamegatze.authorization.remote.security.expired.check;

/**
 * Check expired token in database
 */
public interface ExpiredCheck {
    /**
     * Check token
     *
     * @param token any token, that maybe is located in database
     * @return result different calculations and checks token
     */
    boolean check(String token);
}
