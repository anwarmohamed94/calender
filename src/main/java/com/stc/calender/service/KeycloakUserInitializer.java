package com.stc.calender.service;

import java.util.List;
import java.util.Objects;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KeycloakUserInitializer {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakUserInitializer.class);
    
	private final Keycloak keycloak;

	private final UserService userService;

	@Value("${keycloak.realm:booking-realm}")
	private String realm;

	@Scheduled(fixedRateString = "${sync-users.job-rate}")
	public void initKeycloakUsers() {
		List<UserRepresentation> keycloakUsers = getAllKeycloakUsers();

		if (Objects.nonNull(keycloakUsers))
			keycloakUsers.forEach(kcUser -> {
				userService.createLocalUser(kcUser.getUsername(), kcUser.getEmail(), kcUser.getId());
			});
		
		logger.info("fetch DONE users from keycloak seem it's not init yet");
	}

	private List<UserRepresentation> getAllKeycloakUsers() {
		RealmResource realmResource = null;
		try {
			realmResource = keycloak.realm(realm);
		} catch (Exception e) {
			logger.info("Trying to fetch users from keycloak seem it's not init yet");
		}
		return realmResource.users().list();
	}
}