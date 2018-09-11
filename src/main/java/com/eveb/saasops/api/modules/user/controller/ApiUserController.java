package com.eveb.saasops.api.modules.user.controller;

import static com.eveb.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.eveb.saasops.common.utils.DateUtil.getCurrentDate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;

import com.eveb.saasops.common.utils.*;
import com.eveb.saasops.modules.member.service.*;
import com.eveb.saasops.modules.base.entity.BaseBank;
import com.eveb.saasops.modules.base.service.BaseBankService;
import lombok.extern.log4j.Log4j;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eveb.saasops.api.annotation.Login;
import com.eveb.saasops.api.config.ApiConfig;
import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.modules.apisys.entity.TCpSite;
import com.eveb.saasops.api.modules.user.dto.ActApplyDto;
import com.eveb.saasops.api.modules.user.dto.LoginUserDto;
import com.eveb.saasops.api.modules.user.dto.LoginVerifyDto;
import com.eveb.saasops.api.modules.user.dto.MbrWdApplyDto;
import com.eveb.saasops.api.modules.user.dto.MsgIds;
import com.eveb.saasops.api.modules.user.dto.PwdDto;
import com.eveb.saasops.api.modules.user.dto.RealNameDto;
import com.eveb.saasops.api.modules.user.dto.TransferRequestDto;
import com.eveb.saasops.api.modules.user.dto.UserDto;
import com.eveb.saasops.api.modules.user.dto.VfyMailOrMobDto;
import com.eveb.saasops.api.modules.user.service.ApiUserService;
import com.eveb.saasops.api.modules.user.service.PtService;
import com.eveb.saasops.api.utils.HttpsRequestUtil;
import com.eveb.saasops.api.utils.JwtUtils;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.constants.Constants.Available;
import com.eveb.saasops.common.constants.OprConstants;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.validator.Assert;
import com.eveb.saasops.common.validator.ValidRegUtils;
import com.eveb.saasops.listener.BizEvent;
import com.eveb.saasops.listener.BizEventType;
import com.eveb.saasops.modules.analysis.service.AnalysisService;
import com.eveb.saasops.modules.fund.entity.AccWithdraw;
import com.eveb.saasops.modules.fund.service.FundWithdrawService;
import com.eveb.saasops.modules.member.entity.MbrAccount;
import com.eveb.saasops.modules.member.entity.MbrBankcard;
import com.eveb.saasops.modules.member.entity.MbrDepositCond;
import com.eveb.saasops.modules.operate.entity.OprRecMbr;
import com.eveb.saasops.modules.operate.service.OprActActivityCastService;
import com.eveb.saasops.modules.operate.service.OprActActivityService;
import com.eveb.saasops.modules.operate.service.OprRecMbrService;
import com.eveb.saasops.modules.operate.service.TGmDepotService;
import com.eveb.saasops.modules.system.systemsetting.entity.SysSetting;
import com.eveb.saasops.modules.system.systemsetting.service.SysSettingService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;


@Log4j
@RestController
@RequestMapping("/api/user")
@Api(value = "api", description = "前端会员接口")
public class ApiUserController {

    @Autowired
    MbrAccountService mbrAccountService;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private ApiConfig apiConfig;
    @Autowired
    private ApiUserService apiUserService;
    @Autowired
    private MbrBankcardService mbrBankcardService;
    @Autowired
    private FundWithdrawService fundWithdrawService;
    @Autowired
    private OprRecMbrService oprRecMbrService;
    @Autowired
    private AnalysisService analysisService;
    @Autowired
    private TGmDepotService tGmDepotService;
    @Autowired
    private OprActActivityService oprActActivityService;
    @Autowired
    private PtService ptService;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private MbrWithdrawalCondService withdrawalCond;
    @Autowired
    private MbrDepositCondService mbrDepositCondService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private OprActActivityCastService oprActActivityCastService;
    @Autowired
    private BaseBankService baseBankService;


    @PostMapping("register")
    @ApiOperation(value = "会员接口-注册", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @ApiImplicitParam(name = "dev", value = "登录来源  dev：(PC:0、H5:3、Android、IOS)", required = true, dataType = "String", paramType = "header")
    public R register(@RequestBody UserDto userDto, HttpServletRequest request, HttpSession session) {
        Assert.isContainChinaChar(userDto.getLoginPwd(), "密码不能包含有中文！");
        MbrAccount mbrAccount = new MbrAccount();
        String dev = request.getHeader("dev");
        Byte source = HttpsRequestUtil.getHeaderOfDev(dev);
        mbrAccount.setLoginSource(source);

        List<SysSetting> list = sysSettingService.getRegisterInfoList();
        String kaptcha = apiUserService.getKaptcha(session, ApiConstants.KAPTCHA_REG_SESSION_KEY);
        ValidRegUtils.registerVerify(userDto, mbrAccount, list, apiConfig, kaptcha);

        MbrAccount account = mbrAccountService.webSave(mbrAccount);
        mbrAccountService.asyncLogoInfo(account, request, CommonUtil.getSiteCode(), Boolean.TRUE);

        Map<String, Object> map = getToken(account);
        apiUserService.updateLoginTokenCache(CommonUtil.getSiteCode(), account.getLoginName(), (String) map.get("token"));

        applicationEventPublisher.publishEvent(new BizEvent(this,
                CommonUtil.getSiteCode(), account.getId(), BizEventType.MEMBER_REGISTER_SUCCESS));
        return R.ok(map).put("userInfo", mbrAccountService.webUserInfo(account.getLoginName()));
    }

    @PostMapping("login")
    @ApiOperation(value = "会员接口-登陆", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R login(@RequestBody LoginUserDto model, HttpServletRequest request, HttpSession session) {
        Assert.isAccount(model.getLoginName(), "会员账号,长度为6~10位!");
        model.setLoginName(model.getLoginName().toLowerCase());
        String siteCode = CommonUtil.getSiteCode();
        LoginVerifyDto verifyDto = apiUserService.queryPassLtdNoCache(siteCode, model.getLoginName());
        Integer second = DateUtil.subtractTime(verifyDto.getExpireTime());
        if (verifyDto.getNo() >= 5 && second > 0) {
            return R.error("账号已被锁定,请稍后再试!");
        }
        String kaptcha = (verifyDto.getNo() > 2) ? apiUserService.getKaptcha(session, ApiConstants.KAPTCHA_LOGIN_SESSION_KEY) : "";
        ValidRegUtils.loginVerify(model, verifyDto.getNo(), kaptcha);
        MbrAccount entity = new MbrAccount();
        entity.setLoginName(model.getLoginName());
        entity = mbrAccountService.queryObjectCond(entity);
        if (entity == null) {
            return R.error("无此账号,请注册!");
        } else if (!entity.getLoginPwd().equals(new Sha256Hash(model.getPassword(), entity.getSalt()).toHex())) {
            apiUserService.updatePassLtdNoCache(siteCode, model.getLoginName(), verifyDto.getNo());
            return R.error("密码不正确!");
        } else if (entity.getIsLock().equals(Available.enable)) {
            return R.error("账号已被锁定,请联系在线客服!");
        } else if (entity.getAvailable().equals(Available.disable)) {
            return R.error("账号已被禁用,请联系在线客服!");
        } else {
            String dev = request.getHeader("dev");
            Byte loginSource = HttpsRequestUtil.getHeaderOfDev(dev);
            entity.setLoginSource(loginSource);

            mbrAccountService.update(entity);
            Map<String, Object> map = getToken(entity);
            apiUserService.rmPassLtdNoCache(siteCode, model.getPassword());
            apiUserService.updateLoginTokenCache(siteCode, model.getLoginName(), (String) map.get("token"));

            mbrAccountService.asyncLogoInfo(entity, request, siteCode, Boolean.FALSE);
            return R.ok(map).put("userInfo", mbrAccountService.webUserInfo(entity.getLoginName()));
        }
    }

    @GetMapping("/chkUser")
    @ApiOperation(value = "会员接口-账号检测", notes = "根据会员账号检测账号是否存在，存在msg为真，不存在msg为假!")
    public R chkUser(@ApiParam("会员账号") @RequestParam("username") String username) {
        Assert.isBlank(username, "用户名不能为空");
        MbrAccount entity = new MbrAccount();
        entity.setLoginName(username);
        int count = mbrAccountService.selectCount(entity);
        return R.ok(count > 0 ? Boolean.TRUE : Boolean.FALSE);
    }

    @Login
    @GetMapping("/chkUserOnline")
    @ApiOperation(value = "会员接口-账号检测是否在线", notes = "会员接口-账号检测是否在线!")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R chkUserOnline() {
        return R.ok(Boolean.TRUE);
    }

    @GetMapping("/getRegistSetting")
    @ApiOperation(value = "获取会员注册参数设置", notes = "获取会员注册参数设置")
    public R getRegistSetting() {
        return R.ok().put("regSetting", sysSettingService.getRegisterInfoMap());
    }

    @PostMapping("/loginOut")
    @ApiOperation(value = "会员接口-登出", notes = "根据当前TOKEN 登出此账号!")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R loginOut(HttpServletRequest request) {
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        mbrAccountService.updateOffline(loginName);
        apiUserService.rmLoginTokenCache(cpSite.getSiteCode(), loginName);
        return R.ok(Boolean.TRUE);
    }

    @GetMapping("/getUserInfo")
    @ApiOperation(value = "查找会员信息", notes = "查找会员信息!")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R getUserInfo(HttpServletRequest request) {
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        LinkedHashMap<String, Object> userInfo = mbrAccountService.webUserInfo(loginName);
        return R.ok().put("userInfo", userInfo);
    }

    @GetMapping("/getLtdNo") // limited number
    @ApiOperation(value = "查找会员登陆是否需要显示验证码及锁定时倒计时", notes = "no>=5时 锁定 180秒(second),no>=3应该出现验证码")
    public R getLtdNo(@RequestParam("loginName") String loginName, HttpServletRequest request) {
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        LoginVerifyDto verifyDto = apiUserService.queryPassLtdNoCache(cpSite.getSiteCode(), loginName);
        return R.ok().put("no", verifyDto.getNo()).put("second", DateUtil.subtractTime(verifyDto.getExpireTime()));
    }

    @PostMapping("/modRealName")
    @ApiOperation(value = "修改会员真实姓名", notes = "修改会员真实姓名")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R modRealName(@RequestBody RealNameDto realNameDto, HttpServletRequest request) {
        Assert.isBlank(realNameDto.getRealName(), "会员真实姓名不能为空!");
        Assert.isLenght(realNameDto.getRealName(), "会员真实姓名长度不能大于16位!", 1, 16);
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        MbrAccount mbrAccount = mbrAccountService.updateRealName(userId, realNameDto.getRealName(),
                cpSite.getSiteCode());
        if (mbrAccount.getRealName().equals(realNameDto.getRealName()))
            return R.ok();
        else
            return R.error("修改真实姓名失败!");
    }

    @PostMapping("/modPwd")
    @ApiOperation(value = "修改会员密码", notes = "修改会员密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R modPwd(@RequestBody PwdDto pwdDto, HttpServletRequest request) {
        Assert.isBlank(pwdDto.getLastPwd(), "旧密码不能为空!");
        Assert.isBlank(pwdDto.getPwd(), "密码不能为空!");
        Assert.isContainChinaChar(pwdDto.getPwd(),"密码不能包含有中文！");
        Assert.isLenght(pwdDto.getPwd(), "会员密码,长度为6~18位!", 6, 18);
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        mbrAccountService.updatePwd(userId, pwdDto);
        applicationEventPublisher.publishEvent(new BizEvent(this, CommonUtil.getSiteCode(), userId,
                BizEventType.UPDATE_MEMBER_INFO, pwdDto.getLastPwd(), pwdDto.getPwd()));
        return R.ok();
    }

    @PostMapping("/modScPwd")
    @ApiOperation(value = "修改会员资金密码", notes = "修改会员资金密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R modScPwd(@RequestBody PwdDto pwdDto, HttpServletRequest request) {
        Assert.isBlank(pwdDto.getLastPwd(), "旧密码不能为空!");
        Assert.isBlank(pwdDto.getPwd(), "资金密码不能为空!");
        Assert.isLenght(pwdDto.getPwd(), "会员资金密码,长度为6~18位!", 6, 18);
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        MbrAccount mbrAccount = mbrAccountService.getAccountInfo(userId);
        if (StringUtils.isEmpty(mbrAccount.getSecurePwd())) {
            throw new RRException("请在提款页面设置资金密码!");
        }
        mbrAccountService.updateScPwd(userId, pwdDto);
        return R.ok();
    }

    @PostMapping("/sendMailCode")
    @ApiOperation(value = "会员邮箱验证", notes = "会员邮箱验证")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R sendMailCode(@RequestBody VfyMailOrMobDto vfyDto, HttpServletRequest request) {
        Assert.checkEmail(vfyDto.getEmail(), "邮箱格式不正确!");
        Assert.isLenght(vfyDto.getEmail(), "邮箱长度不能大于30位", 5, 30);
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        String code = apiUserService.sendVfyMailCode(vfyDto, cpSite.getSiteCode(), userId);
        if (!StringUtils.isEmpty(code)) {
            vfyDto.setCode(code);
            apiUserService.updateVfyMailOrMobCodeCache(cpSite.getSiteCode(), loginName, vfyDto);
            return R.ok();
        } else {
            return R.error("邮件发送失败,请联系在线客服!");
        }
    }

    @PostMapping("/vfyMailCode")
    @ApiOperation(value = "会员邮箱验证", notes = "会员邮箱验证")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R vfyMailCode(@RequestBody VfyMailOrMobDto vfyDto, HttpServletRequest request) {
        Assert.checkEmail(vfyDto.getEmail(), "邮箱格式不正确!");
        Assert.isLenght(vfyDto.getEmail(), "邮箱长度不能大于30位", 5, 30);
        Assert.isBlank(vfyDto.getCode(), "验证码不能为空!");
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        VfyMailOrMobDto vfyDotCahce = apiUserService.queryVfyMailOrMobCodeCache(cpSite.getSiteCode(), loginName);
        if (!vfyDto.getEmail().equals(vfyDotCahce.getEmail())) {
            throw new RRException("验证邮箱与申请邮箱不匹配!");
        }
        if (!vfyDto.getCode().equals(vfyDotCahce.getCode())) {
            throw new RRException("验证码不正确!");
        }
        if (mbrAccountService.updateMail(userId, vfyDto.getEmail()) > 0)
            return R.ok();
        else
            return R.error();
    }

    @PostMapping("/sendMobCode")
    @ApiOperation(value = "会员手机验证", notes = "会员手机验证")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R sendMobCode(@RequestBody VfyMailOrMobDto vfyDto, HttpServletRequest request) {
        Assert.checkmobile(vfyDto.getMobile(), "手机号码格式错误!");
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        String code = apiUserService.sendVfySmsCode(vfyDto, cpSite.getSiteCode(), userId);
        if (!StringUtils.isEmpty(code)) {
            vfyDto.setCode(code);
            apiUserService.updateVfyMailOrMobCodeCache(cpSite.getSiteCode(), loginName, vfyDto);
            return R.ok();
        } else {
            return R.error();
        }
    }

    @PostMapping("/vfyMobCode")
    @ApiOperation(value = "会员手机验证", notes = "会员手机验证")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R vfyMobCode(@RequestBody VfyMailOrMobDto vfyDto, HttpServletRequest request) {
        Assert.checkmobile(vfyDto.getMobile(), "手机号码格式错误!");
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        VfyMailOrMobDto vfyDotCahce = apiUserService.queryVfyMailOrMobCodeCache(cpSite.getSiteCode(), loginName);
        if (!vfyDto.getMobile().equals(vfyDotCahce.getMobile())) {
            throw new RRException("验证手机号与申请手机号不匹配!");
        }
        if (!vfyDto.getCode().equals(vfyDotCahce.getCode())) {
            throw new RRException("验证码不正确!");
        }
        if (mbrAccountService.updateMobile(userId, vfyDto.getMobile()) > 0)
            return R.ok();
        else
            return R.error();
    }

    @PostMapping("/saveBankCard")
    @ApiOperation(value = "新增会员银行卡号", notes = "新增会员银行卡号")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R saveBankCard(@RequestBody MbrBankcard mbrBankcard, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        //会员与银行卡关联表增加id字段进行关联，加该校验是因为北京前端无法同步修改
        if (mbrBankcard.getBankCardId() != null) {
            BaseBank baseBank = baseBankService.queryObject(mbrBankcard.getBankCardId());
            mbrBankcard.setBankName(baseBank.getBankName());
        } else {
            BaseBank baseBank = new BaseBank();
            baseBank.setBankName(mbrBankcard.getBankName());
            List<BaseBank> baseBanks = baseBankService.select(baseBank);
            mbrBankcard.setBankCardId(baseBanks.get(0).getId());
        }
        Assert.isBlank(mbrBankcard.getBankName(), "开户银行不能为空!");
        Assert.isBlank(mbrBankcard.getCardNo(), "开户账号不能为空!");
        Assert.isNumeric(mbrBankcard.getCardNo(), "开户账号只能为数字!");
        Assert.isBankCardNo(mbrBankcard.getCardNo(), "长度只能为16或19位!", 16, 19);
        Assert.isBlank(mbrBankcard.getProvince() + mbrBankcard.getCity() + mbrBankcard.getAddress(), "开户支行不能为空!");
        Assert.isChina(mbrBankcard.getAddress(), "支行名称只允许填写中文!");
        mbrBankcard.setAccountId(userId);
        return mbrBankcardService.saveBankCard(mbrBankcard, Constants.EVNumber.one, null);
    }

    @PostMapping("/uservfyInfo")
    @ApiOperation(value = "查询会员密码手机等是否填写", notes = "查询会员密码手机等是否填写")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R uservfyInfo(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put("uservfyInfos", mbrAccountService.webUserVfyInfo(userId));
    }

    @PostMapping("/bankCardList")
    @ApiOperation(value = "会员绑定的银行卡", notes = "会员绑定的银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "Stoken", value = "Stoken", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R userBankCards(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put("bankCards", mbrBankcardService.ListCondBankCard(userId));
    }

    @PostMapping("/withdrawal")
    @ApiOperation(value = "会员取款申请", notes = "会员取款申请")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R withdrawal(@RequestBody MbrWdApplyDto wdApply, HttpServletRequest request) {
        Assert.isNumeric(wdApply.getDrawingAmount(), "申请取款金额只能为正数!");
        Assert.isNull(wdApply.getBankCardId(), "取款银行不能为空!");
        Assert.isBlank(wdApply.getPwd(), "资金密码不能为空!");
        Assert.isLenght(wdApply.getPwd(), "资金密码长度为6~18位!", 6, 18);
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        AccWithdraw withDraw = new AccWithdraw();
        withDraw.setAccountId(userId);
        withDraw.setLoginName(loginName);
        withDraw.setIp(CommonUtil.getIpAddress(request));
        withDraw.setDrawingAmount(wdApply.getDrawingAmount());
        withDraw.setBankCardId(wdApply.getBankCardId());
        //获取取款的客户端来源
        String dev = request.getHeader("dev");
        Byte withdrawSource = HttpsRequestUtil.getHeaderOfDev(dev);
        withDraw.setWithdrawSource(withdrawSource);
        fundWithdrawService.saveApply(withDraw, wdApply.getPwd());
        return R.ok("申请取款成功!");
    }

    @GetMapping("/wdApplyList")
    @ApiOperation(value = "查询取款记录", notes = "查询取款记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R wdApplyList(@RequestParam("startTime") String startTime, @RequestParam("entTime") String entTime,
                         @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize,
                         HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        PageUtils page = fundWithdrawService.queryAccListPage(startTime, entTime, userId, pageNo, pageSize);

        return R.ok().put("page", page).put("totalActuals",
                fundWithdrawService.totalActualArrival(startTime, entTime, userId));
    }

    @GetMapping("/msgList")
    @ApiOperation(value = "会员消息", notes = "会员消息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R msgList(@RequestParam("startTime") String startTime, @RequestParam("entTime") String entTime,
                     @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize,
                     HttpServletRequest request) {
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        OprRecMbr oprRecMbr = new OprRecMbr();
        oprRecMbr.setMbrName(loginName);
        oprRecMbr.setSendTimeFrom(startTime);
        oprRecMbr.setSendTimeTo(entTime);
        PageUtils page = oprRecMbrService.queryListPage(oprRecMbr, pageNo, pageSize, "", null);
        return R.ok().put("page", page);
    }

    @PostMapping("/readMsg")
    @ApiOperation(value = "会员消息", notes = "会员消息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R readMsg(@RequestBody MsgIds msgIds, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        List<OprRecMbr> ormList = new ArrayList<OprRecMbr>();
        for (int i = 0; i < msgIds.getIds().length; i++) {
            OprRecMbr oprRecMbr = new OprRecMbr();
            oprRecMbr.setMsgId(msgIds.getIds()[i]);
            oprRecMbr.setMbrId(userId);
            oprRecMbr.setReadDate(getCurrentDate(FORMAT_18_DATE_TIME));
            oprRecMbr.setIsRead(OprConstants.READED);
            ormList.add(oprRecMbr);

        }
        oprRecMbrService.readBatch(ormList);
        return R.ok();
    }

    @PostMapping("/delMsg")
    @ApiOperation(value = "会员消息", notes = "会员消息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R delMsg(@RequestBody MsgIds msgIds, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        List<OprRecMbr> ormList = new ArrayList<OprRecMbr>();
        for (int i = 0; i < msgIds.getIds().length; i++) {
            OprRecMbr oprRecMbr = new OprRecMbr();
            oprRecMbr.setMsgId(msgIds.getIds()[i]);
            oprRecMbr.setMbrId(userId);
            ormList.add(oprRecMbr);
        }
        oprRecMbrService.modifyOrm(ormList);
        return R.ok();
    }

    @GetMapping("/unReadMsgNo")
    @ApiOperation(value = "未读消条数", notes = "未读消息条数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R unReadMsg(HttpServletRequest request) {
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        return R.ok().put("msgNo", oprRecMbrService.getUnreadMsgCount(loginName));
    }

    @GetMapping("/BetDetailList")
    @ApiOperation(value = "投注记录", notes = "全部游戏")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R betDetailsData(@RequestParam("pageNo") @NotNull Integer pageNo,
                            @RequestParam("pageSize") @NotNull Integer pageSize,
                            @RequestParam(value = "depotName", required = false) String depotName,
                            @RequestParam(value = "gameCatId", required = false) Integer gameCatId,
                            @RequestParam(value = "betStrTime") String betStrTime,
                            @RequestParam(value = "betEndTime") String betEndTime, HttpServletRequest request) {
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        return R.ok()
                .put("page",
                        analysisService.getRptBetListPage(pageNo, pageSize, cpSite.getSiteCode(), loginName, depotName, gameCatId,
                                betStrTime, betEndTime))
                .put("total", analysisService.getRptBetListReport(cpSite.getSiteCode(), loginName, depotName, gameCatId,
                        betStrTime, betEndTime));
    }

    @GetMapping("/bonusList")
    @ApiOperation(value = "红利记录", notes = "红利记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R betDetailsData(@ModelAttribute TransferRequestDto requestDto,
                            @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize,
                            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put("page", oprActActivityService.findAccountBonusList(requestDto.getStartTime(),
                requestDto.getEntTime(), userId, pageNo, pageSize, Constants.EVNumber.one));
    }

    @GetMapping("/ActivityList")
    @ApiOperation(value = "活动记录", notes = "活动记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R ActivityList(@RequestParam("pageNo") @NotNull Integer pageNo,
                          @RequestParam("pageSize") @NotNull Integer pageSize, @RequestParam("actCatId") @NotNull Integer actCatId,
                          @RequestParam(value = "terminal", required = false) Byte terminal, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put("page", oprActActivityService.webActivityList(pageNo, pageSize, actCatId, userId, terminal));
    }

    @GetMapping("/ActivityDepositList")
    @ApiOperation(value = "首存送或存就送记录", notes = "首存送或存就送记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R activityDepositList(@RequestParam(value = "terminal", required = false) Byte terminal,
                                 HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put("page", oprActActivityService.webDepositActivityList(userId, terminal));
    }

    @GetMapping("/checkActivity")
    @ApiOperation(value = "活动检测", notes = "活动检测")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R checkActivity(@RequestParam("activityId") @NotNull Integer activityId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        oprActActivityCastService.checkoutApplyActivity(userId, activityId, BigDecimal.ZERO, Boolean.FALSE);
        return R.ok();
    }

    @GetMapping("/ptLogOut")
    @ApiOperation(value = "PT登出", notes = "PT登出")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R ptLogOut(HttpServletRequest request) {
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        ptService.logOut(ApiConstants.DepotId.PT, loginName, cpSite.getSiteCode());
        return R.ok();
    }

    @GetMapping("/auditDetail")
    @ApiOperation(value = "提款稽核明细", notes = "稽核明细")
    @ApiImplicitParams({@ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R auditList(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(auditAccountService.auditDetail(userId));
    }

    @GetMapping("/isWithdrawal")
    @ApiOperation(value = "提款稽核验证", notes = "稽核验证")
    @ApiImplicitParams({@ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R isWithdrawal(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(auditAccountService.isWithdrawal(accountId));
    }

    @GetMapping("/withdrawalCond")
    @ApiOperation(value = "取款条件", notes = "取款条件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R withdrawalCond(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put("withdrawalCond", withdrawalCond.getMbrWithDrawal(userId));
    }

    @GetMapping("/depotCond")
    @ApiOperation(value = "线下存款条件", notes = "线下存款条件(lowQuota最低,topQuota最高 )")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R depotCond(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        MbrDepositCond mbrDepositCond = mbrDepositCondService.getMbrDeposit(userId);
        return R.ok().put("lowQuota", mbrDepositCond.getLowQuota()).put("topQuota", mbrDepositCond.getTopQuota());
    }

    public Map<String, Object> getToken(MbrAccount entity) {
        String token = jwtUtils.generateToken(entity.getId(), entity.getLoginName());
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("expire", jwtUtils.getExpire());
        return map;
    }

    @PostMapping("/actApply")
    @ApiOperation(value = "会员活动申请", notes = "会员活动申请")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R actApply(@RequestBody ActApplyDto actApplyDto, HttpServletRequest request, HttpSession session) {
        Assert.isNull(actApplyDto.getActivityId(), "活动不能为空!");
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String ip = CommonUtil.getIpAddress(request);
        oprActActivityCastService.applyActivity(userId, actApplyDto.getActivityId(), CommonUtil.getSiteCode(), ip, session);
        return R.ok();
    }

    @GetMapping("/sendMsg")
    @ApiOperation(value = "发送消息", notes = "发送消息()")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "siteCode", value = "siteCode", required = true, dataType = "String", paramType = "header")})
    @Login
    public R sendMsg(HttpServletRequest request) {
        String schemaName = CommonUtil.getSiteCode() == null ? "test" : CommonUtil.getSiteCode();
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        applicationEventPublisher
                .publishEvent(new BizEvent(this, schemaName, userId, BizEventType.MEMBER_REGISTER_SUCCESS));// 在这里发布事件
        return R.ok().put("code", 200);
    }

    @GetMapping("/depotBalanceList")
    @ApiOperation(value = "平台信息列表", notes = "平台信息列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R depotList(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put("tGmDepots", tGmDepotService.findDepotBalanceList(userId));
    }

    @GetMapping("/setAccQQ")
    @ApiOperation(value = "更新QQ号码", notes = "更新QQ号码")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R setAccQQ(MbrAccount mbrAccount, HttpServletRequest request) {
        Assert.isQq(mbrAccount.getQq(), "QQ号码格式不正确");
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        MbrAccount account = new MbrAccount();
        account.setId(userId);
        account.setQq(mbrAccount.getQq());
        mbrAccountService.saveQQOrWeChat(account);
        return R.ok();
    }

    @Login
    @GetMapping("/setAccWeChat")
    @ApiOperation(value = "更新微信号码", notes = "更新微信号码")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    public R setAccWeChat(MbrAccount mbrAccount, HttpServletRequest request) {
        Assert.isWeChat(mbrAccount.getWeChat(), "微信号格式不正确");
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        MbrAccount account = new MbrAccount();
        account.setId(userId);
        account.setWeChat(mbrAccount.getWeChat());
        mbrAccountService.saveQQOrWeChat(account);
        return R.ok();
    }

    @Login
    @GetMapping("/accountBonusList")
    @ApiOperation(value = "会员优惠券管理", notes = "会员优惠券管理")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    public R accountBonusList(@RequestParam("pageNo") @NotNull Integer pageNo,
                              @RequestParam("pageSize") @NotNull Integer pageSize,
                              @RequestParam(value = "status", required = false) Integer status,
                              HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().putPage(oprActActivityCastService.accountBonusList(userId, status, pageNo, pageSize));
    }

    @Login
    @GetMapping("/accountBonusOne")
    @ApiOperation(value = "会员优惠券管理ID查询", notes = "会员优惠券管理ID查询")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    public R accountBonusOne(@RequestParam("id") Integer id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().putPage(oprActActivityCastService.accountBonusOne(userId, id));
    }

    @Login
    @GetMapping("/availableAccountBonusList")
    @ApiOperation(value = "可用的会员优惠券管理", notes = "可用的会员优惠券管理")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    public R availableAccountBonusList(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().putPage(oprActActivityCastService.availableAccountBonusList(userId));
    }

    @Login
    @PostMapping("/setFreeWalletSwitch")
    @ApiOperation(value = "免转钱包开关", notes = "免转钱包开关")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    public R setFreeWalletSwitch(@RequestBody MbrAccount mbrAccount, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        MbrAccount account = new MbrAccount();
        account.setId(userId);
        account.setFreeWalletSwitch(mbrAccount.getFreeWalletSwitch());
        mbrAccountService.update(account);
        return R.ok();
    }

    @Login
    @GetMapping("/setSecurePwdOfFirst")
    @ApiOperation(value = "首次设置资金密码", notes = "首次设置资金密码")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    public R setSecurePwdOfFirst(MbrAccount mbrAccount, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        mbrAccount.setId(userId);
        mbrAccountService.setSecurePwdOfFirst(mbrAccount);
        return R.ok();
    }

    @Login
    @GetMapping("/isDepotAudit")
    @ApiOperation(value = "查询会员是否存在平台优惠稽核 false稽核不过 true通过", notes = "查询会员是否存在平台优惠稽核 false稽核不过 true通过")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    public R isDepotAudit(@RequestParam("depotId") Integer depotId, HttpServletRequest request) {
        Assert.isNull(depotId, "平台ID不能为空");
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(auditAccountService.isBounsOut(userId, depotId));
    }

    @Login
    @GetMapping("/depotAudit")
    @ApiOperation(value = "查询会员平台优惠稽核明细", notes = "查询会员平台优惠稽核明细")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    public R depotAudit(@RequestParam("depotId") Integer depotId, HttpServletRequest request) {
        Assert.isNull(depotId, "平台ID不能为空");
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(auditAccountService.getDepotAuditDto(userId, depotId));
    }
}
