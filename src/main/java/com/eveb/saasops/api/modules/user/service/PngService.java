package com.eveb.saasops.api.modules.user.service;

import java.math.BigDecimal;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import com.eveb.saasops.common.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.eveb.saasops.api.config.PngConfig;
import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.constants.PngConstants;
import com.eveb.saasops.api.constants.ApiConstants.TransferStates;
import com.eveb.saasops.api.constants.PngConstants.Countrys;
import com.eveb.saasops.api.constants.PngConstants.Currencies;
import com.eveb.saasops.api.constants.PngConstants.Gender;
import com.eveb.saasops.api.constants.PngConstants.JsonKey;
import com.eveb.saasops.api.constants.PngConstants.Languages;
import com.eveb.saasops.api.constants.PngConstants.Mod;
import com.eveb.saasops.api.constants.PngConstants.Practice;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.user.dto.GetBalanceResp;
import com.eveb.saasops.api.modules.user.dto.PngBalanceOrTicketDto;
import com.eveb.saasops.api.modules.user.dto.PngBase64Code;
import com.eveb.saasops.api.modules.user.dto.PngCreditDto;
import com.eveb.saasops.api.modules.user.dto.PngDebitDto;
import com.eveb.saasops.api.modules.user.dto.PngLaunchGameDto;
import com.eveb.saasops.api.modules.user.dto.PngRegisterDto;
import com.eveb.saasops.api.modules.user.dto.PtUserInfo;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.service.MbrWalletService;
import com.eveb.saasops.modules.operate.entity.TGmDepot;
import com.eveb.saasops.modules.operate.entity.TGmGame;
import com.eveb.saasops.modules.operate.service.TGmDepotService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PngService {
    @Autowired
    MbrWalletService mbrWalletService;
    @Autowired
    private AginService aginService;
    @Autowired
    private TGmDepotService tGmDepotService;
    @Autowired
    private ApiUserService apiUserService;
    @Autowired
    private PngConfig pngConfig;
    @Autowired
    private OkHttpService okHttpService;

    public MbrDepotWallet createMember(TGmApi gmApi, Integer accountId, String loginName) {
        MbrDepotWallet mbrWallet = aginService.getDepotWallet(accountId, gmApi.getDepotId(), gmApi.getSiteCode(),
                loginName);
        if (mbrWallet.getIsBuild() == false) {
            String param = getPngRegisterDto(gmApi, loginName);
            String result = okHttpService.pngHttpPost(gmApi.getPcUrl(), param, getHeads(gmApi, Mod.register));
            log.debug("PNG—>创建账号[会员账号{}提交参数 {}返回结果{}]", loginName, gmApi.getPcUrl() + param, result);
            if (!StringUtils.isEmpty(result) && result.indexOf(PngConstants.REGISTER_SUC_CODE) != -1) {
                TGmDepot tGmDepot = tGmDepotService.queryObject(mbrWallet.getDepotId());
                mbrWallet.setDepotName(tGmDepot.getDepotName());
                mbrWallet.setIsTransfer(MbrDepotWallet.IsTransFer.yes);
                return mbrWalletService.noExistInsert(mbrWallet, gmApi.getSiteCode());
            } else {
                throw new RRException("PNG创建第三方账号失败!");
            }
        }
        return mbrWallet;
    }

    private String getPngRegisterDto(TGmApi gmApi, String loginName) {
        PngRegisterDto dto = new PngRegisterDto();
        dto.setExternalUserId(gmApi.getPrefix() + loginName);
        dto.setUsername(loginName);
        dto.setNickname(loginName);
        dto.setCurrency(Currencies.china);
        dto.setCountry(Countrys.china);
        dto.setBirthdate(PngConstants.REG_BIRTH_DAY);
        dto.setRegistration(DateUtil.getCurrentDate(DateUtil.FORMAT_10_DATE));
        dto.setBrandId(gmApi.getSecureCodes().get(JsonKey.brandId));
        dto.setLanguage(Languages.chinese_S);
        dto.setIp(PngConstants.DEF_IP);
        dto.setLocked(Boolean.FALSE.toString());
        dto.setGender(Gender.female);
        return dto.toString();
    }

    public PtUserInfo getBalance(TGmApi gmApi, String loginName) {
        String param = getBalanceOrTicketReq(gmApi, loginName, PngConstants.SaopModName.Balance);
        String result = okHttpService.pngHttpPost(gmApi.getPcUrl(), param, getHeads(gmApi, Mod.balance));
        log.debug("PNG—>余额查询[会员账号{}提交参数{} 返回结果{}]", loginName, gmApi.getPcUrl() + param, result);
        if (!StringUtils.isEmpty(result)) {
            GetBalanceResp resp = new GetBalanceResp();
            XmlUtil.pngResp(resp, result);
            PtUserInfo info = new PtUserInfo();
            info.setBALANCE(BigDecimalMath.numberFormat(String.valueOf(resp.getReal())));
            info.setCURRENCY(resp.getCurrency());
            return info;
        } else {
            throw new RRException("暂停服务,请稍后重试!");
        }
    }

    public PtUserInfo getBalance(TGmApi gmApi, Integer accountId, String loginName) {
        createMember(gmApi, accountId, loginName);
        return getBalance(gmApi, loginName);
    }

    public String ticket(TGmApi gmApi, Integer accountId, String loginName) {
        //String token = apiUserService.queryPngLoginTokenCache(gmApi.getPrefix(), loginName);
        //if (StringUtils.isEmpty(token)) {
        // 建立用户
        String token = "";
        createMember(gmApi, accountId, loginName);
        String param = getBalanceOrTicketReq(gmApi, loginName, PngConstants.SaopModName.GetTicket);
        String result = okHttpService.pngHttpPost(gmApi.getPcUrl(), param, getHeads(gmApi, Mod.GetTicket));
        log.debug("PNG—>获取TOKEN[会员账号{}提交参数{} 返回结果{}]", loginName, gmApi.getPcUrl() + param, result);
        if (!StringUtils.isEmpty(result)) {
            GetBalanceResp resp = new GetBalanceResp();
            XmlUtil.pngResp(resp, result);
            apiUserService.updatePngLoginTokenCache(gmApi.getPrefix(), loginName, resp.getTicket());
            token = resp.getTicket();
        } else {
            throw new RRException("暂停服务,请稍后重试!");
        }
        //}
        return token;
    }

    private String getBalanceOrTicketReq(TGmApi gmApi, String loginName, String mod) {
        PngBalanceOrTicketDto dto = new PngBalanceOrTicketDto();
        dto.setExternalUserId(gmApi.getPrefix() + loginName);
        dto.setMod(mod);
        return dto.toString();
    }

    public Integer deposit(TGmApi gmApi, String loginName, BigDecimal amount, String tranid) {
        String param = getPngCreditDto(gmApi, loginName, amount, tranid);
        String result = okHttpService.pngHttpPost(gmApi.getPcUrl(), param, getHeads(gmApi, Mod.credit));
        log.debug("PNG—>存款[会员账号{}提交参数{} 返回结果{}]", loginName, gmApi.getPcUrl() + param, result);
        if (!StringUtils.isEmpty(result)) {
            GetBalanceResp resp = new GetBalanceResp();
            XmlUtil.pngResp(resp, result);
            if (!StringUtils.isEmpty(resp.getTransactionId())) {
                return TransferStates.suc;
            } else {
                return TransferStates.fail;
            }
        } else {
            return TransferStates.fail;
        }
    }

    private String getPngCreditDto(TGmApi gmApi, String loginName, BigDecimal amount, String tranid) {
        PngCreditDto dto = new PngCreditDto();
        dto.setAmount(String.valueOf(amount));
        dto.setExternalUserId(gmApi.getPrefix() + loginName);
        dto.setCurrency(Currencies.china);
        dto.setExternalTransactionId(tranid);
        return dto.toString();
    }

    public Integer withdraw(TGmApi gmApi, String loginName, BigDecimal amount, String tranid) {
        String param = getPngDebitDto(gmApi, loginName, amount, tranid);
        String result = okHttpService.pngHttpPost(gmApi.getPcUrl(), param, getHeads(gmApi, Mod.Debit));
        log.debug("PNG—>取款[会员账号{}提交参数 {}返回结果 {}]", loginName, gmApi.getPcUrl() + param, result);
        if (!StringUtils.isEmpty(result)) {
            GetBalanceResp resp = new GetBalanceResp();
            XmlUtil.pngResp(resp, result);
            if (!StringUtils.isEmpty(resp.getTransactionId())) {
                return TransferStates.suc;
            } else {
                return TransferStates.fail;
            }
        } else {
            return TransferStates.fail;
        }
    }

    private String getPngDebitDto(TGmApi gmApi, String loginName, BigDecimal amount, String tranid) {
        PngDebitDto dto = new PngDebitDto();
        dto.setAmount(String.valueOf(amount));
        dto.setExternalUserId(gmApi.getPrefix() + loginName);
        dto.setCurrency(Currencies.china);
        dto.setExternalTransactionId(tranid);
        return dto.toString();
    }

    public String generateUrl(TGmApi gmApi, TGmGame tgmGame, Integer accountId, String loginName, Byte terminal) {
        String token = ticket(gmApi, accountId, loginName);
        String param = getPngLaunchGameDto(gmApi, tgmGame, token, terminal);
        // String url=gmApi.getPcUrl2() + param;
        String url = pngConfig.getGameRouteUrl() + param;
        log.debug("PNG—>游戏跳转[会员账号{}提交参数 {}]", loginName, url);
        // String url =pngConfig.getGameRouteUrl()+param;
        return url;
    }

    private String getPngLaunchGameDto(TGmApi gmApi, TGmGame tgmGame, String token, Byte terminal) {
        PngLaunchGameDto dto = new PngLaunchGameDto();
        dto.setTerminal(terminal);

        dto.setDiv(PngConstants.GAME_LAUNCH_DIV);
        //dto.setGid(tgmGame.getGameCode());
        dto.setGameId((terminal == ApiConstants.Terminal.mobile) ? tgmGame.getMbGameCode() : tgmGame.getGameCode());
        if (StringUtils.isEmpty(dto.getGameId())) {
            throw new RRException("该款游戏不支持此终端设备！");
        }
        dto.setLang(Languages.chinese_S);
        // 可以不设此参数
        dto.setBrand(gmApi.getSecureCodes().get(JsonKey.brandId));
        dto.setUsername(token);
        dto.setPid(gmApi.getSecureCodes().get(JsonKey.pid));
        dto.setPractice(Practice.pay);
        return dto.toBase64();
    }

    private Map<String, String> getHeads(TGmApi gmApi, String method) {
        PngBase64Code base64 = new PngBase64Code();
        base64.setPassWd(gmApi.getMd5Key());
        base64.setUserName(gmApi.getAgyAcc());
        Map<String, String> maps = new HashMap<String, String>();
        maps.put(PngConstants.AUTHORIZATION, base64.toString());
        maps.put(PngConstants.SOAPACTION, PngConstants.BASIC_SOAPACTION + method);
        return maps;
    }

    private String getServerIp() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        try {
            return IpUtils.getServerIp() + ":" + request.getServerPort();
        } catch (Exception ex) {
            return "获取服务器IP出错";
        }
    }


}
