package com.eveb.saasops.modules.sys.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.eveb.saasops.api.modules.user.service.DepotOperatService;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.Constant;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.common.validator.Assert;
import com.eveb.saasops.common.validator.ValidatorUtils;
import com.eveb.saasops.common.validator.group.AddGroup;
import com.eveb.saasops.modules.base.controller.AbstractController;
import com.eveb.saasops.modules.sys.dao.SysUserDao;
import com.eveb.saasops.modules.sys.entity.SysUserEntity;
import com.eveb.saasops.modules.sys.entity.SysUserEntity.ErrorCode;
import com.eveb.saasops.modules.sys.service.SysUserRoleService;
import com.eveb.saasops.modules.sys.service.SysUserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

/**
 * 系统用户
 */
@RestController
@RequestMapping("/bkapi/sys/user")
@Api(value = "SysUserController", description = "系统用户")
@Slf4j
public class SysUserController extends AbstractController {
	@Autowired
	private SysUserService sysUserService;
	@Autowired
	private SysUserRoleService sysUserRoleService;
    @Autowired
	private SysUserDao sysUserDao;
    @Autowired
	private DepotOperatService depotOperatService;

	/**
	 * 所有用户列表
	 */
	@GetMapping("/list")
	@RequiresPermissions("sys:user:list")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	public R list(@ModelAttribute SysUserEntity userEntity){
		//只有超级管理员，才能查看所有管理员列表
		if(getUserId() != Constant.SUPER_ADMIN){
			userEntity.setCreateUserId(getUserId());
		}
		//查询列表数据
		PageUtils pageUtil = sysUserService.queryList(userEntity);
		return R.ok().put("page", pageUtil);
	}

	/**
	 * 所有用户列表
	 */
	@GetMapping("/queryConditions")
	@RequiresPermissions("sys:user:list")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	public R queryConditions(@ModelAttribute SysUserEntity userEntity){
		SysUserEntity sysUserEntity=getUser();
		log.info(sysUserEntity.toString());
		if(sysUserEntity == null ){
			return R.error(2000,"无法获取当前用户信息");
		}
        if(sysUserEntity.getRoleId() == null){
			sysUserEntity=sysUserDao.queryObject(sysUserEntity.getUserId());
		    //return R.error(2000,"无法获取当前用户角色信息");
        }
		//只有超级管理员，才能查看所有管理员列表
		if(sysUserEntity.getRoleId() != 1 && sysUserEntity.getUserId() != null && sysUserEntity.getUserId() != 1){
			userEntity.setCreateUserId(getUserId());
		}else {
			userEntity.setRoleId(1);
		}
		userEntity.setIsDelete(1);
		//查询列表数据
		//Query query = new Query(params);
		PageUtils pageUtil = sysUserService.queryConditions(userEntity);
		return R.ok().put("page", pageUtil);
	}

	/**
	 * 获取登录的用户信息
	 */
	@RequestMapping("/info")
	public R info(){
		SysUserEntity userEntity=getUser();
		userEntity.setMobile(null);
		userEntity.setPassword(null);
		userEntity.setSecurepwd(null);
		userEntity.setSalt(null);
		
		//排除admin
		if(1!=userEntity.getUserId()) {
			//在此做密码过期校验
			String expireFlag = sysUserDao.checkPasswordIsExpire(userEntity.getUserId());
			if("true".equals(expireFlag)) {
				return R.ok(402, userEntity);
			}
		}
		return R.ok().put("user", userEntity);
	}

	/**
	 * 认证安全密码
	 */
//	@SysLog("认证安全密码")
	@RequestMapping("/authsecpwd")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	public R authsecPwd(@RequestBody SysUserEntity userEntity)
	{
		Assert.isBlank(userEntity.getSecurepwd(), "安全密码不为能空!");
		int code = sysUserService.authsecPwd(getUserId(), userEntity.getSecurepwd());
		switch (code) {
		case ErrorCode.code_06:
			return R.error(2000,"安全密码错误");
		default:
			return R.ok();
		}
	}

	/**
	 * 修改登录用户密码
	 */
//	@SysLog("修改密码")
	@RequestMapping("/password")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	public R password(@RequestBody SysUserEntity userEntity){
		Assert.isNull(userEntity, "用户信息不能为空");
		Assert.isBlank(userEntity.getNewPassword(), "新密码不为能空");
		//sha256加密
		String password = new Sha256Hash(userEntity.getPassword(), getUser().getSalt()).toHex();
		//sha256加密
		String newPassword = new Sha256Hash(userEntity.getNewPassword(), getUser().getSalt()).toHex();
		//更新密码
		int code = sysUserService.updatePassword(getUserId(), password, newPassword);
		switch (code) {
		case ErrorCode.code_01:
			return R.error(2000,"登陆密码与原登陆密码相同,请重新输入!");
		case ErrorCode.code_02:
			return R.error(2000,"登陆密码与安全密码相同,请重新输入!");
		case ErrorCode.code_05:
			return R.error(2000,"原密码错误");
		default:
			return R.ok();
		}
	}
	/**
	 * 修改登录用户安全密码
	 */
//	@SysLog("修改安全密码")
	@RequestMapping("/secpassword")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	public R secpassword(@RequestBody SysUserEntity userEntity){
		Assert.isNull(userEntity, "参数对象不能为空");
		Assert.isBlank(userEntity.getNewPassword(), "新安全密码不为能空!");
		//sha256加密
		String secpassword = new Sha256Hash(userEntity.getSecurepwd(), getUser().getSalt()).toHex();
		//sha256加密
		String newPassword = new Sha256Hash(userEntity.getNewPassword(), getUser().getSalt()).toHex();
		//更新密码
		int code = sysUserService.updateSecPassword(getUserId(), secpassword, newPassword);
		switch (code) {
		case ErrorCode.code_03:
			return R.error(2000,"安全密码与原安全密码相同,请重新输入!");
		case ErrorCode.code_04:
			return R.error(2000,"安全密码与登陆密码相同,请重新输入!");
		case ErrorCode.code_05:
			return R.error(2000,"原登陆密码错误!");
		default:
			return R.ok();
		}
	}
	/**
	 * 用户信息
	 */
	@RequestMapping("/info/{userId}")
	@RequiresPermissions("sys:user:info")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	public R info(@PathVariable("userId") Long userId){
		SysUserEntity user = sysUserService.queryUserEntityOne(userId);
		//获取用户所属的角色列表
		List<Long> roleIdList = sysUserRoleService.queryRoleIdList(userId);
		if(roleIdList.size() == 1 ){
			user.setRoleId(Integer.valueOf(roleIdList.get(0).toString()));
		}
		user.setRoleIdList(roleIdList);
		user.setPassword("******");
		user.setSecurepwd("******");
		return R.ok().put("user", user);
	}

	/**
	 * 保存用户
	 */
//	@SysLog("保存用户")
	@RequestMapping("/save")
	@RequiresPermissions("agent:account:save")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	public R save(@RequestBody SysUserEntity user,HttpServletRequest request) {
		if(user.getRoleId() == 1 ){
			return R.error(2000,"不可赋予超级管理员权限");
		}
		ValidatorUtils.validateEntity(user, AddGroup.class);
		if (user.getPassword().equals(user.getSecurepwd())) {
			return R.error(2000,"密码与安全密码相同,请重新输入!");
		}
		user.setCreateUserId(getUserId());
		sysUserService.save(user);
		return R.ok();
	}

	@RequestMapping("/updateAuth")
	@RequiresPermissions("sys:user:save")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	public R updateAuth(@RequestBody SysUserEntity user,HttpServletRequest request){
		if(user.getRoleId() == 1 && user.getUserId() == 1l){
			return R.error(2000,"超级管理员不可修改,请联系客服");
		}
		//更新權限
		sysUserService.updateDataAuth(user, CommonUtil.getSiteCode());
		return R.ok();
	}
	/**
	 * 修改用户
	 */
//	@SysLog("修改用户")
	@RequestMapping("/update")
	@RequiresPermissions("sys:user:update")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	public R update(@RequestBody SysUserEntity user , HttpServletRequest request) {
		if(user.getRoleId() == 1 && user.getUserId() == 1l){
			return R.error(2000,"超级管理员不可修改,请联系客服");
		}
		if(user.getUserId() == getUserId()){
			return R.error(2000,"自己不能修改自己的数据权限");
		}
		user.setCreateUserId(getUserId());
		sysUserService.update(user);
			return R.ok();
	}

//	@SysLog("修改用户状态")
	@PostMapping("/updateEnable")
	@RequiresPermissions("sys:user:update")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	public R updateEnable( @RequestBody SysUserEntity user) {
		if(user.getRoleId() == 1 && user.getUserId() == 1l){
			return R.error(2000,"超级管理员不可修改,请联系客服");
		}
		sysUserService.updateEnable(user);
		return R.ok();
	}

	/**
	 * 删除用户
	 */
//	@SysLog("删除用户")
	@RequestMapping("/delete")
	@RequiresPermissions("sys:user:delete")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	public R delete(@RequestBody SysUserEntity user){
		if(user.getUserId() == 1l ){
			return R.error(2000,"系统管理员不能删除");
		}
		if(user.getUserId() == getUserId()){
			return R.error(2000,"当前用户不能删除");
		}
		sysUserRoleService.delete(user.getUserId());
		sysUserService.deleteCatchBatch(user.getUserId());
		return R.ok();
	}
	@GetMapping("/userAuth/{userId}")
	@ResponseBody
	@ApiOperation(value="获取角色数据权限", notes="获取角色数据权限")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	public R getUserAuth(@ApiParam @PathVariable("userId") Long userId,HttpServletRequest request){
		if(sysUserService.queryObject(userId).getRoleId() == 1 && userId == 1l){
			return R.error(2000,"超级管理员不可修改,请联系客服");
		}
		return R.ok().put("authority",sysUserService.getUserAuth(userId,CommonUtil.getSiteCode()));
	}

	@GetMapping("/depotLogOut")
	@ApiOperation(value = "第三方平台登出", notes = "第三方平台登出")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),@ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
	public R ptLogOut(@RequestParam("depotId") Integer depotId,@RequestParam("userId") Integer userId,HttpServletRequest request) {
		return depotOperatService.LoginOut(depotId,userId,CommonUtil.getSiteCode());
	}

}
