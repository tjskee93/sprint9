package ru.yandex.practicum.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        .pathMatchers("/actuator/**").authenticated()
                        .pathMatchers(HttpMethod.GET, "/api/accounts/me", "/api/accounts/others").hasAuthority("SCOPE_accounts.read")
                        .pathMatchers(HttpMethod.PUT, "/api/accounts/me").hasAuthority("SCOPE_accounts.write")
                        .pathMatchers(HttpMethod.POST,"/api/internal/**").hasAuthority("SCOPE_accounts.write")
                        .pathMatchers("/api/transfers").hasAuthority("SCOPE_transfer.write")
                        .pathMatchers("/api/cash").hasAuthority("SCOPE_cash.write")
                        .anyExchange().denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter()))))
                .build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthorities = new JwtGrantedAuthoritiesConverter();
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setPrincipalClaimName("preferred_username");
        converter.setJwtGrantedAuthoritiesConverter(jwt -> mergeAuthorities(grantedAuthorities.convert(jwt), getRealmRoles(jwt)));
        return converter;
    }

    private static Collection<GrantedAuthority> mergeAuthorities(Collection<GrantedAuthority> grantedAuthorities, Collection<GrantedAuthority> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (grantedAuthorities != null) {
            authorities.addAll(grantedAuthorities);
        }
        authorities.addAll(roles);
        return authorities;
    }


    private static Collection<GrantedAuthority> getRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || !(realmAccess.get("roles") instanceof Collection<?> roles)) {
            return List.of();
        }

        return roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(role -> new SimpleGrantedAuthority("SCOPE_" + role))
                .map(GrantedAuthority.class::cast)
                .toList();
    }
}
