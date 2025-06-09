package io.github.bootystar.helper.base.date;

import java.time.*;
import java.util.Date;

/**
 * 时间日期助手
 *
 * @author bootystar
 */
public class DateTimeHelper {
    private final ZonedDateTime zonedDateTime;
    
    public static DateTimeHelper of() {
        return new DateTimeHelper();
    }

    public static DateTimeHelper of(LocalDateTime localDateTime) {
        return new DateTimeHelper(localDateTime.atZone(ZoneId.systemDefault()));
    }

    public static DateTimeHelper of(LocalDate localDate) {
        return new DateTimeHelper(localDate.atStartOfDay(ZoneId.systemDefault()));
    }

    public static DateTimeHelper of(LocalTime localTime) {
        return new DateTimeHelper(localTime.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()));
    }

    public static DateTimeHelper of(Date date) {
        return new DateTimeHelper(ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
    }

    public static DateTimeHelper ofEpochSecond(long epochSecond) {
        return new DateTimeHelper(ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), ZoneId.systemDefault()));
    }

    public static DateTimeHelper ofEpochMilli(long epochMilli) {
        return new DateTimeHelper(ZonedDateTime.ofInstant(new Date(epochMilli).toInstant(), ZoneId.systemDefault()));
    }

    public DateTimeHelper() {
        this.zonedDateTime = ZonedDateTime.now();
    }

    public DateTimeHelper(ZonedDateTime zonedDateTime) {
        this.zonedDateTime = zonedDateTime;
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

    public long toEpochSecond() {
        return zonedDateTime.toInstant().getEpochSecond();
    }

    public long toEpochMilli() {
        return zonedDateTime.toInstant().toEpochMilli();
    }

    @Override
    public String toString() {
        return toLocalDateTime().toString();
    }
}
