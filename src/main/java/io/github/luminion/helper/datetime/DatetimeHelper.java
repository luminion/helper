package io.github.luminion.helper.datetime;

import io.github.luminion.helper.time.DateTimeHelper;
import io.github.luminion.helper.time.DateTimeValue;

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
 * 兼容旧包路径，建议改用 {@link io.github.luminion.helper.time.DateTimeHelper}。
 *
 * @author luminion
 * @deprecated 请使用 {@link io.github.luminion.helper.time.DateTimeHelper}
 */
@Deprecated
public class DatetimeHelper extends DateTimeValue {
    public static final DateTimeFormatter FORMATTER_DATE_TIME_ = DateTimeHelper.DATE_TIME_FORMATTER;
    public static final DateTimeFormatter FORMATTER_DATE_HOUR_MINUTE = DateTimeHelper.DATE_HOUR_MINUTE_FORMATTER;
    public static final DateTimeFormatter FORMATTER_DATE_HOUR = DateTimeHelper.DATE_HOUR_FORMATTER;
    public static final DateTimeFormatter FORMATTER_DATE = DateTimeHelper.DATE_FORMATTER;
    public static final DateTimeFormatter FORMATTER_TIME = DateTimeHelper.TIME_FORMATTER;
    public static final DateTimeFormatter FORMATTER_HOUR_MINUTE = DateTimeHelper.HOUR_MINUTE_FORMATTER;

    private DatetimeHelper(ZonedDateTime zonedDateTime) {
        super(zonedDateTime);
    }

    public static DatetimeHelper now() {
        return now(ZoneId.systemDefault());
    }

    public static DatetimeHelper now(ZoneId zoneId) {
        return new DatetimeHelper(ZonedDateTime.now(requireZoneId(zoneId)));
    }

    public static DatetimeHelper of(ZonedDateTime zonedDateTime) {
        return new DatetimeHelper(Objects.requireNonNull(zonedDateTime, "zonedDateTime must not be null"));
    }

    public static DatetimeHelper of(LocalDateTime localDateTime) {
        return of(localDateTime, ZoneId.systemDefault());
    }

    public static DatetimeHelper of(LocalDateTime localDateTime, ZoneId zoneId) {
        Objects.requireNonNull(localDateTime, "localDateTime must not be null");
        return new DatetimeHelper(localDateTime.atZone(requireZoneId(zoneId)));
    }

    public static DatetimeHelper of(LocalDate localDate) {
        return of(localDate, ZoneId.systemDefault());
    }

    public static DatetimeHelper of(LocalDate localDate, ZoneId zoneId) {
        Objects.requireNonNull(localDate, "localDate must not be null");
        return new DatetimeHelper(localDate.atStartOfDay(requireZoneId(zoneId)));
    }

    public static DatetimeHelper of(LocalTime localTime) {
        return of(localTime, LocalDate.now(), ZoneId.systemDefault());
    }

    public static DatetimeHelper of(LocalTime localTime, LocalDate localDate, ZoneId zoneId) {
        Objects.requireNonNull(localTime, "localTime must not be null");
        Objects.requireNonNull(localDate, "localDate must not be null");
        return new DatetimeHelper(localTime.atDate(localDate).atZone(requireZoneId(zoneId)));
    }

    public static DatetimeHelper of(Date date) {
        Objects.requireNonNull(date, "date must not be null");
        return new DatetimeHelper(ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
    }

    public static DatetimeHelper of(Instant instant) {
        return of(instant, ZoneId.systemDefault());
    }

    public static DatetimeHelper of(Instant instant, ZoneId zoneId) {
        Objects.requireNonNull(instant, "instant must not be null");
        return new DatetimeHelper(ZonedDateTime.ofInstant(instant, requireZoneId(zoneId)));
    }

    public static DatetimeHelper ofEpochSecond(long epochSecond) {
        return new DatetimeHelper(ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), ZoneId.systemDefault()));
    }

    public static DatetimeHelper ofEpochMilli(long epochMilli) {
        return new DatetimeHelper(ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault()));
    }

    public static DatetimeHelper ofEpochDay(long epochDay) {
        return new DatetimeHelper(LocalDate.ofEpochDay(epochDay).atStartOfDay(ZoneId.systemDefault()));
    }

    public static DatetimeHelper parseDateTime(String text) {
        return parseDateTime(text, FORMATTER_DATE_TIME_);
    }

    public static DatetimeHelper parseDateTime(String text, DateTimeFormatter formatter) {
        Objects.requireNonNull(text, "text must not be null");
        Objects.requireNonNull(formatter, "formatter must not be null");
        return of(LocalDateTime.parse(text, formatter));
    }

    public static DatetimeHelper parseDate(String text) {
        return parseDate(text, FORMATTER_DATE);
    }

    public static DatetimeHelper parseDate(String text, DateTimeFormatter formatter) {
        Objects.requireNonNull(text, "text must not be null");
        Objects.requireNonNull(formatter, "formatter must not be null");
        return of(LocalDate.parse(text, formatter));
    }

    public static DatetimeHelper parseTime(String text) {
        return parseTime(text, FORMATTER_TIME);
    }

    public static DatetimeHelper parseTime(String text, DateTimeFormatter formatter) {
        Objects.requireNonNull(text, "text must not be null");
        Objects.requireNonNull(formatter, "formatter must not be null");
        return of(LocalTime.parse(text, formatter));
    }

    @Override
    public DatetimeHelper withZoneSameInstant(ZoneId zoneId) {
        return new DatetimeHelper(toZonedDateTime().withZoneSameInstant(requireZoneId(zoneId)));
    }

    @Override
    public DatetimeHelper plusDays(long days) {
        return new DatetimeHelper(toZonedDateTime().plusDays(days));
    }

    @Override
    public DatetimeHelper plusHours(long hours) {
        return new DatetimeHelper(toZonedDateTime().plusHours(hours));
    }

    @Override
    public DatetimeHelper plusMinutes(long minutes) {
        return new DatetimeHelper(toZonedDateTime().plusMinutes(minutes));
    }

    @Override
    public DatetimeHelper minusDays(long days) {
        return new DatetimeHelper(toZonedDateTime().minusDays(days));
    }

    @Override
    public DatetimeHelper minusHours(long hours) {
        return new DatetimeHelper(toZonedDateTime().minusHours(hours));
    }

    @Override
    public DatetimeHelper minusMinutes(long minutes) {
        return new DatetimeHelper(toZonedDateTime().minusMinutes(minutes));
    }

    @Override
    public DatetimeHelper startOfDay() {
        ZonedDateTime zonedDateTime = toZonedDateTime();
        return new DatetimeHelper(zonedDateTime.toLocalDate().atStartOfDay(zonedDateTime.getZone()));
    }

    @Override
    public DatetimeHelper endOfDay() {
        ZonedDateTime zonedDateTime = toZonedDateTime();
        return new DatetimeHelper(zonedDateTime.toLocalDate().plusDays(1).atStartOfDay(zonedDateTime.getZone()).minusNanos(1));
    }

    private static ZoneId requireZoneId(ZoneId zoneId) {
        return Objects.requireNonNull(zoneId, "zoneId must not be null");
    }
}
