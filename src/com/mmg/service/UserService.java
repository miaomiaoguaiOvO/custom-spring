package com.mmg.service;

import com.mmg.spring.Autowired;
import com.mmg.spring.Component;
import com.mmg.spring.Scope;


@Component
@Scope("singleton")
public class UserService implements UserInterface {
    @Autowired
    private OrderService orderService;

    @Override
    public void test() {
        System.out.println(orderService);
    }


}
