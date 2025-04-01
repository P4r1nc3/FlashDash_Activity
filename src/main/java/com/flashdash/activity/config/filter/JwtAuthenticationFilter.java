package com.flashdash.activity.config.filter;

import com.flashdash.activity.config.JwtManager;
import com.flashdash.activity.util.UserContext;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtManager jwtManager;
    private final UserContext userContext;

    public JwtAuthenticationFilter(JwtManager jwtManager, UserContext userContext) {
        this.jwtManager = jwtManager;
        this.userContext = userContext;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authorizationHeader.substring(7);
        try {
            final String userFrn = jwtManager.extractUserFrn(token);
            final String userEmail = jwtManager.extractEmail(token);

            if (userFrn != null && userEmail != null) {
                if (jwtManager.validateToken(token, userFrn)) {
                    userContext.setUserFrn(userFrn);
                    userContext.setUserEmail(userEmail);
                    logger.info("Authenticated user: {}, Email: {}", userFrn, userEmail);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userFrn, null, List.of(new SimpleGrantedAuthority("USER")));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    logger.warn("Invalid JWT token for user: {}", userFrn);
                }
            }
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token has expired: {}", e.getMessage());
        } catch (SignatureException e) {
            logger.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error validating JWT token", e);
        }

        filterChain.doFilter(request, response);
    }
}
