package com.stc.calender.service;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.stc.calender.dto.BookingDTO;
import com.stc.calender.dto.BookingRequestValidationDto;
import com.stc.calender.dto.BookingResponse;
import com.stc.calender.dto.Conflict;
import com.stc.calender.dto.EventDTO;
import com.stc.calender.dto.EventProcessingData;
import com.stc.calender.dto.Interval;
import com.stc.calender.dto.TimeSlot;
import com.stc.calender.dto.WorkingHours;
import com.stc.calender.exception.ResourceNotFoundException;
import com.stc.calender.model.Booking;
import com.stc.calender.model.Property;
import com.stc.calender.model.User;
import com.stc.calender.repo.BookingRepository;
import com.stc.calender.repo.PropertyRepository;
import com.stc.calender.repo.UserRepository;
import com.stc.calender.util.SecurityUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class BookingService {
	
	private final UserRepository userRepository;
	private final PropertyRepository propertyRepository;
	private final BookingRepository bookingRepository;

	@Transactional(readOnly = true)
	public BookingResponse processBooking(BookingRequestValidationDto request) {
		// Validate and parse inputs
		ZoneId timeZone = validateTimeZone(request.getTimeZone());
		LocalDate date = extractDateFromEvents(request.getEvents());
		WorkingHours workingHours = validateWorkingHours(request.getWorkingHours());

		// Convert times to UTC
		Instant[] workHoursUTC = convertWorkingHoursToUTC(workingHours, timeZone, date);
		Instant workStartUTC = workHoursUTC[0];
		Instant workEndUTC = workHoursUTC[1];

		// Parse events
		List<EventProcessingData> events = parseEvents(request.getEvents(), timeZone);

		// Validate working hours boundaries
		validateEventsWithinWorkingHours(events, workStartUTC, workEndUTC);

		// Detect internal conflicts
		List<Conflict> conflicts = detectConflicts(events, timeZone);

		// 6. Calculate free slots
		List<TimeSlot> freeSlots = calculateFreeSlots(events, workStartUTC, workEndUTC, timeZone);

		return new BookingResponse(conflicts, freeSlots);
	}

	private ZoneId validateTimeZone(String timeZoneStr) {
		try {
			return ZoneId.of(timeZoneStr);
		} catch (DateTimeException e) {
			throw new IllegalArgumentException("Invalid time zone: " + timeZoneStr);
		}
	}

	private LocalDate extractDateFromEvents(List<EventDTO> events) {
		if (events == null || events.isEmpty()) {
			throw new IllegalArgumentException("At least one event is required");
		}
		return ZonedDateTime.parse(events.get(0).getStartTime()).toLocalDate();
	}

	private WorkingHours validateWorkingHours(WorkingHours workingHours) {
		try {
			LocalTime.parse(workingHours.getStart());
			LocalTime.parse(workingHours.getEnd());
			return workingHours;
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Invalid working hours format");
		}
	}

	private Instant[] convertWorkingHoursToUTC(WorkingHours workingHours, ZoneId timeZone, LocalDate date) {
		LocalTime startTime = LocalTime.parse(workingHours.getStart());
		LocalTime endTime = LocalTime.parse(workingHours.getEnd());

		ZonedDateTime zonedStart = ZonedDateTime.of(date, startTime, timeZone);
		ZonedDateTime zonedEnd = ZonedDateTime.of(date, endTime, timeZone);

		return new Instant[] { zonedStart.toInstant(), zonedEnd.toInstant() };
	}

	private List<EventProcessingData> parseEvents(List<EventDTO> events, ZoneId timeZone) {
		return events.stream().map(event -> {
			try {
				ZonedDateTime start = ZonedDateTime.parse(event.getStartTime());
				ZonedDateTime end = ZonedDateTime.parse(event.getEndTime());
				return new EventProcessingData(event, start.withZoneSameInstant(ZoneOffset.UTC).toInstant(),
						end.withZoneSameInstant(ZoneOffset.UTC).toInstant());
			} catch (DateTimeException e) {
				throw new IllegalArgumentException("Invalid event time format for: " + event.getTitle());
			}
		}).collect(Collectors.toList());
	}

	private void validateEventsWithinWorkingHours(List<EventProcessingData> events, Instant workStart,
			Instant workEnd) {
		events.forEach(event -> {
			if (event.getStart().isBefore(workStart) || event.getEnd().isAfter(workEnd)) {
				throw new IllegalArgumentException(
						"Event '" + event.getEventDTO().getTitle() + "' is outside working hours");
			}
		});
	}

	private List<Conflict> detectConflicts(List<EventProcessingData> events, ZoneId timeZone) {
		List<Conflict> conflicts = new ArrayList<>();
		events.sort(Comparator.comparing(EventProcessingData::getStart));

		for (int i = 0; i < events.size(); i++) {
			for (int j = i + 1; j < events.size(); j++) {
				EventProcessingData e1 = events.get(i);
				EventProcessingData e2 = events.get(j);

				// Stop if events can't overlap
				if (e1.getEnd().isBefore(e2.getStart()) || e1.getEnd().equals(e2.getStart())) {
					break;
				}

				if (eventsOverlap(e1, e2)) {
					Instant overlapStart = later(e1.getStart(), e2.getStart());
					Instant overlapEnd = earlier(e1.getEnd(), e2.getEnd());

					conflicts.add(new Conflict(e1.getEventDTO().getTitle(), e2.getEventDTO().getTitle(),
							formatInstant(overlapStart, timeZone), formatInstant(overlapEnd, timeZone)));
				}
			}
		}
		return conflicts;
	}

	private boolean eventsOverlap(EventProcessingData e1, EventProcessingData e2) {
		return e1.getStart().isBefore(e2.getEnd()) && e1.getEnd().isAfter(e2.getStart());
	}

	private Instant later(Instant i1, Instant i2) {
		return i1.isAfter(i2) ? i1 : i2;
	}

	private Instant earlier(Instant i1, Instant i2) {
		return i1.isBefore(i2) ? i1 : i2;
	}

	private List<TimeSlot> calculateFreeSlots(List<EventProcessingData> events, Instant workStartUTC,
			Instant workEndUTC, ZoneId timeZone) {
		// Create busy intervals
		List<Interval> busyIntervals = events.stream().map(event -> new Interval(event.getStart(), event.getEnd()))
				.collect(Collectors.toList());

		// Merge overlapping intervals
		List<Interval> merged = mergeIntervals(busyIntervals);

		// Compute free slots
		List<Interval> freeSlotsUTC = new ArrayList<>();
		Instant current = workStartUTC;

		for (Interval busy : merged) {
			if (current.isBefore(busy.getStart())) {
				freeSlotsUTC.add(new Interval(current, busy.getStart()));
			}
			current = later(current, busy.getEnd());
		}

		if (current.isBefore(workEndUTC)) {
			freeSlotsUTC.add(new Interval(current, workEndUTC));
		}

		// Convert to time slots
		return freeSlotsUTC.stream().map(interval -> new TimeSlot(formatInstant(interval.getStart(), timeZone),
				formatInstant(interval.getEnd(), timeZone))).collect(Collectors.toList());
	}

	private List<Interval> mergeIntervals(List<Interval> intervals) {
		if (intervals.isEmpty())
			return Collections.emptyList();

		intervals.sort(Comparator.comparing(Interval::getStart));
		List<Interval> merged = new ArrayList<>();
		Interval current = intervals.get(0);

		for (int i = 1; i < intervals.size(); i++) {
			Interval next = intervals.get(i);
			if (current.getEnd().isBefore(next.getStart())) {
				merged.add(current);
				current = next;
			} else {
				current = new Interval(current.getStart(), later(current.getEnd(), next.getEnd()));
			}
		}
		merged.add(current);
		return merged;
	}

	private String formatInstant(Instant instant, ZoneId timeZone) {
		return ZonedDateTime.ofInstant(instant, timeZone).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
	}

    @Transactional
    public BookingDTO createBooking(BookingDTO bookingDTO) {
    	
    	String userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findByKeycloakId(userId)
        		 .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        
        Property property = propertyRepository.findById(bookingDTO.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        
        // 2. Convert times to UTC
        Instant start = parseAndConvertToUTC(bookingDTO.getStartDate());
        Instant end = parseAndConvertToUTC(bookingDTO.getEndDate());
        
        // 3. Check for overlapping bookings
        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
            bookingDTO.getPropertyId(), 
            start, 
            end
        );
        
        if (!overlapping.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking overlaps with existing reservation");
        }
        
        Booking booking = new Booking();
        booking.setProperty(property);
        booking.setUser(user);
        booking.setStartDate(start);
        booking.setEndDate(end);
        
        Booking savedBooking = bookingRepository.save(booking);
        return toDTO(savedBooking);
    }

    private Instant parseAndConvertToUTC(Instant dateTime) {
        return ZonedDateTime.parse(dateTime.toString())
            .withZoneSameInstant(ZoneOffset.UTC)
            .toInstant();
    }
    
	@Transactional(readOnly = true)
	public Page<BookingDTO> getUserBookings(Pageable pageable) {
		String userId = SecurityUtils.getCurrentUserId();

		return bookingRepository.findByUserKeycloakId(userId, pageable).map(this::toDTO);
	}

	private BookingDTO toDTO(Booking booking) {
		return new BookingDTO(booking.getId(), booking.getProperty().getId(), booking.getUser().getId(),
				booking.getStartDate(), booking.getEndDate());
	}
}