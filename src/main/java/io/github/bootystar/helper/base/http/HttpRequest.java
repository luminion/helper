package io.github.bootystar.helper.base.http;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author bootystar
 */
@Slf4j
public class HttpRequest {
    private String httpUrl;
    private String method;
    private Charset charset = StandardCharsets.UTF_8;
    private int timeout = 3000;
    private boolean executed;

    private Map<String, String> header;
    private String body;

    private static String createParams(Map<?, ?> args) {
        if (args != null && !args.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            Iterator<? extends Map.Entry<?, ?>> it = args.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<?, ?> next = it.next();
                sb.append(next.getKey()).append("=").append(next.getValue()).append("&");
            }
            return sb.substring(0, sb.length() - 1);
        }
        return "";
    }


    protected HttpRequest(String httpUrl, String method) {
        this.httpUrl = httpUrl;
        this.method = method;
    }

    protected HttpRequest(String httpUrl, String method, Charset charset, int timeout) {
        this.httpUrl = httpUrl;
        this.method = method;
        this.charset = charset;
        this.timeout = timeout;
    }

    public HttpRequest queryParam(String key, String value) {
        return this;
    }

    public HttpRequest queryParam(Map<String, String> params) {
        return this;
    }

    public HttpRequest bodyMap(Map<String, String> params) {
        return this;
    }

    public HttpRequest bodyJson(String json) {
        return this;
    }


    @SneakyThrows
    protected void execute4UrlContention(Consumer<HttpURLConnection> consumer) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(httpUrl);
            // 通过远程url连接对象打开连接
            connection = (HttpURLConnection) url.openConnection();
            // 设置连接请求方式
            connection.setRequestMethod(method);
            // 设置超时时间： 毫秒
            connection.setConnectTimeout(timeout);
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
            connection.getResponseCode();
            consumer.accept(connection);
        } catch (IOException e) {
            log.error("execute failed:", e);
            throw new RuntimeException(e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    protected InputStream execute4InputStream() {

    }

    public String execute4String() {

    }


}
