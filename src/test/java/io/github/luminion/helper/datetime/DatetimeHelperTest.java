package io.github.luminion.helper.datetime;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatetimeHelperTest {

    @Test
    void shouldParseFormatAndConvertZone() {
        DatetimeHelper helper = DatetimeHelper.parseDateTime("2026-04-08 12:34:56");

        assertEquals("2026-04-08 12:34:56", helper.toDateTimeString());
        assertEquals(LocalDate.of(2026, 4, 8), helper.toLocalDate());
        assertEquals(
                LocalDateTime.of(2026, 4, 8, 4, 34, 56),
                helper.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()
        );
    }

    @Test
    void shouldReturnStartAndEndOfDay() {
        DatetimeHelper helper = DatetimeHelper.of(LocalDateTime.of(2026, 4, 8, 12, 34, 56));

        assertEquals("2026-04-08 00:00:00", helper.startOfDay().toDateTimeString());
        assertEquals("2026-04-08 23:59:59", helper.endOfDay().toDateTimeString());
    }
}
