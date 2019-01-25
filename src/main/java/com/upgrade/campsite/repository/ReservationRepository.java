package com.upgrade.campsite.repository;

import com.upgrade.campsite.entity.Reservation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends CrudRepository<Reservation, String> {

    Optional<Reservation> findById(final String id);

    @Query("Select count(r) from Reservation r where r.endDate >= ?1  and r.startDate <= ?2 and (?3 is null or r.transactionId <> ?3)")
    Long countOverlapReservations(final Date checkIn, final Date checkOut, final String transactionId);

    @Query("Select r from Reservation r where r.startDate >= CURRENT_DATE")
    List<Reservation> findNotExpiredReservations();

    Optional<Reservation> findByTransactionId(final String transactionId);
}