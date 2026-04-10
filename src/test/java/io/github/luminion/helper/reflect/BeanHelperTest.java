package io.github.luminion.helper.reflect;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BeanHelperTest {

    @Test
    void shouldCopyPropertiesAndIgnoreNullByDefault() {
        Demo source = new Demo();
        source.setName("demo");
        source.setAge(null);

        Demo target = new Demo();
        target.setName("old");
        target.setAge(18);

        BeanHelper.copyProperties(source, target);

        assertEquals("demo", target.getName());
        assertEquals(Integer.valueOf(18), target.getAge());
    }

    @Test
    void shouldCopyNullWhenRequestedAndBuildDifference() {
        Demo source = new Demo();
        source.setName("demo");
        source.setAge(null);

        Demo target = new Demo();
        target.setName("old");
        target.setAge(18);

        BeanHelper.copyPropertiesWithNull(source, target);
        Demo diff = BeanHelper.toDifference(new Demo("new", 20), new Demo("old", 20));

        assertEquals("demo", target.getName());
        assertNull(target.getAge());
        assertEquals("new", diff.getName());
        assertNull(diff.getAge());
    }

    @Test
    void shouldConvertObjectToMap() {
        Map<String, Object> map = BeanHelper.objectToMap(new Demo("demo", 20));

        assertEquals("demo", map.get("name"));
        assertEquals(20, map.get("age"));
    }

    public static class Demo {
        private String name;
        private Integer age;

        public Demo() {}

        public Demo(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }
}
