package com.eveb.saasops.api.modules.transfer.service;

import com.eveb.saasops.api.modules.transfer.dto.BillManBooleanDto;
import com.eveb.saasops.api.modules.user.service.DepotWalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.eveb.saasops.api.constants.ApiConstants.TransferStates;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.transfer.dto.BillRequestDto;
import com.eveb.saasops.api.modules.user.dto.PtUserInfo;
import com.eveb.saasops.api.modules.user.service.PtNewService;
import com.eveb.saasops.api.modules.user.service.PtService;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.modules.member.entity.MbrBillDetail;
import com.eveb.saasops.modules.member.entity.MbrBillManage;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.service.MbrWalletService;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PtTransferService {
    @Autowired
    private BBINTransferService bbinTransferService;
    @Autowired
    private PtService ptService;
    @Autowired
    MbrWalletService mbrWalletService;
    @Autowired
    PtNewService ptNewService;
    @Autowired
    private DepotWalletService depotWalletService;

    public BillManBooleanDto ptTransferIn(BillRequestDto requestDto, TGmApi gmApi) {
        BillManBooleanDto billManBooleanDto = new BillManBooleanDto();
        MbrDepotWallet mbrWallet = ptService.createMember(gmApi, requestDto.getAccountId(), requestDto.getLoginName());
        if (mbrWallet.getIsTransfer() == MbrDepotWallet.IsTransFer.no) {
            mbrWalletService.updateTransfer(mbrWallet.getId());
        }
        if (isAllStatus(requestDto, gmApi, billManBooleanDto)) return billManBooleanDto;
        return billManBooleanDto;
    }

    private boolean isAllStatus(BillRequestDto requestDto, TGmApi gmApi, BillManBooleanDto billManBooleanDto) {
        MbrBillManage mbrBillManage = bbinTransferService.billChargeIn(requestDto);
        Integer state = ptService.deposit(gmApi, requestDto.getLoginName(), mbrBillManage.getAmount().doubleValue(), mbrBillManage.getOrderNo().toString());
        //TODO 查询转账后余额
        mbrBillManage.setDepotAfterBalance(depotWalletService.queryDepotBalance(requestDto.getAccountId(), gmApi).getBalance());
        if (state == TransferStates.suc) {
            bbinTransferService.updateMbrBillManageStatus(mbrBillManage, Constants.manageStatus.succeed);
            billManBooleanDto.setMbrBillManage(mbrBillManage);
            billManBooleanDto.setIsTransfer(Boolean.TRUE);
            return true;
        } else if (state == TransferStates.fail) {
            bbinTransferService.transferBust(mbrBillManage);
        }
        billManBooleanDto.setIsTransfer(Boolean.FALSE);
        return false;
    }

    public Boolean ptTransferOut(BillRequestDto requestDto, TGmApi gmApi) {
        PtUserInfo info = ptService.getBalance(gmApi, requestDto.getLoginName());
        if (Double.parseDouble(info.getBALANCE()) >= requestDto.getAmount().doubleValue()) {
            MbrBillManage mbrBillManage = bbinTransferService.billChargeOut(requestDto);
            MbrBillDetail mbrBillDetail = bbinTransferService.setMbrBillDetail(mbrBillManage);
            Integer state = ptService.withdraw(gmApi, requestDto.getLoginName(), mbrBillManage.getAmount().doubleValue(), mbrBillManage.getOrderNo().toString());
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

    public BillManBooleanDto ptNewTransferIn(BillRequestDto requestDto, TGmApi gmApi) {
        BillManBooleanDto billManBooleanDto = new BillManBooleanDto();
        MbrBillManage mbrBillManage = bbinTransferService.billChargeIn(requestDto);
        Integer state = ptNewService.deposit(gmApi, requestDto.getAccountId(), requestDto.getLoginName(), requestDto.getAmount().doubleValue(), String.valueOf(mbrBillManage.getOrderNo()));
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

    public Boolean ptNewTransferOut(BillRequestDto requestDto, TGmApi gmApi) {
        String token = ptNewService.loginAgent(gmApi);
        PtUserInfo info = ptNewService.balance(gmApi, requestDto.getAccountId(), requestDto.getLoginName(), token);
        if (Double.parseDouble(info.getBALANCE()) >= requestDto.getAmount().doubleValue()) {
            MbrBillManage mbrBillManage = bbinTransferService.billChargeOut(requestDto);
            MbrBillDetail mbrBillDetail = bbinTransferService.setMbrBillDetail(mbrBillManage);
            Integer state = ptNewService.withdraw(gmApi, requestDto.getAccountId(), requestDto.getLoginName(), requestDto.getAmount().doubleValue(), String.valueOf(mbrBillManage.getOrderNo()), token);
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
