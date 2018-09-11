package com.eveb.saasops.modules.fund.mapper;


import com.eveb.saasops.api.modules.pay.pzpay.dto.DepositListDto;
import com.eveb.saasops.modules.fund.entity.*;
import com.eveb.saasops.modules.member.dto.BillRecordDto;
import com.eveb.saasops.modules.member.entity.MbrBillManage;
import com.eveb.saasops.modules.member.entity.MbrWallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@Mapper
public interface FundMapper {

    Map<String,Object> querySumFeeFreeTimes(@Param("accountId") Integer accountId ,@Param("createTimeFrom") String createTimeFrom ,@Param("createTimeTo") String createTimeTo);

    List<FundDeposit> findDepositList(FundDeposit fundDeposit);

    List<FundDeposit> findDepositListApiOfOnline(DepositListDto fundDeposit);

    List<FundDeposit> findDepositListApiCompany(DepositListDto fundDeposit);

    List<FundDeposit> findDepositListApiOther(DepositListDto fundDeposit);
    
    List<FundDeposit> findDepositAndOtherList(DepositListDto fundDeposit);

    Double findDepositSum(DepositListDto fundDeposit);
    
    Double findDepositSumOther(DepositListDto fundDeposit);
    
    Double findDepositSumAndAudit(DepositListDto fundDeposit);

    Double findSumDepositAmount(FundDeposit fundDeposit);

    int findDepositCount(FundDeposit fundDeposit);

    List<AccWithdraw> findAccWithdrawList(AccWithdraw accWithdraw);

    List<AccWithdraw> findFixateAccWithdraw(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("accountId") Integer accountId);
    
    double totalFixateAccWithdraw(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("accountId") Integer accountId);

    int findAccWithdrawCount(AccWithdraw accWithdraw);

    Double accSumDrawingAmount(AccWithdraw accWithdraw);

    List<AgyWithdraw> findAgyWithdrawList(AgyWithdraw agyWithdraw);

    List<MbrBillManage> findMbrBillManageList(MbrBillManage fundBillReport);

    List<FundAudit> findFundAuditList(FundAudit fundAudit);

    FundDeposit findFundDepositOne(FundDeposit fundDeposit);

    int updatePayStatus(FundDeposit deposit);

    List<FundDeposit> selectForUpdate(FundDeposit deposit);
    
    AccWithdraw sumWithDraw(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("accountId") Integer accountId);
    
    Byte isFreeFee( @Param("feeTimes") Integer feeTimes, @Param("hours") Integer hours, @Param("accountId") Integer accountId);
    
    int sumApplyRec(@Param("accountId") Integer accountId);

    List<FundDeposit> findDepositActivity(FundDeposit deposit);

    int findDepositActivityCount(FundDeposit deposit);

    BigDecimal sumCompanyDeposit(@Param("companyId") Integer companyId);

    DepositPostScript findOfflineDepositInfo(@Param("id") Integer id);

    List<FundMerchantPay> findFundMerchantPayList(FundMerchantPay fundMerchantPay);

    int findMerchantPayCount(@Param("accountId") Integer accountId);

    List<AccWithdraw> fundAccWithdrawMerchant(@Param("accountId") Integer accountId);

    List<QuickFunction> listCount();

    AccWithdraw updateMerchantPayLock(@Param("id") Integer id);

    FundDeposit updateFundDepositLock(FundDeposit fundDeposit);

    int updateMerchantPayAvailable();

	int updateFundDepositMemoByOrderNo(@Param("outTradeNo") String outTradeNo, @Param("message") Object message);

    MbrWallet findAccountBalance(@Param("loginName") String loginName);

    List<BillRecordDto> findBillRecordList(BillRecordDto billRecordDto);
}
