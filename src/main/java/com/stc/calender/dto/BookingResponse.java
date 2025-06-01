package com.stc.calender.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BookingResponse {
    private List<Conflict> conflicts;
    private List<TimeSlot> freeSlots;
}