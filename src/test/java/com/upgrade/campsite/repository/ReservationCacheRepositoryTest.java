package com.upgrade.campsite.repository;

import com.upgrade.campsite.CampsiteApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = CampsiteApplication.class)
@RunWith(SpringRunner.class)
@ActiveProfiles( "test" )
public class ReservationCacheRepositoryTest {

    @Autowired(required =  true)
    private ReservationCacheRepository reservationCacheRepository;

    @Autowired(required =  true)
    private RedisTemplate<String, LocalDate> template;

    @Before
    public void setup() {
        template.delete("reservations");
    }

    private LocalDate[] getLocalDates() {
        Set<LocalDate> localDates = new HashSet<>();
        localDates.add(LocalDate.now());
        localDates.add(LocalDate.now().plusDays(1));
        localDates.add(LocalDate.now().plusDays(2));
        return localDates.toArray(new LocalDate[0]);
    }

    @Test
    public void testAddReservation() {
        reservationCacheRepository.addReservation(getLocalDates());
        assertEquals(reservationCacheRepository.getReservations(), new HashSet(Arrays.asList(getLocalDates())));
    }

    @Test
    public void testRemoveReservation() {
        reservationCacheRepository.addReservation(getLocalDates());
        reservationCacheRepository.removeReservation(LocalDate.now().plusDays(1));
        final Set<LocalDate> expectedDates = new HashSet<>(Arrays.asList(LocalDate.now(), LocalDate.now().plusDays(2)));
        assertEquals(reservationCacheRepository.getReservations(), expectedDates);
    }

    @Test
    public void testGetReservations() {
        reservationCacheRepository.addReservation(getLocalDates());
        assertEquals(reservationCacheRepository.getReservations(), new HashSet(Arrays.asList(getLocalDates())));
    }

    @Test
    public void testInitializeCache() {
        reservationCacheRepository.initializeCache(getLocalDates());
        assertEquals(reservationCacheRepository.getReservations(), new HashSet(Arrays.asList(getLocalDates())));
    }
}