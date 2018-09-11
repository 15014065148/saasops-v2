package com.eveb.saasops.api.modules.transfer.service;

import com.eveb.saasops.api.modules.transfer.dto.BillManBooleanDto;
import com.eveb.saasops.api.modules.user.service.DepotWalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eveb.saasops.api.constants.AginConstants.PPreTrf;
import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.constants.ApiConstants.TransferStates;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.transfer.dto.BillRequestDto;
import com.eveb.saasops.api.modules.user.dto.AGDataDto;
import com.eveb.saasops.api.modules.user.dto.PtUserInfo;
import com.eveb.saasops.api.modules.user.service.AginService;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.modules.member.entity.MbrBillDetail;
import com.eveb.saasops.modules.member.entity.MbrBillManage;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.service.MbrWalletService;

@Service
//@Transactional
public class AginTransferService {

    @Autowired
    private BBINTransferService bbinTransferService;
    @Autowired
    private AginService aginService;
    @Autowired
    MbrWalletService mbrWalletService;
    @Autowired
    private DepotWalletService depotWalletService;

    public BillManBooleanDto aginTransferIn(BillRequestDto requestDto, TGmApi gmApi) {
        BillManBooleanDto billManBooleanDto = new BillManBooleanDto();
        MbrDepotWallet mbrWallet = aginService.checkOrCreateGameAccout(
                gmApi, requestDto.getAccountId(), requestDto.getLoginName());
        if (mbrWallet.getIsTransfer() == MbrDepotWallet.IsTransFer.no)
            mbrWalletService.updateTransfer(mbrWallet.getId());
        MbrBillManage mbrBillManage = bbinTransferService.billChargeIn(requestDto);
        //TODO 查询转账后余额
        mbrBillManage.setDepotAfterBalance(
                depotWalletService.queryDepotBalance(requestDto.getAccountId(), gmApi).getBalance());
        AGDataDto agDataDto = aginService.prepareTransferCredit(mbrWallet, gmApi, ApiConstants.Transfer.in,
                requestDto.getAmount().doubleValue(), String.valueOf(mbrBillManage.getOrderNo()));
        //TODO 预处理
        if (agDataDto.getFlag() == TransferStates.fail) {
            bbinTransferService.transferBust(mbrBillManage);
        } else {
            //TODO 表示预备转账成功  从新设置参数为成功
            agDataDto.setFlag(PPreTrf.suc);
            Integer state = aginService.transferCreditConfirm(gmApi, agDataDto);
            if (state == TransferStates.suc) {
                bbinTransferService.updateMbrBillManageStatus(mbrBillManage, Constants.manageStatus.succeed);
                billManBooleanDto.setMbrBillManage(mbrBillManage);
                billManBooleanDto.setIsTransfer(Boolean.TRUE);
                return billManBooleanDto;
            } else if (state == TransferStates.fail) {
                bbinTransferService.transferBust(mbrBillManage);
            }
        }
        billManBooleanDto.setIsTransfer(Boolean.FALSE);
        return billManBooleanDto;
    }

    public Boolean aginTransferOut(BillRequestDto requestDto, TGmApi gmApi) {
        //TGmApi gmApi = gmApiService.queryApiObject(requestDto.getDepotId(), cpSite.getSitePrefix());
        MbrDepotWallet mbrWallet = aginService.checkOrCreateGameAccout(gmApi, requestDto.getAccountId(), requestDto.getLoginName());
        PtUserInfo info = aginService.getBalance(mbrWallet, gmApi);
        if (Double.parseDouble(info.getBALANCE()) >= requestDto.getAmount().doubleValue()) {
            MbrBillManage mbrBillManage = bbinTransferService.billChargeOut(requestDto);
            MbrBillDetail mbrBillDetail = bbinTransferService.setMbrBillDetail(mbrBillManage);

            AGDataDto agDataDto = aginService.prepareTransferCredit(mbrWallet, gmApi, ApiConstants.Transfer.out, requestDto.getAmount().doubleValue(), String.valueOf(mbrBillManage.getOrderNo()));
            if (agDataDto.getFlag() == TransferStates.fail) {
                bbinTransferService.updateMbrBillManageStatus(mbrBillManage, Constants.manageStatus.defeated);
            } else {
                //表示预备转账成功  从新设置参数为成功
                agDataDto.setFlag(PPreTrf.suc);
                Integer state = aginService.transferCreditConfirm(gmApi, agDataDto);
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
            }
            return Boolean.FALSE;
        } else {
            throw new RRException("余额不足!");
        }
    }
}
