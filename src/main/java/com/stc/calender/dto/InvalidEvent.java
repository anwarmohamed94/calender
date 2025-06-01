package com.stc.calender.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvalidEvent {
    private EventDTO event;
    private String reason;
}