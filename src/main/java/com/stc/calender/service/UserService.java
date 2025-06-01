package com.stc.calender.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stc.calender.model.User;
import com.stc.calender.repo.UserRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class UserService {
	
	private final UserRepository userRepository;

	@Transactional
	public void createLocalUser(String username, String email, String keycloakId) {

		if (!userRepository.findByKeycloakId(keycloakId).isPresent()) {
			User user = new User();
			user.setName(username != null ? username : email.split("@")[0]);
			user.setEmail(email);
			user.setKeycloakId(keycloakId);

			userRepository.save(user);
		}

	}
}