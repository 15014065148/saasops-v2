package com.eveb.saasops.api.modules.user.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.constants.ApiConstants.TransferStates;
import com.eveb.saasops.api.constants.PtNewConstants;
import com.eveb.saasops.api.constants.PtNewConstants.JsonKeys;
import com.eveb.saasops.api.constants.PtNewConstants.Mod;
import com.eveb.saasops.api.constants.PtNewConstants.RegiterDefault;
import com.eveb.saasops.api.constants.PtNewConstants.TransactionInfo;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.user.dto.PtNewPlayerDataDto;
import com.eveb.saasops.api.modules.user.dto.PtNewTransferDto;
import com.eveb.saasops.api.modules.user.dto.PtUserInfo;
import com.eveb.saasops.api.modules.user.dto.UserPtNewDto;
import com.eveb.saasops.api.utils.JsonUtil;
import com.eveb.saasops.common.constants.Constants.Available;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.service.MbrWalletService;
import com.eveb.saasops.modules.operate.entity.TGmDepot;
import com.eveb.saasops.modules.operate.entity.TGmGame;
import com.eveb.saasops.modules.operate.service.TGmDepotService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PtNewService {
	@Autowired
	MbrWalletService mbrWalletService;
	@Autowired
	private TGmDepotService tGmDepotService;
	@Autowired
	private AginService aginService;
	@Autowired
	private  OkHttpService okHttpService;
	
/*	public R getJackPot() {
		String xml = OkHttpUtils.postJson(ptConfig.getLuckyJackpotUrl(), null);
		PtJackPotResDto ptJackPotResDto = XmlUtil.ptJackPotResDto(xml);
		if (ptJackPotResDto == null) {
			R.ok().put("amount", "0.00");
		}
		return R.ok().put("amount", ptJackPotResDto.getAmounts());
	}*/

	public String loginAgent(TGmApi gmApi) {
		//String token = apiUserService.queryPt2LoginTokenCache(gmApi.getSiteCode(), gmApi.getAgyAcc());
		//if (StringUtils.isEmpty(token)) {
			UserPtNewDto dto = getAgentInfo(gmApi);
			String loginAgent = okHttpService.ptNewhttpPost(gmApi.getPcUrl() + Mod.agentLogin, JSON.toJSONString(dto));
			log.debug(" PT NEW—>代理账号登陆 [提交参数{}返回结果{}]",gmApi.getPcUrl() + Mod.agentLogin+" "+JSON.toJSONString(dto),loginAgent);
			Map<?, ?> maps = (Map<?, ?>) JSON.parse(loginAgent);
			String token = (String) maps.get(PtNewConstants.LOGIN_TOKEN);
			//apiUserService.updatePt2LoginTokenCache(gmApi.getSiteCode(), gmApi.getAgyAcc(), token);
		//}
		return token;
	}

	/**
	 * 
	 * @param gmApi
	 * @param accountId
	 * @param loginName
	 * @param token
	 * @return
	 */
	public MbrDepotWallet createMember(TGmApi gmApi, Integer accountId, String loginName, String token) {
		
		MbrDepotWallet mbrWallet = aginService.getDepotWallet(accountId, gmApi.getDepotId(), gmApi.getSiteCode(),
				loginName);
		if (!StringUtils.isEmpty(mbrWallet.getIsBuild()) && mbrWallet.getIsBuild() == false) {
			PtNewPlayerDataDto dto = getPlayerDto(gmApi, mbrWallet);
			String result = okHttpService.ptNewhttpPost(gmApi.getPcUrl() + Mod.register, JSON.toJSONString(dto), token);
			log.debug(" PT NEW—>创建账号 [会员账号{}提交参数 {}返回结果 {}]",loginName,gmApi.getPcUrl() + Mod.register+" "+JSON.toJSONString(dto),result);
			Map<?, ?> maps = (Map<?, ?>) JSON.parse(result);
			if (maps != null &&!StringUtils.isEmpty(maps.get("id"))) {
				TGmDepot tGmDepot = tGmDepotService.queryObject(mbrWallet.getDepotId());
				mbrWallet.setDepotName(tGmDepot.getDepotName());
				mbrWallet.setIsTransfer(MbrDepotWallet.IsTransFer.yes);
				return mbrWalletService.noExistInsert(mbrWallet, gmApi.getSiteCode());
			}
		}
		return mbrWallet;
	}

	public MbrDepotWallet createMember(TGmApi gmApi, Integer accountId, String loginName) {
		String token = loginAgent(gmApi);
		MbrDepotWallet wallet=createMember(gmApi, accountId, loginName, token);
		wallet.setToken(token);
		return wallet;
	}
	
	public PtUserInfo balance(TGmApi gmApi, Integer accountId, String loginName, String token) {
		PtUserInfo info = new PtUserInfo();
		String result = okHttpService.ptNewGet(gmApi.getPcUrl() + Mod.info+gmApi.getPrefix()+loginName, token);
		log.debug(" PT NEW—>余额查询 [会员账号{}提交参数 {}返回结果 {}]",loginName,gmApi.getPcUrl() + Mod.info+gmApi.getPrefix()+loginName,result);
		if (!StringUtils.isEmpty(result)) {
			Map<String, java.lang.Object> maps = JsonUtil.json2map(result);
			String balance = String.valueOf(((Map)((Map)maps.get("balances")).get(RegiterDefault.currency)).get("main"));
			info.setBALANCE(balance);
			info.setCURRENCY(RegiterDefault.currency);
			return info;
		} else {
			throw new RRException("暂停服务,请稍后重试!");
		}
	}

	public PtUserInfo balance(TGmApi gmApi, Integer accountId, String loginName) {
		MbrDepotWallet wallet = createMember(gmApi, accountId, loginName);
		return balance(gmApi, accountId, loginName, wallet.getToken());
	}
	
	private UserPtNewDto getAgentInfo(TGmApi gmApi) {
		UserPtNewDto dto = new UserPtNewDto();
		dto.setPassword(gmApi.getMd5Key());
		dto.setUsername(gmApi.getAgyAcc());
		dto.setSecretKey(gmApi.getSecureCodes().get(JsonKeys.secretKey));
		return dto;
	}

	private PtNewPlayerDataDto getPlayerDto(TGmApi gmApi, MbrDepotWallet mbrWallet) {
		PtNewPlayerDataDto dto = new PtNewPlayerDataDto();
		dto.setCode(gmApi.getPrefix() + mbrWallet.getLoginName());
		dto.setCountry(RegiterDefault.country);
		dto.setCurrency(RegiterDefault.currency);
		dto.setEmail(null);
		dto.setFirstName(null);
		dto.setGameGroup(null);
		dto.setLanguage(RegiterDefault.language);
		dto.setLastName(null);
		//dto.setPassword(mbrWallet.getPwd());
		dto.setStatus(RegiterDefault.status);
		dto.setTest(RegiterDefault.isTest);
		return dto;
	}
	private PtNewTransferDto getPtNewTransferDto(TGmApi gmApi,String loginName,Double amount,String extTrxId)
	{
		PtNewTransferDto dto=new PtNewTransferDto();
		dto.setPlayerCode(gmApi.getPrefix()+loginName);
		dto.setExtTrxId(extTrxId);
		dto.setAmount(amount);
		dto.setCurrency(RegiterDefault.currency);
		return dto;
	}


	public Integer deposit(TGmApi gmApi, Integer accountId, String loginName, double amount, String tranid,String token) {
		PtNewTransferDto dto = getPtNewTransferDto(gmApi, loginName, amount, tranid);
		String result = okHttpService.ptNewhttpPost(gmApi.getPcUrl() + Mod.deposit, JSON.toJSONString(dto), token);
		log.debug(" PT NEW—>存款 [会员账号{}提交参数 {}返回结果{}]",loginName,gmApi.getPcUrl() + Mod.deposit+" "+JSON.toJSONString(dto),result);
		if (!StringUtils.isEmpty(result)) {
			Map<?, ?> maps = (Map<?, ?>) JSON.parse(result);
			if (maps != null && String.valueOf(maps.get("orderStatus")).endsWith("approved")) {
				return TransferStates.suc;
			} else if (maps != null && String.valueOf(maps.get("orderStatus")).endsWith("init")) {
				return TransferStates.progress;
			} /*else if (maps != null && (String.valueOf(maps.get("orderStatus")).endsWith("declined")
					|| String.valueOf(maps.get("orderStatus")).endsWith("blocked"))) {
				return TransferStates.fail;
			}*/
		}
		return TransferStates.fail;
	}

	public Integer deposit(TGmApi gmApi, Integer accountId, String loginName, double amount, String tranid) {
		MbrDepotWallet wallet = createMember(gmApi, accountId, loginName);
		if (wallet.getIsTransfer() == MbrDepotWallet.IsTransFer.no) {
			mbrWalletService.updateTransfer(wallet.getId());
		}
		return deposit(gmApi, accountId, loginName, amount, tranid, wallet.getToken());
	}

	/**
	 * 取款
	 * 
	 * @param gmApi
	 * @param loginName
	 * @param amount
	 * @param tranid
	 * @return
	 */
	public Integer withdraw(TGmApi gmApi, Integer accountId, String loginName, double amount, String tranid,
			String token) {
		PtNewTransferDto dto = getPtNewTransferDto(gmApi, loginName, amount, tranid);
		String result = okHttpService.ptNewhttpPost(gmApi.getPcUrl() + Mod.withdraw, JSON.toJSONString(dto), token);
		log.debug(" PT NEW—>取款 [会员账号{}提交参数{}返回结果{}]",loginName,gmApi.getPcUrl() + Mod.withdraw+" "+JSON.toJSONString(dto),result);
		if (!StringUtils.isEmpty(result)) {
			Map<?, ?> maps = (Map<?, ?>) JSON.parse(result);
			if (maps != null && String.valueOf(maps.get("orderStatus")).endsWith("approved")) {
				return TransferStates.suc;
			} else if (maps != null && String.valueOf(maps.get("orderStatus")).endsWith("init")) {
				return TransferStates.progress;
			} /*else if (maps != null && (String.valueOf(maps.get("orderStatus")).endsWith("declined")
					|| String.valueOf(maps.get("orderStatus")).endsWith("blocked"))) {
				return TransferStates.fail;
			}*/
		}
		return TransferStates.fail;
	}

	public Integer withdraw(TGmApi gmApi, Integer accountId, String loginName, double amount, String tranid) {
		String token=loginAgent(gmApi);
		return withdraw(gmApi, accountId, loginName, amount, tranid,token);
	}

	public String generateUrl(TGmApi gmApi, TGmGame tgmGame,  Integer accountId, String loginName, Byte terminal) {
		if (terminal == ApiConstants.Terminal.mobile && tgmGame.getEnableMb() == Available.disable) {
			throw new RRException("此款游戏不支持手机端!");
		}
		MbrDepotWallet wallet = createMember(gmApi, accountId, loginName);
		String result = okHttpService.ptNewGet(gmApi.getPcUrl() + Mod.gameInfo.replace("playerCode", gmApi.getPrefix() + loginName) + tgmGame.getGameCode(), wallet.getToken());
		log.debug(" PT NEW—>游戏跳转 [会员账号{}提交参数 {} Token:{} 返回结果 {}]",loginName,gmApi.getPcUrl() + Mod.gameInfo.replace("playerCode", gmApi.getPrefix() + loginName) + tgmGame.getGameCode(),wallet.getToken(),result);
		if (!StringUtils.isEmpty(result)) {
			Map<?, ?> maps = (Map<?, ?>) JSON.parse(result);
			return String.valueOf(maps.get("url"));
		} else {
			throw new RRException("URL地址异常!");
		}
	}
	

/**
 * status [absent 失败 |processing 处理中 | committed 成功]
 * @param tranid
 * @param gmApi
 * @param token
 * @return
 */
	public Integer checktransaction(String tranid, TGmApi gmApi, String token) {
		String result = okHttpService.ptNewGet(gmApi.getPcUrl() + Mod.trfrStatus + tranid, token);
		log.debug(" PT NEW—>取款 [提交参数 {} Token:{} 返回结果{}]",gmApi.getPcUrl() + Mod.trfrStatus+" "+tranid,token,result);
		if (!StringUtils.isEmpty(result)) {
			Map<?, ?> maps = (Map<?, ?>) JSON.parse(result);
			if (maps != null) {
				String state = String.valueOf(maps.get("status")).toLowerCase();
				if (state.equals(TransactionInfo.committed)) {
					return TransferStates.suc;
				} else if (state.equals(TransactionInfo.absent)) {
					return TransferStates.fail;
				} else if (state.equals(TransactionInfo.processing)) {
					return TransferStates.progress;
				}
			}
		}
		throw new RRException("订单查询失败!");
	}

	/**
	 * status [absent |processing | committed]
	 * @param tranid
	 * @param gmApi
	 * @return
	 */
	public Integer checktransaction(String tranid, TGmApi gmApi) {
		String token = loginAgent(gmApi);
		return checktransaction(tranid, gmApi, token);
	}
}
