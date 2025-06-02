package com.fpt.metroll.gateway.filter;

import com.fpt.metroll.gateway.util.FirebaseFakeProfileRegistry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final FirebaseFakeProfileRegistry firebaseFakeProfileRegistry;

    public AuthGlobalFilter(FirebaseFakeProfileRegistry firebaseFakeProfileRegistry) {
        this.firebaseFakeProfileRegistry = firebaseFakeProfileRegistry;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        List<String> authHeaders = exchange.getRequest().getHeaders().get("Authorization");

        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.getFirst();

            if (authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    FirebaseToken firebaseToken = firebaseFakeProfileRegistry.verifyIdToken(token);

                    if (firebaseToken == null) {
                        firebaseToken = FirebaseAuth.getInstance().verifyIdToken(token);
                    }

                    String uid = firebaseToken.getUid();
                    String email = firebaseToken.getEmail();
                    String role = (String) firebaseToken.getClaims().get("role");

                    ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", uid)
                            .header("X-User-Role", role)
                            .header("X-User-Email", email)
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                } catch (FirebaseAuthException e) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
            } else {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
