package com.kamegatze.authorization.configuration.security.provider;

import com.kamegatze.authorization.configuration.security.details.UsersDetails;
import com.kamegatze.authorization.exception.Invalid2FaAuthenticationException;
import com.kamegatze.authorization.model.Users;
import com.kamegatze.authorization.services.MFATokenService;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

public class DaoAuthentication2FAProvider extends DaoAuthenticationProvider {

    private final MFATokenService mfaTokenService;
    private UserDetailsService userDetailsService;

    public DaoAuthentication2FAProvider(MFATokenService mfaTokenService) {
        super();
        this.mfaTokenService = mfaTokenService;
    }

    public DaoAuthentication2FAProvider(PasswordEncoder passwordEncoder, MFATokenService mfaTokenService) {
        super(passwordEncoder);
        this.mfaTokenService = mfaTokenService;
    }

    @Override
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
        super.setUserDetailsService(userDetailsService);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsersDetails userDetails = (UsersDetails) userDetailsService
                .loadUserByUsername((String) authentication.getPrincipal());
        Users users = userDetails.getUsers();
        if (!users.isEnable2fa()) {
            return super.authenticate(authentication);
        }
        return authentication2FA(authentication, users, userDetails);
    }

    private Authentication authentication2FA(Authentication authentication, Users users, UserDetails userDetails) {
        String authenticationCode = (String) authentication.getCredentials();
        if (!mfaTokenService.verifyTotp(authenticationCode, users.getSecret())) {
            throw new Invalid2FaAuthenticationException("Error validate code authentication two factoring authorization");
        }
        return super.createSuccessAuthentication(userDetails, authentication, userDetails);
    }
}
