package io.github.luminion.helper.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Jackson 静态入口。
 * <p>
 * 提供全局默认 {@link ObjectMapper}，并暴露常用 JSON 序列化/反序列化快捷方法。
 *
 * @author luminion
 */
public abstract class JacksonHelper {
    private static volatile JacksonWrapper instance = wrapper(createDefaultObjectMapper());

    private JacksonHelper() {}

    /**
     * 替换全局默认的 {@link ObjectMapper}。
     * 适合在 Spring 或自定义配置场景下接管默认行为。
     */
    public static void objectMapper(ObjectMapper objectMapper) {
        instance = wrapper(Objects.requireNonNull(objectMapper, "objectMapper must not be null"));
    }

    /**
     * 获取当前全局默认的 {@link ObjectMapper}。
     */
    public static ObjectMapper objectMapper() {
        return instance.unwrap();
    }

    /**
     * 获取当前全局默认包装器。
     */
    public static JacksonWrapper wrapper() {
        return instance;
    }

    /**
     * 基于指定 {@link ObjectMapper} 创建独立包装器，不影响全局默认实例。
     */
    public static JacksonWrapper wrapper(ObjectMapper objectMapper) {
        return JacksonWrapper.of(Objects.requireNonNull(objectMapper, "objectMapper must not be null"));
    }

    public static String toJson(Object obj) {
        return wrapper().toJson(obj);
    }

    public static String toPrettyJson(Object obj) {
        return wrapper().toPrettyJson(obj);
    }

    public static <T> T parseObject(String json, Class<T> clazz) {
        return wrapper().parseObject(json, clazz);
    }

    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        return wrapper().parseObject(json, typeReference);
    }

    public static Map<String, Object> parseObject(String json) {
        return wrapper().parseObject(json);
    }

    public static List<Object> parseArray(String json) {
        return wrapper().parseArray(json);
    }

    public static <T> List<T> parseArray(String json, Class<T> clazz) {
        return wrapper().parseArray(json, clazz);
    }

    public static <T> T convertValue(Object source, Class<T> clazz) {
        return wrapper().convertValue(source, clazz);
    }

    public static <T> T convertValue(Object source, TypeReference<T> typeReference) {
        return wrapper().convertValue(source, typeReference);
    }

    public static <T> T updateValue(T target, Object source) {
        return wrapper().updateValue(target, source);
    }

    private static ObjectMapper createDefaultObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
        String dateFormat = "yyyy-MM-dd";
        String timeFormat = "HH:mm:ss";
        TimeZone timeZone = TimeZone.getTimeZone(ZoneId.systemDefault());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateTimeFormat);
        simpleDateFormat.setTimeZone(timeZone);
        objectMapper.setDateFormat(simpleDateFormat);
        objectMapper.setTimeZone(timeZone);

        SimpleModule customModule = new SimpleModule();
        customModule.addSerializer(Long.class, ToStringSerializer.instance);
        customModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        customModule.addSerializer(Double.class, ToStringSerializer.instance);
        customModule.addSerializer(Double.TYPE, ToStringSerializer.instance);
        customModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
        customModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormat);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(dateFormat);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(timeFormat);

        customModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        customModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
        customModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFormatter));
        customModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        customModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        customModule.addSerializer(LocalTime.class, new LocalTimeSerializer(timeFormatter));
        objectMapper.registerModule(customModule);
        return objectMapper;
    }
}
