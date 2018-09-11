package com.eveb.saasops.api.modules.user.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.eveb.saasops.api.constants.ApiConstants.TransferStates;
import com.eveb.saasops.api.constants.T188Constants;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.user.dto.Balance188Dto;
import com.eveb.saasops.api.modules.user.dto.Deposit188Dto;
import com.eveb.saasops.api.modules.user.dto.GetT188Resp;
import com.eveb.saasops.api.modules.user.dto.PtUserInfo;
import com.eveb.saasops.api.modules.user.dto.RegisterMember188Dto;
import com.eveb.saasops.api.modules.user.dto.TransferStatus188Dto;
import com.eveb.saasops.api.modules.user.dto.Withdraw188Dto;
import com.eveb.saasops.api.utils.CipherText;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.BigDecimalMath;
import com.eveb.saasops.common.utils.XmlUtil;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.service.MbrWalletService;
import com.eveb.saasops.modules.operate.entity.TGmDepot;
import com.eveb.saasops.modules.operate.entity.TGmGame;
import com.eveb.saasops.modules.operate.service.TGmDepotService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class T188Service {
	@Autowired
	MbrWalletService mbrWalletService;
	@Autowired
	private AginService aginService;
	@Autowired
	private TGmDepotService tGmDepotService;
	@Autowired
	private  OkHttpService okHttpService;
	/**
	 * 注意第三方接口注册返回结果比较慢 注意此接口，连接超时应设大值
	 * @param gmApi
	 * @param accountId
	 * @param loginName
	 * @return
	 */
	public MbrDepotWallet createMember(TGmApi gmApi, Integer accountId, String loginName) {
		MbrDepotWallet mbrWallet = aginService.getDepotWallet(accountId, gmApi.getDepotId(), gmApi.getSiteCode(),
				loginName);
		if (mbrWallet.getIsBuild() == false) {
			String param = getRegisterMember188Dto(gmApi, loginName);
			String result = okHttpService.httpSoapPost(gmApi.getPcUrl() + T188Constants.Mod.register, param);
			log.debug(" T188—>创建账号 [会员账号{}提交参数 {} 返回结果{}]",loginName,gmApi.getPcUrl() + T188Constants.Mod.register +" "+param,result);
			if (!StringUtils.isEmpty(result)) {
				GetT188Resp resp = getT188Resp(gmApi, result);
				if (resp.getReturnCode().equals(T188Constants.SUC_CODE)) {
					TGmDepot tGmDepot = tGmDepotService.queryObject(mbrWallet.getDepotId());
					mbrWallet.setDepotName(tGmDepot.getDepotName());
					mbrWallet.setIsTransfer(MbrDepotWallet.IsTransFer.yes);
					return mbrWalletService.noExistInsert(mbrWallet, gmApi.getSiteCode());
				}
			}
			throw new RRException("188创建第三方账号失败!");
		} else {
			return mbrWallet;
		}
	}

	private String getRegisterMember188Dto(TGmApi gmApi, String loginName) {
		RegisterMember188Dto dto = new RegisterMember188Dto();
		dto.setLoginName(gmApi.getPrefix() + loginName);
		dto.setCurrencyCode(T188Constants.Currencies.rmb);
		dto.setOddsTypeId(T188Constants.Odds.hk);
		dto.setLangCode(T188Constants.Language.chs);
		dto.setTimeZone(T188Constants.REG_DEF_TIME);
		dto.setMemberStatus(T188Constants.Status.active);
		return CipherText.getEncrypt(dto.toString(), gmApi.getMd5Key());
	}

	/**
	 *
	 * @param gmApi
	 * @param loginName
	 * @return
	 */
	public PtUserInfo getBalance(TGmApi gmApi, String loginName) {
		String param = getBalance188Dto(gmApi, loginName);
		String result = okHttpService.httpSoapPost(gmApi.getPcUrl() + T188Constants.Mod.balance, param);
		log.debug(" T188—>余额查询 [会员账号{}提交参数{}返回结果{}]",loginName,gmApi.getPcUrl() + T188Constants.Mod.balance +" "+param,result);
		if (!StringUtils.isEmpty(result)) {
			GetT188Resp resp = getT188Resp(gmApi, result);
			if (resp.getReturnCode().equals(T188Constants.SUC_CODE)) {
				PtUserInfo info = new PtUserInfo();
				info.setBALANCE(BigDecimalMath.numberFormat(resp.getBalance()));
				info.setCURRENCY(T188Constants.Currencies.cny);
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
	public PtUserInfo getBalance(TGmApi gmApi, Integer accountId, String loginName) {
		createMember(gmApi, accountId, loginName);
		return getBalance(gmApi, loginName);
	}

	private String getBalance188Dto(TGmApi gmApi, String loginName) {
		Balance188Dto dto = new Balance188Dto();
		dto.setLoginName(gmApi.getPrefix() + loginName);
		return CipherText.getEncrypt(dto.toString(), gmApi.getMd5Key());
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
		String param = getDeposit188Dto(gmApi, loginName, amount, tranid);
		String result = okHttpService.httpSoapPost(gmApi.getPcUrl() + T188Constants.Mod.deposit, param);
		log.debug(" T188—>存款 [会员账号{}提交参数 {}返回结果 {}]",loginName,gmApi.getPcUrl() + T188Constants.Mod.deposit +" "+param,result);
		if (!StringUtils.isEmpty(result)) {
			GetT188Resp resp = getT188Resp(gmApi, result);
			if (resp.getReturnCode().equals(T188Constants.SUC_CODE)) {
				return TransferStates.suc;
			} else {
				return TransferStates.fail;
			}
		} else {
			return TransferStates.fail;
		}
	}

	private String getDeposit188Dto(TGmApi gmApi, String loginName, BigDecimal amount, String tranid) {
		Deposit188Dto dto = new Deposit188Dto();
		dto.setLoginName(gmApi.getPrefix() + loginName);
		dto.setAmount(String.valueOf(amount));
		dto.setReferenceNo(tranid);
		return CipherText.getEncrypt(dto.toString(), gmApi.getMd5Key());
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
		String param = getWithdraw188Dto(gmApi, loginName, amount, tranid);
		String result = okHttpService.httpSoapPost(gmApi.getPcUrl() + T188Constants.Mod.withdraw, param);
		log.debug(" T188—>取款 [会员账号{}提交参数 {}返回结果 {}]",loginName,gmApi.getPcUrl() + T188Constants.Mod.withdraw +" "+param,result);
		if (!StringUtils.isEmpty(result)) {
			GetT188Resp resp = getT188Resp(gmApi, result);
			if (resp.getReturnCode().equals(T188Constants.SUC_CODE)) {
				return TransferStates.suc;
			} else {
				return TransferStates.fail;
			}
		} else {
			return TransferStates.fail;
		}
	}

	private String getWithdraw188Dto(TGmApi gmApi, String loginName, BigDecimal amount, String tranid) {
		Withdraw188Dto dto = new Withdraw188Dto();
		dto.setLoginName(gmApi.getPrefix() + loginName);
		dto.setAmount(String.valueOf(amount));
		dto.setReferenceNo(tranid);
		return CipherText.getEncrypt(dto.toString(), gmApi.getMd5Key());
	}

	/**
	 * 000|001|006(Cannot find data for this Reference No.)
	 * 只有成功与失败两种奖态
	 * @param tranid
	 * @param gmApi
	 * @return
	 */
	public Integer checktransaction(String tranid, TGmApi gmApi) {
		String param = getTransferStatus188Dto(gmApi, tranid);
		String result = okHttpService.httpSoapPost(gmApi.getPcUrl() + T188Constants.Mod.transferStatus, param);
		log.debug(" T188—>订单查询 [提交参数 {}返回结果 {}]",gmApi.getPcUrl() + T188Constants.Mod.transferStatus +" "+param,result);
		if (!StringUtils.isEmpty(result)) {
			GetT188Resp resp = getT188Resp(gmApi, result);
			if (resp.getReturnCode().equals(T188Constants.SUC_CODE)) {
				return TransferStates.suc;
			} else {
				return TransferStates.fail;
			}
		}
		throw new RRException("查询订单失败,请稍后重试!");
	}

	private String getTransferStatus188Dto(TGmApi gmApi, String tranid) {
		TransferStatus188Dto dto = new TransferStatus188Dto();
		dto.setReferenceNo(tranid);
		return CipherText.getEncrypt(dto.toString(), gmApi.getMd5Key());
	}

	/**
	 *
	 * @param gmApi
	 * @param tgmGame
	 * @param accountId
	 * @param loginName
	 * @return
	 */
	public String generateUrl(TGmApi gmApi, TGmGame tgmGame,  Integer accountId, String loginName) {
		createMember(gmApi, accountId, loginName);
		log.debug(" T188—>游戏跳转 [提交参数 ]");
		return "";
	}

	private GetT188Resp getT188Resp(TGmApi gmApi, String result) {
		String context = CipherText.getDecrypt(result, gmApi.getMd5Key());
		GetT188Resp resp = new GetT188Resp();
		XmlUtil.t188Resp(resp, context);
		return resp;
	}

}
