package com.stc.calender.rest;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stc.calender.dto.PropertyDTO;
import com.stc.calender.service.PropertyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public PropertyDTO createProperty(@RequestBody PropertyDTO dto) {
        return propertyService.createProperty(dto);
    }

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public List<PropertyDTO> getUserProperties() {
        return propertyService.getUserProperties();
    }
}