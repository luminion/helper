package io.github.luminion.helper.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用于获取Spring上下文及bean的工具
 * 
 * @author luminion
 */
@Component
public class SpringContextHelper implements BeanFactoryPostProcessor, ApplicationContextAware {
    private static ConfigurableListableBeanFactory beanFactory;
    private static ApplicationContext applicationContext;

    public static void main(String[] args) {
        LocalDateTime   localDateTime = LocalDateTime.now().plusSeconds(31102);
        System.out.println(localDateTime);
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        SpringContextHelper.beanFactory = beanFactory;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextHelper.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        if (null == applicationContext) {
            throw new IllegalStateException("No ApplicationContext injected, maybe not in the Spring environment?");
        }
        return applicationContext;
    }

    public static BeanFactory getBeanFactory() {
        BeanFactory factory = null == beanFactory ? applicationContext : beanFactory;
        if (null == factory) {
            throw new IllegalStateException("No ConfigurableListableBeanFactory or ApplicationContext injected, maybe not in the Spring environment?");
        } else {
            return factory;
        }
    }

    public static ListableBeanFactory getListableBeanFactory() {
        ListableBeanFactory factory = null == beanFactory ? applicationContext : beanFactory;
        if (null == factory) {
            throw new IllegalStateException("No ConfigurableListableBeanFactory or ApplicationContext injected, maybe not in the Spring environment?");
        } else {
            return factory;
        }
    }

    public static ConfigurableListableBeanFactory getConfigurableBeanFactory() {
        ConfigurableListableBeanFactory factory;
        if (null != beanFactory) {
            factory = beanFactory;
        } else {
            if (!(applicationContext instanceof ConfigurableApplicationContext)) {
                throw new IllegalStateException("No ConfigurableListableBeanFactory from context!");
            }
            factory = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
        }
        return factory;
    }

    public static Environment getEnvironment() {
        return getApplicationContext().getEnvironment();
    }


    public static String getProperty(String key) {
        return null == applicationContext ? null : applicationContext.getEnvironment().getProperty(key);
    }

    public static String getApplicationName() {
        return getProperty("spring.application.name");
    }

    public static String[] getActiveProfiles() {
        return null == applicationContext ? null : applicationContext.getEnvironment().getActiveProfiles();
    }

    public static String getActiveProfile() {
        String[] activeProfiles = getActiveProfiles();
        return activeProfiles != null && activeProfiles.length > 0 ? activeProfiles[0] : null;
    }

    public static <T> T getBean(Class<T> clazz) {
        return (T) getBeanFactory().getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return getBeanFactory().getBean(name, clazz);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        return getListableBeanFactory().getBeansOfType(type);
    }

    public static <T> void registerBean(String beanName, T bean) {
        ConfigurableListableBeanFactory factory = getConfigurableBeanFactory();
        factory.autowireBean(bean);
        factory.registerSingleton(beanName, bean);
    }

    public static void unregisterBean(String beanName) {
        ConfigurableListableBeanFactory factory = getConfigurableBeanFactory();
        if (factory instanceof DefaultSingletonBeanRegistry) {
            DefaultSingletonBeanRegistry registry = (DefaultSingletonBeanRegistry) factory;
            registry.destroySingleton(beanName);
        } else {
            throw new IllegalStateException("Can not unregister bean, the factory is not a DefaultSingletonBeanRegistry!");
        }
    }

    public static void publishEvent(ApplicationEvent event) {
        getApplicationContext().publishEvent(event);
    }

    public static void publishEvent(Object event) {
        getApplicationContext().publishEvent(event);
    }


}
