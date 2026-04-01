package io.github.luminion.helper.http;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * HTTP 静态入口。
 * <p>
 * 适合快速创建请求对象，或者直接完成简单的 GET/POST/PUT/DELETE 调用。
 *
 * @author luminion
 */
public abstract class HttpHelper {
    private HttpHelper() {}

    /**
     * 按 HTTP 方法创建请求对象。
     */
    public static HttpRequest request(String method, String url) {
        return new HttpRequest(url, method);
    }

    /**
     * 创建 GET 请求。
     */
    public static HttpRequest get(String url) {
        return request("GET", url);
    }

    /**
     * 创建 POST 请求。
     */
    public static HttpRequest post(String url) {
        return request("POST", url);
    }

    /**
     * 创建 PUT 请求。
     */
    public static HttpRequest put(String url) {
        return request("PUT", url);
    }

    /**
     * 创建 DELETE 请求。
     */
    public static HttpRequest delete(String url) {
        return request("DELETE", url);
    }

    /**
     * 创建 HEAD 请求。
     */
    public static HttpRequest head(String url) {
        return request("HEAD", url);
    }

    /**
     * 创建 OPTIONS 请求。
     */
    public static HttpRequest options(String url) {
        return request("OPTIONS", url);
    }

    /**
     * 直接发起 GET 请求并返回响应文本。
     */
    public static String get(String url, Map<String, String> queryParams) {
        return get(url).queryParam(queryParams).responseString();
    }

    /**
     * 直接发起 GET 请求并按指定字符集读取响应文本。
     */
    public static String get(String url, Map<String, String> queryParams, Charset charset) {
        return get(url).charset(charset).queryParam(queryParams).responseString();
    }

    /**
     * 直接发起表单 POST 请求并返回响应文本。
     */
    public static String post(String url, Map<String, String> formParams) {
        return post(url).formParam(formParams).responseString();
    }

    /**
     * 直接发起表单 POST 请求并按指定字符集读取响应文本。
     */
    public static String post(String url, Map<String, String> formParams, Charset charset) {
        return post(url).charset(charset).formParam(formParams).responseString();
    }

    /**
     * 直接发起原始 body 的 POST 请求。
     */
    public static String post(String url, String body) {
        return post(url).body(body).responseString();
    }

    /**
     * 直接发起原始 body 的 PUT 请求。
     */
    public static String put(String url, String body) {
        return put(url).body(body).responseString();
    }

    /**
     * 直接发起 DELETE 请求并返回响应文本。
     */
    public static String delete(String url, Map<String, String> queryParams) {
        return delete(url).queryParam(queryParams).responseString();
    }
}
