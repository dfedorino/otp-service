package com.dfedorino.otp;

import com.dfedorino.otp.service.config.ServiceConfig;
import com.dfedorino.otp.controller.config.WebConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@Slf4j
public class Main implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) {
        log.info(">> onStartup 2");

        AnnotationConfigWebApplicationContext context =
            new AnnotationConfigWebApplicationContext();

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
    }
}
