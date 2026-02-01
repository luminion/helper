package io.github.luminion.helper.collection;

import io.github.luminion.helper.reflect.LambdaHelper;
import io.github.luminion.helper.reflect.SFunc;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 枚举助手，方便获取枚举中的key和value
 *
 * @author luminion
 */
public class EnumHelper<E extends Enum<E>, K> {
    private final Map<K, E> keyMap;

    private EnumHelper(Class<E> clazz, Function<E, K> getter) {
        EnumSet<E> enumSet = EnumSet.allOf(clazz);
        Map<K, E> map = new HashMap<>(enumSet.size());

        for (E e : enumSet) {
            K key = getter.apply(e);
            if (key != null) {
                if (map.containsKey(key)) {
                     throw new IllegalArgumentException("Duplicate key found in enum: " + key);
                }
                map.put(key, e);
            }
        }
        this.keyMap = Collections.unmodifiableMap(map);
    }

    /**
     * 创建一个枚举助手实例
     * <p>
     *
     * @param getter 获取指定属性的方法引用，如 UserType::getCode
     * @param <E>    枚举类型
     * @param <K>    Key 类型
     * @return 枚举助手实例
     */
    public static <E extends Enum<E>, K> EnumHelper<E, K> of(SFunc<E, K> getter) {
        Class<E> clazz = LambdaHelper.resolveClass(getter);
        return new EnumHelper<>(clazz, getter);
    }

    /**
     * 根据 key 获取枚举实例 (O(1) 复杂度)
     *
     * @param key key
     * @return 枚举实例，未找到返回 null
     */
    public E resolve(K key) {
        if (key == null) {
            return null;
        }
        return keyMap.get(key);
    }

    /**
     * 根据 key 获取枚举实例
     */
    public Optional<E> resolveOptional(K key) {
        return Optional.ofNullable(resolve(key));
    }

    /**
     * 快捷获取 Value
     *
     * @param key         key
     * @param valueGetter 获取值的 getter
     * @param <V>         值类型
     * @return 值，未找到返回 null
     */
    public <V> V getValue(K key, Function<E, V> valueGetter) {
        E e = resolve(key);
        return e == null ? null : valueGetter.apply(e);
    }

    /**
     * 获取枚举键值对映射的Map
     * 
     * @param valueGetter 获取值的 getter
     * @param <V>         值类型
     * @return 映射的Map
     */
    public <V> Map<K, V> getMap(Function<E, V> valueGetter) {
        return keyMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> valueGetter.apply(e.getValue())));
    }
}