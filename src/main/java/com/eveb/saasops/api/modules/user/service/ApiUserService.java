package com.eveb.saasops.api.modules.user.service;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.eveb.saasops.api.annotation.CacheDuration;
import com.eveb.saasops.api.config.ApiConfig;
import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.modules.apisys.mapper.ApiSysMapper;
import com.eveb.saasops.api.modules.user.dao.FindPwMapper;
import com.eveb.saasops.api.modules.user.dto.LoginVerifyDto;
import com.eveb.saasops.api.modules.user.dto.ModPwdDto;
import com.eveb.saasops.api.modules.user.dto.NtLoginRes;
import com.eveb.saasops.api.modules.user.dto.VfyMailOrMobDto;
import com.eveb.saasops.api.modules.user.entity.FindPwEntity;
import com.eveb.saasops.api.modules.user.mapper.ApiUserMapper;
import com.eveb.saasops.common.constants.Constants.Available;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.DateUtil;
import com.eveb.saasops.common.utils.StringUtil;
import com.eveb.saasops.config.MessagesConfig;
import com.eveb.saasops.modules.member.entity.MbrAccount;
import com.eveb.saasops.modules.member.service.MbrAccountService;
import com.eveb.saasops.modules.system.systemsetting.dto.MailSet;
import com.eveb.saasops.modules.system.systemsetting.dto.SmsSet;
import com.eveb.saasops.modules.system.systemsetting.dto.StationSet;
import com.eveb.saasops.modules.system.systemsetting.service.SysSettingService;

@Slf4j
@Service
public class ApiUserService {

    @Autowired
    ApiSysMapper ApiSysMapper;
    @Autowired
    FindPwMapper findPwMapper;
    @Autowired
    private SendMailSevice sendMailSevice;
    @Autowired
    private MessagesConfig messagesConfig;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private SendSmsSevice sendSmsSevice;
    @Autowired
    private ApiUserMapper apiUserMapper;
    @Autowired
    private MbrAccountService mbrAccountService;
    @Autowired
    private ApiConfig apiConfig;

    /**
     * 特别注意 如果加缓存 如果在同一个类中调用缓存的方法，缓存是不会起做用
     *
     * @param siteCode
     * @param loginName
     * @return
     */
    @Cacheable(cacheNames = ApiConstants.REIDS_LOGIN_PASS_KEY, key = "#siteCode+'_'+#loginName")
    public LoginVerifyDto queryPassLtdNoCache(String siteCode, String loginName) {
        LoginVerifyDto loginVerifyDto = new LoginVerifyDto();
        loginVerifyDto.setNo(0);
        return loginVerifyDto;
    }

    @CachePut(cacheNames = ApiConstants.REIDS_LOGIN_PASS_KEY, key = "#siteCode+'_'+#loginName")
    @CacheDuration(duration = 3600)//1小时
    public LoginVerifyDto updatePassLtdNoCache(String siteCode, String loginName, Integer times) {
        LoginVerifyDto loginVerifyDto = new LoginVerifyDto();
        loginVerifyDto.setNo(times + 1);
        if (loginVerifyDto.getNo() >= 5)
            loginVerifyDto.setExpireTime(DateUtil.addTime(apiConfig.getPasswdLockTime()));
        return loginVerifyDto;
    }

    @CacheEvict(cacheNames = ApiConstants.REIDS_LOGIN_PASS_KEY, key = "#siteCode+'_'+#loginName")
    public void rmPassLtdNoCache(String siteCode, String loginName) {
    }

    public String getKaptcha(HttpSession session, String key) {
        String kaptcha = "";
        if (StringUtils.isEmpty(session.getAttribute(key))) {
            throw new RRException("验证码已失效!");
        } else {
            kaptcha = session.getAttribute(key).toString();
            session.removeAttribute(key);
        }
        return kaptcha;
    }

    //账号OFFLINE
    @Cacheable(cacheNames = ApiConstants.REIDS_LOGIN_OFFLINE_KEY, key = "#siteCode+'_'+#loginName")
    @CacheDuration(duration = 300)
    public Integer queryLoginOfflineCache(String siteCode, String loginName) {
        return 1;
    }

    //账号OFFLINE更新
    @CachePut(cacheNames = ApiConstants.REIDS_LOGIN_OFFLINE_KEY, key = "#siteCode+'_'+#loginName")
    @CacheDuration(duration = 300)
    public Integer updateLoginOfflineCache(String siteCode, String loginName, Integer value) {
        return value;
    }


    //pt2 token 查询 1个小时(59分钟)
    @Cacheable(cacheNames = ApiConstants.REIDS_LOGIN_PT2TOKEN_KEY, key = "#siteCode+'_'+#username")
    @CacheDuration(duration = 3540)
    public String queryPt2LoginTokenCache(String siteCode, String username) {
        return "";
    }

    //pt2 token 保存(59分钟)
    @CachePut(cacheNames = ApiConstants.REIDS_LOGIN_PT2TOKEN_KEY, key = "#siteCode+'_'+#username")
    @CacheDuration(duration = 3540)
    public String updatePt2LoginTokenCache(String siteCode, String username, String token) {
        return token;
    }

    //nt token 查询 半个小时(29分钟)
    @Cacheable(cacheNames = ApiConstants.REIDS_LOGIN_NTTOKEN_KEY, key = "#siteCode+'_'+#username")
    @CacheDuration(duration = 1740)
    public NtLoginRes queryNtLoginTokenCache(String siteCode, String username) {
        return null;
    }

    //nt token 保存 半个小时(29分钟)
    @CachePut(cacheNames = ApiConstants.REIDS_LOGIN_NTTOKEN_KEY, key = "#siteCode+'_'+#username")
    @CacheDuration(duration = 3540)
    public NtLoginRes updateNtLoginTokenCache(String siteCode, String username, NtLoginRes loginRes) {
        return loginRes;
    }

    //Png token 查询 半个小时(59分钟)
    @Cacheable(cacheNames = ApiConstants.REIDS_LOGIN_PNGTOKEN_KEY, key = "#prefix+'_'+#username")
    @CacheDuration(duration = 1740)
    public String queryPngLoginTokenCache(String prefix, String username) {
        return null;
    }

    //nt token 保存 半个小时(59分钟)
    @CachePut(cacheNames = ApiConstants.REIDS_LOGIN_PNGTOKEN_KEY, key = "#prefix+'_'+#username")
    @CacheDuration(duration = 1740)
    public String updatePngLoginTokenCache(String prefix, String username, String token) {
        return token;
    }

    //TOKEN 查询 半小时后失效
    @Cacheable(cacheNames = ApiConstants.REIDS_LOGIN_TOKEN_KEY, key = "#siteCode+'_'+#loginName")
    @CacheDuration(duration = 1800)
    public String queryLoginTokenCache(String siteCode, String loginName) {
        return "";
    }

    //TOKEN 保存 半小时后失效
    @CachePut(cacheNames = ApiConstants.REIDS_LOGIN_TOKEN_KEY, key = "#siteCode+'_'+#loginName")
    @CacheDuration(duration = 1800)
    public String updateLoginTokenCache(String siteCode, String loginName, String token) {
        return token;
    }

    //TOKEN 删除
    @CacheEvict(cacheNames = ApiConstants.REIDS_LOGIN_TOKEN_KEY, key = "#siteCode+'_'+#loginName")
    public void rmLoginTokenCache(String siteCode, String loginName) {
    }


    //邮箱或手机验证CODE
    @Cacheable(cacheNames = ApiConstants.REIDS_VFYMAILORMOB_CODE_KEY, key = "#siteCode+'_'+#loginName")
    //@CacheDuration(duration = 1800)//半个小时有效
    public VfyMailOrMobDto queryVfyMailOrMobCodeCache(String siteCode, String loginName) {
        return null;
    }

    //邮箱或手机验证CODE 保存
    @CachePut(cacheNames = ApiConstants.REIDS_VFYMAILORMOB_CODE_KEY, key = "#siteCode+'_'+#loginName")
    @CacheDuration(duration = 1800)//半个小时有效
    public VfyMailOrMobDto updateVfyMailOrMobCodeCache(String siteCode, String loginName, VfyMailOrMobDto vfyCode) {
        return vfyCode;
    }

    @Transactional
    public void saveMailCode(FindPwEntity findPw, String siteCode) {
        MbrAccount mbrAccount = mbrAccountService.getAccountInfo(findPw.getLoginName());
        if (StringUtil.isEmpty(mbrAccount.getEmail()) || mbrAccount.getIsVerifyEmail().equals(Available.disable))
            throw new RRException("此账号不能使用邮箱找回密码!");
        String code = sendMail(siteCode, mbrAccount.getEmail(), messagesConfig.getValue("api.fp.email.subject"), messagesConfig.getValue("api.fp.email.content"));
        if (StringUtils.isEmpty(code)) throw new RRException("发送邮件失败!");
        findPw.setVaildCode(code);
        apiUserMapper.insertFindPwd(findPw);
    }

    @Transactional
    public void saveSmsCode(FindPwEntity findPw, String siteCode) {
        MbrAccount mbrAccount = mbrAccountService.getAccountInfo(findPw.getLoginName());
        if (StringUtil.isEmpty(mbrAccount.getMobile()) || mbrAccount.getIsVerifyMoblie().equals(Available.disable))
            throw new RRException("此账号不能使用手机号码找回密码!");
        String code = sendSms(siteCode, mbrAccount.getMobile());
        findPw.setVaildCode(code);
        apiUserMapper.insertFindPwd(findPw);
    }

    @Transactional
    public boolean validCode(String code, String loginName) {
        FindPwEntity entity = new FindPwEntity();
        entity.setLoginName(loginName);
        entity = findPwMapper.selectOne(entity);
        if (entity == null)
            throw new RRException("没有下发验证码!");
        if (entity.getVaildTimes() > 2)
            throw new RRException("请不要重复尝试,错误的验证码!");
        if (!entity.getVaildCode().equals(code)) {
            entity.setVaildTimes(entity.getVaildTimes() + 1);
            findPwMapper.updateByPrimaryKey(entity);
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }

    @Transactional
    public boolean modPwd(ModPwdDto modPwdDto) {
        if (validCode(modPwdDto.getCode(), modPwdDto.getLoginName())) {
            MbrAccount info = mbrAccountService.getAccountInfo(modPwdDto.getLoginName());
            String salt = info.getSalt();
            MbrAccount mbrAccount = new MbrAccount();
            mbrAccount.setLoginPwd(new Sha256Hash(modPwdDto.getPassword(), salt).toHex());
            mbrAccount.setId(info.getId());
            mbrAccountService.update(mbrAccount);
            FindPwEntity entity = new FindPwEntity();
            entity.setLoginName(modPwdDto.getLoginName());
            findPwMapper.delete(entity);
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @Transactional
    public String sendVfyMailCode(VfyMailOrMobDto vfyDto, String siteCode, Integer userId) {
        MbrAccount mbrAccount = mbrAccountService.getAccountInfo(userId);
        if (!StringUtils.isEmpty(mbrAccount.getIsVerifyEmail()) && mbrAccount.getIsVerifyEmail() == Available.enable) {
            throw new RRException("邮箱已验证,请不要重复验证!");
        }
        return sendMail(siteCode, vfyDto.getEmail(), messagesConfig.getValue("api.fp.vfyemail.subject"), messagesConfig.getValue("api.fp.vfyemail.content"));
    }

    @Transactional
    public String sendVfySmsCode(VfyMailOrMobDto vfyDto, String siteCode, Integer userId) {
        MbrAccount mbrAccount = mbrAccountService.getAccountInfo(userId);
        if (!StringUtils.isEmpty(mbrAccount.getIsVerifyMoblie()) && mbrAccount.getIsVerifyMoblie() == Available.enable)
            throw new RRException("手机号码已验证,请不要重复验证!");
        return sendSms(siteCode, vfyDto.getMobile());

    }

    private String sendSms(String siteCode, String mobile) {
        SmsSet smsSet = sysSettingService.getSmsSet(siteCode);
        Map<String, String> params = new HashMap<String, String>();
        String code = CommonUtil.getRandomCode();
        String content = smsSet.getSmsTemplate().replace("{0}", code).toString();
        params.put("content", content);
        params.put("account", smsSet.getSmsInterfaceName());
        params.put("password", smsSet.getSmsInterfacePassword());
        params.put("mobile", mobile);
        params.put("format", "json");
        sendSmsSevice.sendSms(params, smsSet.getSmsGetwayAddress());
        return code;
    }

    private String sendMail(String siteCode, String mail, String subject, String content) {
        MailSet mailSet = sysSettingService.getMailSet(siteCode);
        StationSet stationSet = sysSettingService.getStation(siteCode);
        String code = CommonUtil.getRandomCode();
        subject = subject.replace("#gameName", stationSet.getWebsiteTitle());
        content = content.replace("#gameName", stationSet.getWebsiteTitle()).replace("#code", code);
        boolean flag = sendMailSevice.sendMail(mailSet, mail, subject, content);
        if (flag)
            return code;
        else
            return null;
    }
}
