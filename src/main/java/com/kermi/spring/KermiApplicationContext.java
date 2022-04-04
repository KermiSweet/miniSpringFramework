package com.kermi.spring;

import com.kermi.spring.annotation.Autowired;
import com.kermi.spring.annotation.Component;
import com.kermi.spring.annotation.ComponentScan;
import com.kermi.spring.annotation.Scope;
import com.kermi.spring.aware.BeanNameAware;
import com.kermi.spring.constants.ScopType;
import com.kermi.spring.core.BeanDefinition;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class KermiApplicationContext {
    private Class configClass;
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Object> singletonObjecs = new ConcurrentHashMap<>();

    public KermiApplicationContext(Class configClass) {
        this.configClass = configClass;

        //扫描-->beanDefinition-->beanDefinitionMap
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            String path = componentScanAnnotation.value();
            path = path.replace(".", "/");
            ClassLoader classLoader = KermiApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path);
            File file = new File(resource.getFile());

            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    String fileName = f.getAbsolutePath();
                    if (fileName.endsWith(".class")) {
                        String className = fileName.substring(this.getClass().getResource("/").getPath().substring(1).length(), fileName.indexOf(".class"));
                        className = className.replace("\\", ".");
//                        System.out.println(className);
                        try {
                            Class<?> clazz = classLoader.loadClass(className);
                            if (clazz.isAnnotationPresent(Component.class)) {
                                //Bean
                                Component component = clazz.getAnnotation(Component.class);
                                String beanName = component.value().equals("") ? Introspector.decapitalize(clazz.getSimpleName()) : component.value();
                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setType(clazz);
                                if (clazz.isAnnotationPresent(Scope.class)) {
                                    Scope scopeAnnotation = clazz.getAnnotation(Scope.class);
                                    ScopType scope = scopeAnnotation.value();
                                    beanDefinition.setScope(scope);
                                } else {
                                    beanDefinition.setScope(ScopType.SINGLETON);
                                }
                                beanDefinitionMap.put(beanName, beanDefinition);
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        //实例化单例bean
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")) {
                Object bean = createBean(beanName, beanDefinition);
            }

        }

    }

    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new NullPointerException();
        } else {
            ScopType scope = beanDefinition.getScope();
            Object bean = null;
            switch (scope) {
                case SINGLETON -> {
                    bean = singletonObjecs.get(beanName);
                    if (bean == null) {
                        bean = createBean(beanName, beanDefinition);
                        singletonObjecs.put(beanName, bean);
                    }
                    break;
                }
                case PROTOTYPE -> {
                    return createBean(beanName, beanDefinition);
                }
                default -> {
                    return createBean(beanName, beanDefinition);
                }
            }
            return bean;
        }
    }

    public Object getBean(Class clazz) {
        return getBean(Introspector.decapitalize(clazz.getSimpleName()));
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getType();
        try {
            Object instance = clazz.getConstructor().newInstance();
            //依赖注入
            for (Field f : clazz.getDeclaredFields()) {
                if (f.isAnnotationPresent(Autowired.class)) {
                    f.setAccessible(true);
                    f.set(instance, getBean(f.getName()));
                }
            }

            //Aware回调
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            //初始化
            if (instance instanceof InitializingBean) {
                ((InitializingBean) instance).afterPropertiesSet();
            }

            //BeanPostProcessor 初始化后 AOP

            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getSimpleClassName(Class clazz) {
        String className = clazz.getSimpleName();
        if (Character.isLowerCase(className.charAt(0))) {
            return className;
        }
        return (new StringBuilder().append(Character.toLowerCase(className.charAt(0))).append(className.substring(1))).toString();
    }
}
