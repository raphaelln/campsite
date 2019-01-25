package com.upgrade.campsite.utils;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ApplicationUtils {

    public static Date toDate(final LocalDate date) {
        return Date.from(date.atStartOfDay().toInstant(ZoneOffset.UTC));
    }

    public static LocalDate toLocalDate(final Date date) {
        return date.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
    }

    public static Set<LocalDate> getDatesInRange(final LocalDate startDate, final LocalDate endDate) {
        final Long numDays = getBookingDays(startDate, endDate);
        return Stream
                .iterate(startDate, date -> date.plusDays(1))
                .limit(numDays)
                .collect(Collectors.toSet());
    }

    public static Set<LocalDate> getDatesInRange(final Date startDate, final Date endDate) {
        return getDatesInRange(toLocalDate(startDate), toLocalDate(endDate));
    }

    public static Long getBookingDays(final LocalDate startDate, final LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate.plusDays(1));
    }

}
