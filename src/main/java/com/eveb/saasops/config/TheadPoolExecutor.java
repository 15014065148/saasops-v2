package com.eveb.saasops.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by William on 2018/3/2.
 */
@Configuration
@EnableAsync
public class TheadPoolExecutor {

    /***线程池维护线程的最少数量**/
    private final int minSize = 0;
    /***允许的空闲时间**/
    private final int aliveSeconds = 300;
    /***线程池维护线程的最大数量**/
    private final int maxSize = 6;
    /***缓存队列**/
    private final int queueCapacity = 5000;

    @Bean
    public Executor msgAsyncExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(minSize);//线程池维护线程的最少数量
        pool.setKeepAliveSeconds(aliveSeconds);//允许的空闲时间
        pool.setMaxPoolSize(maxSize * 6);//线程池维护线程的最大数量
        pool.setQueueCapacity(queueCapacity);//缓存队列
        pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());//线程调用运行该任务的 execute 本身。此策略提供简单的反馈控制机制，能够减缓新任务的提交速度
        pool.setWaitForTasksToCompleteOnShutdown(true);
        pool.initialize();
        return pool;
    }

    @Bean
    public Executor eventAsyncExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(minSize);//线程池维护线程的最少数量
        pool.setKeepAliveSeconds(aliveSeconds);//允许的空闲时间
        pool.setMaxPoolSize(maxSize * 6);//线程池维护线程的最大数量
        pool.setQueueCapacity(queueCapacity);//缓存队列
        pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());//线程调用运行该任务的 execute 本身。此策略提供简单的反馈控制机制，能够减缓新任务的提交速度
        pool.setWaitForTasksToCompleteOnShutdown(true);
        pool.initialize();
        return pool;
    }

    @Bean
    public Executor getPayResultExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(minSize);//线程池维护线程的最少数量
        pool.setKeepAliveSeconds(aliveSeconds);//允许的空闲时间
        pool.setMaxPoolSize(maxSize * 6);//线程池维护线程的最大数量
        pool.setQueueCapacity(queueCapacity);//缓存队列
        pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());//线程调用运行该任务的 execute 本身。此策略提供简单的反馈控制机制，能够减缓新任务的提交速度
        pool.setWaitForTasksToCompleteOnShutdown(true);
        pool.initialize();
        return pool;
    }
}
