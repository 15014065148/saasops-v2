package com.eveb.saasops.api.modules.user.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.constants.ApiConstants.TransferStates;
import com.eveb.saasops.api.constants.IbcConstants;
import com.eveb.saasops.api.constants.IbcConstants.Json;
import com.eveb.saasops.api.constants.T188Constants;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.user.dto.IbcBetLimitData;
import com.eveb.saasops.api.modules.user.dto.IbcBetLimitRes;
import com.eveb.saasops.api.modules.user.dto.IbcDto;
import com.eveb.saasops.api.modules.user.dto.IbcRes;
import com.eveb.saasops.api.modules.user.dto.IbcTransferRes;
import com.eveb.saasops.api.modules.user.dto.PtUserInfo;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.BigDecimalMath;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.service.MbrWalletService;
import com.eveb.saasops.modules.operate.entity.TGmDepot;
import com.eveb.saasops.modules.operate.entity.TGmGame;
import com.eveb.saasops.modules.operate.service.TGmDepotService;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IbcService {
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
			String param = getIbcCreateMemDto(gmApi, loginName);
			String result = okHttpService.get(gmApi.getPcUrl() + param);
			log.debug(" IBC—>创建账号 [会员账号{}提交参数{}返回结果{}]",loginName,gmApi.getPcUrl(),result);

			if (!StringUtils.isEmpty(result)) {
				IbcRes ibcRes = new Gson().fromJson(result, IbcRes.class);
				if (ibcRes.getError_code().equals(IbcConstants.ERROR_CODE)) {
					// 必须设置这个不然进入游戏显示不了余额
					// 设置账户基本信息
					conMemBetSet(gmApi, loginName);
					TGmDepot tGmDepot = tGmDepotService.queryObject(mbrWallet.getDepotId());
					mbrWallet.setDepotName(tGmDepot.getDepotName());
					mbrWallet.setIsTransfer(MbrDepotWallet.IsTransFer.yes);
					return mbrWalletService.noExistInsert(mbrWallet, gmApi.getSiteCode());
				}
			}
			throw new RRException("IBC创建第三方账号失败!");
		} else {
			return mbrWallet;
		}
	}

	private String getIbcCreateMemDto(TGmApi gmApi, String loginName) {
		IbcDto dto = new IbcDto();
		dto.setOpCode(gmApi.getAgyAcc());
		dto.setPlayerName(gmApi.getPrefix() + loginName);
		dto.setOddsType(IbcConstants.Odds.hk);
		dto.setMinTransfer(IbcConstants.Transfer.MinTransfer);
		dto.setMaxTransfer(IbcConstants.Transfer.MaxTransfer);
		return dto.getParams(gmApi.getMd5Key(),IbcConstants.Mod.createMember);
	}

	/**
	 *
	 * @param gmApi
	 * @param loginName
	 * @return
	 */
	public PtUserInfo getBalance(TGmApi gmApi, String loginName) {
		String param = getDto(gmApi, loginName, IbcConstants.Mod.balance);
		String result = okHttpService.get(gmApi.getPcUrl() + param);
		log.debug(" IBC—>余额查询 [会员账号{}提交参数{}返回结果{}]",loginName,gmApi.getPcUrl()+" "+param,result);
		if (!StringUtils.isEmpty(result)) {
			IbcRes ibcRes = new Gson().fromJson(result, IbcRes.class);
			if (ibcRes.getError_code().equals(IbcConstants.ERROR_CODE)) {
				PtUserInfo info = new PtUserInfo();
				info.setBALANCE(BigDecimalMath.numberFormat(String.valueOf(ibcRes.getData().get(0).getBalance())));
				info.setCURRENCY(T188Constants.Currencies.cny);
				return info;
			}
		}
		throw new RRException("暂停服务,请稍后重试!");
	}

	public PtUserInfo getBalance(TGmApi gmApi, Integer accountId, String loginName) {
		createMember(gmApi, accountId, loginName);
		return getBalance(gmApi, loginName);
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
		String param = getDto(gmApi, loginName, IbcConstants.Mod.login);
		String result = okHttpService.get(gmApi.getPcUrl() + param);
		log.debug(" IBC—>登陆 [会员账号{}提交参数{}返回结果{}]",loginName,gmApi.getPcUrl()+" "+param,result);
		if (!StringUtils.isEmpty(result)) {
			IbcRes ibcRes = new Gson().fromJson(result, IbcRes.class);
			if (ibcRes.getError_code().equals(IbcConstants.ERROR_CODE)) {
				mbrWallet.setToken(ibcRes.getSessionToken());
			} else {
				throw new RRException("IBC登陆失败!");
			}
		} else {
			throw new RRException("暂停服务,请稍后重试!");
		}
		return mbrWallet;
	}

	private String getDto(TGmApi gmApi, String loginName,String mod) {
		IbcDto dto = new IbcDto();
		dto.setOpCode(gmApi.getAgyAcc());
		dto.setPlayerName(gmApi.getPrefix() + loginName);
		return dto.getParams(gmApi.getMd5Key(), mod);
	}

	/**
	 *
	 * @param gmApi
	 * @return
	 */
	public IbcBetLimitRes getBetSetLimit(TGmApi gmApi) {
		String param = getBetSetLimitDto(gmApi,IbcConstants.Mod.getBetSetLimit);
		String result = okHttpService.get(gmApi.getPcUrl() + param);
		if (!StringUtils.isEmpty(result)) {
			IbcBetLimitRes ibcRes = new Gson().fromJson(result, IbcBetLimitRes.class);
			if (ibcRes.getError_code().equals(IbcConstants.ERROR_CODE)) {
				return ibcRes;
			} else {
				throw new RRException("IBC登陆失败!");
			}
		} else {
			throw new RRException("暂停服务,请稍后重试!");
		}
	}

	private String getBetSetLimitDto(TGmApi gmApi,String mod) {
		IbcDto dto = new IbcDto();
		dto.setOpCode(gmApi.getAgyAcc());
		dto.setCurrency(IbcConstants.Currencies.RMB);
		return dto.getParams(gmApi.getMd5Key(), mod);
	}

	/**
	 *
	 * @param gmApi
	 * @param loginName
	 * @return
	 */
	public Boolean preMemBetSet(TGmApi gmApi, String loginName) {
		IbcBetLimitRes ibcBetLimitRes = getBetSetLimit(gmApi);
		List<String> list = getGames(gmApi.getSecureCodes().get(Json.gameids));
		list.forEach(gameId -> {
			IbcBetLimitData data = getLimitData(gameId, ibcBetLimitRes.getData());
			if (!StringUtils.isEmpty(data)) {
				String param = getPreMemBetSetDto(gmApi, loginName, IbcConstants.Mod.preMemBetSet, data);
				String result = okHttpService.get(gmApi.getPcUrl() + param);
				log.debug(" IBC—>会员注册后游戏初始化 [会员账号{}提交参数{}返回结果{}]",loginName,gmApi.getPcUrl()+" "+param,result);
				//System.out.println(gameId =" "+result);
/*				if (!StringUtils.isEmpty(result)) {
					IbcRes ibcRes = new Gson().fromJson(result, IbcRes.class);
					if (ibcRes.getError_code().equals(IbcConstants.ERROR_CODE)) {
						// return Boolean.TRUE;
					} else {
						// return Boolean.FALSE;
					}
				} else {
					// return Boolean.FALSE;
				}*/
			}
		});
		return Boolean.TRUE;
	}
    private List<String> getGames(String splitStr)
    {
    	return Arrays.asList(splitStr.split(","));
    }

	private IbcBetLimitData getLimitData(String sportType, ArrayList<IbcBetLimitData> data) {
		for (IbcBetLimitData limitData : data) {
			if (sportType.equals(limitData.getSport_type()))
				return limitData;
		}
		return null;
	}

	private String getPreMemBetSetDto(TGmApi gmApi, String loginName, String mod, IbcBetLimitData data) {
		IbcDto dto = new IbcDto();
		dto.setOpCode(gmApi.getAgyAcc());
		dto.setPlayerName(gmApi.getPrefix() + loginName);
		dto.setSportType(data.getSport_type());
		dto.setMinBet(data.getMin_bet().intValue());
		dto.setMaxBet(data.getMax_bet().intValue());
		dto.setMaxBetPerMatch(data.getMax_bet_per_match().intValue());
		if (data.getSport_type().equals(IbcConstants.GAME_161))
			dto.setMaxBetPerMatch(data.getMax_bet_per_match().intValue());
		return dto.getParams(gmApi.getMd5Key(), mod);
	}

	/**
	 *
	 * @param gmApi
	 * @param loginName
	 * @return
	 */
	public Boolean conMemBetSet(TGmApi gmApi, String loginName) {
		if (preMemBetSet(gmApi, loginName)) {
			String param = getConMemBetSetDto(gmApi, loginName, IbcConstants.Mod.conMemBetSet);
			String result = okHttpService.get(gmApi.getPcUrl() + param);
			log.debug(" IBC—>游戏参数初始化确认 [会员账号{}提交参数{}返回结果{}]",loginName,gmApi.getPcUrl()+" "+param,result);
			if (!StringUtils.isEmpty(result)) {
				IbcRes ibcRes = new Gson().fromJson(result, IbcRes.class);
				if (ibcRes.getError_code().equals(IbcConstants.ERROR_CODE)) {
					return Boolean.TRUE;
				}
			}
		}
		throw new RRException("暂停服务,请稍后重试!");
	}

	private String getConMemBetSetDto(TGmApi gmApi,String loginName,String mod) {
		IbcDto dto = new IbcDto();
		dto.setOpCode(gmApi.getAgyAcc());
		dto.setPlayerName(gmApi.getPrefix()+loginName);
		return dto.getParams(gmApi.getMd5Key(), mod);
	}


	/**
	 *
	 * @param gmApi
	 * @param loginName
	 * @param amount
	 * @param tranid
	 * @param direction
	 * @return
	 */
	public Integer transfer(TGmApi gmApi, String loginName, BigDecimal amount, String tranid, Integer direction) {
		String param = getDepositDto(gmApi, loginName, amount, tranid, direction);
		String result = okHttpService.get(gmApi.getPcUrl() + param);
		log.debug(" IBC—>转账 [会员账号{}提交参数{}返回结果{}]",loginName,gmApi.getPcUrl()+" "+param,result);
		if (!StringUtils.isEmpty(result)) {
			IbcTransferRes ibcRes = new Gson().fromJson(result, IbcTransferRes.class);
			if (ibcRes.getError_code().equals(IbcConstants.ERROR_CODE)) {
				return ibcRes.getData().getStatus();
			} else {
				return TransferStates.fail;
			}
		} else {
			return TransferStates.fail;
		}
	}

	private String getDepositDto(TGmApi gmApi, String loginName, BigDecimal amount, String tranid, Integer direction) {
		IbcDto dto = new IbcDto();
		dto.setOpCode(gmApi.getAgyAcc());
		dto.setPlayerName(gmApi.getPrefix() + loginName);
		dto.setOpTransId(tranid);
		dto.setAmount(amount);
		if (direction == IbcConstants.Transfer.toIn) {
			dto.setDirection(IbcConstants.Transfer.toIn);
			return dto.getParams(gmApi.getMd5Key(), IbcConstants.Mod.deposit);
		} else {
			dto.setDirection(IbcConstants.Transfer.toOut);
			return dto.getParams(gmApi.getMd5Key(), IbcConstants.Mod.withdraw);
		}
	}

	public Integer deposit(TGmApi gmApi, String loginName, BigDecimal amount, String tranid) {
		return transfer(gmApi, loginName, amount, tranid, IbcConstants.Transfer.toIn);
	}

	public Integer withdraw(TGmApi gmApi, String loginName, BigDecimal amount, String tranid) {
		return transfer(gmApi, loginName, amount, tranid, IbcConstants.Transfer.toOut);
	}


	/**
	 * 0 还行 成功执行
		1 失败 系统错误
		2 挂起 状态未知，调用
	 * @param tranid
	 * @param gmApi
	 * @return
	 */
	public Integer checktransaction(String tranid, String loginName, TGmApi gmApi) {
		String param = getTransferStatusDto(gmApi, loginName, tranid);
		String result = okHttpService.get(gmApi.getPcUrl() + param);
		log.debug(" IBC—>订单查询 [会员账号{}提交参数{}返回结果{}]",loginName,gmApi.getPcUrl()+" "+param,result);
		if (!StringUtils.isEmpty(result)) {
			IbcTransferRes ibcRes = new Gson().fromJson(result, IbcTransferRes.class);
			if (ibcRes.getError_code().equals(IbcConstants.ERROR_CODE)) {
				return ibcRes.getData().getStatus();
			} else {
				return TransferStates.fail;
			}
		} else {
			throw new RRException("暂停服务,请稍后重试!");
		}
	}

	private String getTransferStatusDto(TGmApi gmApi,String loginName, String tranid) {
		IbcDto dto = new IbcDto();
		dto.setOpCode(gmApi.getAgyAcc());
		dto.setPlayerName(gmApi.getPrefix() + loginName);
		dto.setOpTransId(tranid);
		return dto.getParams(gmApi.getMd5Key(), IbcConstants.Mod.trfrStatus);
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
		MbrDepotWallet wallet = login(gmApi, accountId, loginName);
		// 没有登陆的时候的URL http://mkt.gsoft-ib.com/vender.aspx?lang=cs
		if (terminal == ApiConstants.Terminal.pc) {
			log.debug(" IBC—>PC端游戏跳转 [会员账号{}提交参数{}]",loginName,gmApi.getPcUrl2() + wallet.getToken());
			return gmApi.getPcUrl2() + wallet.getToken();
		} else {
			log.debug(" IBC—>MB端游戏跳转 [会员账号{}提交参数 {}]",loginName,gmApi.getMbUrl() + wallet.getToken());
			return gmApi.getMbUrl() + wallet.getToken();
		}
	}

	/**
	 * 登出
	 * @param gmApi
	 * @param loginName
	 * @return
	 */
	public boolean logOut(TGmApi gmApi, String loginName) {
		boolean rsCode = false;
		String param = getDto(gmApi, loginName, IbcConstants.Mod.logOut);
		String result = okHttpService.get(gmApi.getPcUrl() + param);
		log.debug(" IBC—>登出 [会员账号{}提交参数{}返回结果{}]",loginName,gmApi.getPcUrl()+" "+param,result);
		if (!StringUtils.isEmpty(result)) {
			IbcRes ibcRes = new Gson().fromJson(result, IbcRes.class);
			if (ibcRes.getError_code().equals(IbcConstants.ERROR_CODE)||ibcRes.getError_code().equals(IbcConstants.ERROR_CODE_ACCUNLOGIN)) {
				rsCode = true;
			}
		} else {
			throw new RRException("暂停服务,请稍后重试!");
		}
		return rsCode;
	}
}
