package com.eveb.saasops.modules.fund.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.servlet.http.HttpServletResponse;

import com.eveb.saasops.common.constants.MerchantPayConstants;
import com.eveb.saasops.common.constants.SystemConstants;
import com.eveb.saasops.common.exception.R200Exception;
import com.eveb.saasops.common.utils.*;
import com.eveb.saasops.listener.BizEvent;
import com.eveb.saasops.listener.BizEventType;
import com.eveb.saasops.modules.fund.dao.*;
import com.eveb.saasops.modules.fund.dto.*;
import com.eveb.saasops.modules.fund.entity.*;
import com.eveb.saasops.modules.member.dao.MbrAccountMapper;
import com.eveb.saasops.modules.member.dao.MbrAuditAccountMapper;
import com.eveb.saasops.modules.member.dao.MbrBillDetailMapper;
import com.eveb.saasops.modules.member.entity.*;
import com.eveb.saasops.modules.member.service.*;
import com.eveb.saasops.modules.system.systemsetting.entity.SysSetting;
import com.eveb.saasops.modules.system.systemsetting.service.SysSettingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.constants.Constants.Available;
import com.eveb.saasops.common.constants.OrderConstants;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.config.MessagesConfig;
import com.eveb.saasops.modules.base.service.BaseService;
import com.eveb.saasops.modules.fund.mapper.FundMapper;
import com.eveb.saasops.modules.member.dao.MbrBankcardMapper;
import com.eveb.saasops.modules.member.entity.MbrWithdrawalCond.FeeWayVal;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;

import static com.eveb.saasops.common.constants.MerchantPayConstants.HUI_TONG_PAY_ID;
import static com.eveb.saasops.common.constants.MerchantPayConstants.PAN_ZI_PAY_ID;
import static com.eveb.saasops.common.utils.DateUtil.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@Transactional
public class FundWithdrawService extends BaseService<AccWithdrawMapper, AccWithdraw> {

    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private MbrBankcardMapper mbrBankcardMapper;
    @Autowired
    private AccWithdrawMapper accWithdrawMapper;
    @Autowired
    private AgyBankCardMapper agyBankCardMapper;
    @Autowired
    private AgyWithdrawMapper agyWithdrawMapper;
    @Autowired
    private MessagesConfig messagesConfig;
    @Autowired
    private MbrBankcardService mbrBankcardService;
    @Value("${fund.accWithdraw.excel.path}")
    private String accExcelPath;
    @Value("${fund.agyWithdraw.excel.path}")
    private String agyExcelPath;
    @Autowired
    private MbrAccountService mbrAccountService;
    @Autowired
    private MbrWalletService mbrWalletService;
    @Autowired
    private MbrBillDetailMapper mbrBillDetailMapper;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private MbrAuditAccountMapper accountAuditMapper;
    @Autowired
    private MbrWithdrawalCondService withdrawalCond;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private FundWhiteListMapper whiteListMapper;
    @Autowired
    private HuiTongPayService huiTongPayService;
    @Autowired
    private PanZiPayService panZiPayService;
    @Autowired
    private FundMerchantDetailMapper merchantDetailMapper;
    @Autowired
    private FundMerchantPayMapper merchantPayMapper;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private TChannelPayMapper channelPayMapper;


    public PageUtils queryAccListPage(AccWithdraw accWithdraw, Integer pageNo, Integer pageSize) {
        accWithdraw.setBaseAuth(getRowAuth());
        PageHelper.startPage(pageNo, pageSize);
        List<Integer> statuss = accWithdraw.getStatuss();
        //判断statuss是否为空
        if (Collections3.isNotEmpty(statuss)) {
            int forFlag = statuss.size();
            for (int i = 0; i < statuss.size(); i++) {
                if (2 == statuss.get(i) || 4 == statuss.get(i)) {
                    statuss.add(4);
                    statuss.add(2);
                } else if (3 == statuss.get(i) || 5 == statuss.get(i)) {
                    statuss.add(3);
                    statuss.add(5);
                }
                if (i + 1 == forFlag) {
                    break;
                }
            }
        }
        List<AccWithdraw> list = fundMapper.findAccWithdrawList(accWithdraw);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils queryAccListPage(String startTime, String endTime, Integer accountId, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<AccWithdraw> list = fundMapper.findFixateAccWithdraw(startTime, endTime, accountId);
        return BeanUtil.toPagedResult(list);
    }

    public double totalActualArrival(String startTime, String endTime, Integer accountId) {
        return fundMapper.totalFixateAccWithdraw(startTime, endTime, accountId);
    }

    public Double accSumDrawingAmount(String loginSysUserName) {
        AccWithdraw accWithdraw = new AccWithdraw();
        accWithdraw.setStatus(Constants.IsStatus.succeed);
        accWithdraw.setPassTime(getCurrentDate(FORMAT_10_DATE));
        accWithdraw.setLoginSysUserName(loginSysUserName);
        return fundMapper.accSumDrawingAmount(accWithdraw);
    }

    public AccWithdraw queryAccObject(Integer id) {
        AccWithdraw accWithdraw = new AccWithdraw();
        accWithdraw.setId(id);
        Optional<AccWithdraw> optional = Optional.ofNullable(
                fundMapper.findAccWithdrawList(accWithdraw)
                        .stream().findAny()).get();
        if (optional.isPresent()) {
            AccWithdraw accWithdraw1 = optional.get();
            accWithdraw1.setMbrBankcard(mbrBankcardMapper.
                    selectByPrimaryKey(accWithdraw1.getBankCardId()));
            AccWithdraw accCount = new AccWithdraw();
            accCount.setNotStatus(Constants.IsStatus.defeated);
            accCount.setAccountId(accWithdraw1.getAccountId());
            accWithdraw1.setWithdrawCount(fundMapper.findAccWithdrawCount(accCount));
            return accWithdraw1;
        }
        return null;
    }


    public void checkoutStatusByTwo(Integer id) {
        AccWithdraw withdraw = accWithdrawMapper.selectByPrimaryKey(id);
        if (withdraw.getStatus() != Constants.IsStatus.pending
                && withdraw.getStatus() != Constants.IsStatus.four) {
            throw new R200Exception("请刷新数据");
        }
    }

    public void updateAccStatus(AccWithdraw accWithdraw, String loginName, BizEvent bizEvent) {
        AccWithdraw withdraw = checkoutFund(accWithdraw.getId());
        setBizEvent(withdraw, bizEvent, accWithdraw);
        withdraw.setMemo(accWithdraw.getMemo());
        withdraw.setModifyUser(loginName);
        withdraw.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));

        if (accWithdraw.getStatus() == Constants.EVNumber.one
                && withdraw.getStatus() == Constants.EVNumber.three) {
            withdraw.setPassTime(getCurrentDate(FORMAT_18_DATE_TIME));
            withdraw.setPassUser(loginName);
            withdraw.setStatus(accWithdraw.getStatus());
            updateIsDrawings(withdraw.getAccountId(), Constants.EVNumber.one);
        }

        if (accWithdraw.getStatus() == Constants.EVNumber.one
                && withdraw.getStatus() == Constants.EVNumber.two) {
            withdraw.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
            withdraw.setAuditUser(loginName);
            withdraw.setStatus(Constants.EVNumber.three);
            beginMerchantPayment(withdraw);
            return;
        }

        if (accWithdraw.getStatus() == Constants.EVNumber.zero
                && (withdraw.getStatus() == Constants.EVNumber.two
                || withdraw.getStatus() == Constants.EVNumber.three)) {
            withdraw.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
            withdraw.setAuditUser(loginName);
            withdraw.setStatus(accWithdraw.getStatus());
            setAccWithdrawZreo(withdraw);
            updateIsDrawings(withdraw.getAccountId(), Constants.EVNumber.zero);
        }
        accWithdrawMapper.updateByPrimaryKey(withdraw);
     /*   if (accWithdraw.getStatus() == Constants.EVNumber.zero
                && withdraw.getStatus() == Constants.EVNumber.four) {
            withdraw.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
            withdraw.setAuditUser(loginName);
            withdraw.setType(Constants.EVNumber.zero);
            withdraw.setStatus(Constants.EVNumber.two);
        }

        if (accWithdraw.getStatus() == Constants.EVNumber.one
                && withdraw.getStatus() == Constants.EVNumber.four) {
            withdraw.setStatus(accWithdraw.getStatus());
            beginMerchantPayment(withdraw);
            withdraw.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
            withdraw.setAuditUser(loginName);
        }*/
    }

    private void setBizEvent(AccWithdraw withdraw, BizEvent bizEvent, AccWithdraw accWithdraw) {
        bizEvent.setUserId(withdraw.getAccountId());
        bizEvent.setWithdrawMoney(withdraw.getDrawingAmount());
        if (accWithdraw.getStatus() == Constants.EVNumber.zero
                && withdraw.getStatus() == Constants.EVNumber.two) {
            bizEvent.setEventType(BizEventType.MEMBER_WITHDRAWAL_PRIMARY_VERIFY_FAILED);
        }
        if (accWithdraw.getStatus() == Constants.EVNumber.zero
                && withdraw.getStatus() == Constants.EVNumber.three) {
            bizEvent.setEventType(BizEventType.MEMBER_WITHDRAWAL_REVIEW_VERIFY_FAILED);
        }
        if (accWithdraw.getStatus() == Constants.EVNumber.one
                && withdraw.getStatus() == Constants.EVNumber.three) {
            bizEvent.setEventType(BizEventType.MEMBER_WITHDRAWAL_REVIEW_VERIFY_SUCCESS);
        }
    }


    private void setAccWithdrawZreo(AccWithdraw accWithdraw) {
        MbrBillDetail billDetail = mbrBillDetailMapper.selectByPrimaryKey(accWithdraw.getBillDetailId());
        if (Objects.nonNull(billDetail)) {
            mbrWalletService.castWalletAndBillDetail(billDetail.getLoginName(), billDetail.getAccountId(),
                    billDetail.getFinancialCode(), billDetail.getAmount(), accWithdraw.getOrderNo(), Boolean.TRUE);
        }
    }


    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public void saveApply(AccWithdraw withDraw, String pwd) {
        if (mbrAccountService.updateOrCheckScPwd(withDraw.getAccountId(), pwd)) {
            checkoutAccWithdraw(withDraw);
            // 行政费与优惠计算
            withdrawAudit(withDraw);
            withDraw.setDiscountAmount(nonNull(withDraw.getDiscountAmount())
                    ? withDraw.getDiscountAmount() : BigDecimal.ZERO);
            BigDecimal bigDecimal = withDraw.getCutAmount().add(withDraw.getDiscountAmount());
            if (bigDecimal.compareTo(withDraw.getDrawingAmount()) == 1) {
                throw new R200Exception("提款金额必须大于需要扣款的金额:" + bigDecimal + "元!");
            }

            MbrBillDetail mbrBillDetail = new MbrBillDetail();
            mbrBillDetail.setLoginName(withDraw.getLoginName());
            mbrBillDetail.setAccountId(withDraw.getAccountId());
            mbrBillDetail.setFinancialCode(OrderConstants.FUND_ORDER_ACCWITHDRAW);
            mbrBillDetail.setOrderNo(new SnowFlake().nextId()+"");
            mbrBillDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
            mbrBillDetail.setDepotId(Constants.SYS_DEPOT_ID);
            mbrBillDetail.setOpType(MbrBillDetail.OpTypeStatus.expenditure);
            mbrBillDetail.setAmount(withDraw.getDrawingAmount());
            mbrBillDetail.setMemo("提款金额:" + withDraw.getDrawingAmount()
                    + ",行政扣款:" + withDraw.getCutAmount() + ",扣除优惠:"
                    + withDraw.getDiscountAmount() + ",转账手续费:"
                    + withDraw.getHandlingCharge());

            MbrWallet mbrWallet = new MbrWallet();
            mbrWallet.setAccountId(mbrBillDetail.getAccountId());
            mbrWallet.setBalance(mbrBillDetail.getAmount());

            withDraw.setOrderNo(mbrBillDetail.getOrderNo());
            withDraw.setOrderPrefix(mbrBillDetail.getFinancialCode());
            boolean flag = mbrWalletService.walletSubtract(mbrWallet, mbrBillDetail);
            if (Boolean.FALSE.equals(flag)) {
                throw new RRException("取款失败!");
            }
            withDraw.setBillDetailId(mbrBillDetail.getId());
            withDraw.setType(Constants.EVNumber.zero);
            withDraw.setStatus(Constants.EVNumber.two);
            withDraw.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
            super.save(withDraw);
            //merchantPayment(withDraw);
        }
    }

    private void checkoutAccWithdraw(AccWithdraw withDraw) {
        MbrAccount account = accountMapper.selectByPrimaryKey(withDraw.getAccountId());
        if (account.getAvailable() == Constants.EVNumber.two) {
            throw new R200Exception("余额冻结，不可以提款");
        }
        if (fundMapper.sumApplyRec(withDraw.getAccountId()) > 0) {
            throw new R200Exception("你最近有一笔取款正在处理中，请等待处理完成之后再申请取款");
        }
        MbrBankcard mbrBankcard = new MbrBankcard();
        mbrBankcard.setId(withDraw.getBankCardId());
        mbrBankcard.setAccountId(withDraw.getAccountId());
        mbrBankcard.setIsDel(Available.disable);
        mbrBankcard = mbrBankcardService.queryObjectCond(mbrBankcard);
        if (Objects.isNull(mbrBankcard)) {
            throw new R200Exception("无此银行卡,不能取款");
        }
        MbrWithdrawalCond cond = withdrawalCond.getMbrWithDrawal(withDraw.getAccountId());
        if (!StringUtils.isEmpty(cond.getLowQuota())
                && withDraw.getDrawingAmount().compareTo(cond.getLowQuota()) == -1) {
            throw new R200Exception("取款金额小于最低取款金额");
        }
        if (!StringUtils.isEmpty(cond.getTopQuota())
                && withDraw.getDrawingAmount().compareTo(cond.getTopQuota()) == 1) {
            throw new R200Exception("取款金额大于最高取款金额");
        }
        AccWithdraw withdrawCount = sumWithDraw(withDraw.getAccountId());
        if (!StringUtils.isEmpty(cond.getFeeAvailable()) && cond.getFeeAvailable() == Available.enable) {
            if (!StringUtils.isEmpty(cond.getWithDrawalTimes())
                    && withdrawCount.getWithdrawCount() >= cond.getWithDrawalTimes()) {
                throw new R200Exception("每天取款次数已达到最多" + cond.getWithDrawalTimes() + "次");
            }
            if (!StringUtils.isEmpty(cond.getWithDrawalQuota())) {
                BigDecimal temp = BigDecimalMath.add(withdrawCount.getActualArrival(), withDraw.getDrawingAmount());
                if (temp.compareTo(cond.getWithDrawalQuota()) == 1) {
                    throw new R200Exception("每天最大取现金额为" + cond.getWithDrawalQuota() + "元");
                }
            }
        }
    }

    @Deprecated
    private void merchantPayment(AccWithdraw withDraw) {
        Boolean isPayment = checkoutMerchantPayment(withDraw);
        FundMerchantPay merchantPay = getFundMerchantPay();
        if (Boolean.FALSE.equals(isPayment) || null == merchantPay) {
            withDraw.setType(Constants.EVNumber.zero);
            accWithdrawMapper.updateByPrimaryKey(withDraw);
            return;
        }
        //判断取款的设备来源与出款管理中代付的设备来源是否匹配
        Boolean isMatchDevSource = merchantPay.getDevSource().contains(withDraw.getWithdrawSource().toString());
        if (!isMatchDevSource) {
            withDraw.setType(Constants.EVNumber.zero);
            accWithdrawMapper.updateByPrimaryKey(withDraw);
            return;
        }
        isMerchantPayment(withDraw);

    }

    @Deprecated
    private void isMerchantPayment(AccWithdraw withDraw) {
        FundWhiteList whiteList = new FundWhiteList();
        whiteList.setAccountId(withDraw.getAccountId());
        int count = whiteListMapper.selectCount(whiteList);
        if (count == 0) {
            withDraw.setStatus(Constants.EVNumber.four);
            withDraw.setType(Constants.EVNumber.one);
            accWithdrawMapper.updateByPrimaryKey(withDraw);
            return;
        }
        withDraw.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        withDraw.setAuditUser(Constants.SYSTEM_USER);
        beginMerchantPayment(withDraw);
    }

    private FundMerchantPay getFundMerchantPay() {
        FundMerchantPay merchantPay = new FundMerchantPay();
        merchantPay.setAvailable(Constants.EVNumber.one);
        return merchantPayMapper.selectOne(merchantPay);
    }

    private void beginMerchantPayment(AccWithdraw withDraw) {
        Boolean isPayment = checkoutMerchantPayment(withDraw);
        if (Boolean.FALSE.equals(isPayment)) {
            withDraw.setType(Constants.EVNumber.zero);
            accWithdrawMapper.updateByPrimaryKey(withDraw);
            return;
        }
        FundMerchantPay merchantPay = getFundMerchantPay();
        MbrBankcard bankcard = mbrBankcardMapper.selectByPrimaryKey(withDraw.getBankCardId());
        if (merchantPay.getChannelId() == HUI_TONG_PAY_ID) {
            huiTongPayment(withDraw, merchantPay, bankcard);
        }
        if (merchantPay.getChannelId() == PAN_ZI_PAY_ID) {
            panZiPayment(withDraw, merchantPay, bankcard);
        }
    }

    private void huiTongPayment(AccWithdraw withDraw, FundMerchantPay merchantPay, MbrBankcard bankcard) {
        HTBalanceResponseDto balanceResponseDto = huiTongPayService.balance(merchantPay);
        String bankCode = MerchantPayConstants.htMerchantPayMap.get(bankcard.getBankName());
        String realName = bankcard.getRealName();
        String cardNo = bankcard.getCardNo();
        if (isNull(balanceResponseDto) || Boolean.FALSE.equals(balanceResponseDto.getIs_success())
                || withDraw.getActualArrival().compareTo(nonNull(balanceResponseDto.getMoney()) ?
                new BigDecimal(balanceResponseDto.getMoney()) : BigDecimal.ZERO) == 1
                || StringUtil.isEmpty(bankCode) || StringUtil.isEmpty(realName) || StringUtil.isEmpty(cardNo)) {
            updateMerchantPaymentWithdraw(withDraw);
            return;
        }
        HTRemitResponseDto responseDto = huiTongPayService.remitPay(
                merchantPay, withDraw.getOrderNo().toString(),
                withDraw.getActualArrival(), bankCode, realName, cardNo);
        if (isNull(responseDto) || Boolean.FALSE.equals(responseDto.getIs_success())
                || StringUtil.isEmpty(responseDto.getBank_status())
                || "3".equals(responseDto.getBank_status())) {
            updateMerchantPaymentWithdraw(withDraw);
            return;
        }
        succeedMerchantPay(merchantPay, withDraw, responseDto.getBank_status(), responseDto.getOrder_id(), responseDto.getTransid());
    }

    private void panZiPayment(AccWithdraw withDraw, FundMerchantPay merchantPay, MbrBankcard bankcard) {
        String bankCode = MerchantPayConstants.pzMerchantPayMap.get(bankcard.getBankName());
        String realName = bankcard.getRealName();
        String cardNo = bankcard.getCardNo();
        if (StringUtil.isEmpty(bankCode) || StringUtil.isEmpty(realName) || StringUtil.isEmpty(cardNo)) {
            updateMerchantPaymentWithdraw(withDraw);
            return;
        }
        PZPaymentRequestDto responseDto = panZiPayService.debitPayment(withDraw.getOrderNo(),
                bankCode, realName, cardNo, withDraw.getActualArrival(), merchantPay);
        if (isNull(responseDto)) {
            updateMerchantPaymentWithdraw(withDraw);
            return;
        }
        succeedMerchantPay(merchantPay, withDraw, "1", null, responseDto.getOut_trade_no());
    }

    private void succeedMerchantPay(FundMerchantPay merchantPay, AccWithdraw withDraw, String bankStatus, String orderId, String transId) {
        FundMerchantDetail merchantDetail = new FundMerchantDetail();
        merchantDetail.setMerchantId(merchantPay.getId());
        merchantDetail.setMerchantName(merchantPay.getMerchantName());
        merchantDetail.setMerchantNo(merchantPay.getMerchantNo());
        merchantDetail.setBankStatus(bankStatus);
        merchantDetail.setOrderId(orderId);
        merchantDetail.setTransId(transId);
        merchantDetail.setAccWithdrawId(withDraw.getId());
        withDraw.setStatus("2".equals(bankStatus) || "SUCCESS".equals(bankStatus) ?
                Constants.EVNumber.one : Constants.EVNumber.five);
        if ("2".equals(bankStatus) || "SUCCESS".equals(bankStatus)) {
            withDraw.setPassTime(getCurrentDate(FORMAT_18_DATE_TIME));
            withDraw.setPassUser(Constants.SYSTEM_PASSUSER);
        }
        withDraw.setType(Constants.EVNumber.one);
        accWithdrawMapper.updateByPrimaryKey(withDraw);
        merchantDetailMapper.insert(merchantDetail);
    }

    private void updateMerchantPaymentWithdraw(AccWithdraw withDraw) {
        withDraw.setStatus(Constants.EVNumber.three);
        withDraw.setType(Constants.EVNumber.zero);
        accWithdrawMapper.updateByPrimaryKey(withDraw);
    }

    private Boolean checkoutMerchantPayment(AccWithdraw withDraw) {
        SysSetting setting = sysSettingService.getSysSetting(SystemConstants.PAY_AUTOMATIC);
        SysSetting moneySetting = sysSettingService.getSysSetting(SystemConstants.PAY_MONEY);
        if (isNull(setting) || "0".equals(setting.getSysvalue())) {
            return Boolean.FALSE;
        }
        if (nonNull(moneySetting) && withDraw.getDrawingAmount()
                .compareTo(new BigDecimal(moneySetting.getSysvalue())) == 1) {
            return Boolean.FALSE;
        }
        int count = fundMapper.findMerchantPayCount(withDraw.getAccountId());
        if (count == 0) {
            return Boolean.FALSE;
        }
        FundMerchantPay merchantPay = getFundMerchantPay();
        if (isNull(merchantPay) || !merchantPay.getDevSource().contains(withDraw.getWithdrawSource().toString())) {
            return Boolean.FALSE;
        }
        TChannelPay channelPay = channelPayMapper.selectByPrimaryKey(merchantPay.getChannelId());
        if (isNull(channelPay) || channelPay.getAvailable() == Constants.EVNumber.zero) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public void updateMerchantPayment(Integer accountId, String siteCode) {
        List<AccWithdraw> accWithdraws = fundMapper.fundAccWithdrawMerchant(accountId);
        if (Collections3.isNotEmpty(accWithdraws)) {
            accWithdraws.stream().forEach(as -> {
                AccWithdraw accWithdraw = fundMapper.updateMerchantPayLock(as.getId());
                if (accWithdraw.getStatus() == Constants.EVNumber.five) {
                    FundMerchantPay merchantPay = merchantPayMapper.selectByPrimaryKey(as.getMerchantId());
                    if (merchantPay.getChannelId() == HUI_TONG_PAY_ID) {
                        updateHuiTongMerchantPayment(merchantPay, as, siteCode);
                    }
                    if (merchantPay.getChannelId() == PAN_ZI_PAY_ID) {
                        updatePanZiMerchantPayment(merchantPay, as, siteCode);
                    }
                }
            });
        }
    }

    private void updateHuiTongMerchantPayment(FundMerchantPay merchantPay, AccWithdraw as, String siteCode) {
        HTRemitResponseDto responseDto = huiTongPayService.remitQuery(as, merchantPay);
        if (nonNull(responseDto)) {
            if (Boolean.TRUE.equals(responseDto.getIs_success()) && "2".equals(responseDto.getBank_status())) {
                updateMerchantPaymentStatus(Constants.EVNumber.one, as.getId(),
                        as.getMerchantDetailId(), responseDto.getBank_status());
                sendWithdrawMsg(siteCode, as.getAccountId());
            }
            if (Boolean.TRUE.equals(responseDto.getIs_success()) && "3".equals(responseDto.getBank_status())) {
                updateMerchantPaymentStatus(Constants.EVNumber.three, as.getId(),
                        as.getMerchantDetailId(), responseDto.getBank_status());
            }
        }
    }

    private void updatePanZiMerchantPayment(FundMerchantPay merchantPay, AccWithdraw as, String siteCode) {
        PZQueryContentDto responseDto = panZiPayService.debitQuery(as, merchantPay);
        if (nonNull(responseDto) && StringUtil.isNotEmpty(responseDto.getTrade_status())) {
            if ("SUCCESS".equals(responseDto.getTrade_status())) {
                updateMerchantPaymentStatus(Constants.EVNumber.one, as.getId(), as.getMerchantDetailId(), "2");
                sendWithdrawMsg(siteCode, as.getAccountId());
            }
            if ("FAIL".equals(responseDto.getTrade_status()) || "REFUND".equals(responseDto.getTrade_status())) {
                updateMerchantPaymentStatus(Constants.EVNumber.three, as.getId(), as.getMerchantDetailId(), "3");
            }
        }
    }

    private void sendWithdrawMsg(String siteCode, Integer accountId) {
        BizEvent bizEvent = new BizEvent(this, siteCode, accountId, BizEventType.MEMBER_WITHDRAWAL_REVIEW_VERIFY_SUCCESS);
        applicationEventPublisher.publishEvent(bizEvent);
    }

    private void updateMerchantPaymentStatus(int status, int accWithdrawId, int merchantDetailId, String bankStatus) {
        AccWithdraw accWithdraw = new AccWithdraw();
        accWithdraw.setStatus(status);
        accWithdraw.setId(accWithdrawId);
        if (status == Constants.EVNumber.three) {
            accWithdraw.setType(Constants.EVNumber.zero);
        }
        if (status == Constants.EVNumber.one) {
            accWithdraw.setPassTime(getCurrentDate(FORMAT_18_DATE_TIME));
            accWithdraw.setPassUser(Constants.SYSTEM_PASSUSER);
        }
        accWithdrawMapper.updateByPrimaryKeySelective(accWithdraw);
        FundMerchantDetail merchantDetail = new FundMerchantDetail();
        merchantDetail.setId(merchantDetailId);
        merchantDetail.setBankStatus(bankStatus);
        merchantDetailMapper.updateByPrimaryKeySelective(merchantDetail);
    }

    private void updateIsDrawings(Integer accountId, Integer isDrawings) {
        MbrAuditAccount accountAudit = new MbrAuditAccount();
        accountAudit.setAccountId(accountId);
        accountAudit.setIsDrawings(Constants.EVNumber.two);
        List<MbrAuditAccount> auditAccounts = accountAuditMapper.select(accountAudit);
        if (Collections3.isNotEmpty(auditAccounts)) {
            auditAccounts.stream().forEach(as -> {
                MbrAuditAccount mbrAccountAudit = new MbrAuditAccount();
                mbrAccountAudit.setIsDrawings(isDrawings);
                mbrAccountAudit.setId(as.getId());
                if (isDrawings == Constants.EVNumber.one) {
                    mbrAccountAudit.setStatus(Constants.EVNumber.one);
                }
                accountAuditMapper.updateByPrimaryKeySelective(mbrAccountAudit);
            });
            if (isDrawings == Constants.EVNumber.one) {
                MbrAuditAccount auditAccount = auditAccounts.get(auditAccounts.size() - 1);
                auditAccountService.addOrUpdateHistoryByDeposit(auditAccounts.get(0), auditAccount.getTime());
            }
        } else {
            if (isDrawings == Constants.EVNumber.one) {
                MbrAuditAccount mbrAccountAudit = new MbrAuditAccount();
                mbrAccountAudit.setAccountId(accountId);
                auditAccountService.addOrUpdateHistoryByDeposit(mbrAccountAudit, getCurrentDate(FORMAT_18_DATE_TIME));
            }
        }
    }

    private void withdrawAudit(AccWithdraw withDraw) {
        List<MbrAuditAccount> audits = auditAccountService
                .getMbrAuditAccounts(withDraw.getAccountId());
        checkoutAudit(audits);
        withDraw.setCutAmount(BigDecimal.ZERO);
        withDraw.setHandlingCharge(nonNull(withDraw.getHandlingCharge()) ?
                withDraw.getHandlingCharge() : BigDecimal.ZERO);
        BigDecimal bigDecimal = withDraw.getCutAmount().add(nonNull(withDraw.getDiscountAmount())
                ? withDraw.getDiscountAmount() : BigDecimal.ZERO);
        withDraw.setActualArrival(withDraw.getDrawingAmount().subtract(bigDecimal));
        audits.stream().forEach(as -> {
            as.setIsDrawings(Constants.EVNumber.two);
            accountAuditMapper.updateByPrimaryKey(as);
        });
    }

    private void checkoutAudit(List<MbrAuditAccount> audits) {
        //SysSetting setting = sysSettingService.getSysSetting(SystemConstants.PAY_DRAW);
        //if (nonNull(setting) && "0".equals(setting.getSysvalue())) {
        long depositCountNot = audits.stream().filter(p -> nonNull(p.getDepositAmount())
                && p.getDepositAmount().compareTo(BigDecimal.ZERO) == 1
                && Constants.EVNumber.zero == p.getStatus()).map(MbrAuditAccount::getId).count();
        if (depositCountNot > 0) {
            throw new R200Exception("不满足稽核条件,无法提款!");
        }
        // }
    }

    private AccWithdraw checkoutFund(Integer id) {
        AccWithdraw withdraw = accWithdrawMapper.selectByPrimaryKey(id);
        if (withdraw.getStatus() == Constants.IsStatus.succeed
                || withdraw.getStatus() == Constants.IsStatus.defeated) {
            throw new R200Exception(messagesConfig.getValue("saasops.illegal.request"));
        }
        if (nonNull(withdraw.getType())) {
            if (withdraw.getStatus() == Constants.EVNumber.two
                    && withdraw.getType() == Constants.EVNumber.three) {
                throw new R200Exception("该订单正在进行代付处理!");
            }
            if (withdraw.getStatus() == Constants.EVNumber.five) {
                throw new R200Exception("该订单已经由代付处理，请勿手工处理");
            }
        }
        return withdraw;
    }

    public void updateAccMemo(Integer id, String memo, String loginName) {
        AccWithdraw withdraw = accWithdrawMapper.selectByPrimaryKey(id);
        if (withdraw.getStatus() == Constants.EVNumber.one) {
            throw new R200Exception("已经完成出款，不可以修改备注");
        }
        withdraw.setMemo(memo);
        withdraw.setModifyUser(loginName);
        withdraw.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        accWithdrawMapper.updateByPrimaryKey(withdraw);
    }

    public PageUtils queryAgyListPage(AgyWithdraw agyWithdraw, Integer pageNo, Integer pageSize, String orderBy) {
        agyWithdraw.setBaseAuth(getRowAuth());
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        List<AgyWithdraw> list = fundMapper.findAgyWithdrawList(agyWithdraw);
        return BeanUtil.toPagedResult(list);
    }

    public AgyWithdraw queryAgyObject(Integer id) {
        AgyWithdraw agyWithdraw = new AgyWithdraw();
        agyWithdraw.setBaseAuth(getRowAuth());
        agyWithdraw.setId(id);
        Optional<AgyWithdraw> optional = Optional.ofNullable(
                fundMapper.findAgyWithdrawList(agyWithdraw)
                        .stream().findAny()).get();
        if (optional.isPresent()) {
            AgyWithdraw agyWithdraw1 = optional.get();
            agyWithdraw1.setAgyBankcard(agyBankCardMapper.
                    selectByPrimaryKey(agyWithdraw1.getBankCardId()));
            return agyWithdraw1;
        }
        return null;
    }

    public void updateAgyStatus(AgyWithdraw withdraw, String loginName) {
        AgyWithdraw agyWithdraw = agyWithdrawMapper.selectByPrimaryKey(withdraw.getId());
        if (agyWithdraw.getStatus() != Constants.IsStatus.pending
                && agyWithdraw.getStatus() != Constants.IsStatus.outMoney) {
            throw new RRException(messagesConfig.getValue("saasops.illegal.request"));
        }
        agyWithdraw.setMemo(withdraw.getMemo());
        agyWithdraw.setStatus(withdraw.getStatus());
        agyWithdraw.setModifyUser(loginName);
        agyWithdraw.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        agyWithdraw.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        agyWithdraw.setAuditUser(loginName);
        agyWithdrawMapper.updateByPrimaryKey(agyWithdraw);
    }

    public void updateAgyMemo(Integer id, String memo, String loginName) {
        AgyWithdraw withdraw = agyWithdrawMapper.selectByPrimaryKey(id);
        withdraw.setMemo(memo);
        withdraw.setModifyUser(loginName);
        withdraw.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        agyWithdrawMapper.updateByPrimaryKey(withdraw);
    }


    public void agyExportExecl(AgyWithdraw agyWithdraw, HttpServletResponse response) {
        String fileName = "代理提款" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xls";
        List<AgyWithdraw> withdraws = fundMapper.findAgyWithdrawList(agyWithdraw);
        List<Map<String, Object>> list = Lists.newArrayList();
        withdraws.stream().forEach(ws -> {
            Map<String, Object> paramr = new HashMap<>();
            paramr.put("orderNo", ws.getOrderPrefix() + ws.getOrderNo());
            paramr.put("withdrawCount", ws.getWithdrawCount() > 1 ? "否" : "是");
            paramr.put("agyAccount", ws.getAgyAccount());
            paramr.put("drawingAmount", ws.getDrawingAmount());
            paramr.put("auditUser", ws.getAuditUser());
            paramr.put("memo", ws.getMemo());
            list.add(paramr);
        });
        Workbook workbook = ExcelUtil.commonExcelExportList("mapList", accExcelPath, list);
        try {
            ExcelUtil.writeExcel(response, workbook, fileName);
        } catch (IOException e) {
            throw new RRException(e.getMessage());
        }
    }


    public void accExportExecl(AccWithdraw accWithdraw, HttpServletResponse response) {
        String fileName = "会员提款" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xls";
        List<AccWithdraw> fundDeposits = fundMapper.findAccWithdrawList(accWithdraw);
        List<Map<String, Object>> list = Lists.newArrayList();
        fundDeposits.stream().forEach(deposit -> {
            Map<String, Object> paramr = new HashMap<>();
            paramr.put("orderNo", deposit.getOrderPrefix() + deposit.getOrderNo());
            paramr.put("loginName", deposit.getLoginName());
            paramr.put("groupName", deposit.getGroupName());
            paramr.put("drawingAmount", deposit.getDrawingAmount());
            paramr.put("auditUser", deposit.getAuditUser());
            paramr.put("status", deposit.getStatus() == 0
                    ? Constants.ChineseStatus.defeated : deposit.getStatus() == 1
                    ? Constants.ChineseStatus.succeed : Constants.ChineseStatus.pending);
            list.add(paramr);
        });
        Workbook workbook = ExcelUtil.commonExcelExportList("mapList", agyExcelPath, list);
        try {
            ExcelUtil.writeExcel(response, workbook, fileName);
        } catch (IOException e) {
            throw new RRException(e.getMessage());
        }
    }

    public AccWithdraw sumWithDraw(Integer accountId) {
        String startTime = getTodayStart(FORMAT_10_DATE);
        String endTime = getTodayEnd(FORMAT_10_DATE);
        return fundMapper.sumWithDraw(startTime, endTime, accountId);
    }

    public BigDecimal calculateFee(BigDecimal actualArrival, MbrWithdrawalCond cond, Integer accountId) {
        Byte freeFee = fundMapper.isFreeFee(cond.getFeeTimes(), cond.getFeeHours(), accountId);
        if (freeFee == Available.enable) {
            return BigDecimal.ZERO;
        } else {
            if (!StringUtils.isEmpty(cond.getFeeWay()) && cond.getFeeWay() == FeeWayVal.fixed) {
                return cond.getFeeFixed();
            } else {
                BigDecimal fee = BigDecimalMath.div(BigDecimalMath.mul(actualArrival, cond.getFeeScale()), new BigDecimal("100"), 2);
                if (fee.compareTo(cond.getFeeTop()) == 1)
                    return cond.getFeeTop();
                else
                    return fee;
            }
        }
    }

    /**
     * @param actualArrival 实际出款金额(不是申请金额)
     * @param accountId     会员账号
     * @return
     */
    public BigDecimal calculateFee(BigDecimal actualArrival, Integer accountId) {
        MbrWithdrawalCond cond = withdrawalCond.getMbrWithDrawal(accountId);
        return calculateFee(actualArrival, cond, accountId);
    }
}
