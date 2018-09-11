package com.eveb.saasops.api.modules.user.service;

import com.eveb.saasops.api.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.eveb.saasops.api.constants.AginConstants;
import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.constants.ApiConstants.TransferStates;
import com.eveb.saasops.api.constants.MgConstants.DetailsResKey;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.user.dto.AGDataDto;
import com.eveb.saasops.api.modules.user.dto.AGDataDto.Mh5;
import com.eveb.saasops.api.modules.user.dto.AgResDto;
import com.eveb.saasops.api.modules.user.dto.PtUserInfo;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.XmlUtil;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.service.MbrWalletService;
import com.eveb.saasops.modules.operate.entity.TGmDepot;
import com.eveb.saasops.modules.operate.entity.TGmGame;
import com.eveb.saasops.modules.operate.service.TGmDepotService;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AginService {
    @Autowired
    MbrWalletService mbrWalletService;
    @Autowired
    private TGmDepotService tGmDepotService;
    @Autowired
    private OkHttpService okHttpService;
    @Resource(name = "redisTemplate")
    RedisTemplate<String, Object>  redisTemplate;

    /**
     * @param gmApi
     * @param accountId
     * @param loginName
     * @return
     */
    public MbrDepotWallet checkOrCreateGameAccout(TGmApi gmApi, Integer accountId, String loginName) {
        MbrDepotWallet mbrWallet = getDepotWallet(accountId, gmApi.getDepotId(), gmApi.getSiteCode(), loginName);
        if (!StringUtils.isEmpty(mbrWallet.getIsBuild()) && mbrWallet.getIsBuild() == false) {
            AGDataDto agDataDto = getAgDataDto(mbrWallet, gmApi);
            agDataDto.setMethod(AginConstants.AGIN_FUN_CHECKORCREATEGAMEACCOUT);
            String result = getResult(gmApi, agDataDto);
            log.debug(" ag—>会员注册 [会员账号为{}|提交参数{}|返回结果 {}]", loginName, new Gson().toJson(agDataDto), result);
            AgResDto agResDto = XmlUtil.getAginResult(result);
            if (agResDto != null && agResDto.getInfo().equals(AginConstants.Res.succ)) {
                if (!StringUtils.isEmpty(mbrWallet.getIsBuild()) && mbrWallet.getIsBuild() == false) {
                    TGmDepot tGmDepot = tGmDepotService.queryObject(gmApi.getDepotId());
                    mbrWallet.setLoginName(mbrWallet.getLoginName());
                    mbrWallet.setDepotName(tGmDepot.getDepotName());
                    mbrWallet.setIsTransfer(MbrDepotWallet.IsTransFer.yes);
                    return mbrWalletService.noExistInsert(mbrWallet, gmApi.getPrefix());
                }
            }
            throw new RRException("AGIN创建或检测第三方账号失败!");
        } else {
            return mbrWallet;
        }
    }

    /**
     * @param mbrWallet
     * @param gmApi
     * @return
     */
    public PtUserInfo getBalance(MbrDepotWallet mbrWallet, TGmApi gmApi) {
        AGDataDto agDataDto = getAgDataDto(mbrWallet, gmApi);
        agDataDto.setMethod(AginConstants.AGIN_FUN_GETBALANCE);
        String result = getResult(gmApi, agDataDto);
        log.debug(" ag—>余额查询 [会员账号{}|提交参数{}|返回结果{}]", mbrWallet.getLoginName(), new Gson().toJson(agDataDto), result);
        AgResDto agResDto = XmlUtil.getAginResult(result);
        if (agResDto == null) {
            throw new RRException("查询第三方余额失败!");
        } else {
            PtUserInfo info = new PtUserInfo();
            if (!StringUtils.isEmpty(agResDto.getInfo())) {
                info.setBALANCE(agResDto.getInfo());
                info.setCURRENCY(DetailsResKey.currency);
                return info;
            }
            // AGIN 首次创建账号之后没有(Account not exist with this currency value or account
            // hierarchical error)
				/*if (agResDto.getInfo().equals(ApiConstants.AGIN_BALANCE_INFO)&& agResDto.getMsg().indexOf(ApiConstants.AGIN_BALANCE_MSG) != -1) {
					info.setBALANCE(String.valueOf(ApiConstants.USER_DEFAULT_BALANCE));
					info.setCURRENCY(DetailsResKey.currency);
					return info;
				} else {
					info.setBALANCE(agResDto.getInfo());
					info.setCURRENCY(DetailsResKey.currency);
					return info;
				}*/
        }
        throw new RRException("AGIN余额查询,其它失败!");
    }

    public void findOne() {
        System.out.println(redisTemplate.opsForValue().get("test001") + "==========================");
        redisTemplate.opsForValue().set("test001", "100", 10, TimeUnit.SECONDS);
        System.out.println(redisTemplate.opsForValue().get("test001") + "--------------------------");
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
    public AGDataDto prepareTransferCredit(MbrDepotWallet mbrWallet, TGmApi gmApi, String type, double amount,
                                           String sequence) {
        AGDataDto agDataDto = getAgDataDto(mbrWallet, gmApi);
        agDataDto.setMethod(AginConstants.AGIN_FUN_PREPARETRANSFERCREDIT);
        agDataDto.setBillno(agDataDto.getCagent() + sequence);
        agDataDto.setType(type);
        agDataDto.setCredit(amount);
        String result = getResult(gmApi, agDataDto);
        log.debug(" ag—>预转账 [会员账号{}|提交参数{}|返回结果{}]", mbrWallet.getLoginName(), new Gson().toJson(agDataDto), result);
        if (!StringUtils.isEmpty(result)) {
            AgResDto agResDto = XmlUtil.getAginResult(result);
            if (agResDto == null || !agResDto.getInfo().equals(AginConstants.Res.succ)) {
                agDataDto.setFlag(TransferStates.fail);
            } else {
                agDataDto.setFlag(TransferStates.suc);
            }
        } else {
            agDataDto.setFlag(TransferStates.fail);
        }
        return agDataDto;
    }

    /**
     * 确认转账
     *
     * @param gmApi
     * @param agDataDto
     */
    public Integer transferCreditConfirm(TGmApi gmApi, AGDataDto agDataDto) {
        agDataDto.setMethod(AginConstants.AGIN_FUN_TRANSFERCREDITCONFIRM);
        String result = getResult(gmApi, agDataDto);
        log.debug(" ag—>确认预转账 [会员账号{}|提交参数{}|返回结果]", agDataDto.getLoginname().substring(gmApi.getPrefix().length() - 1), new Gson().toJson(agDataDto), result);
        if (!StringUtils.isEmpty(result)) {
            AgResDto agResDto = XmlUtil.getAginResult(result);
            if (agResDto == null || !agResDto.getInfo().equals(AginConstants.Res.succ)) {
                return TransferStates.fail;
            } else {
                return TransferStates.suc;
            }
        } else {
            return TransferStates.fail;
        }
    }

    /**
     * 0 成功
     * 1 失败, 订单未处理状态
     * 2 因无效的转账金额引致的失败
     *
     * @param billNo
     * @param gmApi
     * @return
     */
    public Integer queryOrderStatus(Long billNo, TGmApi gmApi) {
        AGDataDto agDataDto = new AGDataDto();
        agDataDto.setCagent(gmApi.getAgyAcc());
        agDataDto.setBillno(agDataDto.getCagent() + String.valueOf(billNo));
        agDataDto.setMethod(AginConstants.AGIN_FUN_QUERYORDERSTATUS);
        agDataDto.setActype(AginConstants.Actype.trueAccount);
        agDataDto.setCur(AginConstants.CurTpye.curCny);
        String result = getResult(gmApi, agDataDto);
        log.debug(" ag—>订单查询 [订单号{}|提交参数{}|返回结果{}]", billNo.toString(), new Gson().toJson(agDataDto), result);
        if (!StringUtils.isEmpty(result)) {
            // 0 成功
            // 1 失败, 订单未处理状态
            AgResDto agResDto = XmlUtil.getAginResult(result);
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
     * @param tgmGame
     * @param accountId
     * @param loginName //* @param weburl
     * @return
     */
    public String generateUrl(TGmApi gmApi, TGmGame tgmGame, Integer accountId, String loginName, Byte terminal) {
        MbrDepotWallet mbrWallet = checkOrCreateGameAccout(gmApi, accountId, loginName);
        AGDataDto agDataDto = getAgDataDto(mbrWallet, gmApi);
        // agDataDto.setMethod(AginConstants.AGIN_FUN_CHECKORCREATEGAMEACCOUT);
        // agDataDto.setCagent(gmApi.getAgyAcc());
        // agDataDto.setDm("veb88.com");
        agDataDto.setSid(CommonUtil.genRandomNum(13, 16));
        agDataDto.setLang(AginConstants.LangCode.cn_code);
        if (terminal == ApiConstants.Terminal.pc) {
            agDataDto.setGameType(tgmGame.getGameParam());
        }
        if (terminal == ApiConstants.Terminal.mobile) {
            agDataDto.setGameType(tgmGame.getMbGameParam());
        }
        if (terminal == ApiConstants.Terminal.mobile)
            agDataDto.setMh5(Mh5.isMobile);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(gmApi.getPcUrl2()).append(AginConstants.AGIN_M_FORWARDGAME).append(
                getParams(gmApi.getSecureCodes().get(AginConstants.AGIN_DESCODE_KEY), gmApi.getMd5Key(), agDataDto));
        log.debug(" ag—>游戏跳转 [会员账号{}|提交参数 {}]", loginName, stringBuffer.toString());
        return stringBuffer.toString();
    }

    /**
     * @param accountId
     * @param depotId
     * @param siteCode
     * @param loginName
     * @return
     */
    public MbrDepotWallet getDepotWallet(Integer accountId, Integer depotId, String siteCode, String loginName) {
        MbrDepotWallet wallet = new MbrDepotWallet();
        wallet.setAccountId(accountId);
        wallet.setDepotId(depotId);
        wallet = mbrWalletService.queryObjectCond(wallet, siteCode);
        if (wallet == null) {
            wallet = new MbrDepotWallet();
            wallet.setLoginName(loginName);
            wallet.setAccountId(accountId);
            wallet.setPwd(CommonUtil.genRandomNum(6, 8));
            wallet.setDepotId(depotId);
            wallet.setIsBuild(Boolean.FALSE);
        } else {
            wallet.setIsBuild(Boolean.TRUE);
        }
        return wallet;
    }

    private String getResult(TGmApi gmApi, AGDataDto agDataDto) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(gmApi.getPcUrl()).append(AginConstants.AGIN_M_DOBUSINESS)
                .append(getParams(gmApi.getSecureCodes().get(AginConstants.AGIN_DESCODE_KEY), gmApi.getMd5Key(), agDataDto));
        //return okHttpProxyUtils.postJson(stringBuffer.toString(), null,okHttpProxyUtils.proxyClient);
        return okHttpService.post(stringBuffer.toString());
    }

    private AGDataDto getAgDataDto(MbrDepotWallet mbrWallet, TGmApi gmApi) {
        AGDataDto agDataDto = new AGDataDto();
        agDataDto.setLoginname(gmApi.getPrefix() + mbrWallet.getLoginName());
        agDataDto.setPassword(mbrWallet.getPwd());
        agDataDto.setCur(AginConstants.CurTpye.curCny);
        agDataDto.setActype(AginConstants.Actype.trueAccount);
        agDataDto.setOddtype(AginConstants.OddTpye.OddA);
        agDataDto.setCagent(gmApi.getAgyAcc());
        return agDataDto;
    }

    private String getParams(String secureCode, String md5Key, AGDataDto agDataDto) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            DESEncrypt d = new DESEncrypt(secureCode);
            String params = d.encrypt(agDataDto.toString());
            String key = MD5.getMD5(params + md5Key);
            stringBuffer.append("params=").append(params).append("&key=").append(key);
        } catch (Exception e) {
            throw new RRException("创建或检测第三方账号失败!");
        }
        return stringBuffer.toString();
    }
}
