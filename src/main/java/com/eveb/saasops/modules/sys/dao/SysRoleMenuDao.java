package com.eveb.saasops.modules.sys.dao;

import com.eveb.saasops.modules.sys.entity.SysRoleMenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 角色与菜单对应关系
 */
@Component
@Mapper
public interface SysRoleMenuDao extends BaseDao<SysRoleMenuEntity> {

	void saveBatch(List<SysRoleMenuEntity> sysRoleMenuEntities);
	/**
	 * 根据角色ID，获取菜单ID列表
	 */
	List<Long> queryMenuIdList(Long roleId);


    List<SysRoleMenuEntity> queryMenuList(@Param("roleId") Long roleId);

    List<SysRoleMenuEntity> queryMenuAuthList(Long roleId);

    Integer findSysRoleMenuByRoleIdAndMenuId(
    		@Param("roleId") Integer roleId,
			@Param("menuId") Long menuId);
}
