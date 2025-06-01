package com.stc.calender.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stc.calender.dto.PropertyDTO;
import com.stc.calender.exception.ResourceNotFoundException;
import com.stc.calender.model.Property;
import com.stc.calender.model.User;
import com.stc.calender.repo.PropertyRepository;
import com.stc.calender.repo.UserRepository;
import com.stc.calender.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PropertyService {
	
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    @Transactional
    public PropertyDTO createProperty(PropertyDTO dto) {
        String userId = SecurityUtils.getCurrentUserId();

        User owner = userRepository.findByKeycloakId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Property property = new Property();
        property.setTitle(dto.getTitle());
        property.setOwner(owner);
        return toDTO(propertyRepository.save(property));
    }

    @Transactional(readOnly = true)
    public List<PropertyDTO> getUserProperties() {
        String userId = SecurityUtils.getCurrentUserId();
        return propertyRepository.findByOwnerKeycloakId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    
    private PropertyDTO toDTO(Property property) {
        return new PropertyDTO(
                property.getId(),
                property.getTitle(),
                property.getOwner().getId()
        );
    }
}