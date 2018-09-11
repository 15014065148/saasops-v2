package com.eveb.saasops.api.modules.transfer.service;

import com.eveb.saasops.api.modules.transfer.dto.BillManBooleanDto;
import com.eveb.saasops.api.modules.user.service.DepotWalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eveb.saasops.api.constants.ApiConstants.TransferStates;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.transfer.dto.BillRequestDto;
import com.eveb.saasops.api.modules.user.dto.PtUserInfo;
import com.eveb.saasops.api.modules.user.service.MgService;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.modules.member.entity.MbrBillDetail;
import com.eveb.saasops.modules.member.entity.MbrBillManage;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.service.MbrWalletService;

@Service
// @Transactional
public class MgTransferService {

    @Autowired
    private BBINTransferService bbinTransferService;
    @Autowired
    private MgService mgService;
    @Autowired
    MbrWalletService mbrWalletService;
    @Autowired
    private DepotWalletService depotWalletService;

    public BillManBooleanDto mgTransferIn(BillRequestDto requestDto, TGmApi gmApi) {
        BillManBooleanDto billManBooleanDto = new BillManBooleanDto();
        MbrDepotWallet mbrWallet = mgService.login(gmApi, requestDto.getAccountId(), requestDto.getLoginName());
        if (mbrWallet.getIsTransfer() == MbrDepotWallet.IsTransFer.no) {
            mbrWalletService.updateTransfer(mbrWallet.getId());
        }
        MbrBillManage mbrBillManage = bbinTransferService.billChargeIn(requestDto);
        Integer state = mgService.deposit(gmApi, requestDto.getAccountId(), requestDto.getLoginName(), mbrBillManage.getAmount(), mbrBillManage.getOrderNo().toString(), mbrWallet);
        /**查询转账后余额*/
        mbrBillManage.setDepotAfterBalance(depotWalletService.queryDepotBalance(requestDto.getAccountId(), gmApi).getBalance());
        if (state == TransferStates.suc) {
            bbinTransferService.updateMbrBillManageStatus(mbrBillManage, Constants.manageStatus.succeed);
            billManBooleanDto.setMbrBillManage(mbrBillManage);
            billManBooleanDto.setIsTransfer(Boolean.TRUE);
            return billManBooleanDto;
        } else if (state == TransferStates.fail) {
            bbinTransferService.transferBust(mbrBillManage);
        }
        billManBooleanDto.setIsTransfer(Boolean.FALSE);
        return billManBooleanDto;
    }

    public Boolean mgTransferOut(BillRequestDto requestDto, TGmApi gmApi) {
        MbrDepotWallet mbrWallet = mgService.login(gmApi, requestDto.getAccountId(), requestDto.getLoginName());
        PtUserInfo info = mgService.getBalance(gmApi, requestDto.getAccountId(), requestDto.getLoginName(), mbrWallet);
        if (Double.parseDouble(info.getBALANCE()) >= requestDto.getAmount().doubleValue()) {
            MbrBillManage mbrBillManage = bbinTransferService.billChargeOut(requestDto);
            MbrBillDetail mbrBillDetail = bbinTransferService.setMbrBillDetail(mbrBillManage);
            Integer state = mgService.withdraw(gmApi, requestDto.getAccountId(), requestDto.getLoginName(), mbrBillManage.getAmount(), mbrBillManage.getOrderNo().toString(), mbrWallet);
            /**查询转账后余额*/
            mbrBillManage.setDepotAfterBalance(depotWalletService.queryDepotBalance(requestDto.getAccountId(), gmApi).getBalance());
            if (state == TransferStates.suc) {
                mbrBillManage.setBeforeBalance(mbrBillDetail.getBeforeBalance());
                mbrBillManage.setAfterBalance(mbrBillDetail.getAfterBalance());
                bbinTransferService.transferOutSuc(mbrBillDetail, mbrBillManage, Constants.manageStatus.succeed);
                return Boolean.TRUE;
            } else if (state == TransferStates.fail) {
                bbinTransferService.updateMbrBillManageStatus(mbrBillManage, Constants.manageStatus.defeated);
            }
            return Boolean.FALSE;
        } else {
            throw new RRException("余额不足!");
        }
    }


}
