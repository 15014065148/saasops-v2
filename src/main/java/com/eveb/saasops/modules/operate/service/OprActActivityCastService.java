package com.eveb.saasops.modules.operate.service;

import com.eveb.saasops.api.utils.JsonUtil;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.constants.OrderConstants;
import com.eveb.saasops.common.exception.R200Exception;
import com.eveb.saasops.common.utils.*;
import com.eveb.saasops.listener.BizEvent;
import com.eveb.saasops.listener.BizEventType;
import com.eveb.saasops.modules.fund.entity.FundDeposit;
import com.eveb.saasops.modules.fund.mapper.FundMapper;
import com.eveb.saasops.modules.member.dao.MbrAccountMapper;
import com.eveb.saasops.modules.member.dao.MbrBankcardMapper;
import com.eveb.saasops.modules.member.dao.MbrWalletMapper;
import com.eveb.saasops.modules.member.entity.*;
import com.eveb.saasops.modules.member.mapper.MbrMapper;
import com.eveb.saasops.modules.member.service.MbrWalletService;
import com.eveb.saasops.modules.operate.dao.*;
import com.eveb.saasops.modules.operate.dto.*;
import com.eveb.saasops.modules.operate.entity.*;
import com.eveb.saasops.modules.operate.mapper.GameMapper;
import com.eveb.saasops.modules.operate.mapper.OperateActivityMapper;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.eveb.saasops.common.utils.BigDecimalMath.formatDownRounding;
import static com.eveb.saasops.common.utils.DateUtil.*;
import static com.eveb.saasops.modules.operate.entity.TOpActtmpl.depositSentCode;
import static com.eveb.saasops.modules.operate.entity.TOpActtmpl.preferentialCode;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@Transactional
public class OprActActivityCastService {

    @Autowired
    private MbrBankcardMapper bankcardMapper;
    @Autowired
    private OperateActivityMapper operateActivityMapper;
    @Autowired
    private OprActBonusMapper actBonusMapper;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private MbrWalletService walletService;
    @Autowired
    private OprActRuleMapper ruleMapper;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private OprActRuleMapper actRuleMapper;
    @Autowired
    private TGmCatMapper gmCatMapper;
    @Autowired
    private TGmDepotMapper gmDepotMapper;
    @Autowired
    private OprActActivityMapper actActivityMapper;
    @Autowired
    private MbrWalletMapper walletMapper;
    @Autowired
    private GameMapper gameMapper;


    public static final String SEESION_ACTIVITY = "applyActivity:";

    /**
     * 申请活动(存就送，首存送) 存款勾选活动
     *
     * @param deposit
     */
    @Deprecated
    public void applyDepositActivity(FundDeposit deposit) {
        OprActActivity actActivity = operateActivityMapper.findOprActActivity(deposit.getActivityId());
        String message = checkoutActivityMsg(actActivity, deposit.getAccountId(),
                deposit.getDepositAmount(), Boolean.TRUE, deposit.getId());
        if (nonNull(message)) return;
        OprActBonus bonus = new OprActBonus();
        bonus.setDepositId(deposit.getId());
        bonus.setIsStatus(Constants.EVNumber.one);
        int count = operateActivityMapper.findOprActBouns(bonus);
        if (count > 0) return;
        MbrAccount account = accountMapper.selectByPrimaryKey(deposit.getAccountId());
        if (preferentialCode.equals(actActivity.getTmplCode())) {
            JPreferentialDto dto = jsonUtil.fromJson(actActivity.getRule(), JPreferentialDto.class);
            //addFirstDepositBonus(dto, deposit, actActivity, siteCode, account);
        }
        if (depositSentCode.equals(actActivity.getTmplCode())) {
            JDepositSentDto dto = jsonUtil.fromJson(actActivity.getRule(), JDepositSentDto.class);
            //addDepositSent(dto, deposit, actActivity, siteCode, account);
        }
    }

    /**
     * 申请活动(注册送) 活动申请页面
     *
     * @param accountId
     * @param activityId
     */
    public void applyActivity(int accountId, int activityId, String siteCode, String ip, HttpSession session) {
        Object aId = session.getAttribute(SEESION_ACTIVITY + accountId);
        if (nonNull(aId) && aId.equals(activityId)) {
            return;
        }
        session.setAttribute(SEESION_ACTIVITY + accountId, activityId);
        try {
            OprActActivity actActivity = operateActivityMapper.findOprActActivity(activityId);
            String isActivity = checkoutActivity(actActivity);
            if (nonNull(isActivity)) {
                throw new R200Exception(isActivity);
            }
            if (TOpActtmpl.registerCode.equals(actActivity.getTmplCode())) {
                registerBonus(actActivity, accountId, siteCode, ip);
            }
            if (preferentialCode.equals(actActivity.getTmplCode())) {
                applyfirstDeposit(actActivity, accountId, ip);
            }
            if (depositSentCode.equals(actActivity.getTmplCode())) {
                applyDepositSentBonus(actActivity, accountId, ip);
            }
        } finally {
            session.removeAttribute(SEESION_ACTIVITY + accountId);
        }
    }

    /**
     * 申请活动校验(存就送，首存送)
     *
     * @param accountId
     * @param activityId
     * @param depositAmount
     * @param isAmount      是否校验存款
     */
    public void checkoutApplyActivity(int accountId, int activityId, BigDecimal depositAmount, Boolean isAmount) {
        OprActActivity actActivity = operateActivityMapper.findOprActActivity(activityId);
        String message = checkoutActivityMsg(actActivity, accountId, depositAmount, isAmount, null);
        if (nonNull(message)) {
            throw new R200Exception(message);
        }
    }

    public String checkoutActivityMsg(OprActActivity actActivity, int accountId,
                                      BigDecimal depositAmount, Boolean isAmount, Integer depositId) {
        String isActivity = checkoutActivity(actActivity);
        if (nonNull(isActivity)) {
            return isActivity;
        }
        String message = null;
        if (TOpActtmpl.preferentialCode.equals(actActivity.getTmplCode())) {
            message = checkoutAapplyfirstDeposit(actActivity, accountId, depositAmount, isAmount, depositId);
        }
        if (TOpActtmpl.depositSentCode.equals(actActivity.getTmplCode())) {
            message = checkoutApplyDepositSentBonus(actActivity, accountId, depositAmount, isAmount, depositId);
        }
        return message;
    }

    private void applyActivityMsg(String siteCode, OprActBonus actBonus, String acvitityName) {
        BizEvent bizEvent = new BizEvent(this, siteCode, actBonus.getAccountId(),
                BizEventType.PROMOTE_VERIFY_SUCCESS);
        bizEvent.setAcvitityMoney(actBonus.getBonusAmount());
        bizEvent.setAcvitityName(acvitityName);
        applicationEventPublisher.publishEvent(bizEvent);
    }

    /**
     * 首存送活动
     */
    public String checkoutAapplyfirstDeposit(OprActActivity actActivity, int accountId,
                                             BigDecimal depositAmount, Boolean isAmount, Integer depositId) {
        MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
        JPreferentialDto dto = jsonUtil.fromJson(actActivity.getRule(), JPreferentialDto.class);
        String isAccountMsg = checkoutAccountMsg(account, dto.getScopeDto(), dto.getIsName(),
                dto.getIsBank(), dto.getIsMobile(), dto.getIsMail());
        if (nonNull(isAccountMsg)) {
            return isAccountMsg;
        }
        Boolean isBonus = checkoutFirstDeposi(dto, actActivity.getId(), account.getId(), depositId);
        if (Boolean.FALSE.equals(isBonus)) {
            return "您已经申请过该活动";
        }
        if (Boolean.TRUE.equals(isAmount)) {
            ActivityRuleDto ruleDto = getActivityRuleDto(dto.getActivityRuleDtos(), depositAmount);
            if (isNull(ruleDto)) {
                BigDecimal amountMin = dto.getActivityRuleDtos().get(dto.getActivityRuleDtos().size() - 1).getAmountMin();
                return String.format("参加该活动最少充值%s元", amountMin);
            }
        }
        return null;
    }

    /**
     * 首次送 申请页面
     *
     * @param actActivity
     * @param accountId
     */
    public void applyfirstDeposit(OprActActivity actActivity, int accountId, String ip) {
        MbrAccount account = mbrMapper.findMbrAccount(accountId, null, null);
        JPreferentialDto dto = jsonUtil.fromJson(actActivity.getRule(), JPreferentialDto.class);
        String isAccountMsg = checkoutAccountMsg(account, dto.getScopeDto(), dto.getIsName(),
                dto.getIsBank(), dto.getIsMobile(), dto.getIsMail());
        if (Objects.nonNull(isAccountMsg)) {
            throw new R200Exception(isAccountMsg);
        }
        Boolean isBonus = checkoutFirstDepositBonus(dto, actActivity.getId(), account.getId());
        if (Boolean.FALSE.equals(isBonus)) {
            throw new R200Exception("您已经申请过该活动");
        }
        List<FundDeposit> deposits = getFundDeposits(account, dto.getDepositType(), actActivity);
        if (Collections3.isEmpty(deposits)) {
            throw new R200Exception("您没有符合条件的存款");
        }
        deposits.stream().forEach(ds -> accountDepositBonus(dto, ds, actActivity, account, ip));
    }

    private List<FundDeposit> getFundDeposits(MbrAccount account, int depositType, OprActActivity actActivity) {
        FundDeposit fundDeposit = new FundDeposit();
        fundDeposit.setAccountId(account.getId());
        fundDeposit.setAuditTimeFrom(actActivity.getUseStart());
        fundDeposit.setAuditTimeTo(actActivity.getUseEnd());
        if (Constants.EVNumber.zero == depositType) {
            FundDeposit fundDeposit1 = new FundDeposit();
            fundDeposit1.setAccountId(account.getId());
            fundDeposit1.setAuditTimeFrom(actActivity.getUseStart());
            List<FundDeposit> depositList = fundMapper.findDepositActivity(fundDeposit1);
            if (depositList.size() == 0) {
                throw new R200Exception("您不符合该活动的申请条件");
            }
            fundDeposit.setIsSign(Constants.EVNumber.one);
        }
        if (Constants.EVNumber.one == depositType) {
            fundDeposit.setIsSign(Constants.EVNumber.two);
        }
        if (Constants.EVNumber.two == depositType) {
            fundDeposit.setIsSign(Constants.EVNumber.three);
        }
        List<FundDeposit> deposits = fundMapper.findDepositActivity(fundDeposit);
        if (deposits.size() == 0) {
            throw new R200Exception("您不符合该活动的申请条件");
        }
        return deposits.stream().map(d -> {
            OprActBonus bonus = new OprActBonus();
            bonus.setAccountId(account.getId());
            bonus.setDepositId(d.getId());
            bonus.setIsStatus(Constants.EVNumber.one);
            int count = operateActivityMapper.findOprActBouns(bonus);
            if (count == 0) return d;
            return null;
        }).collect(Collectors.toList())
                .stream().filter(c -> nonNull(c))
                .collect(Collectors.toList());
    }

    private void accountDepositBonus(JPreferentialDto dto, FundDeposit deposit,
                                     OprActActivity actActivity, MbrAccount account, String ip) {
        ActivityRuleDto ruleDto = getActivityRuleDto(dto.getActivityRuleDtos(), deposit.getDepositAmount());
        if (isNull(ruleDto)) {
            throw new R200Exception("您不符合该活动的申请条件");
        }
        OprActBonus bonus = setOprActBonus(account.getId(), account.getLoginName(),
                actActivity.getId(), deposit.getDepositAmount(),
                deposit.getId(), actActivity.getRuleId());
        bonus.setIp(ip);
        bonus.setDevSource(account.getLoginSource());
        if (Boolean.FALSE.equals(dto.getIsAudit())) {
            bonus.setAuditUser(Constants.SYSTEM_USER);
            bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
            bonus.setStatus(Constants.EVNumber.three);
        }
        actBonusMapper.insert(bonus);
    }

    @Deprecated
    private void addFirstDepositBonus(JPreferentialDto dto, FundDeposit deposit,
                                      OprActActivity actActivity, MbrAccount account, String ip) {
        ActivityRuleDto ruleDto = getActivityRuleDto(dto.getActivityRuleDtos(), deposit.getDepositAmount());
        if (nonNull(ruleDto)) {
            OprActBonus bonus = setOprActBonus(account.getId(), account.getLoginName(),
                    actActivity.getId(), deposit.getDepositAmount(), deposit.getId(), actActivity.getRuleId());
            if (ruleDto.getDonateType() == Constants.EVNumber.one) {
                bonus.setBonusAmount(ruleDto.getDonateAmount());
            } else {
                BigDecimal bigDecimal = CommonUtil.adjustScale(ruleDto.getDonateAmount().divide(
                        new BigDecimal(Constants.ONE_HUNDRED)).multiply(deposit.getDepositAmount()));
                bonus.setBonusAmount(bigDecimal.compareTo(ruleDto.getDonateAmountMax()) == 1
                        ? ruleDto.getDonateAmountMax() : bigDecimal);
            }
            int multipleWater = nonNull(ruleDto.getMultipleWater()) ? ruleDto.getMultipleWater().intValue() : 0;
            bonus.setDiscountAudit(multipleWater);
            bonus.setScope(dto.getScope());
            bonus.setIp(ip);
            bonus.setDevSource(account.getLoginSource());
            actBonusMapper.insert(bonus);
            if (Boolean.FALSE.equals(dto.getIsAudit())) {
                bonus.setAuditUser(Constants.SYSTEM_USER);
                bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
                auditOprActBonus(bonus, multipleWater, dto.getScope(),
                        new Gson().toJson(dto.getAuditCats()), OrderConstants.ACTIVITY_PREFERENTIAL);
                // applyActivityMsg(siteCode, bonus, actActivity.getActivityName());
            }
        }
    }

    private ActivityRuleDto getActivityRuleDto(List<ActivityRuleDto> ruleDtos, BigDecimal amount) {
        ruleDtos.sort((r1, r2) -> r2.getAmountMax().compareTo(r1.getAmountMax()));
        for (ActivityRuleDto rs : ruleDtos) {
            Boolean isActivity = compareAmount(amount, rs.getAmountMin(), rs.getAmountMax());
            if (Boolean.TRUE.equals(isActivity)) {
                return rs;
            }
        }
        return null;
    }

    private Boolean compareAmount(BigDecimal amount, BigDecimal amountMin, BigDecimal amountMax) {
        if (amount.compareTo(amountMax) == 0 || amount.compareTo(amountMax) == 1) {
            return Boolean.TRUE;
        }
        if (amount.compareTo(amountMin) == 0 || amount.compareTo(amountMin) == 1) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private Boolean checkoutFirstDeposi(JPreferentialDto dto, Integer activityId, Integer accountId, Integer depositId) {
        FundDeposit deposit = new FundDeposit();
        deposit.setActivityId(activityId);
        deposit.setAccountId(accountId);
        if (nonNull(depositId)) {
            deposit.setDepositId(depositId);
        }
        if (Constants.EVNumber.one == dto.getDepositType()) {
            deposit.setCreateTime(getCurrentDate(FORMAT_10_DATE));
        }
        if (Constants.EVNumber.two == dto.getDepositType()) {
            deposit.setCreateTimeFrom(getMonday(FORMAT_10_DATE));
            deposit.setCreateTimeTo(getWeek(FORMAT_10_DATE));
        }
        int count = fundMapper.findDepositActivityCount(deposit);
        if (count > 0) return Boolean.FALSE;
        return Boolean.TRUE;
    }

    private Boolean checkoutFirstDepositBonus(JPreferentialDto dto, Integer activityId, Integer accountId) {
        OprActBonus bonus = new OprActBonus();
        bonus.setActivityId(activityId);
        bonus.setAccountId(accountId);
        if (Constants.EVNumber.one == dto.getDepositType()) {
            bonus.setApplicationTime(getCurrentDate(FORMAT_10_DATE));
        }
        if (Constants.EVNumber.two == dto.getDepositType()) {
            bonus.setStartTime(getMonday(FORMAT_10_DATE));
            bonus.setEndTime(getWeek(FORMAT_10_DATE));
        }
        int count = operateActivityMapper.findBounsCount(bonus);
        if (count > 0) return Boolean.FALSE;
        return Boolean.TRUE;
    }

    /**
     * 注册送活动
     */
    public void registerBonus(OprActActivity actActivity, int accountId, String siteCode, String ip) {
        JRegisterDto dto = jsonUtil.fromJson(actActivity.getRule(), JRegisterDto.class);
        MbrAccount account = mbrMapper.findMbrAccount(accountId, dto.getRegisterStartTime(), dto.getRegisterEndTime());
        if (Objects.isNull(account)) {
            throw new R200Exception("不在活动注册时间内");
        }
        String isAccountMsg = checkoutAccountMsg(account, dto.getScopeDto(), dto.getIsName(),
                dto.getIsBank(), dto.getIsMobile(), dto.getIsMail());
        if (nonNull(isAccountMsg)) {
            throw new R200Exception(isAccountMsg);
        }
        if (checkoutRegisterBonus(account.getId(), actActivity.getId()) > 0) {
            throw new R200Exception("请勿重复申请");
        }
        addRegisterBonus(account, actActivity, dto, siteCode, ip);
    }

    public void setRegisterBonus(OprActBonus actBonus) {
        OprActRule rule = ruleMapper.selectByPrimaryKey(actBonus.getRuleId());
        JRegisterDto dto = jsonUtil.fromJson(rule.getRule(), JRegisterDto.class);
        auditOprActBonus(actBonus, actBonus.getDiscountAudit(), null,
                new Gson().toJson(dto.getAuditCats()), OrderConstants.ACTIVITY_REGISTER);
    }

    private void addRegisterBonus(MbrAccount account, OprActActivity actActivity, JRegisterDto dto, String siteCode, String ip) {
        OprActBonus bonus = setOprActBonus(account.getId(), account.getLoginName(), actActivity.getId(),
                BigDecimal.ZERO, null, actActivity.getRuleId());
        bonus.setBonusAmount(dto.getRuleDto().getDonateAmount());
        bonus.setIsShow(Constants.EVNumber.three);
        int multipleWater = nonNull(dto.getRuleDto().getMultipleWater())
                ? dto.getRuleDto().getMultipleWater().intValue() : 0;
        bonus.setDiscountAudit(multipleWater);
        //设置IP
        bonus.setIp(ip);
        //获取设备来源
        bonus.setDevSource(account.getLoginSource());
        actBonusMapper.insert(bonus);
        if (Boolean.FALSE.equals(dto.getIsAudit())) {
            bonus.setAuditUser(Constants.SYSTEM_USER);
            bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
            auditOprActBonus(bonus, multipleWater, null,
                    new Gson().toJson(dto.getAuditCats()), OrderConstants.ACTIVITY_REGISTER);
            applyActivityMsg(siteCode, bonus, actActivity.getActivityName());
        }
    }

    private Integer checkoutRegisterBonus(Integer accountId, Integer activityId) {
        OprActBonus registerBonus = new OprActBonus();
        registerBonus.setAccountId(accountId);
        registerBonus.setActivityId(activityId);
        return operateActivityMapper.findBounsCount(registerBonus);
    }

    /**
     * 存就送
     */
    public String checkoutApplyDepositSentBonus(OprActActivity actActivity, int accountId,
                                                BigDecimal depositAmount, Boolean isAmount, Integer depositId) {
        MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
        JDepositSentDto dto = jsonUtil.fromJson(actActivity.getRule(), JDepositSentDto.class);
        String isAccountMsg = checkoutAccountMsg(account, dto.getScopeDto(), dto.getIsName(),
                dto.getIsBank(), dto.getIsMobile(), dto.getIsMail());
        if (nonNull(isAccountMsg)) {
            return isAccountMsg;
        }
        Boolean isDepositSent = checkoutDeposit(dto, actActivity.getId(), account.getId(), depositId);
        if (Boolean.FALSE.equals(isDepositSent)) {
            return "请勿重复申请";
        }
        if (Boolean.TRUE.equals(isAmount)) {
            ActivityRuleDto ruleDto = getActivityRuleDto(dto.getActivityRuleDtos(), depositAmount);
            if (Objects.isNull(ruleDto)) {
                BigDecimal amountMin = dto.getActivityRuleDtos().get(dto.getActivityRuleDtos().size() - 1).getAmountMin();
                return String.format("参加该活动最少充值%s元", amountMin);
            }
        }
        return null;
    }


    /**
     * 存就送 申请页面
     *
     * @param actActivity
     * @param accountId
     */
    public void applyDepositSentBonus(OprActActivity actActivity, int accountId, String ip) {
        MbrAccount account = mbrMapper.findMbrAccount(accountId, null, null);
        JDepositSentDto dto = jsonUtil.fromJson(actActivity.getRule(), JDepositSentDto.class);
        String isAccountMsg = checkoutAccountMsg(account, dto.getScopeDto(), dto.getIsName(),
                dto.getIsBank(), dto.getIsMobile(), dto.getIsMail());
        if (nonNull(isAccountMsg)) {
            throw new R200Exception(isAccountMsg);
        }
        Boolean isDepositSent = checkoutDepositSent(dto, actActivity.getId(), account.getId());
        if (Boolean.FALSE.equals(isDepositSent)) {
            throw new R200Exception("请勿重复申请");
        }
        FundDeposit fundDeposit = getDepositSentDeposits(dto.getDrawType(), account, actActivity);
        ActivityRuleDto ruleDto = getActivityRuleDto(dto.getActivityRuleDtos(), fundDeposit.getDepositAmount());
        if (Objects.isNull(ruleDto)) {
            throw new R200Exception("您的存款不满足活动最基本条件");
        }
        accountDepositSent(dto, fundDeposit, actActivity, account, ip);
    }

    private FundDeposit getDepositSentDeposits(int drawType, MbrAccount account, OprActActivity actActivity) {
        FundDeposit fundDeposit = new FundDeposit();
        fundDeposit.setAccountId(account.getId());
        fundDeposit.setIsSign(Constants.EVNumber.four);
        if (Constants.EVNumber.zero == drawType) {
            fundDeposit.setAuditTime(getCurrentDate(FORMAT_10_DATE));
        }
        if (Constants.EVNumber.one == drawType) {
            fundDeposit.setAuditTimeFrom(getMonday(FORMAT_10_DATE));
            fundDeposit.setAuditTimeTo(getWeek(FORMAT_10_DATE));
        }
        if (Constants.EVNumber.two == drawType) {
            fundDeposit.setAuditTimeFrom(getPastDate(Constants.EVNumber.six, FORMAT_10_DATE));
            fundDeposit.setAuditTimeTo(getCurrentDate(FORMAT_10_DATE));
        }
        if (Constants.EVNumber.three == drawType) {
            fundDeposit.setAuditTimeFrom(actActivity.getUseStart());
            fundDeposit.setAuditTimeTo(actActivity.getUseEnd());
        }
        List<FundDeposit> deposits = fundMapper.findDepositActivity(fundDeposit);
        if (Collections3.isEmpty(deposits)) {
            throw new R200Exception("您的存款不符合申请条件");
        }
        FundDeposit deposit = deposits.get(0);
        OprActBonus bonus = new OprActBonus();
        bonus.setAccountId(account.getId());
        bonus.setDepositId(deposit.getId());
        bonus.setIsStatus(Constants.EVNumber.one);
        int count = operateActivityMapper.findOprActBouns(bonus);
        if (count > 0) {
            throw new R200Exception("您的存款已经申请其他活动");
        }
        return deposit;
    }

    private void accountDepositSent(JDepositSentDto dto, FundDeposit deposit, OprActActivity actActivity, MbrAccount account, String ip) {
        OprActBonus bonus = setOprActBonus(account.getId(), account.getLoginName(), actActivity.getId(),
                deposit.getDepositAmount(), deposit.getId(), actActivity.getRuleId());
        bonus.setScope(null);
        bonus.setIp(ip);
        bonus.setDevSource(account.getLoginSource());
        if (Boolean.FALSE.equals(dto.getIsAudit())) {
            bonus.setAuditUser(Constants.SYSTEM_USER);
            bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
            bonus.setStatus(Constants.EVNumber.three);
        }
        actBonusMapper.insert(bonus);
    }


    @Deprecated
    private void addDepositSent(JDepositSentDto dto, FundDeposit deposit, OprActActivity actActivity, MbrAccount account, String ip) {
        OprActBonus bonus = setOprActBonus(account.getId(), account.getLoginName(), actActivity.getId(),
                deposit.getDepositAmount(), deposit.getId(), actActivity.getRuleId());
        setBonusAmount(dto, deposit.getDepositAmount(), bonus);
        bonus.setScope(null);
        bonus.setIp(ip);
        bonus.setDevSource(account.getLoginSource());
        if (Boolean.FALSE.equals(dto.getIsAudit())) {
            bonus.setAuditUser(Constants.SYSTEM_USER);
            bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
            bonus.setStatus(Constants.EVNumber.three);
        }
        actBonusMapper.insert(bonus);
        if (Boolean.FALSE.equals(dto.getIsAudit())) {
            bonus.setAuditUser(Constants.SYSTEM_USER);
            bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
            auditOprActBonus(bonus, bonus.getDiscountAudit(), null,
                    new Gson().toJson(dto.getAuditCats()), OrderConstants.ACTIVITY_DEPOSITSENT);
            //applyActivityMsg(siteCode, bonus, actActivity.getActivityName());
        }
    }

    private void setBonusAmount(JDepositSentDto dto, BigDecimal amount, OprActBonus bonus) {
        List<ActivityRuleDto> ruleDtos = dto.getActivityRuleDtos();
        if (Collections3.isNotEmpty(ruleDtos)) {
            ruleDtos.sort((r1, r2) -> r2.getAmountMax().compareTo(r1.getAmountMax()));
        }
        Boolean isSign = Boolean.FALSE;
        BigDecimal bigDecimalSum = BigDecimal.ZERO;
        for (int j = 0; j < ruleDtos.size(); j++) {
            ActivityRuleDto ruleDto = ruleDtos.get(j);
            int multipleWater = nonNull(ruleDto.getMultipleWater()) ? ruleDto.getMultipleWater().intValue() : 0;
            if (dto.getFormulaMode() == Constants.EVNumber.one) {
                if (compareAmount(amount, ruleDto.getAmountMin(), ruleDto.getAmountMax())) {
                    bonus.setDiscountAudit(multipleWater);
                    bonus.setBonusAmount(addBigDecimal(ruleDto, amount));
                    break;
                }
            }
            if (Boolean.FALSE.equals(isSign) && compareAmount(amount, ruleDto.getAmountMin(), ruleDto.getAmountMax())) {
                bonus.setDiscountAudit(multipleWater);
                isSign = Boolean.TRUE;
            }
            if (Boolean.TRUE.equals(isSign)) {
                BigDecimal bigDecimal = addBigDecimal(ruleDto, amount);
                bigDecimalSum = bigDecimalSum.add(bigDecimal);
            }
        }
        if (Boolean.TRUE.equals(isSign)) {
            bonus.setBonusAmount(bigDecimalSum);
        }
    }

    private BigDecimal addBigDecimal(ActivityRuleDto ruleDto, BigDecimal amount) {
        if (ruleDto.getDonateType() == Constants.EVNumber.zero) {
            BigDecimal bigDecimal = CommonUtil.adjustScale(ruleDto.getDonateAmount().divide(
                    new BigDecimal(Constants.ONE_HUNDRED)).multiply(amount));
            if (nonNull(ruleDto.getDonateAmountMax())
                    && ruleDto.getDonateAmountMax().compareTo(BigDecimal.ZERO) == 1
                    && bigDecimal.compareTo(ruleDto.getDonateAmountMax()) == 1) {
                bigDecimal = ruleDto.getDonateAmountMax();
            }
            return bigDecimal;
        }
        return ruleDto.getDonateAmount();
    }

    private Boolean checkoutDeposit(JDepositSentDto dto, Integer activityId, Integer accountId, Integer depositId) {
        FundDeposit deposit = new FundDeposit();
        deposit.setActivityId(activityId);
        deposit.setAccountId(accountId);
        if (nonNull(depositId)) {
            deposit.setDepositId(depositId);
        }
        if (Constants.EVNumber.zero == dto.getDrawType()) {
            deposit.setCreateTime(getCurrentDate(FORMAT_10_DATE));
        }
        if (Constants.EVNumber.one == dto.getDrawType()) {
            deposit.setCreateTimeFrom(getMonday(FORMAT_10_DATE));
            deposit.setCreateTimeTo(getWeek(FORMAT_10_DATE));
        }
        if (Constants.EVNumber.two == dto.getDrawType()) {
            deposit.setCreateTimeFrom(getPastDate(Constants.EVNumber.six, FORMAT_10_DATE));
            deposit.setCreateTimeTo(getCurrentDate(FORMAT_10_DATE));
        }
        int count = fundMapper.findDepositActivityCount(deposit);
        if (count >= dto.getDrawNumber()) return Boolean.FALSE;
        return Boolean.TRUE;
    }


    private Boolean checkoutDepositSent(JDepositSentDto dto, Integer activityId, Integer accountId) {
        OprActBonus bonus = new OprActBonus();
        bonus.setActivityId(activityId);
        bonus.setAccountId(accountId);
        if (Constants.EVNumber.zero == dto.getDrawType()) {
            bonus.setApplicationTime(getCurrentDate(FORMAT_10_DATE));
        }
        if (Constants.EVNumber.one == dto.getDrawType()) {
            bonus.setStartTime(getMonday(FORMAT_10_DATE));
            bonus.setEndTime(getWeek(FORMAT_10_DATE));
        }
        if (Constants.EVNumber.two == dto.getDrawType()) {
            bonus.setStartTime(getPastDate(Constants.EVNumber.six, FORMAT_10_DATE));
            bonus.setEndTime(getCurrentDate(FORMAT_10_DATE));
        }
        int count = operateActivityMapper.findBounsCount(bonus);
        if (count >= dto.getDrawNumber()) return Boolean.FALSE;
        return Boolean.TRUE;
    }

    private void auditOprActBonus(OprActBonus bonus, Integer multipleWater, Integer scope,
                                  String auditCats, String financialCode) {
     /*   MbrAccountAudit accountAudit = accountAuditService.addAccountAuditBonus(
                bonus, multipleWater, scope, auditCats, financialCode);*/

        MbrBillDetail mbrBillDetail = walletService.castWalletAndBillDetail(bonus.getLoginName(),
                bonus.getAccountId(), financialCode, bonus.getBonusAmount(), null, Boolean.TRUE);

        bonus.setStatus(Constants.EVNumber.one);
        bonus.setBillDetailId(mbrBillDetail.getId());
        actBonusMapper.updateByPrimaryKey(bonus);
    }

    private OprActBonus setOprActBonus(Integer accountId, String loginName, Integer activityId,
                                       BigDecimal depositAmount, Integer depositId, Integer ruleId) {
        OprActBonus bonus = new OprActBonus();
        bonus.setAccountId(accountId);
        bonus.setLoginName(loginName);
        bonus.setApplicationTime(getCurrentDate(FORMAT_18_DATE_TIME));
        bonus.setActivityId(activityId);
        bonus.setDepositedAmount(depositAmount);
        bonus.setDepositId(depositId);
        bonus.setIsShow(Constants.EVNumber.two);
        bonus.setStatus(Constants.EVNumber.two);
        bonus.setRuleId(ruleId);
        bonus.setOrderNo(new SnowFlake().nextId());
        bonus.setOrderPrefix(OrderConstants.ACTIVITY_AC);
        bonus.setTransferAmount(BigDecimal.ZERO);
        return bonus;
    }

    private String checkoutActivity(OprActActivity actActivity) {
        if (Objects.isNull(actActivity)) {
            return "活动不存在";
        }
        if (Constants.EVNumber.one != actActivity.getUseState()) {
            return "活动必须为进行中";
        }
        if (Constants.EVNumber.zero == actActivity.getAvailable()) {
            return "活动已经禁用";
        }
        return null;
    }

    private String checkoutAccountMsg(MbrAccount account, ActivityScopeDto scopeDto, Boolean isName,
                                      Boolean isBank, Boolean isMobile, Boolean isMail) {
        if (Boolean.FALSE.equals(scopeDto.getIsAccAll()) && !scopeDto.getAccIds().contains(account.getGroupId())) {
            return "所在会员组没有权限参加活动";
        }
        if (Boolean.FALSE.equals(scopeDto.getIsAgyTopAll()) && !scopeDto.getAgyTopIds().contains(account.getTagencyId())) {
            return "所属总代没有权限参加活动";
        }
        if (Boolean.FALSE.equals(scopeDto.getIsAgyAll()) && !scopeDto.getAgyIds().contains(account.getCagencyId())) {
            return "所属代理没有权限参加活动";
        }
        if (Boolean.TRUE.equals(isName) && StringUtil.isEmpty(account.getRealName())) {
            return "请先填写真实姓名";
        }
        if (Boolean.TRUE.equals(isMobile) && Constants.EVNumber.zero == account.getIsVerifyMoblie()) {
            return "请先验证手机号码";
        }
        if (Boolean.TRUE.equals(isMail) && Constants.EVNumber.zero == account.getIsVerifyEmail()) {
            return "请先验证邮箱";
        }
        if (Boolean.TRUE.equals(isBank)) {
            MbrBankcard bankcard = new MbrBankcard();
            bankcard.setAccountId(account.getId());
            bankcard.setAvailable(Constants.Available.enable);
            bankcard.setIsDel(Constants.Available.disable);
            int count = bankcardMapper.selectCount(bankcard);
            if (count == 0) {
                return "请先绑定银行卡";
            }
        }
        return null;
    }

    public PageUtils accountBonusList(Integer accountId, Integer status, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<BonusListDto> bonusListDtos = operateActivityMapper.findAccountBouns(accountId, status, null);
        if (Collections3.isNotEmpty(bonusListDtos)) setBonusListDto(bonusListDtos);
        return BeanUtil.toPagedResult(bonusListDtos);
    }

    public BonusListDto accountBonusOne(Integer accountId, Integer id) {
        List<BonusListDto> bonusListDtos = operateActivityMapper.findAccountBouns(accountId, null, id);
        if (Collections3.isNotEmpty(bonusListDtos)) {
            setBonusListDto(bonusListDtos);
            BonusListDto dto = bonusListDtos.get(0);
            MbrWallet wallet = new MbrWallet();
            wallet.setAccountId(accountId);
            wallet = walletMapper.selectOne(wallet);
            dto.setWalletBalance(wallet.getBalance());
            return dto;
        }
        return null;
    }

    public List<BonusListDto> availableAccountBonusList(Integer accountId) {
        List<BonusListDto> bonusListDtos = operateActivityMapper.findAccountBouns(accountId,
                Constants.EVNumber.three, null);
        if (Collections3.isNotEmpty(bonusListDtos)) setBonusListDto(bonusListDtos);
        return bonusListDtos;
    }

   /* private void setBonusStatus(List<OprActBonus> bonusList) {
        if (Collections3.isNotEmpty(bonusList)) {
            bonusList.stream().forEach(bs -> {
                OprActBonus bonus = new OprActBonus();
                bonus.setId(bs.getId());
                bonus.setStatus(Constants.EVNumber.four);
                actBonusMapper.updateByPrimaryKeySelective(bonus);
            });
        }
    }*/

    private void setBonusListDto(List<BonusListDto> bonusListDtos) {
        bonusListDtos.stream().forEach(bs -> {
            OprActRule rule = actRuleMapper.selectByPrimaryKey(bs.getRuleId());
            if (isNull(rule)) return;
            if (TOpActtmpl.preferentialCode.equals(bs.getTmplCode())) {
                JPreferentialDto dto = jsonUtil.fromJson(rule.getRule(), JPreferentialDto.class);
                if (nonNull(dto)) bs.setCatDtoList(setBonusCatDtos(dto.getAuditCats()));
                ActivityRuleDto ruleDto = castMinAmount(dto.getActivityRuleDtos());
                if (nonNull(ruleDto)) {
                    bs.setMinAmount(ruleDto.getAmountMin());
                    bs.setDiscountAudit(ruleDto.getMultipleWater());
                }
            }
            if (TOpActtmpl.depositSentCode.equals(bs.getTmplCode())) {
                JDepositSentDto dto = jsonUtil.fromJson(rule.getRule(), JDepositSentDto.class);
                if (nonNull(dto)) bs.setCatDtoList(setBonusCatDtos(dto.getAuditCats()));
                ActivityRuleDto ruleDto = castMinAmount(dto.getActivityRuleDtos());
                if (nonNull(ruleDto)) {
                    bs.setMinAmount(ruleDto.getAmountMin());
                    bs.setDiscountAudit(ruleDto.getMultipleWater());
                }
            }
            if (TOpActtmpl.registerCode.equals(bs.getTmplCode())) {
                JRegisterDto dto = jsonUtil.fromJson(rule.getRule(), JRegisterDto.class);
                if (nonNull(dto)) bs.setCatDtoList(setBonusCatDtos(dto.getAuditCats()));
                bs.setDiscountAudit(dto.getRuleDto().getMultipleWater());
                bs.setMinAmount(BigDecimal.ZERO);
            }
        });
    }

    private ActivityRuleDto castMinAmount(List<ActivityRuleDto> activityRuleDtos) {
        if (Collections3.isNotEmpty(activityRuleDtos)) {
            Collections.sort(activityRuleDtos, Comparator.comparing(ActivityRuleDto::getAmountMin));
            return activityRuleDtos.get(0);
        }
        return null;
    }


    private List<BonusCatDto> setBonusCatDtos(List<AuditCat> auditCats) {
        List<BonusCatDto> catDtoList = Lists.newArrayList();
        auditCats.stream().forEach(as -> {
            if (Boolean.TRUE.equals(as.getIsAll()) || Collections3.isNotEmpty(as.getDepots())) {
                BonusCatDto bonusCatDto = new BonusCatDto();
                TGmCat tGmCat = gmCatMapper.selectByPrimaryKey(as.getCatId());
                bonusCatDto.setCatId(as.getCatId());
                bonusCatDto.setCatName(tGmCat.getCatName());
                if (Boolean.TRUE.equals(as.getIsAll())) {
                    List<BonusDepotDto> bonusDepotDtos = Lists.newArrayList();
                    TGameLogo gameLogo = new TGameLogo();
                    gameLogo.setCatId(as.getCatId());
                    List<TGameLogo> list = gameMapper.listtGameLogo(gameLogo);
                    list.stream().forEach(ds -> {
                        BonusDepotDto depotDto = new BonusDepotDto();
                        depotDto.setDepotId(ds.getDepotId());
                        depotDto.setDepotName(ds.getDepotName());
                        bonusDepotDtos.add(depotDto);
                    });
                    bonusCatDto.setDepotDtos(bonusDepotDtos);
                } else {
                    if (Collections3.isNotEmpty(as.getDepots())) {
                        List<BonusDepotDto> bonusDepotDtos = Lists.newArrayList();
                        as.getDepots().stream().forEach(ds -> {
                            TGmDepot depot = gmDepotMapper.selectByPrimaryKey(ds.getDepotId());
                            BonusDepotDto depotDto = new BonusDepotDto();
                            depotDto.setDepotId(ds.getDepotId());
                            depotDto.setDepotName(depot.getDepotName());
                            bonusDepotDtos.add(depotDto);
                        });
                        bonusCatDto.setDepotDtos(bonusDepotDtos);
                    }
                }
                catDtoList.add(bonusCatDto);
            }
        });
        return catDtoList;
    }

    public OprActBonus billManageBonus(int depotId, OprActBonus bonus, BigDecimal amount) {
        List<BonusListDto> bonusListDtos = operateActivityMapper.findAccountBouns(null, null, bonus.getId());
        if (Collections3.isEmpty(bonusListDtos)) {
            throw new R200Exception("优惠不存在");
        }
        BonusListDto bonusDto = bonusListDtos.get(0);
        OprActActivity actActivity = actActivityMapper.selectByPrimaryKey(bonusDto.getActivityId());
        if (actActivity.getUseState() == Constants.EVNumber.two
                || bonusDto.getStatus() != Constants.EVNumber.three) {
            throw new R200Exception("优惠已经失效");
        }
        OprActRule rule = actRuleMapper.selectByPrimaryKey(bonusDto.getRuleId());
        if (TOpActtmpl.preferentialCode.equals(bonusDto.getTmplCode())) {
            setPreferentialBonus(rule, depotId, amount, bonus);
        }
        if (TOpActtmpl.depositSentCode.equals(bonusDto.getTmplCode())) {
            setDepositSentBonus(rule, depotId, amount, bonus);
        }
        return bonus;
    }

    private void setPreferentialBonus(OprActRule rule, int depotId, BigDecimal amount, OprActBonus bonus) {
        JPreferentialDto dto = jsonUtil.fromJson(rule.getRule(), JPreferentialDto.class);
        checkoutDepot(dto.getAuditCats(), depotId);
        checkoutAmountMin(dto.getActivityRuleDtos(), amount);
        ActivityRuleDto ruleDto = getActivityRuleDto(dto.getActivityRuleDtos(), amount);
        if (ruleDto.getDonateType() == Constants.EVNumber.one) {
            bonus.setBonusAmount(ruleDto.getDonateAmount());
        } else {
            BigDecimal bigDecimal = CommonUtil.adjustScale(ruleDto.getDonateAmount().divide(
                    new BigDecimal(Constants.ONE_HUNDRED)).multiply(amount));
            if (nonNull(ruleDto.getDonateAmountMax())
                    && ruleDto.getDonateAmountMax().compareTo(BigDecimal.ZERO) == 1
                    && bigDecimal.compareTo(ruleDto.getDonateAmountMax()) == 1) {
                bigDecimal = ruleDto.getDonateAmountMax();
            }
            bonus.setBonusAmount(formatDownRounding(bigDecimal));
        }
        int multipleWater = nonNull(ruleDto.getMultipleWater()) ? ruleDto.getMultipleWater().intValue() : 0;
        bonus.setAuditRule(dto.getAuditCats());
        bonus.setDiscountAudit(multipleWater);
        bonus.setScope(dto.getScope());
    }

    private void checkoutAmountMin(List<ActivityRuleDto> activityRuleDtos, BigDecimal amount) {
        ActivityRuleDto minRuleDto = castMinAmount(activityRuleDtos);
        if (minRuleDto.getAmountMin().compareTo(amount) == 1) {
            throw new R200Exception("该优惠最少使用金额:" + minRuleDto.getAmountMin());
        }
    }

    private void setDepositSentBonus(OprActRule rule, Integer depotId, BigDecimal amount, OprActBonus bonus) {
        JDepositSentDto dto = jsonUtil.fromJson(rule.getRule(), JDepositSentDto.class);
        checkoutDepot(dto.getAuditCats(), depotId);
        checkoutAmountMin(dto.getActivityRuleDtos(), amount);
        setBonusAmount(dto, amount, bonus);
        bonus.setBonusAmount(formatDownRounding(bonus.getBonusAmount()));
        bonus.setAuditRule(dto.getAuditCats());
        bonus.setScope(null);
    }

    private void checkoutDepot(List<AuditCat> auditCats, Integer depotId) {
        Boolean isDepot = Boolean.FALSE;
        for (AuditCat auditCat : auditCats) {
            if (Boolean.TRUE.equals(auditCat.getIsAll())) {
                List<TGmDepot> depots = operateActivityMapper.findDepotByCatId(auditCat.getCatId());
                for (TGmDepot depot : depots) {
                    if (depot.getId() == depotId) {
                        isDepot = Boolean.TRUE;
                        break;
                    }
                }
            }
            if (Boolean.FALSE.equals(auditCat.getIsAll()) && Collections3.isNotEmpty(auditCat.getDepots())) {
                for (AuditDepot depot : auditCat.getDepots()) {
                    if (depot.getDepotId() == depotId) {
                        isDepot = Boolean.TRUE;
                        break;
                    }
                }
            }
            if (Boolean.TRUE.equals(isDepot)) break;
        }
        if (Boolean.FALSE.equals(isDepot)) {
            throw new R200Exception("该平台不在优惠的使用范围");
        }
    }

}

