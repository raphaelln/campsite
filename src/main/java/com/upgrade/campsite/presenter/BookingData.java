package com.upgrade.campsite.presenter;

import com.upgrade.campsite.entity.Reservation;
import com.upgrade.campsite.utils.ApplicationUtils;
import lombok.*;

import java.time.LocalDate;

@EqualsAndHashCode
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingData {

    private String transactionId;

    private String name;

    private String email;

    private LocalDate checkIn;

    private LocalDate checkOut;

    public Reservation toReservation() {
        final Reservation reservation = new Reservation();
        reservation.setTransactionId(this.transactionId);
        reservation.setName(this.email);
        reservation.setEmail(this.email);
        reservation.setStartDate(ApplicationUtils.toDate(this.getCheckIn()));
        reservation.setEndDate(ApplicationUtils.toDate(this.getCheckOut()));
        return reservation;
    }
}
