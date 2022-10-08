package com.mmg.spring;

/**
 * 希望对Bean创建的过程做一些处理（初始化处理）
 *
 * @author mmg
 */
public interface BeanPostProcessor {
    /**
     * postProcessBeforeInitialization
     * @param beanName beanName
     * @param bean bean
     * @return object
     */
    Object postProcessBeforeInitialization(String beanName, Object bean);

    /**
     * postProcessAfterInitialization
     * @param beanName beanName
     * @param bean Bean
     * @return object
     */
    Object postProcessAfterInitialization(String beanName, Object bean);
}
