package com.dfedorino.otp.controller.auth.filter;

import com.dfedorino.otp.controller.auth.context.SecurityContext;
import com.dfedorino.otp.controller.dto.ErrorResponse;
import com.dfedorino.otp.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter implements Filter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest request,
        ServletResponse response,
        FilterChain chain)
        throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String path = httpRequest.getRequestURI();

        log.debug(">> Validating jwt token for path: {}", path);

        // skip auth endpoints
        if (path.startsWith("/api/auth")) {
            log.debug(">> Skipping auth request");
            chain.doFilter(request, response);
            return;
        }

        try {
            String header = httpRequest.getHeader("Authorization");

            String token = header == null ? null : header.substring(7);

            if (header == null || !header.startsWith("Bearer ") || !jwtService.isTokenValid(token)) {
                log.warn(">> Token is missing or invalid");

                var httpResponse = (HttpServletResponse) response;

                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json");

                httpResponse.getWriter().write(objectMapper.writeValueAsString(
                    new ErrorResponse("Missing or invalid Authorization header")));

                return;
            }

            SecurityContext.set(
                jwtService.extractUsername(token),
                jwtService.extractRole(token)
            );

            chain.doFilter(request, response);
        } catch (Exception e) {
            log.error(">> ", e);
        } finally {
            SecurityContext.clear();
        }
    }
}
