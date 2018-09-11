
package com.eveb.saasops.modules.operate.service;

import java.math.BigDecimal;
import java.util.*;

import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.constants.OrderConstants;
import com.eveb.saasops.common.exception.R200Exception;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.*;
import com.eveb.saasops.config.MessagesConfig;
import com.eveb.saasops.listener.BizEvent;
import com.eveb.saasops.listener.BizEventType;
import com.eveb.saasops.modules.member.entity.MbrAccount;
import com.eveb.saasops.modules.member.entity.MbrBillDetail;
import com.eveb.saasops.modules.member.service.MbrAccountService;
import com.eveb.saasops.modules.member.service.MbrWalletService;
import com.eveb.saasops.modules.operate.dao.OprActBonusMapper;
import com.eveb.saasops.modules.operate.dao.OprActRuleMapper;
import com.eveb.saasops.modules.operate.dao.TOpActtmplMapper;
import com.eveb.saasops.modules.operate.dto.*;
import com.eveb.saasops.modules.operate.entity.OprActRule;
import com.eveb.saasops.modules.operate.entity.OprActBonus;
import com.eveb.saasops.modules.operate.entity.TOpActtmpl;
import com.eveb.saasops.modules.operate.mapper.OperateActivityMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.eveb.saasops.modules.operate.dao.OprActActivityMapper;
import com.eveb.saasops.modules.operate.entity.OprActActivity;
import com.github.pagehelper.PageHelper;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import static com.eveb.saasops.common.utils.DateUtil.*;

@Service
@Transactional
public class OprActActivityService {

    @Autowired
    private OperateActivityMapper operateMapper;
    @Autowired
    private OprActActivityMapper actActivityMapper;
    @Autowired
    private OprActRuleMapper oprActRuleMapper;
    @Autowired
    private TOpActtmplMapper acttmplMapper;
    @Autowired
    private OprActBonusMapper oprActBonusMapper;
    @Autowired
    private MessagesConfig messagesConfig;
    @Autowired
    private MbrAccountService mbrAccountService;
    @Autowired
    private OprActActivityCastService actActivityCastService;
    @Autowired
    private MbrWalletService mbrWalletService;
    @Autowired
    private QiNiuYunUtil qiNiuYunUtil;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public List<OprActActivity> queryListAll(Integer actCatId) {
        OprActActivity actActivity = new OprActActivity();
        actActivity.setUseState(Constants.EVNumber.one);
        actActivity.setAvailable((byte) Constants.EVNumber.one);
        actActivity.setActCatId(actCatId);
        return actActivityMapper.select(actActivity);
    }

    public List<OprActActivity> activityList(String actTmplIds) {
        return operateMapper.listActivity(actTmplIds);
    }

    public PageUtils queryListPage(OprActActivity oprActBase, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<OprActActivity> list = operateMapper.findOprActActivityList(oprActBase);
        return BeanUtil.toPagedResult(list);
    }

    public void save(ActivityDto activityDto, String userName, MultipartFile uploadPcFile, MultipartFile uploadMbFile) {
        OprActActivity activity = new Gson().fromJson(activityDto.getActActivity().toString(), OprActActivity.class);
        checkoutJson(activity.getActTmplId(), activityDto.getObject().toString());
        getImageUrl(uploadPcFile, uploadMbFile, activity);
        activity.setCreateUser(userName);
        saveActivtiy(activity);
        saveRule(activity, activityDto.getObject().toString());
        updateActivityState();
    }

    private void saveRule(OprActActivity activity, String object) {
        OprActRule actRule = new OprActRule();
        actRule.setActivityId(activity.getId());
        actRule.setRule(object);
        actRule.setTime(getCurrentDate(FORMAT_22_DATE_TIME));
        oprActRuleMapper.insert(actRule);
    }

    private void saveActivtiy(OprActActivity activity) {
        activity.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        activity.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        activity.setModifyUser(activity.getCreateUser());
        activity.setAvailable(Constants.Available.enable);
        setUseState(activity);
        actActivityMapper.insert(activity);
    }

    public void updateActivity(ActivityDto activityDto, MultipartFile uploadPcFile, MultipartFile uploadMbFile, String userName) {
        OprActActivity activity = new Gson().fromJson(activityDto.getActActivity().toString(), OprActActivity.class);
        activity.setModifyUser(userName);
        checkoutJson(activity.getActTmplId(), activityDto.getObject().toString());
        getImageUrl(uploadPcFile, uploadMbFile, activity);
        setUseState(activity);
        actActivityMapper.updateByPrimaryKeySelective(activity);
        saveRule(activity, activityDto.getObject().toString());
        updateActivityState();
    }

    /**
     * 修改活动状态 优惠券状态 活动有效期年月日
     */
    public void updateActivityState() {
        List<OprActActivity> activities = operateMapper.findActivityBySatatus();
        if (Collections3.isNotEmpty(activities)) {
            activities.forEach(ac -> setUseState(ac));
            activities.stream().forEach(ac -> {
                OprActActivity actActivity = new OprActActivity();
                actActivity.setId(ac.getId());
                actActivity.setUseState(ac.getUseState());
                actActivityMapper.updateByPrimaryKeySelective(actActivity);
                if (ac.getUseState() == Constants.EVNumber.two) {
                    operateMapper.updateBounsState(ac.getId(), Constants.EVNumber.four);
                }
            });
        }

    }

    private void setUseState(OprActActivity activity) {
        Date start = DateUtil.parse(activity.getUseStart(), FORMAT_10_DATE);
        Date data = DateUtil.parse(getCurrentDate(FORMAT_10_DATE), FORMAT_10_DATE);
        Date end = DateUtil.parse(activity.getUseEnd(), FORMAT_10_DATE);
        int startTime = data.compareTo(start);
        int endTime = data.compareTo(end);
        activity.setUseState(Constants.EVNumber.two);
        if ((startTime == 0 || startTime == 1) && (endTime == 0 || endTime == -1)) {
            activity.setUseState(Constants.EVNumber.one);
        }
        if (startTime == -1 && endTime == -1) {
            activity.setUseState(Constants.EVNumber.zero);
        }
    }


    public void updateAvailable(OprActActivity activity, String loginName) {
        OprActActivity actActivity = actActivityMapper.selectByPrimaryKey(activity.getId());
        actActivity.setAvailable(activity.getAvailable());
        actActivity.setModifyUser(loginName);
        actActivityMapper.updateByPrimaryKey(actActivity);
    }

    private void getImageUrl(MultipartFile uploadPcFile, MultipartFile uploadMbFile, OprActActivity activity) {
        if (Objects.nonNull(uploadPcFile)) {
            String fileName = null;
            try {
                String prefix = uploadPcFile.getOriginalFilename()
                        .substring(uploadPcFile.getOriginalFilename().indexOf("."));
                byte[] fileBuff = IOUtils.toByteArray(uploadPcFile.getInputStream());
                fileName = qiNiuYunUtil.uploadFile(fileBuff, UUID.randomUUID().toString() + prefix);
            } catch (Exception e) {
                throw new RRException(e.getMessage());
            }
            activity.setPcGroupName(fileName);
            activity.setPcRemoteFileName(fileName);
            activity.setPcLogoUrl(fileName);
        }
        if (Objects.nonNull(uploadMbFile)) {
            String fileName = null;
            try {
                String prefix = uploadMbFile.getOriginalFilename()
                        .substring(uploadMbFile.getOriginalFilename().indexOf("."));
                byte[] fileBuff = IOUtils.toByteArray(uploadMbFile.getInputStream());
                fileName = qiNiuYunUtil.uploadFile(fileBuff, UUID.randomUUID().toString() + prefix);
            } catch (Exception e) {
                throw new RRException(e.getMessage());
            }
            activity.setMbGroupName(fileName);
            activity.setMbRemoteFileName(fileName);
            activity.setMbLogoUrl(fileName);
        }
    }

    private void checkoutJson(Integer actTmplId, String object) {
        try {
            TOpActtmpl acttmpl = acttmplMapper.selectByPrimaryKey(actTmplId);
            Gson gson = new Gson();
            if (TOpActtmpl.preferentialCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, JPreferentialDto.class);
            }
            if (TOpActtmpl.registerCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, JRegisterDto.class);
            }
            if (TOpActtmpl.depositSentCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, JDepositSentDto.class);
            }
            if (TOpActtmpl.rescueCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, JRescueDto.class);
            }
            if (TOpActtmpl.waterRebatesCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, JWaterRebatesDto.class);
            }
            if (TOpActtmpl.validCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, JValidDto.class);
            }
            if (TOpActtmpl.recommendBonusCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, JRecommendDto.class);
            }
            if (TOpActtmpl.signInCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, JSignInDto.class);
            }
            if (TOpActtmpl.redPacketCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, JRedPacketDto.class);
            }
            if (TOpActtmpl.contentCode.equals(acttmpl.getTmplCode())) {
                gson.fromJson(object, JContentDto.class);
            }
        } catch (Exception e) {
            throw new RRException(messagesConfig.getValue("activity.data"));
        }
    }

    public OprActActivity queryObject(Integer id) {
        return operateMapper.findOprActActivity(id);
    }

    public void deleteActivity(Integer id) {
        OprActActivity activity = actActivityMapper.selectByPrimaryKey(id);
        Date date = DateUtil.parse(activity.getUseStart());
        int co = new Date().compareTo(date);
        if (co == -1) {
            OprActRule actRule = new OprActRule();
            actRule.setActivityId(activity.getId());
            oprActRuleMapper.delete(actRule);
            actActivityMapper.deleteByPrimaryKey(id);
            if (StringUtil.isNotEmpty(activity.getMbGroupName())
                    && StringUtil.isNotEmpty(activity.getMbRemoteFileName())) {
                qiNiuYunUtil.deleteFile(activity.getMbRemoteFileName());
            }
            if (StringUtil.isNotEmpty(activity.getPcGroupName())
                    && StringUtil.isNotEmpty(activity.getPcRemoteFileName())) {
                qiNiuYunUtil.deleteFile(activity.getPcRemoteFileName());
            }
        }
    }

    public PageUtils webActivityList(Integer pageNo, Integer pageSize, Integer actCatId, Integer userId, Byte terminal) {
        Integer groupId = null;
        Integer tagencyId = null;
        Integer cagencyId = null;
        if (StringUtils.isEmpty(terminal)) {
            terminal = ApiConstants.Terminal.pc;
        }
        MbrAccount mbrAccount = null;
        if (!StringUtils.isEmpty(userId)) {
            mbrAccount = mbrAccountService.getAccountInfo(userId);
            groupId = mbrAccount.getGroupId();
            tagencyId = mbrAccount.getTagencyId();
            cagencyId = mbrAccount.getCagencyId();
        }
        PageHelper.startPage(pageNo, pageSize);
        List<OprActActivity> list = operateMapper.findWebActList(actCatId, groupId, tagencyId, cagencyId, terminal);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils webDepositActivityList(Integer userId, Byte terminal) {
        if (StringUtils.isEmpty(terminal)) {
            terminal = ApiConstants.Terminal.pc;
        }
        MbrAccount mbrAccount = mbrAccountService.getAccountInfo(userId);
        Integer groupId = mbrAccount.getGroupId();
        Integer tagencyId = mbrAccount.getTagencyId();
        Integer cagencyId = mbrAccount.getCagencyId();
        List<OprActActivity> list = operateMapper.findWebDepositActList(groupId, tagencyId, cagencyId, terminal);
        list.forEach(opr -> {
            if (!StringUtils.isEmpty(opr.getRule())) {
                List<ActivityRuleDto> ruleDtoList = new Gson().fromJson(opr.getRule(),
                        new TypeToken<List<ActivityRuleDto>>() {
                        }.getType());
                Optional<BigDecimal> optionalBigDecimal = ruleDtoList.stream().map(ActivityRuleDto::getAmountMin)
                        .min((o1, o2) -> o1.compareTo(o2));
                opr.setAmountMin(optionalBigDecimal.get());
            }
        });
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findAccountBonusList(String startTime, String endTime, Integer accountId, Integer pageNo,
                                          Integer pageSize, Integer status) {
        OprActBonus bonus = new OprActBonus();
        bonus.setStartTime(startTime);
        bonus.setEndTime(endTime);
        bonus.setAccountId(accountId);
        bonus.setFinancialCode(OrderConstants.FUND_ORDER_CODE_AA);
        bonus.setStatus(status);
        PageHelper.startPage(pageNo, pageSize);
        List<OprActBonus> bonuses = operateMapper.findAccountBonusList(bonus);
        return BeanUtil.toPagedResult(bonuses);
    }

    public PageUtils activityAuditList(OprActBonus bonus, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<OprActBonus> auditDtoList = operateMapper.findWaterBonusList(bonus);
        return BeanUtil.toPagedResult(auditDtoList);
    }

    public void activityAudit(ActBonusAuditDto bonusAuditDto, String userName) {
        if (Collections3.isNotEmpty(bonusAuditDto.getBonuses())) {
            bonusAuditDto.getBonuses().stream().forEach(bs -> isActivity(bs, bonusAuditDto.getTmplCode(),
                    bonusAuditDto.getMemo(), bonusAuditDto.getStatus(), userName));
        }
    }

    public void activityAuditMsg(ActBonusAuditDto bonusAuditDto, String siteCode) {
        if (Collections3.isNotEmpty(bonusAuditDto.getBonuses())) {
            bonusAuditDto.getBonuses().stream().forEach(bs -> {
                OprActBonus actBonus = oprActBonusMapper.selectByPrimaryKey(bs);
                OprActActivity actActivity = operateMapper.findOprActActivity(actBonus.getActivityId());
                BizEvent bizEvent = new BizEvent(this, siteCode, actBonus.getAccountId(), null);
                if (TOpActtmpl.waterRebatesCode.equals(actActivity.getTmplCode())) {
                    if (Constants.EVNumber.one == actBonus.getStatus()) {
                        bizEvent.setEventType(BizEventType.MEMBER_COMMISSION_SUCCESS);
                        bizEvent.setAcvitityMoney(actBonus.getBonusAmount());
                        applicationEventPublisher.publishEvent(bizEvent);
                    }
                } else {
                    if (Constants.EVNumber.one == actBonus.getStatus()) {
                        bizEvent.setEventType(BizEventType.PROMOTE_VERIFY_SUCCESS);
                    }
                    if (Constants.EVNumber.zero == actBonus.getStatus()) {
                        bizEvent.setEventType(BizEventType.PROMOTE_VERIFY_FAILED);
                    }
                    bizEvent.setAcvitityName(actActivity.getActivityName());
                    bizEvent.setAcvitityMoney(actBonus.getBonusAmount());
                    applicationEventPublisher.publishEvent(bizEvent);
                }
            });
        }
    }

    private void isActivity(Integer id, String tmplCode, String memo, Integer status, String userName) {
        OprActBonus actBonus = oprActBonusMapper.selectByPrimaryKey(id);
        if (Constants.EVNumber.two != actBonus.getStatus()) {
            throw new R200Exception("只能审核待处理的订单");
        }
        actBonus.setStatus(status);
        actBonus.setAuditUser(userName);
        actBonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        actBonus.setMemo(memo);
        if (Constants.EVNumber.zero == status) {
            oprActBonusMapper.updateByPrimaryKey(actBonus);
            return;
        }
        if (TOpActtmpl.waterRebatesCode.equals(tmplCode)) {
            waterBonusAudit(actBonus);
        }
        if (TOpActtmpl.registerCode.equals(tmplCode)) {
            actActivityCastService.setRegisterBonus(actBonus);
        }
        if (TOpActtmpl.depositSentCode.equals(tmplCode)
                || TOpActtmpl.preferentialCode.equals(tmplCode)) {
            actBonus.setStatus(Constants.EVNumber.three);
            oprActBonusMapper.updateByPrimaryKey(actBonus);
        }
    }

    private void waterBonusAudit(OprActBonus actBonus) {
        MbrBillDetail mbrBillDetail = mbrWalletService.castWalletAndBillDetail(actBonus.getLoginName(),
                actBonus.getAccountId(), OrderConstants.ACTIVITY_WATERBONUS, actBonus.getBonusAmount(), null,
                Boolean.TRUE);
        actBonus.setBillDetailId(mbrBillDetail.getId());
        oprActBonusMapper.updateByPrimaryKey(actBonus);
    }

}
