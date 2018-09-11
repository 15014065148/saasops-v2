package com.eveb.saasops.api.modules.transfer.service;

import com.eveb.saasops.api.constants.ApiConstants.TransferStates;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.transfer.dto.BillManBooleanDto;
import com.eveb.saasops.api.modules.transfer.dto.BillRequestDto;
import com.eveb.saasops.api.modules.user.dto.PtUserInfo;
import com.eveb.saasops.api.modules.user.service.*;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.modules.member.entity.MbrBillDetail;
import com.eveb.saasops.modules.member.entity.MbrBillManage;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.service.MbrWalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
// @Transactional
public class OpusTransferService {
    @Autowired
    private BBINTransferService bbinTransferService;
    @Autowired
    private OpusSbService opusSbService;
    @Autowired
    private MbrWalletService mbrWalletService;
    @Autowired
    private OpusLiveService opusLiveService;
    @Autowired
    private DepotWalletService depotWalletService;

    public BillManBooleanDto opusSbTransferIn(BillRequestDto requestDto, TGmApi gmApi) {
        BillManBooleanDto billManBooleanDto = new BillManBooleanDto();
        MbrDepotWallet mbrWallet = opusSbService.createMember(gmApi, requestDto.getAccountId(), requestDto.getLoginName());
        if (mbrWallet.getIsTransfer() == MbrDepotWallet.IsTransFer.no) {
            mbrWalletService.updateTransfer(mbrWallet.getId());
        }
        MbrBillManage mbrBillManage = bbinTransferService.billChargeIn(requestDto);
        Integer state = opusSbService.deposit(gmApi, requestDto.getLoginName(), mbrBillManage.getAmount(), mbrBillManage.getOrderNo().toString());
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

    public Boolean opusSbTransferOut(BillRequestDto requestDto, TGmApi gmApi) {
        PtUserInfo info = opusSbService.getBalance(gmApi, requestDto.getLoginName());
        if (Double.parseDouble(info.getBALANCE()) >= requestDto.getAmount().doubleValue()) {
            MbrBillManage mbrBillManage = bbinTransferService.billChargeOut(requestDto);
            MbrBillDetail mbrBillDetail = bbinTransferService.setMbrBillDetail(mbrBillManage);

            Integer state = opusSbService.withdraw(gmApi, requestDto.getLoginName(), mbrBillManage.getAmount(), mbrBillManage.getOrderNo().toString());
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

    public BillManBooleanDto opusLiveTransferIn(BillRequestDto requestDto, TGmApi gmApi) {
        BillManBooleanDto billManBooleanDto = new BillManBooleanDto();
        MbrDepotWallet mbrWallet = opusLiveService.createMember(gmApi, requestDto.getAccountId(), requestDto.getLoginName());
        if (mbrWallet.getIsTransfer() == MbrDepotWallet.IsTransFer.no) {
            mbrWalletService.updateTransfer(mbrWallet.getId());
        }
        MbrBillManage mbrBillManage = bbinTransferService.billChargeIn(requestDto);
        Integer state = opusLiveService.deposit(gmApi, requestDto.getLoginName(), mbrBillManage.getAmount(), mbrBillManage.getOrderNo().toString());
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

    public Boolean opusLiveTransferOut(BillRequestDto requestDto, TGmApi gmApi) {
        PtUserInfo info = opusLiveService.getBalance(gmApi, requestDto.getLoginName());
        if (Double.parseDouble(info.getBALANCE()) >= requestDto.getAmount().doubleValue()) {
            MbrBillManage mbrBillManage = bbinTransferService.billChargeOut(requestDto);
            MbrBillDetail mbrBillDetail = bbinTransferService.setMbrBillDetail(mbrBillManage);

            Integer state = opusLiveService.withdraw(gmApi, requestDto.getLoginName(), mbrBillManage.getAmount(), mbrBillManage.getOrderNo().toString());
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
