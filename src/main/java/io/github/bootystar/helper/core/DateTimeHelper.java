package io.github.bootystar.helper.core;

import lombok.RequiredArgsConstructor;

import java.time.*;
import java.util.Date;

/**
 * 时间日期助手
 *
 * @author bootystar
 */
@RequiredArgsConstructor
public class DateTimeHelper {
    public static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final ZonedDateTime zonedDateTime;

    /**
     * 获取当前时间
     * @return {@link DateTimeHelper}
     */
    public static DateTimeHelper now() {
        return new DateTimeHelper(ZonedDateTime.now());
    }

    /**
     * 根据 {@link LocalDateTime} 创建
     * @param localDateTime {@link LocalDateTime}
     * @return {@link DateTimeHelper}
     */
    public static DateTimeHelper of(LocalDateTime localDateTime) {
        return new DateTimeHelper(localDateTime.atZone(ZoneId.systemDefault()));
    }

    /**
     * 根据 {@link LocalDate} 创建
     * @param localDate {@link LocalDate}
     * @return {@link DateTimeHelper}
     */
    public static DateTimeHelper of(LocalDate localDate) {
        return new DateTimeHelper(localDate.atStartOfDay(ZoneId.systemDefault()));
    }

    /**
     * 根据 {@link LocalTime} 创建
     * @param localTime {@link LocalTime}
     * @return {@link DateTimeHelper}
     */
    public static DateTimeHelper of(LocalTime localTime) {
        return new DateTimeHelper(localTime.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()));
    }

    /**
     * 根据 {@link Date} 创建
     * @param date {@link Date}
     * @return {@link DateTimeHelper}
     */
    public static DateTimeHelper of(Date date) {
        return new DateTimeHelper(ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
    }

    /**
     * 根据纪元秒创建
     * @param epochSecond 从 1970-01-01T00:00:00Z 开始的秒数
     * @return {@link DateTimeHelper}
     */
    public static DateTimeHelper ofEpochSecond(long epochSecond) {
        return new DateTimeHelper(ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), ZoneId.systemDefault()));
    }

    /**
     * 根据纪元毫秒创建
     * @param epochMilli 从 1970-01-01T00:00:00Z 开始的毫秒数
     * @return {@link DateTimeHelper}
     */
    public static DateTimeHelper ofEpochMilli(long epochMilli) {
        return new DateTimeHelper(ZonedDateTime.ofInstant(new Date(epochMilli).toInstant(), ZoneId.systemDefault()));
    }

    /**
     * 根据纪元日创建
     * @param epochDay 从 1970-01-01 开始的天数
     * @return {@link DateTimeHelper}
     */
    public static DateTimeHelper ofEpochDay(long epochDay) {
        return new DateTimeHelper(LocalDate.ofEpochDay(epochDay).atStartOfDay(ZoneId.systemDefault()));
    }

    /**
     * 转为 {@link LocalDateTime}
     * @return {@link LocalDateTime}
     */
    public LocalDateTime toLocalDateTime() {
        return zonedDateTime.toLocalDateTime();
    }

    /**
     * 转为 {@link LocalDate}
     * @return {@link LocalDate}
     */
    public LocalDate toLocalDate() {
        return zonedDateTime.toLocalDate();
    }

    /**
     * 转为 {@link LocalTime}
     * @return {@link LocalTime}
     */
    public LocalTime toLocalTime() {
        return zonedDateTime.toLocalTime();
    }

    /**
     * 转为纪元秒
     * @return 从 1970-01-01T00:00:00Z 开始的秒数
     */
    public long toEpochSecond() {
        return zonedDateTime.toInstant().getEpochSecond();
    }

    /**
     * 转为纪元毫秒
     * @return 从 1970-01-01T00:00:00Z 开始的毫秒数
     */
    public long toEpochMilli() {
        return zonedDateTime.toInstant().toEpochMilli();
    }

    /**
     * 转为字符串
     * @return {@link String}
     */
    @Override
    public String toString() {
        return toLocalDateTime().toString();
    }
}
