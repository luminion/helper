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
 * 时间静态入口。
 * <p>
 * 负责创建和解析 {@link DateTimeValue}，实例行为统一放在值对象上。
 *
 * @author luminion
 */
public abstract class DateTimeHelper {
    /**
     * 默认日期时间格式：`yyyy-MM-dd HH:mm:ss`
     */
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * 默认日期时分格式：`yyyy-MM-dd HH:mm`
     */
    public static final DateTimeFormatter DATE_HOUR_MINUTE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter DATE_HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter HOUR_MINUTE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private DateTimeHelper() {}

    /**
     * 按系统默认时区创建当前时间值。
     */
    public static DateTimeValue of() {
        return of(ZoneId.systemDefault());
    }

    /**
     * 按指定时区创建当前时间值。
     */
    public static DateTimeValue of(ZoneId zoneId) {
        return DateTimeValue.create(ZonedDateTime.now(requireZoneId(zoneId)));
    }

    /**
     * 当前时间的便捷入口。
     */
    public static DateTimeValue now() {
        return of();
    }

    /**
     * 按指定时区获取当前时间。
     */
    public static DateTimeValue now(ZoneId zoneId) {
        return of(zoneId);
    }

    /**
     * 兼容旧入口，建议改用 {@link #of()}。
     *
     * @deprecated 请使用 {@link #of()}
     */
    @Deprecated
    public static DateTimeValue create() {
        return of();
    }

    /**
     * 兼容旧入口，建议改用 {@link #of(ZoneId)}。
     *
     * @deprecated 请使用 {@link #of(ZoneId)}
     */
    @Deprecated
    public static DateTimeValue create(ZoneId zoneId) {
        return of(zoneId);
    }

    /**
     * 从 {@link ZonedDateTime} 创建时间值对象。
     */
    public static DateTimeValue of(ZonedDateTime zonedDateTime) {
        return DateTimeValue.create(Objects.requireNonNull(zonedDateTime, "zonedDateTime must not be null"));
    }

    public static DateTimeValue of(LocalDateTime localDateTime) {
        return of(localDateTime, ZoneId.systemDefault());
    }

    public static DateTimeValue of(LocalDateTime localDateTime, ZoneId zoneId) {
        Objects.requireNonNull(localDateTime, "localDateTime must not be null");
        return DateTimeValue.create(localDateTime.atZone(requireZoneId(zoneId)));
    }

    public static DateTimeValue of(LocalDate localDate) {
        return of(localDate, ZoneId.systemDefault());
    }

    public static DateTimeValue of(LocalDate localDate, ZoneId zoneId) {
        Objects.requireNonNull(localDate, "localDate must not be null");
        return DateTimeValue.create(localDate.atStartOfDay(requireZoneId(zoneId)));
    }

    public static DateTimeValue of(LocalTime localTime) {
        return of(localTime, LocalDate.now(), ZoneId.systemDefault());
    }

    public static DateTimeValue of(LocalTime localTime, LocalDate localDate, ZoneId zoneId) {
        Objects.requireNonNull(localTime, "localTime must not be null");
        Objects.requireNonNull(localDate, "localDate must not be null");
        return DateTimeValue.create(localTime.atDate(localDate).atZone(requireZoneId(zoneId)));
    }

    public static DateTimeValue of(Date date) {
        Objects.requireNonNull(date, "date must not be null");
        return DateTimeValue.create(ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
    }

    public static DateTimeValue of(Instant instant) {
        return of(instant, ZoneId.systemDefault());
    }

    public static DateTimeValue of(Instant instant, ZoneId zoneId) {
        Objects.requireNonNull(instant, "instant must not be null");
        return DateTimeValue.create(ZonedDateTime.ofInstant(instant, requireZoneId(zoneId)));
    }

    public static DateTimeValue ofEpochSecond(long epochSecond) {
        return DateTimeValue.create(ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), ZoneId.systemDefault()));
    }

    public static DateTimeValue ofEpochMilli(long epochMilli) {
        return DateTimeValue.create(ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault()));
    }

    public static DateTimeValue ofEpochDay(long epochDay) {
        return DateTimeValue.create(LocalDate.ofEpochDay(epochDay).atStartOfDay(ZoneId.systemDefault()));
    }

    /**
     * 从文本解析日期时间。
     */
    public static DateTimeValue parseDateTime(String text) {
        return parseDateTime(text, DATE_TIME_FORMATTER);
    }

    public static DateTimeValue parseDateTime(String text, DateTimeFormatter formatter) {
        Objects.requireNonNull(text, "text must not be null");
        Objects.requireNonNull(formatter, "formatter must not be null");
        return of(LocalDateTime.parse(text, formatter));
    }

    /**
     * 从文本解析日期。
     */
    public static DateTimeValue parseDate(String text) {
        return parseDate(text, DATE_FORMATTER);
    }

    public static DateTimeValue parseDate(String text, DateTimeFormatter formatter) {
        Objects.requireNonNull(text, "text must not be null");
        Objects.requireNonNull(formatter, "formatter must not be null");
        return of(LocalDate.parse(text, formatter));
    }

    /**
     * 从文本解析时间。
     */
    public static DateTimeValue parseTime(String text) {
        return parseTime(text, TIME_FORMATTER);
    }

    public static DateTimeValue parseTime(String text, DateTimeFormatter formatter) {
        Objects.requireNonNull(text, "text must not be null");
        Objects.requireNonNull(formatter, "formatter must not be null");
        return of(LocalTime.parse(text, formatter));
    }

    private static ZoneId requireZoneId(ZoneId zoneId) {
        return Objects.requireNonNull(zoneId, "zoneId must not be null");
    }
}
