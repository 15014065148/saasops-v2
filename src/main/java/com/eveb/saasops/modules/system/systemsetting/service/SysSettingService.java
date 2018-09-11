package com.eveb.saasops.modules.system.systemsetting.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.utils.Collections3;
import com.eveb.saasops.modules.system.mapper.SetBasicSysMapper;
import com.eveb.saasops.modules.system.systemsetting.dto.*;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.modules.user.service.SendMailSevice;
import com.eveb.saasops.api.modules.user.service.SendSmsSevice;
import com.eveb.saasops.common.constants.SystemConstants;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.QiNiuYunUtil;
import com.eveb.saasops.modules.system.systemsetting.dao.StationSetMapper;
import com.eveb.saasops.modules.system.systemsetting.dao.SysSettingMapper;
import com.eveb.saasops.modules.system.systemsetting.entity.SysSetting;
import com.eveb.saasops.modules.system.systemsetting.entity.SysSetting.SysValueConst;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

@Service("sysSettingService")
@Transactional
public class SysSettingService {
    @Autowired
    private SysSettingMapper sysSettingMapper;
    @Autowired
    private SendSmsSevice sendSmsSevice;
    @Autowired
    private SendMailSevice sendMailSevice;
    @Autowired
    private QiNiuYunUtil qiNiuYunUtil;
    @Autowired
    private SetBasicSysMapper setBasicSysMapper;
    @Autowired
    private StationSetMapper stationSetMapper;

    public SysSetting queryObject(String syskey) {
        return sysSettingMapper.selectByPrimaryKey(syskey);
    }

    // 获取会员注册设置
    public List<SysSetting> getRegisterInfoList() {

        List<SysSetting> list = sysSettingMapper.selectAll();
        List<SysSetting> ssList = new ArrayList<>();
        List<String> keys = Lists.newArrayList(SystemConstants.MEMBER_ACCOUNT, SystemConstants.MEMBER_LOGIN_PASSWORD,
                SystemConstants.MEMBER_REPEATED_PASSWORD, SystemConstants.MEMBER_VERIFICATION_CODE,
                SystemConstants.MEMBER_REAL_NAME, SystemConstants.MEMBER_TELPHONE, SystemConstants.MEMBER_EMAIL,
                SystemConstants.MEMBER_QQ, SystemConstants.MEMBER_WECHAT, SystemConstants.MEMBER_ADDRESS);
        if (list != null) {
            for (SysSetting ss : list) {
                if (keys.contains(ss.getSyskey())) {
                    ssList.add(ss);
                }
            }
        }
        return ssList;
    }

    public LinkedHashMap<String, Boolean> getRegisterInfoMap() {
        List<SysSetting> ssList = getRegisterInfoList();
        LinkedHashMap<String, Boolean> map = getMap(ssList);
        return map;
    }

    public LinkedHashMap<String, Boolean> getMap(List<SysSetting> ssList) {
        LinkedHashMap<String, Boolean> map = new LinkedHashMap<>();
        if (ssList != null) {
            for (SysSetting ss : ssList) {
                if (SysValueConst.none.equals(ss.getSysvalue())) {
                    map.put(ss.getSyskey() + SystemConstants.KEY_ISVISIBLE, false);
                    map.put(ss.getSyskey() + SystemConstants.KEY_ISREQUIRE, false);
                } else if (SysValueConst.visible.equals(ss.getSysvalue())) {
                    map.put(ss.getSyskey() + SystemConstants.KEY_ISVISIBLE, true);
                    map.put(ss.getSyskey() + SystemConstants.KEY_ISREQUIRE, false);
                } else if (SysValueConst.require.equals(ss.getSysvalue())) {
                    map.put(ss.getSyskey() + SystemConstants.KEY_ISVISIBLE, true);
                    map.put(ss.getSyskey() + SystemConstants.KEY_ISREQUIRE, true);
                }
            }
        }
        return map;
    }

    public void save(SysSetting sysSetting) {
        sysSettingMapper.insert(sysSetting);
    }

    public void update(SysSetting sysSetting) {
        sysSettingMapper.updateByPrimaryKeySelective(sysSetting);
    }

    public void delete(String syskey) {
        sysSettingMapper.deleteByPrimaryKey(syskey);
    }

    // 保存或更新用户条款
    public void modifyOrUpdate(List<SysSetting> ssList) {
        if (ssList != null) {
            for (SysSetting ss : ssList) {
                if (null != ss.getSyskey() && null != ss.getSysvalue()) {
                    SysSetting sys = new SysSetting();
                    sys.setSyskey(ss.getSyskey());
                    SysSetting s01 = sysSettingMapper.selectOne(sys);
                    if (null == s01) {
                        sysSettingMapper.insert(ss);
                    } else {
                        sysSettingMapper.updateByPrimaryKeySelective(ss);
                    }
                }
                if (null != ss.getWebsiteTerms() && null != ss.getSyskey()) {
                    SysSetting sys = new SysSetting();
                    sys.setSyskey(ss.getSyskey());
                    SysSetting s01 = sysSettingMapper.selectOne(sys);
                    if (null == s01) {
                        sysSettingMapper.insert(ss);
                    } else {
                        sysSettingMapper.updateByPrimaryKeySelective(ss);
                    }
                }
            }
        }
    }

    // 获取用户协议条款
    @Cacheable(cacheNames = ApiConstants.REDIS_PROTOCOL_USER_KEY, key = "#siteCode")
    public SysWebTerms getMbrSysWebTerms(String siteCode) {
        Map<String, String> map = getWebsiteTerms();
        SysWebTerms swt = new SysWebTerms();
        Set<String> set = map.keySet();
        if (null != set) {
            for (String key : set) {
                if (key.equals(SystemConstants.MEMBER_REGISTER_DISPLAY_TERMS_OF_WEBSITE)) {
                    swt.setDisplay(map.get(key));
                }
                if (key.equals(SystemConstants.MEMBER_SERVICE_TERMS_OF_WEBSITE)) {
                    swt.setServiceTerms(map.get(key));
                }
            }
        }
        return swt;
    }

    public Map<String, String> getWebsiteTerms() {
        List<SysSetting> ssList = sysSettingMapper.selectAll();
        Map<String, String> map = new HashMap<>();
        List<String> tKeys = Lists.newArrayList(SystemConstants.MEMBER_SERVICE_TERMS_OF_WEBSITE,
                SystemConstants.AGENT_SERVICE_TERMS_OF_WEBSITE);

        List<String> dKeys = Lists.newArrayList(SystemConstants.MEMBER_REGISTER_DISPLAY_TERMS_OF_WEBSITE,
                SystemConstants.AGENT_REGISTER_DISPLAY_TERMS_OF_WEBSITE);

        if (ssList != null) {
            for (SysSetting ss : ssList) {
                if (tKeys.contains(ss.getSyskey())) {
                    map.put(ss.getSyskey(), ss.getWebsiteTerms());
                }
                if (dKeys.contains(ss.getSyskey())) {
                    map.put(ss.getSyskey(), ss.getSysvalue());
                }
            }
        }
        return map;
    }

    // 获取站点设置信息
    @Cacheable(cacheNames = ApiConstants.REIDS_STATION_SET_KEY, key = "#siteCode")
    public StationSet getStation(String siteCode) {
        List<SysSetting> ssList = sysSettingMapper.selectAll();
        StationSet stationSet = new StationSet();
        if (null != ssList) {
            for (SysSetting ss : ssList) {
                if (SystemConstants.DEFAULT_QUERY_DAYS.equals(ss.getSyskey())) {
                    stationSet.setDefaultQueryDays(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.MEMBER_QUERY_DAYS.equals(ss.getSyskey())) {
                    stationSet.setMemberQueryDays(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.PASSWORD_EXPIRE_DAYS.equals(ss.getSyskey())) {
                    stationSet.setPasswordExpireDays(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.LOGO_PATH.equals(ss.getSyskey())) {
                    stationSet.setLogoPath(ss.getSysvalue());
                }
                if (SystemConstants.TITLE_PATH.equals(ss.getSyskey())) {
                    stationSet.setTitlePath(ss.getSysvalue());
                }
                if (SystemConstants.WEBSITE_TITLE.equals(ss.getSyskey())) {
                    stationSet.setWebsiteTitle(ss.getSysvalue());
                }
                if (SystemConstants.WEBSITE_KEYWORDS.equals(ss.getSyskey())) {
                    stationSet.setWebsiteKeywords(ss.getSysvalue());
                }
                if (SystemConstants.WEBSITE_DESCRIPTION.equals(ss.getSyskey())) {
                    stationSet.setWebsiteDescription(ss.getSysvalue());
                }
                if (SystemConstants.LOGO_PATH.equals(ss.getSyskey())) {
                    stationSet.setLogoPath(ss.getSysvalue());
                }
                if (SystemConstants.TITLE_PATH.equals(ss.getSyskey())) {
                    stationSet.setTitlePath(ss.getSysvalue());
                }
                if (SystemConstants.CONFIG_CODE_PC.equals(ss.getSyskey())) {
                    stationSet.setConfigCodePc(ss.getSysvalue());
                }
                if (SystemConstants.CONFIG_CODE_MB.equals(ss.getSyskey())) {
                    stationSet.setConfigCodeMb(ss.getSysvalue());
                }
                if (SystemConstants.AUTO_DELETE_DAYS.equals(ss.getSyskey())) {
                    stationSet.setAutoDeleteDays(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.WEBSITE_CODE_MB.equals(ss.getSyskey())) {
                    stationSet.setWebsiteCodeMb(ss.getSysvalue());
                }
                if (SystemConstants.WEBSITE_CODE_PC.equals(ss.getSyskey())) {
                    stationSet.setWebsiteCodePc(ss.getSysvalue());
                }
            }
        }
        return stationSet;
    }

    // 获取邮件设置信息
    @Cacheable(cacheNames = ApiConstants.REIDS_MAIL_SET_KEY, key = "#siteCode")
    public MailSet getMailSet(String siteCode) {
        List<SysSetting> ssList = sysSettingMapper.selectAll();
        MailSet ms = new MailSet();
        if (null != ssList) {
            for (SysSetting ss : ssList) {
                if (SystemConstants.MAIL_SEND_SERVER.equals(ss.getSyskey())) {
                    ms.setMailSendServer(ss.getSysvalue());
                }
                if (SystemConstants.MAIL_SEND_PORT.equals(ss.getSyskey())) {
                    ms.setMailSendPort(ss.getSysvalue());
                }
                if (SystemConstants.MAIL_SEND_ACCOUNT.equals(ss.getSyskey())) {
                    ms.setMailSendAccount(ss.getSysvalue());
                }
                if (SystemConstants.MAIL_PASSWORD.equals(ss.getSyskey())) {
                    ms.setMailPassword(ss.getSysvalue());
                }
                if (SystemConstants.WETHER_SSL.equals(ss.getSyskey())) {
                    ms.setWetherSsl(ss.getSysvalue());
                }
                if (SystemConstants.CHARACTER_SET.equals(ss.getSyskey())) {
                    ms.setCharacterSet(ss.getSysvalue());
                }
            }
        }
        return ms;
    }

    // 获取短信设置信息
    @Cacheable(cacheNames = ApiConstants.REIDS_SMS_SET_KEY, key = "#siteCode")
    public SmsSet getSmsSet(String siteCode) {
        List<SysSetting> ssList = sysSettingMapper.selectAll();
        SmsSet smsSet = new SmsSet();
        if ((null != ssList)) {
            for (SysSetting ss : ssList) {
                if (SystemConstants.SMS_GETWAY_ADDRESS.equals(ss.getSyskey())) {
                    smsSet.setSmsGetwayAddress(ss.getSysvalue());
                }
                if (SystemConstants.SMS_INTERFACE_NAME.equals(ss.getSyskey())) {
                    smsSet.setSmsInterfaceName(ss.getSysvalue());
                }
                if (SystemConstants.SMS_INTERFACE_PASSWORD.equals(ss.getSyskey())) {
                    smsSet.setSmsInterfacePassword(ss.getSysvalue());
                }
                if (SystemConstants.SMS_SEND_NAME.equals(ss.getSyskey())) {
                    smsSet.setSmsSendName(ss.getSysvalue());
                }
                if (SystemConstants.SMS_TEMPLATE.equals(ss.getSyskey())) {
                    smsSet.setSmsTemplate(ss.getSysvalue());
                }
            }
        }
        return smsSet;
    }

    public RegisterSet queryRegisterSet() {
        List<SysSetting> ssList = sysSettingMapper.selectAll();
        RegisterSet registerSet = new RegisterSet();
        if (null != ssList) {
            for (SysSetting ss : ssList) {
                if (SystemConstants.MEMBER_ACCOUNT.equals(ss.getSyskey())) {
                    registerSet.setLoginName(Integer.parseInt(ss.getSysvalue()));
                }

                if (SystemConstants.MEMBER_LOGIN_PASSWORD.equals(ss.getSyskey())) {
                    registerSet.setLoginPwd(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.MEMBER_REPEATED_PASSWORD.equals(ss.getSyskey())) {
                    registerSet.setReLoginPwd(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.MEMBER_VERIFICATION_CODE.equals(ss.getSyskey())) {
                    registerSet.setCaptchareg(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.MEMBER_REAL_NAME.equals(ss.getSyskey())) {
                    registerSet.setRealName(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.MEMBER_TELPHONE.equals(ss.getSyskey())) {
                    registerSet.setMobile(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.MEMBER_EMAIL.equals(ss.getSyskey())) {
                    registerSet.setEmail(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.MEMBER_QQ.equals(ss.getSyskey())) {
                    registerSet.setQq(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.MEMBER_WECHAT.equals(ss.getSyskey())) {
                    registerSet.setWeChat(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.MEMBER_ADDRESS.equals(ss.getSyskey())) {
                    registerSet.setAddress(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.AGENT_ACCOUNT.equals(ss.getSyskey())) {
                    registerSet.setAgentLoginName(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.AGENT_LOGIN_PASSWORD.equals(ss.getSyskey())) {
                    registerSet.setAgentLoginPwd(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.AGENT_REPEATED_PASSWORD.equals(ss.getSyskey())) {
                    registerSet.setAgentReLoginPwd(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.AGENT_VERIFICATION_CODE.equals(ss.getSyskey())) {
                    registerSet.setAgentCaptchareg(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.AGENT_REAL_NAME.equals(ss.getSyskey())) {
                    registerSet.setAgentRealName(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.AGENT_TELPHONE.equals(ss.getSyskey())) {
                    registerSet.setAgentMobile(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.AGENT_EMAIL.equals(ss.getSyskey())) {
                    registerSet.setAgentEmail(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.AGENT_QQ.equals(ss.getSyskey())) {
                    registerSet.setAgentQQ(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.AGENT_WECHAT.equals(ss.getSyskey())) {
                    registerSet.setAgentWechat(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.AGENT_ADDRESS.equals(ss.getSyskey())) {
                    registerSet.setAgentAddress(Integer.parseInt(ss.getSysvalue()));
                }
            }
        }
        return registerSet;
    }

    public void saveStationSet(String str) {
        Gson gson = new Gson();
        StationSet stationSet = gson.fromJson(str, StationSet.class);
        if (null != stationSet) {
            if (null != stationSet.getDefaultQueryDays()) {
                String key = SystemConstants.DEFAULT_QUERY_DAYS;
                String value = Integer.toString(stationSet.getDefaultQueryDays());
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getMemberQueryDays()) {
                String key = SystemConstants.MEMBER_QUERY_DAYS;
                String value = Integer.toString(stationSet.getMemberQueryDays());
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getAutoDeleteDays()) {
                String key = SystemConstants.AUTO_DELETE_DAYS;
                String value = Integer.toString(stationSet.getAutoDeleteDays());
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getPasswordExpireDays()) {
                String key = SystemConstants.PASSWORD_EXPIRE_DAYS;
                String value = Integer.toString(stationSet.getPasswordExpireDays());
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getWebsiteCodeMb()) {
                String key = SystemConstants.WEBSITE_CODE_MB;
                String value = stationSet.getWebsiteCodeMb();
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getWebsiteCodePc()) {
                String key = SystemConstants.WEBSITE_CODE_PC;
                String value = stationSet.getWebsiteCodePc();
                modifyOrUpdate(key, value);
            }

            if (null != stationSet.getWebsiteTitle()) {
                String key = SystemConstants.WEBSITE_TITLE;
                String value = stationSet.getWebsiteTitle();
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getWebsiteKeywords()) {
                String key = SystemConstants.WEBSITE_KEYWORDS;
                String value = stationSet.getWebsiteKeywords();
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getWebsiteDescription()) {
                String key = SystemConstants.WEBSITE_DESCRIPTION;
                String value = stationSet.getWebsiteDescription();
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getConfigCodeMb()) {
                String key = SystemConstants.CONFIG_CODE_MB;
                String value = stationSet.getConfigCodeMb();
                modifyOrUpdate(key, value);
            }
            if (null != stationSet.getConfigCodePc()) {
                String key = SystemConstants.CONFIG_CODE_PC;
                String value = stationSet.getConfigCodePc();
                modifyOrUpdate(key, value);
            }
        }
    }

    public void modifyPic(MultipartFile titlePicFile, MultipartFile logoPicFile) {
        if (Objects.nonNull(titlePicFile)) {
            String fileName = null;
            try {
                String prefix = titlePicFile.getOriginalFilename()
                        .substring(titlePicFile.getOriginalFilename().indexOf("."));
                byte[] fileBuff = IOUtils.toByteArray(titlePicFile.getInputStream());
                fileName = qiNiuYunUtil.uploadFile(fileBuff, UUID.randomUUID().toString() + prefix);
            } catch (Exception e) {
                throw new RRException(e.getMessage());
            }

            modifyPicFile(SystemConstants.TITLE_PATH, fileName);
        }
        if (Objects.nonNull(logoPicFile)) {
            String fileName = null;
            try {
                String prefix = logoPicFile.getOriginalFilename()
                        .substring(logoPicFile.getOriginalFilename().indexOf("."));
                byte[] fileBuff = IOUtils.toByteArray(logoPicFile.getInputStream());
                fileName = qiNiuYunUtil.uploadFile(fileBuff, UUID.randomUUID().toString() + prefix);
            } catch (Exception e) {
                throw new RRException(e.getMessage());
            }
            modifyPicFile(SystemConstants.LOGO_PATH, fileName);
        }
    }

    private void modifyPicFile(String key, String fileName) {
        SysSetting ss = sysSettingMapper.selectByPrimaryKey(key);
        SysSetting record = new SysSetting();
        record.setSyskey(key);
        record.setSysvalue(fileName);

        if (null == ss) {
            sysSettingMapper.insertSelective(record);
        } else {
            sysSettingMapper.updateByPrimaryKey(record);
        }
    }

    public void saveMailSet(MailSet mailSet) {
        if (mailSet != null) {
            if (null != mailSet.getMailSendServer()) {
                String key = SystemConstants.MAIL_SEND_SERVER;
                String value = mailSet.getMailSendServer();
                modifyOrUpdate(key, value);
            }
            if (null != mailSet.getMailSendPort()) {
                String key = SystemConstants.MAIL_SEND_PORT;
                String value = mailSet.getMailSendPort();
                modifyOrUpdate(key, value);
            }
            if (null != mailSet.getMailSendAccount()) {
                String key = SystemConstants.MAIL_SEND_ACCOUNT;
                String value = mailSet.getMailSendAccount();
                modifyOrUpdate(key, value);
            }
            if (null != mailSet.getMailPassword()) {
                String key = SystemConstants.MAIL_PASSWORD;
                String value = mailSet.getMailPassword();
                modifyOrUpdate(key, value);
            }
            if (null != mailSet.getWetherSsl()) {
                String key = SystemConstants.WETHER_SSL;
                String value = mailSet.getWetherSsl();
                modifyOrUpdate(key, value);
            }
            if (null != mailSet.getCharacterSet()) {
                String key = SystemConstants.CHARACTER_SET;
                String value = mailSet.getWetherSsl();
                modifyOrUpdate(key, value);
            }
        }
    }

    public void saveSmsSet(SmsSet smsSet) {
        if (null != smsSet) {
            if (null != smsSet.getSmsGetwayAddress()) {
                String key = SystemConstants.SMS_GETWAY_ADDRESS;
                String value = smsSet.getSmsGetwayAddress();
                modifyOrUpdate(key, value);
            }
            if (null != smsSet.getSmsInterfaceName()) {
                String key = SystemConstants.SMS_INTERFACE_NAME;
                String value = smsSet.getSmsInterfaceName();
                modifyOrUpdate(key, value);
            }
            if (null != smsSet.getSmsInterfacePassword()) {
                String key = SystemConstants.SMS_INTERFACE_PASSWORD;
                String value = smsSet.getSmsInterfacePassword();
                modifyOrUpdate(key, value);
            }
            if (null != smsSet.getSmsSendName()) {
                String key = SystemConstants.SMS_SEND_NAME;
                String value = smsSet.getSmsSendName();
                modifyOrUpdate(key, value);
            }
            if (null != smsSet.getSmsTemplate()) {
                String key = SystemConstants.SMS_TEMPLATE;
                String value = smsSet.getSmsTemplate();
                modifyOrUpdate(key, value);
            }
        }
    }

    public void saveRegSet(RegisterSet registerSet) {
        if (null != registerSet) {
            if (null != registerSet.getLoginName()) {
                String key = SystemConstants.MEMBER_ACCOUNT;
                String value = Integer.toString(registerSet.getLoginName());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getLoginPwd()) {
                String key = SystemConstants.MEMBER_LOGIN_PASSWORD;
                String value = Integer.toString(registerSet.getLoginPwd());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getReLoginPwd()) {
                String key = SystemConstants.MEMBER_REPEATED_PASSWORD;
                String value = Integer.toString(registerSet.getReLoginPwd());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getCaptchareg()) {
                String key = SystemConstants.MEMBER_VERIFICATION_CODE;
                String value = Integer.toString(registerSet.getCaptchareg());
                modifyOrUpdate(key, value);
            }

            if (null != registerSet.getRealName()) {
                String key = SystemConstants.MEMBER_REAL_NAME;
                String value = Integer.toString(registerSet.getRealName());
                modifyOrUpdate(key, value);
            }

            if (null != registerSet.getMobile()) {
                String key = SystemConstants.MEMBER_TELPHONE;
                String value = Integer.toString(registerSet.getMobile());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getEmail()) {
                String key = SystemConstants.MEMBER_EMAIL;
                String value = Integer.toString(registerSet.getEmail());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getQq()) {
                String key = SystemConstants.MEMBER_QQ;
                String value = Integer.toString(registerSet.getQq());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getWeChat()) {
                String key = SystemConstants.MEMBER_WECHAT;
                String value = Integer.toString(registerSet.getWeChat());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getAddress()) {
                String key = SystemConstants.MEMBER_ADDRESS;
                String value = Integer.toString(registerSet.getAddress());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getLoginName()) {
                String key = SystemConstants.AGENT_ACCOUNT;
                String value = Integer.toString(registerSet.getLoginName());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getAgentLoginPwd()) {
                String key = SystemConstants.AGENT_LOGIN_PASSWORD;
                String value = Integer.toString(registerSet.getAgentLoginPwd());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getAgentReLoginPwd()) {
                String key = SystemConstants.AGENT_REPEATED_PASSWORD;
                String value = Integer.toString(registerSet.getAgentReLoginPwd());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getAgentCaptchareg()) {
                String key = SystemConstants.AGENT_VERIFICATION_CODE;
                String value = Integer.toString(registerSet.getAgentCaptchareg());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getAgentRealName()) {
                String key = SystemConstants.AGENT_REAL_NAME;
                String value = Integer.toString(registerSet.getAgentRealName());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getAgentMobile()) {
                String key = SystemConstants.AGENT_TELPHONE;
                String value = Integer.toString(registerSet.getAgentMobile());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getAgentEmail()) {
                String key = SystemConstants.AGENT_EMAIL;
                String value = Integer.toString(registerSet.getAgentEmail());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getAgentQQ()) {
                String key = SystemConstants.AGENT_QQ;
                String value = Integer.toString(registerSet.getAgentQQ());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getAgentWechat()) {
                String key = SystemConstants.AGENT_WECHAT;
                String value = Integer.toString(registerSet.getAgentWechat());
                modifyOrUpdate(key, value);
            }
            if (null != registerSet.getAgentAddress()) {
                String key = SystemConstants.AGENT_ADDRESS;
                String value = Integer.toString(registerSet.getAgentAddress());
                modifyOrUpdate(key, value);
            }

        }
    }

    public void modifyOrUpdate(String key, String value) {
        SysSetting ss = new SysSetting();
        ss.setSyskey(key);
        ss.setSysvalue(value);
        SysSetting record = new SysSetting();
        record.setSyskey(key);
        SysSetting re = sysSettingMapper.selectOne(record);
        if (null != re) {
            sysSettingMapper.updateByPrimaryKeySelective(ss);
        } else {
            sysSettingMapper.insertSelective(ss);
        }
    }

    public void modifyOrUpdate01(String key, String value) {
        SysSetting ss = new SysSetting();
        ss.setSyskey(key);
        ss.setWebsiteTerms(value);
        SysSetting record = new SysSetting();
        record.setSyskey(key);
        SysSetting re = sysSettingMapper.selectOne(record);
        if (null != re) {
            sysSettingMapper.updateByPrimaryKeySelective(ss);
        } else {
            sysSettingMapper.insertSelective(ss);
        }
    }

    public StationSet queryConfigDaysAndScope() {
        StationSet stationSet = stationSetMapper.queryConfigDaysAndScope();
        return stationSet;
    }

    public StationSet queryStationSet() {
        List<SysSetting> list = sysSettingMapper.selectAll();
        StationSet stationSet = new StationSet();
        for (SysSetting ss : list) {
            if (SystemConstants.DEFAULT_QUERY_DAYS.equals(ss.getSyskey())) {
                stationSet.setDefaultQueryDays(Integer.parseInt(ss.getSysvalue()));
            }
            if (SystemConstants.MEMBER_QUERY_DAYS.equals(ss.getSyskey())) {
                stationSet.setMemberQueryDays(Integer.parseInt(ss.getSysvalue()));
            }
            if (SystemConstants.PASSWORD_EXPIRE_DAYS.equals(ss.getSyskey())) {
                stationSet.setPasswordExpireDays(Integer.parseInt(ss.getSysvalue()));
            }
            if (SystemConstants.WEBSITE_CODE_PC.equals(ss.getSyskey())) {
                stationSet.setWebsiteCodePc(ss.getSysvalue());
            }
            if (SystemConstants.WEBSITE_CODE_MB.equals(ss.getSyskey())) {
                stationSet.setWebsiteCodeMb(ss.getSysvalue());
            }
            if (SystemConstants.WEBSITE_TITLE.equals(ss.getSyskey())) {
                stationSet.setWebsiteTitle(ss.getSysvalue());
            }
            if (SystemConstants.WEBSITE_KEYWORDS.equals(ss.getSyskey())) {
                stationSet.setWebsiteKeywords(ss.getSysvalue());
            }
            if (SystemConstants.WEBSITE_DESCRIPTION.equals(ss.getSyskey())) {
                stationSet.setWebsiteDescription(ss.getSysvalue());
            }
            if (SystemConstants.CONFIG_CODE_PC.equals(ss.getSyskey())) {
                stationSet.setConfigCodePc(ss.getSysvalue());
            }
            if (SystemConstants.CONFIG_CODE_MB.equals(ss.getSyskey())) {
                stationSet.setConfigCodeMb(ss.getSysvalue());
            }

            if (SystemConstants.AUTO_DELETE_DAYS.equals(ss.getSyskey())) {
                stationSet.setAutoDeleteDays(Integer.parseInt(ss.getSysvalue()));
            }
            if (SystemConstants.WEBSITE_CODE_PC.equals(ss.getSyskey())) {
                stationSet.setWebsiteCodePc(ss.getSysvalue());
            }
            if (SystemConstants.WEBSITE_CODE_MB.equals(ss.getSyskey())) {
                stationSet.setWebsiteCodeMb(ss.getSysvalue());
            }

            if (SystemConstants.TITLE_PATH.equals(ss.getSyskey())) {
                String path = ss.getSysvalue();
                stationSet.setTitlePath(path);
            }
            if (SystemConstants.LOGO_PATH.equals(ss.getSyskey())) {
                String path = ss.getSysvalue();
                stationSet.setLogoPath(path);
            }
        }
        return stationSet;
    }

    public String getCustomerSerUrl(Byte terminal) {
        if (!StringUtils.isEmpty(terminal) && terminal == ApiConstants.Terminal.mobile) {
            return queryStationSet().getConfigCodeMb();
        } else {
            return queryStationSet().getConfigCodePc();
        }
    }

    public MailSet queryMaliSet() {
        List<SysSetting> list = sysSettingMapper.selectAll();
        MailSet mailSet = new MailSet();
        for (SysSetting ss : list) {
            if (SystemConstants.MAIL_SEND_SERVER.equals(ss.getSyskey())) {
                mailSet.setMailSendServer(ss.getSysvalue());
            }
            if (SystemConstants.MAIL_SEND_PORT.equals(ss.getSyskey())) {
                mailSet.setMailSendPort(ss.getSysvalue());
            }
            if (SystemConstants.MAIL_SEND_ACCOUNT.equals(ss.getSyskey())) {
                mailSet.setMailSendAccount(ss.getSysvalue());
            }
            if (SystemConstants.MAIL_PASSWORD.equals(ss.getSyskey())) {
                mailSet.setMailPassword(ss.getSysvalue());
            }
            if (SystemConstants.WETHER_SSL.equals(ss.getSyskey())) {
                mailSet.setWetherSsl(ss.getSysvalue());
            }
            if (SystemConstants.CHARACTER_SET.equals(ss.getSyskey())) {
                mailSet.setCharacterSet(ss.getSysvalue());
            }
        }
        return mailSet;
    }

    public SmsSet querySmsSet() {
        List<SysSetting> list = sysSettingMapper.selectAll();
        SmsSet smsSet = new SmsSet();
        for (SysSetting ss : list) {
            if (SystemConstants.SMS_GETWAY_ADDRESS.equals(ss.getSyskey())) {
                smsSet.setSmsGetwayAddress(ss.getSysvalue());
            }
            if (SystemConstants.SMS_INTERFACE_NAME.equals(ss.getSyskey())) {
                smsSet.setSmsInterfaceName(ss.getSysvalue());
            }
            if (SystemConstants.SMS_INTERFACE_PASSWORD.equals(ss.getSyskey())) {
                smsSet.setSmsInterfacePassword(ss.getSysvalue());
            }
            if (SystemConstants.SMS_SEND_NAME.equals(ss.getSyskey())) {
                smsSet.setSmsSendName(ss.getSysvalue());
            }
            if (SystemConstants.SMS_TEMPLATE.equals(ss.getSyskey())) {
                smsSet.setSmsTemplate(ss.getSysvalue());
            }
        }
        return smsSet;
    }

    @CacheEvict(cacheNames = ApiConstants.REDIS_PROTOCOL_USER_KEY, key = "#siteCode")
    public void saveWebTerms(WebTerms webTerms, String siteCode) {
        String mDisplay = Integer.toString(webTerms.getMemberDisplayTermsOfWebsite());
        String mTerms = webTerms.getMemberServiceTermsOfWebsite();
        String aDisplay = Integer.toString(webTerms.getAgentDisplayTermsOfWebsite());
        String aTerms = webTerms.getAgentServiceTermsOfWebsite();

        if (null != mDisplay) {
            String key = SystemConstants.MEMBER_REGISTER_DISPLAY_TERMS_OF_WEBSITE;
            modifyOrUpdate(key, mDisplay);
        }
        if (null != mTerms) {
            String key = SystemConstants.MEMBER_SERVICE_TERMS_OF_WEBSITE;
            modifyOrUpdate01(key, mTerms);
        }

        if (null != aDisplay) {
            String key = SystemConstants.AGENT_REGISTER_DISPLAY_TERMS_OF_WEBSITE;
            modifyOrUpdate(key, aDisplay);
        }
        if (null != aTerms) {
            String key = SystemConstants.AGENT_SERVICE_TERMS_OF_WEBSITE;
            modifyOrUpdate01(key, aTerms);
        }
    }

    public WebTerms queryWebTerms() {
        List<SysSetting> list = sysSettingMapper.selectAll();
        WebTerms webTerms = new WebTerms();
        if (null != list) {
            for (SysSetting ss : list) {
                if (SystemConstants.MEMBER_REGISTER_DISPLAY_TERMS_OF_WEBSITE.equals(ss.getSyskey())) {
                    webTerms.setMemberDisplayTermsOfWebsite(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.MEMBER_SERVICE_TERMS_OF_WEBSITE.equals(ss.getSyskey())) {
                    webTerms.setMemberServiceTermsOfWebsite(ss.getWebsiteTerms());
                }
                if (SystemConstants.AGENT_REGISTER_DISPLAY_TERMS_OF_WEBSITE.equals(ss.getSyskey())) {
                    webTerms.setAgentDisplayTermsOfWebsite(Integer.parseInt(ss.getSysvalue()));
                }
                if (SystemConstants.AGENT_SERVICE_TERMS_OF_WEBSITE.equals(ss.getSyskey())) {
                    webTerms.setAgentServiceTermsOfWebsite(ss.getWebsiteTerms());
                }
            }
        }
        return webTerms;
    }


    public String testReceiveSms(SmsSet smsSet) {
        Map<String, String> params = new HashMap<>();
        String code = CommonUtil.getRandomCode();
        String content = smsSet.getSmsTemplate();
        params.put("content", content);
        params.put("account", smsSet.getSmsInterfaceName());
        params.put("password", smsSet.getSmsInterfacePassword());
        params.put("mobile", smsSet.getMobile());
        params.put("format", "json");
        sendSmsSevice.sendSms(params, smsSet.getSmsGetwayAddress());
        return code;
    }

    public void sendSms(SmsSet smsSet) {
        Map<String, String> params = new HashMap<>();
        String content = smsSet.getSmsTemplate();
        params.put("content", content);
        params.put("account", smsSet.getSmsInterfaceName());
        params.put("password", smsSet.getSmsInterfacePassword());
        params.put("mobile", smsSet.getMobile());
        params.put("format", "json");
        sendSmsSevice.sendSms(params, smsSet.getSmsGetwayAddress());
    }

    public String testReceiveMail(MailSet mailSet) {
        String code = CommonUtil.getRandomCode();
        String subject = "测试标题";
        String content = "测试内容:" + code;
        boolean flag = sendMailSevice.sendMail(mailSet, mailSet.getMailReceiver(), subject, content);
        if (flag) {
            return content;
        } else {
            return null;
        }
    }

    public void modifyPaySet(PaySet paySet) {
        deleteSysSetting(SystemConstants.PAY_AUTOMATIC);
        deleteSysSetting(SystemConstants.PAY_MONEY);
        deleteSysSetting(SystemConstants.FREE_WALLETSWITCH);
        setBasicSysMapper.insertSysSetting(setSysSetting(SystemConstants.PAY_AUTOMATIC,
                Constants.EVNumber.one == paySet.getPayAutomatic() ? paySet.getPayAutomatic().toString() : "0"));
        setBasicSysMapper.insertSysSetting(setSysSetting(SystemConstants.PAY_MONEY,
                Objects.nonNull(paySet.getPayMoney()) ? paySet.getPayMoney().toString() : null));
        setBasicSysMapper.insertSysSetting(setSysSetting(SystemConstants.FREE_WALLETSWITCH, "1"));
    }

    public PaySet queryPaySet() {
        List<SysSetting> settingList = sysSettingMapper.selectAll();
        if (Collections3.isNotEmpty(settingList)) {
            PaySet paySet = new PaySet();
            settingList.stream().forEach(sysSetting -> {
                if (SystemConstants.PAY_AUTOMATIC.equals(sysSetting.getSyskey())) {
                    paySet.setPayAutomatic(Integer.parseInt(sysSetting.getSysvalue()));
                }
                if (SystemConstants.PAY_MONEY.equals(sysSetting.getSyskey())) {
                    paySet.setPayMoney(new BigDecimal(sysSetting.getSysvalue()));
                }
                if (SystemConstants.FREE_WALLETSWITCH.equals(sysSetting.getSyskey())) {
                    paySet.setFreeWalletSwitch(Integer.parseInt(sysSetting.getSysvalue()));
                }
            });
            return paySet;
        }
        return null;
    }

    public SysSetting getSysSetting(String syskey) {
        SysSetting sysSetting = new SysSetting();
        sysSetting.setSyskey(syskey);
        return sysSettingMapper.selectOne(sysSetting);
    }

    private SysSetting setSysSetting(String syskey, String sysvalue) {
        SysSetting sysSetting = new SysSetting();
        sysSetting.setSyskey(syskey);
        sysSetting.setSysvalue(sysvalue);
        return sysSetting;
    }

    private void deleteSysSetting(String syskey) {
        SysSetting sysSetting = new SysSetting();
        sysSetting.setSyskey(syskey);
        sysSettingMapper.delete(sysSetting);
    }
}
