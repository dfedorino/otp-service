package com.dfedorino.otp;

import com.dfedorino.otp.controller.filter.JwtFilter;
import com.dfedorino.otp.repository.config.RepositoryConfig;
import com.dfedorino.otp.service.JwtService;
import com.dfedorino.otp.service.config.ServiceConfig;
import com.dfedorino.otp.controller.config.WebConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@Slf4j
@RequiredArgsConstructor
public class Main implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) {
        AnnotationConfigWebApplicationContext context =
            new AnnotationConfigWebApplicationContext();

        context.register(RepositoryConfig.class);
        context.register(ServiceConfig.class);
        context.register(WebConfig.class);

        DispatcherServlet dispatcherServlet =
            new DispatcherServlet(context);

        ServletRegistration.Dynamic servlet =
            servletContext.addServlet(
                "dispatcher",
                dispatcherServlet
            );

        servlet.setLoadOnStartup(1);
        servlet.addMapping("/");

        FilterRegistration.Dynamic jwtFilter =
            servletContext.addFilter("jwtFilter",
                new JwtFilter(
                    context.getBean(JwtService.class),
                    context.getBean(ObjectMapper.class)
                ));

        jwtFilter.addMappingForUrlPatterns(null, false, "/api/*");
    }
}
