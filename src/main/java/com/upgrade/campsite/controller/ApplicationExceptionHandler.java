package com.upgrade.campsite.controller;

import com.upgrade.campsite.exception.AvailabilityException;
import com.upgrade.campsite.exception.BookingDataException;
import com.upgrade.campsite.exception.ReservationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ApplicationExceptionHandler {

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Reservation not Found.")
    @ExceptionHandler(ReservationNotFoundException.class)
    public void handleNotFound() {}

    @ExceptionHandler({ BookingDataException.class, AvailabilityException.class })
    public ResponseEntity<String> handleBookingDataException(final Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }
}
