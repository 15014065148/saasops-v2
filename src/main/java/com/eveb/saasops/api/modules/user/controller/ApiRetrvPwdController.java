package com.eveb.saasops.api.modules.user.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.modules.apisys.entity.TCpSite;
import com.eveb.saasops.api.modules.user.dto.FindPwdDto;
import com.eveb.saasops.api.modules.user.dto.ModPwdDto;
import com.eveb.saasops.api.modules.user.entity.FindPwEntity;
import com.eveb.saasops.api.modules.user.service.ApiUserService;
import com.eveb.saasops.api.utils.JwtUtils;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.DateUtil;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.common.utils.ShiroUtils;
import com.eveb.saasops.common.validator.Assert;
import com.eveb.saasops.common.validator.ValidRegUtils;
import com.eveb.saasops.modules.member.service.MbrAccountService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 注册
 */
@RestController
@RequestMapping("/api/retrvpwd")
@Api(value = "api", description = "前端找回密码接口")
public class ApiRetrvPwdController {
	@Autowired
	MbrAccountService mbrAccountService;
	@Autowired
	private JwtUtils jwtUtils;
	@Autowired
	private ApiUserService apiUserService;
	
	
	@PostMapping("/validUser")
	@ApiOperation(value = "第一步：会员验证", notes = "第一步：会员验证!")
	public R fpUser(@RequestBody FindPwdDto model, HttpServletRequest request,HttpServletResponse response,HttpSession session) {
		ShiroUtils.setCookie(request, response);
		Assert.isBlank(model.getUserName(), "用户名不能为空");
		model.setUserName(model.getUserName().toLowerCase());
		String kaptcha = apiUserService.getKaptcha(session,ApiConstants.KAPTCHA_RETRVPWD_SESSION_KEY);
		if (!model.getCaptcha().equalsIgnoreCase(kaptcha)) {
			Assert.message("验证码不正确!");
		}
		if (mbrAccountService.getAccountNum(model.getUserName()) == 0) {
			return R.error("无效的用户名!");
		} else {
			LinkedHashMap<String, Object> userInfo = mbrAccountService.webFindPwdUserInfo(model.getUserName());
			return R.ok(getFindPwdToken(model.getUserName())).put("userInfo", userInfo);
		}

	}
	
	@GetMapping("/retrvway")
	@ApiOperation(value = "第二步：会员找回密码方式(邮件,短信)", notes = "validType 会员找回密码方式,0 邮箱,1短信")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
	public R retrvway(@RequestParam("validType") String validType, HttpServletRequest request) {
		Assert.isBlank(validType, "找回方式不能为空!");
		if ((!validType.equals("1")) && (!validType.equals("0")))
			Assert.isNull(validType, "没有此种方式找回密码!");
		ValidRegUtils.checkFpValid(request, jwtUtils);
		String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
		TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
		FindPwEntity findPwEntity = new FindPwEntity();
		findPwEntity.setExpire(jwtUtils.getExpire());
		findPwEntity.setLoginName(loginName);
		findPwEntity.setVaildTimes(0);
		findPwEntity.setVaildType(new Byte(validType));
		findPwEntity.setApplyTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
		if (validType.equals("0")) {
			apiUserService.saveMailCode(findPwEntity, cpSite.getSiteCode());
		} else {
			apiUserService.saveSmsCode(findPwEntity, cpSite.getSiteCode());
		}
		return R.ok();
	}
	@GetMapping("/validCode")
	@ApiOperation(value = "第二步：发邮短信或邮箱后，验证code", notes = "第二步：发邮短信或邮箱后，验证code,特别注意此处的TOKEN与第一步产生的TOKEN是不一样的!")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
	public R validCode(@RequestParam(value="code") String code, HttpServletRequest request) {
		Assert.isBlank(code, "验证码不能为空!");
		ValidRegUtils.checkFpValid(request, jwtUtils);
		String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
		boolean flag=apiUserService.validCode(code,loginName);
		if(!flag)throw new RRException("错误验证码,请重试!");
		return R.ok(getPassValidToken(loginName,code));
	}
	@GetMapping("/modpwd")
	@ApiOperation(value = "第三步：会员密码修改", notes = "第三步：会员密码修改!")
	@ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
	public R modpwd(@RequestParam("password") String password, HttpServletRequest request) {
		//Assert.isBlank(modPwd.getCode(), "验证码不能为空!");
		//Assert.isBlank(password, "密码不能为空!");
		Assert.isLenght(password, "会员密码,长度为6~18位!",6,18);
		ValidRegUtils.checkFpValid(request, jwtUtils);
		String str = (String) request.getAttribute(ApiConstants.USER_NAME);
		String urerInfos[] = str.split(ApiConstants.USER_TOKEN_SPLIT);
		if(urerInfos==null||urerInfos.length!=2)
			throw new RRException("错误的Token!");
		ModPwdDto modPwdDto=new ModPwdDto();
		modPwdDto.setCode(urerInfos[0]);
		modPwdDto.setLoginName(urerInfos[1]);
		modPwdDto.setPassword(password);
		Assert.accEqualPwd(modPwdDto.getLoginName(),modPwdDto.getPassword(),"会员密码不能与账号相同!");
		boolean flag=apiUserService.modPwd(modPwdDto);
		if(!flag)throw new RRException("错误验证码,请重试!");
		return R.ok();
	}

	public Map<String, Object> getFindPwdToken(String loginName) {
		String token = jwtUtils.generatefindPwdToken(loginName);
		return generateToken(token);
	}

	public Map<String, Object> getPassValidToken(String loginName, String code) {
		String token = jwtUtils.generatefindPwdToken(loginName, code);
		return generateToken(token);
	}

	private Map<String, Object> generateToken(String token) {
		Map<String, Object> map = new HashMap<>();
		map.put("token", token);
		map.put("expire", jwtUtils.getExpireFindPwd());
		return map;
	}
}
