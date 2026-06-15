package com.example.swaggerjwtapi.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
public class HttpsEnforcementConfig {

    @Bean
    public FilterRegistrationBean<HttpsEnforcementFilter> httpsEnforcementFilter() {
        FilterRegistrationBean<HttpsEnforcementFilter> bean =
                new FilterRegistrationBean<>(new HttpsEnforcementFilter());
        bean.setOrder(0);
        return bean;
    }

    public static class HttpsEnforcementFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain
        ) throws ServletException, IOException {
            boolean isSecure = request.isSecure() ||
                    "https".equals(request.getHeader("X-Forwarded-Proto"));

            if (!isSecure && isProduction()) {
                String redirectUrl = "https://" + request.getServerName() +
                        request.getRequestURI();
                if (request.getQueryString() != null) {
                    redirectUrl += "?" + request.getQueryString();
                }
                response.sendRedirect(redirectUrl);
                return;
            }

            response.setHeader("Strict-Transport-Security",
                    "max-age=31536000; includeSubDomains");
        }

        private boolean isProduction() {
            return !"dev".equals(System.getProperty("environment"));
        }
    }
}
