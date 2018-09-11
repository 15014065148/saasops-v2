package com.eveb.saasops.api.modules.user.service;

import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.modules.transfer.dto.BillRequestDto;
import com.eveb.saasops.api.modules.transfer.mapper.AccountDepotMapper;
import com.eveb.saasops.api.modules.unity.service.GameDepotService;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.constants.OrderConstants;
import com.eveb.saasops.common.exception.R200Exception;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.SnowFlake;
import com.eveb.saasops.config.MessagesConfig;
import com.eveb.saasops.modules.member.dao.MbrBillDetailMapper;
import com.eveb.saasops.modules.member.dao.MbrBillManageMapper;
import com.eveb.saasops.modules.member.entity.MbrBillDetail;
import com.eveb.saasops.modules.member.entity.MbrBillManage;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.entity.MbrWallet;
import com.eveb.saasops.modules.member.service.MbrBillManageService;
import com.eveb.saasops.modules.member.service.MbrWalletService;
import com.eveb.saasops.modules.operate.service.TGmDepotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import static com.eveb.saasops.common.utils.DateUtil.*;

@Service
@Slf4j
public class DepositCommonService {

    @Autowired
    private GameDepotService gameDepotService;

    @Autowired
    private DepotWalletService depotWalletService;

    @Autowired
    private MbrWalletService mbrWalletService;

    @Autowired
    private TGmDepotService tGmDepotService;

    @Autowired
    private AccountDepotMapper accountPayMapper;

    @Autowired
    private MessagesConfig messagesConfig;

    @Autowired
    private MbrBillManageMapper mbrBillManageMapper;

    @Autowired
    private MbrBillDetailMapper mbrBillDetailMapper;

    @Autowired
    private MbrBillManageService mbrBillManageService;

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


    public MbrDepotWallet getDepotWallet(Integer accountId, Integer depotId, String siteCode, String loginName) {
        MbrDepotWallet wallet = new MbrDepotWallet();
        wallet.setAccountId(accountId);
        wallet.setDepotId(depotId);
        wallet = mbrWalletService.queryObjectCond(wallet, siteCode);
        if (wallet == null) {
            wallet = new MbrDepotWallet();
            wallet.setLoginName(loginName);
            wallet.setAccountId(accountId);
            wallet.setPwd(CommonUtil.genRandomNum(6, 8));
            wallet.setDepotId(depotId);
            wallet.setIsBuild(Boolean.FALSE);
        } else {
            wallet.setIsBuild(Boolean.TRUE);
        }
        return wallet;
    }

    //生成随机数字和字母,
    public String getStringRandom(int length) {

        String val = "";
        Random random = new Random();

        //参数length，表示生成几位随机数
        for(int i = 0; i < length; i++) {

            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            //输出字母还是数字
            if( "char".equalsIgnoreCase(charOrNum) ) {
                //输出是大写字母还是小写字母
                int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
                val += (char)(random.nextInt(26) + temp);
            } else if( "num".equalsIgnoreCase(charOrNum) ) {
                val += String.valueOf(random.nextInt(10));
            }
        }
        return val;
    }

    public MbrBillManage setMbrBillManage(BillRequestDto requestDto) {
        MbrBillManage mbrBillManage = new MbrBillManage();
        mbrBillManage.setAccountId(requestDto.getAccountId());
        mbrBillManage.setDepotId(requestDto.getDepotId());
        mbrBillManage.setLoginName(requestDto.getLoginName());
        mbrBillManage.setAmount(requestDto.getAmount());
        if (ApiConstants.DepotId.GD == requestDto.getDepotId()){
            //D180816134512Dn2D6  年月日+时分秒+5随机字符
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");
            String dateStr = format.format(date); //转为字符串
            String orderNo = "D"+
                    dateStr +
                    getStringRandom(5);
            requestDto.setOrderNo(orderNo);
            mbrBillManage.setOrderNo(orderNo);
        } else {
            requestDto.setOrderNo(new SnowFlake().nextId()+"");
            mbrBillManage.setOrderNo(requestDto.getOrderNo());
        }
        mbrBillManage.setStatus(Constants.manageStatus.freeze);
        mbrBillManage.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        mbrBillManage.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        mbrBillManage.setModifyUser(requestDto.getLoginName());
        mbrBillManage.setLogId(requestDto.getId());
        return mbrBillManage;
    }

    public int updateMbrBillManageStatus(MbrBillManage manage, Integer status) {
        MbrBillManage billManage = new MbrBillManage();
        billManage.setId(manage.getId());
        billManage.setStatus(status);
        billManage.setDepotAfterBalance(manage.getDepotAfterBalance());
        return mbrBillManageService.updateStatus(billManage);
    }


    public void transferBust(MbrBillManage manage) {
        //TODO 判断修改转帐预处理表(中间表)是否成功
        if (updateMbrBillManageStatus(manage, Constants.manageStatus.defeated) > 0) {
            MbrBillDetail mbrBillDetail = setMbrBillDetail(manage, MbrBillDetail.OpTypeStatus.income, Boolean.TRUE);
            walletIncome(mbrBillDetail);
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

}
