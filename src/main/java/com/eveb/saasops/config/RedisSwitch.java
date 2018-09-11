package com.eveb.saasops.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisSwitch {

    @Autowired
    private JedisConnectionFactory jedisConnectionFactory;

    @Bean
    public StringRedisTemplate stringRedisTemplate_0() {
        return initStringRedisTemplate(0);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_1() {
        return initStringRedisTemplate(1);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_2() {
        return initStringRedisTemplate(2);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_3() {
        return initStringRedisTemplate(3);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_4() {
        return initStringRedisTemplate(4);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_5() {
        return initStringRedisTemplate(5);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_6() {
        return initStringRedisTemplate(6);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_7() {
        return initStringRedisTemplate(7);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_8() {
        return initStringRedisTemplate(8);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_9() {
        return initStringRedisTemplate(9);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_10() {
        return initStringRedisTemplate(10);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_11() {
        return initStringRedisTemplate(11);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_12() {
        return initStringRedisTemplate(12);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_13() {
        return initStringRedisTemplate(13);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_14() {
        return initStringRedisTemplate(14);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate_15() {
        return initStringRedisTemplate(15);
    }

    private StringRedisTemplate initStringRedisTemplate(int index) {
        JedisConnectionFactory jedis = new JedisConnectionFactory();
        jedis.setHostName(jedisConnectionFactory.getHostName());
        jedis.setPort(jedisConnectionFactory.getPort());
        jedis.setDatabase(index);
        jedis.setPoolConfig(jedisConnectionFactory.getPoolConfig());
        jedis.afterPropertiesSet();
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(jedis);
        return template;
    }

}
