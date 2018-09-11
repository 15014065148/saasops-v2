package com.eveb.saasops.api.modules.transfer.service;

import com.eveb.saasops.api.modules.transfer.dto.BillManBooleanDto;
import com.eveb.saasops.api.modules.user.service.DepotWalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eveb.saasops.api.constants.ApiConstants.TransferStates;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.transfer.dto.BillRequestDto;
import com.eveb.saasops.api.modules.user.dto.PtUserInfo;
import com.eveb.saasops.api.modules.user.service.NtService;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.modules.member.entity.MbrBillDetail;
import com.eveb.saasops.modules.member.entity.MbrBillManage;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.service.MbrWalletService;

@Service
// @Transactional
public class NtTransferService {

	@Autowired
	private BBINTransferService bbinTransferService;
	@Autowired
	private NtService ntService;
	@Autowired
	MbrWalletService mbrWalletService;
	@Autowired
	private DepotWalletService depotWalletService;

	public BillManBooleanDto ntTransferIn(BillRequestDto requestDto, TGmApi gmApi) {
		BillManBooleanDto billManBooleanDto = new BillManBooleanDto();
		MbrDepotWallet mbrWallet = ntService.isPlayerSessionAlive(gmApi,requestDto.getAccountId(), requestDto.getLoginName());
		if (mbrWallet.getIsTransfer() == MbrDepotWallet.IsTransFer.no) {
			mbrWalletService.updateTransfer(mbrWallet.getId());
		}
		MbrBillManage mbrBillManage = bbinTransferService.billChargeIn(requestDto);
		Integer state=ntService.deposit(gmApi,mbrBillManage.getAmount(),mbrBillManage.getOrderNo().toString(), mbrWallet.getUuid());
		/**查询转账后余额*/
		mbrBillManage.setDepotAfterBalance(depotWalletService.queryDepotBalance(requestDto.getAccountId(), gmApi).getBalance());
		if (state==TransferStates.suc) {
			bbinTransferService.updateMbrBillManageStatus(mbrBillManage, Constants.manageStatus.succeed);
			billManBooleanDto.setMbrBillManage(mbrBillManage);
			billManBooleanDto.setIsTransfer(Boolean.TRUE);
			return billManBooleanDto;
		} else if(state==TransferStates.fail){
			bbinTransferService.transferBust(mbrBillManage);
		}
		billManBooleanDto.setIsTransfer(Boolean.FALSE);
		return billManBooleanDto;
	}

	public Boolean ntTransferOut(BillRequestDto requestDto, TGmApi gmApi) {
		MbrDepotWallet mbrWallet = ntService.isPlayerSessionAlive(gmApi,requestDto.getAccountId(), requestDto.getLoginName());
		PtUserInfo info = ntService.getBalance(gmApi,requestDto.getAccountId(),requestDto.getLoginName(),mbrWallet.getUuid());
		if (Double.parseDouble(info.getBALANCE()) >= requestDto.getAmount().doubleValue()) {
			MbrBillManage mbrBillManage = bbinTransferService.billChargeOut(requestDto);
			MbrBillDetail mbrBillDetail = bbinTransferService.setMbrBillDetail(mbrBillManage);
			Integer state=ntService.withdraw(gmApi,mbrBillManage.getAmount(),mbrBillManage.getOrderNo().toString(),mbrWallet.getUuid());
			/**查询转账后余额*/
			mbrBillManage.setDepotAfterBalance(depotWalletService.queryDepotBalance(requestDto.getAccountId(), gmApi).getBalance());
			if (state==TransferStates.suc) {
				mbrBillManage.setBeforeBalance(mbrBillDetail.getBeforeBalance());
				mbrBillManage.setAfterBalance(mbrBillDetail.getAfterBalance());
				bbinTransferService.transferOutSuc(mbrBillDetail, mbrBillManage, Constants.manageStatus.succeed);
				return Boolean.TRUE;
			} else if(state==TransferStates.fail){
				bbinTransferService.updateMbrBillManageStatus(mbrBillManage, Constants.manageStatus.defeated);
			}
			return Boolean.FALSE;
		} else {
			throw new RRException("余额不足!");
		}
	}

	
}
