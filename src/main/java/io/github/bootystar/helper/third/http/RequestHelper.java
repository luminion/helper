package io.github.bootystar.helper.third.http;

import io.github.bootystar.helper.base.http.IpHelper;

/**
 * @author bootystar
 */
public class RequestHelper {

    /**
     * 获取请求的ip地址
     *
     * @param request 请求
     * @return {@link String }
     */
    public static String getRequestIpAddr(jakarta.servlet.http.HttpServletRequest request) {
        return IpHelper.getRequestIpaddr(request.getHeader("x-forwarded-for"), request.getHeader("Proxy-Client-IP"), request.getHeader("WL-Proxy-Client-IP"), request.getRemoteAddr());
    }

    /**
     * 获取请求的ip地址
     *
     * @param request 请求
     * @return {@link String }
     */
    public static String getRequestIpAddr(javax.servlet.http.HttpServletRequest request) {
        return IpHelper.getRequestIpaddr(request.getHeader("x-forwarded-for"), request.getHeader("Proxy-Client-IP"), request.getHeader("WL-Proxy-Client-IP"), request.getRemoteAddr());
    }
    
}
