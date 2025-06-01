package com.stc.calender.repo;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stc.calender.model.Property;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    // Corrected method names using proper naming convention
    
    @Query("SELECT p FROM Property p JOIN p.owner u WHERE u.keycloakId = :keycloakId")
    List<Property> findByOwnerKeycloakId(@Param("keycloakId") String keycloakId);
    
    @Query("SELECT p FROM Property p JOIN p.owner u WHERE u.keycloakId = :keycloakId")
    Page<Property> findByOwnerKeycloakId(@Param("keycloakId") String keycloakId, Pageable pageable);
    
    // Alternative derived query method
    Page<Property> findByOwner_KeycloakId(String keycloakId, Pageable pageable);
    
    // Find properties by owner's ID with pagination
    Page<Property> findByOwnerId(Long ownerId, Pageable pageable);
    
    @Query("SELECT p FROM Property p JOIN p.owner u WHERE p.id = :propertyId AND u.keycloakId = :keycloakId")
    Optional<Property> findByIdAndOwnerKeycloakId(
        @Param("propertyId") Long propertyId, 
        @Param("keycloakId") String keycloakId
    );
    
    // Derived query method for the same
    Optional<Property> findByIdAndOwner_KeycloakId(Long propertyId, String keycloakId);
}