package com.eveb.saasops.common.validator;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.eveb.saasops.common.exception.R200Exception;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;

import com.eveb.saasops.api.config.ApiConfig;
import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.modules.user.dto.LoginUserDto;
import com.eveb.saasops.api.modules.user.dto.UserDto;
import com.eveb.saasops.api.utils.JwtUtils;
import com.eveb.saasops.common.constants.Constants.Available;
import com.eveb.saasops.common.constants.SystemConstants;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.DateUtil;
import com.eveb.saasops.modules.member.entity.MbrAccount;
import com.eveb.saasops.modules.system.systemsetting.entity.SysSetting;
import com.eveb.saasops.modules.system.systemsetting.entity.SysSetting.SysValueConst;

import io.jsonwebtoken.Claims;


public class ValidRegUtils {


    public static void validateReg(MbrAccount mbrAccount, List<SysSetting> list, String kaptcha) {
        for (SysSetting seting : list) {
            switch (seting.getSyskey()) {
                case SystemConstants.MEMBER_VERIFICATION_CODE:
                    validCaptchar(mbrAccount, seting.getSysvalue(), kaptcha);
                    break;
                case SystemConstants.MEMBER_ACCOUNT:
                    validloginName(mbrAccount, seting.getSysvalue());
                    break;
                case SystemConstants.MEMBER_LOGIN_PASSWORD:
                    validPwd(mbrAccount, seting.getSysvalue());
                    break;
                case SystemConstants.MEMBER_REAL_NAME:
                    validRealName(mbrAccount, seting.getSysvalue());
                    break;
                case SystemConstants.MEMBER_TELPHONE:
                    validPhone(mbrAccount, seting.getSysvalue());
                    break;
                case SystemConstants.MEMBER_EMAIL:
                    validEmail(mbrAccount, seting.getSysvalue());
                    break;
                case SystemConstants.MEMBER_QQ:
                    validQQ(mbrAccount, seting.getSysvalue());
                    break;
                case SystemConstants.MEMBER_WECHAT:
                    validWeChat(mbrAccount, seting.getSysvalue());
                    break;
                case SystemConstants.MEMBER_ADDRESS:
                    validAdress(mbrAccount, seting.getSysvalue());
                    break;
            }
        }
    }


    public static void validloginName(MbrAccount mbrAccount, String validType) {
        switch (validType) {
            case SysValueConst.none:
                mbrAccount.setLoginName("");
                break;
            case SysValueConst.visible:
                if (!StringUtils.isEmpty(mbrAccount.getLoginName())) {
                    Assert.isAccount(mbrAccount.getLoginName(), "会员账号,长度为6~10位!");
                    Assert.isCharacter(mbrAccount.getLoginName(), "会员账号只能为字符a-z,A-Z,0-9与数字,并且首字符必为字符!");
                }
                break;
            case SysValueConst.require:
                Assert.isBlank(mbrAccount.getLoginName(), "会员账号不能为空,长度为6~10位!");
                Assert.isAccount(mbrAccount.getLoginName(), "会员账号,长度为6~10位!");
                Assert.isCharacter(mbrAccount.getLoginName(), "会员账号只能为字符a-z,A-Z,0-9与数字,并且首字符必为字符!");
                break;
        }
    }

    public static void validPwd(MbrAccount mbrAccount, String validType) {
        switch (validType) {
            case SysValueConst.none:
                mbrAccount.setLoginPwd("");
                break;
            case SysValueConst.visible:
                if (!StringUtils.isEmpty(mbrAccount.getLoginPwd())) {
                    Assert.isLenght(mbrAccount.getLoginPwd(), "会员密码,长度为6~18位!", 6, 18);
                    Assert.accEqualPwd(mbrAccount.getLoginName(), mbrAccount.getLoginPwd(), "会员密码不能与账号相同!");
                }

                break;
            case SysValueConst.require:
                Assert.isLenght(mbrAccount.getLoginPwd(), "会员密码,长度为6~18位!", 6, 18);
                Assert.accEqualPwd(mbrAccount.getLoginName(), mbrAccount.getLoginPwd(), "会员密码不能与账号相同!");
                break;
        }
    }

    public static void validScPwd(MbrAccount mbrAccount, String validType) {
        switch (validType) {
            case SysValueConst.none:
                mbrAccount.setSecurePwd("");
                break;
            case SysValueConst.visible:
                if (!StringUtils.isEmpty(mbrAccount.getSecurePwd())) {
                    Assert.isLenght(mbrAccount.getSecurePwd(), "资金密码,长度为6~18位!", 6, 18);
                    Assert.accEqualPwd(mbrAccount.getLoginName(), mbrAccount.getSecurePwd(), "资金密码不能与账号相同!");
                }

                break;
            case SysValueConst.require:
                Assert.isLenght(mbrAccount.getSecurePwd(), "资金密码,长度为6~18位!", 6, 18);
                Assert.accEqualPwd(mbrAccount.getLoginName(), mbrAccount.getSecurePwd(), "资金密码不能与账号相同!");
                break;
        }
    }

    private static void validCaptchar(MbrAccount mbrAccount, String validType, String kaptcha) {
        switch (validType) {
            case SysValueConst.none:
            case SysValueConst.visible:
                mbrAccount.setCaptchareg("");
                break;
            case SysValueConst.require:
                //String kaptcha = ShiroUtils.getKaptcha(ApiConstants.KAPTCHA_REG_SESSION_KEY);
                if (!mbrAccount.getCaptchareg().equalsIgnoreCase(kaptcha)) {
                    throw new R200Exception("验证码不正确!");
                }
                break;
        }
    }

    public static void validRealName(MbrAccount mbrAccount, String validType) {
        switch (validType) {
            case SysValueConst.none:
                mbrAccount.setRealName("");
                break;
            case SysValueConst.visible:
                if (!StringUtils.isEmpty(mbrAccount.getRealName())) {
                    Assert.isLenght(mbrAccount.getRealName(), "会员真实姓名最大长度为16位!", 0, 16);
                    Assert.chineseCharacter(mbrAccount.getRealName(), "会员真实姓名只能为汉字及.");
                }
                break;
            case SysValueConst.require:
                Assert.isLenght(mbrAccount.getRealName(), "会员真实姓名不能为空,最大长度为16位!", 1, 16);
                Assert.chineseCharacter(mbrAccount.getRealName(), "会员真实姓名只能为汉字及.");
                break;
        }
    }

    public static void validPhone(MbrAccount mbrAccount, String validType) {
        switch (validType) {
            case SysValueConst.none:
                mbrAccount.setMobile("");
                break;
            case SysValueConst.visible:
                if (!StringUtils.isEmpty(mbrAccount.getMobile())) {
                    //Assert.isLenght(mbrAccount.getMobile(), "会员电话号码最大长度为11位!",0,11);
                    Assert.isPhone(mbrAccount.getMobile(), "会员电话号码只能为数字,并且长度为11位!");
                }
                break;
            case SysValueConst.require:
                //Assert.isLenght(mbrAccount.getMobile(), "会员电话号码最大长度为11位,并且不能为空!",6,11);
                Assert.isPhone(mbrAccount.getMobile(), "会员电话号码只能为数字,并且长度为11位!");
                break;
        }
    }

    public static void validEmail(MbrAccount mbrAccount, String validType) {
        switch (validType) {
            case SysValueConst.none:
                mbrAccount.setEmail("");
                break;
            case SysValueConst.visible:
                if (!StringUtils.isEmpty(mbrAccount.getEmail())) {
                    Assert.isLenght(mbrAccount.getEmail(), "邮箱最长为30位,可选!", 0, 30);
                    Assert.checkEmail(mbrAccount.getEmail(), "请输入正确的邮箱格式!");

                }
                break;
            case SysValueConst.require:
                Assert.isLenght(mbrAccount.getEmail(), "邮箱最长为30位,可选!", 4, 30);
                Assert.checkEmail(mbrAccount.getEmail(), "请输入正确的邮箱格式!");
                break;
        }
    }

    public static void validQQ(MbrAccount mbrAccount, String validType) {
        switch (validType) {
            case SysValueConst.none:
                mbrAccount.setQq("");
                break;
            case SysValueConst.visible:
                if (!StringUtils.isEmpty(mbrAccount.getQq())) {
                    Assert.isLenght(mbrAccount.getQq(), "qq最长为 15位!", 0, 15);
                    Assert.isQq(mbrAccount.getQq(), "qq只能为数字!");
                }
                break;
            case SysValueConst.require:
                Assert.isLenght(mbrAccount.getQq(), "qq最长为 15位!", 4, 15);
                Assert.isQq(mbrAccount.getQq(), "qq只能为数字!");
                break;
        }
    }

    public static void validWeChat(MbrAccount mbrAccount, String validType) {
        switch (validType) {
            case SysValueConst.none:
                mbrAccount.setWeChat("");
                break;
            case SysValueConst.visible:
                if (!StringUtils.isEmpty(mbrAccount.getWeChat())) {
                    Assert.isLenght(mbrAccount.getWeChat(), "微信最长为 20位!", 0, 20);
                }
                break;
            case SysValueConst.require:
                Assert.isLenght(mbrAccount.getWeChat(), "微信最长为 20位,并且不能为空!", 1, 20);
                break;
        }
    }

    private static void validAdress(MbrAccount mbrAccount, String validType) {
        switch (validType) {
            case SysValueConst.none:
                mbrAccount.setAddress("");
                break;
            case SysValueConst.visible:
                if (!StringUtils.isEmpty(mbrAccount.getAddress())) {
                    Assert.isLenght(mbrAccount.getAddress(), "地址最长为50位!", 0, 50);
                }
                break;
            case SysValueConst.require:
                Assert.isLenght(mbrAccount.getAddress(), "地址最长为 50位,并且不能为空!", 1, 50);
                break;
        }
    }


    public static void loginVerify(LoginUserDto model, Integer ltdNo, String kaptcha) {
        Assert.isBlank(model.getLoginName(), "用户名不能为空");
        Assert.isLenght(model.getPassword(), "会员密码,长度为6~18位!", 6, 18);
        //登陆连续三次出错,需要验证码
        if (ltdNo > 2) {
            Assert.isBlank(model.getCaptcha(), "验证码不能为空");
            if (!model.getCaptcha().equalsIgnoreCase(kaptcha)) {
                Assert.message("验证码不正确");
            }
        }
    }

    public static void registerVerify(UserDto entity, MbrAccount mbrAccount, List<SysSetting> list, ApiConfig apiConfig, String kaptcha) {
        try {
            entity.setLoginName(entity.getLoginName().toLowerCase());
            BeanUtils.copyProperties(mbrAccount, entity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RRException("内部数据异常!");
        }
        ValidRegUtils.validateReg(mbrAccount, list, kaptcha);
        mbrAccount.setId(null);
        mbrAccount.setTagencyId(apiConfig.getRegDeaultTagencyId());
        mbrAccount.setCagencyId(apiConfig.getRegDeaultCagencyId());
        mbrAccount.setGroupId(apiConfig.getRegDeaultGroup());
        mbrAccount.setIsAllowEmail(Available.enable);
        mbrAccount.setIsAllowMsg(Available.enable);
        mbrAccount.setAvailable(Available.enable);
        mbrAccount.setIsVerifyEmail(Available.disable);
        mbrAccount.setIsVerifyMoblie(Available.disable);
        mbrAccount.setLoginTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        mbrAccount.setRegisterTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        mbrAccount.setIsLock(Available.disable);
        mbrAccount.setSpreadCode(entity.getSpreadCode());
    }

    public static void checkFpValid(HttpServletRequest request, JwtUtils jwtUtils) {
        // 获取用户凭证
        String token = request.getHeader(jwtUtils.getHeader());
        if (StringUtils.isBlank(token)) {
            token = request.getParameter(jwtUtils.getHeader());
        }
        // 凭证为空
        if (StringUtils.isBlank(token)) {
            throw new RRException(jwtUtils.getHeader() + "不能为空", HttpStatus.UNAUTHORIZED.value());
        }
        Claims claims = jwtUtils.getClaimByfindPwdToken(token);
        if (claims == null || jwtUtils.isTokenExpired(claims.getExpiration())) {
            throw new R200Exception("已失效,请重新申请找回密码!", HttpStatus.UNAUTHORIZED.value());
        }
        String loginName = claims.getSubject();
        // 设置userId到request里，后续根据userId，获取用户信息
        request.setAttribute(ApiConstants.USER_NAME, loginName);
    }
/*private static String getFieldValueByName(String fieldName, Object o) {  
    try {    
        String firstLetter = fieldName.substring(0, 1).toUpperCase();    
        String getter = "get" + firstLetter + fieldName.substring(1);    
        Method method = o.getClass().getMethod(getter, new Class[] {});    
        Object value = method.invoke(o, new Object[] {});    
        return String.valueOf(value);    
    } catch (Exception e) {    
        //log.error(e.getMessage(),e);    
        return null;    
    }    
}*/
/*	private static String setFieldValueByName(String fieldName, Object o) {
		try {
			String firstLetter = fieldName.substring(0, 1).toUpperCase();
			String setter = "set" + firstLetter + fieldName.substring(1);
			Method method = o.getClass().getMethod(setter, new Class[] {});
			Object value = method.invoke(o, new Object[] {});
			return String.valueOf(value);
		} catch (Exception e) {
			// log.error(e.getMessage(),e);
			return null;
		}
	}*/
	
/*	public static Object getProperty(Object beanObj, String property)
			throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// 此处应该判断beanObj,property不为null
		PropertyDescriptor pd = new PropertyDescriptor(property, beanObj.getClass(),
				"set" +captureName(property),
				"get" + captureName(property));
		Method getMethod = pd.getReadMethod();
		return getMethod.invoke(beanObj);
	}

	 该方法用于传入某实例对象以及对象方法名、修改值，通过放射调用该对象的某个set方法设置修改值 
	public static Object setProperty(Object beanObj, String property, Object value)
			throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// 此处应该判断beanObj,property不为null
		PropertyDescriptor pd = new PropertyDescriptor(property, beanObj.getClass());
		Method setMethod = pd.getWriteMethod();
		if (setMethod == null) {

		}
		return setMethod.invoke(beanObj, value);
	}
	 public static String captureName(String name) {
	        name = name.substring(0, 1).toUpperCase() + name.substring(1);
	       return  name;
	      
	    }*/
}
