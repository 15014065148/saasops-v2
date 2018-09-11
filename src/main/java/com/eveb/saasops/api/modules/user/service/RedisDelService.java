package com.eveb.saasops.api.modules.user.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RedisDelService {
    @Resource(name = "redisTemplate")
    RedisTemplate<String, Object> redisTemplate;

    /**
     * 使用redis模糊清除缓存
     */
    public void redisCache() {
        redisTemplate.delete(redisTemplate.keys("SaasopsV2:gameSiteCodeCache:*" + "*"));
        redisTemplate.delete(redisTemplate.keys("SaasopsV2:gameCompanyCache:*" + "*"));
        redisTemplate.delete(redisTemplate.keys("SaasopsV2:gameApiCache:*" + "*"));
        redisTemplate.delete(redisTemplate.keys("SaasopsV2:SysRoleMenuTree:*" + "*"));
        redisTemplate.delete(redisTemplate.keys("SaasopsV2:loginPngTokenCache:*" + "*"));
        redisTemplate.delete(redisTemplate.keys("SaasopsV2:redisProxyCache:*" + "*"));
        redisTemplate.delete(redisTemplate.keys("SaasopsV2:loginPt2TokenCache:*" + "*"));
        redisTemplate.delete(redisTemplate.keys("SaasopsV2:loginNtTokenCache:*" + "*"));
        redisTemplate.delete(redisTemplate.keys("SaasopsV2:loginPngTokenCache:*" + "*"));
    }
}
