package com.eveb.saasops.config;


import static com.google.common.collect.Sets.newHashSet;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.util.StringUtils.isEmpty;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import com.eveb.saasops.api.annotation.CacheDuration;

public class SpringRedisCacheManager extends RedisCacheManager implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;

    public SpringRedisCacheManager(RedisOperations<?, ?> redisOperations) {
        super(redisOperations);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        parseCacheDuration(applicationContext);
    }

    private void parseCacheDuration(ApplicationContext applicationContext) {
        final Map<String, Long> cacheExpires = new HashMap<String, Long>();
        String[] beanNames = applicationContext.getBeanNamesForType(Object.class);
        for (String beanName : beanNames) {
            final Class<?> clazz = applicationContext.getType(beanName);
            //拦截所有@Service 中有加@CacheDuration的方法
            Service service = findAnnotation(clazz, Service.class);
            if (null == service) {
                continue;
            }
            //System.out.println(beanName);
            addCacheExpires(clazz, cacheExpires);
        }
        //设置有效期
        super.setExpires(cacheExpires);
    }

	private void addCacheExpires(final Class<?> clazz, final Map<String, Long> cacheExpires) {
		ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {
			@Override
			public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
				ReflectionUtils.makeAccessible(method);
				CacheDuration cacheDuration = findCacheDuration(clazz, method);
				if (null != cacheDuration) {
					Cacheable cacheable = findAnnotation(method, Cacheable.class);
					CacheConfig cacheConfig = findAnnotation(clazz, CacheConfig.class);
					Set<String> cacheNames = findCacheNames(cacheConfig, cacheable);
					for (String cacheName : cacheNames) {
						//System.out.println("方法名称="+cacheName+"  设置的时间="+cacheDuration.duration());
						cacheExpires.put(cacheName, cacheDuration.duration());
					}
				}
			}
		}, new ReflectionUtils.MethodFilter() {
			@Override
			public boolean matches(Method method) {
				return null != findAnnotation(method, Cacheable.class);
			}
		});
	}

    /**
     * CacheDuration标注的有效期，优先使用方法上标注的有效期
     *
     * @param clazz
     * @param method
     * @return
     */
    private CacheDuration findCacheDuration(Class<?> clazz, Method method) {
        CacheDuration methodCacheDuration = findAnnotation(method, CacheDuration.class);
        if (null != methodCacheDuration) {
            return methodCacheDuration;
        }

        CacheDuration classCacheDuration = findAnnotation(clazz, CacheDuration.class);
        if (null != classCacheDuration) {
            return classCacheDuration;
        }
        return null;
        //throw new IllegalStateException("No CacheDuration config on Class " + clazz.getName() + " and method " + method.toString());
    }

    private Set<String> findCacheNames(CacheConfig cacheConfig, Cacheable cacheable) {
        return isEmpty(cacheable.value()) ?
                newHashSet(cacheConfig.cacheNames()) : newHashSet(cacheable.value());
    }
}