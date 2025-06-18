package io.github.bootystar.helper.base.http;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * @author bootystar
 */
@Slf4j
public class IpHelper {
    public static final String UNKNOWN = "unknown";
    public static final String LOCALHOST = "127.0.0.1";

    /**
     * 获取请求的ip地址
     *
     * @param header     x-forwarded-for
     * @param header2    Proxy-Client-IP
     * @param header3    WL-Proxy-Client-IP
     * @param remoteAddr request.getRemoteAddr()
     * @return {@link String }
     */
    public static String getRequestIpaddr(String header, String header2, String header3, String remoteAddr) {
        String ipAddress;
        try {
            ipAddress = header;
            if (ipAddress == null || ipAddress.isEmpty() || UNKNOWN.equalsIgnoreCase(ipAddress)) {
                ipAddress = header2;
            }
            if (ipAddress == null || ipAddress.isEmpty() || UNKNOWN.equalsIgnoreCase(ipAddress)) {
                ipAddress = header3;
            }
            if (ipAddress == null || ipAddress.isEmpty() || UNKNOWN.equalsIgnoreCase(ipAddress)) {
                ipAddress = remoteAddr;
                if (LOCALHOST.equals(ipAddress)) {
                    // 根据网卡取本机配置的IP
                    try {
                        ipAddress = InetAddress.getLocalHost().getHostAddress();
                    } catch (UnknownHostException e) {
                        log.error("获取用户ip错误:", e);
                    }
                }
            }
            // 通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            if (ipAddress != null) {
                if (ipAddress.contains(",")) {
                    return ipAddress.split(",")[0];
                } else {
                    return ipAddress;
                }
            } else {
                return "";
            }
        } catch (Exception e) {
            log.error("获取用户ip错误:", e);
            return "";
        }
    }

    /**
     * 获取本机ip地址
     *
     * @return {@link String }
     */
    public static String getLocalIp() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (inetAddress.isSiteLocalAddress() && !inetAddress.isLoopbackAddress() && !inetAddress.getHostAddress().contains(":")) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
           log.error("获取本机ip错误:", e);
        }
        return "127.0.0.1";
    }
    
}
