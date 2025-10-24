package io.github.luxmixus.helper.core;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * http请求助手
 *
 * @author luxmixus
 */
@Slf4j
public class HttpHelper {
    protected String url;
    protected String method;
    protected Charset charset = StandardCharsets.UTF_8;
    protected int connectTimeout = 3000;
    protected int readTimeout = 10000;
    protected boolean executed;
    protected Map<String, String> header = new LinkedHashMap<>();
    protected Map<String, String> queryParams = new LinkedHashMap<>();
    protected Map<String, String> formParams = new LinkedHashMap<>();
    protected String body;
    protected int responseCode = -1;


    public static HttpHelper get(String url) {
        return new HttpHelper(url, "GET");
    }

    public static HttpHelper post(String url) {
        return new HttpHelper(url, "POST");
    }

    public static HttpHelper put(String url) {
        return new HttpHelper(url, "PUT");
    }

    public static HttpHelper delete(String url) {
        return new HttpHelper(url, "DELETE");
    }

    public static HttpHelper head(String url) {
        return new HttpHelper(url, "HEAD");
    }

    public static HttpHelper options(String url) {
        return new HttpHelper(url, "OPTIONS");
    }

    protected static String formatQueryParams(Map<?, ?> args) {
        if (args != null && !args.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            Iterator<? extends Map.Entry<?, ?>> it = args.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<?, ?> next = it.next();
                sb.append(next.getKey()).append("=").append(next.getValue()).append("&");
            }
            return sb.substring(0, sb.length() - 1);
        }
        return null;
    }

    protected HttpHelper(String httpUrl, String httpMethod) {
        this.url = httpUrl;
        this.method = httpMethod;
    }

    public int getResponseCode() {
        if (responseCode == -1) {
            throw new IllegalStateException("connection not open yet");
        }
        return responseCode;
    }

    public HttpHelper connectTimeout(int timeout) {
        this.connectTimeout = timeout;
        return this;
    }

    public HttpHelper readTimeout(int timeout) {
        this.readTimeout = timeout;
        return this;
    }


    public HttpHelper charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public HttpHelper header(String key, String value) {
        header.put(key, value);
        return this;
    }

    public HttpHelper header(Map<String, String> params) {
        header.putAll(params);
        return this;
    }

    public HttpHelper queryParam(String key, String value) {
        queryParams.put(key, value);
        return this;
    }

    public HttpHelper queryParam(Map<String, String> params) {
        queryParams.putAll(params);
        return this;
    }

    public HttpHelper formParam(String key, String value) {
        if (body != null) {
            throw new IllegalStateException("form and request body cannot be set at the same time");
        }
        formParams.put(key, value);
        return this;
    }

    public HttpHelper formParam(Map<String, String> params) {
        if (body != null) {
            throw new IllegalStateException("form and request body cannot be set at the same time");
        }
        formParams.putAll(params);
        return this;
    }

    public HttpHelper bodyParam(String json) {
        if (!formParams.isEmpty()) {
            throw new IllegalStateException("form and request body cannot be set at the same time");
        }
        body = json;
        return this;
    }


    @SneakyThrows
    protected HttpURLConnection execute() {
        if (executed) {
            throw new IllegalStateException("request has been executed");
        } else {
            executed = true;
        }
        HttpURLConnection connection = null;

        String url = this.url;
        // 路径参数
        if (!queryParams.isEmpty()) {
            if (!url.contains("?")) {
                url += "?" + formatQueryParams(queryParams);
            } else {
                if (url.endsWith("&")) {
                    url += "&";
                }
                url += formatQueryParams(queryParams);
            }
        }
        // form参数
        if (!formParams.isEmpty()) {
            body = formatQueryParams(formParams);
        }
        // 通过远程url连接对象打开连接
        connection = (HttpURLConnection) new URL(url).openConnection();
        // 设置连接请求方式
        connection.setRequestMethod(method);
        // 设置连接超时时间, 毫秒
        connection.setConnectTimeout(connectTimeout);
        // 设置读取超时时间, 毫秒
        connection.setReadTimeout(readTimeout);

        // 设置传入参数的格式(Content-Type等):请求参数应该是 name1=value1&name2=value2 的形式。
        if (header != null && !header.isEmpty()) {
            for (Map.Entry<?, ?> entry : header.entrySet()) {
                connection.setRequestProperty(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
        connection.setDoInput(true);
        if (body != null) {
            // 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                // 通过输出流对象将参数写出去/传输出去,它是通过字节数组写出的
                os.write(body.getBytes());
                // 通过连接对象获取一个输入流，向远程读取
            }
        }
        log.debug("request url:{}, body:{}", url, body);
        return connection;

    }

    @SneakyThrows
    public InputStream responseStream() {
        HttpURLConnection execute = null;
        try {
            execute = execute();
            int code = execute.getResponseCode();
            if (code != 200) {
                log.warn("request may execute failed , http code:{},  url:{}, body:{}", code, url, body);
            }
            return execute.getInputStream();
        } finally {
            if (execute != null) {
                execute.disconnect();
            }
        }
    }

    @SneakyThrows
    public String responseString(Charset charset) {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = responseStream();
             InputStreamReader isr = new InputStreamReader(is, charset);
             BufferedReader br = new BufferedReader(isr)) {
            String temp;
            while ((temp = br.readLine()) != null) {
                sb.append(temp);
                sb.append("\r\n");
            }
            return sb.toString();
        }
    }

    public String responseString() {
        return responseString(charset);
    }

}
