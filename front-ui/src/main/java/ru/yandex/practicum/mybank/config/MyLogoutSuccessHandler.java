package ru.yandex.practicum.mybank.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class MyLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final String FRONT_CLIENT_ID = "front-ui";

    @Value("${keycloak.logout.url:http://localhost:9090/realms/my-bank/protocol/openid-connect/logout}")
    private String myLogoutUri;

    @Override
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        String postLogoutRedirectUri = ServletUriComponentsBuilder.fromRequest(request)
                .replacePath(request.getContextPath().isBlank() ? "/" : request.getContextPath())
                .replaceQuery(null)
                .fragment(null)
                .build()
                .toUriString();

        UriComponentsBuilder logoutUri = UriComponentsBuilder.fromUriString(myLogoutUri)
                .queryParam("post_logout_redirect_uri", postLogoutRedirectUri);

        if (authentication != null && authentication.getPrincipal() instanceof OidcUser oidcUser) {
            logoutUri.queryParam("id_token_hint", oidcUser.getIdToken().getTokenValue());
        } else {
            logoutUri.queryParam("client_id", FRONT_CLIENT_ID);
        }

        response.sendRedirect(logoutUri.build().encode().toUriString());
    }
}
