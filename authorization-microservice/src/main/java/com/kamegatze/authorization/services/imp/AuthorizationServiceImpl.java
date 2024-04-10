package com.kamegatze.authorization.services.imp;

import com.kamegatze.authorization.configuration.security.details.UsersDetails;
import com.kamegatze.authorization.configuration.security.details.UsersDetailsService;
import com.kamegatze.authorization.dto.*;
import com.kamegatze.authorization.exception.NotEqualsPasswordException;
import com.kamegatze.authorization.exception.RefreshTokenIsNullException;
import com.kamegatze.authorization.exception.UserNotExistException;
import com.kamegatze.authorization.exception.UsersExistException;
import com.kamegatze.authorization.model.Authority;
import com.kamegatze.authorization.model.EAuthority;
import com.kamegatze.authorization.model.Users;
import com.kamegatze.authorization.model.UsersAuthority;
import com.kamegatze.authorization.repoitory.AuthorityRepository;
import com.kamegatze.authorization.repoitory.UsersAuthorityRepository;
import com.kamegatze.authorization.repoitory.UsersRepository;
import com.kamegatze.authorization.services.AuthorizationService;
import com.kamegatze.authorization.services.EmailService;
import com.kamegatze.authorization.services.JwtService;
import com.kamegatze.authorization.transfer.client.ClientTransfer;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;


import java.text.ParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import com.kamegatze.authorization.exception.EqualsPasswordException;

@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    private final UsersRepository usersRepository;
    private final AuthorityRepository authorityRepository;
    private final UsersAuthorityRepository usersAuthorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsersDetailsService usersDetailsService;
    private final JwtIssuerValidator jwtValidator;
    private final SpringTemplateEngine templateEngine;
    private final EmailService emailService;
    private final ClientTransfer<Object> clientTransfer;

    private final String EMAIL_PATTERN = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

    @Value("${url.change-password}")
    private String urlChangePassword;
    @Value("${spring.kafka.topics.save.users}")
    private String topicSaveUsers;
    @Override
    public UsersDto signup(UsersDto usersDto) throws UsersExistException {
        Users users = Users.builder()
                .password(usersDto.getPassword())
                .email(usersDto.getEmail())
                .login(usersDto.getLogin())
                .name(usersDto.getFirstName() + " " + usersDto.getLastName())
                .build();

        Optional<Users> usersFindByLogin = usersRepository.findByLogin(users.getLogin());
        if (usersFindByLogin.isPresent()) {
            throw new UsersExistException(String.format("User with login: \"%s\" exist", users.getLogin()));
        }

        Optional<Users> usersFindByEmail = usersRepository.findByEmail(users.getEmail());
        if (usersFindByEmail.isPresent()) {
            throw new UsersExistException(String.format("User with email: \"%s\" exist", users.getEmail()));
        }

        Authority authorityRead = authorityRepository.findByName(EAuthority.AUTHORITY_READ)
                .orElseThrow(() -> new NoSuchElementException("There are no such rights"));

        users.setPassword(passwordEncoder.encode(users.getPassword()));
        users = usersRepository.save(users);
        usersAuthorityRepository.save(UsersAuthority.builder()
                .authorityId(authorityRead.getId())
                .usersId(users.getId())
                .build());

        String[] name = users.getName().split(" ");
        UsersDto usersDtoResult = UsersDto.builder()
                .id(users.getId())
                .login(users.getLogin())
                .email(users.getEmail())
                .firstName(name[0])
                .lastName(name[1])
                .build();
        clientTransfer.sendData(usersDtoResult, topicSaveUsers);
        return usersDtoResult;
    }

    @Override
    public JwtDto signin(Login login) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        login.getLogin(),
                        login.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String tokenAccess = jwtService.generateAccess((UsersDetails) authentication.getPrincipal());
        String tokenRefresh = jwtService.generateRefresh((UsersDetails) authentication.getPrincipal());

        return JwtDto.builder()
                .tokenAccess(tokenAccess)
                .refreshToken(tokenRefresh)
                .type(ETokenType.Bearer)
                .build();
    }

    @Override
    public Boolean isAuthenticationUser(HttpServletRequest request) throws ParseException {
        Optional<String> tokenAccessOptional = Optional.ofNullable(
                request.getHeader(
                        ETypeTokenHeader.Authorization.name()
                )
        );
        Optional<String> refreshTokenOptional = Optional.ofNullable(
                request.getHeader(
                        ETypeTokenHeader.AuthorizationRefresh.name()
                )
        );
        if (tokenAccessOptional.isEmpty() && refreshTokenOptional.isEmpty()) {
            return Boolean.FALSE;
        }

        String tokenRefresh = refreshTokenOptional.get();
        return tokenValid(tokenRefresh);
    }

    private Boolean tokenValid(String token) throws ParseException {
        try {
            OAuth2TokenValidatorResult result = jwtValidator.validate(
                    new Jwt(token,
                            jwtService.getIssuedAt(token),
                            jwtService.getExpiresAt(token),
                            jwtService.getHeaders(token),
                            jwtService.getClaims(token))
            );
            if (result.hasErrors()) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        } catch (JwtException exception) {
            return Boolean.FALSE;
        }
    }

    @Override
    public JwtDto authenticationViaRefreshToken(HttpServletRequest request)
            throws RefreshTokenIsNullException, ParseException, InvalidBearerTokenException, UserNotExistException {
        Optional<String> tokenRefreshOptional = Optional.ofNullable(
                request.getHeader(
                        ETypeTokenHeader.AuthorizationRefresh.name()
                )
        );

        if (tokenRefreshOptional.isEmpty()) {
            throw new RefreshTokenIsNullException("Refresh token is null");
        }

        String token = tokenRefreshOptional.get();

        if (!tokenValid(token)) {
            throw new InvalidBearerTokenException("Refresh token invalid");
        }
        String login = jwtService.getLogin(token);
        if (!usersRepository.existsByLogin(login)) {
            throw new UserNotExistException(String.format("User with login: [%s] not exist", login));
        }
        UserDetails userDetails = usersDetailsService.loadUserByUsername(login);
        String accessToken = jwtService.generateAccess(userDetails);
        String refreshToken = jwtService.generateRefresh(userDetails);
        return JwtDto.builder()
                .refreshToken(refreshToken)
                .tokenAccess(accessToken)
                .type(ETokenType.Bearer)
                .build();
    }

    @Override
    public Boolean isExistUser(String loginOrEmail) {
        boolean isEmail = Pattern.compile(EMAIL_PATTERN).matcher(loginOrEmail).matches();
        if (isEmail) {
            return usersRepository.existsByEmail(loginOrEmail);
        }
        return usersRepository.existsByLogin(loginOrEmail);
    }

    @Override
    public void sendCode(String loginOrEmail) throws MessagingException {
        Context context = new Context();
        boolean isEmail = Pattern.compile(EMAIL_PATTERN).matcher(loginOrEmail).matches();
        Users user;
        if (isEmail) {
            user = usersRepository.findByEmail(loginOrEmail)
                    .orElseThrow(() -> new UserNotExistException(String.format("Not found user by email: \"%s\"", loginOrEmail)));
        } else {
            user = usersRepository.findByLogin(loginOrEmail)
                    .orElseThrow(() -> new UserNotExistException(String.format("Not found user by login: \"%s\"", loginOrEmail)));
        }
        String tokenUUID = UUID.randomUUID().toString();
        String link = String.format("%s?token=%s", urlChangePassword, tokenUUID);
        user.setRecoveryCode(tokenUUID);
        usersRepository.save(user);
        context.setVariable("link", link);
        String htmlBody = templateEngine.process("email-recovery-code-ru.html", context);
        emailService.sendHtmlMessage(user.getEmail(), "File-Manager. Смена пароля", htmlBody);
        asyncRemoveRecoveryCode(5, user);
    }

    @Override
    public void changePassword(ChangePasswordDto changePasswordDto) throws NotEqualsPasswordException, EqualsPasswordException {
        Users user = usersRepository.findByRecoveryCode(changePasswordDto.getRecoveryCode())
                .orElseThrow(() -> new NoSuchElementException(String.format("User not found by recovery code: \"%s\"", changePasswordDto.getRecoveryCode())));
        if (!changePasswordDto.getPassword().equals(changePasswordDto.getPasswordRetry())) {
            throw new NotEqualsPasswordException("Field password and passwordRetry not equals");
        }
        String password = passwordEncoder.encode(changePasswordDto.getPassword());
        if(user.getPassword().equals(password)) {
            throw new EqualsPasswordException("Input other password. Current password equals previous password");
        }
        user.setRecoveryCode("");
        user.setPassword(password);
        usersRepository.save(user);
    }

    @Async
    protected void asyncRemoveRecoveryCode(Integer minute, Users users) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(() -> {
            removeRecoveryCode(users);
        }, minute, TimeUnit.MINUTES);
    }

    private void removeRecoveryCode(Users users) {
        users.setRecoveryCode("");
        usersRepository.save(users);
    }

    @Override
    public List<AuthorityDto> getAuthorityByRequest(HttpServletRequest request) throws ParseException {
        Optional<String> headerOptional = Optional.ofNullable(
                request.getHeader(ETypeTokenHeader.Authorization.name())
        );
        if (headerOptional.isEmpty()) {
            return List.of();
        }
        String header = headerOptional.get().substring(7);
        JWTClaimsSet claimsSet = JWTParser.parse(header).getJWTClaimsSet();
        List<Authority> authorities = usersRepository.findByLogin(claimsSet.getSubject()).orElseThrow(
                () -> new UserNotExistException(String.format("User with login: [%s] not exist", claimsSet.getSubject()))
        ).getAuthorities();
        return authorities.stream().map(
                authority -> new AuthorityDto(authority.getName().name())
        ).toList();
    }
}
