package com.example.employeeservice.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class KeycloakAdminConfig {
    @Bean
    Keycloak keycloak(@Value("${keycloak.admin.server-url}") String url,
                      @Value("${keycloak.admin.realm}") String realm,
                      @Value("${keycloak.admin.client-id}") String clientId,
                      @Value("${keycloak.admin.client-secret}") String secret) {
        return KeycloakBuilder.builder()
                .serverUrl(url).realm(realm)
                .clientId(clientId).clientSecret(secret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }
}