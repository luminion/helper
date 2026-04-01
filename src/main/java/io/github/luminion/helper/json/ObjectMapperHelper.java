package io.github.luminion.helper.json;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 兼容旧版命名，建议改用 {@link JacksonHelper} 和 {@link JacksonWrapper}。
 *
 * @author luminion
 * @deprecated 请使用 {@link JacksonHelper}
 */
@Deprecated
public class ObjectMapperHelper extends JacksonWrapper {
    private ObjectMapperHelper(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    public static void objectMapper(ObjectMapper objectMapper) {
        JacksonHelper.objectMapper(objectMapper);
    }

    public static ObjectMapper objectMapper() {
        return JacksonHelper.objectMapper();
    }

    public static ObjectMapperHelper of(ObjectMapper objectMapper) {
        return new ObjectMapperHelper(objectMapper);
    }

    public static ObjectMapperHelper of() {
        return new ObjectMapperHelper(JacksonHelper.objectMapper());
    }

    public ObjectMapper getObjectMapper() {
        return unwrap();
    }
}
