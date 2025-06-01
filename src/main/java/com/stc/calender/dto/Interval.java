package com.stc.calender.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Interval {
    private Instant start;
    private Instant end;
}