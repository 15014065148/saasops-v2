package com.eveb.saasops.modules.system.sysconfig.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: Miracle
 * @Description:
 * @Date: 16:20 2017/12/14
 **/
@Mapper
public interface SysMapper {

    List<String> selectProxys(@Param("groups") String groups,@Param("keys") String keys);
}
