package com.eveb.saasops.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCachePrefix;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import com.eveb.saasops.api.listener.RedisExpiredListener;
import com.eveb.saasops.common.constants.Constants;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Redis配置
 */
@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {
    @Autowired
    private RedisConnectionFactory factory;

    @Bean
    public CacheManager init(){
        RedisTemplate<String, Object> redisTemplate =redisTemplate();
        return cacheManager(redisTemplate);
    }

    public CacheManager cacheManager(RedisTemplate<String, Object> redisTemplate) {
    	SpringRedisCacheManager manager = new SpringRedisCacheManager(redisTemplate);
    	//RedisCacheManager manager = new RedisCacheManager(redisTemplate);
        manager.setUsePrefix(true);
        RedisCachePrefix cachePrefix = new RedisPrefix(Constants.PROJECT_NAME);
        manager.setCachePrefix(cachePrefix);
        manager.setDefaultExpiration(43200);//设置默认过期时间,12小时
        Map<String, Long> expiresMap = new HashMap<>();
        expiresMap.put("authority", 1000L);
        manager.setExpires(expiresMap);
        return manager;
    }


    
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate template = new StringRedisTemplate(factory);
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }

	@Bean
	public RedisTemplate<String, String> redisTemplate2() {
		RedisTemplate template = new StringRedisTemplate(factory);
		Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
		ObjectMapper om = new ObjectMapper();
		om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		jackson2JsonRedisSerializer.setObjectMapper(om);
		template.setValueSerializer(jackson2JsonRedisSerializer);
		template.afterPropertiesSet();
		return template;
	}

	/*@Bean
	public Executor executor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10);
		executor.setMaxPoolSize(20);
		executor.setQueueCapacity(100);
		executor.setKeepAliveSeconds(60);
		executor.setThreadNamePrefix("V-Thread");
		// rejection-policy：当pool已经达到max size的时候，如何处理新任务
		// CALLER_RUNS：不在新线程中执行任务，而是由调用者所在的线程来执行
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.initialize();
		return executor;
	}
    @Bean
    public RedisMessageListenerContainer listenerContainer(RedisConnectionFactory factory, Executor executor) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        // 设置Redis的连接工厂
        container.setConnectionFactory(factory);
        // 设置监听使用的线程池
        //container.setTaskExecutor(executor);
        // 设置监听的Topic: PatternTopic/ChannelTopic
        Topic topic = new PatternTopic(RedisExpiredListener.LISTENER_PATTERN);
        System.out.println(RedisExpiredListener.LISTENER_PATTERN);
        // 设置监听器
        container.addMessageListener(new RedisExpiredListener(), topic);
        return container;
    }*/
	@Bean
	public RedisMessageListenerContainer listenerContainer(RedisConnectionFactory factory) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(factory);
		Topic topic = new PatternTopic(RedisExpiredListener.LISTENER_PATTERN);
		container.addMessageListener(new RedisExpiredListener(), topic);
		return container;
	}
    @Bean
    public HashOperations<String, String, Object> hashOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForHash();
    }

    @Bean
    public ValueOperations<String, String> valueOperations(RedisTemplate<String, String> redisTemplate2) {
        return redisTemplate2.opsForValue();
    }

    @Bean
    public ListOperations<String, Object> listOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForList();
    }

    @Bean
    public SetOperations<String, Object> setOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForSet();
    }

    @Bean
    public ZSetOperations<String, Object> zSetOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForZSet();
    }
}
