package com.eveb.saasops.modules.system.msgtemple.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.eveb.saasops.listener.BizEvent;
import com.eveb.saasops.modules.agent.service.AgentAccountService;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.eveb.saasops.api.modules.user.service.SendMailSevice;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.common.utils.ExcelUtil;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.config.SiteCodeThreadLocal;
import com.eveb.saasops.config.ThreadLocalCache;
import com.eveb.saasops.modules.member.entity.MbrAccount;
import com.eveb.saasops.modules.member.service.MbrAccountService;
import com.eveb.saasops.modules.operate.entity.OprRecMbr;
import com.eveb.saasops.modules.operate.service.OprRecMbrService;
import com.eveb.saasops.modules.system.msgtemple.dao.MsgModelMapper;
import com.eveb.saasops.modules.system.msgtemple.entity.MsgModel;
import com.eveb.saasops.modules.system.msgtemple.mapper.myMsgModelMapper;
import com.eveb.saasops.modules.system.systemsetting.dto.MailSet;
import com.eveb.saasops.modules.system.systemsetting.dto.SmsSet;
import com.eveb.saasops.modules.system.systemsetting.service.SysSettingService;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;

import io.netty.util.internal.StringUtil;


@Service
public class MsgModelService {
    @Autowired
    private MsgModelMapper msgModelMapper;
    @Autowired
    private myMsgModelMapper modelMapper;
    @Value("${temple.msg.excel.path}")
    private String templeMsgExcelPath;

    @Autowired
    private MbrAccountService mbrAccountService;

    @Autowired
    private SysSettingService sysSettingService;

    @Autowired
    private SendMailSevice sendMailSevice;

    @Autowired
    private OprRecMbrService oprRecMbrService;

    @Autowired
    private AgentAccountService agentAccountService;


    public MsgModel queryObject(Integer id) {
        return msgModelMapper.selectByPrimaryKey(id);
    }

    public PageUtils queryListPage(MsgModel msgModel, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        List<MsgModel> list = msgModelMapper.selectAll();
        return BeanUtil.toPagedResult(list);
    }

    public R save(MsgModel msgModel, String userName) {
        Date d = new Date();
        msgModel.setCreateTime(d);
        msgModel.setModifyTime(d);
        msgModel.setState(Constants.EVNumber.one);
        msgModel.setCreater(userName);
        if(StringUtil.isNullOrEmpty(msgModel.getName())) {
        	return R.error(2000, "必须填写模板名称");
        }
        msgModelMapper.insert(msgModel);
		return R.ok();
    }

    public void update(MsgModel msgModel) {
        msgModelMapper.updateByPrimaryKeySelective(msgModel);
    }

    public void delete(Integer id) {
        msgModelMapper.deleteByPrimaryKey(id);
    }

    public void deleteBatch(String ids) {
        modelMapper.deleteByIds(ids);
    }

    public PageUtils queryByConditions(MsgModel msgModel, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        /*String states = msgModel.getStates();
        if (!StringUtils.isEmpty(states)) {
            msgModel.setState(MsgModel.getStateByStates(states));
        }*/
        List<MsgModel> list = modelMapper.queryByConditions(msgModel);
        return BeanUtil.toPagedResult(list);
    }

    public void exportExcel(MsgModel msgModel, HttpServletResponse response) {
        String fileName = "信息模板" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        List<Map<String, Object>> list = Lists.newLinkedList();
        modelMapper.queryByConditions(msgModel).stream().forEach(
                cs -> {
                    Map<String, Object> param = new HashMap<>();
                    param.put("name", cs.getName());
                    param.put("msgName", cs.getMsgName());
                    param.put("inMail", cs.getInMail());
                    param.put("email", cs.getEmail());
                    param.put("phoneMail", cs.getPhoneMail());
                    param.put("state", cs.getState() == 1 ? "启用 " : "禁用");
                    param.put("creater", cs.getCreater());
                    param.put("createTime", cs.getCreateTime());
                    list.add(param);
                }
        );
        Workbook workbook = ExcelUtil.commonExcelExportList("mapList", templeMsgExcelPath, list);
        try {
            ExcelUtil.writeExcel(response, workbook, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(BizEvent bizEvent) {
        SiteCodeThreadLocal siteCodeThreadLocal = new SiteCodeThreadLocal();
        siteCodeThreadLocal.setSiteCode(bizEvent.getSiteCode());
        ThreadLocalCache.siteCodeThreadLocal.set(siteCodeThreadLocal);
        MbrAccount mbrAccount = mbrAccountService.getAccountInfo(bizEvent.getUserId());
        //TODO 简化成一个，不要列表
        MsgModel msgModel = new MsgModel();
        msgModel.setMsgType(bizEvent.getEventType().getEventCode());
        msgModel.setState(1);
        msgModel = msgModelMapper.selectOne(msgModel);
        if(msgModel != null ) {
            if (msgModel.getInMailDef() == 1) {
                sendInMail(mbrAccount, msgModel, bizEvent);
            }
            if (msgModel.getEmailDef() == 1) {
                MailSet mailSet = sysSettingService.getMailSet(bizEvent.getSiteCode());
                sendEmail(mailSet, mbrAccount, sendMailSevice, msgModel, bizEvent);
            }
            if (msgModel.getPhoneMailDef() == 1) {
                sendPhoneMsg(mbrAccount, msgModel, bizEvent.getSiteCode(), bizEvent);
            }
        }
    }

    /**
     * 处理信息模板,替换模板信息
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String msgContentHandler(String msg, BizEvent bizEvent) {
        switch (bizEvent.getEventType().getEventCode()) {
            case 1:
                return memberRegister(msg, bizEvent);
            case 2:
                return memberLevelUp(msg, bizEvent);
            case 3:
                return updateMemberInfo(msg, bizEvent);
            case 4:
                return forceLogout(msg, bizEvent);
            case 5:
                return memberAccountFreeze(msg, bizEvent);
            case 6:
                return onlinePay(msg, bizEvent);
            case 7:
                return depositVerifySuccess(msg, bizEvent);
            case 8:
                return depositVerifyFailed(msg, bizEvent);
            case 9:
                return promoteVerifySuccess(msg, bizEvent);
            case 10:
                return promoteVerifyFailed(msg, bizEvent);
            case 11:
                return memberCommissionSuccess(msg, bizEvent);
            case 12:
                return memberCommissionRefuse(msg, bizEvent);
            case 13:
                return memberWithdrawalPrimaryVerifyFailed(msg, bizEvent);
            case 14:
                return memberWithdrawalReviewVerifyFailed(msg, bizEvent);
            case 15:
                return memberWithdrawalReviewVerifySuccess(msg, bizEvent);
            case 16:
                return memberWithdrawalRefuse(msg, bizEvent);
            case 17:
                return agencyRegisterSuccess(msg, bizEvent);
            case 18:
                return agencyWithdrawalVerifySuccess(msg, bizEvent);
            case 19:
                return agencyWithdrawalVerifyFailed(msg, bizEvent);
            case 20:
                return agencySalarySuccess(msg, bizEvent);
            case 21:
                return agencyAccountFreeze(msg, bizEvent);
            case 22:
                return agencyWithdrawalRefuse(msg, bizEvent);
            case 23:
                return agencySalaryRefuse(msg, bizEvent);
        }
        return null;
    }

    /**
     * 拒绝返佣
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String agencySalaryRefuse(String msg, BizEvent bizEvent) {
        return msg;
    }

    /**
     * 拒绝代理取款
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String agencyWithdrawalRefuse(String msg, BizEvent bizEvent) {
        return msg;
    }

    /**
     * 代理账户冻结
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String agencyAccountFreeze(String msg, BizEvent bizEvent) {
        return msg.replace("#{loginName}", agentAccountService.findAccountInfo(bizEvent.getAgencyId()).getAgyAccount()).replace("#{date}", LocalTime.now().toString());
    }

    /**
     * 代理返佣成功
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String agencySalarySuccess(String msg, BizEvent bizEvent) {

        return msg.replace("#{term}",bizEvent.getTerm()).replace("#{commssion}",bizEvent.getCommssion().toString());
    }

    /**
     * 代理取款审核失败
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String agencyWithdrawalVerifyFailed(String msg, BizEvent bizEvent) {
        return msg.replace("#{withdrawMoney}",bizEvent.getWithdrawMoney().toString());
    }

    /**
     * 代理取款审核成功
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String agencyWithdrawalVerifySuccess(String msg, BizEvent bizEvent) {
        return msg.replace("#{withdrawMoney}",bizEvent.getWithdrawMoney().toString());
    }

    /**
     * 代理注册成功
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String agencyRegisterSuccess(String msg, BizEvent bizEvent) {
        return msg;
    }

    /**
     * 拒绝会员取款
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberWithdrawalRefuse(String msg, BizEvent bizEvent) {
        return msg;
    }

    /**
     * 会员取款审核成功
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberWithdrawalReviewVerifySuccess(String msg, BizEvent bizEvent) {
        return msg.replace("#{withdrawMoney}",bizEvent.getWithdrawMoney().toString());
    }

    /**
     * 会员提款复审拒绝
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberWithdrawalReviewVerifyFailed(String msg, BizEvent bizEvent) {
        return msg.replace("#{withdrawMoney}",bizEvent.getWithdrawMoney().toString());
    }

    /**
     * 会员提款初审拒绝
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberWithdrawalPrimaryVerifyFailed(String msg, BizEvent bizEvent) {
        return msg.replace("#{withdrawMoney}",bizEvent.getWithdrawMoney().toString());
    }

    /**
     * 拒绝会员返水
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberCommissionRefuse(String msg, BizEvent bizEvent) {return msg;}

    /**
     * 会员返水成功
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberCommissionSuccess(String msg, BizEvent bizEvent) {
        return msg.replace("#{acvitityMoney}",bizEvent.getAcvitityMoney().toString());
    }

    /**
     * 优惠审核失败
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String promoteVerifyFailed(String msg, BizEvent bizEvent) {
        return msg.replace("#{acvitityMoney}",bizEvent.getAcvitityMoney().toString()).replace("#{acvitityName}",bizEvent.getAcvitityName());
    }

    /**
     * 优惠审核成功
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String promoteVerifySuccess(String msg, BizEvent bizEvent) {
        return msg.replace("#{acvitityMoney}",bizEvent.getAcvitityMoney().toString()).replace("#{acvitityName}",bizEvent.getAcvitityName());
    }

    /**
     * 存款审核失败
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String depositVerifyFailed(String msg, BizEvent bizEvent) {
        return msg.replace("#{despoitMoney}",bizEvent.getDespoitMoney().toString());
    }

    /**
     * 存款审核成功
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String depositVerifySuccess(String msg, BizEvent bizEvent) {
        return msg.replace("#{despoitMoney}",bizEvent.getDespoitMoney().toString());
    }

    /**
     * 会员注册成功
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberRegister(String msg, BizEvent bizEvent) {
        return msg.replace("#{loginName}", mbrAccountService.getAccountInfo(bizEvent.getUserId()).getLoginName());
    }

    /**
     * 玩家升级成功
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberLevelUp(String msg, BizEvent bizEvent) {
        return msg;
    }

    /**
     * 修改会员资料
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String updateMemberInfo(String msg, BizEvent bizEvent) {
        return msg.replace("#{oldPassword}", bizEvent.getOldPassword()).replace("#{newPassword}", bizEvent.getNewPassword());
    }

    /**
     * 会员账户冻结
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberAccountFreeze(String msg, BizEvent bizEvent) {
        return msg.replace("#{loginName}", mbrAccountService.getAccountInfo(bizEvent.getUserId()).getLoginName()).replace("#{date}", LocalTime.now().toString());
    }

    /**
     * 强制踢出
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String forceLogout(String msg, BizEvent bizEvent) {
        return msg;
    }

    private String onlinePay(String msg, BizEvent bizEvent) {
        return msg.replace("#{despoitMoney}",bizEvent.getDespoitMoney().toString()).replace("#{orderNum}",bizEvent.getOrderNum());
    }


    private void sendEmail(MailSet mailSet, MbrAccount mbrAccount, SendMailSevice sendMailSevice, MsgModel msgModel, BizEvent bizEvent) {
        sendMailSevice.sendMail(mailSet, mbrAccount.getEmail(), msgModel.getName(), msgContentHandler(msgModel.getInMail(), bizEvent));
    }

    private void sendPhoneMsg(MbrAccount mbrAccount, MsgModel msgModel, String siteCode, BizEvent bizEvent) {
        SmsSet smsSet = sysSettingService.getSmsSet(siteCode);
        smsSet.setMobile(String.valueOf(mbrAccount.getMobile()));
        // String Content = smsSet.getSmsTemplate();
        smsSet.setSmsTemplate(msgContentHandler(msgModel.getPhoneMail(), bizEvent));
        //smsSet.setSmsTemplate(Content.replace("{0}","121991"));
        sysSettingService.sendSms(smsSet);
    }

    private void sendInMail(MbrAccount mbrAccount, MsgModel msgModel, BizEvent bizEvent) {
        OprRecMbr oprRecMbr = new OprRecMbr();
        oprRecMbr.setMbrId(mbrAccount.getId());
        oprRecMbr.setIsRead(0);
        oprRecMbr.setMbrName(mbrAccount.getLoginName());
        oprRecMbr.setTitle(msgModel.getName());
        List<MbrAccount> mbrAccounts = new ArrayList<>();
        mbrAccounts.add(mbrAccount);
        oprRecMbr.setMbrList(mbrAccounts);
        oprRecMbr.setContext(msgContentHandler(msgModel.getInMail(), bizEvent));
        oprRecMbrService.sendInMail(oprRecMbr, msgModel.getName() + "系统自动发送");
    }
}
