package io.github.luminion.helper.datetime;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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
        return new DatetimeHelper(ZonedDateTime.now());
    }

    /**
     * 根据 {@link LocalDateTime} 创建
     *
     * @param localDateTime {@link LocalDateTime}
     * @return {@link DatetimeHelper}
     */
    public static DatetimeHelper of(LocalDateTime localDateTime) {
        return new DatetimeHelper(localDateTime.atZone(ZoneId.systemDefault()));
    }

    /**
     * 根据 {@link LocalDate} 创建
     *
     * @param localDate {@link LocalDate}
     * @return {@link DatetimeHelper}
     */
    public static DatetimeHelper of(LocalDate localDate) {
        return new DatetimeHelper(localDate.atStartOfDay(ZoneId.systemDefault()));
    }

    /**
     * 根据 {@link LocalTime} 创建
     *
     * @param localTime {@link LocalTime}
     * @return {@link DatetimeHelper}
     */
    public static DatetimeHelper of(LocalTime localTime) {
        return new DatetimeHelper(localTime.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()));
    }

    /**
     * 根据 {@link Date} 创建
     *
     * @param date {@link Date}
     * @return {@link DatetimeHelper}
     */
    public static DatetimeHelper of(Date date) {
        return new DatetimeHelper(ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
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
        return new DatetimeHelper(ZonedDateTime.ofInstant(new Date(epochMilli).toInstant(), ZoneId.systemDefault()));
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

    /**
     * 转为字符串
     *
     * @return {@link String}
     */
    @Override
    public String toString() {
        return toLocalDateTime().format(FORMATTER_DATE_TIME_);
    }
    
    public String toDateTimeString() {
        return toLocalDateTime().format(FORMATTER_DATE_TIME_);
    }

    public String toTimeString() {
        return toLocalTime().toString();
    }

    public String toDateString() {
        return toLocalDate().toString();
    }
}
