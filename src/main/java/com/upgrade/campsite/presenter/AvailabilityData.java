package com.upgrade.campsite.presenter;

import lombok.*;

import java.time.LocalDate;

@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class AvailabilityData implements Comparable<AvailabilityData> {

    private LocalDate date;

    private Boolean available;

    @Override
    public int compareTo(AvailabilityData availabilityData) {
        return this.date.compareTo(availabilityData.getDate());
    }
}
