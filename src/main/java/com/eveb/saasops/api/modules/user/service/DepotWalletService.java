package com.eveb.saasops.api.modules.user.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.eveb.saasops.api.utils.JsonUtil;
import com.eveb.saasops.common.constants.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.transfer.dto.BbinUsrBalanceResponseDto;
import com.eveb.saasops.api.modules.transfer.mapper.AccountDepotMapper;
import com.eveb.saasops.api.modules.transfer.service.BBINTransferService;
import com.eveb.saasops.api.modules.user.dto.PtUserInfo;
import com.eveb.saasops.api.modules.user.dto.TransferListDto;
import com.eveb.saasops.api.modules.user.dto.TransferRequestDto;
import com.eveb.saasops.api.modules.user.dto.TransferResponseDto;
import com.eveb.saasops.api.modules.user.dto.UserBalanceResponseDto;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.common.utils.Collections3;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.modules.member.dao.MbrAccountMapper;
import com.eveb.saasops.modules.member.entity.MbrAccount;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class DepotWalletService {

    @Autowired
    private BBINTransferService bbinTransferService;
    @Autowired
    private BbinService bbinService;
    @Autowired
    private AginService aginService;
    @Autowired
    private PtService ptService;
    @Autowired
    private PtNewService ptNewService;
    @Autowired
    private NtService ntService;
    @Autowired
    private MgService mgService;
    @Autowired
    private PngService pngService;
    @Autowired
    private T188Service t188Service;
    @Autowired
    private IbcService ibcService;
    @Autowired
    private EvService evService;
    @Autowired
    private PbService pbService;
    @Autowired
    private OpusSbService opusSbService;
    @Autowired
    private OpusLiveService opusLiveService;
    @Autowired
    private MbrAccountMapper mbrAccountMapper;
    @Autowired
    private AccountDepotMapper depotMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private CommonService commonService;


    /**
     * 查询平台余额（加缓存）
     *
     * @param userId
     * @param gmApi
     * @return
     */
    public UserBalanceResponseDto findDepotBalance(Integer userId, TGmApi gmApi) {
        String depotBalance = RedisConstants.REDIS_DEPOT_BALANCE + gmApi.getDepotId() +
                gmApi.getDepotCode() + userId + "_" + gmApi.getSiteCode();
        if (redisService.booleanRedis(depotBalance)) {
            UserBalanceResponseDto balanceResponseDto = queryDepotBalance(userId, gmApi);
            balanceResponseDto.setDepotId(gmApi.getDepotId());
            redisService.setRedisExpiredTime(depotBalance, balanceResponseDto, 10, TimeUnit.SECONDS);
            return balanceResponseDto;
        } else {
            Object object = redisService.getRedisValus(depotBalance);
            if (Objects.nonNull(object)) {
                return jsonUtil.fromJson(jsonUtil.toJson(object), UserBalanceResponseDto.class);
            }
        }
        return null;
    }

    public UserBalanceResponseDto queryDepotBalance(Integer userId, TGmApi gmApi) {
        MbrAccount user = mbrAccountMapper.selectByPrimaryKey(userId);
        if (Objects.isNull(user)) throw new RRException("会员信息不能为空!");
        UserBalanceResponseDto balanceDto = new UserBalanceResponseDto();
        balanceDto.setBalance(new BigDecimal(0.00));
        balanceDto = getAllDepotBalance(gmApi, user, balanceDto);
        return balanceDto;
    }

    private UserBalanceResponseDto BBINUsrBalance(MbrAccount user, TGmApi gmApi) {
        bbinService.createMember(gmApi, user.getId(), user.getLoginName());
        BbinUsrBalanceResponseDto responseDto = bbinTransferService.checkUsrBalance(user.getLoginName(), gmApi);
        UserBalanceResponseDto balanceDto = new UserBalanceResponseDto();
        if (Objects.isNull(responseDto.getResult()) || Boolean.FALSE.equals(responseDto.getResult())) {
            log.info("BBIN UsrBalance:" + user.getLoginName() + responseDto.getData());
            balanceDto.setBalance(BigDecimal.ZERO);
            return balanceDto;
        }
        if (Collections3.isNotEmpty(responseDto.getData())) {
            balanceDto.setBalance(responseDto.getData().get(0).getBalance());
            balanceDto.setCurrency(responseDto.getData().get(0).getCurrency());
        }
        return balanceDto;
    }

    private UserBalanceResponseDto aginUserBalance(MbrAccount user, TGmApi gmApi) {
        MbrDepotWallet mbrWallet = aginService.checkOrCreateGameAccout(gmApi, user.getId(), user.getLoginName());
        PtUserInfo info = aginService.getBalance(mbrWallet, gmApi);
        return getBalance(info);
    }

    private UserBalanceResponseDto evUserBalance(MbrAccount user, TGmApi gmApi) {
        MbrDepotWallet mbrWallet = evService.checkOrCreateGameAccout(gmApi, user.getId(), user.getLoginName());
        PtUserInfo info = evService.getBalance(mbrWallet, gmApi);
        return getBalance(info);
    }

    private UserBalanceResponseDto ptUserBalance(MbrAccount user, TGmApi gmApi) {
        MbrDepotWallet mbrWallet = ptService.createMember(gmApi, user.getId(), user.getLoginName());
        PtUserInfo info = ptService.getBalance(gmApi, user.getLoginName());
        return getBalance(info);
    }

    private UserBalanceResponseDto ptNewUserBalance(MbrAccount user, TGmApi gmApi) {
        PtUserInfo info = ptNewService.balance(gmApi, user.getId(), user.getLoginName());
        return getBalance(info);
    }

    private UserBalanceResponseDto ntUserBalance(MbrAccount user, TGmApi gmApi) {
        PtUserInfo info = ntService.getBalance(gmApi, user.getId(), user.getLoginName());
        return getBalance(info);
    }

    private UserBalanceResponseDto mgUserBalance(MbrAccount user, TGmApi gmApi) {
        PtUserInfo info = mgService.getBalance(gmApi, user.getId(), user.getLoginName());
        return getBalance(info);
    }

    private UserBalanceResponseDto pngUserBalance(MbrAccount user, TGmApi gmApi) {
        PtUserInfo info = pngService.getBalance(gmApi, user.getId(), user.getLoginName());
        return getBalance(info);
    }

    private UserBalanceResponseDto t188UserBalance(MbrAccount user, TGmApi gmApi) {
        PtUserInfo info = t188Service.getBalance(gmApi, user.getId(), user.getLoginName());
        return getBalance(info);
    }

    private UserBalanceResponseDto ibcUserBalance(MbrAccount user, TGmApi gmApi) {
        PtUserInfo info = ibcService.getBalance(gmApi, user.getId(), user.getLoginName());
        return getBalance(info);
    }

    private UserBalanceResponseDto opusSbUserBalance(MbrAccount user, TGmApi gmApi) {
        PtUserInfo info = opusSbService.getBalance(gmApi, user.getId(), user.getLoginName());
        return getBalance(info);
    }

    private UserBalanceResponseDto opusLiveUserBalance(MbrAccount user, TGmApi gmApi) {
        PtUserInfo info = opusLiveService.getBalance(gmApi, user.getId(), user.getLoginName());
        return getBalance(info);
    }

    private UserBalanceResponseDto pbUserBalance(MbrAccount user, TGmApi gmApi) {
        PtUserInfo info = pbService.getBalance(gmApi, user.getId(), user.getLoginName());
        return getBalance(info);
    }

    private UserBalanceResponseDto commonUserBalance(MbrAccount user, TGmApi gmApi) {
        PtUserInfo info = commonService.getBalance(gmApi, user.getLoginName());
        return getBalance(info);
    }

    public UserBalanceResponseDto getBalance(PtUserInfo info) {
        UserBalanceResponseDto balanceDto = new UserBalanceResponseDto();
        balanceDto.setBalance(new BigDecimal(info.getBALANCE()));
        balanceDto.setCurrency(ApiConstants.CURRENCY_TYPE);
        return balanceDto;
    }

    public TransferListDto findTransferList(TransferRequestDto requestDto, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<TransferResponseDto> list = depotMapper.findTransferList(requestDto);
        PageUtils pageUtils = BeanUtil.toPagedResult(list);
        TransferListDto dto = new TransferListDto();
        dto.setList(pageUtils.getList());
        dto.setCurrPage(pageUtils.getCurrPage());
        dto.setPageSize(pageUtils.getPageSize());
        dto.setTotalCount(pageUtils.getTotalCount());
        dto.setTotalPage(pageUtils.getTotalPage());
        return dto;
    }

    /**
     * 获取各个平台余额
     *
     * @param gmApi
     * @param user
     * @param balanceDto
     * @return
     */
    private UserBalanceResponseDto getAllDepotBalance(TGmApi gmApi, MbrAccount user, UserBalanceResponseDto balanceDto) {
        if (Objects.nonNull(gmApi)) {
            switch (gmApi.getDepotId()) {
                case ApiConstants.DepotId.BBIN:
                    balanceDto = BBINUsrBalance(user, gmApi);
                    break;
                case ApiConstants.DepotId.AGIN:
                    balanceDto = aginUserBalance(user, gmApi);
                    break;
                case ApiConstants.DepotId.PT:
                    balanceDto = ptUserBalance(user, gmApi);
                    break;
                case ApiConstants.DepotId.PTNEW:
                    balanceDto = ptNewUserBalance(user, gmApi);
                    break;
                case ApiConstants.DepotId.NT:
                    balanceDto = ntUserBalance(user, gmApi);
                    break;
                case ApiConstants.DepotId.MG:
                    balanceDto = mgUserBalance(user, gmApi);
                    break;
                case ApiConstants.DepotId.PNG:
                    balanceDto = pngUserBalance(user, gmApi);
                    break;
                case ApiConstants.DepotId.T188:
                    balanceDto = t188UserBalance(user, gmApi);
                    break;
                case ApiConstants.DepotId.IBC:
                    balanceDto = ibcUserBalance(user, gmApi);
                    break;
                case ApiConstants.DepotId.EG:
                    balanceDto = evUserBalance(user, gmApi);
                    break;
                case ApiConstants.DepotId.OPUSSB:
                    balanceDto = opusSbUserBalance(user, gmApi);
                    break;
                case ApiConstants.DepotId.OPUSLIVE:
                    balanceDto = opusLiveUserBalance(user, gmApi);
                    break;
                case ApiConstants.DepotId.PB:
                    balanceDto = pbUserBalance(user, gmApi);
                    break;
                default:
                    balanceDto = commonUserBalance(user, gmApi);
                    break;
            }
        }
        return balanceDto;
    }

}
