package com.eveb.saasops.api.modules.pay.pzpay.service;

import java.math.BigDecimal;

import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.modules.fund.entity.DepositPostScript;
import com.eveb.saasops.modules.fund.service.FundDepositService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eveb.saasops.api.modules.pay.pzpay.entity.PzpayPayParams;
import com.eveb.saasops.common.constants.OrderConstants;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.BigDecimalMath;
import com.eveb.saasops.modules.fund.dao.FundDepositMapper;
import com.eveb.saasops.modules.fund.entity.FundDeposit;
import com.eveb.saasops.modules.fund.entity.FundDeposit.Mark;
import com.eveb.saasops.modules.fund.entity.FundDeposit.PaymentStatus;
import com.eveb.saasops.modules.fund.entity.FundDeposit.Status;
import com.eveb.saasops.modules.fund.mapper.FundMapper;
import com.eveb.saasops.modules.member.entity.MbrAccount;
import com.eveb.saasops.modules.member.entity.MbrDepositCond;
import com.eveb.saasops.modules.member.service.MbrAccountService;
import com.eveb.saasops.modules.system.cmpydeposit.entity.SysDeposit;
import com.eveb.saasops.modules.system.cmpydeposit.service.SysDepositService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import static com.eveb.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.eveb.saasops.common.utils.DateUtil.getCurrentDate;

@Service

public class OfflinePayService {

	@Autowired
	MbrAccountService mbrAccountService;
	@Autowired
	SysDepositService sysDepositService;
    @Autowired
    private FundDepositMapper fundDepositMapper;
    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private FundDepositService fundDepositService;

	public SysDeposit offlinePayVerify(PzpayPayParams pzpayPayParams,MbrDepositCond mbrDepositCond) {
        if (mbrDepositCond == null)
            throw new RRException("会员组无存款条件,系统设置错误!");
        if (mbrDepositCond.getLowQuota().compareTo(pzpayPayParams.getFee()) == 1)
            throw new RRException("充值金额小于最低充值金额!");
        if ((pzpayPayParams.getFee()).compareTo(mbrDepositCond.getTopQuota()) == 1)
            throw new RRException("充值金额大于最高充值金额!");
        SysDeposit sysDeposit = sysDepositService.queryObjectAndGroup(pzpayPayParams.getBankCardId().intValue(),mbrDepositCond.getGroupId());
        if (sysDeposit == null)
            throw new RRException("此银行卡现不接受线下充值!");
        MbrAccount account = mbrAccountService.getAccountInfo(pzpayPayParams.getAccountId());
        pzpayPayParams.setRealName(account.getRealName());
        if (sysDeposit.getMbrGrpId() != account.getGroupId())
            throw new RRException("此银行卡不接该会员充值,逻辑关系错误!");

        BigDecimal dayDepAmt = fundDepositService.totalCompanyDeposit(pzpayPayParams.getBankCardId().intValue());
        if (!StringUtils.isEmpty(dayDepAmt)) {
            if (BigDecimalMath.add(dayDepAmt,pzpayPayParams.getFee()).compareTo(sysDeposit.getDayMaxAmt()) == 1)
                throw new RRException("此银行卡已达到单日充值最大限额,请选择其它银行卡,或下调你的充值金额!");
        }
        return sysDeposit;

    }
	
    @Transactional
    public DepositPostScript saveFundDespoit(PzpayPayParams pzpayPayParams, SysDeposit sysDeposit, MbrDepositCond mbrDepositCond) {
        FundDeposit deposit = new FundDeposit();
        deposit.setOrderNo(pzpayPayParams.getOutTradeNo().toString());
        deposit.setMark(Mark.offlinePay);
        deposit.setStatus(Status.apply);
        deposit.setIsPayment(PaymentStatus.unPay);
        //deposit.setOnlinePayId(pzpayPayParams.getPayType());
        deposit.setCompanyPayId(pzpayPayParams.getBankCardId().intValue());
        deposit.setActivityId(pzpayPayParams.getActivityId());
        deposit.setDepositAmount(pzpayPayParams.getFee());
        BigDecimal feeScale=sysDepositService.getOnlineHandlingCharge(sysDeposit, deposit.getDepositAmount(),mbrDepositCond);
        deposit.setHandlingCharge(feeScale);
        deposit.setActualArrival(sysDeposit.getBankType()==0?deposit.getDepositAmount().add(feeScale):deposit.getDepositAmount());
        deposit.setIp(pzpayPayParams.getIp());
        deposit.setOrderPrefix(OrderConstants.FUND_ORDER_COMPANYDEPOSIT);
        deposit.setDepositUser(pzpayPayParams.getRealName());
        deposit.setCreateUser(pzpayPayParams.getLoginName());
        deposit.setAccountId(pzpayPayParams.getAccountId());
        deposit.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        deposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        deposit.setFundSource(Byte.parseByte(sysDeposit.getDevSource()));
        deposit.setHandingback((sysDeposit.getBankType() == 0 && mbrDepositCond.getFeeEnable() == Constants.Available.disable) ? Constants.Available.disable : Constants.Available.enable);
        fundDepositMapper.insert(deposit);
        fundDepositMapper.updateByPrimaryKeySelective(getPostscript(deposit));
        return fundMapper.findOfflineDepositInfo(deposit.getId());
    }
    
    private FundDeposit getPostscript(FundDeposit deposit)
    {
    	FundDeposit postscript = new FundDeposit();
    	postscript.setDepositPostscript(String.format("%08d", deposit.getId()));
    	postscript.setId(deposit.getId());
    	return postscript;
    }
}
