package com.stc.calender.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookingRequestValidationDto {
    private WorkingHours workingHours;
    private String timeZone;
    private List<EventDTO> events;
}