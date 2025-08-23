package com.qiapi.project.provider;


import org.apache.dubbo.config.annotation.DubboService;

/**
 * @author zhexueqi
 * @ClassName DemoServiceImpl
 * @since 2025-08-12    23:34
 */
@DubboService
public class DemoServiceImpl implements DemoService {
    @Override
    public String sayHello(String name) {
        return "123";
    }

    @Override
    public String sayHello2(String name) {
        return "312";
    }
}
