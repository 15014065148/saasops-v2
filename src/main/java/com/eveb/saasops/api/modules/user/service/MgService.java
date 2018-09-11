package com.eveb.saasops.api.modules.user.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.constants.ApiConstants.TransferStates;
import com.eveb.saasops.api.constants.MgConstants;
import com.eveb.saasops.api.constants.MgConstants.DetailsResKey;
import com.eveb.saasops.api.constants.MgConstants.Json;
import com.eveb.saasops.api.constants.MgConstants.LaunchResKey;
import com.eveb.saasops.api.constants.MgConstants.LoginResKey;
import com.eveb.saasops.api.constants.MgConstants.MgCreationVal;
import com.eveb.saasops.api.constants.MgConstants.Mod;
import com.eveb.saasops.api.constants.MgConstants.Product;
import com.eveb.saasops.api.constants.MgConstants.TransferParam;
import com.eveb.saasops.api.constants.MgConstants.TransferResKey;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.user.dto.Casino;
import com.eveb.saasops.api.modules.user.dto.MgBalanceDto;
import com.eveb.saasops.api.modules.user.dto.MgCreationDto;
import com.eveb.saasops.api.modules.user.dto.MgLaunchGmDto;
import com.eveb.saasops.api.modules.user.dto.MgLoginDto;
import com.eveb.saasops.api.modules.user.dto.MgTokenDto;
import com.eveb.saasops.api.modules.user.dto.MgTransferDto;
import com.eveb.saasops.api.modules.user.dto.Poker;
import com.eveb.saasops.api.modules.user.dto.PtUserInfo;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.BigDecimalMath;
import com.eveb.saasops.common.utils.DateUtil;
import com.eveb.saasops.common.utils.XmlUtil;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.service.MbrWalletService;
import com.eveb.saasops.modules.operate.entity.TGmDepot;
import com.eveb.saasops.modules.operate.entity.TGmGame;
import com.eveb.saasops.modules.operate.service.TGmDepotService;

import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
public class MgService {
	@Autowired
	MbrWalletService mbrWalletService;
	@Autowired
	private TGmDepotService tGmDepotService;
	@Autowired
	private AginService aginService;
	@Autowired
	private  OkHttpService okHttpService;
	/**
	 *
	 * @param gmApi
	 * @return
	 */
	public String loginAgent(TGmApi gmApi) {
		String loginAgent = okHttpService.mgHttpPost(gmApi.getPcUrl() + Mod.token + getTokenParam(gmApi), "");
		log.debug(" MG—>代理账号登陆 [提交参数{}]",gmApi.getPcUrl() + Mod.token + getTokenParam(gmApi));
		if (!StringUtils.isEmpty(loginAgent)) {
			@SuppressWarnings("unchecked")
			Map<String, String> maps = (Map<String, String>) JSON.parse(loginAgent);
			return maps.get(MgConstants.TOKEN_RES_KEY);
		} else {
			throw new RRException("获取TOKEN失败!");
		}
	}

	/**
	 *
	 * @param gmApi
	 * @return
	 */
	public  String getTokenParam(TGmApi gmApi)
	{
		MgTokenDto mgTokenDto=new MgTokenDto();
		mgTokenDto.setJ_username(gmApi.getSecureCodes().get(Json.j_username));
		mgTokenDto.setJ_password(gmApi.getSecureCodes().get(Json.j_password));
		return mgTokenDto.toString();
	}

	/*public static void main(String args[])
	{
		String loginAgent = HttpsRequestUtil.mgHttpsPost("http://ag.adminserv88.com/" + Mod.token + getTokenParam(null), null);
		//log.debug(" MG—>代理账号登陆 [提交参数{}]",gmApi.getPcUrl() + Mod.token + getTokenParam(gmApi));
		if (!StringUtils.isEmpty(loginAgent)) {
			@SuppressWarnings("unchecked")
			Map<String, String> maps = (Map<String, String>) JSON.parse(loginAgent);
			//return maps.get(MgConstants.TOKEN_RES_KEY);
		} else {
			throw new RRException("获取TOKEN失败!");
		}
	}*/

	/**
	 *
	 * @param gmApi
	 * @param accountId
	 * @param loginName
	 * @return
	 */
	public MbrDepotWallet createMember(TGmApi gmApi, Integer accountId, String loginName){
		//return createMember(gmApi, accountId, loginName, loginAgent(gmApi));
		MbrDepotWallet mbrWallet = aginService.getDepotWallet(accountId, gmApi.getDepotId(), gmApi.getSiteCode(),loginName);
		if (!StringUtils.isEmpty(mbrWallet.getIsBuild()) && mbrWallet.getIsBuild() == false) {
			String param = getPlayerDto(gmApi, mbrWallet);
			String result = okHttpService.mghttpPut(gmApi.getPcUrl() + (Mod.createMember.replace(MgConstants.URL_ID, gmApi.getSecureCodes().get(Json.crId))), param,loginAgent(gmApi));
			log.debug(" MG—>创建账号 [会员账号{} 提交参数{} 返回结果 {}]",loginName,param,result);
			Map<?, ?> maps = (Map<?, ?>) JSON.parse(result);
			if (maps != null &&((Boolean)maps.get(MgConstants.RS_SUCCESS))) {
				TGmDepot tGmDepot = tGmDepotService.queryObject(mbrWallet.getDepotId());
				mbrWallet.setDepotName(tGmDepot.getDepotName());
				mbrWallet.setIsTransfer(MbrDepotWallet.IsTransFer.yes);
				return mbrWalletService.noExistInsert(mbrWallet, gmApi.getSiteCode());
			}else {
				throw new RRException("MG创建账号失败!");
			}
		}
		return mbrWallet;
	}

	private String  getPlayerDto(TGmApi gmApi, MbrDepotWallet mbrWallet) {		
		MgCreationDto dto = new MgCreationDto();
		dto.setCrId(gmApi.getSecureCodes().get(Json.crId));
		dto.setCrType(gmApi.getSecureCodes().get(Json.crType));
		dto.setNeId(gmApi.getSecureCodes().get(Json.neId));
		dto.setNeType(gmApi.getSecureCodes().get(Json.neType));
		dto.setTarType(MgCreationVal.tarType);
		dto.setUsername(gmApi.getPrefix()+mbrWallet.getLoginName());
		dto.setName(mbrWallet.getLoginName());
		dto.setPassword(mbrWallet.getPwd());
		dto.setConfirmPassword(mbrWallet.getPwd());
		dto.setCurrency(MgCreationVal.currency);
		dto.setLanguage(MgCreationVal.language);
		dto.setEmail("");
		dto.setMobile("");
		Casino casino=new Casino();
		casino.setEnable(Boolean.TRUE);
		Poker poker= new Poker();
		poker.setEnable(Boolean.FALSE);
		dto.setCasino(casino);
		dto.setPoker(poker);
		return JSON.toJSONString(dto);
	}

	/**
	 *
	 * @param gmApi
	 * @param accountId
	 * @param loginName
	 * @return
	 */
	public MbrDepotWallet login(TGmApi gmApi, Integer accountId, String loginName) {
		MbrDepotWallet mbrWallet = createMember(gmApi, accountId, loginName);
		String param = getLoginDto(gmApi, mbrWallet);
		String result = okHttpService.mgHttpPostXml(gmApi.getPcUrl() + Mod.login, param);
		log.debug(" MG—>登陆 [会员账号{} 提交参数{} 返回结果{}]",loginName,param,result);
		if (result != null) {
			Map<String, String> maps = getResult(result);
			if (!StringUtils.isEmpty(maps.get(LoginResKey.status))
					&& maps.get(LoginResKey.status).equals(MgConstants.RES_SUC_CODE)) {
				mbrWallet.setToken(maps.get(LoginResKey.token));
				return mbrWallet;
			}
		}
		throw new RRException("mg通信错误!");
	}

	private String getLoginDto(TGmApi gmApi, MbrDepotWallet mbrWallet) {
		MgLoginDto mgLoginDto = new MgLoginDto();
		mgLoginDto.setTimestamp(DateUtil.getUTCTimeStr());
		mgLoginDto.setApiusername(gmApi.getSecureCodes().get(Json.apiAdmin));
		mgLoginDto.setApipassword(gmApi.getSecureCodes().get(Json.apiPwd));
		mgLoginDto.setUsername(gmApi.getPrefix() + mbrWallet.getLoginName());
		mgLoginDto.setPassword(mbrWallet.getPwd());
		mgLoginDto.setPartnerId(gmApi.getSecureCodes().get(Json.partnerId));
		mgLoginDto.setCurrencyCode(MgCreationVal.currency);
		return mgLoginDto.toString();
	}


	/**
	 *
	 * @param gmApi
	 * @param accountId
	 * @param loginName
	 * @param mbrWallet
	 * @return
	 */
	public PtUserInfo getBalance(TGmApi gmApi, Integer accountId, String loginName, MbrDepotWallet mbrWallet) {
		String param = getBalanceDto(gmApi, mbrWallet);
		String result = okHttpService.mgHttpPostXml(gmApi.getPcUrl() + Mod.balance, param);
		log.debug(" MG—>余额查询 [会员账号{}提交参数{}返回结果{}]",loginName,param,result);
		Map<String, Object> maps = getBanlceResult(result);
		if (maps != null && maps.get(DetailsResKey.status).equals(MgConstants.RES_SUC_CODE)) {
			PtUserInfo info = new PtUserInfo();
			@SuppressWarnings("unchecked")
			String balance = ((Map<String, String>) maps.get(DetailsResKey.account_wallet)).get(DetailsResKey.cash_balance);
			info.setBALANCE(BigDecimalMath.numberFormat(balance));
			info.setCURRENCY(DetailsResKey.currency);
			return info;
		} else {
			throw new RRException("查询余额失败!");
		}
	}

	/**
	 *
	 * @param gmApi
	 * @param accountId
	 * @param loginName
	 * @return
	 */
	PtUserInfo getBalance(TGmApi gmApi, Integer accountId, String loginName) {
		MbrDepotWallet mbrWallet = login(gmApi, accountId, loginName);
		return getBalance(gmApi, accountId, loginName, mbrWallet);
	}
	
	private String getBalanceDto(TGmApi gmApi, MbrDepotWallet mbrWallet) {
		MgBalanceDto mgBalanceDto = new MgBalanceDto();
		mgBalanceDto.setTimestamp(DateUtil.getUTCTimeStr());
		mgBalanceDto.setApiusername(gmApi.getSecureCodes().get(Json.apiAdmin));
		mgBalanceDto.setApipassword(gmApi.getSecureCodes().get(Json.apiPwd));
		mgBalanceDto.setToken(mbrWallet.getToken());
		return mgBalanceDto.toString();
	}

	/**
	 *
	 * @param gmApi
	 * @param accountId
	 * @param loginName
	 * @param amount
	 * @param tranid
	 * @param mbrWallet
	 * @return
	 */
	public Integer deposit(TGmApi gmApi, Integer accountId, String loginName, BigDecimal amount, String tranid,MbrDepotWallet mbrWallet) {
		return transfer(gmApi, accountId, loginName, amount, tranid, TransferParam.deposit,mbrWallet);
	}

	/**
	 *
	 * @param gmApi
	 * @param accountId
	 * @param loginName
	 * @param amount
	 * @param tranid
	 * @param mbrWallet
	 * @return
	 */
	public Integer withdraw(TGmApi gmApi, Integer accountId, String loginName, BigDecimal amount, String tranid,MbrDepotWallet mbrWallet) {
		return transfer(gmApi, accountId, loginName, amount, tranid, TransferParam.withdraw,mbrWallet);
	}

	/**
	 *
	 * @param gmApi
	 * @param accountId
	 * @param loginName
	 * @param amount
	 * @param tranid
	 * @return
	 */
	public Integer deposit(TGmApi gmApi, Integer accountId, String loginName, BigDecimal amount, String tranid) {
		MbrDepotWallet mbrWallet = login(gmApi, accountId, loginName);
		return transfer(gmApi, accountId, loginName, amount, tranid, TransferParam.deposit,mbrWallet);
	}

	/**
	 *
	 * @param gmApi
	 * @param accountId
	 * @param loginName
	 * @param amount
	 * @param tranid
	 * @return
	 */
	public Integer withdraw(TGmApi gmApi, Integer accountId, String loginName, BigDecimal amount, String tranid) {
		MbrDepotWallet mbrWallet = login(gmApi, accountId, loginName);
		return transfer(gmApi, accountId, loginName, amount, tranid, TransferParam.withdraw,mbrWallet);
	}

	private Integer transfer(TGmApi gmApi, Integer accountId, String loginName, BigDecimal amount, String tranid,String transferType,MbrDepotWallet mbrWallet) {
		String param = getTransferDto(gmApi, mbrWallet, transferType, amount, tranid);
		String result = okHttpService.mgHttpPostXml(gmApi.getPcUrl() + Mod.transfer, param);
		log.debug(" MG—>转账 [会员账号{}提交参数{}返回结果 {}]",loginName,param,result);
		Map<String, String> maps = getResult(result);
		if (maps != null && maps.get(TransferResKey.status).equals(MgConstants.RES_SUC_CODE)) {
			return TransferStates.suc;
		} else {
			return TransferStates.fail;
		}
	}

	private String getTransferDto(TGmApi gmApi, MbrDepotWallet mbrWallet,String transferType,BigDecimal amount, String tranid) {
		MgTransferDto mgTransferDto = new MgTransferDto();
		mgTransferDto.setTimestamp(DateUtil.getUTCTimeStr());
		mgTransferDto.setApiusername(gmApi.getSecureCodes().get(Json.apiAdmin));
		mgTransferDto.setApipassword(gmApi.getSecureCodes().get(Json.apiPwd));
		mgTransferDto.setToken(mbrWallet.getToken());
		mgTransferDto.setProduct(Product.casino);
		mgTransferDto.setOperation(transferType);
		mgTransferDto.setAmount(String.valueOf(amount.doubleValue())); 
		mgTransferDto.setTxId(tranid);
		return mgTransferDto.toString();
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
		MbrDepotWallet mbrWallet = login(gmApi, accountId, loginName);
		String param = getLaunchGmDto(gmApi, mbrWallet, tgmGame, terminal);
		String result = okHttpService.mgHttpPostXml(gmApi.getPcUrl() + Mod.gameInfo, param);
		log.debug(" MG—>游戏跳转 [会员账号{}提交参数{}返回结果{}]", loginName, param, result);
		Map<String, String> maps = getResult(result);
		if (MapUtils.isNotEmpty(maps)) {
			if (maps.get(LaunchResKey.status).equals(MgConstants.RES_SUC_CODE)) {
				return maps.get(LaunchResKey.launchUrl);
			} else {
				throw new RRException(maps.get(LaunchResKey.status) + "游戏无效!");
			}
		} else {
			throw new RRException("MG登陆失败!");
		}
	}

	private String getLaunchGmDto(TGmApi gmApi, MbrDepotWallet mbrWallet, TGmGame tgmGame,Byte terminal) {
		MgLaunchGmDto mgLaunchGmDto = new MgLaunchGmDto();
		mgLaunchGmDto.setTimestamp(DateUtil.getUTCTimeStr());
		mgLaunchGmDto.setApiusername(gmApi.getSecureCodes().get(Json.apiAdmin));
		mgLaunchGmDto.setApipassword(gmApi.getSecureCodes().get(Json.apiPwd));
		mgLaunchGmDto.setToken(mbrWallet.getToken());
		mgLaunchGmDto.setGameId((terminal==ApiConstants.Terminal.mobile)?tgmGame.getMbGameCode():tgmGame.getGameCode());
		mgLaunchGmDto.setLanguage(MgCreationVal.language);
		mgLaunchGmDto.setBankingUrl("");
		mgLaunchGmDto.setLobbyUrl("");
		mgLaunchGmDto.setLogoutRedirectUrl("");
		mgLaunchGmDto.setDemoMode(Boolean.FALSE.toString());
		mgLaunchGmDto.setTitanium("unknown");
		return mgLaunchGmDto.toString();
	}



	private Map<String, String> getResult(String result) {
		Map<String, String> map = new HashMap<String, String>();
		XmlUtil.mgParseRes(map, result);
		return map;
	}
	private Map<String, Object> getBanlceResult(String result) {
		Map<String, Object> map = new HashMap<String, Object>();
		XmlUtil.mgParseBanlceRes(map, result);
		return map;
	}
}
