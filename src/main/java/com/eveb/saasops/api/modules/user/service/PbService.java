package com.eveb.saasops.api.modules.user.service;

import com.alibaba.fastjson.JSON;
import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.constants.PbConstants;
import com.eveb.saasops.api.constants.PtNewConstants;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.user.dto.*;
import com.eveb.saasops.api.utils.CipherText;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.service.MbrWalletService;
import com.eveb.saasops.modules.operate.entity.TGmDepot;
import com.eveb.saasops.modules.operate.service.TGmDepotService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.eveb.saasops.api.constants.PbConstants.Json;
import com.eveb.saasops.api.constants.PbConstants.Mod;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PbService {
    @Autowired
    MbrWalletService mbrWalletService;
    @Autowired
    private AginService aginService;
    @Autowired
    private TGmDepotService tGmDepotService;
    @Autowired
    private  OkHttpService okHttpService;
    /**
     * 登陆
     *
     * @param gmApi
     * @param accountId
     * @param loginName
     * @return
     */
    public String login(TGmApi gmApi, Integer accountId, String loginName) {
        String param = getLoginDto(gmApi, loginName);
        String result = okHttpService.get(gmApi.getPcUrl() + Mod.login + param, getHeads(gmApi));
        log.debug("PB—>登陆 [提交参数{}] 返回结果{}]", gmApi.getPcUrl() + Mod.login + param, result);
        if (!StringUtils.isEmpty(result)) {
            Map<?, ?> maps = (Map<?, ?>) JSON.parse(result);
            if (maps != null && !StringUtils.isEmpty(maps.get("loginUrl"))) {
                return String.valueOf(maps.get("loginUrl"));
            }
        }
        throw new RRException("登陆异常!");
    }

    private String getLoginDto(TGmApi gmApi, String loginName) {
        PbDto dto = new PbDto();
        dto.setUserCode(loginName);
        dto.setLocale(PbConstants.Language.zhCn);
        return dto.toString();
    }

    /**
     * 建立会员
     *
     * @param gmApi
     * @param accountId
     * @param loginName
     * @return
     */
    public MbrDepotWallet createMember(TGmApi gmApi, Integer accountId, String loginName) {
        MbrDepotWallet mbrWallet = aginService.getDepotWallet(accountId, gmApi.getDepotId(), gmApi.getSiteCode(), loginName);
        if (!StringUtils.isEmpty(mbrWallet.getIsBuild()) && mbrWallet.getIsBuild() == false) {
            String param = getPlayerDto(gmApi, loginName);
            String result = okHttpService.get(gmApi.getPcUrl() + Mod.createMember + param, getHeads(gmApi));
            log.debug("PB—>创建账号 [提交参数{}] 返回结果{}]", gmApi.getPcUrl() + Mod.createMember + param, result);
            Map<?, ?> maps = (Map<?, ?>) JSON.parse(result);
            if (maps != null && !StringUtils.isEmpty(maps.get("loginId"))) {
                TGmDepot tGmDepot = tGmDepotService.queryObject(mbrWallet.getDepotId());
                mbrWallet.setDepotName(tGmDepot.getDepotName());
                mbrWallet.setIsTransfer(MbrDepotWallet.IsTransFer.yes);
                mbrWallet.setLoginId(String.valueOf(maps.get("loginId")));
                return mbrWalletService.noExistInsert(mbrWallet, gmApi.getSiteCode());
            }
        }
        return mbrWallet;
    }

    private String getPlayerDto(TGmApi gmApi, String loginName) {
        PbDto dto = new PbDto();
        dto.setAgentCode(gmApi.getAgyAcc());
        dto.setUserCode(gmApi.getPrefix() + loginName);
        dto.setLocale(PbConstants.Language.zhCn);
        return dto.toString();
    }

    /**
     * 余额查询
     *
     * @param gmApi
     * @param loginName
     * @return
     */

    public PtUserInfo getBalance(TGmApi gmApi, String loginName) {
        String param = getBalanceDto(loginName);
        String result = okHttpService.get(gmApi.getPcUrl() + Mod.balance + param, getHeads(gmApi));
        log.debug("PB—>余额查询 [提交参数{}] 返回结果{}]", gmApi.getPcUrl() + Mod.balance + param, result);
        if (!StringUtils.isEmpty(result)) {
            Map<?, ?> maps = (Map<?, ?>) JSON.parse(result);
            if (maps != null && !StringUtils.isEmpty(maps.get("availableBalance"))) {
                PtUserInfo info = new PtUserInfo();
                info.setBALANCE(String.valueOf(maps.get("availableBalance")));
                info.setCURRENCY(PtNewConstants.RegiterDefault.currency);
                return info;
            }
        }
        throw new RRException("暂停服务,请稍后重试!");
    }


    /**
     * 余额查询
     *
     * @param gmApi
     * @param accountId
     * @param loginName
     * @return
     */
    public PtUserInfo getBalance(TGmApi gmApi, Integer accountId, String loginName) {
        MbrDepotWallet mbrWallet = createMember(gmApi, accountId, loginName);
        return getBalance(gmApi, mbrWallet.getLoginId());
    }


    private String getBalanceDto(String loginId) {
        PbDto dto = new PbDto();
        dto.setUserCode(loginId);
        return dto.toString();
    }

    /**
     * 转账
     *
     * @param gmApi
     * @param loginName
     * @param amount
     * @param mod
     * @return
     */

    public Integer Transfer(TGmApi gmApi, String loginName, BigDecimal amount, String mod) {
        String param = getFundTransferDto(gmApi, loginName, amount);
        String result = okHttpService.get(gmApi.getPcUrl() + mod + param, getHeads(gmApi));
        log.debug("PB—>存款 [提交参数{}] 返回结果{}", gmApi.getPcUrl() + mod + param, result);
        if (!StringUtils.isEmpty(result)) {
            Map<?, ?> maps = (Map<?, ?>) JSON.parse(result);
            if (maps != null && !StringUtils.isEmpty(maps.get("availableBalance"))) {
                return ApiConstants.TransferStates.suc;
            }
        }
        return ApiConstants.TransferStates.fail;
    }


    private String getFundTransferDto(TGmApi gmApi, String loginName, BigDecimal amount) {
        PbDto dto = new PbDto();
        dto.setUserCode(loginName);
        dto.setAmount(amount);
        return dto.toString();
    }

    /**
     * 存款
     *
     * @param gmApi
     * @param loginName
     * @param amount
     * @return
     */
    public Integer deposit(TGmApi gmApi, String loginName, BigDecimal amount) {
        return Transfer(gmApi, loginName, amount, Mod.deposit);
    }

    /**
     * 取款
     *
     * @param gmApi
     * @param loginName
     * @param amount
     * @return
     */
    public Integer withdraw(TGmApi gmApi, String loginName, BigDecimal amount) {
        return Transfer(gmApi, loginName, amount, Mod.withdraw);
    }

    /**
     * 游戏跳转
     *
     * @param gmApi
     * @param accountId
     * @param loginName
     * @param terminal
     * @return
     */
    public String generateUrl(TGmApi gmApi, Integer accountId, String loginName, Byte terminal) {
        MbrDepotWallet mbrWallet = createMember(gmApi, accountId, loginName);
        String url = login(gmApi, accountId, mbrWallet.getLoginId());
        return url;
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
        String param = getLoginDto(gmApi, loginName);
        String result = okHttpService.get(gmApi.getPcUrl() + Mod.login + param, getHeads(gmApi));
        log.debug("PB—>登出 [提交参数{}] 返回结果{}]", gmApi.getPcUrl() + Mod.logOut + param, result);
        try {
            if (!StringUtils.isEmpty(result)) {
                Map<?, ?> maps = (Map<?, ?>) JSON.parse(result);
                if (maps != null && !StringUtils.isEmpty(maps.get("status")) && maps.get("status").equals("successful")) {
                    rsCode = true;
                }
            }
        } catch (Exception ex) {
            throw new RRException("ev登出失败!");
        }
        return rsCode;
    }

    /**
     * 产生TOKEN
     *
     * @param agentCode
     * @param agentKey
     * @param secretKey
     * @return
     */
    private static String generateToken(String agentCode, String agentKey, String secretKey) {
        String sTimestamp = String.valueOf(System.currentTimeMillis());
        String hashToken = DigestUtils.md5Hex(agentCode + sTimestamp + agentKey);
        String tokenPayLoad = String.format("%s|%s|%s", agentCode, sTimestamp, hashToken);
        String token = CipherText.getPbEncrypt(tokenPayLoad, secretKey);
        return token;
    }

    private Map<String, String> getHeads(TGmApi gmApi) {
        Map<String, String> maps = new HashMap<String, String>();
        maps.put(PbConstants.Headkey.token, generateToken(gmApi.getAgyAcc(), gmApi.getSecureCodes().get(Json.agentKey), gmApi.getSecureCodes().get(Json.secretKey)));
        maps.put(PbConstants.Headkey.userCode, gmApi.getAgyAcc());
        return maps;
    }
}
