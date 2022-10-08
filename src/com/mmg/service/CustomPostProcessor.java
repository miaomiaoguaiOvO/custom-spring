package com.mmg.service;

import com.mmg.spring.BeanPostProcessor;
import com.mmg.spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 初始化的时候操作bean
 * @author mmg
 */
@Component
public class CustomPostProcessor implements BeanPostProcessor {

    private static final String USER_SERVICE = "userService";
    @Override
    public Object postProcessBeforeInitialization(String beanName, Object bean) {
        if (USER_SERVICE.equals(beanName)) {
            System.out.println("before...");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(String beanName, Object bean) {
        if (USER_SERVICE.equals(beanName)) {
            return Proxy.newProxyInstance(CustomPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("切面逻辑...");
                    return method.invoke(bean, args);
                }
            });
        }
        return bean;
    }
}
