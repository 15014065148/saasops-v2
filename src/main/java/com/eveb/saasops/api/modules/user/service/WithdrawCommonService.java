package com.eveb.saasops.api.modules.user.service;

import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.modules.transfer.dto.BillRequestDto;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.constants.OrderConstants;
import com.eveb.saasops.common.exception.R200Exception;
import com.eveb.saasops.common.utils.SnowFlake;
import com.eveb.saasops.config.MessagesConfig;
import com.eveb.saasops.modules.member.dao.MbrBillDetailMapper;
import com.eveb.saasops.modules.member.dao.MbrBillManageMapper;
import com.eveb.saasops.modules.member.entity.MbrBillDetail;
import com.eveb.saasops.modules.member.entity.MbrBillManage;
import com.eveb.saasops.modules.member.entity.MbrWallet;
import com.eveb.saasops.modules.member.service.MbrBillManageService;
import com.eveb.saasops.modules.member.service.MbrWalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import static com.eveb.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.eveb.saasops.common.utils.DateUtil.FORMAT_25_DATE_TIME;
import static com.eveb.saasops.common.utils.DateUtil.getCurrentDate;

@Service
@Slf4j
public class WithdrawCommonService {

    @Autowired
    private MbrBillManageMapper mbrBillManageMapper;

    @Autowired
    private MbrBillManageService mbrBillManageService;

    @Autowired
    private MbrWalletService mbrWalletService;

    @Autowired
    private MessagesConfig messagesConfig;

    @Autowired
    private MbrBillDetailMapper mbrBillDetailMapper;

    public MbrBillManage billChargeOut(BillRequestDto requestDto) {
        MbrBillManage mbrBillManage = setMbrBillManage(requestDto);
        mbrBillManage.setOpType(Constants.TransferType.into);
        mbrBillManage.setDepotBeforeBalance(requestDto.getDepotBeforeBalance());
        mbrBillManage.setTransferSource(requestDto.getTransferSource());
        mbrBillManageMapper.insert(mbrBillManage);
        return mbrBillManage;
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
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");
            String dateStr = format.format(date); //转为字符串
            String orderNo = "W"+
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
