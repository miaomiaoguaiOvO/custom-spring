package com.mmg.spring;

/**
 * 初始化bean
 * @author mmg
 */
public interface InitializingBean {
    /**
     * afterPropertiesSet
     */
    void afterPropertiesSet();
}
