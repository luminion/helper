package io.github.luxmixus.helper.core;

import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * 枚举助手，方便获取枚举中的key和value
 *
 * @author luxmixus
 */
@RequiredArgsConstructor
public class EnumHelper<E extends Enum<E>> {
    private final EnumSet<E> enums;
    private final Function<E, Integer> keyGetter;
    private final Function<E, String> valueGetter;

    /**
     * 创建一个枚举助手实例
     *
     * @param clazz       枚举类
     * @param keyGetter   获取key的方法
     * @param valueGetter 获取value的方法
     * @param <E>         枚举类型
     * @return 枚举助手实例
     */
    public static <E extends Enum<E>> EnumHelper<E> of(Class<E> clazz, 
                                                       Function<E, Integer> keyGetter, 
                                                       Function<E, String> valueGetter) {
        return new EnumHelper<>(EnumSet.allOf(clazz), keyGetter, valueGetter);
    }

    /**
     * 根据key获取枚举实例
     *
     * @param key key
     * @return 枚举实例
     */
    public E getEnum(Integer key) {
        if (key == null) {
            return null;
        }
        for (E e : enums) {
            Integer apply = keyGetter.apply(e);
            if (Objects.equals(key, apply)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 根据value获取枚举实例
     *
     * @param value value
     * @return 枚举实例
     */
    public E getEnum(String value) {
        if (value == null) {
            return null;
        }
        for (E e : enums) {
            String apply = valueGetter.apply(e);
            if (Objects.equals(value, apply)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 根据key获取value
     *
     * @param key key
     * @return value
     */
    public String getValue(Integer key) {
        if (key == null) {
            return null;
        }
        E e = getEnum(key);
        if (e != null) {
            return valueGetter.apply(e);
        }
        return null;
    }

    /**
     * 根据value获取key
     *
     * @param value value
     * @return key
     */
    public Integer getKey(String value) {
        if (value == null) {
            return null;
        }
        E e = getEnum(value);
        if (e != null) {
            return keyGetter.apply(e);
        }
        return null;
    }


    /**
     * 将枚举转换为Map
     *
     * @return key-value构成的map
     */
    public Map<Integer, String> toMap() {
        Map<Integer, String> map = new HashMap<>();
        for (E e : enums) {
            map.put(keyGetter.apply(e), valueGetter.apply(e));
        }
        return map;
    }


}
