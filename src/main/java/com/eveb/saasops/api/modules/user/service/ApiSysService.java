package com.eveb.saasops.api.modules.user.service;

import com.alibaba.fastjson.JSON;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.apisys.service.TGmApiService;
import com.eveb.saasops.api.modules.transfer.dto.BillRequestDto;
import com.eveb.saasops.api.modules.transfer.service.TransferService;
import com.eveb.saasops.api.modules.unity.dto.PlayGameModel;
import com.eveb.saasops.api.modules.unity.service.GameDepotService;
import com.eveb.saasops.api.modules.user.dto.UserBalanceResponseDto;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.utils.BigDecimalMath;
import com.eveb.saasops.common.utils.Collections3;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.config.ThreadLocalCache;
import com.eveb.saasops.modules.fund.service.FundReportService;
import com.eveb.saasops.modules.member.dto.AuditBonusDto;
import com.eveb.saasops.modules.member.dto.DepotFailDto;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.entity.MbrWallet;
import com.eveb.saasops.modules.member.service.*;
import com.eveb.saasops.modules.operate.service.TGmDepotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.modules.apisys.entity.TCpSite;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.modules.member.entity.MbrAccount;
import com.eveb.saasops.modules.operate.entity.TGmGame;
import com.eveb.saasops.modules.operate.service.TGmGameService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class ApiSysService {
    @Autowired
    private TGmGameService gmGameService;
    @Autowired
    BbinService bbinService;
    @Autowired
    AginService aginService;
    @Autowired
    PtService ptService;
    @Autowired
    PtNewService ptNewService;
    @Autowired
    MbrAccountService userService;
    @Autowired
    NtService ntService;
    @Autowired
    MgService mgService;
    @Autowired
    PngService pngService;
    @Autowired
    T188Service t188Service;
    @Autowired
    IbcService ibcService;
    @Autowired
    EvService evService;
    @Autowired
    OpusSbService opusSbService;
    @Autowired
    PbService pbService;
    @Autowired
    private TGmApiService gmApiService;
    @Autowired
    private TransferService transferService;
    @Autowired
    private FundReportService fundReportService;
    @Autowired
    private MbrWalletService mbrWalletService;
    @Autowired
    private AuditAccountService auditAccountService;

    @Autowired
    CommonService commonService;




    public R transit(TCpSite cpSite, Integer userId, String loginName, MbrDepotWallet wallet, BillRequestDto requestDto) {
        MbrAccount user = userService.queryObject(userId, cpSite.getSiteCode());
        if (user.getAvailable() == MbrAccount.Status.LOCKED)
            throw new RRException("账号余额已冻结,不能进入游戏!");
        TGmGame tGmGame = gmGameService.queryObjectOne(requestDto.getGameId(), cpSite.getSiteCode());
        if (Objects.isNull(tGmGame)) throw new RRException("平台无此游戏!");
        gmGameService.updateClickNum(tGmGame.getId(), cpSite.getSiteCode());
        TGmApi gmApi = gmApiService.queryApiObject(tGmGame.getDepotId(), cpSite.getSiteCode());
        if (Objects.isNull(gmApi)) throw new RRException("无此API线路!");

        wallet.setDepotId(tGmGame.getDepotId());
        mbrWalletService.recoverBalanceNew(wallet, requestDto.getIp(), requestDto.getTransferSource(), Boolean.TRUE);
        //TODO 查询主账户信息
        MbrWallet mbrWallet = fundReportService.queryAccountBalance(loginName);
        if (mbrWallet.getBalance().compareTo(BigDecimal.ONE) == Constants.EVNumber.one ||
                mbrWallet.getBalance().compareTo(BigDecimal.ONE) == Constants.EVNumber.zero) {
            requestDto.setAccountId(mbrWallet.getId());
            requestDto.setDepotId(tGmGame.getDepotId());
            requestDto.setLoginName(loginName);
            BigDecimal bd = new BigDecimal(mbrWallet.getBalance().toString());
            requestDto.setAmount(bd.setScale(0, BigDecimal.ROUND_DOWN));
            MbrAccount account = new MbrAccount();
            account.setId(userId);
            account.setLoginName(loginName);
            AuditBonusDto auditBonusDto = auditAccountService.outAuditBonus(account, requestDto.getDepotId());
            if (Boolean.TRUE.equals(auditBonusDto.getIsSucceed()) && Boolean.TRUE.equals(auditBonusDto.getIsFraud())) {
                transferService.TransferIn_mz(requestDto, gmApi.getSiteCode());
            }
        }
        String url = getAllDepotUrl(userId, loginName, requestDto, tGmGame, gmApi);
        return R.ok(url);
    }

    private String getAllDepotUrl(Integer userId, String loginName, BillRequestDto requestDto, TGmGame tGmGame, TGmApi gmApi) {
        String url = "";
        Byte terminal = requestDto.getTerminal();
        switch (tGmGame.getDepotId().intValue()) {
            case ApiConstants.DepotId.BBIN:
                url = bbinService.generateUrl(gmApi, tGmGame, userId, loginName, terminal);
                break;
            case ApiConstants.DepotId.AGIN:
                url = aginService.generateUrl(gmApi, tGmGame, userId, loginName, terminal);
                break;
            case ApiConstants.DepotId.PT:
                url = ptService.generateUrl(gmApi, tGmGame, userId, loginName, terminal);
                break;
            case ApiConstants.DepotId.PTNEW:
                url = ptNewService.generateUrl(gmApi, tGmGame, userId, loginName, terminal);
                break;
            case ApiConstants.DepotId.NT:
                url = ntService.generateUrl(gmApi, tGmGame, userId, loginName, terminal);
                break;
            case ApiConstants.DepotId.MG:
                url = mgService.generateUrl(gmApi, tGmGame, userId, loginName, terminal);
                break;
            case ApiConstants.DepotId.PNG:
                url = pngService.generateUrl(gmApi, tGmGame, userId, loginName, terminal);
                break;
            // 这个手机还未有实现
            case ApiConstants.DepotId.T188:
                url = t188Service.generateUrl(gmApi, tGmGame, userId, loginName);
                break;
            // 已验证手机可用
            case ApiConstants.DepotId.IBC:
                url = ibcService.generateUrl(gmApi, tGmGame, userId, loginName, terminal);
                break;
            case ApiConstants.DepotId.EG:
                url = evService.generateUrl(gmApi, userId, loginName, terminal);
                break;
            case ApiConstants.DepotId.OPUSSB:
                //url = opusSbService.generateUrl(tGmGame, cpSite, userId, loginName, terminal);
                break;
            /*case ApiConstants.DepotId.OPUSSB:
                url = opusSbService.generateUrl(tGmGame, cpSite, userId, loginName, terminal);
                break;*/
            case ApiConstants.DepotId.PB:
                url = pbService.generateUrl(gmApi, userId, loginName, terminal);
                break;
            default:
                url = commonService.generateUrl(gmApi, tGmGame, userId, loginName, terminal);
                break;
        }
        return url;
    }
}
