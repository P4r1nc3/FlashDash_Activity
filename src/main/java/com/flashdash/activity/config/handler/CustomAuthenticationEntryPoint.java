package com.flashdash.activity.config.handler;


import com.flashdash.activity.exception.ErrorCode;
import com.flashdash.activity.exception.ErrorDetailsProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ErrorDetailsProvider errorDetailsProvider;

    public CustomAuthenticationEntryPoint(ErrorDetailsProvider errorDetailsProvider) {
        this.errorDetailsProvider = errorDetailsProvider;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String errorResponseJson = errorDetailsProvider.createErrorResponseJson(ErrorCode.E401001);
        response.getWriter().write(errorResponseJson);
    }
}
