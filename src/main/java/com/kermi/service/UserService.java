package com.kermi.service;

import com.kermi.spring.annotation.Autowired;
import com.kermi.spring.annotation.Component;
import com.kermi.spring.annotation.Scope;
import com.kermi.spring.aware.BeanNameAware;
import com.kermi.spring.constants.ScopType;

@Component
@Scope(ScopType.PROTOTYPE)
public class UserService implements BeanNameAware {

    @Autowired
    private OrderService orderService;

    private String beanName;

    public void test(){
        System.out.println(orderService);
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
}
