package com.upgrade.campsite.controller;

import com.upgrade.campsite.exception.AvailabilityException;
import com.upgrade.campsite.exception.BookingDataException;
import com.upgrade.campsite.exception.ReservationNotFoundException;
import com.upgrade.campsite.presenter.AvailabilityData;
import com.upgrade.campsite.presenter.BookingData;
import com.upgrade.campsite.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("v1/reservation")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @DeleteMapping("/{transactionid}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteReservation(@PathVariable String transactionid) throws ReservationNotFoundException {
        reservationService.unBook(transactionid);
    }

    @PutMapping(value = "/{transactionId}", consumes = { MediaType.APPLICATION_JSON_VALUE })
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateReservation(@PathVariable String transactionId, @RequestBody BookingData bookingData)
            throws ReservationNotFoundException, AvailabilityException, BookingDataException {
        bookingData.setTransactionId(transactionId);
        reservationService.modifyBook(bookingData);
    }

    @PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE})
    public String createReservation(@RequestBody BookingData bookingData)
            throws AvailabilityException, BookingDataException {
        return reservationService.book(bookingData);
    }

    @GetMapping(value = "list-availability", produces = { MediaType.APPLICATION_JSON_VALUE })
    public List<AvailabilityData> getAvailabilityData(@RequestParam String startDate, @RequestParam String finalDate) {
        return reservationService.findCampsiteAvailability(LocalDate.parse(startDate), LocalDate.parse(finalDate));
    }
}
