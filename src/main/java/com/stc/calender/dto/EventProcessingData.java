package com.stc.calender.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventProcessingData {
    private EventDTO eventDTO;
    private Instant start;
    private Instant end;
}