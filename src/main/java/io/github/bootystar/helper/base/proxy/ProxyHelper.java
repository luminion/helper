package io.github.bootystar.helper.base.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 代理助手
 *
 * @author bootystar
 */
public class ProxyHelper {
    
    
    public static <T> T newProxyInstance(Class<T> clazz) {
        return null;
    }

    
    public static class Inner implements InvocationHandler{

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return null;
        }
    }
}
