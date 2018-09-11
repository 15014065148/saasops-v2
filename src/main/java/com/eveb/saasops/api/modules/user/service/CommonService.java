package com.eveb.saasops.api.modules.user.service;

import com.alibaba.fastjson.JSON;
import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.constants.PtNewConstants;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.transfer.dto.BillManBooleanDto;
import com.eveb.saasops.api.modules.transfer.dto.BillRequestDto;
import com.eveb.saasops.api.modules.transfer.mapper.AccountDepotMapper;
import com.eveb.saasops.api.modules.unity.dto.LoginModel;
import com.eveb.saasops.api.modules.unity.dto.PlayGameModel;
import com.eveb.saasops.api.modules.unity.dto.RegisterModel;
import com.eveb.saasops.api.modules.unity.dto.TransferModel;
import com.eveb.saasops.api.modules.unity.service.GameDepotService;
import com.eveb.saasops.api.modules.user.dto.PtUserInfo;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.config.MessagesConfig;
import com.eveb.saasops.modules.member.dao.MbrBillDetailMapper;
import com.eveb.saasops.modules.member.dao.MbrBillManageMapper;
import com.eveb.saasops.modules.member.entity.MbrBillDetail;
import com.eveb.saasops.modules.member.entity.MbrBillManage;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.service.MbrBillManageService;
import com.eveb.saasops.modules.member.service.MbrWalletService;
import com.eveb.saasops.modules.operate.entity.TGmDepot;
import com.eveb.saasops.modules.operate.entity.TGmGame;
import com.eveb.saasops.modules.operate.service.TGmDepotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
public class CommonService {

    @Autowired
    private GameDepotService gameDepotService;

    @Autowired
    private DepotWalletService depotWalletService;

    @Autowired
    private MbrWalletService mbrWalletService;

    @Autowired
    private TGmDepotService tGmDepotService;

    @Autowired
    private AccountDepotMapper accountPayMapper;

    @Autowired
    private MessagesConfig messagesConfig;

    @Autowired
    private MbrBillManageMapper mbrBillManageMapper;

    @Autowired
    private MbrBillDetailMapper mbrBillDetailMapper;

    @Autowired
    private MbrBillManageService mbrBillManageService;

    @Autowired
    private DepositCommonService depositCommonService;

    @Autowired
    private WithdrawCommonService withdrawCommonService;

    public String generateUrl(TGmApi gmApi, TGmGame tGmGame,Integer accountId, String loginName, Byte terminal) {
        createMember(gmApi,loginName);
        String resultStr;
        if(tGmGame.getTopLink()==0){
            resultStr =  playGame(gmApi, tGmGame, loginName);
        } else {
            resultStr = openHall(gmApi, loginName);
        }
        Map resultMaps = (Map) JSON.parse(resultStr);
        Boolean code =  Boolean.parseBoolean(resultMaps.get("code").toString());
        if(code){
            return resultMaps.get("message").toString();
        }
        throw new RRException("游戏跳转异常!");
    }

    /**
     * 游戏跳转
     * @param gmApi
     * @param loginName
     * @return
     */
    public String openHall(TGmApi gmApi, String loginName) {
        PlayGameModel playGameModel = new PlayGameModel();
        playGameModel.setDepotId(gmApi.getDepotId());
        playGameModel.setSiteCode(gmApi.getSiteCode());
        playGameModel.setDepotName(gmApi.getDepotCode());
        playGameModel.setUserName(loginName);
        String resultStr = gameDepotService.openHall(playGameModel);
        log.error("请求返回数据============"+resultStr);
        return resultStr;
    }

    public static void main(String [] args){
        String param = "{\"code\":true,\"message\":\"http://apiapi125.com/main.php?LoginTokenID=tdUviQesu&OperatorCode=EVEBtest&lang=zh-cn&playerid=ybhnull&Currency=CNY&Key=04f8dcdbb4b2af716746c2e32d6451c6f7dc5e4be3e8834d1fc92d20caab2e4b&mobile=0\"}";
        Map resultMaps = (Map) JSON.parse(param);
        Boolean code =  Boolean.parseBoolean(resultMaps.get("code").toString());
        if(code){
            System.out.println("======"+resultMaps.get("message").toString());
        }
    }


    /**
     * 登陆
     *
     * @param gmApi
     * @param loginName
     * @return
     */
    public String login(TGmApi gmApi, String loginName) {
        LoginModel loginModel = new LoginModel();
        loginModel.setDepotId(gmApi.getDepotId());
        loginModel.setSiteCode(gmApi.getSiteCode());
        loginModel.setDepotName(gmApi.getDepotCode());
        String resultStr = gameDepotService.login(loginModel);
        log.error("请求返回数据============"+resultStr);
        Map resultMaps = (Map) JSON.parse(resultStr);
        Boolean code =  Boolean.parseBoolean(resultMaps.get("code").toString());
        if(code){
            return resultMaps.get("message").toString();
        }
        throw new RRException("游戏跳转异常!");
    }


    /**
     * 建立会员
     *
     * @param gmApi
     * @param loginName
     * @return
     */
    public String createMember(TGmApi gmApi, String loginName) {
        RegisterModel registerModel = new RegisterModel();
        registerModel.setSiteCode(gmApi.getSiteCode());
        registerModel.setUserName(loginName);
        registerModel.setDepotId(gmApi.getDepotId());
        registerModel.setDepotName(gmApi.getDepotCode());
        String createStr = gameDepotService.createMember(registerModel);
        log.error("创建会员======"+createStr);
        return createStr;
    }


    /**
     * 余额查询
     *
     * @param gmApi
     * @param loginName
     * @return
     */
    public PtUserInfo getBalance(TGmApi gmApi, String loginName) {
        createMember(gmApi,loginName);
        LoginModel loginModel = new LoginModel();
        loginModel.setDepotId(gmApi.getDepotId());
        loginModel.setDepotName(gmApi.getDepotCode());
        loginModel.setSiteCode(gmApi.getSiteCode());
        loginModel.setUserName(loginName);
        String resultStr = gameDepotService.queryBalance(loginModel);
        log.error("请求返回数据============"+resultStr);
        Map resultMaps = (Map) JSON.parse(resultStr);
        Boolean code =  Boolean.parseBoolean(resultMaps.get("code").toString());
        if(code){
            PtUserInfo info = new PtUserInfo();
            info.setBALANCE(String.valueOf(resultMaps.get("message")));
            info.setCURRENCY(PtNewConstants.RegiterDefault.currency);
            return info;
        }
        throw new RRException("暂停服务,请稍后重试!");
    }


    public BillManBooleanDto commonTransferIn(BillRequestDto requestDto, TGmApi gmApi) {
        BillManBooleanDto billManBooleanDto = new BillManBooleanDto();
        MbrDepotWallet mbrWallet = depositCommonService.getDepotWallet(requestDto.getAccountId(), gmApi.getDepotId(), gmApi.getSiteCode(), requestDto.getLoginName());
        if (!StringUtils.isEmpty(mbrWallet.getIsBuild()) && mbrWallet.getIsBuild() == false) {
            String createStr = createMember(gmApi, requestDto.getLoginName());
            log.error("请求返回数据============"+createStr);
            Map resultMaps = (Map) JSON.parse(createStr);
            Boolean code =  Boolean.parseBoolean(resultMaps.get("code").toString());
            if(code){
                TGmDepot tGmDepot = tGmDepotService.queryObject(gmApi.getDepotId());
                mbrWallet.setDepotName(tGmDepot.getDepotName());
                mbrWalletService.noExistInsert(mbrWallet, gmApi.getSiteCode());
            } else {
                mbrWalletService.updateTransfer(mbrWallet.getId());
            }
        }

        MbrBillManage mbrBillManage = depositCommonService.billChargeIn(requestDto);
        Integer state = deposit(gmApi, mbrWallet.getLoginName(), mbrBillManage.getAmount(), requestDto.getOrderNo());
        /**查询转账后余额*/
        mbrBillManage.setDepotAfterBalance(depotWalletService.queryDepotBalance(requestDto.getAccountId(), gmApi).getBalance());
        if (state == ApiConstants.TransferStates.suc) {
            depositCommonService. updateMbrBillManageStatus(mbrBillManage, Constants.manageStatus.succeed);
            billManBooleanDto.setMbrBillManage(mbrBillManage);
            billManBooleanDto.setIsTransfer(Boolean.TRUE);
            return billManBooleanDto;
        } else if (state == ApiConstants.TransferStates.fail) {
            depositCommonService.transferBust(mbrBillManage);
        }
        billManBooleanDto.setIsTransfer(Boolean.FALSE);
        return billManBooleanDto;
    }


    /**
     * 存款
     * @param gmApi
     * @param loginName
     * @param amount
     * @return
     */
    public Integer deposit(TGmApi gmApi, String loginName, BigDecimal amount, String orderNo) {
        TransferModel transferModel = new TransferModel();
        transferModel.setDepotId(gmApi.getDepotId());
        transferModel.setDepotName(gmApi.getDepotCode());
        transferModel.setSiteCode(gmApi.getSiteCode());
        transferModel.setUserName(loginName);
        transferModel.setOrderNo(orderNo);
        transferModel.setAmount(amount.setScale( 0, BigDecimal.ROUND_DOWN).intValue());
        String resultStr = gameDepotService.deposit(transferModel);
        log.error("请求返回数据============"+resultStr);
        Map resultMaps = (Map) JSON.parse(resultStr);
        Boolean code = Boolean.parseBoolean(resultMaps.get("code").toString());
        return code==true?0:1;
    }


    public Boolean commonTransferOut(BillRequestDto requestDto, TGmApi gmApi) {
        BillManBooleanDto billManBooleanDto = new BillManBooleanDto();
        MbrDepotWallet mbrWallet = depositCommonService.getDepotWallet(requestDto.getAccountId(), gmApi.getDepotId(), gmApi.getSiteCode(), requestDto.getLoginName());
        if (!StringUtils.isEmpty(mbrWallet.getIsBuild()) && mbrWallet.getIsBuild() == false) {
            String createStr = createMember(gmApi, requestDto.getLoginName());
            log.error("请求返回数据============"+createStr);
            Map resultMaps = (Map) JSON.parse(createStr);
            Boolean code =  Boolean.parseBoolean(resultMaps.get("code").toString());
            if(code){
                TGmDepot tGmDepot = tGmDepotService.queryObject(gmApi.getDepotId());
                mbrWallet.setDepotName(tGmDepot.getDepotName());
                mbrWalletService.noExistInsert(mbrWallet, gmApi.getSiteCode());
            } else {
                mbrWalletService.updateTransfer(mbrWallet.getId());
            }
        }

        PtUserInfo info = getBalance(gmApi, mbrWallet.getLoginName());
        if (Double.parseDouble(info.getBALANCE()) >= requestDto.getAmount().doubleValue()) {
            MbrBillManage mbrBillManage = withdrawCommonService.billChargeOut(requestDto);
            MbrBillDetail mbrBillDetail = withdrawCommonService.setMbrBillDetail(mbrBillManage);
            Integer state = withdraw(gmApi, mbrWallet.getLoginName(), mbrBillManage.getAmount(), requestDto.getOrderNo());
            /**查询转账后余额*/
            mbrBillManage.setDepotAfterBalance(depotWalletService.queryDepotBalance(requestDto.getAccountId(), gmApi).getBalance());
            if (state == ApiConstants.TransferStates.suc) {
                mbrBillManage.setBeforeBalance(mbrBillDetail.getBeforeBalance());
                mbrBillManage.setAfterBalance(mbrBillDetail.getAfterBalance());
                withdrawCommonService.transferOutSuc(mbrBillDetail, mbrBillManage, Constants.manageStatus.succeed);
                return Boolean.TRUE;
            } else if (state == ApiConstants.TransferStates.fail) {
                withdrawCommonService.updateMbrBillManageStatus(mbrBillManage, Constants.manageStatus.defeated);
            }
            return Boolean.FALSE;
        } else {
            throw new RRException("余额不足!");
        }
    }


    /**
     * 取款
     * @param gmApi
     * @param loginName
     * @param amount
     * @return
     */
    public Integer withdraw(TGmApi gmApi, String loginName, BigDecimal amount, String orderNo) {
        TransferModel transferModel = new TransferModel();
        transferModel.setDepotId(gmApi.getDepotId());
        transferModel.setDepotName(gmApi.getDepotCode());
        transferModel.setSiteCode(gmApi.getSiteCode());
        transferModel.setUserName(loginName);
        transferModel.setOrderNo(orderNo);
        transferModel.setAmount(amount.setScale( 0, BigDecimal.ROUND_DOWN).intValue());
        String resultStr = gameDepotService.withdrawal(transferModel);
        log.error("请求返回数据============"+resultStr);
        Map resultMaps = (Map) JSON.parse(resultStr);
        Boolean code = Boolean.parseBoolean(resultMaps.get("code").toString());
        return code==true?0:1;
    }

    /**
     * 登出
     *
     * @param gmApi
     * @param loginName
     * @return
     */
    public boolean logOut(TGmApi gmApi, String loginName) {
        LoginModel loginModel = new LoginModel();
        loginModel.setDepotId(gmApi.getDepotId());
        loginModel.setDepotName(gmApi.getDepotCode());
        loginModel.setSiteCode(gmApi.getSiteCode());
        loginModel.setUserName(loginName);
        String resultStr = gameDepotService.logout(loginModel);
        log.error("请求返回数据============"+resultStr);
        Map resultMaps = (Map) JSON.parse(resultStr);
        Boolean code =  Boolean.parseBoolean(resultMaps.get("code").toString());
        return code;
    }


    /**
     *  玩游戏
     *
     * @param gmApi
     * @param tGmGame
     * @param loginName
     * @return
     */
    public String playGame(TGmApi gmApi, TGmGame tGmGame, String loginName) {
        PlayGameModel playGameModel = new PlayGameModel();
        playGameModel.setDepotId(gmApi.getDepotId());
        playGameModel.setDepotName(gmApi.getDepotCode());
        playGameModel.setSiteCode(gmApi.getSiteCode());
        playGameModel.setUserName(loginName);
        playGameModel.setGameId(tGmGame.getGameCode());
        playGameModel.setGameType(tGmGame.getCatId().toString());
        String resultStr = gameDepotService.playGame(playGameModel);
        log.error("请求返回数据============"+resultStr);
        return resultStr;
    }
}
