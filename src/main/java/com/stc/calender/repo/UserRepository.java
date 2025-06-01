package com.stc.calender.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stc.calender.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByKeycloakId(String keycloakId);
}
