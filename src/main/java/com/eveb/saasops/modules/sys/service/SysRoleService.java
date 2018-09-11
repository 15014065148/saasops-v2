package com.eveb.saasops.modules.sys.service;

import com.eveb.saasops.common.utils.*;
import com.eveb.saasops.modules.sys.dao.SysRoleDao;
import com.eveb.saasops.modules.sys.dto.SysRoleEntities;
import com.eveb.saasops.modules.sys.entity.SysRoleEntity;
import com.eveb.saasops.modules.sys.entity.SysUserEntity;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * 角色
 */
@Service("sysRoleService")
public class SysRoleService{
	@Autowired
	private SysRoleDao sysRoleDao;
	@Autowired
	private SysRoleMenuService sysRoleMenuService;
	@Autowired
	private SysUserService sysUserService;

	@Value("${sys.authority.excel.path}")
	private String sysAuthorityExcelPath;

	@Resource(name = "stringRedisTemplate_0")
	private RedisTemplate redisTemplate;

	public SysRoleEntity queryObject(Long roleId) {
		return sysRoleDao.queryObject(roleId);
	}

	public PageUtils queryListPage(SysRoleEntity roleEntity) {
        PageHelper.startPage(roleEntity.getPageNo(), roleEntity.getPageSize());
        if (!StringUtils.isEmpty(roleEntity.getOrder()))
            PageHelper.orderBy(roleEntity.getOrder());
        List<SysRoleEntity> list = sysRoleDao.queryList(roleEntity);
        PageUtils p = BeanUtil.toPagedResult(list);
		return p;
	}
	
	public List<SysRoleEntity> queryList(SysRoleEntity roleEntity) {
        List<SysRoleEntity> list = sysRoleDao.queryList(roleEntity);
		return list;
	}

	public int queryTotal(SysRoleEntity roleEntity) {
		return sysRoleDao.queryTotal(roleEntity);
	}

	@Transactional
	@Deprecated
	public void save(SysRoleEntity role) {
		role.setCreateTime(new Date());
		sysRoleDao.save(role);
		
		//检查权限是否越权
		checkPrems(role);
		
		//保存角色与菜单关系
		sysRoleMenuService.saveOrUpdate(role.getRoleId(), role.getMenuIdList());
	}

	@Transactional
	@CacheEvict(value = "SysRoleMenuTree", key = "#siteCode")
	public R save(SysRoleEntities roles,SysUserEntity user,String siteCode) {
		SysRoleEntity role = roles.getSysRole();
		//检查权限是否越权
		List<Long> roleMenuIds = new ArrayList<>();
		roles.getSysRoleMenuEntities().forEach(roleMenu -> {
			roleMenuIds.add(roleMenu.getMenuId());
		});
		role.setMenuIdList(roleMenuIds);
		checkPrems(role);
		if(role.getRoleId() ==null ) {
			role.setCreateTime(new Date());
			sysRoleDao.save(role);
			Map<String,Object> map = new HashMap<>();
			map.put("roleName",role.getRoleName());
			roles.setSysRole(sysRoleDao.queryList(map).get(0));
		}else {
			sysRoleDao.update(role);
		}
		//保存角色与菜单关系
		return sysRoleMenuService.saveOrUpdate2(roles.getSysRoleMenuEntities(),role.getRoleId());
	}

	@Transactional
	public void update(SysRoleEntity role) {
		sysRoleDao.update(role);
		//检查权限是否越权
		checkPrems(role);
		//更新角色与菜单关系
		sysRoleMenuService.saveOrUpdate(role.getRoleId(), role.getMenuIdList());
	}

	public void updateRoleEnable(SysRoleEntity role) {
		sysRoleDao.update(role);
	}

	@Transactional
	public void deleteBatch(Long[] roleIds) {
		sysRoleDao.deleteBatch(roleIds);
		Arrays.stream(roleIds).forEach(roleId ->{
			redisTemplate.delete("SaasopsV2:SysRoleMenuTree" + CommonUtil.getSiteCode() + ":" + roleId);
		});
	}
	
	public List<Long> queryRoleIdList(String createUser) {
		return sysRoleDao.queryRoleIdList(createUser);
	}

	public PageUtils queryConditions(SysRoleEntity roleEntity) {
		PageHelper.startPage(roleEntity.getPageNo(), roleEntity.getPageSize());
        if (!StringUtils.isEmpty(roleEntity.getOrder()))
            PageHelper.orderBy(roleEntity.getOrder());
        List<SysRoleEntity> list = sysRoleDao.queryConditions(roleEntity);
        PageUtils p = BeanUtil.toPagedResult(list);
		return p;
	}

	public void exportExcel(SysRoleEntity roleEntity, HttpServletResponse response) {
		String fileName = "角色权限" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		List<Map<String, Object>> list = Lists.newLinkedList();
		sysRoleDao.queryConditions(roleEntity).stream().forEach(
				cs->{
					Map<String, Object> param = new HashMap<>();
					param.put("role_name", cs.getRoleName());
					param.put("role_nickName", cs.getRoleNickName());
					param.put("remark", cs.getRemark());
					param.put("userNum", cs.getUserNum());
					param.put("isEnable", "1".equals(cs.getIsEnable())?"启用 ":"禁用");
					param.put("createUser", cs.getCreateUser());
					param.put("create_time", cs.getCreateTime());
					list.add(param);
				}
		);
		Workbook workbook = ExcelUtil.commonExcelExportList("mapList", sysAuthorityExcelPath, list);
		try {
			ExcelUtil.writeExcel(response, workbook, fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 检查权限是否越权
	 */
	private void checkPrems(SysRoleEntity role){
		//如果不是超级管理员，则需要判断角色的权限是否超过自己的权限
		if(role.getCreateUserId().equals(Constant.SUPER_ADMIN)){
			return ;
		}
		
		//查询用户所拥有的菜单列表
		//List<Long> menuIdList = sysUserService.queryAllMenuId(role.getCreateUserId());
		
		//判断是否越权
		/*if(!menuIdList.containsAll(role.getMenuIdList())){
			throw new RRException("新增角色的权限，已超出你的权限范围");
		}*/
	}

	public SysRoleEntity queryByRoleName(String roleName) {
		List<SysRoleEntity> sysRoleEntities=sysRoleDao.queryByRolename(roleName);
		return sysRoleEntities.size() >= 1?sysRoleEntities.get(0):null;
	}

	public int queryCountByRoleName(String roleName){
		return sysRoleDao.queryByRolename(roleName).size();

	}

	
}
