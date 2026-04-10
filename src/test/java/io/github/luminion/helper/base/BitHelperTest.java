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
    void shouldReturnSetBitsInAscendingOrderAndIgnoreNegativeValues() {
        assertEquals(Arrays.asList(1, 2, 4), BitHelper.getSetBits(7));
        assertEquals(Collections.<Integer>emptyList(), BitHelper.getSetBits(-1));
        assertEquals(Arrays.asList(1L, 2L, 8L), BitHelper.getSetBits(11L));
        assertEquals(Collections.<Long>emptyList(), BitHelper.getSetBits(-1L));
    }
}
