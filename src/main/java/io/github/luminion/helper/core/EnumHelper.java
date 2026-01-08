package io.github.luminion.helper.core;

import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.Objects;
import java.util.function.Function;

/**
 * 枚举助手，方便获取枚举中的key和value
 *
 * @author luminion
 */
@RequiredArgsConstructor
public class EnumHelper<E extends Enum<E>, K> {
    private final EnumSet<E> enums;
    private final Function<E, K> getter;

    /**
     * 创建一个枚举助手实例
     *
     * @param clazz       枚举类
     * @param keyGetter   获取key的方法
     * @param <E>         枚举类型
     * @return 枚举助手实例
     */
    public static <E extends Enum<E>, K> EnumHelper<E, K> of(Class<E> clazz,
                                                             Function<E, K> keyGetter) {
        return new EnumHelper<>(EnumSet.allOf(clazz), keyGetter);
    }

    /**
     * 根据key获取枚举实例
     *
     * @param key key
     * @return 枚举实例
     */
    public E get(K key) {
        if (key == null) {
            return null;
        }
        for (E e : enums) {
            K apply = getter.apply(e);
            if (Objects.equals(key, apply)) {
                return e;
            }
        }
        return null;
    }


}
