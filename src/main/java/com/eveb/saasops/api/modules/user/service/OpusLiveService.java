package com.eveb.saasops.api.modules.user.service;

import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.constants.OpusLiveConstants;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.user.dto.*;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.BigDecimalMath;
import com.eveb.saasops.common.utils.XmlUtil;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.service.MbrWalletService;
import com.eveb.saasops.modules.operate.entity.TGmDepot;
import com.eveb.saasops.modules.operate.service.TGmDepotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
@Slf4j
public class OpusLiveService {
	@Autowired
	MbrWalletService mbrWalletService;
	@Autowired
	private AginService aginService;
	@Autowired
	private TGmDepotService tGmDepotService;
	@Autowired
	private  OkHttpService okHttpService;
	/**
	 *
	 * @param gmApi
	 * @param accountId
	 * @param loginName
	 * @return
	 */
	public MbrDepotWallet createMember(TGmApi gmApi, Integer accountId, String loginName) {
		MbrDepotWallet mbrWallet = aginService.getDepotWallet(accountId, gmApi.getDepotId(), gmApi.getSiteCode(),
				loginName);
		if (mbrWallet.getIsBuild() == false) {
			String param = getOpusMemberDto(gmApi, loginName);
			String result = okHttpService.get(gmApi.getPcUrl()+OpusLiveConstants.Mod.createMember+param);
			log.debug("OPUS LIVE—>创建账号[会员账号{}提交参数{}返回结果{}]",loginName,gmApi.getPcUrl()+OpusLiveConstants.Mod.createMember + param,result);
			if (!StringUtils.isEmpty(result)) {
				OpusLiveResp resp = new OpusLiveResp();
				XmlUtil.opusSbResp(resp, result);
				if(!StringUtils.isEmpty(resp.getStatusCode())&&resp.getStatusCode().equals(OpusLiveConstants.SUC_CODE)) {
					TGmDepot tGmDepot = tGmDepotService.queryObject(mbrWallet.getDepotId());
					mbrWallet.setDepotName(tGmDepot.getDepotName());
					mbrWallet.setIsTransfer(MbrDepotWallet.IsTransFer.yes);
					return mbrWalletService.noExistInsert(mbrWallet, gmApi.getSiteCode());
				}
			}
			throw new RRException("PNG创建第三方账号失败!");
		}
		return mbrWallet;
	}

	private String getOpusMemberDto(TGmApi gmApi, String loginName) {
		OpusLiveDto dto = basicInfo(gmApi);
		dto.setMemberId(gmApi.getPrefix() + loginName);
		dto.setLanguage(OpusLiveConstants.Languages.chinese);
		dto.setCurrency(OpusLiveConstants.Currencys.rmb);
		return dto.toString();
	}

	private OpusLiveDto basicInfo(TGmApi gmApi) {
		OpusLiveDto dto = new OpusLiveDto();
		dto.setSecretKey(gmApi.getSecureCodes().get(OpusLiveConstants.JosnKey.secretKey));
		dto.setOperatorId(gmApi.getSecureCodes().get(OpusLiveConstants.JosnKey.operatiorId));
		dto.setSiteCode(gmApi.getSecureCodes().get(OpusLiveConstants.JosnKey.siteCode));
		dto.setProductCode(gmApi.getSecureCodes().get(OpusLiveConstants.JosnKey.productCode));
		return dto;
	}

	/**
	 *
	 * @param gmApi
	 * @param loginName
	 * @return
	 */
	public PtUserInfo getBalance(TGmApi gmApi, String loginName) {
		String param = getBalanceDto(gmApi, loginName);
		String result = okHttpService.get(gmApi.getPcUrl() + OpusLiveConstants.Mod.balance + param);
		log.debug("OPUS LIVE—>余额查询[会员账号{}提交参数{}返回结果{}]",loginName,gmApi.getPcUrl() + OpusLiveConstants.Mod.balance + param,result);
		if (!StringUtils.isEmpty(result)) {
			OpusLiveResp resp = new OpusLiveResp();
			XmlUtil.opusSbResp(resp, result);
			if (!StringUtils.isEmpty(resp.getStatusCode()) && resp.getStatusCode().equals(OpusLiveConstants.SUC_CODE)) {
				PtUserInfo info = new PtUserInfo();
				info.setBALANCE(BigDecimalMath.numberFormat(String.valueOf(resp.getUserBalance())));
				info.setCURRENCY(resp.getCurrency());
				return info;
			}
		}
		throw new RRException("暂停服务,请稍后重试!");
	}

	/**
	 *
	 * @param gmApi
	 * @param accountId
	 * @param loginName
	 * @return
	 */
	public PtUserInfo getBalance(TGmApi gmApi,Integer accountId,String loginName) {
		createMember(gmApi,accountId,loginName);
		return getBalance( gmApi, loginName);
	}


	private String getBalanceDto(TGmApi gmApi, String loginName) {
		OpusLiveDto dto = basicInfo(gmApi);
		dto.setMemberId(gmApi.getPrefix() + loginName);
		return dto.toString();
	}

	/**
	 *
	 * @param gmApi
	 * @param loginName
	 * @return
	 */
	public Boolean kickUser(TGmApi gmApi, String loginName) {
		String param = getBalanceDto(gmApi, loginName);
		String result = okHttpService.get(gmApi.getPcUrl() + OpusLiveConstants.Mod.kickUser + param);
		log.debug("OPUS LIVE—>会员踢线[会员账号{}提交参数{}返回结果{}]",loginName,gmApi.getPcUrl() + OpusLiveConstants.Mod.kickUser + param,result);
		if (!StringUtils.isEmpty(result)) {
			OpusLiveResp resp = new OpusLiveResp();
			XmlUtil.opusSbResp(resp, result);
			if (!StringUtils.isEmpty(resp.getStatusCode()) && resp.getStatusCode().equals( OpusLiveConstants.SUC_CODE))
				return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	/**
	 *
	 * @param gmApi
	 * @param loginName
	 * @return
	 */
	public Boolean checkIsOnline(TGmApi gmApi, String loginName) {
		String param = getBalanceDto(gmApi, loginName);
		String result = okHttpService.get(gmApi.getPcUrl() + OpusLiveConstants.Mod.checkIsOnline + param);
		log.debug("OPUS LIVE—>会员是否在线[会员账号{}提交参数{}返回结果 {}]",loginName,gmApi.getPcUrl() + OpusLiveConstants.Mod.checkIsOnline + param,result);
		if (!StringUtils.isEmpty(result)) {
			OpusLiveResp resp = new OpusLiveResp();
			XmlUtil.opusSbResp(resp, result);
			if (!StringUtils.isEmpty(resp.getMemberStatus()) && resp.getMemberStatus().equals(OpusLiveConstants.Memstatus.online))
				return resp.getIsOnline();
		}
		return Boolean.FALSE;
	}

	/**
	 *
	 * @param gmApi
	 * @param loginName
	 * @param amount
	 * @param tranid
	 * @param mod
	 * @return
	 */
	public Integer fundTransfer(TGmApi gmApi, String loginName, BigDecimal amount, String tranid,String  mod)
	{
		String param = getFundTransferDto(gmApi, loginName, amount, tranid);
		String result = okHttpService.get(gmApi.getPcUrl()+mod + param);
		log.debug("OPUS LIVE—>存取款 [会员账号{}提交参数{} 返回结果{}]",loginName,gmApi.getPcUrl() +mod+ param,result);
		if (!StringUtils.isEmpty(result)) {
			OpusLiveResp resp = new OpusLiveResp();
			XmlUtil.opusSbResp(resp, result);
			if (!StringUtils.isEmpty(resp.getStatusCode())&& resp.getStatusCode().equals(OpusLiveConstants.SUC_CODE)) {
					return ApiConstants.TransferStates.suc;
			}
		} return ApiConstants.TransferStates.fail;
	}

	private String getFundTransferDto(TGmApi gmApi, String loginName, BigDecimal amount, String tranid) {
		OpusLiveDto dto = basicInfo(gmApi);
		dto.setMemberId(gmApi.getPrefix() + loginName);
		dto.setAmount(amount);
		dto.setCurrency(OpusLiveConstants.Currencys.rmb);
		dto.setReferenceId(tranid);
		return dto.toString();
	}

	/**
	 *
	 * @param gmApi
	 * @param loginName
	 * @param amount
	 * @param tranid
	 * @return
	 */
	public Integer deposit(TGmApi gmApi, String loginName, BigDecimal amount, String tranid) {
		return fundTransfer(gmApi, loginName, amount, tranid, OpusLiveConstants.Mod.creditBalance);
	}

	/**
	 *
	 * @param gmApi
	 * @param loginName
	 * @param amount
	 * @param tranid
	 * @return
	 */
	public Integer withdraw(TGmApi gmApi, String loginName, BigDecimal amount, String tranid) {
		return fundTransfer(gmApi, loginName, amount, tranid, OpusLiveConstants.Mod.debitBalance);
	}

	/**
	 *
	 * @param tranid
	 * @param loginName
	 * @param gmApi
	 * @return
	 */
	public Integer queryOrderStatus(String tranid, String loginName,TGmApi gmApi) {
		String param = getCheckTransferDto(gmApi, loginName, tranid);
		String result = okHttpService.get(gmApi.getPcUrl() + OpusLiveConstants.Mod.checkFundTransfer + param);
		log.debug("OPUS LIVE—>查询存取款 [会员账号{}提交参数{}返回结果{}]",loginName,gmApi.getPcUrl() + OpusLiveConstants.Mod.checkFundTransfer + param,result);
		if (!StringUtils.isEmpty(result)) {
			OpusLiveResp resp = new OpusLiveResp();
			XmlUtil.opusSbResp(resp, result);
			if (!StringUtils.isEmpty(resp.getStatusCode())) {
				if (resp.getStatusCode().equals(OpusLiveConstants.SUC_CODE)) {
					return ApiConstants.TransferStates.suc;
				} else{
					return ApiConstants.TransferStates.fail;
				}
			}
		}
		throw new RRException("暂停服务,请稍后重试!");
	}

	private String getCheckTransferDto(TGmApi gmApi, String loginName,String tranid) {
		OpusLiveDto dto = basicInfo(gmApi);
		dto.setMemberId(gmApi.getPrefix() + loginName);
		dto.setReferenceId(tranid);
		return dto.toString();
	}

}
