package com.mmg.spring;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mmg
 */
public class ApplicationContext {
    private Class<?> configClass;
    /**
     * 类定义Map
     */
    private static final Map<String, BeanDefinition> BEAN_DEFINITION_MAP = new ConcurrentHashMap<>();
    /**
     * 单例bean Map
     */
    private static final Map<String, Object> SINGLETON_OBJECTS = new ConcurrentHashMap<>();
    /**
     * 操作bean初始化的处理器
     */
    private static final List<BeanPostProcessor> BEAN_POST_PROCESSOR_LIST = new ArrayList<>();
    private static final String SINGLETON = "singleton";

    public ApplicationContext(Class<?> configClass) {
        this.configClass = configClass;
        //扫描包路径 -->创建BeanDefinition-->放入beanDefinitionMap
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            //获取到扫描路径 com.mmg.service
            ComponentScan componentScanAnnotation = configClass.getAnnotation(ComponentScan.class);
            String path = componentScanAnnotation.value();
            path = path.replace(".", "/");

            //获取到需要加载的class文件对应的文件夹
            ClassLoader classLoader = ApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path);

            //获取到对应的文件夹以及文件
            File file = new File(resource.getFile());
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    //获取文件的绝对路径
                    String fileName = f.getAbsolutePath();

                    //筛选出class文件
                    if (fileName.endsWith(".class")) {
                        //获取到className com.mmg.service.UserService
                        String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                        className = className.replace("\\", ".");

                        try {
                            //加载Class
                            Class<?> clazz = classLoader.loadClass(className);
                            //是否存在Component注解（这个类是不是一个bean）
                            if (clazz.isAnnotationPresent(Component.class)) {
                                //不会在这一步生成实例化Bean，而是生成一个BeanDefinition对象
                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setType(clazz);
                                //这个Bean是单例还是多例
                                if (clazz.isAnnotationPresent(Scope.class)) {
                                    Scope scope = clazz.getAnnotation(Scope.class);
                                    beanDefinition.setScope(scope.value());
                                } else {
                                    beanDefinition.setScope(SINGLETON);
                                }

                                //创建bean之前的操作
                                //判断是这个类是不是实现了BeanPostProcessor接口
                                if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                    BeanPostProcessor instance = (BeanPostProcessor) clazz.newInstance();
                                    //如果实现了就创建一个对象放到beanPostProcessorList中
                                    BEAN_POST_PROCESSOR_LIST.add(instance);
                                }

                                //生成beanName
                                Component componentAnnotation = clazz.getAnnotation(Component.class);
                                String beanName = componentAnnotation.value();
                                if (beanName == null || "".equals(beanName)) {
                                    beanName = Introspector.decapitalize(clazz.getSimpleName());
                                }
                                //把生成的beanDefinition放到一个map中
                                BEAN_DEFINITION_MAP.put(beanName, beanDefinition);
                            }
                        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }
            }
        }

        //扫描结束后，先把单例bean生成好（实例化单例bean）
        for (String beanName : BEAN_DEFINITION_MAP.keySet()) {
            BeanDefinition beanDefinition = BEAN_DEFINITION_MAP.get(beanName);
            if (SINGLETON.equals(beanDefinition.getScope())) {
                Object bean = createBean(beanName, beanDefinition);
                SINGLETON_OBJECTS.put(beanName, bean);
            }
        }

    }

    /**
     * 创建bean
     * bean的生命周期 ：实例化 依赖注入
     */
    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class<?> clazz = beanDefinition.getType();
        try {
            //实例化
            Object instance = clazz.getConstructor().newInstance();
            //依赖注入
            //获取所有字段 遍历
            for (Field field : clazz.getDeclaredFields()) {
                //是否有AutoWired注解
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);
                    field.set(instance, getBean(field.getName()));
                }
            }

            //Aware回调 是不是实现了BeanNameAware接口，如果是的话需要回调
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            //BeanPostProcessor 初始化前对bean的一些操作
            for (BeanPostProcessor beanPostProcessor : BEAN_POST_PROCESSOR_LIST) {
                instance = beanPostProcessor.postProcessBeforeInitialization(beanName, instance);
            }

            //初始化
            if (instance instanceof InitializingBean) {
                ((InitializingBean) instance).afterPropertiesSet();
            }

            //BeanPostProcessor 初始化后对bean的一些操作
            for (BeanPostProcessor beanPostProcessor : BEAN_POST_PROCESSOR_LIST) {
                instance = beanPostProcessor.postProcessAfterInitialization(beanName, instance);
            }


            //初始化后 AOP

            return instance;
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取Bean
     */
    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = BEAN_DEFINITION_MAP.get(beanName);
        if (beanDefinition == null) {
            throw new NullPointerException();
        } else {
            String scope = beanDefinition.getScope();
            //如果是个单例的
            if (SINGLETON.equals(scope)) {
                //如果是单例直接从单例池中返回
                Object bean = SINGLETON_OBJECTS.get(beanName);
                // 如果没有还需要创建bean并放入单例池中
                if (bean == null) {
                    bean = createBean(beanName, beanDefinition);
                    SINGLETON_OBJECTS.put(beanName, bean);
                }
                return bean;
            } else {
                //多例 每次都创建一个新的Bean
                return createBean(beanName, beanDefinition);
            }
        }
    }
}
