package io.github.luminion.helper.spring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringContextHelperTest {

    @AfterEach
    void tearDown() throws Exception {
        resetStaticState();
    }

    @Test
    void shouldRegisterAndUnregisterBeans() throws Exception {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("managedBean", SampleBean.class, ()->new SampleBean());
        context.refresh();

        SpringContextHelper helper = new SpringContextHelper();
        helper.postProcessBeanFactory(context.getBeanFactory());
        helper.setApplicationContext(context);

        SpringContextHelper.registerBean("dynamicBean", new SampleBean("dynamic"));

        assertTrue(SpringContextHelper.containsBean("managedBean"));
        assertNotNull(SpringContextHelper.getBean("dynamicBean"));
        assertNotNull(SpringContextHelper.getBeanIfPresent("managedBean", SampleBean.class));

        SpringContextHelper.unregisterBean("managedBean");
        SpringContextHelper.unregisterBean("dynamicBean");

        assertFalse(SpringContextHelper.containsBean("managedBean"));
        assertNull(SpringContextHelper.getBeanIfPresent("managedBean", SampleBean.class));

        context.close();
    }

    @Test
    void shouldReadEnvironmentProperty() throws Exception {
        GenericApplicationContext context = new GenericApplicationContext();
        context.getEnvironment().getSystemProperties().put("demo.key", "demo-value");
        context.refresh();

        SpringContextHelper helper = new SpringContextHelper();
        helper.postProcessBeanFactory(context.getBeanFactory());
        helper.setApplicationContext(context);

        assertEquals("demo-value", SpringContextHelper.getProperty("demo.key"));

        context.close();
    }

    private void resetStaticState() throws Exception {
        Field beanFactoryField = SpringContextHelper.class.getDeclaredField("beanFactory");
        beanFactoryField.setAccessible(true);
        beanFactoryField.set(null, null);

        Field applicationContextField = SpringContextHelper.class.getDeclaredField("applicationContext");
        applicationContextField.setAccessible(true);
        applicationContextField.set(null, null);
    }

    public static class SampleBean {
        private final String name;

        public SampleBean() {
            this("managed");
        }

        public SampleBean(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
