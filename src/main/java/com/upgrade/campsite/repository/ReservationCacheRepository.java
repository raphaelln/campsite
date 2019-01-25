package com.upgrade.campsite.repository;

import java.time.LocalDate;
import java.util.Set;

public interface ReservationCacheRepository {

    void addReservation(final LocalDate... dates);

    void removeReservation(final LocalDate... dates);

    void initializeCache(final LocalDate... dates);

    Boolean isCacheInitialized();

    Set<LocalDate> getReservations();
}
