package io.github.bootystar.helper.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 代理助手
 * todo 待完善
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
