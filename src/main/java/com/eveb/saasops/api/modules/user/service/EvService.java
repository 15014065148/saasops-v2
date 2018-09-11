package com.eveb.saasops.api.modules.user.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.eveb.saasops.api.constants.AginConstants;
import com.eveb.saasops.api.constants.ApiConstants.TransferStates;
import com.eveb.saasops.api.constants.EvConstants;
import com.eveb.saasops.api.constants.MgConstants.DetailsResKey;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.user.dto.AgResDto;
import com.eveb.saasops.api.modules.user.dto.EvDataDto;
import com.eveb.saasops.api.modules.user.dto.PtUserInfo;
import com.eveb.saasops.api.utils.MD5;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.service.MbrWalletService;
import com.eveb.saasops.modules.operate.entity.TGmDepot;
import com.eveb.saasops.modules.operate.service.TGmDepotService;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EvService {
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
	 * @param accountId
	 * @param loginName
	 * @return
	 */
	public MbrDepotWallet checkOrCreateGameAccout(TGmApi gmApi, Integer accountId, String loginName) {
		MbrDepotWallet mbrWallet = aginService.getDepotWallet(accountId, gmApi.getDepotId(), gmApi.getSiteCode(),loginName);
		if (!StringUtils.isEmpty(mbrWallet.getIsBuild()) && mbrWallet.getIsBuild() == false) {
				EvDataDto evDataDto = getEvDataDto(mbrWallet, gmApi);
				evDataDto.setMethod(EvConstants.EV_FUN_CHECKORCREATEGAMEACCOUT);
				String result = getResult(gmApi, evDataDto);
				log.debug(" EG—>会员注册 [会员名{}提交参数{}返回结果{}]",loginName,new Gson().toJson(evDataDto),result);
				AgResDto responseDto = new Gson().fromJson(result, AgResDto.class);
				if (responseDto.getInfo().equals(EvConstants.Res.succ)) {
					if (!StringUtils.isEmpty(mbrWallet.getIsBuild()) && mbrWallet.getIsBuild() == false) {
						TGmDepot tGmDepot = tGmDepotService.queryObject(gmApi.getDepotId());
						mbrWallet.setLoginName(mbrWallet.getLoginName());
						mbrWallet.setDepotName(tGmDepot.getDepotName());
						mbrWallet.setIsTransfer(MbrDepotWallet.IsTransFer.yes);
						mbrWalletService.noExistInsert(mbrWallet, gmApi.getPrefix());
					}
				}else {
					throw new RRException("Ev创建或检测第三方账号失败,或第三方账号已注册!");
				}
		}
		return mbrWallet;
	}
	
    private EvDataDto getEvDataDto(MbrDepotWallet mbrWallet, TGmApi gmApi) {
    	EvDataDto evDataDto = new EvDataDto();
    	evDataDto.setLoginname(gmApi.getPrefix() + mbrWallet.getLoginName());
    	evDataDto.setPassword(MD5.getMD5(mbrWallet.getPwd()));
    	evDataDto.setCur(EvConstants.CurTpye.curCny);
    	evDataDto.setOddtype(EvConstants.OddTpye.OddA);
    	evDataDto.setCagent(gmApi.getAgyAcc());
    	evDataDto.setLimit(EvConstants.CreateAccoutParam.limit);
    	evDataDto.setLimitroulette(EvConstants.CreateAccoutParam.limitroulette);
    	evDataDto.setLimitvideo(EvConstants.CreateAccoutParam.limitvideo);
    	//精确到秒
    	evDataDto.setTime(String.valueOf(System.currentTimeMillis()/ 1000));
        return evDataDto;
    }
    /**
     * 余额查询
     *
     * @param mbrWallet
     * @param gmApi
     * @return
     */
	public PtUserInfo getBalance(MbrDepotWallet mbrWallet, TGmApi gmApi) {
		EvDataDto evDataDto = getEvDataDto(mbrWallet, gmApi);
		evDataDto.setMethod(EvConstants.EV_FUN_GETBALANCE);
		try {
			String result = getResult(gmApi, evDataDto);
			log.debug(" EG—>余额查询 [会员名{}提交参数{}返回结果{}]",mbrWallet.getLoginName(),new Gson().toJson(evDataDto),result);
			AgResDto responseDto = new Gson().fromJson(result, AgResDto.class);
			PtUserInfo info = new PtUserInfo();
			if (Double.parseDouble(responseDto.getInfo()) >= 0){
				info.setBALANCE(String.valueOf(responseDto.getInfo()));
				info.setCURRENCY(DetailsResKey.currency);
			}
			return info;
		} catch (Exception ex) {
			throw new RRException("ev余额查询,其它失败!");
		}
	}

    /**
     * 预备转账接口
     *
     * @param mbrWallet
     * @param gmApi
     * @param type
     * @param amount
     * @return
     */
	public EvDataDto prepareTransferCredit(MbrDepotWallet mbrWallet, TGmApi gmApi, String type, double amount,
			String sequence) {
		EvDataDto evDataDto = getEvDataDto(mbrWallet, gmApi);
		evDataDto.setMethod(EvConstants.EV_FUN_PREPARETRANSFERCREDIT);
		evDataDto.setBillno(sequence);
		evDataDto.setType(type);
		evDataDto.setCredit(amount);
		String result = getResult(gmApi, evDataDto);
		log.debug(" EG—>预转账 [会员名{}提交参数{}返回结果{}]",mbrWallet.getLoginName(),new Gson().toJson(evDataDto),result);
		if (!StringUtils.isEmpty(result)) {
			AgResDto evResDto = new Gson().fromJson(result, AgResDto.class);
			if (evResDto == null || !evResDto.getInfo().equals(EvConstants.Res.succ)) {
				evDataDto.setFlag(TransferStates.fail);
			} else {
				evDataDto.setFlag(TransferStates.suc);
			}
		}else {
			evDataDto.setFlag(TransferStates.fail);
		}
		return evDataDto;
	}

	/**
	 * 确认转账
	 * @param gmApi
	 * @param evDataDto
	 * @return
	 */
	public Integer transferCreditConfirm(TGmApi gmApi, EvDataDto evDataDto) {
		evDataDto.setMethod(EvConstants.EV_FUN_TRANSFERCREDITCONFIRM);
		String result = getResult(gmApi, evDataDto);
		log.debug(" EG—>确认预转账 [会员账号{}|提交参数{}|返回结果]",evDataDto.getLoginname().substring(gmApi.getPrefix().length()-1),new Gson().toJson(evDataDto),result);
		if (!StringUtils.isEmpty(result)) {
			AgResDto evResDto = new Gson().fromJson(result, AgResDto.class);
			if (evResDto == null || !evResDto.getInfo().equals(AginConstants.Res.succ)) {
				return TransferStates.fail;
			} else {
				return TransferStates.suc;
			}
		} else {
			return TransferStates.fail;
		}
	}

	public Integer queryOrderStatus(Long billNo, TGmApi gmApi) {
		EvDataDto evDataDto = new EvDataDto();
		evDataDto.setCagent(gmApi.getAgyAcc());
		evDataDto.setBillno(String.valueOf(billNo));
		evDataDto.setMethod(EvConstants.EV_FUN_QUERYORDERSTATUS);
		evDataDto.setTime(String.valueOf(System.currentTimeMillis() / 1000));
		String result = getResult(gmApi, evDataDto);
		log.debug(" EG—>订单查询 [订单号{}|提交参数{}|返回结果{}]",billNo,new Gson().toJson(evDataDto),result);
		// 0 成功
		// 1 失败, 订单未处理状态 为失败则在 5秒后调用transferCreditConfirm 接口
		if (!StringUtils.isEmpty(result)) {
			// 0 成功
			// 1 失败, 订单未处理状态
			AgResDto agResDto = new Gson().fromJson(result, AgResDto.class);
			if (agResDto != null && agResDto.getInfo().equals(AginConstants.Res.succ)) {
				return TransferStates.suc;
			} else if (agResDto != null) {
				return TransferStates.fail;
			}
		}
		throw new RRException("查询订单状态失败!");
	}
	

    /**
     * 根据游戏Id 产生URL地址
     *
     * @param accountId
     * @param loginName //* @param weburl
     * @return
     */
	public String generateUrl(TGmApi gmApi, Integer accountId, String loginName, Byte terminal) {
		MbrDepotWallet mbrWallet = checkOrCreateGameAccout(gmApi, accountId, loginName);
		EvDataDto evDataDto = getEvGmDataDto(mbrWallet, gmApi);
		try {
			String result = getResult(gmApi, evDataDto, EvConstants.EV_M_FORWARDGAME);
			AgResDto responseDto = new Gson().fromJson(result, AgResDto.class);
			log.debug(" EG—>游戏跳转 [会员账号{}返回结果{}",loginName,result);
			return responseDto.getInfo();
		} catch (Exception e) {
			throw new RRException("获取游戏链接失败!");
		}
	}

	private EvDataDto getEvGmDataDto(MbrDepotWallet mbrWallet, TGmApi gmApi) {
		EvDataDto evDataDto = new EvDataDto();
		evDataDto.setCagent(gmApi.getAgyAcc());
		evDataDto.setLoginname(gmApi.getPrefix() + mbrWallet.getLoginName());
		evDataDto.setPassword(MD5.getMD5(mbrWallet.getPwd()));
		evDataDto.setDm(EvConstants.EV_DM);
		evDataDto.setLang(EvConstants.LangCode.cn_code);
		evDataDto.setOddtype(EvConstants.OddTpye.OddA);
		evDataDto.setGameType(EvConstants.GameType.hall_0);
		evDataDto.setSid(gmApi.getAgyAcc() + CommonUtil.genRandomNum(13, 16));
		evDataDto.setToken("");
		//精确到秒
		evDataDto.setTime(String.valueOf(System.currentTimeMillis() / 1000));
		return evDataDto;
	}

	private String getResult(TGmApi gmApi, EvDataDto evDataDto, String mod) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(gmApi.getPcUrl()).append(mod == null ? EvConstants.EV_M_DOBUSINESS : mod)
				.append(getParams(gmApi, evDataDto));
		Map<String, String> headParams = new HashMap<String, String>();
		headParams.put(EvConstants.EV_USER_AGENT, EvConstants.EV_USER_AGENT_VAL + gmApi.getAgyAcc());
		/*if (mod!=null&&mod.equals(EvConstants.EV_M_FORWARDGAME)) {
			return HttpsRequestUtil.evPost(stringBuffer.toString(),evDataDto.getParams(),headParams);
		} else {*/
			return okHttpService.evPost(stringBuffer.toString(), headParams);
		//}
	}
    private String getResult(TGmApi gmApi, EvDataDto evDataDto) {
    	return getResult( gmApi,evDataDto,null);
    }


	private String getParams(TGmApi gmApi, EvDataDto evDataDto) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("params=").append(evDataDto.getEncrypt(gmApi.getMd5Key()));
		return stringBuffer.toString();
	}

	/**
	 * 登出
	 *
	 * @param gmApi
	 * @param loginName
	 * @return
	 */
	public boolean logOut(TGmApi gmApi, String loginName) {
		boolean rsCode = false;
		EvDataDto evDataDto = new EvDataDto();
		evDataDto.setCagent(gmApi.getAgyAcc());
		evDataDto.setLoginname(gmApi.getPrefix() + loginName);
		evDataDto.setMethod(EvConstants.EV_FUN_LOGOUT);
		//精确到秒
		evDataDto.setTime(String.valueOf(System.currentTimeMillis()/ 1000));
		try {
			String result = getResult(gmApi, evDataDto);
			log.debug(" EG—>登出 [会员名{}提交参数{}返回结果{}]",loginName,new Gson().toJson(evDataDto),result);
			AgResDto responseDto = new Gson().fromJson(result, AgResDto.class);
			if (Double.parseDouble(responseDto.getInfo()) >= 0){
				rsCode=true;
			}
		} catch (Exception ex) {
			throw new RRException("ev登出失败!");
		}
		return rsCode;
	}
}
