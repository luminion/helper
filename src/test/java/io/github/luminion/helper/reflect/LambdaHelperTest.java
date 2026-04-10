package io.github.luminion.helper.reflect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LambdaHelperTest {

    @Test
    void shouldResolveLambdaMetadata() {
        assertEquals(Demo.class, LambdaHelper.resolveClass(Demo::getName));
        assertEquals("name", LambdaHelper.resolvePropertyName(Demo::getName));
        assertEquals("isActive", LambdaHelper.resolveMethodName(Demo::isActive));
    }

    public static class Demo {
        public String getName() {
            return "demo";
        }

        public boolean isActive() {
            return true;
        }
    }
}
