package io.github.luminion.helper.json;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObjectMapperHelperTest {

    @Test
    void shouldSerializeNumbersAsStringsAndParseBack() {
        Demo demo = new Demo();
        demo.setId(1L);
        demo.setCreatedAt(LocalDateTime.of(2026, 4, 8, 12, 34, 56));

        String json = ObjectMapperHelper.of().toJson(demo);
        Demo parsed = ObjectMapperHelper.of().parseObject(json, Demo.class);

        assertTrue(json.contains("\"id\":\"1\""));
        assertEquals(demo.getCreatedAt(), parsed.getCreatedAt());
    }

    public static class Demo {
        private Long id;
        private LocalDateTime createdAt;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}
