package com.eveb.saasops.api.modules.transfer.service;

import com.eveb.saasops.api.modules.transfer.dto.*;
import com.eveb.saasops.api.modules.user.dto.UserBalanceResponseDto;
import com.eveb.saasops.api.modules.user.service.*;
import com.eveb.saasops.common.utils.BigDecimalMath;
import com.eveb.saasops.common.validator.Assert;
import com.eveb.saasops.modules.member.dto.AuditBonusDto;
import com.eveb.saasops.modules.member.service.AuditAccountService;
import com.eveb.saasops.modules.operate.dao.TGmDepotMapper;
import com.eveb.saasops.modules.operate.dto.BonusListDto;
import com.eveb.saasops.modules.operate.entity.OprActBonus;
import com.eveb.saasops.modules.operate.entity.TGmDepot;
import com.eveb.saasops.modules.operate.service.OprActActivityCastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.constants.ApiConstants.TransferStates;
import com.eveb.saasops.api.constants.BbinConstants;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.apisys.service.TGmApiService;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.modules.fund.entity.FundAccLog;
import com.eveb.saasops.modules.member.entity.MbrAccount;
import com.eveb.saasops.modules.member.entity.MbrBillDetail;
import com.eveb.saasops.modules.member.entity.MbrBillManage;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.service.MbrAccountService;
import com.eveb.saasops.modules.member.service.MbrBillManageService;
import com.eveb.saasops.modules.member.service.MbrWalletService;

import java.math.BigDecimal;
import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@Transactional
public class TransferService {
    @Autowired
    private BBINTransferService bbinTransferService;
    @Autowired
    private TGmApiService gmApiService;
    @Autowired
    private AginTransferService aginTransferService;
    @Autowired
    private PtTransferService ptTransferService;
    @Autowired
    private MbrBillManageService mbrBillManageService;
    @Autowired
    private AginService aginService;
    @Autowired
    private PtService ptService;
    @Autowired
    private PtNewService ptNewService;
    @Autowired
    private NtService ntService;
    @Autowired
    private T188Service t188Service;
    @Autowired
    private IbcService ibcService;
    @Autowired
    private EvService evService;
    @Autowired
    private OpusSbService opusSbService;
    @Autowired
    private OpusLiveService opusLiveService;
    @Autowired
    private MbrAccountService userService;
    @Autowired
    private NtTransferService ntTransferService;
    @Autowired
    private MgTransferService mgTransferService;
    @Autowired
    private PngTransferService pngTransferService;
    @Autowired
    private T188TransferService t188TransferService;
    @Autowired
    private IbcTransferService ibcTransferService;
    @Autowired
    private EvTransferService evTransferService;
    @Autowired
    private OpusTransferService opusTransferService;
    @Autowired
    private PbTransferService pbTransferService;
    @Autowired
    private MbrWalletService mbrWalletService;
    @Autowired
    private DepotWalletService depotWalletService;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private TGmDepotMapper depotMapper;
    @Autowired
    private OprActActivityCastService actActivityCastService;
    @Autowired
    private CommonService commonService;


    /**
     * 会员信息的操作
     *
     * @param requestDto
     * @return
     */
    private MbrAccount getMbrAccount(BillRequestDto requestDto) {
        MbrAccount account = new MbrAccount();
        account.setId(requestDto.getAccountId());
        account.setLoginName(requestDto.getLoginName());
        return account;
    }

    /**
     * 转入前的操作
     *
     * @param requestDto
     * @param sitePrefix
     * @return
     */
    public TGmApi gettGmApi(BillRequestDto requestDto, String sitePrefix) {
        MbrAccount user = userService.queryObject(requestDto.getAccountId(), sitePrefix);
        if (user.getAvailable() == MbrAccount.Status.LOCKED)
            throw new RRException("账号余额已冻结,不能转账!");
        TGmApi gmApi = gmApiService.queryApiObject(requestDto.getDepotId(), sitePrefix);
        if (gmApi == null) throw new RRException("无此API线路!");
        bbinTransferService.saveBillDepotLog(requestDto, FundAccLog.typeSign.report);
        return gmApi;
    }

    /**
     * 转入平台的操作
     *
     * @param requestDto
     * @param sitePrefix
     * @return
     */
    public BillManBooleanDto gettBillManBooleanDto(BillRequestDto requestDto, String sitePrefix) {
        TGmApi gmApi = gettGmApi(requestDto, sitePrefix);
        //TODO 查询平台余额
        requestDto.setDepotBeforeBalance(depotWalletService.queryDepotBalance(requestDto.getAccountId(), gmApi).getBalance());
        requestDto.setAmount(BigDecimalMath.formatDownRounding(requestDto.getAmount()));
        return getAllDepotTransferIn(requestDto, gmApi);
    }

    /**
     * 获取优惠及添加到操作金额中
     *
     * @param requestDto
     * @return
     */
    public OprActBonus getOprActBonus(BillRequestDto requestDto) {
        //TODO 判断是否存在优惠ID
        if (!Objects.isNull(requestDto.getBonusId())) {
            return auditAccountService.getAccountBonus(requestDto.getDepotId(), requestDto.getCatId(),
                    requestDto.getBonusId(), requestDto.getAmount(), requestDto.getAccountId());
        }
        return null;
    }

    private BillRequestDto getBillRequestDto(BillRequestDto requestDto) {
        BillRequestDto requestDto1 = new BillRequestDto();
        requestDto1.setAmount(requestDto.getAmount());
        requestDto1.setDepotId(requestDto.getDepotId());
        requestDto1.setDepotBeforeBalance(requestDto.getDepotBeforeBalance());
        requestDto1.setCatId(requestDto.getCatId());
        requestDto1.setOpType(requestDto.getOpType());
        requestDto1.setOrderNo(requestDto.getOrderNo());
        requestDto1.setIp(requestDto.getIp());
        requestDto1.setId(requestDto.getId());
        requestDto1.setAccountId(requestDto.getAccountId());
        requestDto1.setTransferSource(requestDto.getTransferSource());
        requestDto1.setTerminal(requestDto.getTerminal());
        requestDto1.setDepotName(requestDto.getDepotName());
        requestDto1.setBonusId(requestDto.getBonusId());
        requestDto1.setMemo(requestDto.getMemo());
        requestDto1.setGameId(requestDto.getGameId());
        requestDto1.setLoginName(requestDto.getLoginName());
        return requestDto1;
    }

    public TransferAmoutDto isTransferAmount(Integer depotId, Integer accountId, String loginName, String siteCode) {
        MbrAccount account = new MbrAccount();
        account.setId(accountId);
        account.setLoginName(loginName);
        AuditBonusDto auditBonusDto = auditAccountService.outAuditBonus(account, depotId);
        TransferAmoutDto amoutDto = new TransferAmoutDto();
        if (Boolean.TRUE.equals(auditBonusDto.getIsSucceed()) && Boolean.TRUE.equals(auditBonusDto.getIsFraud())) {
            TGmApi gmApi = gmApiService.queryApiObject(depotId, siteCode);
            if (isNull(gmApi)) {
                throw new RRException("无此API线路!");
            }
            BigDecimal balance = depotWalletService.queryDepotBalance(accountId, gmApi).getBalance();
            if (balance.compareTo(BigDecimal.ONE) == Constants.EVNumber.one) {
                TGmDepot depot = depotMapper.selectByPrimaryKey(depotId);
                amoutDto.setAmount(balance);
                amoutDto.setIsShow(Boolean.TRUE);
                amoutDto.setDepotName(depot.getDepotName());
            }
        }
        return amoutDto;
    }

    /**
     * 转入（公）
     *
     * @param requestDto
     * @param sitePrefix
     * @return
     */
    public TransferAmoutDto TransferIn(BillRequestDto requestDto, String sitePrefix) {
        TGmApi gmApi = gettGmApi(requestDto, sitePrefix);
        BillRequestDto requestDto1 = getBillRequestDto(requestDto);
        OprActBonus oprActBonus = getOprActBonus(requestDto);
        TransferAmoutDto amoutDto = new TransferAmoutDto();
        if (nonNull(oprActBonus)) {
            Assert.isNull(requestDto.getCatId(), "分类ID不能为空");
            requestDto.setAmount(requestDto.getAmount().add(oprActBonus.getBonusAmount()));
            requestDto.setBonusAmount(oprActBonus.getBonusAmount());
            //TODO 查询平台余额
            requestDto.setDepotBeforeBalance(depotWalletService.queryDepotBalance(requestDto.getAccountId(), gmApi).getBalance());
            if (requestDto.getDepotBeforeBalance().compareTo(BigDecimal.ONE) == Constants.EVNumber.one ||
                    requestDto.getDepotBeforeBalance().compareTo(BigDecimal.ONE) == Constants.EVNumber.zero) {
                BillBalanceDto balanceDto = TransferOut_zr(requestDto1, sitePrefix);
                if (Boolean.TRUE.equals(balanceDto.getIsTransferOut())) {
                    amoutDto.setIsShow(Boolean.TRUE);
                    amoutDto.setDepotName(gmApi.getDepotCode());
                    amoutDto.setTransferAmount(requestDto.getAmount());
                    amoutDto.setBounsAmount(oprActBonus.getBonusAmount());
                    amoutDto.setAmount(BigDecimalMath.formatDownRounding(requestDto.getDepotBeforeBalance()));
                }
            }
            //TODO 获取当个优惠券的可用余额
            BonusListDto bonusListDto = actActivityCastService.accountBonusOne(requestDto.getAccountId(), requestDto.getBonusId());
            if (bonusListDto.getWalletBalance().compareTo(requestDto.getAmount().subtract(oprActBonus.getBonusAmount())) == Constants.EVNumber.one ||
                    bonusListDto.getWalletBalance().compareTo(requestDto.getAmount().subtract(oprActBonus.getBonusAmount())) == Constants.EVNumber.zero) {
                //TODO 必须存在优惠才能转账
                BillManBooleanDto billManBooleanDto = getAllDepotTransferIn(requestDto, gmApi);
                //TODO 判断转账是否成功
                if (Boolean.TRUE.equals(billManBooleanDto.getIsTransfer()) && nonNull(oprActBonus)) {
                    auditAccountService.accountUseBonus(oprActBonus, getMbrAccount(requestDto),
                            requestDto.getAmount().subtract(oprActBonus.getBonusAmount()),
                            billManBooleanDto.getMbrBillManage().getId(), requestDto.getDepotId(), requestDto.getCatId());
                }
                if (Boolean.FALSE.equals(billManBooleanDto.getIsTransfer())
                        && billManBooleanDto.getMbrBillManage().getStatus() == Constants.EVNumber.zero
                        && nonNull(oprActBonus)) {
                    auditAccountService.accountBonusFreeze(oprActBonus, billManBooleanDto.getMbrBillManage().getId(),
                            requestDto.getAmount().subtract(oprActBonus.getBonusAmount()));
                }
            } else {
                throw new RRException("可用余额不足！");
            }
            return amoutDto;
        } else {
            throw new RRException("不存在优惠，不能转账！");
        }
    }


    /**
     * 转入→免转（免转不需转回）
     *
     * @param requestDto
     * @param sitePrefix
     * @return
     */
    public void TransferIn_mz(BillRequestDto requestDto, String sitePrefix) {
        gettBillManBooleanDto(requestDto, sitePrefix);
    }

    /**
     * 转出前操作
     *
     * @param requestDto
     * @param siteCode
     * @return
     */
    public TGmApi getAllGmApi(BillRequestDto requestDto, String siteCode) {
        TGmApi gmApi = gmApiService.queryApiObject(requestDto.getDepotId(), siteCode);
        if (gmApi == null) throw new RRException("无此API线路!");
        bbinTransferService.saveBillDepotLog(requestDto, FundAccLog.typeSign.report);
        return gmApi;
    }

    /**
     * 转出（公）
     *
     * @param requestDto
     * @param siteCode
     * @return
     */
    public AuditBonusDto TransferOut(BillRequestDto requestDto, String siteCode) {
        Boolean isTransfer = false;
        TGmApi gmApi = getAllGmApi(requestDto, siteCode);
        AuditBonusDto auditBonusDto = new AuditBonusDto();
        if (Boolean.TRUE.equals(requestDto.getIsTransferBouns())) {
            auditBonusDto = auditAccountService.outAuditBonus(getMbrAccount(requestDto), requestDto.getDepotId());
        }
        if (Boolean.TRUE.equals(auditBonusDto.getIsFraud()) && Boolean.TRUE.equals(auditBonusDto.getIsSucceed())) {
            requestDto.setDepotBeforeBalance(depotWalletService.queryDepotBalance(requestDto.getAccountId(), gmApi).getBalance());
            int isSgin = requestDto.getDepotBeforeBalance().compareTo(BigDecimal.ONE);
            if (isSgin == Constants.EVNumber.one || isSgin == Constants.EVNumber.zero) {
                requestDto.setDepotBeforeBalance(BigDecimalMath.formatDownRounding(requestDto.getDepotBeforeBalance()));
                isTransfer = getAllDepotTransferOut(requestDto, gmApi);
            } else {
                throw new RRException("平台余额小于1!");
            }
        }
        if (Boolean.TRUE.equals(isTransfer))
            auditAccountService.succeedAuditBonus(getMbrAccount(requestDto), requestDto.getDepotId());
        return auditBonusDto;
    }

    /**
     * 转出→转入（转入的转出）
     *
     * @param requestDto
     * @param siteCode
     * @return
     */
    public BillBalanceDto TransferOut_zr(BillRequestDto requestDto, String siteCode) {
        Boolean isTransfer = false;
        TGmApi gmApi = getAllGmApi(requestDto, siteCode);
        AuditBonusDto auditBonusDto = auditAccountService.outAuditBonus(getMbrAccount(requestDto), requestDto.getDepotId());
        BillBalanceDto billBalanceDto = new BillBalanceDto();
        if (Boolean.TRUE.equals(auditBonusDto.getIsFraud()) && Boolean.TRUE.equals(auditBonusDto.getIsSucceed())) {
            requestDto.setDepotBeforeBalance(depotWalletService.queryDepotBalance(requestDto.getAccountId(), gmApi).getBalance());
            billBalanceDto.setDepotBeforeBalance(requestDto.getDepotBeforeBalance());
            billBalanceDto.setDepotName(requestDto.getDepotName());
            requestDto.setAmount(BigDecimalMath.formatDownRounding(requestDto.getDepotBeforeBalance()));
            isTransfer = getAllDepotTransferOut(requestDto, gmApi);
            billBalanceDto.setIsTransferOut(Boolean.TRUE);
        }
        if (Boolean.TRUE.equals(isTransfer)) {
            auditAccountService.succeedAuditBonus(getMbrAccount(requestDto), requestDto.getDepotId());
        }
        return billBalanceDto;
    }

    public R checkTransfer(Long orderNo, String siteCode) {
        MbrBillManage mbrBillManage = mbrBillManageService.queryOrderNo(orderNo);
        if (Objects.isNull(mbrBillManage)) throw new RRException("无此订单,请确认订单号是否正确,或此订单已更新!");
        TGmApi gmApi = gmApiService.queryApiObject(mbrBillManage.getDepotId(), siteCode);
        if (Objects.isNull(gmApi)) throw new RRException("无此API线路!");
        Integer state = null;
        //TODO 所有接查询订单状态返回结果需要转成平台统一格式
        switch (mbrBillManage.getDepotId()) {
            //TODO 有处理中这种状态
            case ApiConstants.DepotId.BBIN:
                BbinResponseDto bbinResponseDto = bbinTransferService.checkTransfer(orderNo, gmApi);
                if (bbinResponseDto.getData().getStatus().equals(BbinConstants.BBIN_TRF_FAIL)) {
                    if (mbrBillManage.getIsTimeOut() == Constants.EVNumber.one) {
                        state = TransferStates.fail;
                    } else {
                        state = TransferStates.progress;
                    }
                } else if (bbinResponseDto.getData().getStatus().equals(BbinConstants.BBIN_TRF_SUC)) {
                    state = TransferStates.suc;
                }
                break;
            case ApiConstants.DepotId.AGIN:
                //TODO FIXME L77_AGIN9321238699966464,this order is not exist in the system! info=1
                //TODO 但文档中 为 1是订单未处理中所以这里为 1暂未处理，后期再处理
                state = aginService.queryOrderStatus(orderNo, gmApi);
                break;
            case ApiConstants.DepotId.PT:
                state = ptService.checktransaction(orderNo.toString(), gmApi);
                break;
            //TODO 有处理中这种状态
            case ApiConstants.DepotId.PTNEW:
                state = ptNewService.checktransaction(orderNo.toString(), gmApi);
                break;
            case ApiConstants.DepotId.NT:
                state = ntService.checktransaction(orderNo.toString(), gmApi);
                break;
            case ApiConstants.DepotId.T188:
                state = t188Service.checktransaction(orderNo.toString(), gmApi);
                break;
            //TODO 0 还行 成功执行 1 失败 系统错误 2 挂起 状态未知，调用
            case ApiConstants.DepotId.IBC:
                state = ibcService.checktransaction(orderNo.toString(), mbrBillManage.getLoginName(), gmApi);
                break;
            //TODO 0 成功 1 失败, 订单未处理状态 2 因无效的转账金额引致的失败
            case ApiConstants.DepotId.EG:
                state = evService.queryOrderStatus(orderNo, gmApi);
                break;
            case ApiConstants.DepotId.OPUSSB:
                state = opusSbService.queryOrderStatus(orderNo.toString(), mbrBillManage.getLoginName(), gmApi);
                break;
            case ApiConstants.DepotId.OPUSLIVE:
                state = opusLiveService.queryOrderStatus(orderNo.toString(), mbrBillManage.getLoginName(), gmApi);
                break;
            case ApiConstants.DepotId.MG:
            case ApiConstants.DepotId.PNG:
            case ApiConstants.DepotId.PB:
                throw new RRException("此平台无单个订单状态查询!");
            default:
                break;
        }
        if (!StringUtils.isEmpty(state)) {
            if (state == TransferStates.suc) {
                if (mbrBillManage.getOpType() == Constants.TransferType.out) {
                    bbinTransferService.updateMbrBillManageStatus(mbrBillManage, Constants.manageStatus.succeed);
                } else if (mbrBillManage.getOpType() == Constants.TransferType.into) {
                    MbrBillDetail mbrBillDetail = bbinTransferService.setMbrBillDetail(mbrBillManage);
                    mbrBillManage.setBeforeBalance(mbrBillDetail.getBeforeBalance());
                    mbrBillManage.setAfterBalance(mbrBillDetail.getAfterBalance());
                    bbinTransferService.transferOutSuc(mbrBillDetail, mbrBillManage, Constants.manageStatus.succeed);
                }
                state = Constants.EVNumber.one;
            } else if (state == TransferStates.fail) {
                if (mbrBillManage.getOpType() == Constants.TransferType.out) {
                    bbinTransferService.transferBust(mbrBillManage);
                } else if (mbrBillManage.getOpType() == Constants.TransferType.into) {
                    bbinTransferService.updateMbrBillManageStatus(mbrBillManage, Constants.manageStatus.defeated);
                }
                state = Constants.EVNumber.two;
            } else if (state == TransferStates.progress) {
                throw new RRException("第三方正在处理中,请稍等!");
            }
            if (mbrBillManage.getStatus() == Constants.EVNumber.one || mbrBillManage.getStatus() == Constants.EVNumber.two) {
                auditAccountService.accountBonusFreeze(mbrBillManage);
            }
        }
        return R.ok().put("state", state);
    }

    private BillManBooleanDto getAllDepotTransferIn(BillRequestDto requestDto, TGmApi gmApi) {
        BillManBooleanDto billManBooleanDto;
        switch (requestDto.getDepotId()) {
            case ApiConstants.DepotId.BBIN:
                MbrDepotWallet mbrWallet = bbinTransferService.checkReg(gmApi, requestDto.getAccountId(), requestDto.getLoginName());
                if (mbrWallet.getIsTransfer() == MbrDepotWallet.IsTransFer.no) {
                    mbrWalletService.updateTransfer(mbrWallet.getId());
                }
                MbrBillManage mbrBillManage = bbinTransferService.billChargeIn(requestDto);
                billManBooleanDto = bbinTransferService.sendTransferIn(mbrBillManage, gmApi);
                return billManBooleanDto;
            case ApiConstants.DepotId.AGIN:
                billManBooleanDto = aginTransferService.aginTransferIn(requestDto, gmApi);
                return billManBooleanDto;
            case ApiConstants.DepotId.PT:
                billManBooleanDto = ptTransferService.ptTransferIn(requestDto, gmApi);
                return billManBooleanDto;
            case ApiConstants.DepotId.PTNEW:
                billManBooleanDto = ptTransferService.ptNewTransferIn(requestDto, gmApi);
                return billManBooleanDto;
            case ApiConstants.DepotId.NT:
                billManBooleanDto = ntTransferService.ntTransferIn(requestDto, gmApi);
                return billManBooleanDto;
            case ApiConstants.DepotId.MG:
                billManBooleanDto = mgTransferService.mgTransferIn(requestDto, gmApi);
                return billManBooleanDto;
            case ApiConstants.DepotId.PNG:
                billManBooleanDto = pngTransferService.pngTransferIn(requestDto, gmApi);
                return billManBooleanDto;
            case ApiConstants.DepotId.T188:
                billManBooleanDto = t188TransferService.t188TransferIn(requestDto, gmApi);
                return billManBooleanDto;
            case ApiConstants.DepotId.IBC:
                billManBooleanDto = ibcTransferService.ibcTransferIn(requestDto, gmApi);
                return billManBooleanDto;
            case ApiConstants.DepotId.EG:
                billManBooleanDto = evTransferService.evTransferIn(requestDto, gmApi);
                return billManBooleanDto;
            case ApiConstants.DepotId.OPUSSB:
                billManBooleanDto = opusTransferService.opusSbTransferIn(requestDto, gmApi);
                return billManBooleanDto;
            case ApiConstants.DepotId.OPUSLIVE:
                billManBooleanDto = opusTransferService.opusLiveTransferIn(requestDto, gmApi);
                return billManBooleanDto;
            case ApiConstants.DepotId.PB:
                billManBooleanDto = pbTransferService.pbTransferIn(requestDto, gmApi);
                return billManBooleanDto;
            default:
                billManBooleanDto = commonService.commonTransferIn(requestDto, gmApi);
                return billManBooleanDto;
        }
    }

    private Boolean getAllDepotTransferOut(BillRequestDto requestDto, TGmApi gmApi) {
        Boolean isTransfer;
        switch (requestDto.getDepotId()) {
            case ApiConstants.DepotId.BBIN:
                isTransfer = bbinTransferService.sendTransferOut(bbinTransferService.bbINBillChargeOut(requestDto, gmApi), gmApi);
                break;
            case ApiConstants.DepotId.AGIN:
                isTransfer = aginTransferService.aginTransferOut(requestDto, gmApi);
                break;
            case ApiConstants.DepotId.PT:
                isTransfer = ptTransferService.ptTransferOut(requestDto, gmApi);
                break;
            case ApiConstants.DepotId.PTNEW:
                isTransfer = ptTransferService.ptNewTransferOut(requestDto, gmApi);
                break;
            case ApiConstants.DepotId.NT:
                isTransfer = ntTransferService.ntTransferOut(requestDto, gmApi);
                break;
            case ApiConstants.DepotId.MG:
                isTransfer = mgTransferService.mgTransferOut(requestDto, gmApi);
                break;
            case ApiConstants.DepotId.PNG:
                isTransfer = pngTransferService.pngTransferOut(requestDto, gmApi);
                break;
            case ApiConstants.DepotId.T188:
                isTransfer = t188TransferService.t188TransferOut(requestDto, gmApi);
                break;
            case ApiConstants.DepotId.IBC:
                isTransfer = ibcTransferService.ibcTransferOut(requestDto, gmApi);
                break;
            case ApiConstants.DepotId.EG:
                isTransfer = evTransferService.evTransferOut(requestDto, gmApi);
                break;
            case ApiConstants.DepotId.OPUSSB:
                isTransfer = opusTransferService.opusSbTransferOut(requestDto, gmApi);
                break;
            case ApiConstants.DepotId.OPUSLIVE:
                isTransfer = opusTransferService.opusLiveTransferOut(requestDto, gmApi);
                break;
            case ApiConstants.DepotId.PB:
                isTransfer = pbTransferService.pbTransferOut(requestDto, gmApi);
                break;
            default:
                isTransfer = commonService.commonTransferOut(requestDto, gmApi);
                break;
        }
        return isTransfer;
    }

}
