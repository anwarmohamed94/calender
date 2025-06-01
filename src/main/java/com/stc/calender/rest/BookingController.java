package com.stc.calender.rest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stc.calender.dto.BookingDTO;
import com.stc.calender.dto.BookingRequestValidationDto;
import com.stc.calender.dto.BookingResponse;
import com.stc.calender.service.BookingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

	@PostMapping("/validate")
	public ResponseEntity<BookingResponse> processBooking(@RequestBody BookingRequestValidationDto request) {

		return ResponseEntity.ok(bookingService.processBooking(request));
	}
	
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookingDTO> createBooking(@RequestBody BookingDTO request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

	@PreAuthorize("hasRole('USER')")
	@GetMapping
	public Page<BookingDTO> getUserBookings(@RequestParam(defaultValue = "0", name = "page") int page,
			@RequestParam(defaultValue = "10", name = "size") int size) {
		return bookingService.getUserBookings(PageRequest.of(page, size));
	}
}