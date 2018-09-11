package com.eveb.saasops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableDiscoveryClient
@SpringBootApplication
@EnableTransactionManagement
@ServletComponentScan
@EnableCaching
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableRedisHttpSession
@EnableAsync
@EnableScheduling
public class Saasopsv2Application {

    public static void main(String[] args) {
        SpringApplication.run(Saasopsv2Application.class, args);
    }
}
