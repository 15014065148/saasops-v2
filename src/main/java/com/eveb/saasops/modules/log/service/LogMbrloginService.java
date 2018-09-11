package com.eveb.saasops.modules.log.service;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import com.eveb.saasops.common.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.modules.base.service.BaseService;
import com.eveb.saasops.modules.log.dao.LogMbrloginMapper;
import com.eveb.saasops.modules.log.entity.LogMbrLogin;
import com.eveb.saasops.modules.log.mapper.LogMapper;
import com.eveb.saasops.modules.member.entity.MbrAccount;
import com.eveb.saasops.modules.member.service.IpService;
import com.github.pagehelper.PageHelper;

@Service
public class LogMbrloginService extends BaseService<LogMbrloginMapper, LogMbrLogin> {
    @Autowired
    private LogMapper logMapper;
    @Autowired
    private IpService ipService;

    public PageUtils queryListPage(LogMbrLogin logMbrlogin, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        PageHelper.orderBy(" loginTime desc");
        List<LogMbrLogin> list = queryListCond(logMbrlogin);
        list.forEach(log -> {
            if (!StringUtils.isEmpty(log.getOnlineTime()))
                log.setOnlineTimeStr(StringUtil.formatOnlineTime(log.getOnlineTime()));
            else
                log.setOnlineTimeStr(Constants.SYSTEM_NONE);
            /*if (StringUtils.isEmpty(log.getLoginArea())) {
                String ip = log.getLoginIp();
                if (StringUtils.isEmpty(ip) && ip.length() > 15) {
                    if (ip.indexOf(",") > 0) {
                        ip = ip.substring(0, ip.indexOf(","));
                    }
                }
                log.setLoginArea(ipService.getIpArea(ip));
            }*/
        });
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findLogMemberLoginLastOne(LogMbrLogin logMbrlogin, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        PageHelper.orderBy(" loginTime desc");
        List<LogMbrLogin> list = queryListCond(logMbrlogin);
        return BeanUtil.toPagedResult(list);
    }


    public void saveLoginLog(MbrAccount entity, HttpServletRequest request) {
        LogMbrLogin logMbrLogin = new LogMbrLogin();
        logMbrLogin.setAccountId(entity.getId());
        logMbrLogin.setLoginName(entity.getLoginName());
        logMbrLogin.setLoginIp(CommonUtil.getIpAddress(request));
        logMbrLogin.setLoginUrl(IpUtils.getUrl(request));
        logMbrLogin.setLoginTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        String ip = logMbrLogin.getLoginIp();
        if (StringUtils.isEmpty(ip) && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        logMbrLogin.setLoginArea(ipService.getIpArea(ip));
        save(logMbrLogin);
    }

    public void setLoginOffTime(String loginName) {
        LogMbrLogin logMbrLogin = logMapper.findMemberLoginLastOne(loginName);
        if (logMbrLogin != null && StringUtils.isEmpty(logMbrLogin.getLogoutTime())) {
            logMapper.updateLoginTime(logMbrLogin.getId());
        }
    }
}
