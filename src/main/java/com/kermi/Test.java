package com.kermi;

import com.kermi.AppConfig;
import com.kermi.spring.KermiApplicationContext;
import com.kermi.service.UserService;

public class Test {
    public static void main(String[] args) {
        KermiApplicationContext applicationContext = new KermiApplicationContext(AppConfig.class);

        UserService bean = (UserService) applicationContext.getBean(UserService.class);
        bean.test();

    }
}
