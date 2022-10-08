package com.mmg.spring;

/**
 * 希望对Bean创建的过程做一些处理（初始化处理）
 */
public interface BeanPostProcessor {
    Object postProcessBeforeInitialization(String beanName,Object bean);
    Object postProcessAfterInitialization(String beanName,Object bean);
}
