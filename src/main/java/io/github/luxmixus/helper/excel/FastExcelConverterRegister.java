package io.github.luxmixus.helper.excel;

import cn.idev.excel.converters.Converter;
import cn.idev.excel.converters.DefaultConverterLoader;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 为Excel的转换器进行注册
 * 
 * @author luxmixus
 */
@Slf4j
public abstract class FastExcelConverterRegister {
    private static final Class<?> CONVERTER_CLASS = Converter.class;
    private static final Class<?> CONVERTER_LOADER_CLASS = DefaultConverterLoader.class;

    public static void registerExtraConverters() {
        registerExtraConverters("yyyy-MM-dd", "HH:mm:ss", "yyyy-MM-dd HH:mm:ss");
    }

    public static void registerExtraConverters(String dateFormat, String timeFormat, String dateTimeFormat) {
        List<Converter<?>> converters = new ArrayList<>();

        converters.add(new BooleanConverter());
        converters.add(new LongConverter());
        
        converters.add(new FloatConverter());
        converters.add(new DoubleConverter());

        converters.add(new BigIntergerConverter());
        converters.add(new BigDecimalConverter());

        converters.add(new DateConverter(dateTimeFormat, "GMT+8"));

        converters.add(new SqlTimestampConverter(dateTimeFormat));
        converters.add(new SqlDateConverter(dateFormat));
        converters.add(new SqlTimeConverter(timeFormat));

        converters.add(new LocalDateTimeConverter(dateTimeFormat));
        converters.add(new LocalDateConverter(dateFormat));
        converters.add(new LocalTimeConverter(timeFormat));

        registerConverters(converters);
    }


    @SneakyThrows
    public static void registerConverters(List<Converter<?>> converters) {
        Method putWriteConverter = CONVERTER_LOADER_CLASS.getDeclaredMethod("putWriteConverter", CONVERTER_CLASS);
        putWriteConverter.setAccessible(true);
        Method putAllConverter = CONVERTER_LOADER_CLASS.getDeclaredMethod("putAllConverter", CONVERTER_CLASS);
        putAllConverter.setAccessible(true);
        try {
            for (Converter<?> converter : converters) {
                putWriteConverter.invoke(null, converter);
                putAllConverter.invoke(null, converter);
            }
        } catch (IllegalAccessException e) {
            log.warn("IllegalAccessException", e);
        } catch (InvocationTargetException e) {
            log.warn("InvocationTargetException", e);
        }
    }


    public static class BooleanConverter implements Converter<Boolean> {

        @Override
        public Class<Boolean> supportJavaTypeKey() {
            return Boolean.class;
        }

        @Override
        public CellDataTypeEnum supportExcelTypeKey() {
            return CellDataTypeEnum.STRING;
        }

        @Override
        public Boolean convertToJavaData(ReadCellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            String cellValue = cellData.getStringValue();
            if (cellValue == null || cellValue.isEmpty()) {
                return null;
            }
            return "是".equals(cellValue) || "TRUE".equalsIgnoreCase(cellValue);
        }

        @Override
        public WriteCellData<String> convertToExcelData(Boolean value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            if (value == null) {
                return new WriteCellData<>("");
            }
            return new WriteCellData<>(value ? "是" : "否");
        }
    }

    public static class LongConverter implements Converter<Long> {

        @Override
        public Class<Long> supportJavaTypeKey() {
            return Long.class;
        }

        @Override
        public CellDataTypeEnum supportExcelTypeKey() {
            return CellDataTypeEnum.STRING;
        }

        @Override
        public Long convertToJavaData(ReadCellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            String cellValue = cellData.getStringValue();
            if (cellValue == null || cellValue.isEmpty()) {
                return null;
            }
            return Long.parseLong(cellValue);
        }

        @Override
        public WriteCellData<String> convertToExcelData(Long value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            if (value == null) {
                return new WriteCellData<>("");
            }
            return new WriteCellData<>(String.valueOf(value));
        }
    }


    public static class FloatConverter implements Converter<Float> {

        @Override
        public Class<Float> supportJavaTypeKey() {
            return Float.class;
        }

        @Override
        public CellDataTypeEnum supportExcelTypeKey() {
            return CellDataTypeEnum.STRING;
        }

        @Override
        public Float convertToJavaData(ReadCellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            String cellValue = cellData.getStringValue();
            if (cellValue == null || cellValue.isEmpty()) {
                return null;
            }
            return Float.parseFloat(cellValue);
        }

        @Override
        public WriteCellData<String> convertToExcelData(Float value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            if (value == null) {
                return new WriteCellData<>("");
            }
            return new WriteCellData<>(new BigDecimal(Float.toString(value)).toPlainString());
        }
    }

    public static class DoubleConverter implements Converter<Double> {

        @Override
        public Class<Double> supportJavaTypeKey() {
            return Double.class;
        }

        @Override
        public CellDataTypeEnum supportExcelTypeKey() {
            return CellDataTypeEnum.STRING;
        }

        @Override
        public Double convertToJavaData(ReadCellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            String cellValue = cellData.getStringValue();
            if (cellValue == null || cellValue.isEmpty()) {
                return null;
            }
            return Double.parseDouble(cellValue);
        }

        @Override
        public WriteCellData<String> convertToExcelData(Double value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            if (value == null) {
                return new WriteCellData<>("");
            }
            return new WriteCellData<>(new BigDecimal(Double.toString(value)).toPlainString());
        }
    }


    public static class BigIntergerConverter implements Converter<BigInteger> {

        @Override
        public Class<BigInteger> supportJavaTypeKey() {
            return BigInteger.class;
        }

        @Override
        public CellDataTypeEnum supportExcelTypeKey() {
            return CellDataTypeEnum.STRING;
        }

        @Override
        public BigInteger convertToJavaData(ReadCellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            String cellValue = cellData.getStringValue();
            if (cellValue == null || cellValue.isEmpty()) {
                return null;
            }
            return new BigInteger(cellValue);
        }

        @Override
        public WriteCellData<String> convertToExcelData(BigInteger value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            if (value == null) {
                return new WriteCellData<>("");
            }
            return new WriteCellData<>(String.valueOf(value));
        }
    }

    public static class BigDecimalConverter implements Converter<BigDecimal> {

        @Override
        public Class<BigDecimal> supportJavaTypeKey() {
            return BigDecimal.class;
        }

        @Override
        public CellDataTypeEnum supportExcelTypeKey() {
            return CellDataTypeEnum.STRING;
        }

        @Override
        public BigDecimal convertToJavaData(ReadCellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            String cellValue = cellData.getStringValue();
            if (cellValue == null || cellValue.isEmpty()) {
                return null;
            }
            return new BigDecimal(cellValue);
        }

        @Override
        public WriteCellData<String> convertToExcelData(BigDecimal value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            if (value == null) {
                return new WriteCellData<>("");
            }
            return new WriteCellData<>(value.toPlainString());
        }
    }


    public static class DateConverter implements Converter<Date> {
        private final DateTimeFormatter formatter;
        private final ZoneId zoneId;

        public DateConverter(String pattern, String zoneId) {
            this.formatter = DateTimeFormatter.ofPattern(pattern);
            this.zoneId = ZoneId.of(zoneId);
        }

        @Override
        public Class<Date> supportJavaTypeKey() {
            return Date.class;
        }

        @Override
        public CellDataTypeEnum supportExcelTypeKey() {
            return CellDataTypeEnum.STRING;
        }

        @Override
        public Date convertToJavaData(ReadCellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            String cellValue = cellData.getStringValue();
            if (cellValue == null || cellValue.isEmpty()) {
                return null;
            }
            return Date.from(LocalDateTime.parse(cellValue, formatter).atZone(zoneId).toInstant());
        }

        @Override
        public WriteCellData<String> convertToExcelData(Date value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            if (value == null) {
                return new WriteCellData<>("");
            }
            return new WriteCellData<>(value.toInstant().atZone(zoneId).toLocalDateTime().format(formatter));
        }
    }


    public static class LocalDateTimeConverter implements Converter<LocalDateTime> {
        private final DateTimeFormatter formatter;

        public LocalDateTimeConverter(String pattern) {
            formatter = DateTimeFormatter.ofPattern(pattern);
        }

        @Override
        public Class<LocalDateTime> supportJavaTypeKey() {
            return LocalDateTime.class;
        }

        @Override
        public CellDataTypeEnum supportExcelTypeKey() {
            return CellDataTypeEnum.STRING;
        }

        @Override
        public LocalDateTime convertToJavaData(ReadCellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            String cellValue = cellData.getStringValue();
            if (cellValue == null || cellValue.isEmpty()) {
                return null;
            }
            return LocalDateTime.parse(cellValue, formatter);
        }

        @Override
        public WriteCellData<String> convertToExcelData(LocalDateTime value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            if (value == null) {
                return new WriteCellData<>("");
            }
            return new WriteCellData<>(value.format(formatter));
        }

    }


    public static class LocalDateConverter implements Converter<LocalDate> {
        private final DateTimeFormatter formatter;

        public LocalDateConverter(String pattern) {
            formatter = DateTimeFormatter.ofPattern(pattern);
        }

        @Override
        public Class<LocalDate> supportJavaTypeKey() {
            return LocalDate.class;
        }

        @Override
        public CellDataTypeEnum supportExcelTypeKey() {
            return CellDataTypeEnum.STRING;
        }

        @Override
        public LocalDate convertToJavaData(ReadCellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            String cellValue = cellData.getStringValue();
            if (cellValue == null || cellValue.isEmpty()) {
                return null;
            }
            return LocalDate.parse(cellValue, formatter);
        }

        @Override
        public WriteCellData<String> convertToExcelData(LocalDate value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            if (value == null) {
                return new WriteCellData<>("");
            }
            return new WriteCellData<>(value.format(formatter));
        }
    }


    public static class LocalTimeConverter implements Converter<LocalTime> {
        private final DateTimeFormatter formatter;

        public LocalTimeConverter(String pattern) {
            formatter = DateTimeFormatter.ofPattern(pattern);
        }

        @Override
        public Class<LocalTime> supportJavaTypeKey() {
            return LocalTime.class;
        }

        @Override
        public CellDataTypeEnum supportExcelTypeKey() {
            return CellDataTypeEnum.STRING;
        }

        @Override
        public LocalTime convertToJavaData(ReadCellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            String cellValue = cellData.getStringValue();
            if (cellValue == null || cellValue.isEmpty()) {
                return null;
            }
            return LocalTime.parse(cellValue, formatter);
        }

        @Override
        public WriteCellData<String> convertToExcelData(LocalTime value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            if (value == null) {
                return new WriteCellData<>("");
            }
            return new WriteCellData<>(value.format(formatter));
        }
    }


    public static class SqlTimestampConverter implements Converter<Timestamp> {
        private final DateTimeFormatter formatter;

        public SqlTimestampConverter(String pattern) {
            formatter = DateTimeFormatter.ofPattern(pattern);
        }

        @Override
        public Class<Timestamp> supportJavaTypeKey() {
            return Timestamp.class;
        }

        @Override
        public CellDataTypeEnum supportExcelTypeKey() {
            return CellDataTypeEnum.STRING;
        }

        @Override
        public Timestamp convertToJavaData(ReadCellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            String cellValue = cellData.getStringValue();
            if (cellValue == null || cellValue.isEmpty()) {
                return null;
            }
            LocalDateTime ldt = LocalDateTime.parse(cellValue, formatter);
            return Timestamp.valueOf(ldt);
        }

        @Override
        public WriteCellData<String> convertToExcelData(Timestamp value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            if (value == null) {
                return new WriteCellData<>("");
            }
            return new WriteCellData<>(value.toLocalDateTime().format(formatter));
        }
    }


    public static class SqlDateConverter implements Converter<java.sql.Date> {
        private final DateTimeFormatter formatter;

        public SqlDateConverter(String pattern) {
            formatter = DateTimeFormatter.ofPattern(pattern);
        }

        @Override
        public Class<java.sql.Date> supportJavaTypeKey() {
            return java.sql.Date.class;
        }

        @Override
        public CellDataTypeEnum supportExcelTypeKey() {
            return CellDataTypeEnum.STRING;
        }

        @Override
        public java.sql.Date convertToJavaData(ReadCellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            String cellValue = cellData.getStringValue();
            if (cellValue == null || cellValue.isEmpty()) {
                return null;
            }
            return java.sql.Date.valueOf(LocalDate.parse(cellValue, formatter));
        }

        @Override
        public WriteCellData<String> convertToExcelData(java.sql.Date value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            if (value == null) {
                return new WriteCellData<>("");
            }
            return new WriteCellData<>(value.toLocalDate().format(formatter));
        }
    }

    public static class SqlTimeConverter implements Converter<Time> {
        private final DateTimeFormatter formatter;

        public SqlTimeConverter(String pattern) {
            formatter = DateTimeFormatter.ofPattern(pattern);
        }

        @Override
        public Class<Time> supportJavaTypeKey() {
            return Time.class;
        }

        @Override
        public CellDataTypeEnum supportExcelTypeKey() {
            return CellDataTypeEnum.STRING;
        }

        @Override
        public Time convertToJavaData(ReadCellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            String cellValue = cellData.getStringValue();
            if (cellValue == null || cellValue.isEmpty()) {
                return null;
            }
            return Time.valueOf(LocalTime.parse(cellValue, formatter));
        }

        @Override
        public WriteCellData<String> convertToExcelData(Time value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
            if (value == null) {
                return new WriteCellData<>("");
            }
            return new WriteCellData<>(value.toLocalTime().format(formatter));
        }
    }


}