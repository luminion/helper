package io.github.luminion.helper.base;

/**
 * Bit位操作辅助类，用于权限管理等场景
 *
 * @author luminion
 * @since 1.0.0
 */
public abstract class BitHelper {

    private BitHelper() {}

    // ==================== 判断操作 ====================

    /**
     * 判断是否包含指定位（任意一位）
     *
     * @param source  源值
     * @param bitMask 位掩码
     * @return 是否包含任意一位
     */
    public static boolean hasAnyBits(int source, int bitMask) {
        return (source & bitMask) != 0;
    }

    /**
     * 判断是否包含指定位（任意一位）- long版本
     */
    public static boolean hasAnyBits(long source, long bitMask) {
        return (source & bitMask) != 0;
    }

    /**
     * 判断是否完全包含指定位（所有位）
     *
     * @param source  源值
     * @param bitMask 位掩码
     * @return 是否完全包含所有位
     */
    public static boolean hasAllBits(int source, int bitMask) {
        return (source & bitMask) == bitMask;
    }

    /**
     * 判断是否完全包含指定位（所有位）- long版本
     */
    public static boolean hasAllBits(long source, long bitMask) {
        return (source & bitMask) == bitMask;
    }

    /**
     * 判断是否不包含指定位（所有位）
     *
     * @param source  源值
     * @param bitMask 位掩码
     * @return 是否不包含任何指定位
     */
    public static boolean hasNoBits(int source, int bitMask) {
        return (source & bitMask) == 0;
    }

    /**
     * 判断是否不包含指定位（所有位）- long版本
     */
    public static boolean hasNoBits(long source, long bitMask) {
        return (source & bitMask) == 0;
    }
    
}
