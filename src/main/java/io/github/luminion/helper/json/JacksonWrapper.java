package io.github.luminion.helper.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Jackson 包装器。
 *
 * @author luminion
 */
public class JacksonWrapper {
    private final ObjectMapper objectMapper;

    protected JacksonWrapper(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    public static JacksonWrapper of(ObjectMapper objectMapper) {
        return new JacksonWrapper(objectMapper);
    }

    public ObjectMapper unwrap() {
        return objectMapper;
    }

    @SneakyThrows
    public String toJson(Object obj) {
        return objectMapper.writeValueAsString(obj);
    }

    @SneakyThrows
    public String toPrettyJson(Object obj) {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

    @SneakyThrows
    public <T> T parseObject(String json, Class<T> clazz) {
        return objectMapper.readValue(json, clazz);
    }

    @SneakyThrows
    public <T> T parseObject(String json, TypeReference<T> typeReference) {
        return objectMapper.readValue(json, typeReference);
    }

    @SneakyThrows
    public Map<String, Object> parseObject(String json) {
        return parseObject(json, new TypeReference<Map<String, Object>>() {});
    }

    @SneakyThrows
    public List<Object> parseArray(String json) {
        return parseObject(json, new TypeReference<List<Object>>() {});
    }

    @SneakyThrows
    public <T> List<T> parseArray(String json, Class<T> clazz) {
        return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }

    public <T> T convertValue(Object source, Class<T> clazz) {
        return objectMapper.convertValue(source, clazz);
    }

    public <T> T convertValue(Object source, TypeReference<T> typeReference) {
        return objectMapper.convertValue(source, typeReference);
    }

    @SneakyThrows
    public <T> T updateValue(T target, Object source) {
        return objectMapper.updateValue(Objects.requireNonNull(target, "target must not be null"), source);
    }
}
