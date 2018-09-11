package com.eveb.saasops.api.modules.user.service;

import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.apisys.service.TGmApiService;
import com.eveb.saasops.api.modules.user.dto.DepotManageDto;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.config.ThreadLocalCache;
import com.eveb.saasops.modules.member.entity.MbrAccount;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.service.MbrAccountService;
import com.eveb.saasops.modules.member.service.MbrDepotWalletService;
import com.eveb.saasops.modules.operate.entity.TGmDepot;
import com.eveb.saasops.modules.operate.service.TGmDepotService;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @Author: Miracle
 * @Description: 平台操作服务 登陆、登出、锁定等
 * @Date: 16:16 2018/05/07
 **/
@Service
@Slf4j
public class DepotOperatService {

    @Autowired
    private MbrAccountService userService;
    @Autowired
    private TGmApiService gmApiService;
    @Autowired
    private BbinService bbinService;
    @Autowired
    private PtService ptService;
    @Autowired
    private IbcService ibcService;
    @Autowired
    private EvService evService;
    @Autowired
    private OpusSbService opusSbService;
    @Autowired
    private OpusLiveService opusLiveService;
    @Autowired
    private PbService pbService;
    @Autowired
    private TGmDepotService tGmDepotService;
    @Autowired
    private DepotWalletService depotWalletService;
    @Autowired
    private MbrDepotWalletService mbrDepotWalletService;
    @Autowired
    private CommonService commonService;

    /**
     * 登出
     * @param depotId
     * @param sitePrefix
     * @return
     */
    public R LoginOut(Integer depotId,Integer accountId, String sitePrefix) {
        Boolean isTransfer=true;
        MbrAccount user = userService.queryObject(accountId, sitePrefix);
        TGmApi gmApi = gmApiService.queryApiObject(depotId, sitePrefix);
        if (gmApi == null) {
            throw new RRException("无此API线路!");
        }
        switch (depotId) {
            case ApiConstants.DepotId.BBIN:
                isTransfer = bbinService.logOut(gmApi,user.getLoginName());
                break;
            case ApiConstants.DepotId.AGIN:
                break;
            case ApiConstants.DepotId.PT:
                isTransfer = ptService.logOut(gmApi,user.getLoginName());
                break;
            case ApiConstants.DepotId.PTNEW:
                break;
            case ApiConstants.DepotId.NT:
                //todo 需要用到登陆session才能登出
                break;
            case ApiConstants.DepotId.MG:
                break;
            case ApiConstants.DepotId.PNG:
                break;
            case ApiConstants.DepotId.T188:
                break;
            case ApiConstants.DepotId.IBC:
                isTransfer =ibcService.logOut(gmApi,user.getLoginName());
                break;
            case ApiConstants.DepotId.EG:
                isTransfer = evService.logOut(gmApi,user.getLoginName());
                break;
            case ApiConstants.DepotId.OPUSSB:
                isTransfer = opusSbService.kickUser(gmApi,user.getLoginName());
                break;
            case ApiConstants.DepotId.OPUSLIVE:
                isTransfer = opusLiveService.kickUser(gmApi,user.getLoginName());
                break;
            case ApiConstants.DepotId.PB:
                isTransfer = pbService.logOut(gmApi,user.getLoginName());
                break;
            default:
                isTransfer = commonService.logOut(gmApi, user.getLoginName());
                break;
        }
        if (isTransfer) {
            return R.ok();
        } else {
            return R.error("登出失败!");
        }
    }
    
    /***
     * 查询第三方平台列表
     * @param pageNo
     * @param pageSize
     * @param accountId
     * @return
     */
    public R GetDepotList(Integer pageNo,Integer pageSize,Integer accountId)
    {
        String siteCode = CommonUtil.getSiteCode();
        TGmDepot tGmDepot = new TGmDepot();
        tGmDepot.setAvailable(Constants.Available.enable);
        int size =tGmDepotService.selectCount(tGmDepot);
        List<MbrDepotWallet> depots=mbrDepotWalletService.findDepots(accountId);
        MbrAccount user = userService.queryObject(accountId, siteCode);
        List<DepotManageDto> depotManageDtos = new ArrayList<>();
        for (MbrDepotWallet depot : depots) {
            CompletableFuture<DepotManageDto> completableFuture=getAsyncBalance(depot,user,siteCode);
            CompletableFuture.allOf(completableFuture).join();
            try {
                depotManageDtos.add(completableFuture.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        PageHelper.startPage(pageNo,pageSize);
        PageUtils page = BeanUtil.toPagedResult(depotManageDtos);
        page.setTotalCount(size);
        int totalPage =size / pageSize + (size%pageSize == 0 ?0:1);
        page.setTotalPage(totalPage);
        return R.ok().put("data",page);
    }

    /**
     * 获取当前站点下玩家各个平台的余额
     * @param depot
     * @param user
     * @param siteCode
     * @return
     */
    public CompletableFuture<DepotManageDto> getAsyncBalance(MbrDepotWallet depot, MbrAccount user, String  siteCode)  {
        return CompletableFuture.supplyAsync(() ->{
            ThreadLocalCache.setSiteCodeAsny(siteCode);
            TGmApi gmApi = gmApiService.queryApiObject(depot.getDepotId(), siteCode);
            DepotManageDto depotManageDto= new DepotManageDto();
            depotManageDto.setPlayer(gmApi.getPrefix()+user.getLoginName());
            depotManageDto.setPlatform(depot.getDepotName());
            depotManageDto.getOpterate().setEnableLoggingOut(getLoginOutAuth(depot.getDepotId()));
            depotManageDto.getOpterate().setEnableLock(getLockAuth(depot.getDepotId()));
            depotManageDto.setPlatformId(depot.getDepotId());
            try {
                depotManageDto.setApiId(gmApi.getId());
                depotManageDto.setBalance(depotWalletService.queryDepotBalance(user.getId(), gmApi).getBalance());
                log.info("【" + user.getLoginName() + "】获取用户余余额:" + depotManageDto.getBalance());
                //查询是否可以转账
            } catch (Exception e) {
                depotManageDto.setBalance(BigDecimal.ZERO);
            }
            return depotManageDto;
        });

    }

    private Boolean getLoginOutAuth(int depotId){
        switch (depotId) {
            case ApiConstants.DepotId.BBIN:
                return true;
            case ApiConstants.DepotId.AGIN:
                return false;
            case ApiConstants.DepotId.PT:
                return true;
            case ApiConstants.DepotId.PTNEW:
                return false;
            case ApiConstants.DepotId.NT:
                return false;
            case ApiConstants.DepotId.MG:
                return false;
            case ApiConstants.DepotId.PNG:
                return false;
            case ApiConstants.DepotId.T188:
                return false;
            case ApiConstants.DepotId.IBC:
                return true;
            case ApiConstants.DepotId.EG:
                return true;
            case ApiConstants.DepotId.OPUSSB:
                return true;
            case ApiConstants.DepotId.OPUSLIVE:
                return true;
            case ApiConstants.DepotId.PB:
                return true;
            default:
               return false;
        }
    }

    private Boolean getLockAuth(int depotId){
        switch (depotId) {
            case ApiConstants.DepotId.BBIN:
                return false;
            case ApiConstants.DepotId.AGIN:
                return false;
            case ApiConstants.DepotId.PT:
                return true;
            case ApiConstants.DepotId.PTNEW:
                return false;
            case ApiConstants.DepotId.NT:
                return false;
            case ApiConstants.DepotId.MG:
                return false;
            case ApiConstants.DepotId.PNG:
                return false;
            case ApiConstants.DepotId.T188:
                return false;
            case ApiConstants.DepotId.IBC:
                return false;
            case ApiConstants.DepotId.EG:
                return false;
            case ApiConstants.DepotId.OPUSSB:
                return false;
            case ApiConstants.DepotId.OPUSLIVE:
                return false;
            case ApiConstants.DepotId.PB:
                return false;
            default:
                return false;
        }
    }

    public BigDecimal flushBalance(Integer accountId, Integer platformId, String siteCode) {
        try {
            TGmApi gmApi = gmApiService.queryApiObject(platformId, siteCode);
            BigDecimal balance=depotWalletService.queryDepotBalance(accountId, gmApi).getBalance();
            log.info("【" + accountId + "】获取用户余余额:" + balance);
            return balance;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public R lockPlayer(Integer accountId, Integer depotId,String sitePrefix){
        MbrAccount user = userService.queryObject(accountId, sitePrefix);
        TGmApi gmApi = gmApiService.queryApiObject(depotId, sitePrefix);
        String result=ptService.lockPlayer(gmApi,user.getLoginName());
        return  R.ok().put(result);
    }

    public R unlockPlayer(Integer accountId, Integer depotId,String sitePrefix){
        MbrAccount user = userService.queryObject(accountId, sitePrefix);
        TGmApi gmApi = gmApiService.queryApiObject(depotId, sitePrefix);
        return  R.ok().put(ptService.unlockPlayer(gmApi,user.getLoginName()));
    }
}
