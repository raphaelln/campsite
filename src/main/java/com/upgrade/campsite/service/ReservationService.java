package com.upgrade.campsite.service;

import com.upgrade.campsite.exception.AvailabilityException;
import com.upgrade.campsite.exception.BookingDataException;
import com.upgrade.campsite.exception.ReservationNotFoundException;
import com.upgrade.campsite.presenter.AvailabilityData;
import com.upgrade.campsite.presenter.BookingData;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {

    String book(final BookingData bookingData) throws BookingDataException, AvailabilityException;

    void unBook(final String transactionId) throws ReservationNotFoundException;

    String modifyBook(final BookingData bookingData) throws ReservationNotFoundException, BookingDataException,
            AvailabilityException;

    List<AvailabilityData> findCampsiteAvailability(final LocalDate startDate, final LocalDate finalDate);

}
