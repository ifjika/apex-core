package com.apex.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

/**
 * Global Ingress Filter evaluating stateless Bearer JWT claims and enriching downstream requests.
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final SecretKey signingKey;
    private final List<String> whitelistedPrefixes = List.of(
        "/actuator",
        "/api/v1/auth",
        "/swagger-ui",
        "/v3/api-docs"
    );

    public JwtAuthFilter(@Value("${apex.security.jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Check if path is whitelisted from JWT authentication
        for (String prefix : whitelistedPrefixes) {
            if (path.startsWith(prefix)) {
                return chain.filter(exchange);
            }
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorizedResponse(exchange, "Missing or malformed Authorization header. Expected Bearer token.");
        }

        String token = authHeader.substring(7).trim();

        try {
            Claims claims = Jwts.parser()
                .verifyWith(this.signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            String userId = claims.getSubject();
            String roles = claims.get("roles", String.class);
            String costCenter = claims.get("cost_center_id", String.class);

            // Mutate request by propagating authenticated claims in secure internal headers
            ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Auth-User-Id", userId != null ? userId : "")
                .header("X-Auth-Roles", roles != null ? roles : "")
                .header("X-Auth-Cost-Center", costCenter != null ? costCenter : "")
                .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception ex) {
            return unauthorizedResponse(exchange, "Invalid or expired JWT token: " + ex.getMessage());
        }
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String detailMessage) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);

        String json = String.format("""
            {
              "type": "https://api.apex-core.internal/errors/unauthorized",
              "title": "Unauthorized Access",
              "status": 401,
              "detail": "%s",
              "instance": "%s",
              "code": "APEX_UNAUTHORIZED",
              "timestamp": "%s"
            }
            """,
            detailMessage,
            exchange.getRequest().getURI().getPath(),
            Instant.now()
        );

        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // High priority before routing
    }
}

