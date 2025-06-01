package com.stc.calender.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Conflict {
    private String event1;
    private String event2;
    private String overlapStart;
    private String overlapEnd;
}