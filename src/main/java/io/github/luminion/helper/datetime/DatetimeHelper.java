package io.github.luminion.helper.datetime;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

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
 * 时间日期助手
 *
 * @author luminion
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DatetimeHelper {
    public static final DateTimeFormatter FORMATTER_DATE_TIME_ = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter FORMATTER_DATE_HOUR_MINUTE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter FORMATTER_DATE_HOUR = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
    public static final DateTimeFormatter FORMATTER_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter FORMATTER_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter FORMATTER_HOUR_MINUTE = DateTimeFormatter.ofPattern("HH:mm");
    private final ZonedDateTime zonedDateTime;

    /**
     * 获取当前时间
     *
     * @return {@link DatetimeHelper}
     */
    public static DatetimeHelper now() {
        return now(ZoneId.systemDefault());
    }

    public static DatetimeHelper now(ZoneId zoneId) {
        return new DatetimeHelper(ZonedDateTime.now(requireZoneId(zoneId)));
    }

    /**
     * 根据 {@link ZonedDateTime} 创建
     */
    public static DatetimeHelper of(ZonedDateTime zonedDateTime) {
        return new DatetimeHelper(Objects.requireNonNull(zonedDateTime, "zonedDateTime must not be null"));
    }

    /**
     * 根据 {@link LocalDateTime} 创建
     *
     * @param localDateTime {@link LocalDateTime}
     * @return {@link DatetimeHelper}
     */
    public static DatetimeHelper of(LocalDateTime localDateTime) {
        return of(localDateTime, ZoneId.systemDefault());
    }

    public static DatetimeHelper of(LocalDateTime localDateTime, ZoneId zoneId) {
        Objects.requireNonNull(localDateTime, "localDateTime must not be null");
        return new DatetimeHelper(localDateTime.atZone(requireZoneId(zoneId)));
    }

    /**
     * 根据 {@link LocalDate} 创建
     *
     * @param localDate {@link LocalDate}
     * @return {@link DatetimeHelper}
     */
    public static DatetimeHelper of(LocalDate localDate) {
        return of(localDate, ZoneId.systemDefault());
    }

    public static DatetimeHelper of(LocalDate localDate, ZoneId zoneId) {
        Objects.requireNonNull(localDate, "localDate must not be null");
        return new DatetimeHelper(localDate.atStartOfDay(requireZoneId(zoneId)));
    }

    /**
     * 根据 {@link LocalTime} 创建
     *
     * @param localTime {@link LocalTime}
     * @return {@link DatetimeHelper}
     */
    public static DatetimeHelper of(LocalTime localTime) {
        return of(localTime, LocalDate.now(), ZoneId.systemDefault());
    }

    public static DatetimeHelper of(LocalTime localTime, LocalDate localDate, ZoneId zoneId) {
        Objects.requireNonNull(localTime, "localTime must not be null");
        Objects.requireNonNull(localDate, "localDate must not be null");
        return new DatetimeHelper(localTime.atDate(localDate).atZone(requireZoneId(zoneId)));
    }

    /**
     * 根据 {@link Date} 创建
     *
     * @param date {@link Date}
     * @return {@link DatetimeHelper}
     */
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

    /**
     * 根据纪元秒创建
     *
     * @param epochSecond 从 1970-01-01T00:00:00Z 开始的秒数
     * @return {@link DatetimeHelper}
     */
    public static DatetimeHelper ofEpochSecond(long epochSecond) {
        return new DatetimeHelper(ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), ZoneId.systemDefault()));
    }

    /**
     * 根据纪元毫秒创建
     *
     * @param epochMilli 从 1970-01-01T00:00:00Z 开始的毫秒数
     * @return {@link DatetimeHelper}
     */
    public static DatetimeHelper ofEpochMilli(long epochMilli) {
        return new DatetimeHelper(ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault()));
    }

    /**
     * 根据纪元日创建
     *
     * @param epochDay 从 1970-01-01 开始的天数
     * @return {@link DatetimeHelper}
     */
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

    /**
     * 转为 {@link ZonedDateTime}
     */
    public ZonedDateTime toZonedDateTime() {
        return zonedDateTime;
    }

    /**
     * 转为 {@link LocalDateTime}
     *
     * @return {@link LocalDateTime}
     */
    public LocalDateTime toLocalDateTime() {
        return zonedDateTime.toLocalDateTime();
    }

    /**
     * 转为 {@link LocalDate}
     *
     * @return {@link LocalDate}
     */
    public LocalDate toLocalDate() {
        return zonedDateTime.toLocalDate();
    }

    /**
     * 转为 {@link LocalTime}
     *
     * @return {@link LocalTime}
     */
    public LocalTime toLocalTime() {
        return zonedDateTime.toLocalTime();
    }

    public Instant toInstant() {
        return zonedDateTime.toInstant();
    }

    public Date toDate() {
        return Date.from(toInstant());
    }

    /**
     * 转为纪元秒
     *
     * @return 从 1970-01-01T00:00:00Z 开始的秒数
     */
    public long toEpochSecond() {
        return zonedDateTime.toInstant().getEpochSecond();
    }

    /**
     * 转为纪元毫秒
     *
     * @return 从 1970-01-01T00:00:00Z 开始的毫秒数
     */
    public long toEpochMilli() {
        return zonedDateTime.toInstant().toEpochMilli();
    }

    public DatetimeHelper withZoneSameInstant(ZoneId zoneId) {
        return new DatetimeHelper(zonedDateTime.withZoneSameInstant(requireZoneId(zoneId)));
    }

    public DatetimeHelper plusDays(long days) {
        return new DatetimeHelper(zonedDateTime.plusDays(days));
    }

    public DatetimeHelper plusHours(long hours) {
        return new DatetimeHelper(zonedDateTime.plusHours(hours));
    }

    public DatetimeHelper plusMinutes(long minutes) {
        return new DatetimeHelper(zonedDateTime.plusMinutes(minutes));
    }

    public DatetimeHelper minusDays(long days) {
        return new DatetimeHelper(zonedDateTime.minusDays(days));
    }

    public DatetimeHelper minusHours(long hours) {
        return new DatetimeHelper(zonedDateTime.minusHours(hours));
    }

    public DatetimeHelper minusMinutes(long minutes) {
        return new DatetimeHelper(zonedDateTime.minusMinutes(minutes));
    }

    public DatetimeHelper startOfDay() {
        return new DatetimeHelper(zonedDateTime.toLocalDate().atStartOfDay(zonedDateTime.getZone()));
    }

    public DatetimeHelper endOfDay() {
        return new DatetimeHelper(zonedDateTime.toLocalDate().plusDays(1)
                .atStartOfDay(zonedDateTime.getZone()).minusNanos(1));
    }

    public String format(DateTimeFormatter formatter) {
        return zonedDateTime.format(Objects.requireNonNull(formatter, "formatter must not be null"));
    }

    /**
     * 转为字符串
     *
     * @return {@link String}
     */
    @Override
    public String toString() {
        return toDateTimeString();
    }

    public String toDateTimeString() {
        return format(FORMATTER_DATE_TIME_);
    }

    public String toDateHourMinuteString() {
        return format(FORMATTER_DATE_HOUR_MINUTE);
    }

    public String toDateString() {
        return format(FORMATTER_DATE);
    }

    public String toTimeString() {
        return format(FORMATTER_TIME);
    }

    public String toHourMinuteString() {
        return format(FORMATTER_HOUR_MINUTE);
    }

    private static ZoneId requireZoneId(ZoneId zoneId) {
        return Objects.requireNonNull(zoneId, "zoneId must not be null");
    }
}
