package com.kamegatze.authorization.configuration.security.http.entry.point;

import com.kamegatze.authorization.remote.security.http.entry.point.ExceptionEntryPoint;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@RequiredArgsConstructor
public class ExceptionEntryPointContainer {
    private final HandlerExceptionResolver handlerExceptionResolver;
    @Getter
    private ExceptionEntryPoint exceptionEntryPoint;

    @PostConstruct
    protected void init() {
        exceptionEntryPoint = new ExceptionEntryPoint(handlerExceptionResolver);
    }
}
