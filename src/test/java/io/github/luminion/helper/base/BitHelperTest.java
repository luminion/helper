package io.github.luminion.helper.base;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BitHelperTest {

    @Test
    void shouldHandleBitOperations() {
        assertTrue(BitHelper.hasAnyBits(0b0110, 0b0010));
        assertTrue(BitHelper.hasAllBits(0b0110, 0b0010));
        assertFalse(BitHelper.hasAllBits(0b0110, 0b0111));
        assertTrue(BitHelper.hasNoBits(0b0110, 0b1000));
        assertEquals(0b1110, BitHelper.setBits(0b0110, 0b1000));
        assertEquals(0b0100, BitHelper.clearBits(0b0110, 0b0010));
        assertEquals(0b0101, BitHelper.toggleBits(0b0110, 0b0011));
        assertTrue(BitHelper.isSingleBit(8));
        assertFalse(BitHelper.isSingleBit(10));
        assertEquals(0b1110, BitHelper.mergeBits(0b0010, 0b0100, 0b1000));
    }

    @Test
    void shouldReturnSetBitsInAscendingOrderIncludingHighBit() {
        assertEquals(Arrays.asList(1, 2, 4), BitHelper.getSetBits(7));
        assertEquals(Collections.<Integer>emptyList(), BitHelper.getSetBits(0));
        assertEquals(Arrays.asList(1L, 2L, 8L), BitHelper.getSetBits(11L));
        assertEquals(Collections.<Long>emptyList(), BitHelper.getSetBits(0L));
        // 最高位（符号位）也应被正确识别
        assertEquals(Collections.singletonList(Integer.MIN_VALUE), BitHelper.getSetBits(Integer.MIN_VALUE));
        assertEquals(Collections.singletonList(Long.MIN_VALUE), BitHelper.getSetBits(Long.MIN_VALUE));
        // -1 的所有 32/64 位均被设置
        assertEquals(32, BitHelper.getSetBits(-1).size());
        assertEquals(64, BitHelper.getSetBits(-1L).size());
        assertTrue(BitHelper.isSingleBit(Integer.MIN_VALUE));
        assertTrue(BitHelper.isSingleBit(Long.MIN_VALUE));
    }
}
