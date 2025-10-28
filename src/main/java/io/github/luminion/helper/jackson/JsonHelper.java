package io.github.luminion.helper.jackson;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Map;

/**
 * @author luminion
 */
@RequiredArgsConstructor
public class JsonHelper {
    @Getter
    private final ObjectMapper objectMapper;
    
    /**
     * 创建一个 JsonHelper 实例。
     *
     * @param objectMapper 用于解析 JSON 的 ObjectMapper 实例
     * @return 一个 JsonHelper 实例
     */
    public static JsonHelper of(ObjectMapper objectMapper){
        return new JsonHelper(objectMapper);
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
