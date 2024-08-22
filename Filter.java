package com.ihsw.authentication.api.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
public class ControllerLogFilter implements Filter {

    private ServletContext context;

    public void init(FilterConfig fConfig) throws ServletException {
        this.context = fConfig.getServletContext();
        this.context.log("AuthenticationFilter initialized");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;

            var connectionId = UUID.randomUUID().toString();

            httpServletResponse.addHeader("app-connection-id", connectionId);

            var queryString = httpServletRequest.getQueryString();

            log.info("------------------------------------------------------------------------------------------------------------");
            log.info("CONNECTION ID -> " + connectionId);
            log.info("CLIENT IP     -> " + httpServletRequest.getLocalAddr());
            log.info("REST PATH     -> " + httpServletRequest.getMethod() + "  " +  httpServletRequest.getRequestURI() + (queryString != null ? "?" + queryString : ""));

            ContentCachingRequestWrapper contentCachingRequestWrapper = new ContentCachingRequestWrapper(httpServletRequest);
            ContentCachingResponseWrapper contentCachingResponseWrapper = new ContentCachingResponseWrapper(httpServletResponse);

            chain.doFilter(contentCachingRequestWrapper, contentCachingResponseWrapper);

            byte[] requestBody = contentCachingRequestWrapper.getContentAsByteArray();
            byte[] responseBody = contentCachingResponseWrapper.getContentAsByteArray();

            if(httpServletRequest.getMethod().equals("POST") || httpServletRequest.getMethod().equals("PUT")) {
                log.info("REQUEST BODY  -> " + new String(requestBody, StandardCharsets.UTF_8)
                        .replace("\n", "")
                        .replace("\t", "")
                        .replace("    ", ""));
            }

            log.info("RESPONSE BODY -> " + new String(responseBody, StandardCharsets.UTF_8));

            contentCachingResponseWrapper.copyBodyToResponse();

            log.info("------------------------------------------------------------------------------------------------------------");
        }
        catch (Exception e) {
            chain.doFilter(request, response);
        }
    }

    public void destroy() {
        //close any resources here
    }
}
