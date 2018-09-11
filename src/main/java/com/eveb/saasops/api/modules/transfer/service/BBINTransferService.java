package com.eveb.saasops.api.modules.transfer.service;

import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.constants.BbinConstants;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.transfer.dto.*;
import com.eveb.saasops.api.modules.transfer.mapper.AccountDepotMapper;
import com.eveb.saasops.api.modules.user.service.BbinService;
import com.eveb.saasops.api.modules.user.service.DepotWalletService;
import com.eveb.saasops.api.modules.user.service.OkHttpService;
import com.eveb.saasops.api.utils.MD5;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.constants.OrderConstants;
import com.eveb.saasops.common.exception.R200Exception;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.Collections3;
import com.eveb.saasops.common.utils.DateUtil;
import com.eveb.saasops.common.utils.SnowFlake;
import com.eveb.saasops.common.utils.StringUtil;
import com.eveb.saasops.config.MessagesConfig;
import com.eveb.saasops.modules.fund.dao.FundAccLogMapper;
import com.eveb.saasops.modules.fund.entity.FundAccLog;
import com.eveb.saasops.modules.member.dao.MbrBillDetailMapper;
import com.eveb.saasops.modules.member.dao.MbrBillManageMapper;
import com.eveb.saasops.modules.member.entity.MbrBillDetail;
import com.eveb.saasops.modules.member.entity.MbrBillManage;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.entity.MbrWallet;
import com.eveb.saasops.modules.member.service.MbrBillManageService;
import com.eveb.saasops.modules.member.service.MbrWalletService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

import static com.eveb.saasops.api.constants.ApiConstants.*;
import static com.eveb.saasops.common.utils.DateUtil.*;

@Service
@Transactional
@Slf4j
public class BBINTransferService {

    @Autowired
    private AccountDepotMapper accountPayMapper;
    @Autowired
    private MessagesConfig messagesConfig;
    @Autowired
    private MbrWalletService mbrWalletService;
    @Autowired
    private MbrBillManageMapper mbrBillManageMapper;
    @Autowired
    private MbrBillDetailMapper mbrBillDetailMapper;
    @Autowired
    private FundAccLogMapper accLogMapper;
    @Autowired
    private MbrBillManageService mbrBillManageService;
    @Autowired
    private OkHttpService okHttpService;
    @Autowired
    private BbinService bbinService;
    @Autowired
    private DepotWalletService depotWalletService;

    public MbrBillManage billChargeIn(BillRequestDto requestDto) {
        int isBalance = accountPayMapper.findAccountBalance(
                requestDto.getAccountId(), requestDto.getAmount(), Constants.SYS_DEPOT_ID, requestDto.getBonusAmount());
        if (isBalance == 0) throw new R200Exception(messagesConfig.getValue("wallet.account.balance"));
        MbrBillManage mbrBillManage = setMbrBillManage(requestDto);
        mbrBillManage.setOpType(Constants.TransferType.out);
        mbrBillManage.setDepotBeforeBalance(requestDto.getDepotBeforeBalance());
        mbrBillManage.setTransferSource(requestDto.getTransferSource());
        MbrWallet wallet = new MbrWallet();
        wallet.setAccountId(requestDto.getAccountId());
        wallet.setBalance(requestDto.getAmount());
        wallet.setBonusAmount(requestDto.getBonusAmount());
        Boolean isWallet = mbrWalletService.walletArriveDepot(wallet, mbrBillManage);
        if (isWallet == Boolean.FALSE) throw new RRException(messagesConfig.getValue("wallet.account.amount"));
        mbrBillManageMapper.insert(mbrBillManage);
        MbrBillDetail mbrBillDetail = setMbrBillDetail(
                mbrBillManage, MbrBillDetail.OpTypeStatus.expenditure, Boolean.FALSE);
        mbrBillDetailMapper.insert(mbrBillDetail);
        return mbrBillManage;
    }

    public MbrDepotWallet checkReg(TGmApi gmApi, Integer accountId, String loginName) {
        return bbinService.createMember(gmApi, accountId, loginName);
    }

    public BillManBooleanDto sendTransferIn(MbrBillManage manage, TGmApi gmApi) {
        BillManBooleanDto billManBooleanDto = new BillManBooleanDto();
        TransferRequestDto requestDto = setTransferRequestDto(manage.getLoginName(), Long.parseLong(manage.getOrderNo()),
                ApiConstants.Transfer.in, manage.getAmount(), gmApi);
        String url = gmApi.getPcUrl() + BbinConstants.BBIN_API_TRANSFER;
        String result = okHttpService.postJson(url, requestDto);
        log.debug(" BWIN—>转入 [提交参数 " + new Gson().toJson(requestDto) + " 返回结果 " + result + "]");
        if (StringUtil.isNotEmpty(result)) {
            BbinResponseDto responseDto = new Gson().fromJson(result, BbinResponseDto.class);
            if (responseDto.getResult() && BBIN_TRANSFER_SUCCEED.equals(responseDto.getData().getCode())) {
                updateMbrBillManageStatus(manage, Constants.manageStatus.succeed);
                billManBooleanDto.setMbrBillManage(manage);
                billManBooleanDto.setIsTransfer(Boolean.TRUE);
                return billManBooleanDto;
            } else if (responseDto.getResult() == Boolean.FALSE && (BBIN_TRANSFER_FAIL.equals(responseDto.getData().getCode())
                    || BBIN_SYS_MAINTENANCE.equals(responseDto.getData().getCode())
                    || BBIN_GM_MAINTENANCE.equals(responseDto.getData().getCode())
                    || BBIN_KEY_ERROR.equals(responseDto.getData().getCode())
            )) {
                transferBust(manage);
            }
        }
        billManBooleanDto.setIsTransfer(Boolean.FALSE);
        return billManBooleanDto;
    }


    private TransferRequestDto setTransferRequestDto(String loginName, Long orderNo, String action, BigDecimal amount,
                                                     TGmApi gmApi) {
        TransferRequestDto requestDto = new TransferRequestDto();
        requestDto.setWebsite(gmApi.getWebName());
        requestDto.setUppername(gmApi.getAgyAcc());
        requestDto.setUsername(gmApi.getPrefix() + loginName);
        requestDto.setRemitno(orderNo);
        requestDto.setAction(action);
        requestDto.setRemit(amount.intValue());
        String[] keys = gmApi.getSecureCodes().get(BbinConstants.BBIN_API_TRANSFER).split(",");
        String ps = keys[0] + MD5.getMD5(requestDto.getWebsite() + requestDto.getUsername()
                + requestDto.getRemitno() + keys[1]
                + DateUtil.getAmericaDate(FORMAT_8_DATE, new Date())) + keys[2];
        requestDto.setKey(ps);
        return requestDto;
    }

    public void transferBust(MbrBillManage manage) {
        //TODO 判断修改转帐预处理表(中间表)是否成功
        if (updateMbrBillManageStatus(manage, Constants.manageStatus.defeated) > 0) {
            MbrBillDetail mbrBillDetail = setMbrBillDetail(manage, MbrBillDetail.OpTypeStatus.income, Boolean.TRUE);
            walletIncome(mbrBillDetail);
        }
    }

    /**
     * @param mbrBillDetail
     * @param mbrBillManage 这个参数目前只使用到了 id
     * @param status
     */
    public void transferOutSuc(MbrBillDetail mbrBillDetail, MbrBillManage mbrBillManage, Integer status) {
        if (updateMbrBillManageStatus(mbrBillManage, status) > 0) {
            if (Constants.manageStatus.succeed == status) {
                walletIncome(mbrBillDetail);
                MbrBillManage billManage = new MbrBillManage();
                billManage.setId(mbrBillManage.getId());
                billManage.setBeforeBalance(mbrBillDetail.getBeforeBalance());
                billManage.setAfterBalance(mbrBillDetail.getAfterBalance());
                mbrBillManageMapper.updateByPrimaryKeySelective(billManage);
            }
        }
    }


    public int updateMbrBillManageStatus(MbrBillManage manage, Integer status) {
        MbrBillManage billManage = new MbrBillManage();
        billManage.setId(manage.getId());
        billManage.setStatus(status);
        billManage.setDepotAfterBalance(manage.getDepotAfterBalance());
        return mbrBillManageService.updateStatus(billManage);
    }

    public MbrBillManage setMbrBillManage(BillRequestDto requestDto) {
        MbrBillManage mbrBillManage = new MbrBillManage();
        mbrBillManage.setAccountId(requestDto.getAccountId());
        mbrBillManage.setDepotId(requestDto.getDepotId());
        mbrBillManage.setLoginName(requestDto.getLoginName());
        mbrBillManage.setAmount(requestDto.getAmount());
        mbrBillManage.setOrderNo(new SnowFlake().nextId()+"");
        mbrBillManage.setStatus(Constants.manageStatus.freeze);
        mbrBillManage.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        mbrBillManage.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        mbrBillManage.setModifyUser(requestDto.getLoginName());
        mbrBillManage.setLogId(requestDto.getId());
        return mbrBillManage;
    }

    private MbrBillDetail setMbrBillDetail(MbrBillManage manage, Byte opType, Boolean isMemo) {
        MbrBillDetail mbrBillDetail = new MbrBillDetail();
        mbrBillDetail.setOrderNo(manage.getOrderNo());
        mbrBillDetail.setLoginName(manage.getLoginName());
        mbrBillDetail.setAccountId(manage.getAccountId());
        mbrBillDetail.setAmount(manage.getAmount());
        mbrBillDetail.setAfterBalance(manage.getAfterBalance());
        mbrBillDetail.setBeforeBalance(manage.getBeforeBalance());
        mbrBillDetail.setBeforeBalance(manage.getBeforeBalance());
        mbrBillDetail.setOpType(new Byte(manage.getOpType().toString()));
        mbrBillDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
        mbrBillDetail.setDepotId(manage.getDepotId());
        mbrBillDetail.setFinancialCode(opType == 1 ? OrderConstants.FUND_ORDER_TRIN : OrderConstants.FUND_ORDER_TROUT);
        mbrBillDetail.setOpType(opType);
        mbrBillDetail.setOrderPrefix(opType == 1 ? OrderConstants.FUND_ORDER_TRIN : OrderConstants.FUND_ORDER_TROUT);
        if (isMemo) {
            mbrBillDetail.setMemo(manage.getOrderNo().toString());
            mbrBillDetail.setOrderNo(new SnowFlake().nextId()+"");
        }
        return mbrBillDetail;
    }

    public MbrBillManage bbINBillChargeOut(BillRequestDto requestDto, TGmApi gmApi) {

        BbinUsrBalanceResponseDto responseDto = checkUsrBalance(requestDto.getLoginName(), gmApi);
        checkoutUsrBalance(responseDto, requestDto.getAmount());
        return billChargeOut(requestDto);
    }

    public MbrBillManage billChargeOut(BillRequestDto requestDto) {
        MbrBillManage mbrBillManage = setMbrBillManage(requestDto);
        mbrBillManage.setOpType(Constants.TransferType.into);
        mbrBillManage.setDepotBeforeBalance(requestDto.getDepotBeforeBalance());
        mbrBillManage.setTransferSource(requestDto.getTransferSource());
        mbrBillManageMapper.insert(mbrBillManage);
        return mbrBillManage;
    }

    public Boolean sendTransferOut(MbrBillManage mbrBillManage, TGmApi gmApi) {
        MbrBillDetail mbrBillDetail = setMbrBillDetail(mbrBillManage);
        TransferRequestDto requestDto = setTransferRequestDto(mbrBillDetail.getLoginName(), Long.parseLong(mbrBillDetail.getOrderNo()),
                ApiConstants.Transfer.out, mbrBillDetail.getAmount(), gmApi);
        // FIXME 这里改成同步的
        String url = gmApi.getPcUrl() + BbinConstants.BBIN_API_TRANSFER;
        String result = okHttpService.postJson(url, requestDto);
        log.debug(" BWIN—>转出 [提交参数 " + new Gson().toJson(requestDto) + " 返回结果 " + result + "]");
        /**查询转账后余额*/
        mbrBillManage.setDepotAfterBalance(depotWalletService.queryDepotBalance(mbrBillManage.getAccountId(), gmApi).getBalance());
        Integer status = Constants.manageStatus.defeated;
        if (StringUtil.isNotEmpty(result)) {
            BbinResponseDto responseDto = new Gson().fromJson(result, BbinResponseDto.class);
            if (responseDto.getResult() && BBIN_TRANSFER_SUCCEED.equals(responseDto.getData().getCode())) {
                status = Constants.manageStatus.succeed;
            } else {
                throw new RRException("转出失败!");
            }
        }
        transferOutSuc(mbrBillDetail, mbrBillManage, status);
        if (status == Constants.manageStatus.succeed) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }

    }

    private void checkoutUsrBalance(BbinUsrBalanceResponseDto responseDto, BigDecimal decimal) {
        if (Objects.isNull(responseDto)) {
            throw new R200Exception(messagesConfig.getValue("system.exception"));
        }
        Gson gson = new Gson();
        if (responseDto.getResult() == Boolean.FALSE) {
            BbinDataDto dto = gson.fromJson(gson.toJson(responseDto.getData()), BbinDataDto.class);
            throw new RRException(messagesConfig.getValue(dto.getMessage()));
        }
        if (responseDto.getResult()) {
            if (Collections3.isNotEmpty(responseDto.getData()) && decimal.compareTo(responseDto.getData().get(0).getTotalBalance()) == 1) {
                throw new RRException(messagesConfig.getValue("wallet.account.balance"));
            }
        }
    }

    private void walletIncome(MbrBillDetail mbrBillDetail) {
        MbrWallet mbrWallet = new MbrWallet();
        mbrWallet.setBalance(mbrBillDetail.getAmount());
        mbrWallet.setAccountId(mbrBillDetail.getAccountId());
        //TODO 平台转钱包
        Boolean isSucceed = mbrWalletService.depotArriveWallet(mbrWallet, mbrBillDetail);
        if (isSucceed == false) {
            throw new R200Exception(messagesConfig.getValue("saasops.illegal.request"));
        }
        //TODO 添加流水详情
        mbrBillDetailMapper.insert(mbrBillDetail);
    }

    public MbrBillDetail setMbrBillDetail(MbrBillManage mbrBillManage) {
        MbrBillDetail mbrBillDetail = new MbrBillDetail();
        mbrBillDetail.setAmount(mbrBillManage.getAmount());
        mbrBillDetail.setOpType(MbrBillDetail.OpTypeStatus.income);
        mbrBillDetail.setLoginName(mbrBillManage.getLoginName());
        mbrBillDetail.setFinancialCode(OrderConstants.FUND_ORDER_TRIN);
        mbrBillDetail.setAccountId(mbrBillManage.getAccountId());
        mbrBillDetail.setDepotId(mbrBillManage.getDepotId());
        mbrBillDetail.setOrderPrefix(OrderConstants.FUND_ORDER_TRIN);
        mbrBillDetail.setOrderNo(mbrBillManage.getOrderNo());
        mbrBillDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
        return mbrBillDetail;
    }

    public FundAccLog getBillDepotLog(BillRequestDto billReport, Integer type) {
        FundAccLog accLog = new FundAccLog();
        accLog.setAccountId(billReport.getAccountId());
        accLog.setContent(new Gson().toJson(billReport));
        accLog.setCreateUser(billReport.getLoginName());
        accLog.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        accLog.setIp(billReport.getIp());
        accLog.setType(type);
        return accLog;
    }

    @Transactional
    public void saveBillDepotLog(BillRequestDto billReport, Integer type) {
        FundAccLog accLog = new FundAccLog();
        accLog.setAccountId(billReport.getAccountId());
        accLog.setContent(new Gson().toJson(billReport));
        accLog.setCreateUser(billReport.getLoginName());
        accLog.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        accLog.setIp(billReport.getIp());
        accLog.setType(type);
        accLogMapper.insert(accLog);
        billReport.setId(accLog.getId());
    }

    /* 回传结果-1中有可能是处理中。处理中这种状态怎么获取，等到大面积使用后反回结果再订，待订
     * {"result":"回傳結果(true or false)","data":{"TransID":"轉帳序號","TransType":"轉帳型
     * 態(入/出)","Status":"狀態(1:成功；-1:處理中或失敗 )"}}
     */
    public BbinResponseDto checkTransfer(Long transid, TGmApi gmApi) {
        String[] keys = gmApi.getSecureCodes().get(BbinConstants.BBIN_API_CHECKTRANSFER).split(",");
        TransferRequestDto requestDto = new TransferRequestDto();
        requestDto.setWebsite(gmApi.getWebName());
        requestDto.setTransid(transid);// 131378094694215680 入 131384329615192064 出
        String ps = keys[0]
                + MD5.getMD5(requestDto.getWebsite() + keys[1]
                + DateUtil.getAmericaDate(FORMAT_8_DATE, new Date()))
                + keys[2];
        requestDto.setKey(ps);
        String url = gmApi.getPcUrl() + BbinConstants.BBIN_API_CHECKTRANSFER;
        String result = okHttpService.postJson(url, requestDto);
        log.debug(" 播音查账—>查账 [提交参数 " + new Gson().toJson(requestDto) + " 返回结果 " + result + "]");
        BbinResponseDto responseDto = null;
        if (StringUtil.isNotEmpty(result)) {
            responseDto = new Gson().fromJson(result, BbinResponseDto.class);
        }
        return responseDto;
    }

    public BbinUsrBalanceResponseDto checkUsrBalance(String loginName, TGmApi gmApi) {
        String[] keys = gmApi.getSecureCodes().get(BbinConstants.BBIN_API_CHECKUSRBALANCE).split(",");
        TransferRequestDto requestDto = new TransferRequestDto();
        requestDto.setWebsite(gmApi.getWebName());
        requestDto.setUsername(gmApi.getPrefix() + loginName);
        requestDto.setUppername(gmApi.getAgyAcc());
        String ps = keys[0] + MD5.getMD5(requestDto.getWebsite() + requestDto.getUsername()
                + keys[1]
                + DateUtil.getAmericaDate(FORMAT_8_DATE, new Date())) + keys[2];
        requestDto.setKey(ps);
        String url = gmApi.getPcUrl() + BbinConstants.BBIN_API_CHECKUSRBALANCE;
        String result = okHttpService.postJson(url, requestDto);
        log.debug(" 播音查余额—>查余额 [提交参数 " + new Gson().toJson(requestDto) + " 返回结果 " + result + "]");
        if (StringUtil.isNotEmpty(result)) {
            return new Gson().fromJson(result, BbinUsrBalanceResponseDto.class);
        }
        return null;
    }
    /*
     * public String transferRecord(TransferRequestDto requestDto) {
     * requestDto.setUsername("testLgnacio"); requestDto.setPage(1);
     * requestDto.setPagelimit(10); requestDto.setDate_start("2016-11-24");
     * requestDto.setDate_end("2017-11-24");
     * requestDto.setWebsite(apiConfig.getBbinWebsite());
     * requestDto.setUppername(apiConfig.getBbinUppername()); String ps =
     * genRandomNum(3) + MD5.getMD5(requestDto.getWebsite() +
     * requestDto.getUsername() + apiConfig.getBbinTransferRecordKey() +
     * DateUtil.getAmericaDate(FORMAT_8_DATE, new Date())) + genRandomNum(4);
     * requestDto.setKey(ps); String url = apiConfig.getBbinUrl() + transferRecord;
     * String result = OkHttpUtils.postJson(url, requestDto); if
     * (StringUtil.isNotEmpty(result)) { System.out.println(result); return result;
     * } return null; }
     */

}
