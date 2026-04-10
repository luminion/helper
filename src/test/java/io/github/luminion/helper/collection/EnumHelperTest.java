package io.github.luminion.helper.collection;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnumHelperTest {

    @Test
    void shouldResolveEnumAndValues() {
        EnumHelper<Status, Integer> helper = EnumHelper.of(Status.class, Status::getCode);

        assertEquals(Status.ENABLED, helper.resolveEnum(1));
        assertEquals("禁用", helper.resolveValue(0, Status::getLabel));
        assertEquals(Arrays.asList("禁用", "启用"), helper.resolveValues(Status::getLabel, 0, 1));
        assertTrue(helper.containsKey(1));

        assertEquals(Status.ENABLED, EnumHelper.resolveEnum(Status::getCode, 1));
        assertEquals("启用", EnumHelper.resolveValue(Status::getCode, Status::getLabel, 1));
        assertEquals(Arrays.asList(0, 1), EnumHelper.resolveKeys(Status::getCode, Status::getLabel, "禁用", "启用"));
        assertNull(EnumHelper.resolveEnum(Status::getCode, 99));
    }

    private enum Status {
        DISABLED(0, "禁用"),
        ENABLED(1, "启用");

        private final Integer code;
        private final String label;

        Status(Integer code, String label) {
            this.code = code;
            this.label = label;
        }

        public Integer getCode() {
            return code;
        }

        public String getLabel() {
            return label;
        }
    }
}
