package com.eveb.saasops.api.modules.user.controller;

import com.eveb.saasops.api.modules.user.service.RedisDelService;
import com.eveb.saasops.common.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/redis")
@Api(value = "删除Redis的KEY", description = "删除Redis的KEY")
public class RedisDelController {
    @Autowired
    private RedisDelService redisDelService;

    @GetMapping("/redisCache")
    @ApiOperation(value = "删除Redis的KEY", notes = "删除Redis的KEY")
    public R redisDelete() {
        redisDelService.redisCache();
        return R.ok();
    }
}
