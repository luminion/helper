package io.github.luminion.helper.base;

import java.util.ArrayList;
import java.util.List;

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

    // ==================== 写入位操作 ====================

    public static int setBits(int source, int bitMask) {
        return source | bitMask;
    }

    public static long setBits(long source, long bitMask) {
        return source | bitMask;
    }

    public static int clearBits(int source, int bitMask) {
        return source & ~bitMask;
    }

    public static long clearBits(long source, long bitMask) {
        return source & ~bitMask;
    }

    public static int toggleBits(int source, int bitMask) {
        return source ^ bitMask;
    }

    public static long toggleBits(long source, long bitMask) {
        return source ^ bitMask;
    }

    public static boolean isSingleBit(int bitMask) {
        return bitMask > 0 && (bitMask & (bitMask - 1)) == 0;
    }

    public static boolean isSingleBit(long bitMask) {
        return bitMask > 0 && (bitMask & (bitMask - 1)) == 0;
    }

    public static int mergeBits(int... bitMasks) {
        int result = 0;
        if (bitMasks == null || bitMasks.length == 0) {
            return result;
        }
        for (int bitMask : bitMasks) {
            result |= bitMask;
        }
        return result;
    }

    public static long mergeBits(long... bitMasks) {
        long result = 0L;
        if (bitMasks == null || bitMasks.length == 0) {
            return result;
        }
        for (long bitMask : bitMasks) {
            result |= bitMask;
        }
        return result;
    }

    // ==================== 提取位操作 ====================

    /**
     * 获取值中所有被设置的位
     * <p>
     * 例如：传入7（二进制0111），返回[1, 2, 4]
     *
     * @param value 源值（仅处理非负数，负数会被忽略）
     * @return 所有被设置的位组成的列表，按位权升序排列
     */
    public static List<Integer> getSetBits(int value) {
        if (value == 0) {
            return new ArrayList<>();
        }
        List<Integer> result = new ArrayList<>(Integer.bitCount(value));
        while (value != 0) {
            int lowestBit = value & -value;
            result.add(lowestBit);
            value ^= lowestBit;
        }
        return result;
    }

    /**
     * 获取值中所有被设置的位 - long版本
     * <p>
     * 例如：传入7L（二进制0111），返回[1, 2, 4]
     *
     * @param value 源值（仅处理非负数，负数会被忽略）
     * @return 所有被设置的位组成的列表，按位权升序排列
     */
    public static List<Long> getSetBits(long value) {
        if (value == 0) {
            return new ArrayList<>();
        }
        List<Long> result = new ArrayList<>(Long.bitCount(value));
        while (value != 0) {
            long lowestBit = value & -value;
            result.add(lowestBit);
            value ^= lowestBit;
        }
        return result;
    }

    public static String toBinaryString(int value) {
        return Integer.toBinaryString(value);
    }

    public static String toBinaryString(long value) {
        return Long.toBinaryString(value);
    }

}
