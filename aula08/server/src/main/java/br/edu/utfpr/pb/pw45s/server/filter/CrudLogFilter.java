package br.edu.utfpr.pb.pw45s.server.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Adiciona informações da requisição no MDC (Mapped Diagnostic Context), que são gravadas como campos em cada linha
 * de log em JSON e podem ser filtradas no Kibana.
 * O filtro executa antes da cadeia de filtros do Spring Security (HIGHEST_PRECEDENCE), assim as requisições de login,
 * que são tratadas pelo JWTAuthenticationFilter, também recebem as informações.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CrudLogFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    private static final String REQUEST_ID = "requestId";
    private static final String CRUD_OPERATION = "crudOperation";
    private static final String HTTP_METHOD = "httpMethod";
    private static final String ENDPOINT = "endpoint";
    private static final String STATUS = "status";
    private static final String DURATION_MS = "durationMs";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();

        String httpMethod = request.getMethod();
        String endpoint = request.getRequestURI();

        // ID de correlação: reaproveita o header enviado pelo cliente ou gera um novo
        // e devolve o valor na resposta para que o cliente consiga localizar a requisição nos logs
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (!StringUtils.hasText(requestId)) {
            requestId = UUID.randomUUID().toString();
        }
        response.setHeader(REQUEST_ID_HEADER, requestId);

        MDC.put(REQUEST_ID, requestId);
        MDC.put(CRUD_OPERATION, isLogin(endpoint) ? "LOGIN" : mapToCrudOperation(httpMethod));
        MDC.put(HTTP_METHOD, httpMethod);
        MDC.put(ENDPOINT, endpoint);

        try {
            chain.doFilter(request, response);
        } finally {
            // log de acesso: registra o resultado da requisição (status e tempo de resposta)
            MDC.put(STATUS, String.valueOf(response.getStatus()));
            MDC.put(DURATION_MS, String.valueOf(System.currentTimeMillis() - start));
            log.info("{} {} -> {}", httpMethod, endpoint, response.getStatus());

            // remove apenas as chaves adicionadas por este filtro
            MDC.remove(REQUEST_ID);
            MDC.remove(CRUD_OPERATION);
            MDC.remove(HTTP_METHOD);
            MDC.remove(ENDPOINT);
            MDC.remove(STATUS);
            MDC.remove(DURATION_MS);
        }
    }

    // As requisições do Spring Boot Admin para o Actuator (a cada poucos segundos) não são registradas
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator");
    }

    private boolean isLogin(String endpoint) {
        return endpoint.equals("/login") || endpoint.startsWith("/auth-social");
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
