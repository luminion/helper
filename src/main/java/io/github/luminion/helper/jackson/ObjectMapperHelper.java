package io.github.luminion.helper.jackson;

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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

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
import java.util.TimeZone;

/**
 * @author luminion
 */
@RequiredArgsConstructor
public class ObjectMapperHelper {
    private static ObjectMapperHelper instance;
    @Getter
    private final ObjectMapper objectMapper;


    static {
        ObjectMapper objectMapper = new ObjectMapper();
        // 1. 基本特性配置
        // 序列化时，对象为 null，不抛异常
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 反序列化时，json 中包含 pojo 不存在属性时，不抛异常
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 禁止将 java.util.Date、Calendar 序列化为数字(时间戳)
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // 2. 配置 java.util.Date 的格式和时区
        String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
        String dateFormat = "yyyy-MM-dd";
        String timeFormat = "HH:mm:ss";
        TimeZone timeZone = TimeZone.getTimeZone(ZoneId.systemDefault());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateTimeFormat);
        simpleDateFormat.setTimeZone(timeZone);
        objectMapper.setDateFormat(simpleDateFormat);
        objectMapper.setTimeZone(timeZone);

        // 3. 创建一个模块来注册自定义的序列化和反序列化器
        SimpleModule customModule = new SimpleModule();
        // 4. 配置数字类型的序列化（解决前端精度丢失问题）
        customModule.addSerializer(Long.class, ToStringSerializer.instance);
        customModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        customModule.addSerializer(Double.class, ToStringSerializer.instance);
        customModule.addSerializer(Double.TYPE, ToStringSerializer.instance);
        customModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
        customModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);

        // 5. 配置 java.time.* 类型的序列化和反序列化
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormat);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(dateFormat);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(timeFormat);

        // 添加反序列化器
        customModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        customModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
        customModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFormatter));

        // 添加序列化器
        customModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        customModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        customModule.addSerializer(LocalTime.class, new LocalTimeSerializer(timeFormatter));

        // 6. 注册模块
        objectMapper.registerModule(customModule);

        instance = ObjectMapperHelper.of(objectMapper);
    }

    /**
     * 允许自定义或替换全局的 ObjectMapper 实例。
     * <p>
     * 这在与 Spring 等框架集成时非常有用。
     *
     * @param objectMapper 要使用的 ObjectMapper 实例
     */
    public static void objectMapper(ObjectMapper objectMapper) {
        instance = ObjectMapperHelper.of(objectMapper);
    }

    /**
     * 获取全局使用的 ObjectMapper 实例。
     *
     * @return 全局使用的 ObjectMapper 实例
     */
    public static ObjectMapper objectMapper() {
        return instance.getObjectMapper();
    }
    
    /**
     * 创建一个 ObjectMapperHelper 实例。
     *
     * @param objectMapper 用于解析 JSON 的 ObjectMapper 实例
     * @return 一个 ObjectMapperHelper 实例
     */
    public static ObjectMapperHelper of(ObjectMapper objectMapper){
        return new ObjectMapperHelper(objectMapper);
    }

    public static ObjectMapperHelper of(){
        return instance;
    }

    /**
     * 将 Java 对象序列化为 JSON 字符串。
     *
     * @param obj 要序列化的对象
     * @return JSON 字符串
     */
    @SneakyThrows
    public String toJson(Object obj) {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * 将 Java 对象序列化为格式化（美化）的 JSON 字符串。
     *
     * @param obj 要序列化的对象
     * @return 格式化的 JSON 字符串
     */
    @SneakyThrows
    public String toPrettyJson(Object obj) {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

    /**
     * 将 JSON 字符串解析为指定类型的对象。
     *
     * @param json  JSON 字符串
     * @param clazz 目标对象的 Class
     * @param <T>   目标对象的类型
     * @return 目标类的实例
     */
    @SneakyThrows
    public <T> T parseObject(String json, Class<T> clazz) {
        return objectMapper.readValue(json, clazz);
    }

    /**
     * 将 JSON 字符串解析为复杂的泛型类型（例如，List<String> 或 Map<String, User>）。
     *
     * @param json          JSON 字符串
     * @param typeReference 描述目标泛型类型的 TypeReference
     * @param <T>           目标泛型类型
     * @return 目标类型的实例
     */
    @SneakyThrows
    public <T> T parseObject(String json, TypeReference<T> typeReference) {
        return objectMapper.readValue(json, typeReference);
    }

    /**
     * 将 JSON 对象字符串解析为 Map。
     *
     * @param json JSON 对象字符串
     * @return 代表该 JSON 对象的 Map
     */
    @SneakyThrows
    public Map<String, Object> parseObject(String json) {
        return parseObject(json, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * 将 JSON 数组字符串解析为对象的列表 (List<Object>)。
     *
     * @param json JSON 数组字符串
     * @return 对象的列表
     */
    @SneakyThrows
    public List<Object> parseArray(String json) {
        return parseObject(json, new TypeReference<List<Object>>() {});
    }

    /**
     * 将 JSON 数组字符串解析为指定类型的对象列表。
     *
     * @param json  JSON 数组字符串
     * @param clazz 列表中元素的 Class
     * @param <T>   列表中元素的类型
     * @return 指定类型的对象列表
     */
    @SneakyThrows
    public <T> List<T> parseArray(String json, Class<T> clazz) {
        return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }
    
    
}
