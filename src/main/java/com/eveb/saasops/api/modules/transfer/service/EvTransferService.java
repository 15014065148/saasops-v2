package com.eveb.saasops.api.modules.transfer.service;

import com.eveb.saasops.api.modules.transfer.dto.BillManBooleanDto;
import com.eveb.saasops.api.modules.user.service.DepotWalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.constants.ApiConstants.TransferStates;
import com.eveb.saasops.api.constants.EvConstants.PPreTrf;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.transfer.dto.BillRequestDto;
import com.eveb.saasops.api.modules.user.dto.EvDataDto;
import com.eveb.saasops.api.modules.user.dto.PtUserInfo;
import com.eveb.saasops.api.modules.user.service.EvService;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.modules.member.entity.MbrBillDetail;
import com.eveb.saasops.modules.member.entity.MbrBillManage;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.service.MbrWalletService;

@Service
//@Transactional
public class EvTransferService {

    @Autowired
    private BBINTransferService bbinTransferService;
    @Autowired
    private EvService evService;
    @Autowired
    MbrWalletService mbrWalletService;
    @Autowired
    private DepotWalletService depotWalletService;

    public BillManBooleanDto evTransferIn(BillRequestDto requestDto, TGmApi gmApi) {
        BillManBooleanDto billManBooleanDto = new BillManBooleanDto();
        MbrDepotWallet mbrWallet = evService.checkOrCreateGameAccout(gmApi, requestDto.getAccountId(), requestDto.getLoginName());
        if (mbrWallet.getIsTransfer() == MbrDepotWallet.IsTransFer.no) {
            mbrWalletService.updateTransfer(mbrWallet.getId());
        }
        MbrBillManage mbrBillManage = bbinTransferService.billChargeIn(requestDto);
        EvDataDto evDataDto = evService.prepareTransferCredit(mbrWallet, gmApi, ApiConstants.Transfer.in, requestDto.getAmount().doubleValue(), String.valueOf(mbrBillManage.getOrderNo()));
        /**查询转账后余额*/
        mbrBillManage.setDepotAfterBalance(depotWalletService.queryDepotBalance(requestDto.getAccountId(), gmApi).getBalance());
        if (evDataDto.getFlag() == TransferStates.fail) {
            bbinTransferService.transferBust(mbrBillManage);
        } else {
            //表示预备转账成功  从新设置参数为成功
            evDataDto.setFlag(PPreTrf.suc);
            Integer state = evService.transferCreditConfirm(gmApi, evDataDto);
            if (state == TransferStates.suc) {
                bbinTransferService.updateMbrBillManageStatus(mbrBillManage, Constants.manageStatus.succeed);
                billManBooleanDto.setMbrBillManage(mbrBillManage);
                billManBooleanDto.setIsTransfer(Boolean.TRUE);
                return billManBooleanDto;
            } else if (state == TransferStates.fail) {
                bbinTransferService.transferBust(mbrBillManage);
            }

            //FIXME 重复调用
        }
        billManBooleanDto.setIsTransfer(Boolean.FALSE);
        return billManBooleanDto;
    }

    public Boolean evTransferOut(BillRequestDto requestDto, TGmApi gmApi) {
        MbrDepotWallet mbrWallet = evService.checkOrCreateGameAccout(gmApi, requestDto.getAccountId(), requestDto.getLoginName());
        PtUserInfo info = evService.getBalance(mbrWallet, gmApi);
        if (Double.parseDouble(info.getBALANCE()) >= requestDto.getAmount().doubleValue()) {
            MbrBillManage mbrBillManage = bbinTransferService.billChargeOut(requestDto);
            MbrBillDetail mbrBillDetail = bbinTransferService.setMbrBillDetail(mbrBillManage);

            EvDataDto evDataDto = evService.prepareTransferCredit(mbrWallet, gmApi, ApiConstants.Transfer.out, requestDto.getAmount().doubleValue(), String.valueOf(mbrBillManage.getOrderNo()));
            /**查询转账后余额*/
            mbrBillManage.setDepotAfterBalance(depotWalletService.queryDepotBalance(requestDto.getAccountId(), gmApi).getBalance());
            if (evDataDto.getFlag() == TransferStates.fail) {
                bbinTransferService.updateMbrBillManageStatus(mbrBillManage, Constants.manageStatus.defeated);
            } else {
                //表示预备转账成功  从新设置参数为成功
                evDataDto.setFlag(PPreTrf.suc);
                Integer state = evService.transferCreditConfirm(gmApi, evDataDto);
                if (state == TransferStates.suc) {
                    mbrBillManage.setBeforeBalance(mbrBillDetail.getBeforeBalance());
                    mbrBillManage.setAfterBalance(mbrBillDetail.getAfterBalance());
                    bbinTransferService.transferOutSuc(mbrBillDetail, mbrBillManage, Constants.manageStatus.succeed);
                    return Boolean.TRUE;
                } else if (state == TransferStates.fail) {
                    bbinTransferService.updateMbrBillManageStatus(mbrBillManage, Constants.manageStatus.defeated);
                }
                //FIXME 重复调用
            }
            return Boolean.FALSE;
        } else {
            throw new RRException("余额不足!");
        }
    }
}
