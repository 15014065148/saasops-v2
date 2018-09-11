package com.eveb.saasops.modules.member.service;

import static com.eveb.saasops.common.utils.DateUtil.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.math.BigDecimal;
import java.util.*;

import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.apisys.service.TGmApiService;
import com.eveb.saasops.api.modules.user.dto.UserBalanceResponseDto;
import com.eveb.saasops.api.modules.user.service.DepotWalletService;
import com.eveb.saasops.common.constants.OrderConstants;
import com.eveb.saasops.common.exception.R200Exception;
import com.eveb.saasops.common.utils.*;
import com.eveb.saasops.modules.fund.dao.FundAuditMapper;
import com.eveb.saasops.modules.fund.entity.FundAudit;
import com.eveb.saasops.modules.member.dao.*;
import com.eveb.saasops.modules.member.dto.AuditBonusDto;
import com.eveb.saasops.modules.member.dto.AuditDetailDto;
import com.eveb.saasops.modules.member.dto.AuditInfoDto;
import com.eveb.saasops.modules.member.entity.*;
import com.eveb.saasops.modules.member.mapper.AuditMapper;
import com.eveb.saasops.modules.operate.dao.OprActBonusMapper;
import com.eveb.saasops.modules.operate.dao.TGmDepotMapper;
import com.eveb.saasops.modules.operate.entity.OprActBonus;
import com.eveb.saasops.modules.operate.entity.TGmDepot;
import com.eveb.saasops.modules.operate.service.OprActActivityCastService;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.utils.Collections3;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@Transactional
public class AuditAccountService {

    @Autowired
    private MbrAuditAccountMapper auditAccountMapper;
    @Autowired
    private AuditMapper auditMapper;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private MbrWithdrawalCondMapper withdrawalCondMapper;
    @Autowired
    private OprActBonusMapper actBonusMapper;
    @Autowired
    private OprActActivityCastService actActivityCastService;
    @Autowired
    private MbrAuditBonusMapper auditBonusMapper;
    @Autowired
    private MbrWalletService walletService;
    @Autowired
    private AuditCastService auditCastService;
    @Autowired
    private MbrAuditFraudMapper auditFraudMapper;
    @Autowired
    private MbrAuditHistoryMapper auditHistoryMapper;
    @Autowired
    private DepotWalletService depotWalletService;
    @Autowired
    private TGmApiService gmApiService;
    @Autowired
    private MbrWalletMapper mbrWalletMapper;
    @Autowired
    private FundAuditMapper fundAuditMapper;
    @Autowired
    private MbrGroupMapper groupMapper;
    @Autowired
    private TGmDepotMapper depotMapper;
    @Autowired
    private MbrBillDetailMapper billDetailMapper;


    public AuditInfoDto immediatelyAudit(String loginName) {
        MbrAccount account = new MbrAccount();
        account.setLoginName(loginName);
        MbrAccount mbrAccount = accountMapper.selectOne(account);
        if (isNull(mbrAccount)) {
            throw new R200Exception("无此会员，请核实会员名");
        }
        List<AuditDetailDto> detailDtos =
                auditMapper.findAuditAccountList
                        (mbrAccount.getId(), null, null, Constants.EVNumber.zero);
        if (Collections3.isNotEmpty(detailDtos)) {
            AuditInfoDto auditInfoDto = casAuditInfoDto(detailDtos);
            auditInfoDto.getAuditDetailDtos().stream().forEach(ds -> {
                BigDecimal validBet = ds.getRemainValidBet().add(ds.getValidBet());
                if (ds.getIsOut() == Constants.EVNumber.one
                        && validBet.compareTo(ds.getValidBet()) == -1) {
                    ds.setStatus(Constants.EVNumber.two);
                }
            });
            return auditInfoDto;
        }
        return null;
    }

    public PageUtils auditHistoryList(String loginName, Integer pageNo, Integer pageSize) {
        MbrAccount account = new MbrAccount();
        account.setLoginName(loginName);
        MbrAccount mbrAccount = accountMapper.selectOne(account);
        if (nonNull(mbrAccount)) {
            PageHelper.startPage(pageNo, pageSize);
            MbrAuditHistory auditHistory = new MbrAuditHistory();
            auditHistory.setAccountId(mbrAccount.getId());
            auditHistory.setIsSign(Constants.EVNumber.one);
            List<MbrAuditHistory> auditHistories = auditMapper.findMbrAuditHistory(auditHistory);
            auditHistories.stream().forEach(as -> {
                List<AuditDetailDto> detailDtos =
                        auditMapper.findAuditAccountList(
                                mbrAccount.getId(), as.getStartTime(), as.getEndTime(), Constants.EVNumber.one);
                if (Collections3.isNotEmpty(detailDtos)) {
                    detailDtos.stream().forEach(ds -> {
                        BigDecimal validBet = ds.getRemainValidBet().add(ds.getValidBet());
                        if (ds.getIsOut() == Constants.EVNumber.one
                                && validBet.compareTo(ds.getValidBet()) == -1) {
                            ds.setStatus(Constants.EVNumber.two);
                        }
                    });
                }
                as.setAuditInfoDto(casAuditInfoDto(detailDtos));
            });
            return BeanUtil.toPagedResult(auditHistories);
        }
        return new PageUtils();
    }

    public Map<String, Object> findAccouGroupByName(String loginName) {
        MbrAccount account = new MbrAccount();
        account.setLoginName(loginName);
        account = accountMapper.selectOne(account);
        if (isNull(account)) {
            throw new R200Exception("无此会员，请核实会员名");
        }
        MbrGroup group = groupMapper.selectByPrimaryKey(account.getGroupId());
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("GroupName", group.getGroupName());
        MbrWithdrawalCond withdrawalCond = getMbrWithdrawalCond(account.getId());
        if (Objects.nonNull(withdrawalCond)) {
            objectMap.put("OverFee", Objects.nonNull(withdrawalCond.getOverFee())
                    ? withdrawalCond.getOverFee() : BigDecimal.ZERO);
        }
        return objectMap;
    }

    public List<AuditDetailDto> auditHistoryBonusList(String loginName) {
        MbrAccount account = new MbrAccount();
        account.setLoginName(loginName);
        MbrAccount mbrAccount = accountMapper.selectOne(account);
        if (nonNull(mbrAccount)) {
            MbrAuditHistory auditHistory = new MbrAuditHistory();
            auditHistory.setAccountId(mbrAccount.getId());
            auditHistory.setIsSign(Constants.EVNumber.zero);
            List<MbrAuditHistory> auditHistories = auditMapper.findMbrAuditHistory(auditHistory);
            if (Collections3.isNotEmpty(auditHistories)) {
                return auditMapper.findAuditAccountList(mbrAccount.getId(),
                        auditHistories.get(0).getStartTime(),
                        getCurrentDate(FORMAT_18_DATE_TIME), Constants.EVNumber.one);
            }
        }
        return null;
    }

    public MbrAuditBonus auditBonusInfo(Integer auditBonusId) {
        MbrAuditBonus auditBonus = auditBonusMapper.selectByPrimaryKey(auditBonusId);
        if (nonNull(auditBonus) && nonNull(auditBonus.getOrderNo())) {
            MbrAuditFraud auditFraud = new MbrAuditFraud();
            auditFraud.setOrderNo(auditBonus.getOrderNo());
            auditBonus.setAuditFrauds(auditFraudMapper.select(auditFraud));
        }
        return auditBonus;
    }


    public Map<String, Object> isWithdrawal(Integer accountId) {
        List<MbrAuditAccount> accountAudits = getMbrAuditAccounts(accountId);
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("isPassed", Boolean.TRUE);
        if (Collections3.isNotEmpty(accountAudits)) {
            long count = accountAudits.stream().filter(
                    as -> Constants.EVNumber.zero == as.getStatus())
                    .map(MbrAuditAccount::getId).count();
            if (count > Constants.EVNumber.zero) {
                hashMap.put("isPassed", Boolean.FALSE);
            }
        }
        return hashMap;
    }

    public List<MbrAuditAccount> getMbrAuditAccounts(Integer accountId) {
        MbrAuditAccount accountAudit = new MbrAuditAccount();
        accountAudit.setIsDrawings(Constants.EVNumber.zero);
        accountAudit.setAccountId(accountId);
        return auditMapper.finAuditList(accountAudit);
    }


    private AuditInfoDto casAuditInfoDto(List<AuditDetailDto> detailDtos) {
        AuditInfoDto infoDto = new AuditInfoDto();
        if (Collections3.isNotEmpty(detailDtos)) {
            castDepositValue(infoDto, detailDtos);
            castBounsValue(infoDto, detailDtos);
            long DepositCount = detailDtos.stream().filter(
                    as -> Constants.EVNumber.zero == as.getStatus()
                            && as.getAuditType() == Constants.EVNumber.zero)
                    .map(AuditDetailDto::getId).count();
            long bounsCount = detailDtos.stream().filter(
                    as -> Constants.EVNumber.zero == as.getStatus()
                            && as.getAuditType() == Constants.EVNumber.one)
                    .map(AuditDetailDto::getId).count();
            if (DepositCount > 0) {
                infoDto.setDepositSucceed(Boolean.FALSE);
            }
            if (bounsCount > 0) {
                infoDto.setDbounsSucceed(Boolean.FALSE);
            }
            infoDto.setAuditDetailDtos(detailDtos);
        }
        return infoDto;
    }

    private void castDepositValue(AuditInfoDto infoDto, List<AuditDetailDto> detailDtos) {
        Optional<BigDecimal> depositTotal = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.zero &&
                        nonNull(p.getDepositAmount())
                        && p.getDepositAmount().compareTo(BigDecimal.ZERO) == 1
                        && p.getDiscardAmount().compareTo(BigDecimal.ZERO) == 0)
                .map(AuditDetailDto::getDepositAmount).reduce(BigDecimal::add);

        Optional<BigDecimal> depositDiscardAmount = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.zero &&
                        nonNull(p.getDiscardAmount())
                        && p.getDiscardAmount().compareTo(BigDecimal.ZERO) == 1)
                .map(AuditDetailDto::getDiscardAmount).reduce(BigDecimal::add);


        Optional<BigDecimal> depositValidBet = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.zero &&
                        nonNull(p.getAuditAmount()))
                .map(AuditDetailDto::getAuditAmount).reduce(BigDecimal::add);
        Optional<BigDecimal> currentDepositValidBet = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.zero &&
                        nonNull(p.getValidBet()))
                .map(AuditDetailDto::getValidBet).reduce(BigDecimal::add);

        BigDecimal bigDecimalDeposit = depositTotal.isPresent()
                ? depositTotal.get() : BigDecimal.ZERO;
        BigDecimal bigDecimalDiscard = depositDiscardAmount.isPresent()
                ? depositDiscardAmount.get() : BigDecimal.ZERO;
        infoDto.setDepositTotal(bigDecimalDeposit.add(bigDecimalDiscard));
        infoDto.setDepositValidBet(depositValidBet.isPresent()
                ? depositValidBet.get() : BigDecimal.ZERO);
        infoDto.setCurrentDepositValidBet(currentDepositValidBet.isPresent()
                ? currentDepositValidBet.get() : BigDecimal.ZERO);

        Optional<BigDecimal> depositNotAuditAmount = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.zero
                        && p.getStatus() == Constants.EVNumber.zero
                        && nonNull(p.getAuditAmount()))
                .map(AuditDetailDto::getAuditAmount).reduce(BigDecimal::add);

        Optional<BigDecimal> depositNotValidBet = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.zero
                        && p.getStatus() == Constants.EVNumber.zero
                        && nonNull(p.getValidBet()))
                .map(AuditDetailDto::getValidBet).reduce(BigDecimal::add);

        infoDto.setDepositResidueValidBet(depositNotAuditAmount.isPresent()
                && depositNotValidBet.isPresent()
                ? depositNotAuditAmount.get().subtract(depositNotValidBet.get())
                : BigDecimal.ZERO);

    }

    private void castBounsValue(AuditInfoDto infoDto, List<AuditDetailDto> detailDtos) {
        Optional<BigDecimal> transferTotal = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.one &&
                        nonNull(p.getDepositAmount()))
                .map(AuditDetailDto::getDepositAmount).reduce(BigDecimal::add);
        Optional<BigDecimal> bonusAmount = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.one &&
                        nonNull(p.getDiscountAmount()))
                .map(AuditDetailDto::getDiscountAmount).reduce(BigDecimal::add);
        Optional<BigDecimal> transferValidBet = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.one &&
                        nonNull(p.getAuditAmount()))
                .map(AuditDetailDto::getAuditAmount).reduce(BigDecimal::add);
        Optional<BigDecimal> currentTransferValidBet = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.one &&
                        nonNull(p.getValidBet()))
                .map(AuditDetailDto::getValidBet).reduce(BigDecimal::add);
        infoDto.setTransferTotal(transferTotal.isPresent()
                ? transferTotal.get() : BigDecimal.ZERO);
        infoDto.setBonusAmount(bonusAmount.isPresent()
                ? bonusAmount.get() : BigDecimal.ZERO);
        infoDto.setTransferValidBet(transferValidBet.isPresent()
                ? transferValidBet.get() : BigDecimal.ZERO);
        infoDto.setCurrentTransferValidBet(currentTransferValidBet.isPresent()
                ? currentTransferValidBet.get() : BigDecimal.ZERO);

        Optional<BigDecimal> bonusNotAuditAmount = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.one
                        && p.getStatus() == Constants.EVNumber.zero
                        && nonNull(p.getAuditAmount()))
                .map(AuditDetailDto::getAuditAmount).reduce(BigDecimal::add);

        Optional<BigDecimal> bonusNotValidBet = detailDtos.stream()
                .filter(p -> p.getAuditType() == Constants.EVNumber.one
                        && p.getStatus() == Constants.EVNumber.zero
                        && nonNull(p.getValidBet()))
                .map(AuditDetailDto::getValidBet).reduce(BigDecimal::add);

        infoDto.setBonusResidueValidBet(bonusNotAuditAmount.isPresent()
                && bonusNotValidBet.isPresent()
                ? bonusNotAuditAmount.get().subtract(bonusNotValidBet.get())
                : BigDecimal.ZERO);
    }

    public void updateAccountAuditList(List<AuditDetailDto> detailDtos, String userName) {
        detailDtos.stream().forEach(audit -> {
            if (audit.getAuditType() == Constants.EVNumber.zero) {
                MbrAuditAccount accountAudit = auditAccountMapper.selectByPrimaryKey(audit.getId());
                if (accountAudit.getStatus() == Constants.EVNumber.zero) {
                    accountAudit.setModifyUser(userName);
                    accountAudit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
                    accountAudit.setAuditAmount(nonNull(audit.getAuditAmount())
                            ? audit.getAuditAmount() : BigDecimal.ZERO);
                    auditAccountMapper.updateByPrimaryKey(accountAudit);
                }
            }
            if (audit.getAuditType() == Constants.EVNumber.one) {
                MbrAuditBonus auditBonus = auditBonusMapper.selectByPrimaryKey(audit.getId());
                if (auditBonus.getIsValid() == Constants.EVNumber.one
                        && auditBonus.getStatus() == Constants.EVNumber.zero
                        && auditBonus.getIsDrawings() == Constants.EVNumber.zero) {
                    auditBonus.setModifyUser(userName);
                    auditBonus.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
                    auditBonus.setAuditAmount(nonNull(audit.getAuditAmount())
                            ? audit.getAuditAmount() : BigDecimal.ZERO);
                    auditBonusMapper.updateByPrimaryKey(auditBonus);
                }
            }
        });
    }

    public void clearAccountAudit(String loginName, String userName, String siteCode, String memo) {
        MbrAccount account = new MbrAccount();
        account.setLoginName(loginName);
        MbrAccount mbrAccount = accountMapper.selectOne(account);
        if (nonNull(mbrAccount)) {
            MbrAuditAccount accountAudit = new MbrAuditAccount();
            accountAudit.setAccountId(mbrAccount.getId());
            accountAudit.setIsDrawings(Constants.EVNumber.zero);
            accountAudit.setStatus(Constants.EVNumber.zero);
            List<MbrAuditAccount> audits = auditAccountMapper.select(accountAudit);
            audits.stream().forEach(audit -> {
                audit.setModifyUser(userName);
                audit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
                audit.setAuditAmount(BigDecimal.ZERO);
                audit.setStatus(Constants.EVNumber.one);
                if (StringUtils.isNotEmpty(memo)) {
                    audit.setMemo(memo);
                }
                auditAccountMapper.updateByPrimaryKey(audit);
            });

            MbrAuditBonus auditBonus = new MbrAuditBonus();
            auditBonus.setAccountId(mbrAccount.getId());
            auditBonus.setIsDrawings(Constants.EVNumber.zero);
            auditBonus.setStatus(Constants.EVNumber.zero);
            List<MbrAuditBonus> auditBonuses = auditBonusMapper.select(auditBonus);
            auditBonuses.stream().forEach(bonus -> {
                if (bonus.getIsValid() == Constants.EVNumber.one
                        && bonus.getIsDrawings() == Constants.EVNumber.zero) {
                    bonus.setModifyUser(userName);
                    bonus.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
                    bonus.setAuditAmount(BigDecimal.ZERO);
                    bonus.setStatus(Constants.EVNumber.one);
                    if (StringUtils.isNotEmpty(memo)) {
                        bonus.setMemo(memo);
                    }
                    auditBonusMapper.updateByPrimaryKey(bonus);
                }
            });
            auditCastService.doingCronAuditAccount(siteCode, mbrAccount.getId());
        }
    }

    public AuditBonusDto auditDetail(Integer accountId) {
        AuditBonusDto dto = new AuditBonusDto();
        List<MbrAuditAccount> auditAccounts = getMbrAuditAccounts(accountId);
        if (Collections3.isNotEmpty(auditAccounts)) {
            long count = auditAccounts.stream().filter(
                    as -> Constants.EVNumber.zero == as.getStatus())
                    .map(MbrAuditAccount::getId).count();
            if (count > 0) {
                dto.setIsSucceed(Boolean.FALSE);
                Optional<BigDecimal> totalValidBet = auditAccounts.stream()
                        .filter(p -> nonNull(p.getAuditAmount()))
                        .map(MbrAuditAccount::getAuditAmount).reduce(BigDecimal::add);
                dto.setTotalValidBet(totalValidBet.get());
                dto.setResidueValidBet(getResidueValidBet(auditAccounts));
            }
        }
        return dto;
    }

    private BigDecimal getResidueValidBet(List<MbrAuditAccount> auditAccounts) {
        BigDecimal bigDecimal = BigDecimal.ZERO;
        for (MbrAuditAccount auditAccount : auditAccounts) {
            if (auditAccount.getStatus() == Constants.EVNumber.zero) {
                BigDecimal bigDecimal1 = auditAccount.getAuditAmount()
                        .subtract(auditAccount.getValidBet());
                bigDecimal = bigDecimal.add(bigDecimal1);
            }
        }
        return bigDecimal;
    }

    public MbrAuditAccount insertAccountAudit(Integer accountId, BigDecimal depositAmount, Integer depositId, Integer auditMultiple) {
        MbrAuditAccount accountAudit = new MbrAuditAccount();
        accountAudit.setDepositBalance(BigDecimal.ZERO);
        MbrWithdrawalCond withdrawalCond = getMbrWithdrawalCond(accountId);
        Integer depositAudit = Constants.EVNumber.zero;
        BigDecimal depositOutBalance = BigDecimal.ZERO;
        if (Objects.nonNull(withdrawalCond)) {
            depositAudit = Objects.nonNull(withdrawalCond.getWithDrawalAudit()) ?
                    withdrawalCond.getWithDrawalAudit() : withdrawalCond.getWithDrawalAudit();
            depositOutBalance = Objects.nonNull(withdrawalCond.getOverFee()) ?
                    withdrawalCond.getOverFee() : BigDecimal.ZERO;
        }
        if (isNull(auditMultiple)) {
            accountAudit.setAuditAmount(CommonUtil.adjustScale(
                    new BigDecimal(depositAudit).multiply(depositAmount)));
            accountAudit.setDepositAudit(new BigDecimal(depositAudit));
        }
        if (nonNull(auditMultiple)) {
            accountAudit.setAuditAmount(CommonUtil.adjustScale(
                    new BigDecimal(auditMultiple).multiply(depositAmount)));
            accountAudit.setDepositAudit(new BigDecimal(auditMultiple));
        }
        accountAudit.setDepositOutBalance(depositOutBalance);
        MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
        accountAudit.setLoginName(account.getLoginName());
        accountAudit.setAccountId(accountId);
        accountAudit.setTime(getCurrentDate(FORMAT_18_DATE_TIME));
        accountAudit.setDepositAmount(depositAmount);
        accountAudit.setIsDrawings(Constants.EVNumber.zero);
        accountAudit.setDepositId(depositId);
        accountAudit.setPayOut(BigDecimal.ZERO);
        accountAudit.setDiscardAmount(BigDecimal.ZERO);
        accountAudit.setIsOut(Constants.EVNumber.zero);
        accountAudit.setStatus(Constants.EVNumber.zero);
        accountAudit.setValidBet(BigDecimal.ZERO);
        accountAudit.setRemainValidBet(BigDecimal.ZERO);
        accountAudit.setBonusRemainValidBet(BigDecimal.ZERO);
        auditAccountMapper.insert(accountAudit);
        return accountAudit;
    }

    public MbrWithdrawalCond getMbrWithdrawalCond(int accountId) {
        MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
        MbrWithdrawalCond withdrawalCond = new MbrWithdrawalCond();
        withdrawalCond.setGroupId(account.getGroupId());
        return withdrawalCondMapper.selectOne(withdrawalCond);
    }


    public AuditBonusDto getDepotAuditDto(Integer accountId, Integer depotId) {
        MbrAccount account = accountMapper.selectByPrimaryKey(accountId);
        AuditBonusDto bonusDto = outAuditBonus(account, depotId);
        return nonNull(bonusDto) ? bonusDto : new AuditBonusDto();
    }


    public Boolean isBounsOut(Integer accountId, Integer depotId) {
        List<MbrAuditBonus> auditBonuses = auditCastService.
                findMbrAuditBonusByAccountId(accountId, depotId, null);
        if (Collections3.isNotEmpty(auditBonuses)) {
            MbrAuditBonus auditBonus = auditBonuses.get(0);
            long count = auditBonuses.stream().filter(p ->
                    Constants.EVNumber.zero == p.getStatus())
                    .map(MbrAuditBonus::getId).count();
            if (auditBonus.getIsValid() == Constants.EVNumber.zero) {
                return Boolean.FALSE;
            }
            if (count > 0) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 转入得到优惠
     *
     * @param depotId
     * @param catId
     * @param bonusId
     * @param amount
     * @param accountId
     * @return
     */
    public OprActBonus getAccountBonus(Integer depotId, Integer catId, Integer bonusId,
                                       BigDecimal amount, Integer accountId) {
        OprActBonus bonus = actBonusMapper.selectByPrimaryKey(bonusId);
        if (isNull(bonus) || !bonus.getAccountId().equals(accountId)) {
            throw new R200Exception("优惠不存在");
        }
        if (bonus.getStatus() != Constants.EVNumber.three) {
            throw new R200Exception("优惠不可使用");
        }
        checkoutAuditBonus(bonus, depotId, catId);
        return actActivityCastService.billManageBonus(depotId, bonus, amount);
    }

    private void checkoutAuditBonus(OprActBonus bonus, Integer depotId, Integer catId) {
        MbrAuditBonus auditBonus = new MbrAuditBonus();
        auditBonus.setIsDrawings(Constants.EVNumber.zero);
        auditBonus.setDepotId(depotId);
        auditBonus.setAccountId(bonus.getAccountId());
        List<MbrAuditBonus> auditBonuses = auditBonusMapper.select(auditBonus);
        if (Collections3.isNotEmpty(auditBonuses)) {
            auditBonuses.stream().forEach(as -> {
                if (as.getCatId() != catId) {
                    throw new R200Exception("不同类型优惠同平台不能同时使用");
                }
            });
        }
    }

    /**
     * 转入 状态待处理 即冻结service
     * * @param amount 转账金额
     */
    public void accountBonusFreeze(OprActBonus bonus, Integer billManageId, BigDecimal amount) {
        bonus.setStatus(Constants.EVNumber.one);
        bonus.setBillDetailId(billManageId);
        bonus.setTransferAmount(amount);
        actBonusMapper.updateByPrimaryKey(bonus);
    }

    /**
     * 会员转账手动刷新 更新优惠service
     * * @param amount 转账金额
     */
    public void accountBonusFreeze(MbrBillManage mbrBillManage) {
        OprActBonus bonus = actBonusMapper.selectByPrimaryKey(mbrBillManage.getBonusId());
        if (mbrBillManage.getStatus() == Constants.EVNumber.one) {
            MbrAccount account = new MbrAccount();
            account.setId(mbrBillManage.getAccountId());
            account.setLoginName(mbrBillManage.getLoginName());
            accountUseBonus(bonus, account, mbrBillManage.getAmount(), mbrBillManage.getId(),
                    mbrBillManage.getDepotId(), mbrBillManage.getCatId());
        }
        if (mbrBillManage.getStatus() == Constants.EVNumber.two) {
            bonus.setStatus(Constants.EVNumber.three);
            bonus.setBillDetailId(mbrBillManage.getId());
            bonus.setTransferAmount(mbrBillManage.getAmount());
            actBonusMapper.updateByPrimaryKey(bonus);
        }
    }


    /**
     * 转入成功
     *
     * @param bonus
     * @param depotId
     * @param catId
     */
    public void accountUseBonus(OprActBonus bonus, MbrAccount account, BigDecimal amount,
                                Integer billManageId, Integer depotId, Integer catId) {
        MbrAuditBonus bonusAudit = new MbrAuditBonus();
        bonusAudit.setAccountId(account.getId());
        bonusAudit.setLoginName(account.getLoginName());
        bonusAudit.setTime(getCurrentDate(FORMAT_18_DATE_TIME));
        bonusAudit.setValidBet(BigDecimal.ZERO);
        BigDecimal outBalance = BigDecimal.ZERO;
        MbrWithdrawalCond withdrawalCond = getMbrWithdrawalCond(account.getId());
        if (Objects.nonNull(withdrawalCond)) {
            outBalance = Objects.nonNull(withdrawalCond.getOverFee())
                    ? withdrawalCond.getOverFee() : BigDecimal.ZERO;
        }
        TGmDepot depot = depotMapper.selectByPrimaryKey(depotId);
        bonusAudit.setDepotId(depotId);
        bonusAudit.setDepotName(depot.getDepotName());
        bonusAudit.setDepotCode(depot.getDepotCode());
        bonusAudit.setOutBalance(outBalance);
        bonusAudit.setDepositAmount(nonNull(amount) ? amount : BigDecimal.ZERO);
        bonusAudit.setDiscountAmount(bonus.getBonusAmount());
        BigDecimal auditAmount = bonusAudit.getDepositAmount()
                .add(bonusAudit.getDiscountAmount());
        bonusAudit.setAuditAmount(CommonUtil.adjustScale(
                new BigDecimal(bonus.getDiscountAudit()).multiply(auditAmount)));
        bonusAudit.setRemainValidBet(BigDecimal.ZERO);
        bonusAudit.setScope(bonus.getScope());
        bonusAudit.setDiscountBalance(BigDecimal.ZERO);
        bonusAudit.setStatus(Constants.EVNumber.zero);
        bonusAudit.setIsValid(Constants.EVNumber.one);
        bonusAudit.setBillManageId(billManageId);
        bonusAudit.setCatId(catId);
        bonusAudit.setPayOut(BigDecimal.ZERO);
        bonusAudit.setIsDrawings(Constants.EVNumber.zero);
        bonusAudit.setActivityId(bonus.getActivityId());
        bonusAudit.setIsDispose(Constants.EVNumber.zero);
        bonusAudit.setIsOut(Constants.EVNumber.zero);
        bonusAudit.setIsClean(Constants.EVNumber.zero);
        auditBonusMapper.insert(bonusAudit);
        MbrBillDetail mbrBillDetail = addMbrBillDetail(bonus);
        bonus.setStatus(Constants.EVNumber.one);
        bonus.setBillDetailId(mbrBillDetail.getId());
        bonus.setTransferAmount(amount);
        actBonusMapper.updateByPrimaryKey(bonus);
        setDiscardAmount(account, amount);
    }

    private MbrBillDetail addMbrBillDetail(OprActBonus bonus) {
        MbrBillDetail billDetail = new MbrBillDetail();
        billDetail.setLoginName(bonus.getLoginName());
        billDetail.setAccountId(bonus.getAccountId());
        billDetail.setFinancialCode(bonus.getFinancialCode());
        billDetail.setOrderNo(bonus.getOrderNo().toString());
        billDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
        billDetail.setDepotId(Constants.SYS_DEPOT_ID);
        billDetail.setAmount(bonus.getBonusAmount());
        MbrWallet entity = new MbrWallet();
        entity.setAccountId(bonus.getAccountId());
        entity = walletService.queryObjectCond(entity);
        billDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
        billDetail.setOpType(MbrBillDetail.OpTypeStatus.income);
        billDetail.setAfterBalance(entity.getBalance());
        billDetail.setBeforeBalance(
                BigDecimalMath.round(BigDecimalMath.sub(entity.getBalance(), billDetail.getAmount()), 2));
        billDetailMapper.insert(billDetail);
        return billDetail;
    }

    private void setDiscardAmount(MbrAccount account, BigDecimal amount) {
        MbrAuditAccount auditAccount = new MbrAuditAccount();
        auditAccount.setIsDrawings(Constants.EVNumber.zero);
        auditAccount.setAccountId(account.getId());
        List<MbrAuditAccount> auditAccounts = auditMapper.finAuditList(auditAccount);
        if (Collections3.isNotEmpty(auditAccounts)) {
            for (MbrAuditAccount mbrAuditAccount : auditAccounts) {
                Integer discardSign = mbrAuditAccount.getDepositAmount().compareTo(amount);
                if (mbrAuditAccount.getDepositAmount().compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }
                if (discardSign == 0) {
                    if (mbrAuditAccount.getDiscardAmount().compareTo(BigDecimal.ZERO) == 0) {
                        mbrAuditAccount.setDiscardAmount(mbrAuditAccount.getDepositAmount());
                    }
                    mbrAuditAccount.setDepositAmount(BigDecimal.ZERO);
                    mbrAuditAccount.setAuditAmount(BigDecimal.ZERO);
                    updateDiscardAmount(mbrAuditAccount);
                    break;
                }
                if (discardSign == 1) {
                    if (mbrAuditAccount.getDiscardAmount().compareTo(BigDecimal.ZERO) == 0) {
                        mbrAuditAccount.setDiscardAmount(mbrAuditAccount.getDepositAmount());
                    }
                    mbrAuditAccount.setDepositAmount(mbrAuditAccount.getDepositAmount().subtract(amount));
                    mbrAuditAccount.setAuditAmount(mbrAuditAccount.getDepositAudit().multiply(mbrAuditAccount.getDepositAmount()));
                    updateDiscardAmount(mbrAuditAccount);
                    break;
                }
                if (discardSign == -1) {
                    amount = amount.subtract(mbrAuditAccount.getDepositAmount());
                    if (mbrAuditAccount.getDiscardAmount().compareTo(BigDecimal.ZERO) == 0) {
                        mbrAuditAccount.setDiscardAmount(mbrAuditAccount.getDepositAmount());
                    }
                    mbrAuditAccount.setDepositAmount(BigDecimal.ZERO);
                    mbrAuditAccount.setAuditAmount(BigDecimal.ZERO);
                    updateDiscardAmount(mbrAuditAccount);
                }
            }
        }
    }

    private void updateDiscardAmount(MbrAuditAccount auditAccount) {
        auditAccountMapper.updateByPrimaryKey(auditAccount);
    }


    /**
     * 转账 转出判断优惠是否通过违规优惠稽核
     */
    public AuditBonusDto outAuditBonus(MbrAccount account, Integer depotId) {
        List<MbrAuditBonus> auditBonuses = auditCastService.findMbrAuditBonusByAccountId(account.getId(), depotId, null);
        AuditBonusDto dto = new AuditBonusDto();
        if (Collections3.isNotEmpty(auditBonuses)) {
            MbrAuditBonus auditBonus = auditBonuses.get(0);
            long count = auditBonuses.stream().filter(p ->
                    Constants.EVNumber.zero == p.getStatus())
                    .map(MbrAuditBonus::getId).count();
            if (auditBonus.getIsValid() == Constants.EVNumber.zero) {
                dto.setIsFraud(Boolean.FALSE);
                return dto;
            }
            if (count > 0) {
                dto.setIsSucceed(Boolean.FALSE);
                Optional<BigDecimal> totalValidBet = auditBonuses.stream()
                        .filter(p -> nonNull(p.getAuditAmount()))
                        .map(MbrAuditBonus::getAuditAmount).reduce(BigDecimal::add);
                Optional<BigDecimal> validBet = auditBonuses.stream()
                        .filter(p -> nonNull(p.getValidBet()))
                        .map(MbrAuditBonus::getValidBet).reduce(BigDecimal::add);
                dto.setTotalValidBet(totalValidBet.get());
                dto.setResidueValidBet(dto.getTotalValidBet().subtract(validBet.get()));
                return dto;
            }
        }
        return dto;
    }

    /**
     * 转账成功
     */
    public void succeedAuditBonus(MbrAccount account, Integer depotId) {
        MbrAuditBonus auditBonus = new MbrAuditBonus();
        auditBonus.setAccountId(account.getId());
        auditBonus.setDepotId(depotId);
        auditBonus.setIsDrawings(Constants.EVNumber.zero);
        List<MbrAuditBonus> auditBonuses = auditBonusMapper.select(auditBonus);
        if (Collections3.isNotEmpty(auditBonuses)) {
            auditBonuses.forEach(as -> {
                as.setStatus(Constants.EVNumber.one);
                as.setIsDrawings(Constants.EVNumber.one);
                as.setTransferTime(getCurrentDate(FORMAT_18_DATE_TIME));
                auditBonusMapper.updateByPrimaryKey(as);
            });
            addOrUpdateMbrAuditHistory(auditBonuses.get(0), auditBonuses.get(auditBonuses.size() - 1).getTime());
        }
    }

    public void addOrUpdateMbrAuditHistory(MbrAuditBonus bonus, String time) {
        MbrAuditHistory auditHistory = findMbrAuditHistory(bonus.getAccountId());
        if (isNull(auditHistory)) {
            MbrAuditHistory history = new MbrAuditHistory();
            history.setAccountId(bonus.getAccountId());
            history.setLoginName(bonus.getLoginName());
            history.setStartTime(bonus.getTime());
            history.setIsSign(Constants.EVNumber.zero);
            history.setEndTime(time);
            auditHistoryMapper.insert(history);
        } else {
            if (bonus.getTime().compareTo(auditHistory.getStartTime()) < 1) {
                auditHistory.setStartTime(bonus.getTime());
            }
            if (time.compareTo(auditHistory.getEndTime()) > 0) {
                auditHistory.setEndTime(time);
            }
            auditHistoryMapper.updateByPrimaryKey(auditHistory);
        }
    }

    public void addOrUpdateHistoryByDeposit(MbrAuditAccount auditAccount, String time) {
        MbrAuditHistory auditHistory = findMbrAuditHistory(auditAccount.getAccountId());
        if (isNull(auditHistory)) {
            MbrAuditHistory auditHistory1 = new MbrAuditHistory();
            auditHistory1.setAccountId(auditAccount.getAccountId());
            auditHistory1.setLoginName(auditAccount.getLoginName());
            auditHistory1.setStartTime(auditAccount.getTime());
            auditHistory1.setIsSign(Constants.EVNumber.one);
            auditHistory1.setEndTime(time);
            auditHistoryMapper.insert(auditHistory1);
        } else {
            if (StringUtils.isNotEmpty(auditAccount.getTime())
                    && auditAccount.getTime().compareTo(auditHistory.getStartTime()) < 1) {
                auditHistory.setStartTime(auditAccount.getTime());
            }
            if (time.compareTo(auditHistory.getEndTime()) > 0) {
                auditHistory.setStartTime(time);
            }
            auditHistory.setIsSign(Constants.EVNumber.one);
            auditHistoryMapper.updateByPrimaryKey(auditHistory);
        }
    }

    private MbrAuditHistory findMbrAuditHistory(Integer accountId) {
        MbrAuditHistory history = new MbrAuditHistory();
        history.setAccountId(accountId);
        history.setIsSign(Constants.EVNumber.zero);
        return auditHistoryMapper.selectOne(history);
    }

    public void updateNormal(MbrAuditBonus mbrAuditBonus, String userName) {
        MbrAuditBonus auditBonus = auditBonusMapper.selectByPrimaryKey(mbrAuditBonus.getId());
        auditBonus.setMemo(mbrAuditBonus.getMemo());
        updateAuditBonusById(auditBonus, userName);
    }

    public void addAuditBonus(MbrAuditBonus mbrAuditBonus, String userName) {
        MbrAuditBonus auditBonus = auditBonusMapper.selectByPrimaryKey(mbrAuditBonus.getId());
        auditBonus.setMemo(mbrAuditBonus.getMemo());
        auditBonus.setAuditAmount(mbrAuditBonus.getAuditAmount().add(auditBonus.getAuditAmount()));
        updateAuditBonusById(auditBonus, userName);
    }

    private void updateAuditBonusById(MbrAuditBonus auditBonus, String userName) {
        if (nonNull(auditBonus) && auditBonus.getIsValid() == Constants.EVNumber.zero
                && auditBonus.getIsDrawings() == Constants.EVNumber.zero) {
            auditBonus.setIsValid(Constants.EVNumber.one);
            auditBonus.setUpdateAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
            auditBonus.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
            auditBonus.setModifyUser(userName);
            auditBonus.setIsDispose(Constants.EVNumber.one);
            auditBonusMapper.updateByPrimaryKey(auditBonus);
        }
    }

    public void clearAuditBonus(MbrAuditBonus mbrAuditBonus, String userName) {
        MbrAuditBonus auditBonus = auditBonusMapper.selectByPrimaryKey(mbrAuditBonus.getId());
        if (nonNull(auditBonus) && auditBonus.getIsValid() == Constants.EVNumber.zero) {
            MbrAuditBonus auditBonus1 = new MbrAuditBonus();
            auditBonus1.setAccountId(auditBonus.getAccountId());
            auditBonus1.setCatId(auditBonus1.getCatId());
            auditBonus1.setDepotId(auditBonus1.getDepotId());
            auditBonus1.setIsValid(auditBonus.getIsValid());
            auditBonus1.setIsDrawings(Constants.EVNumber.zero);
            List<MbrAuditBonus> auditBonuses = auditBonusMapper.select(auditBonus1);
            if (Collections3.isNotEmpty(auditBonuses)) {
                auditBonuses.stream().forEach(as -> {
                    as.setMemo(mbrAuditBonus.getMemo());
                    as.setIsClean(Constants.EVNumber.one);
                    as.setStatus(Constants.EVNumber.one);
                    as.setAuditAmount(BigDecimal.ZERO);
                    as.setIsDrawings(Constants.EVNumber.one);
                    as.setTransferTime(getCurrentDate(FORMAT_18_DATE_TIME));
                    updateAuditBonusById(as, userName);
                });
                addOrUpdateMbrAuditHistory(auditBonuses.get(0), auditBonuses.get(auditBonuses.size() - 1).getTime());
            }
        }
    }

    public BigDecimal findAuditAccountBalance(MbrAuditBonus bonus, String siteCode) {
        MbrAuditBonus auditBonus = auditBonusMapper.selectByPrimaryKey(bonus.getId());
        if (nonNull(auditBonus)) {
            return getBalance(auditBonus, siteCode);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getBalance(MbrAuditBonus bonus, String siteCode) {
        TGmApi gmApi = gmApiService.queryApiObject(bonus.getDepotId(), siteCode);
        UserBalanceResponseDto responseDto = depotWalletService.queryDepotBalance(bonus.getAccountId(), gmApi);
        return responseDto.getBalance();
    }

    public void auditCharge(Integer auditBonusId, BigDecimal amount, String memo, String ip, String siteCode, String userName) {
        MbrAuditBonus auditBonus = auditBonusMapper.selectByPrimaryKey(auditBonusId);
        if (nonNull(auditBonus) && auditBonus.getIsDrawings() == Constants.EVNumber.zero) {
            MbrWallet wallet = new MbrWallet();
            wallet.setAccountId(auditBonus.getAccountId());
            MbrWallet mbrWallet = mbrWalletMapper.selectOne(wallet);
            BigDecimal balance = getBalance(auditBonus, siteCode);
            if (mbrWallet.getBalance().add(balance).compareTo(amount) == -1) {
                throw new R200Exception("余额不足");
            }
            MbrDepotWallet depotWallet = new MbrDepotWallet();
            depotWallet.setAccountId(auditBonus.getAccountId());
            Integer[] ids = {auditBonus.getDepotId()};
            depotWallet.setDepotIds(ids);
            Boolean isSusssne = walletService.recoverBalanceNew(depotWallet, ip, (byte) 0, Boolean.FALSE);
            if (Boolean.FALSE.equals(isSusssne)) {
                throw new R200Exception("扣款，转账失败");
            }
            FundAudit fundAudit = new FundAudit();
            fundAudit.setAccountId(auditBonus.getAccountId());
            fundAudit.setAmount(amount);
            fundAudit.setMemo(memo);
            fundAudit.setFinancialCode(OrderConstants.FUND_ORDER_CODE_AM);
            fundAudit.setLoginName(auditBonus.getLoginName());
            fundAudit.setStatus(Constants.EVNumber.one);
            fundAudit.setOrderNo(new SnowFlake().nextId()+"");
            fundAudit.setOrderPrefix(OrderConstants.FUND_ORDER_AUDIT);
            fundAudit.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
            fundAudit.setCreateUser(userName);
            fundAudit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
            fundAudit.setModifyUser(userName);
            fundAudit.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
            fundAudit.setAuditUser(userName);
            MbrBillDetail mbrBillDetail = walletService.castWalletAndBillDetail(fundAudit.getLoginName(),
                    fundAudit.getAccountId(), fundAudit.getFinancialCode(), fundAudit.getAmount(),
                    fundAudit.getOrderNo(), Boolean.FALSE);
            fundAudit.setBillDetailId(mbrBillDetail.getId());
            fundAuditMapper.insert(fundAudit);
            auditChargeClean(auditBonus, userName);
        }
    }

    private void auditChargeClean(MbrAuditBonus bonus, String userName) {
        MbrAuditBonus auditBonus = new MbrAuditBonus();
        auditBonus.setAccountId(bonus.getAccountId());
        auditBonus.setDepotId(bonus.getDepotId());
        List<MbrAuditBonus> auditBonuses = auditBonusMapper.select(auditBonus);
        if (Collections3.isNotEmpty(auditBonuses)) {
            auditBonuses.stream().forEach(as -> {
                as.setMemo(bonus.getMemo());
                as.setAuditAmount(BigDecimal.ZERO);
                as.setModifyUser(userName);
                as.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
                as.setStatus(Constants.EVNumber.one);
                as.setIsDrawings(Constants.EVNumber.one);
                as.setIsValid(Constants.EVNumber.one);
                as.setTransferTime(getCurrentDate(FORMAT_18_DATE_TIME));
                auditBonusMapper.updateByPrimaryKey(as);
            });
            addOrUpdateMbrAuditHistory(auditBonuses.get(0), auditBonuses.get(auditBonuses.size() - 1).getTime());
        }
    }
}
