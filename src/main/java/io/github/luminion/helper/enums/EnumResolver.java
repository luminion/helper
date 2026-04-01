package io.github.luminion.helper.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * 枚举解析器。
 *
 * @author luminion
 */
public class EnumResolver<E extends Enum<E>, K> {
    private final Map<K, E> keyMap;

    public EnumResolver(Class<E> clazz, Function<E, K> getter) {
        EnumSet<E> enumSet = EnumSet.allOf(clazz);
        // 构造时一次性建好 key -> enum 索引，后续查询就不需要每次遍历枚举常量。
        Map<K, E> map = new LinkedHashMap<K, E>(enumSet.size());
        for (E e : enumSet) {
            K key = getter.apply(e);
            if (key != null) {
                if (map.containsKey(key)) {
                    throw new IllegalArgumentException("Duplicate key:[" + key + "] found in enum: " + clazz.getName());
                }
                map.put(key, e);
            }
        }
        this.keyMap = Collections.unmodifiableMap(map);
    }

    public static <E extends Enum<E>, K> EnumResolver<E, K> of(Class<E> clazz, Function<E, K> getter) {
        return new EnumResolver<E, K>(clazz, getter);
    }

    public E resolveEnum(K key) {
        if (key == null) {
            return null;
        }
        return keyMap.get(key);
    }

    public Optional<E> resolveEnumOptional(K key) {
        return Optional.ofNullable(resolveEnum(key));
    }

    public E requireEnum(K key) {
        E value = resolveEnum(key);
        if (value == null) {
            throw new IllegalArgumentException("Enum not found for key: " + key);
        }
        return value;
    }

    public boolean containsKey(K key) {
        return key != null && keyMap.containsKey(key);
    }

    public <V> V resolveValue(K key, Function<E, V> valueGetter) {
        E e = resolveEnum(key);
        return e == null ? null : valueGetter.apply(e);
    }

    public <V> List<V> resolveValues(Function<E, V> valueGetter, K... keys) {
        if (keys == null || keys.length == 0) {
            return Collections.emptyList();
        }
        Set<K> keySet = new HashSet<K>(Arrays.asList(keys));
        List<V> values = new ArrayList<V>();
        for (Map.Entry<K, E> entry : keyMap.entrySet()) {
            if (keySet.contains(entry.getKey())) {
                V value = valueGetter.apply(entry.getValue());
                if (value != null) {
                    values.add(value);
                }
            }
        }
        return values;
    }

    public <V> List<K> resolveKeys(Function<E, V> valueGetter, V... values) {
        if (values == null || values.length == 0) {
            return Collections.emptyList();
        }
        Set<V> valueSet = new HashSet<V>(Arrays.asList(values));
        List<K> keys = new ArrayList<K>();
        for (Map.Entry<K, E> entry : keyMap.entrySet()) {
            V value = valueGetter.apply(entry.getValue());
            if (value != null && valueSet.contains(value)) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    public <V> Map<K, V> resolveMap(Function<E, V> valueGetter) {
        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, E> entry : keyMap.entrySet()) {
            V value = valueGetter.apply(entry.getValue());
            if (value != null) {
                result.put(entry.getKey(), value);
            }
        }
        return result;
    }

    public Set<K> keys() {
        return keyMap.keySet();
    }

    public List<E> enums() {
        return new ArrayList<E>(keyMap.values());
    }

    public Map<K, E> keyMap() {
        return keyMap;
    }
}
