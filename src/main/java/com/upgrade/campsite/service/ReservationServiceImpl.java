package com.upgrade.campsite.service;

import com.upgrade.campsite.entity.Reservation;
import com.upgrade.campsite.exception.AvailabilityException;
import com.upgrade.campsite.exception.BookingDataException;
import com.upgrade.campsite.exception.ReservationNotFoundException;
import com.upgrade.campsite.presenter.AvailabilityData;
import com.upgrade.campsite.presenter.BookingData;
import com.upgrade.campsite.repository.ReservationCacheRepository;
import com.upgrade.campsite.repository.ReservationRepository;
import com.upgrade.campsite.utils.ApplicationUtils;
import com.upgrade.campsite.utils.ResourceLocker;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReservationServiceImpl implements ReservationService {

    @Autowired(required = true)
    private ReservationRepository reservationRepository;

    @Autowired(required = true)
    private ReservationCacheRepository reservationCacheRepository;

    @Value("${campsite.booking.max-stay}")
    private Long maxBookingDays;

    @Value("${campsite.booking.day-limit-reservation}")
    private Long minBookingLimit;

    @Value("${campsite.booking.month-limit-reservation}")
    private Long maxBookingLimit;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public String book(final BookingData bookingData) throws BookingDataException, AvailabilityException {
        try {
            ResourceLocker.getInstance().lock();
            validateBookingData(bookingData);
            bookingData.setTransactionId(null);
            final Reservation reservation = reservationRepository.save(bookingData.toReservation());
            addReservationInCache(bookingData);
            return reservation.getTransactionId();
        } finally {
            ResourceLocker.getInstance().unlock();
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void unBook(String transactionId) throws ReservationNotFoundException {
        try {
            ResourceLocker.getInstance().lock();
            final Reservation reservation = findReservation(transactionId);
            reservationRepository.delete(reservation);
            removeReservationFromCache(ApplicationUtils.toLocalDate(reservation.getStartDate()),
                    ApplicationUtils.toLocalDate(reservation.getEndDate()));
        } finally {
            ResourceLocker.getInstance().unlock();
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public String modifyBook(BookingData bookingData) throws ReservationNotFoundException, BookingDataException,
            AvailabilityException {
        unBook(bookingData.getTransactionId());
        final String transactionId = book(bookingData);
        return transactionId;
    }

    @Override
    public List<AvailabilityData> findCampsiteAvailability(final LocalDate startDate, LocalDate finalDate) {

        final Set<LocalDate> findRangeDates = ApplicationUtils.getDatesInRange(startDate, finalDate);
        final Set<LocalDate> reservations = reservationCacheRepository.getReservations();
        final List<AvailabilityData> campSiteAvailability = findRangeDates.stream().map( date -> {
            boolean booked = !reservations.contains(date);
            return AvailabilityData.builder().date(date).available(booked).build();
        }).collect(Collectors.toList());
        Collections.sort(campSiteAvailability);
        return campSiteAvailability;
    }

    private Reservation findReservation(final String transactionId) throws ReservationNotFoundException {
        return reservationRepository.findByTransactionId(transactionId)
                .orElseThrow(ReservationNotFoundException::new);
    }

    /**
     * Adds a reservation in cache
     * @param bookingData
     */
    private void addReservationInCache(final BookingData bookingData) {

        if (!reservationCacheRepository.isCacheInitialized()) {
            initializeCache();
        }
        final Set<LocalDate> datesToAdd = ApplicationUtils.getDatesInRange(bookingData.getCheckIn(),
                bookingData.getCheckOut());
        reservationCacheRepository.addReservation(datesToAdd.toArray(new LocalDate[0]));
    }

    /**
     * Remove from cache the reservation dates
     */
    private void removeReservationFromCache(final LocalDate startDate, final LocalDate endDate) {

        final Set<LocalDate> dates = ApplicationUtils.getDatesInRange(startDate, endDate);
        reservationCacheRepository.removeReservation(dates.toArray(new LocalDate[0]));
    }

    /**
     * Method responsible to initialize the redis with booked dates stored in the database
     */
    private void initializeCache() {
        final Set<LocalDate> bookedDates = getBookedDates();
        if (CollectionUtils.isNotEmpty(bookedDates)) {
            reservationCacheRepository.initializeCache(bookedDates.toArray(new LocalDate[0]));
        }
    }

    /**
     * Method responsible find all booked dates which are not expired
     */
    private Set<LocalDate> getBookedDates() {
        final List<Reservation> reservations = reservationRepository.findNotExpiredReservations();
        final Set<LocalDate> bookedDates = new HashSet<>();
        reservations.forEach(reservation -> {
            bookedDates.addAll(ApplicationUtils.getDatesInRange(reservation.getStartDate(), reservation.getEndDate()));
        });

        return bookedDates;
    }

    private void validateBookingData(final BookingData bookingData) throws BookingDataException, AvailabilityException {

        final Long bookingDays = ApplicationUtils.getBookingDays(bookingData.getCheckIn(),
                bookingData.getCheckOut());
        if (bookingDays > maxBookingDays) {
            throw new BookingDataException(String.format("The campsite only be booked up to %d days.", maxBookingDays));
        }

        if (isBookingOutOfLimit(bookingData.getCheckIn())) {
            throw new BookingDataException(String.format("The campsite only be booked with a time limit of %d day " +
                    "ahead or up to %d month in advance.", minBookingLimit, maxBookingLimit));
        }

        if (isSelectedDateUnavailable(bookingData)) {
            throw new AvailabilityException("The selected dates is not available anymore.");
        }
    }

    private Boolean isBookingOutOfLimit(final LocalDate checkIn) {
        Boolean lateBooking = checkIn.minusDays(minBookingLimit).isBefore(LocalDate.now());
        Boolean earlyBooking = checkIn.isAfter(LocalDate.now().plusMonths(maxBookingLimit));
        return lateBooking || earlyBooking;
    }

    private Boolean isSelectedDateUnavailable(final BookingData bookingData) {
        return this.reservationRepository.countOverlapReservations(
                ApplicationUtils.toDate(bookingData.getCheckOut()),
                ApplicationUtils.toDate(bookingData.getCheckIn()),
                bookingData.getTransactionId()) > 0;
    }

}