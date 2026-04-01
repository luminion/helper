package io.github.luminion.helper.http;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HTTP 请求对象。
 * <p>
 * 负责管理一次请求的配置、发送与响应读取。
 *
 * @author luminion
 */
@Slf4j
public class HttpRequest {
    private final String url;
    private final String method;
    private Charset charset = StandardCharsets.UTF_8;
    private int connectTimeout = 3000;
    private int readTimeout = 10000;
    private boolean executed;
    private final Map<String, String> header = new LinkedHashMap<String, String>();
    private final Map<String, String> queryParams = new LinkedHashMap<String, String>();
    private final Map<String, String> formParams = new LinkedHashMap<String, String>();
    private String body;
    private int responseCode = -1;

    HttpRequest(String url, String method) {
        this.url = url;
        this.method = method;
    }

    public int getResponseCode() {
        if (responseCode == -1) {
            throw new IllegalStateException("connection not open yet");
        }
        return responseCode;
    }

    public HttpRequest connectTimeout(int timeout) {
        this.connectTimeout = timeout;
        return this;
    }

    public HttpRequest readTimeout(int timeout) {
        this.readTimeout = timeout;
        return this;
    }

    public HttpRequest charset(Charset charset) {
        this.charset = charset == null ? StandardCharsets.UTF_8 : charset;
        return this;
    }

    public HttpRequest header(String key, String value) {
        if (key != null && value != null) {
            header.put(key, value);
        }
        return this;
    }

    public HttpRequest header(Map<String, String> params) {
        if (params != null && !params.isEmpty()) {
            header.putAll(params);
        }
        return this;
    }

    public HttpRequest queryParam(String key, String value) {
        if (key != null && value != null) {
            queryParams.put(key, value);
        }
        return this;
    }

    public HttpRequest queryParam(Map<String, String> params) {
        if (params != null && !params.isEmpty()) {
            queryParams.putAll(params);
        }
        return this;
    }

    public HttpRequest formParam(String key, String value) {
        if (body != null) {
            throw new IllegalStateException("form and request body cannot be set at the same time");
        }
        if (key != null && value != null) {
            formParams.put(key, value);
        }
        return this;
    }

    public HttpRequest formParam(Map<String, String> params) {
        if (body != null) {
            throw new IllegalStateException("form and request body cannot be set at the same time");
        }
        if (params != null && !params.isEmpty()) {
            formParams.putAll(params);
        }
        return this;
    }

    public HttpRequest bodyParam(String json) {
        if (!formParams.isEmpty()) {
            throw new IllegalStateException("form and request body cannot be set at the same time");
        }
        this.body = json;
        return this;
    }

    public HttpRequest body(String body) {
        return bodyParam(body);
    }

    /**
     * 执行请求并返回完整响应对象。
     */
    @SneakyThrows
    public HttpResponse response() {
        HttpURLConnection connection = execute();
        InputStream stream = responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
        if (stream == null) {
            stream = new ByteArrayInputStream(new byte[0]);
        }
        try (InputStream actualStream = stream; ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // 统一把响应流读入内存，避免调用方在连接关闭后无法再次读取响应内容。
            byte[] buffer = new byte[16 * 1024];
            int len;
            while ((len = actualStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            return new HttpResponse(responseCode, connection.getResponseMessage(), connection.getHeaderFields(),
                    outputStream.toByteArray(), charset);
        } finally {
            connection.disconnect();
        }
    }

    public byte[] responseBytes() {
        return response().bodyBytes();
    }

    public String responseString(Charset charset) {
        return response().body(charset);
    }

    public String responseString() {
        return response().body();
    }

    @SneakyThrows
    private HttpURLConnection execute() {
        if (executed) {
            throw new IllegalStateException("request has been executed");
        }
        executed = true;

        String actualUrl = url;
        if (!queryParams.isEmpty()) {
            // 查询参数统一在发送前拼接，避免调用阶段多处维护 URL 组装逻辑。
            if (!actualUrl.contains("?")) {
                actualUrl += "?" + formatQueryParams(queryParams, charset);
            } else {
                if (!actualUrl.endsWith("&")) {
                    actualUrl += "&";
                }
                actualUrl += formatQueryParams(queryParams, charset);
            }
        }
        if (!formParams.isEmpty()) {
            // 表单请求最终会编码成 x-www-form-urlencoded 文本后写入请求体。
            body = formatQueryParams(formParams, charset);
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(actualUrl).openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);

        for (Map.Entry<String, String> entry : header.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }

        connection.setDoInput(true);
        if (body != null) {
            connection.setDoOutput(true);
            try (OutputStream outputStream = connection.getOutputStream()) {
                // 请求体统一在这里写出，所有调用路径共享同一套发送逻辑。
                outputStream.write(body.getBytes(charset));
            }
        }
        responseCode = connection.getResponseCode();
        log.debug("request url:{}, body:{}", actualUrl, body);
        return connection;
    }

    private static String formatQueryParams(Map<?, ?> args, Charset charset) {
        if (args != null && !args.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            // 保留插入顺序，便于调试，也避免同一请求在日志里反复变序。
            Iterator<? extends Map.Entry<?, ?>> it = args.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<?, ?> next = it.next();
                sb.append(urlEncode(next.getKey(), charset))
                        .append("=")
                        .append(urlEncode(next.getValue(), charset))
                        .append("&");
            }
            return sb.substring(0, sb.length() - 1);
        }
        return null;
    }

    @SneakyThrows
    private static String urlEncode(Object value, Charset charset) {
        return URLEncoder.encode(value == null ? "" : value.toString(), charset.name());
    }
}
