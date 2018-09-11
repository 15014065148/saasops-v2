package com.eveb.saasops.api.modules.user.service;

import com.eveb.saasops.api.config.PtConfig;
import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.constants.ApiConstants.TransferStates;
import com.eveb.saasops.api.constants.PtConstants;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.apisys.service.TGmApiService;
import com.eveb.saasops.api.modules.user.dto.*;
import com.eveb.saasops.api.utils.JsonUtil;
import com.eveb.saasops.common.constants.Constants.Available;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.common.utils.XmlUtil;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.service.MbrWalletService;
import com.eveb.saasops.modules.operate.entity.TGmDepot;
import com.eveb.saasops.modules.operate.entity.TGmGame;
import com.eveb.saasops.modules.operate.service.TGmDepotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class PtService {
    @Autowired
    MbrWalletService mbrWalletService;
    @Autowired
    private TGmApiService gmApiService;
    @Autowired
    private TGmDepotService tGmDepotService;
    @Autowired
    private AginService aginService;
    @Autowired
    private PtConfig ptConfig;
    @Autowired
    private OkHttpService okHttpService;


    /**
     * 锁定用户
     *
     * @param gmApi
     * @param loginName
     */
    public String lockPlayer(TGmApi gmApi, String loginName) {
        UserPtDto userPtDto = new UserPtDto();
        userPtDto.setLoginname(gmApi.getPrefix() + loginName);
        userPtDto.setFrozen(PtConstants.mod.frozen);
        setEntity(userPtDto, gmApi);
        String result = okHttpService.send(gmApi.getPcUrl() + userPtDto.toString(), userPtDto.getPtEntity());
        log.debug(" PT—>余额查询 [会员账号{}提交参数{}返回结果{}]", loginName, gmApi.getPcUrl() + userPtDto.toString(), result);
        if (!StringUtils.isEmpty(result)) {
            Map<String, java.lang.Object> list = JsonUtil.json2map(result);
            if (list.get("result") != null) {
                Map<String, Object> rs = (Map<String, Object>) list.get("result");
                return rs.get("result").toString();
            } else {
                throw new RRException(gmApi.getPrefix() + loginName + "该用户不存在");
            }
        } else {
            throw new RRException("PT会员通信异常!");
        }
    }

    /**
     * 锁定用户
     *
     * @param gmApi
     * @param loginName
     */
    public String unlockPlayer(TGmApi gmApi, String loginName) {
        UserPtDto userPtDto = new UserPtDto();
        userPtDto.setLoginname(gmApi.getPrefix() + loginName);
        userPtDto.setUnFrozen(PtConstants.mod.unFrozen);
        setEntity(userPtDto, gmApi);
        String result = okHttpService.send(gmApi.getPcUrl() + userPtDto.toString(), userPtDto.getPtEntity());
        log.debug(" PT—>余额查询 [会员账号{}提交参数{}返回结果{}]", loginName, gmApi.getPcUrl() + userPtDto.toString(), result);
        if (!StringUtils.isEmpty(result)) {
            Map<String, java.lang.Object> list = JsonUtil.json2map(result);
            if (list.get("result") != null) {
                Map<String, Object> rs = (Map<String, Object>) list.get("result");
                return rs.get("result").toString();
            } else {
                throw new RRException(gmApi.getPrefix() + loginName + "该用户不存在");
            }
        } else {
            throw new RRException("PT会员通信异常!");
        }
    }

    /**
     * 获取奖池奖金
     *
     * @return
     */
    public R getJackPot() {
        String result = okHttpService.get(ptConfig.getLuckyJackpotUrl());
        log.debug(" PT—>奖池查询 [提交参数{}返回结果{}]", ptConfig.getLuckyJackpotUrl(), result);
        PtJackPotResDto ptJackPotResDto = XmlUtil.ptJackPotResDto(result);
        if (Objects.isNull(ptJackPotResDto.getAmounts())) {
            ptJackPotResDto.setAmounts("0.00");
        }
        return R.ok().put("amount", ptJackPotResDto.getAmounts());
    }

    /**
     * @param gmApi
     * @param accountId
     * @param loginName
     * @return
     */
    public MbrDepotWallet createMember(TGmApi gmApi, Integer accountId, String loginName) {
        MbrDepotWallet mbrWallet = aginService.getDepotWallet(accountId, gmApi.getDepotId(), gmApi.getSiteCode(), loginName);
        if (!StringUtils.isEmpty(mbrWallet.getIsBuild()) && mbrWallet.getIsBuild() == false) {
            UserPtDto userPtDto = getUserPtDto(gmApi, mbrWallet);
            userPtDto.setMod(PtConstants.mod.createMember);
            String result = okHttpService.send(gmApi.getPcUrl() + userPtDto.toString(), userPtDto.getPtEntity());
            log.debug(" PT—>创建账号 [会员账号{}提交参数 {}返回结果 {}]", loginName, gmApi.getPcUrl() + userPtDto.toString(), result);
            if (result != null && result.indexOf(PtConstants.PLAY_CREATED) != -1) {
                TGmDepot tGmDepot = tGmDepotService.queryObject(mbrWallet.getDepotId());
                mbrWallet.setDepotName(tGmDepot.getDepotName());
                mbrWallet.setIsTransfer(MbrDepotWallet.IsTransFer.yes);
                mbrWallet = mbrWalletService.noExistInsert(mbrWallet, gmApi.getSiteCode());
            } else {
                throw new RRException("PT创建账号失败!");
            }
        }
        return mbrWallet;
    }

    /**
     * 余额
     *
     * @param gmApi
     * @param loginName
     */
    @SuppressWarnings("rawtypes")
    public PtUserInfo getBalance(TGmApi gmApi, String loginName) {
        UserPtDto userPtDto = getUserPtDto(gmApi, loginName);
        userPtDto.setMod(PtConstants.mod.info);
        String result = okHttpService.send(gmApi.getPcUrl() + userPtDto.toString(), userPtDto.getPtEntity());
        log.debug(" PT—>余额查询 [会员账号{}提交参数{}返回结果{}]", loginName, gmApi.getPcUrl() + userPtDto.toString(), result);
        PtUserInfo ptUserInfo = new PtUserInfo();
        if (!StringUtils.isEmpty(result)) {
            Map<String, java.lang.Object> list = JsonUtil.json2map(result);
            if (list.get("result") != null) {
                ptUserInfo.setBALANCE(String.valueOf(((Map) list.get("result")).get("BALANCE")));
                ptUserInfo.setCURRENCY(String.valueOf(((Map) list.get("result")).get("CURRENCY")));
            } else {
                throw new RRException("PT查询会员余额失败!");
            }
        } else {
            throw new RRException("PT查询会员通信异常!");
        }
        return ptUserInfo;
    }

    /**
     * 存款
     *
     * @param gmApi
     * @param loginName
     * @param amount
     * @param tranid
     * @return
     */
    public Integer deposit(TGmApi gmApi, String loginName, double amount, String tranid) {
        UserPtDto userPtDto = new UserPtDto();
        userPtDto.setAdminname(gmApi.getWebName());
        userPtDto.setLoginname(gmApi.getPrefix() + loginName);
        setEntity(userPtDto, gmApi);
        userPtDto.setAmount(amount);
        userPtDto.setExternaltranid(tranid);
        userPtDto.setMod(PtConstants.mod.deposit);
        String result = okHttpService.send(gmApi.getPcUrl() + userPtDto.toString(), userPtDto.getPtEntity());
        log.debug(" PT—>存款 [会员账号{}提交参数 {}返回结果{}]", loginName, gmApi.getPcUrl() + userPtDto.toString(), result);
        if (!StringUtils.isEmpty(result)) {
            if (result.toLowerCase().indexOf(PtConstants.DEPOSIT_OK) != -1)
                return TransferStates.suc;
        }
        return TransferStates.fail;
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
    public Integer withdraw(TGmApi gmApi, String loginName, double amount, String tranid) {
        UserPtDto userPtDto = new UserPtDto();
        userPtDto.setAdminname(gmApi.getWebName());
        userPtDto.setLoginname(gmApi.getPrefix() + loginName);
        setEntity(userPtDto, gmApi);
        userPtDto.setAmount(amount);
        userPtDto.setExternaltranid(tranid);
        userPtDto.setIsForce(UserPtDto.IsForce.is);
        userPtDto.setMod(PtConstants.mod.withdraw);
        String result = okHttpService.send(gmApi.getPcUrl() + userPtDto.toString(), userPtDto.getPtEntity());
        log.debug(" PT—>取款 [会员账号{}提交参数{}返回结果 {}]", loginName, gmApi.getPcUrl() + userPtDto.toString(), result);
        // PtUserInfo ptUserInfo = new PtUserInfo();
        if (!StringUtils.isEmpty(result)) {
            if (result.toLowerCase().indexOf(PtConstants.WITHDRAW_OK) != -1)
                return TransferStates.suc;
        }
        return TransferStates.fail;
    }

    /**
     * 检测某一个订单转账是否成功
     * "status": "approved" 与"status": "missing" 两种状态
     *
     * @param gmApi
     * @param tranid
     * @return
     */
    public Integer checktransaction(String tranid, TGmApi gmApi) {
        UserPtDto userPtDto = new UserPtDto();
        userPtDto.setExternaltransactionid(tranid);
        userPtDto.setMod(PtConstants.mod.checktransaction);
        String result = okHttpService.send(gmApi.getPcUrl() + userPtDto.toString(), userPtDto.getPtEntity());
        log.debug(" PT—>查询订单 [提交参数 {}返回结果 {}]", gmApi.getPcUrl() + userPtDto.toString(), result);
        if (!StringUtils.isEmpty(result)) {
            if (result.toLowerCase().indexOf(PtConstants.TRANSACTION_SC) != -1) {
                return TransferStates.suc;
            } else if (result.toLowerCase().indexOf(PtConstants.TRANSACTION_FAIL) != -1) {
                return TransferStates.fail;
            }
        }
        throw new RRException("查询订单状态失败!");
    }

    /**
     * 登出
     *
     * @param gmApi
     * @param loginName
     * @return
     */

    public boolean logOut(TGmApi gmApi, String loginName) {
        UserPtDto userPtDto = new UserPtDto();
        userPtDto.setLoginname(gmApi.getPrefix() + loginName);
        userPtDto.setMod(PtConstants.mod.logout);
        setEntity(userPtDto, gmApi);
        String result = okHttpService.send(gmApi.getPcUrl() + userPtDto.toString(), userPtDto.getPtEntity());
        log.debug(" PT—>登出 [会员账号{}提交参数 {}返回结果 {}]", loginName, gmApi.getPcUrl() + userPtDto.toString(), result);
        if (!StringUtils.isEmpty(result)) {
            if (result.toLowerCase().indexOf(PtConstants.LOGOUT) != -1)
                return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 登出
     *
     * @param depotId
     * @param loginName
     * @param sitePrefix
     * @return
     */
    public boolean logOut(Integer depotId, String loginName, String sitePrefix) {
        TGmApi gmApi = gmApiService.queryApiObject(depotId, sitePrefix);
        return logOut(gmApi, loginName);
    }

    /**
     * 是否在线
     *
     * @param gmApi
     * @param loginName
     * @param sitePrefix
     * @return
     */
    public boolean online(TGmApi gmApi, String loginName, String sitePrefix) {
        UserPtDto userPtDto = new UserPtDto();
        userPtDto.setLoginname(sitePrefix + loginName);
        userPtDto.setMod(PtConstants.mod.online);
        setEntity(userPtDto, gmApi);
        String result = okHttpService.send(gmApi.getPcUrl() + userPtDto.toString(), userPtDto.getPtEntity());
        log.debug(" PT—>在线状态查询 [会员账号{}提交参数 {}返回结果 {}]", loginName, gmApi.getPcUrl() + userPtDto.toString(), result);
        if (!StringUtils.isEmpty(result)) {
            Map<String, java.lang.Object> list = JsonUtil.json2map(result);
            @SuppressWarnings("rawtypes")
            Double aa = new Double(String.valueOf(((Map) list.get("result")).get("result")));
            if (aa.intValue() == PtConstants.Online.yes) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    /**
     * 登陆失败重置
     *
     * @param gmApi
     * @param loginName
     * @param sitePrefix
     * @return
     */

    public boolean resetfailedlogin(TGmApi gmApi, String loginName, String sitePrefix) {
        UserPtDto userPtDto = new UserPtDto();
        userPtDto.setLoginname(gmApi.getPrefix() + loginName);
        userPtDto.setMod(PtConstants.mod.resetfailedlogin);
        setEntity(userPtDto, gmApi);
        String result = okHttpService.send(gmApi.getPcUrl() + userPtDto.toString(), userPtDto.getPtEntity());
        log.debug(" PT—>登陆失败重置 [会员账号{}提交参数{}返回结果 {}]", gmApi.getPrefix() + loginName, gmApi.getPcUrl() + userPtDto.toString(), result);
        if (!StringUtils.isEmpty(result)) {
            if (result.toLowerCase().indexOf(PtConstants.RESETFAILEDLOGIN) != -1)
                return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 游戏跳转
     *
     * @param tgmGame
     * @param accountId
     * @param loginName
     * @return
     */
    public String generateUrl(TGmApi gmApi, TGmGame tgmGame, Integer accountId, String loginName, Byte terminal) {
        tgmGame.setTerminal(terminal);
        if (terminal == ApiConstants.Terminal.mobile && tgmGame.getEnableMb() == Available.disable) {
            throw new RRException("此款游戏不支持手机端!");
        }
        MbrDepotWallet mbrWallet = createMember(gmApi, accountId, loginName);
        logOut(gmApi, loginName);
        String param = getParam(loginName, mbrWallet.getPwd(), gmApi, tgmGame);
        log.debug(" PT—>游戏跳转[会员账号{}提交参数 {}]", loginName, gmApi.getPcUrl() + param);
        return ptConfig.getGameUrl() + param;
    }

    private UserPtDto getUserPtDto(TGmApi gmApi, String loginName) {
        MbrDepotWallet mbrWallet = new MbrDepotWallet();
        mbrWallet.setLoginName(loginName);
        return getUserPtDto(gmApi, mbrWallet);
    }

    private UserPtDto getUserPtDto(TGmApi gmApi, MbrDepotWallet mbrWallet) {
        UserPtDto userPtDto = new UserPtDto();
        userPtDto.setAdminname(gmApi.getWebName());
        userPtDto.setKioskname(gmApi.getAgyAcc());
        userPtDto.setPassword(mbrWallet.getPwd());
        userPtDto.setLoginname(gmApi.getPrefix() + mbrWallet.getLoginName());
        setEntity(userPtDto, gmApi);
        return userPtDto;
    }

    private void setEntity(UserPtDto userPtDto, TGmApi gmApi) {
        PtEntity ptEntity = new PtEntity();
        ptEntity.setEntityKey(PtConstants.JsonKey.ENTITY_KEY_NAME);
        ptEntity.setEntityContext(gmApi.getSecureCodes().get(PtConstants.JsonKey.ENTITY_KEY_NAME));
        userPtDto.setPtEntity(ptEntity);
    }

    private String getParam(String loginName, String pwd, TGmApi gmApi, TGmGame tgmGame) {
        PtRouteDto ptRouteDto = new PtRouteDto();
        ptRouteDto.setLoginName(gmApi.getPrefix() + loginName);
        ptRouteDto.setPwd(pwd);
        ptRouteDto.setTerminal(tgmGame.getTerminal());
        ptRouteDto.setNolobby(PtConstants.RouteParam.nolobby);
        ptRouteDto.setLanguage(PtConstants.RouteParam.language);
        ptRouteDto.setGame(tgmGame.getGameCode());
        ptRouteDto.setGameId(tgmGame.getMbGameCode());
        return ptRouteDto.toString();
    }


}
