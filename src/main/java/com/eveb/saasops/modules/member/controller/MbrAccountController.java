package com.eveb.saasops.modules.member.controller;

import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eveb.saasops.common.annotation.SysLog;
import com.eveb.saasops.common.constants.Constants.Available;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.common.validator.Assert;
import com.eveb.saasops.common.validator.ValidRegUtils;
import com.eveb.saasops.listener.BizEvent;
import com.eveb.saasops.listener.BizEventType;
import com.eveb.saasops.modules.analysis.service.AnalysisService;
import com.eveb.saasops.modules.base.controller.AbstractController;
import com.eveb.saasops.modules.member.entity.MbrAccount;
import com.eveb.saasops.modules.member.entity.MbrAccountOnline;
import com.eveb.saasops.modules.member.entity.MbrBankcard;
import com.eveb.saasops.modules.member.service.MbrAccountService;
import com.eveb.saasops.modules.member.service.MbrBankcardService;
import com.eveb.saasops.modules.system.systemsetting.dto.StationSet;
import com.eveb.saasops.modules.system.systemsetting.entity.SysSetting;
import com.eveb.saasops.modules.system.systemsetting.service.SysSettingService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/bkapi/member/mbraccount")
@Api(value = "MbrAccount", description = "会员信息")
public class MbrAccountController  extends AbstractController {

	@Autowired
	private MbrAccountService mbrAccountService;
	@Autowired
	private MbrBankcardService mbrBankcardService;
	@Autowired
	private SysSettingService sysSettingService;
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	@Autowired
	private AnalysisService analysisService;

	@GetMapping("/list")
	@RequiresPermissions("member:mbraccount:list")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	@ApiOperation(value = "会员信息-列表", notes = "根据当前页及每页笔数显示")
	public R list(@ModelAttribute MbrAccount mbrAccount, @RequestParam("pageNo") @NotNull Integer pageNo,
			@RequestParam("pageSize") @NotNull Integer pageSize,@RequestParam(value="orderBy",required=false) String orderBy) {
		PageUtils page=mbrAccountService.queryListPage(mbrAccount, pageNo, pageSize,orderBy, getUser().getRoleId());
		return R.ok().put("page", page);
	}
	
	@GetMapping("/seachList")
	@RequiresPermissions("member:mbraccount:list")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	@ApiOperation(value = "查询会员列表展示列", notes = "根据当前页及每页笔数显示")
	public R seachList(@RequestParam("typeId") Long typeId) {
		Assert.isNull(typeId, "参数不能为空");
		return R.ok().put("items", mbrAccountService.querySeachCondition(getUser().getRoleId(), typeId));
	}
	
	@GetMapping("/columnFrameList")
	@RequiresPermissions("member:mbraccount:list")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	@ApiOperation(value = "查询会员列表展示列", notes = "根据当前页及每页笔数显示")
	public R columnList() {
		return R.ok().put("items", mbrAccountService.columnFrameList(getUser().getRoleId()));
	}

	@GetMapping("/listOnline")
	@RequiresPermissions("member:mbraccount:listOnline")
	@ApiOperation(value = "在线会员信息-列表", notes = "根据当前页及每页笔数显示")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	public R listOnline(@ModelAttribute  MbrAccountOnline mbrAccountOnline,
			@RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize,@RequestParam(value="orderBy",required=false) String orderBy) {
		mbrAccountOnline.setIsOnline(Available.enable);
		return R.ok().put("page", mbrAccountService.queryListOnlinePage(mbrAccountOnline, pageNo, pageSize,orderBy));
	}


	@GetMapping("/view/{id}")
	@RequiresPermissions("member:mbraccount:info")
	@ApiOperation(value = "会员信息-详细信息", notes = "根据会员Id,显示会员详细信息(包含 登陆信息、会员资料 其他资料等)")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
	public R view(@PathVariable("id") Integer id) {
		MbrAccount mbrAccount = mbrAccountService.queryObject(id);
		if(Objects.isNull(mbrAccount)) {
			return R.ok().put("betTotal", null)
					.put("fundTotal", null);
		}
		return R.ok().put("betTotal", analysisService.getMbrBetListReport(
		        CommonUtil.getSiteCode(), mbrAccount.getLoginName(), null,null,null,getUser().getRoleId()))
				.put("fundTotal",mbrAccountService.findMbrTotal(mbrAccount.getId(), getUser().getRoleId()));
	}

    @GetMapping("/viewAccountInfo/{id}")
    @RequiresPermissions("member:mbraccount:info")
    @ApiOperation(value = "会员信息-会员资料", notes = "会员信息-会员资料")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R viewAccountInfo(@PathVariable("id") Integer id) {
        MbrAccount mbrAccount = mbrAccountService.viewAccount(getUser().getRoleId(),getUserId(), id);
        if(!Objects.isNull(mbrAccount)) {
        	StationSet stationSet = sysSettingService.queryStationSet();
            mbrAccount.setWebsiteTitle(stationSet.getWebsiteTitle());
        }
        return R.ok().put("mbr", mbrAccount).put("mbrcard",
				Optional.ofNullable(mbrBankcardService.findMemberCardOne(id)).orElse(new MbrBankcard()));
    }


    @GetMapping("/viewOther/{id}")
	@RequiresPermissions("member:mbraccount:info")
	@ApiOperation(value = "会员信息-详细信息", notes = "根据会员Id,显示会员详细信息(包含 登陆信息IP、注册信息、银行卡信息)")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	public R viewOther(@PathVariable("id") Integer id) {
    	MbrAccount mbrAccount = mbrAccountService.viewOtherAccount(getUser().getRoleId(),getUserId(), id);
		return R.ok().put("other", mbrAccount);
	}
    
	/*@GetMapping("/viewOther/{id}")
	@RequiresPermissions("member:mbraccount:info")
	@ApiOperation(value = "会员信息-详细信息", notes = "根据会员Id,显示会员详细信息(包含 登陆信息IP、注册信息、银行卡信息)")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	public R viewOther(@PathVariable("id") Integer id) {
		MbrAccount mbrAccount = mbrAccountService.queryObject(id);
		LogMbrRegister logMbrRegister = new LogMbrRegister();
		logMbrRegister.setLoginName(mbrAccount.getLoginName());
		logMbrRegister = LogMbrregisterService.selectOne(logMbrRegister);
		LogMbrLogin logMbrLogin = logMbrloginService.findLogMemberLoginLastOne(mbrAccount.getLoginName());
		return R.ok().put("logreg", logMbrRegister).put("logLogin", logMbrLogin).put("mbrcard",
				Optional.ofNullable(mbrBankcardService.findMemberCardOne(id)).orElse(new MbrBankcard()));
	}*/


	@GetMapping("/chkUser")
	@ApiOperation(value = "会员接口-账号检测", notes = "根据会员账号检测账号是否存在，存在msg为真，不存在msg为假!")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
	public R chkUser(@RequestParam("loginName") String loginName) {
		Assert.isBlank(loginName, "用户名不能为空");
		MbrAccount entity = new MbrAccount();
		entity.setLoginName(loginName);
		int count = mbrAccountService.selectCount(entity);
		return R.ok(count > 0 ? Boolean.TRUE : Boolean.FALSE);
	}

	@PostMapping("/save")
	@RequiresPermissions("member:mbraccount:save")
	@ApiOperation(value = "会员信息-保存", notes = "保存会员基本信息到资料库")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
	@SysLog(module = "会员模块",methodText = "新增会员")
	public R save(@RequestBody MbrAccount mbrAccount, HttpServletRequest request) {
		mbrAccount.setId(null);
		ValidRegUtils.validloginName(mbrAccount, SysSetting.SysValueConst.require);
		mbrAccount.setLoginName(mbrAccount.getLoginName().toLowerCase());
		ValidRegUtils.validPwd(mbrAccount, SysSetting.SysValueConst.require);
		ValidRegUtils.validRealName(mbrAccount, SysSetting.SysValueConst.visible);
		ValidRegUtils.validPhone(mbrAccount, SysSetting.SysValueConst.visible);
		Assert.isLenght(mbrAccount.getMemo(), "备注最大长度为", 0, 200);
		if (StringUtils.isEmpty(mbrAccount.getMobile())) {
			mbrAccount.setIsVerifyMoblie(Available.disable);
			mbrAccount.setIsAllowMsg(Available.disable);
		} else {
			mbrAccount.setIsVerifyMoblie(Available.enable);
			mbrAccount.setIsAllowMsg(Available.enable);
		}
		mbrAccount.setIsLock(Available.disable);
		mbrAccount.setAvailable(Available.enable);
		mbrAccount.setIsVerifyEmail(Available.disable);
		mbrAccount.setIsAllowEmail(Available.enable);
		mbrAccount.setIsOnline(Available.disable);

		mbrAccountService.adminSave(mbrAccount, request);
		return R.ok();
	}

	@PostMapping("/update")
	@RequiresPermissions("member:mbraccount:update")
	@ApiOperation(value = "会员信息-更新会员资料", notes = "会员信息-更新会员资料")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
	@SysLog(module = "会员模块",methodText = "更新会员基本信息")
	public R update(@RequestBody MbrAccount mbrAccount) {
		MbrAccount account =new MbrAccount();
		account.setId(mbrAccount.getId());
		account.setRealName(mbrAccount.getRealName());
		account.setQq(mbrAccount.getQq());
		account.setWeChat(mbrAccount.getWeChat());
		account.setEmail(mbrAccount.getEmail());
		account.setMobile(mbrAccount.getMobile());
		mbrAccountService.updateMbrAccount(account, getUser().getUsername());
		return R.ok();
	}

	@PostMapping("/updateAccountRest")
	@RequiresPermissions("member:mbraccount:update")
	@ApiOperation(value = "会员信息-会员其他资料修改", notes = "会员其他资料修改")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	@SysLog(module = "会员模块", methodText = "会员信息-会员其他资料修改")
	public R updateAccountRest(@RequestBody MbrAccount mbrAccount) {
		Assert.isNull(mbrAccount.getAvailable(), "状态不能为空");
		Assert.isNull(mbrAccount.getId(), "会员ID不能为空");
		Assert.isNull(mbrAccount.getGroupId(), "会员组不能为空");
		Assert.isNull(mbrAccount.getCagencyId(), "代理不能为空");
		BizEvent bizEvent = new BizEvent(this, CommonUtil.getSiteCode(), mbrAccount.getId(), null);
		mbrAccountService.updateAccountRest(mbrAccount, bizEvent, getUser().getUsername());
		if (Objects.nonNull(bizEvent.getEventType())) {
			applicationEventPublisher.publishEvent(bizEvent);
		}
		return R.ok();
	}

	@PostMapping("/kickLine")
	@RequiresPermissions("member:mbraccount:update")
	@ApiOperation(value = "会员信息-会员踢线", notes = "根据会员Id强制让会员下线")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
	@SysLog(module = "会员模块",methodText = "会员踢线")
	public R kickLine(@RequestBody MbrAccount mbrAccount) {
		mbrAccountService.kickLine(mbrAccount.getId());
		applicationEventPublisher.publishEvent(new BizEvent(this,CommonUtil.getSiteCode(),mbrAccount.getId(), BizEventType.FORCE_LOGOUT));
		return R.ok();
	}

	/**
	 * 会员密码修改
	 *
	 * @return
	 */
	@PostMapping("/pwdUpdate")
	@RequiresPermissions("member:mbraccount:pwdUpdate")
	@ApiOperation(value = "会员信息-会员登陆密码修改", notes = "根据会员Id修改会员密码")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
	@SysLog(module = "会员模块",methodText = "修改会员登陆密码")
	public R pwdUpdate(@RequestBody MbrAccount mbraccModel) {
		Assert.isNull(mbraccModel.getId(),"会员ID不能为空!");
		MbrAccount info=mbrAccountService.getAccountInfo(mbraccModel.getId());
		mbraccModel.setLoginName(info.getLoginName());
		ValidRegUtils.validPwd(mbraccModel, SysSetting.SysValueConst.visible);
		String salt =info.getSalt();
		MbrAccount mbrAccount = new MbrAccount();
		mbrAccount.setLoginPwd(new Sha256Hash(mbraccModel.getLoginPwd(), salt).toHex());
		mbrAccount.setId(mbraccModel.getId());
		mbrAccountService.update(mbrAccount);
		return R.ok();
	}

	/**
	 * 会员资金密码修改
	 *
	 * @return
	 */
	@PostMapping("/secPwdUpdate")
	@RequiresPermissions("member:mbraccount:secPwdUpdate")
	@ApiOperation(value = "会员信息-会员资金密码修改", notes = "根据会员Id修改会员资金密码")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
	@SysLog(module = "会员模块",methodText = "修改会员资金密码")
	public R secPwdUpdate(@RequestBody MbrAccount mbraccModel) {
		ValidRegUtils.validPwd(mbraccModel, SysSetting.SysValueConst.visible);
		MbrAccount info=mbrAccountService.getAccountInfo(mbraccModel.getId());
		String salt =info.getSalt();
		MbrAccount mbrAccount = new MbrAccount();
		mbrAccount.setSecurePwd(new Sha256Hash(mbraccModel.getSecurePwd(), salt).toHex());
		mbrAccount.setId(mbraccModel.getId());
		mbrAccountService.update(mbrAccount);
		return R.ok();
	}


	/**
	 * 会员组修改
	 *
	 * @return
	 */
	@PostMapping("/groupIdUpdate")
	@RequiresPermissions("member:mbraccount:groupIdUpdate")
	@ApiOperation(value = "会员信息-修改会员组信息", notes = "根据会员Id修改会员组信息")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
	@SysLog(module = "会员模块",methodText = "修改会员所属组")
	public R groupIdUpdate(@RequestBody MbrAccount mbraccModel ) {
		mbrAccountService.updateGroupBatch(mbraccModel.getIds(),mbraccModel.getGroupId());
		return R.ok();
	}

	/**
	 * 会员状态
	 *
	 * @return
	 */
	@PostMapping("/avlUpdate")
	@RequiresPermissions("member:mbraccount:avlUpdate")
	@ApiOperation(value = "会员信息-修改会员状态", notes = "根据会员Id修改会员状态")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
	@SysLog(module = "会员模块",methodText = "修改会员状态")
	public R availableUpdate(@RequestBody MbrAccount mbraccModel) {
		Assert.isNull(mbraccModel.getIds(), "会员id不能为空!");
		Assert.isNull(mbraccModel.getAvailable(), "修改的状态不能为空!");
		for (int i = 0; i < mbraccModel.getIds().length; i++) {
			MbrAccount mbrAccount=mbrAccountService.updateAvailable(mbraccModel.getIds()[i],
					mbraccModel.getAvailable(), getUser().getUsername());
			if(mbraccModel.getAvailable().compareTo(new Byte("0")) == 0){
				applicationEventPublisher.publishEvent(new BizEvent(this,CommonUtil.getSiteCode(),mbrAccount.getId(),BizEventType.MEMBER_ACCOUNT_FREEZE));
			}else if(mbraccModel.getAvailable().compareTo(new Byte("2")) == 0){
				applicationEventPublisher.publishEvent(new BizEvent(this,CommonUtil.getSiteCode(),mbrAccount.getId(),BizEventType.MEMBER_WITHDRAWAL_REFUSE));
			}
		}
		return R.ok();
	}

	@GetMapping("/queryAccountAuditList")
	@RequiresPermissions("member:mbraccount:info")
	@ApiOperation(value = "会员信息-风控审核数据列表", notes = "会员信息-风控审核数据列表")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
	public R queryAccountAuditList(@RequestParam("accountId") Integer accountId) {
		Assert.isNull(accountId, "会员id不能为空!");
		return R.ok().put(mbrAccountService.queryAccountAuditList(accountId));
	}

	@GetMapping("/queryAccountAuditInfo")
	@RequiresPermissions("member:mbraccount:info")
	@ApiOperation(value = "会员信息-风控审核数据列表明细", notes = "会员信息-风控审核数据列表明细")
	public R queryAccountAuditInfo(@RequestParam("accountId") Integer accountId,
								   @RequestParam("keys") String keys,
								   @RequestParam("item") String item,
								   @RequestParam("pageNo") @NotNull Integer pageNo,
								   @RequestParam("pageSize") @NotNull Integer pageSize) {
		Assert.isNull(accountId, "会员id不能为空!");
		return R.ok().put(mbrAccountService.queryAccountAuditInfo(accountId, keys, item, pageNo, pageSize));
	}

	@Deprecated
	@GetMapping("/queryAccountBonusRepor")
	@RequiresPermissions("member:mbraccount:info")
	@ApiOperation(value = "会员信息-输赢报表数据列表", notes = "会员信息-输赢报表数据列表")
	public R queryAccountBonusRepor(@RequestParam("accountId") Integer accountId) {
		Assert.isNull(accountId, "会员id不能为空!");
		return R.ok().put(mbrAccountService.queryAccountBonusReporList(accountId));
	}

	@GetMapping("/bonusList")
	@RequiresPermissions("member:mbraccount:info")
	@ApiOperation(value = "会员信息-红利记录", notes = "会员信息-红利记录")
	public R bonusList(@RequestParam("accountId") Integer accountId,
					   @RequestParam("pageNo") @NotNull Integer pageNo,
					   @RequestParam("pageSize") @NotNull Integer pageSize) {
		return R.ok().putPage(mbrAccountService.bonusList(accountId, pageNo, pageSize));
	}

	@GetMapping("/withdrawList")
	@RequiresPermissions("member:mbraccount:info")
	@ApiOperation(value = "会员信息-提款记录", notes = "会员信息-提款记录")
	public R withdrawList(@RequestParam("accountId") Integer accountId,
						  @RequestParam("pageNo") @NotNull Integer pageNo,
						  @RequestParam("pageSize") @NotNull Integer pageSize) {
		return R.ok().putPage(mbrAccountService.withdrawList(accountId, pageNo, pageSize));
	}

	@GetMapping("/depositList")
	@RequiresPermissions("member:mbraccount:info")
	@ApiOperation(value = "会员信息-存款记录", notes = "会员信息-存款记录")
	public R depositList(@RequestParam("accountId") Integer accountId,
						 @RequestParam("pageNo") @NotNull Integer pageNo,
						 @RequestParam("pageSize") @NotNull Integer pageSize) {
		return R.ok().putPage(mbrAccountService.depositList(accountId, pageNo, pageSize));
	}

	@GetMapping("/manageList")
	@RequiresPermissions("member:mbraccount:info")
	@ApiOperation(value = "会员信息-转账记录", notes = "会员信息-转账记录")
	public R manageList(@RequestParam("accountId") Integer accountId,
						@RequestParam("pageNo") @NotNull Integer pageNo,
						@RequestParam("pageSize") @NotNull Integer pageSize) {
		return R.ok().putPage(mbrAccountService.manageList(accountId, pageNo, pageSize));
	}

	@GetMapping("/fundList")
	@RequiresPermissions("member:mbraccount:info")
	@ApiOperation(value = "会员信息-资金流水", notes = "会员信息-资金流水")
	public R fundList(@RequestParam("accountId") Integer accountId,
					  @RequestParam("pageNo") @NotNull Integer pageNo,
					  @RequestParam("pageSize") @NotNull Integer pageSize) {
		return R.ok().putPage(mbrAccountService.fundList(accountId, pageNo, pageSize));
	}

	@GetMapping("/accountLogList")
	@RequiresPermissions("member:mbraccount:info")
	@ApiOperation(value = "会员信息-资料变更", notes = "会员信息-资料变更")
	public R accountLogList(@RequestParam("accountId") Integer accountId,
					  @RequestParam("pageNo") @NotNull Integer pageNo,
					  @RequestParam("pageSize") @NotNull Integer pageSize) {
		return R.ok().putPage(mbrAccountService.accountLogList(accountId, pageNo, pageSize, getUserId()));
	}

	@GetMapping("/findAccountByName")
	@ApiOperation(value = "会员信息根据NAME查询", notes = "会员信息根据NAME查询")
	public R findAccountByName(@RequestParam("loginName") String loginName) {
		return R.ok().put(mbrAccountService.findAccountByName(loginName));
	}

	@GetMapping("/findHomePageCount")
	@ApiOperation(value = "首页数据总览", notes = "首页数据总览")
	public R findHomePageCount() {
		return R.ok().put(mbrAccountService.findHomePageCount());
	}
}
