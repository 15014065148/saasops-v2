package com.eveb.saasops.modules.sys.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import com.eveb.saasops.modules.sys.entity.SysUserAgyaccountrelation;
import com.eveb.saasops.modules.sys.entity.SysUserEntity;
import com.eveb.saasops.modules.sys.entity.SysUserMbrgrouprelation;

/**
 * 系统用户
 */
@Component
@Mapper
public interface SysUserDao extends BaseDao<SysUserEntity> {
	
	/**
	 * 查询用户的所有权限
	 * @param userId  用户ID
	 */
	List<String> queryAllPerms(Long userId);
	
	/**
	 * 查询用户的所有菜单ID
	 */
	List<Long> queryAllMenuId(Long userId);
	
	/**
	 * 根据用户名，查询系统用户
	 */
	SysUserEntity queryByUserName(String username);
	
	/**
	 * 修改密码
	 */
	int updatePassword(Map<String, Object> map);

	/**
	 * 修改安全密码
	 */
	int updateSecPassword(Map<String, Object> map);

    List<SysUserEntity> queryConditions(SysUserEntity userEntity);

	/**
	 * 删除权限设置
	 * @param userId
	 */
	void deleteAuthority(@Param("userId") Long userId);

    List<SysUserAgyaccountrelation> getAuthAgy(@Param("userId") Long userId);
	List<SysUserMbrgrouprelation> getAuthMbr(@Param("userId") Long userId);
	void updateEnable(SysUserEntity user);
	void deleteSysUser(Long userId);

    SysUserEntity selectOne(@Param("userId")Long userId);
    
    String checkPasswordIsExpire(@Param("userId") Long userId);
}
