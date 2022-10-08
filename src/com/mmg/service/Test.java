package com.mmg.service;

import com.mmg.spring.ApplicationContext;

/**
 * @author mmg
 */
public class Test {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ApplicationContext(AppConfig.class);
        UserInterface userService = (UserInterface) applicationContext.getBean("userService");
        userService.test();
    }
}
