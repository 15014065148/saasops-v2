package com.eveb.saasops.modules.sys.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eveb.saasops.common.utils.Constant;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.common.validator.ValidatorUtils;
import com.eveb.saasops.modules.base.controller.AbstractController;
import com.eveb.saasops.modules.sys.dto.TreeMenuDto;
import com.eveb.saasops.modules.sys.entity.SysRoleEntity;
import com.eveb.saasops.modules.sys.service.SysMenuService;
import com.eveb.saasops.modules.sys.service.SysRoleMenuService;
import com.eveb.saasops.modules.sys.service.SysRoleService;
import com.eveb.saasops.modules.sys.service.SysUserRoleService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 角色管理
 */
@RestController
@RequestMapping("/bkapi/sys/role")
public class SysRoleController extends AbstractController {
	@Autowired
	private SysRoleService sysRoleService;
	@Autowired
	private SysRoleMenuService sysRoleMenuService;
	@Autowired
	private SysMenuService sysMenuService;
	@Autowired
	private SysUserRoleService sysUserRoleService;
	
	/**
	 * 角色列表
	 */
	@GetMapping("/list")
	@RequiresPermissions("sys:role:list")
	public R list(@ModelAttribute SysRoleEntity roleEntity){
		//如果不是超级管理员，则只查询自己创建的角色列表
		if(getUserId() != Constant.SUPER_ADMIN){
			roleEntity.setCreateUser(getUser().getRoleName());
		}
		//查询列表数据
		PageUtils p = sysRoleService.queryListPage(roleEntity);
		return R.ok().put("page", p);
	}
    /**
     * 角色列表
     */
	@GetMapping("/listAll")
    @RequiresPermissions("sys:role:list")
    public R listAll(){
        //查询列表数据
        List<SysRoleEntity> list = sysRoleService.queryList(null);
        return R.ok().put("list", list);
    }

    @GetMapping("/queryConditions")
	@RequiresPermissions("sys:role:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	public R queryConditions(@ModelAttribute SysRoleEntity roleEntity, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize){
		//如果不是超级管理员，则只查询自己创建的角色列表
		if(getUserId() != Constant.SUPER_ADMIN){
			roleEntity.setCreateUserId(getUserId());
		}
		//查询列表数据
		//Query query = new Query(params);
		PageUtils p = sysRoleService.queryConditions(roleEntity);
		return R.ok().put("page", p);
	}
	
	/**
	 * 角色列表
	 */
	@GetMapping("/select")
	@RequiresPermissions("sys:role:select")
	public R select(){
		SysRoleEntity roleEntity = new SysRoleEntity();
		//如果不是超级管理员，则只查询自己所拥有的角色列表
		if(getUserId() != Constant.SUPER_ADMIN){
			roleEntity.setCreateUserId(getUserId());
		}
		roleEntity.setIsEnable(1);
		List<SysRoleEntity> list = sysRoleService.queryList(roleEntity);
		return R.ok().put("list", list);
	}
	
	/**
	 * 角色信息
	 */
	@GetMapping("/info/{roleId}")
	@RequiresPermissions("sys:role:info")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	public R info(@PathVariable("roleId") Long roleId){
		SysRoleEntity role = sysRoleService.queryObject(roleId);
		//查询角色对应的菜单
		List<Long> menuIdList = sysRoleMenuService.queryMenuIdList(roleId);		
		role.setMenuIdList(menuIdList);
		List<TreeMenuDto> treeMenuList = sysMenuService.queryMenuList(menuIdList);
		role.setTreeMenuList(treeMenuList);
		return R.ok().put("role", role);
	}


	/**
	 * 保存角色
	 */
//	@SysLog("保存角色")
	@PostMapping("/saveRole")
	@RequiresPermissions("sys:role:save")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	@Deprecated
	public R saveRole(@RequestBody SysRoleEntity role){
		ValidatorUtils.validateEntity(role);
		if(sysRoleService.queryCountByRoleName(role.getRoleName()) >0 ){
			return R.error(2000,"该角色已存在");
		}
		role.setCreateUserId(getUserId());
		role.setCreateUser(getUser().getUsername());
		sysRoleService.save(role);
		return R.ok();
	}


	/**
	 * 修改角色
	 */
//	@SysLog("修改角色")
	@PostMapping("/update")
	@RequiresPermissions("sys:role:update")
	@Deprecated
	public R update(@RequestBody SysRoleEntity role){
		if(role.getRoleId() == Constant.SUPER_ADMIN){
			return R.error(2000,"超级管理员不可修改,请联系客服");
		}
		role.setCreateUserId(getUserId());
		sysRoleService.update(role);
		sysRoleMenuService.saveOrUpdate(role.getRoleId(), role.getMenuIdList());
		return R.ok();
	}
    /**
     * 修改角色
     */
//    @SysLog("修改角色是否启用")
    @PostMapping("/updateEnable")
    @RequiresPermissions("sys:role:update")
    public R updateEnable(@RequestBody SysRoleEntity role){
        sysRoleService.updateRoleEnable(role);
        return R.ok();
    }
	
	/**
	 * 删除角色
	 */
//	@SysLog("删除角色")
	@PostMapping("/delete")
	@RequiresPermissions("sys:role:delete")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
	,@ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
	public R delete(@RequestBody SysRoleEntity role){
		//判断角色下是否有用户
		for (Long roleId : role.getRoleIds()) {
			if(sysUserRoleService.countRoleUsers(roleId) != 0) {
				return R.error(2000,"该角色下还有用户,不可删除");
			}
			sysRoleMenuService.deleteByRoleId(roleId);
		}
		sysRoleService.deleteBatch(role.getRoleIds());
		return R.ok();
	}
	/**
	 * 导出报表
	 * @param params
	 * @param response
	 */
	@GetMapping("/ExportExcel")
	@RequiresPermissions("system:systemdomain:save")
	@ApiOperation(value="导出报表", notes="导出报表")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	public void ExportExcel(@ModelAttribute SysRoleEntity roleEntity, HttpServletResponse response){
		//如果不是超级管理员，则只查询自己创建的角色列表
		if(getUserId() != Constant.SUPER_ADMIN){
			roleEntity.setCreateUserId(getUserId());
		}
		//查询列表数据
		//Query query = new Query(params);
		sysRoleService.exportExcel(roleEntity,response);
	}
}
