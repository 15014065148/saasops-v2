package com.eveb.saasops.api.modules.user.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.constants.ApiConstants.TransferStates;
import com.eveb.saasops.api.constants.NtConstants;
import com.eveb.saasops.api.constants.NtConstants.JsonKey;
import com.eveb.saasops.api.constants.NtConstants.Mod;
import com.eveb.saasops.api.constants.PtNewConstants.RegiterDefault;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.user.dto.GetBalanceReq;
import com.eveb.saasops.api.modules.user.dto.GetBalanceResp;
import com.eveb.saasops.api.modules.user.dto.GetTransactionReq;
import com.eveb.saasops.api.modules.user.dto.GetTransactionResp;
import com.eveb.saasops.api.modules.user.dto.NtDto;
import com.eveb.saasops.api.modules.user.dto.NtLoginRes;
import com.eveb.saasops.api.modules.user.dto.PtUserInfo;
import com.eveb.saasops.api.modules.user.dto.TransferResp;
import com.eveb.saasops.api.modules.user.dto.TransferV2Req;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.XmlUtil;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.service.MbrWalletService;
import com.eveb.saasops.modules.operate.entity.TGmDepot;
import com.eveb.saasops.modules.operate.entity.TGmGame;
import com.eveb.saasops.modules.operate.service.TGmDepotService;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NtService {
	@Autowired
	MbrWalletService mbrWalletService;
	@Autowired
	private TGmDepotService tGmDepotService;
	@Autowired
	private AginService aginService;
	@Autowired
	private ApiUserService apiUserService;
	@Autowired
	private  OkHttpService okHttpService;

	/**
	 *
	 * @param gmApi
	 * @return
	 */
	public boolean ping(TGmApi gmApi) {
		String result = okHttpService.get(gmApi.getPcUrl() + Mod.ping);
		log.debug("NT—>测试ping[提交参数 {}返回结果 {}]",gmApi.getPcUrl() + Mod.ping,result);
		if (result.equals(NtConstants.RES_PING))
			return Boolean.TRUE;
		else
			return Boolean.FALSE;
	}

	/**
	 *
	 * @param gmApi
	 * @param accountId
	 * @param loginName
	 * @return
	 */
	public MbrDepotWallet isPlayerSessionAlive(TGmApi gmApi, Integer accountId, String loginName) {
		NtLoginRes loginRes = apiUserService.queryNtLoginTokenCache(gmApi.getSiteCode(), loginName);
		if (!StringUtils.isEmpty(loginRes)) {
			MbrDepotWallet mbrWallet = aginService.getDepotWallet(accountId, gmApi.getDepotId(), gmApi.getSiteCode(),
					loginName);
			mbrWallet.setToken(loginRes.getSessionKey());
			mbrWallet.setPartyId(loginRes.getPartyId());
			return mbrWallet;
		} else {
			return createOrLoginMember(gmApi, accountId, loginName);
		}
	}

	/**
	 *
	 * @param gmApi
	 * @param accountId
	 * @param loginName
	 * @return
	 */
	public MbrDepotWallet createOrLoginMember(TGmApi gmApi, Integer accountId, String loginName) {
		MbrDepotWallet mbrWallet = aginService.getDepotWallet(accountId, gmApi.getDepotId(), gmApi.getSiteCode(),
				loginName);
		if (mbrWallet.getIsBuild() == false) {
			mbrWallet.setUuid(UUID.randomUUID().toString().replaceAll("-", ""));
			NtLoginRes loginRes = loginMember(gmApi, accountId, loginName, mbrWallet);
			if (loginRes != null && loginRes.getStatus().equals(NtConstants.LOGING_SUCCESS)) {
				TGmDepot tGmDepot = tGmDepotService.queryObject(mbrWallet.getDepotId());
				mbrWallet.setDepotName(tGmDepot.getDepotName());
				mbrWallet.setIsTransfer(MbrDepotWallet.IsTransFer.yes);
				mbrWallet.setToken(loginRes.getSessionKey());
				mbrWallet.setPartyId(loginRes.getPartyId());
				apiUserService.updateNtLoginTokenCache(gmApi.getSiteCode(), loginName, loginRes);
				return mbrWalletService.noExistInsert(mbrWallet, gmApi.getSiteCode());
			} else {
				throw new RRException("NT创建第三方账号失败!");
			}
		} else {
			NtLoginRes loginRes = loginMember(gmApi, accountId, loginName, mbrWallet);
			if (loginRes != null && loginRes.getStatus().equals(NtConstants.LOGING_SUCCESS)) {
				mbrWallet.setToken(String.valueOf(loginRes.getSessionKey()));
				mbrWallet.setPartyId(loginRes.getPartyId());
				apiUserService.updateNtLoginTokenCache(gmApi.getSiteCode(), loginName, loginRes);
			} else {
				throw new RRException("NT登陆失败!");
			}
		}
		return mbrWallet;
	}

	/**
	 *
	 * @param gmApi
	 * @param accountId
	 * @param loginName
	 * @param mbrWallet
	 * @return
	 */
	public NtLoginRes loginMember(TGmApi gmApi, Integer accountId, String loginName, MbrDepotWallet mbrWallet) {
		String dtoStr=getLoginDto(gmApi, loginName, mbrWallet.getUuid());
		String result = okHttpService.httpsNtPost(gmApi.getPcUrl() + Mod.login + dtoStr);
		log.debug("NT—>登陆或创建账号[会员账号{}提交参数{}返回结果{}]",mbrWallet.getLoginName(),gmApi.getPcUrl() + Mod.login+" "+dtoStr,result);
		return new Gson().fromJson(result, NtLoginRes.class);
	}

	/**
	 *
	 * @param gmApi
	 * @param accountId
	 * @param loginName
	 * @param uuid
	 * @return
	 */
	public PtUserInfo balance(TGmApi gmApi, Integer accountId, String loginName, String uuid) {
		String context = getBalanceReq(gmApi, loginName, uuid);
		String result = okHttpService.httpNtSoapPost(gmApi.getSecureCodes().get(JsonKey.walletUrl), context);
		log.debug("NT—>余额查询 [会员账号{}提交参数{}返回结果{}]",loginName,context,result);
		if (!StringUtils.isEmpty(result)) {
			GetBalanceResp resp = new GetBalanceResp();
			XmlUtil.balanceResp(resp, result);
			PtUserInfo info = new PtUserInfo();
			info.setBALANCE(String.valueOf(resp.getReal()));
			info.setCURRENCY(RegiterDefault.currency);
			return info;
		} else {
			throw new RRException("暂停服务,请稍后重试!");
		}
	}

	/**
	 *
	 * @param gmApi
	 * @param accountId
	 * @param loginName
	 * @param uuid
	 * @return
	 */
	public PtUserInfo getBalance(TGmApi gmApi, Integer accountId, String loginName,String uuid) {
		return balance(gmApi, accountId, loginName, uuid);
	}

	/**
	 *
	 * @param gmApi
	 * @param accountId
	 * @param loginName
	 * @return
	 */
	public PtUserInfo getBalance(TGmApi gmApi, Integer accountId, String loginName) {
		MbrDepotWallet mbrWallet = isPlayerSessionAlive(gmApi, accountId, loginName);
		return balance(gmApi, accountId, loginName, mbrWallet.getUuid());
	}

	private String getBalanceReq(TGmApi gmApi, String loginName, String uuid) {
		GetBalanceReq dto = new GetBalanceReq();
		dto.setUuid(uuid);
		dto.setBrandId(gmApi.getSecureCodes().get(JsonKey.brandId));
		dto.setBrandPassword(gmApi.getSecureCodes().get(JsonKey.brandPwd));
		return dto.toString();
	}

	private String getLoginDto(TGmApi gmApi, String loginName, String uuid) {
		NtDto dto = new NtDto();
		dto.setUuid(uuid);
		dto.setLoginName(gmApi.getPrefix() + loginName);
		dto.setBrandId(gmApi.getSecureCodes().get(JsonKey.brandId));
		dto.setBrandPassword(gmApi.getSecureCodes().get(JsonKey.brandPwd));
		dto.setCurrency(NtConstants.DEF_CURRENCY);
		return dto.toString();
	}

	private String getGameDto(TGmApi gmApi, String sessionKey, String gameCode, Byte terminal) {
		NtDto dto = new NtDto();
		dto.setPlayForReal(Boolean.TRUE);
		dto.setSessionKey(sessionKey);
		dto.setBrandId(gmApi.getSecureCodes().get(JsonKey.brandId));
		dto.setGameId(gameCode);
		dto.setPlatform(NtConstants.NETENT_CAS);
		if (terminal == ApiConstants.Terminal.mobile)
			dto.setMobile(Boolean.TRUE);
		return dto.toString();
	}

	String getSessionAlive(String token) {
		NtDto dto = new NtDto();
		dto.setSessionKey(token);
		return dto.toString();
	}

	private String getTransferReq(TGmApi gmApi, String uuid, BigDecimal amount, String tranid){
		TransferV2Req dto = new TransferV2Req();
		dto.setUuid(uuid);
		dto.setBrandId(gmApi.getSecureCodes().get(JsonKey.brandId));
		dto.setBrandPassword(gmApi.getSecureCodes().get(JsonKey.brandPwd));
		dto.setAmount(amount);
		dto.setIso3Currency(NtConstants.DEF_CURRENCY);
		dto.setPlatformTranId(tranid);
		return dto.toString();
	}

	private String getCheckTransferReq(TGmApi gmApi, String tranid) {
		GetTransactionReq dto = new GetTransactionReq();
		dto.setBrandId(gmApi.getSecureCodes().get(JsonKey.brandId));
		dto.setBrandPassword(gmApi.getSecureCodes().get(JsonKey.brandPwd));
		dto.setTransactionId(tranid);
		return dto.toString();
	}

	/**
	 *
	 * @param gmApi
	 * @param amount
	 * @param tranid
	 * @param uuid
	 * @return
	 */
	public Integer deposit(TGmApi gmApi, BigDecimal amount, String tranid, String uuid) {
		String context = getTransferReq(gmApi, uuid, amount, tranid);
		return transferService(gmApi, context);

	}

	/**
	 *
	 * @param gmApi
	 * @param amount
	 * @param tranid
	 * @param uuid
	 * @return
	 */
	public Integer withdraw(TGmApi gmApi, BigDecimal amount, String tranid, String uuid) {
		String context = getTransferReq(gmApi, uuid, BigDecimal.ZERO.subtract(amount), tranid);
		return transferService(gmApi, context);
	}

	/**
	 * 查一下这 以下四个字段的表现行式
	 amountReleasedBonus BigDecimal The amount of funds that were credited/debited to releasedBonus (see money types section)
	 amountPlayableBonus BigDecimal The amount of funds that were credited/debited to playableBonus (see money types section)
	 balanceReleasedBonus BigDecimal The post balance of releasedBonus funds
	 balancePlayableBonus BigDecimal The post balance of playableBonus funds
	 * @param gmApi
	 * @param context
	 * @return
	 */
	public Integer transferService(TGmApi gmApi, String context) {
		String result = okHttpService.httpNtSoapPost(gmApi.getSecureCodes().get(JsonKey.walletUrl), context);
		log.debug("NT—>转账 [提交参数{}返回结果{}]",context,result);
		if (!StringUtils.isEmpty(result)) {
			TransferResp resp = new TransferResp();
			XmlUtil.transferResp(resp, result);
			if (!StringUtils.isEmpty(resp.getResp().getAmountReal()))
				return TransferStates.suc;
			else
				return TransferStates.fail;
		} else {
			return TransferStates.fail;
		}
	}

	/**
	 *
	 * @param gmApi
	 * @param tgmGame
	 * @param accountId
	 * @param loginName
	 * @param terminal
	 * @return
	 */
	public String generateUrl(TGmApi gmApi, TGmGame tgmGame,  Integer accountId, String loginName, Byte terminal) {
		//NT平台的 PC 端GAMECOD<_>在 gameCode 手机端 gameCode在 gameParam
		MbrDepotWallet wallet = isPlayerSessionAlive(gmApi, accountId, loginName);
		String url = gmApi.getPcUrl() + Mod.gameInfo + getGameDto(gmApi, wallet.getToken(), (terminal == ApiConstants.Terminal.mobile) ? tgmGame.getMbGameCode() : tgmGame.getGameCode(), terminal);
		log.debug("NT—>游戏跳转 [提交参数{}]",url);
		return url;
	}

	/**
	 *
	 * @param tranid
	 * @param gmApi
	 * @return
	 */
	public Integer checktransaction(String tranid, TGmApi gmApi) {
		String context = getCheckTransferReq(gmApi, tranid);
		String result = okHttpService.httpNtSoapPost(gmApi.getSecureCodes().get(JsonKey.walletUrl), context);
		log.debug("NT—>订单查询 [提交参数{}返回结果 {}]",context,result);
		if (!StringUtils.isEmpty(result)) {
			GetTransactionResp resp = new GetTransactionResp();
			XmlUtil.checkTransferResp(resp, result);
			if (!StringUtils.isEmpty(resp.getAmountReal())) {
				return TransferStates.suc;
			} else {
				return TransferStates.fail;
			}
		} else {
			throw new RRException("暂停服务,请稍后重试!");
		}
	}

}
