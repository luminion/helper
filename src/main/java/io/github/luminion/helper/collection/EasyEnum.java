package io.github.luminion.helper.collection;

import io.github.luminion.helper.core.SFunc;

/**
 * @author luminion
 * @since 1.0.0
 */
public interface EasyEnum<E extends Enum<E>> {

    /**
     * 根据 key 获取枚举实例
     *
     * @return 枚举实例，未找到返回 null
     */
    default <R> E resolveEnum(SFunc<E, R> getter, R value) {
        return EnumHelper.resolve(getter, value);
    }

    /**
     * 根据 key 获取 value
     *
     * @param keyGetter  key 获取器
     * @param valueGetter value 获取器
     * @param key       key
     * @return value，未找到返回 null
     */
    default <K, V> V resolveValue(SFunc<E, K> keyGetter, SFunc<E, V> valueGetter, K key) {
        E resolve = EnumHelper.resolve(keyGetter, key);
        if (resolve == null) {
            return null;
        }
        return valueGetter.apply(resolve);
    }


}
