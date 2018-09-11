package com.eveb.saasops.modules.sys.service;

import com.eveb.saasops.common.utils.Collections3;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.modules.sys.dao.SysMenuDao;
import com.eveb.saasops.modules.sys.dao.SysRoleMenuDao;
import com.eveb.saasops.modules.sys.dto.ColumnAuthTreeDto;
import com.eveb.saasops.modules.sys.entity.SysRoleMenuEntity;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 角色与菜单对应关系
 */
@Service("sysRoleMenuService")
public class SysRoleMenuService{
	@Autowired
	private SysRoleMenuDao sysRoleMenuDao;
	@Autowired
	private SysMenuService sysMenuService;
	@Autowired
	private SysMenuDao sysMenuDao;

	/**
	 *
	 * @param roleId
	 * @param menuIdList
	 */
	@Transactional
	@Deprecated
	public void saveOrUpdate(Long roleId, List<Long> menuIdList) {
		// 先删除角色与菜单关系
		sysRoleMenuDao.delete(roleId);
		if (menuIdList == null || menuIdList.size() == 0) {
			return;
		}

		// 保存角色与菜单关系
		Map<String, Object> map = new HashMap<>();
		map.put("roleId", roleId);
		map.put("menuIdList", menuIdList);
		sysRoleMenuDao.save(map);
		sysMenuService.deleteTreeByRole(roleId.toString(),CommonUtil.getSiteCode());
	}

	public List<Long> queryMenuIdList(Long roleId) {
		return sysRoleMenuDao.queryMenuIdList(roleId);
	}

	public void deleteByRoleId(Long roleId) {
		sysRoleMenuDao.delete(roleId);
	}

	public List<SysRoleMenuEntity> queryRoleMenu(Long roleId) {
		return sysRoleMenuDao.queryMenuList(roleId);
	}

	public List<SysRoleMenuEntity> queryRoleMenuAuth(Long roleId) {
		return sysRoleMenuDao.queryMenuAuthList(roleId);
	}

	@Transactional
	public R saveOrUpdate2(List<SysRoleMenuEntity> sysRoleMenuEntities,Long roleId) {
		sysRoleMenuDao.delete(roleId);
		sysRoleMenuEntities.forEach(e -> {
			e.setRoleId(roleId);
		});
		//排除重复  
		List<SysRoleMenuEntity> repeatList = getNoRepeatList(sysRoleMenuEntities);
		syscSaveRelationColumn(repeatList);
		sysRoleMenuDao.saveBatch(repeatList);
		//删除角色缓存
		sysMenuService.deleteTreeByRole(roleId.toString(),CommonUtil.getSiteCode());
		return R.ok().put("保存成功");
	}
	
	public void syscSaveRelationColumn(List<SysRoleMenuEntity> repeatList) {
		Set<Long> set = new HashSet<>();
		ColumnAuthTreeDto dto = new ColumnAuthTreeDto();
		for (SysRoleMenuEntity sysRoleMenuEntity : repeatList) {
			set.add(sysRoleMenuEntity.getMenuId());
		}
		dto.setParamList(set);
		//通过menuId查询columnName相等得数据
		List<SysRoleMenuEntity> roleMenuList = sysMenuDao.findMenuByColumn(dto);
		if(Collections3.isEmpty(roleMenuList)) {
			return;
		}
		for (SysRoleMenuEntity sysRoleMenuEntity : roleMenuList) {
			sysRoleMenuEntity.setRoleId(repeatList.get(0).getRoleId());
		}
		sysRoleMenuDao.saveBatch(roleMenuList);
	}
	
	/** 
     * 去除List内复杂字段重复对象 
     * @author : lebron 
     * @param oldList 
     * @return 
     */  
    public List<SysRoleMenuEntity> getNoRepeatList(List<SysRoleMenuEntity> oldList){  
        List<SysRoleMenuEntity> list = new ArrayList<>();  
        if(CollectionUtils.isNotEmpty(oldList)){  
            for (SysRoleMenuEntity roleMenuEntity : oldList) {  
                //list去重复，内部重写equals  
                if(!list.contains(roleMenuEntity)){  
                    list.add(roleMenuEntity);  
                }  
            }  
        }
        return list;          
    }   

}
