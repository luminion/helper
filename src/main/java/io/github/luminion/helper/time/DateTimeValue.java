package io.github.luminion.helper.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

/**
 * 时间值对象。
 *
 * @author luminion
 */
public class DateTimeValue {
    private final ZonedDateTime zonedDateTime;

    protected DateTimeValue(ZonedDateTime zonedDateTime) {
        this.zonedDateTime = Objects.requireNonNull(zonedDateTime, "zonedDateTime must not be null");
    }

    static DateTimeValue create(ZonedDateTime zonedDateTime) {
        return new DateTimeValue(zonedDateTime);
    }

    public ZonedDateTime toZonedDateTime() {
        return zonedDateTime;
    }

    public LocalDateTime toLocalDateTime() {
        return zonedDateTime.toLocalDateTime();
    }

    public LocalDate toLocalDate() {
        return zonedDateTime.toLocalDate();
    }

    public LocalTime toLocalTime() {
        return zonedDateTime.toLocalTime();
    }

    public Instant toInstant() {
        return zonedDateTime.toInstant();
    }

    public Date toDate() {
        return Date.from(toInstant());
    }

    public long toEpochSecond() {
        return zonedDateTime.toInstant().getEpochSecond();
    }

    public long toEpochMilli() {
        return zonedDateTime.toInstant().toEpochMilli();
    }

    public DateTimeValue withZoneSameInstant(ZoneId zoneId) {
        return create(zonedDateTime.withZoneSameInstant(requireZoneId(zoneId)));
    }

    public DateTimeValue plusDays(long days) {
        return create(zonedDateTime.plusDays(days));
    }

    public DateTimeValue plusHours(long hours) {
        return create(zonedDateTime.plusHours(hours));
    }

    public DateTimeValue plusMinutes(long minutes) {
        return create(zonedDateTime.plusMinutes(minutes));
    }

    public DateTimeValue minusDays(long days) {
        return create(zonedDateTime.minusDays(days));
    }

    public DateTimeValue minusHours(long hours) {
        return create(zonedDateTime.minusHours(hours));
    }

    public DateTimeValue minusMinutes(long minutes) {
        return create(zonedDateTime.minusMinutes(minutes));
    }

    public DateTimeValue startOfDay() {
        return create(zonedDateTime.toLocalDate().atStartOfDay(zonedDateTime.getZone()));
    }

    public DateTimeValue endOfDay() {
        return create(zonedDateTime.toLocalDate().plusDays(1).atStartOfDay(zonedDateTime.getZone()).minusNanos(1));
    }

    public String format(DateTimeFormatter formatter) {
        return zonedDateTime.format(Objects.requireNonNull(formatter, "formatter must not be null"));
    }

    @Override
    public String toString() {
        return toDateTimeString();
    }

    public String toDateTimeString() {
        return format(DateTimeHelper.DATE_TIME_FORMATTER);
    }

    public String toDateHourMinuteString() {
        return format(DateTimeHelper.DATE_HOUR_MINUTE_FORMATTER);
    }

    public String toDateString() {
        return format(DateTimeHelper.DATE_FORMATTER);
    }

    public String toTimeString() {
        return format(DateTimeHelper.TIME_FORMATTER);
    }

    public String toHourMinuteString() {
        return format(DateTimeHelper.HOUR_MINUTE_FORMATTER);
    }

    private static ZoneId requireZoneId(ZoneId zoneId) {
        return Objects.requireNonNull(zoneId, "zoneId must not be null");
    }
}
