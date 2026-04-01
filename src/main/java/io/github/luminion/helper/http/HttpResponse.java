package io.github.luminion.helper.http;

import io.github.luminion.helper.json.JacksonHelper;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * HTTP 响应。
 *
 * @author luminion
 */
public class HttpResponse {
    private final int statusCode;
    private final String statusMessage;
    private final Map<String, List<String>> headers;
    private final byte[] body;
    private final Charset charset;

    HttpResponse(int statusCode, String statusMessage, Map<String, List<String>> headers, byte[] body, Charset charset) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.headers = headers == null ? Collections.<String, List<String>>emptyMap() : headers;
        this.body = body == null ? new byte[0] : Arrays.copyOf(body, body.length);
        this.charset = charset == null ? StandardCharsets.UTF_8 : charset;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public Charset getCharset() {
        return charset;
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    public String header(String name) {
        List<String> values = headers(name);
        return values.isEmpty() ? null : values.get(0);
    }

    public List<String> headers(String name) {
        if (name == null || name.trim().isEmpty() || headers.isEmpty()) {
            return Collections.emptyList();
        }
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String key = entry.getKey();
            if (key != null && key.equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return Collections.emptyList();
    }

    public byte[] bodyBytes() {
        return Arrays.copyOf(body, body.length);
    }

    public String body() {
        return new String(body, charset);
    }

    public String body(Charset charset) {
        return new String(body, charset == null ? this.charset : charset);
    }

    public Map<String, Object> bodyMap() {
        return JacksonHelper.parseObject(body());
    }

    public <T> T bodyBean(Class<T> clazz) {
        return JacksonHelper.parseObject(body(), clazz);
    }
}
