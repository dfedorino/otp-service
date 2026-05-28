package com.dfedorino.otp.controller.auth.annotation;

import com.dfedorino.otp.controller.auth.context.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler) {

        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }

        RequiresRole role = hm.getMethodAnnotation(RequiresRole.class);

        if (role == null) {
            return true;
        }

        log.debug(">> Checking role {} for path {}", role.value(), request.getRequestURI());

        if (!SecurityContext.hasRole(role.value())) {
            log.warn(">> Role rejected");
            response.setStatus(403);
            return false;
        }

        return true;
    }
}
