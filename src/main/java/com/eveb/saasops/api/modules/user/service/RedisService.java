package com.eveb.saasops.api.modules.user.service;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.eveb.saasops.api.constants.ApiConstants;


@Service
public class RedisService {
    @Resource(name = "redisTemplate")
    RedisTemplate<String, Object> redisTemplate;


    public void setRedisExpiredTime(String key, Object var, int expiredTime, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, var, expiredTime, timeUnit.SECONDS);
    }

    public void setRedisValue(String key, Object var) {
        redisTemplate.opsForValue().set(key, var);
    }

    public Boolean booleanRedis(String key) {
        if (Objects.isNull(redisTemplate.opsForValue().get(key))) return Boolean.TRUE;
        return Boolean.FALSE;
    }

    public Object getRedisValus(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public long del(final String... keys) {
        return (Long) redisTemplate.execute((RedisCallback<?>) connection -> {
            long result = 0;
            for (int i = 0; i < keys.length; i++) {
                result = connection.del(keys[i].getBytes());
            }
            return result;
        });
    }

    public void set(final byte[] key, final byte[] value, final long liveTime) {
        redisTemplate.execute((RedisCallback<?>) connection -> {
            connection.set(key, value);
            if (liveTime > 0) {
                connection.expire(key, liveTime);
            }
            return 1L;
        });
    }

    public void set(String key, String value, long liveTime) {
        this.set(key.getBytes(), value.getBytes(), liveTime);
    }

    public void set(String key, String value) {
        this.set(key, value, 0L);
    }

    public void set(byte[] key, byte[] value) {
        this.set(key, value, 0L);
    }

    public String get(final String key) {
        return (String) redisTemplate.execute((RedisCallback<?>) connection -> {
            try {
                return new String(connection.get(key.getBytes()), ApiConstants.REDIS_CODE_KEY);
            } catch (Exception e) {
            }
            return "";
        });
    }

    public Set<String> Setkeys(String pattern) {
        return redisTemplate.keys(pattern);

    }

    public boolean exists(final String key) {
        return (Boolean) redisTemplate.execute((RedisCallback<?>) connection -> connection.exists(key.getBytes()));
    }

    public boolean flushDB() {
        return (Boolean) redisTemplate.execute((RedisCallback<?>) connection -> {
            connection.flushDb();
            return Boolean.TRUE;
        });
    }

    public long dbSize() {
        return (Long) redisTemplate.execute((RedisCallback<?>) connection -> connection.dbSize());
    }

    public String ping() {
        return (String) redisTemplate.execute((RedisCallback<?>) connection -> connection.ping());
    }
	
/*	public boolean add(final T entity,final String key) {
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection)
                    throws DataAccessException {
                ValueOperations<String, Object> valueOper = redisTemplate.opsForValue();
                valueOper.set(key, entity);
                return true;
            }
        }, false, true);
        return result;
    }*/
	
/*    public boolean update(final T entity,final String key) {
        if (get(key) == null) {
            throw new NullPointerException("数据行不存在, key = " + key);
        }
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection)
                    throws DataAccessException {
                ValueOperations<String, Object> valueOper = redisTemplate.opsForValue();
                valueOper.set(key, entity);
                return true;
            }
        });
        return result;
    }*/


}
