package com.stc.calender.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminConfig {

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;
    
    @Value("${keycloak.admin.username}")
    private String adminUser;
    
    @Value("${keycloak.admin.password}")
    private String adminPassword;
    
    @Value("${keycloak.master-realm}")
    private String masterRealm;
    
    @Value("${keycloak.master-client}")
    private String masterClient;

    @Bean
    Keycloak keycloakAdminClient() {
        return KeycloakBuilder.builder()
            .serverUrl(serverUrl)
            .realm(masterRealm)
            .username(adminUser)
            .password(adminPassword)
            .clientId(masterClient)
            .grantType("password")
            .build();
    }
}