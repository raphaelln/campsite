package com.upgrade.campsite.service;

import com.upgrade.campsite.CampsiteApplication;
import com.upgrade.campsite.entity.Reservation;
import com.upgrade.campsite.exception.AvailabilityException;
import com.upgrade.campsite.exception.BookingDataException;
import com.upgrade.campsite.presenter.AvailabilityData;
import com.upgrade.campsite.presenter.BookingData;
import com.upgrade.campsite.repository.ReservationRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = CampsiteApplication.class)
@RunWith(SpringRunner.class)
@ActiveProfiles( "test" )
public class ReservationServiceTest
{
    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RedisTemplate<String, LocalDate> template;

    @Value("${campsite.booking.max-stay}")
    private Long maxBookingDays;

    @Value("${campsite.booking.day-limit-reservation}")
    private Long minBookingLimit;

    @Value("${campsite.booking.month-limit-reservation}")
    private Long maxBookingLimit;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private BookingData getDefaulBookingData(LocalDate checkIn, LocalDate checkout) {
        return BookingData.builder()
                .checkIn(checkIn)
                .checkOut(checkout)
                .email("thomas.edison@gmail.com")
                .name("Thomas Edison")
                .build();
    }

    @Before
    public void setup() throws Exception {
        reservationService.book(getDefaulBookingData(LocalDate.now().plusDays(5), LocalDate.now().plusDays(7)));
    }

    @After
    public void after() throws Exception {
        reservationRepository.deleteAll();
        template.delete("reservations");
    }

    @Test
    public void testCheckinOverlap() throws Exception {
        expectedException.expect(AvailabilityException.class);
        reservationService.book(getDefaulBookingData(LocalDate.now().plusDays(5), LocalDate.now().plusDays(5)));
    }

    @Test
    public void testSameReservationDates() throws Exception {
        expectedException.expect(AvailabilityException.class);
        reservationService.book(getDefaulBookingData(LocalDate.now().plusDays(5), LocalDate.now().plusDays(7)));
    }

    @Test
    public void testOverlap() throws Exception {
        expectedException.expect(AvailabilityException.class);
        reservationService.book(getDefaulBookingData(LocalDate.now().plusDays(6), LocalDate.now().plusDays(7)));
    }

    @Test
    public void testCheckoutOverlap() throws Exception {
        expectedException.expect(AvailabilityException.class);
        reservationService.book(getDefaulBookingData(LocalDate.now().plusDays(7), LocalDate.now().plusDays(7)));
    }

    @Test
    public void testBookingDaysOverLimit() throws Exception {
        expectedException.expect(BookingDataException.class);
        expectedException.expectMessage(String.format("The campsite only be booked up to %d days.", maxBookingDays));
        LocalDate checkIn = LocalDate.now().plusDays(3);
        reservationService.book(getDefaulBookingData(checkIn, checkIn.plusDays(maxBookingDays + 1)));
    }

    @Test
    public void testLateBooking() throws Exception {
        expectedException.expect(BookingDataException.class);
        expectedException.expectMessage(String.format("The campsite only be booked with a time limit of %d day " +
            "ahead or up to %d month in advance.", minBookingLimit, maxBookingLimit));
        reservationService.book(getDefaulBookingData(LocalDate.now(), LocalDate.now().plusDays(2)));
    }

    @Test
    public void testEarlyBooking() throws Exception {
        expectedException.expect(BookingDataException.class);
        expectedException.expectMessage(String.format("The campsite only be booked with a time limit of %d day " +
            "ahead or up to %d month in advance.", minBookingLimit, maxBookingLimit));

        reservationService.book(getDefaulBookingData(LocalDate.now().plusMonths(1).plusDays(1),
                LocalDate.now().plusMonths(1).plusDays(2)));
    }

    @Test
    public void findCampsiteAvailability() throws Exception {
        final List<AvailabilityData> campsiteAvailabilityList = reservationService.findCampsiteAvailability(
                LocalDate.now(), LocalDate.now().plusDays(10));

        final List<LocalDate> bookedDates = campsiteAvailabilityList.stream()
                .filter(a  -> !a.getAvailable())
                .map(d -> d.getDate())
                .collect(Collectors.toList());
        assertEquals(bookedDates,
                Arrays.asList(LocalDate.now().plusDays(5),LocalDate.now().plusDays(6),LocalDate.now().plusDays(7)));
    }

    @Test
    public void testUnBook() throws Exception {
        final Reservation reservation = reservationRepository.findNotExpiredReservations().get(0);
        reservationService.unBook(reservation.getTransactionId());
        assertEquals(Optional.empty(), reservationRepository.findByTransactionId(reservation.getTransactionId()));
    }
}