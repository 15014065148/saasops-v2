package com.eveb.saasops.modules.member.controller;

import javax.validation.constraints.NotNull;

import com.eveb.saasops.common.annotation.SysLog;
import com.eveb.saasops.common.utils.CommonUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.eveb.saasops.common.constants.Constants.Available;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.common.validator.Assert;
import com.eveb.saasops.modules.base.controller.AbstractController;
import com.eveb.saasops.modules.base.entity.BaseAuth;
import com.eveb.saasops.modules.member.entity.MbrGroup;
import com.eveb.saasops.modules.member.service.MbrGroupService;
import com.eveb.saasops.modules.sys.entity.SysUserMbrgrouprelation;
import com.eveb.saasops.modules.sys.service.SysUserMbrgrouprelationService;
import com.eveb.saasops.modules.sys.service.SysUserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/bkapi/member/mbrgroup")
@Api(value = "MbrGroup", description = "会员组")
public class MbrGroupController extends AbstractController {

	@Autowired
	private MbrGroupService mbrGroupService;
	@Autowired
	private SysUserMbrgrouprelationService sysUserMbrgrouprelationService;
	@Autowired
	private SysUserService sysUserService;

	/**
	 * 查询所启用会员组
	 * 
	 */
	@GetMapping("listAll")
	@ResponseBody
	@ApiOperation(value = "会员组-所有已启用的会员组信息,存在权限", notes = "查询只有启用的会员组所有信息,存在权限")
	public R findGroupAll() {
		MbrGroup mbrGroup = new MbrGroup();
		mbrGroup.setAvailable(Available.enable);
		mbrGroup.setBaseAuth(mbrGroupService.getRowAuth());
		return R.ok().put("page", mbrGroupService.queryListCondInAuth(mbrGroup));
	}

	@GetMapping("findGroupAllNoAuth")
	@ResponseBody
	@ApiOperation(value = "会员组-所有已启用的会员组信息,不存在权限", notes = "查询只有启用的会员组所有信息,不存在权限")
	public R findGroupAllNoAuth() {
		MbrGroup mbrGroup = new MbrGroup();
		mbrGroup.setAvailable(Available.enable);
		mbrGroup.setBaseAuth(new BaseAuth());
		return R.ok().put("page", mbrGroupService.queryListCondInAuth(mbrGroup));
	}

	/**
	 * 列表required
	 */
	@GetMapping("/list")
	@RequiresPermissions("member:mbrgroup:list")
	@ApiOperation(value = "会员组-根据当前页和每页笔数列表显示会员信息", notes = "查询所有会员组信息,并分页")
	public R list(@ModelAttribute MbrGroup mbrGroup, @RequestParam("pageNo") @NotNull Integer pageNo,
			@RequestParam("pageSize") @NotNull Integer pageSize,
			@RequestParam(value = "orderBy", required = false) String orderBy) {
		PageUtils utils = mbrGroupService.queryListPage(mbrGroup, pageNo, pageSize, orderBy, getUserId());
		return R.ok().put("page", utils);
	}

	/**
	 * 信息
	 */
	@GetMapping("/info/{id}")
	@RequiresPermissions("member:mbrgroup:info")
	@ApiOperation(value = "会员组-单笔会员信息", notes = "根据会员组Id,显示会员组明细信息")
	public R info(@PathVariable("id") Integer id) {
		MbrGroup mbrGroup = mbrGroupService.queryObject(id);
		if(mbrGroup==null)
			mbrGroup=new MbrGroup();
		return R.ok().put("mbrGroup", mbrGroup);
	}

	/**
	 * 保存
	 */
	@PostMapping("/save")
	@ApiOperation(value = "会员组-保存", notes = "保存一条会员组明细信息到数据库")
	@RequiresPermissions("member:mbrgroup:save")
	@SysLog(module = "会员组模块",methodText = "保存会员组")
	@Transactional
	public R save(@RequestBody MbrGroup mbrGroup) {
		verifyMbrGroup(mbrGroup);
		mbrGroup.setAvailable(Available.disable);
		mbrGroupService.save(mbrGroup);
		// 添加会员组权限到用户权限
		Long userId = super.getUserId();
		SysUserMbrgrouprelation sysUserMbrgrouprelation = new SysUserMbrgrouprelation(mbrGroup.getId(), userId);
		sysUserMbrgrouprelationService.save(sysUserMbrgrouprelation);
		sysUserService.deleteAuthorityCache(userId, CommonUtil.getSiteCode());
		return R.ok().put("groupId", mbrGroup.getId());
	}

	@GetMapping("/queryListByUserId")
	@ApiOperation(value = "用户会员组权限", notes = "用户会员组权限")
	@RequiresPermissions("member:mbrgroup:info")
	public R queryListByUserId(@RequestParam("userId") Long userId) {
		return R.ok().put("data", sysUserMbrgrouprelationService.queryListByUserId(userId));
	}

	/**
	 * 修改
	 */
	@PostMapping("/update")
	@ApiOperation(value = "会员组-更新", notes = "更新一条会员组明细信息到数据库")
	@RequiresPermissions("member:mbrgroup:update")
	@SysLog(module = "会员组模块",methodText = "更新会员组")
	public R update(@RequestBody MbrGroup mbrGroup) {
		Assert.isNull(mbrGroup.getId(), "会员组id不能为空!");
		verifyMbrGroup(mbrGroup);
		mbrGroup.setAvailable(null);
		mbrGroupService.update(mbrGroup);
		return R.ok();
	}

	/**
	 * 删除
	 */
	@PostMapping("/delete")
	@ApiOperation(value = "会员组-删除", notes = "根据会员组Id删除会员组并删除存款,取款条件信息")
	@SysLog(module = "会员组模块",methodText = "删除员组")
	@RequiresPermissions("member:mbrgroup:delete")
	public R deleteBatch(@RequestBody MbrGroup mbrGroup) {
		mbrGroupService.deleteBatch(mbrGroup.getIds());
		return R.ok();
	}

	/**
	 * 修改组状态
	 *
	 * @return
	 */
	@PostMapping("/updateAvailable")
	@ApiOperation(value = "会员组-更新状态", notes = "根据会员组Id更新一条会员组状态")
	@RequiresPermissions("member:mbrgroup:update")
	public R updateAvailable(@RequestBody MbrGroup mbrGroup) {
		Assert.isNull(mbrGroup.getId(), "会员组不能为空!");
		Assert.isNull(mbrGroup.getAvailable(), "会员组状态不能为空!");
		Integer mod = mbrGroupService.updateGroupAvil(mbrGroup.getId(), mbrGroup.getAvailable());
		if (mod == 0)
			throw new RRException("不能修改状态!");
		return R.ok();
	}

	private void verifyMbrGroup(MbrGroup mbrGroup) {
		Assert.isBlank(mbrGroup.getGroupName(), "会员组名不能为空!");
		Assert.isLenght(mbrGroup.getGroupName(), "会员组名最大长度为16位!", 1, 16);
		Assert.isLenght(mbrGroup.getMemo(), "会员组备注最大长度为200个字符", 0, 200);
	}
}
