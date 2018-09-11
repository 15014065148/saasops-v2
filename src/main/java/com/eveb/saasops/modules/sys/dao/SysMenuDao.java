package com.eveb.saasops.modules.sys.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import com.eveb.saasops.modules.sys.dto.ColumnAuthTreeDto;
import com.eveb.saasops.modules.sys.dto.TreeMenuDto2;
import com.eveb.saasops.modules.sys.entity.SysMenuEntity;
import com.eveb.saasops.modules.sys.entity.SysMenuTree;
import com.eveb.saasops.modules.sys.entity.SysRoleMenuEntity;

/**
 * 菜单管理
 */
@Component
@Mapper
public interface SysMenuDao extends BaseDao<SysMenuEntity> {
	
	/**
	 * 根据父菜单，查询子菜单
	 * @param parentId 父菜单ID
	 */
	List<SysMenuEntity> queryListParentId(Long parentId);
	
	/**
	 * 获取不包含按钮的菜单列表
	 */
	List<SysMenuEntity> queryNotButtonList();
	List<TreeMenuDto2> queryMenuList();
	
	/**
	 * 查询用户的权限列表
	 */
	List<SysMenuEntity> queryUserList(Long userId);

	List<SysMenuTree> selectTree(@Param("roleIds") String roleIds);

	List<SysMenuEntity> queryAllMenu();

    List<TreeMenuDto2> queryMenuAuth(@Param("menuId") Long menuId);

    List<SysMenuEntity> queryAll();

    List<SysMenuTree> selectRoleMenuTree(@Param("roleIds") String roles);
    
    List<SysRoleMenuEntity> findMenuByColumn(ColumnAuthTreeDto dto);
    
    List<TreeMenuDto2> getChildMenuList(@Param("menuId") Long menuId);
}
