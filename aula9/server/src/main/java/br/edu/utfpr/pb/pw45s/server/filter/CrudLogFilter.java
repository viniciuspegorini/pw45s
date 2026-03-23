package br.edu.utfpr.pb.pw45s.server.filter;

import org.slf4j.MDC;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CrudLogFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        String httpMethod = req.getMethod();
        String endpoint = req.getRequestURI();


        if (endpoint.contains("login")) {
            MDC.put("crudOperation", "login");
        } else {
            String crudOperation = mapToCrudOperation(httpMethod);
            MDC.put("crudOperation", crudOperation);
        }

        MDC.put("httpMethod", httpMethod);
        MDC.put("endpoint", endpoint);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String mapToCrudOperation(String httpMethod) {
        return switch (httpMethod) {
            case "POST" -> "CREATE";
            case "GET" -> "READ";
            case "PUT", "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> "OTHER";
        };
    }
}
