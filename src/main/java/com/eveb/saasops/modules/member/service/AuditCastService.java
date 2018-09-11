package com.eveb.saasops.modules.member.service;

import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.utils.Collections3;
import com.eveb.saasops.common.utils.SnowFlake;
import com.eveb.saasops.modules.analysis.entity.RptBetModel;
import com.eveb.saasops.modules.analysis.entity.RptBetTotalModel;
import com.eveb.saasops.modules.analysis.service.AnalysisService;
import com.eveb.saasops.modules.member.dao.MbrAccountMapper;
import com.eveb.saasops.modules.member.dao.MbrAuditAccountMapper;
import com.eveb.saasops.modules.member.dao.MbrAuditBonusMapper;
import com.eveb.saasops.modules.member.dao.MbrAuditFraudMapper;
import com.eveb.saasops.modules.member.dto.BounsValidBetDto;
import com.eveb.saasops.modules.member.entity.*;
import com.eveb.saasops.modules.member.mapper.AuditMapper;
import com.eveb.saasops.modules.operate.dao.TGmCatMapper;
import com.eveb.saasops.modules.operate.dao.TGmDepotMapper;
import com.eveb.saasops.modules.operate.entity.TGmCat;
import com.eveb.saasops.modules.operate.entity.TGmDepot;
import com.eveb.saasops.modules.operate.service.TGmDepotService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.eveb.saasops.common.utils.DateUtil.*;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@Transactional
public class AuditCastService {

    @Autowired
    private AuditMapper auditMapper;
    @Autowired
    private MbrAuditAccountMapper auditAccountMapper;
    @Autowired
    private AnalysisService analysisService;
    @Autowired
    private TGmCatMapper gmCatMapper;
    @Autowired
    private MbrAuditFraudMapper auditFraudMapper;
    @Autowired
    private MbrAuditBonusMapper auditBonusMapper;
    @Autowired
    private TGmDepotService depotService;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private TGmDepotMapper depotMapper;
    @Autowired
    private AuditAccountService auditAccountService;


    public List<Integer> findAuditAccountIds(Integer accountId) {
        return auditMapper.findAuditAccountId(accountId);
    }

    public void doingCronAuditAccount(String siteCode, Integer id) {
        MbrAccount account = accountMapper.selectByPrimaryKey(id);
        BounsValidBetDto betDto = updateAuditBonus(account, null, siteCode);
        transferBonus(betDto.getAuditBonuses());
        updateAuditAccount(account, siteCode, betDto);
    }

    private void transferBonus(List<MbrAuditBonus> auditBonuses) {
        if (Collections3.isNotEmpty(auditBonuses)) {
            Map<Integer, List<MbrAuditBonus>> auditBonusGroupingBy =
                    auditBonuses.stream().collect(
                            Collectors.groupingBy(
                                    MbrAuditBonus::getDepotId));
            for (Integer depotIdKey : auditBonusGroupingBy.keySet()) {
                List<MbrAuditBonus> auditBonusList = auditBonusGroupingBy.get(depotIdKey);
                long counValid = auditBonusList.stream().filter(p ->
                        Constants.EVNumber.zero == p.getIsValid())
                        .map(MbrAuditBonus::getId).count();
                long couStatusnt = auditBonusList.stream().filter(p ->
                        Constants.EVNumber.zero == p.getStatus())
                        .map(MbrAuditBonus::getId).count();
                if (counValid == Constants.EVNumber.zero
                        && couStatusnt == Constants.EVNumber.zero) {
                    auditBonusList.stream().forEach(b -> {
                        b.setIsDrawings(Constants.EVNumber.one);
                        b.setTransferTime(getCurrentDate(FORMAT_18_DATE_TIME));
                        auditBonusMapper.updateByPrimaryKey(b);
                    });
                    auditAccountService.addOrUpdateMbrAuditHistory(
                            auditBonusList.get(auditBonusList.size() - 1),
                            auditBonusList.get(0).getTime());
                }
            }
        }
    }

    private List<MbrAuditAccount> updateAuditAccount(MbrAccount account, String siteCode, BounsValidBetDto validBetDto) {
        List<MbrAuditAccount> auditAccounts = updateValidBet(account.getId(), siteCode, validBetDto);
        if (Collections3.isNotEmpty(auditAccounts)) {
            updateAccountAudit(auditAccounts);
        }
        return auditAccounts;
    }

    private List<MbrAuditAccount> updateValidBet(Integer accountId, String siteCode, BounsValidBetDto validBetDto) {
        MbrAuditAccount accountAudit = new MbrAuditAccount();
        accountAudit.setIsDrawings(Constants.EVNumber.zero);
        accountAudit.setSort(Boolean.TRUE);
        accountAudit.setAccountId(accountId);
        List<MbrAuditAccount> audits = auditMapper.finAuditList(accountAudit);
        for (int i = 0; i < audits.size(); i++) {
            MbrAuditAccount audit = audits.get(i);
            RptBetModel rptBetModel = getValidBet(audit, audits, i, siteCode);
            audit.setPayOut(BigDecimal.ZERO);
            audit.setDepositBalance(BigDecimal.ZERO);
            audit.setStatus(Constants.EVNumber.zero);
            audit.setIsOut(Constants.EVNumber.zero);
            audit.setValidBet(BigDecimal.ZERO);
            if (nonNull(rptBetModel)) {
                audit.setValidBet(nonNull(rptBetModel.getValidBet()) ? rptBetModel.getValidBet() : BigDecimal.ZERO);
                audit.setPayOut(nonNull(rptBetModel.getPayout()) ? rptBetModel.getPayout() : BigDecimal.ZERO);
                audit.setDepositBalance(audit.getPayOut());
            }
        }
        if (Collections3.isNotEmpty(audits)) {
            Collections.reverse(audits);
        }
        for (int i = 0; i < audits.size(); i++) {
            MbrAuditAccount audit = audits.get(i);
            audit.setBonusRemainValidBet(getBonusRemainValidBet(validBetDto, audit));
            audit.setValidBet(audit.getValidBet().add(
                    nonNull(audit.getBonusRemainValidBet())
                            ? audit.getBonusRemainValidBet() : BigDecimal.ZERO));
            auditAccountMapper.updateByPrimaryKey(audit);
        }
        return audits;
    }


    private void updateAccountAudit(List<MbrAuditAccount> accountAudits) {
        MbrAuditAccount remainValidBet = new MbrAuditAccount();
        remainValidBet.setRemainValidBet(BigDecimal.ZERO);
        accountAudits.forEach(st -> castAuditAccout(st, remainValidBet));

        long count = accountAudits.stream().filter(p ->
                Constants.EVNumber.zero == p.getStatus())
                .map(MbrAuditAccount::getId).count();
        if (count > 0) castDepositBalance(accountAudits);
    }

    private List<String> getDepotCode(Integer accountId, String endTime, List<String> depotCodes) {
        List<TGmDepot> depots = depotService.finfTGmDepotList();
        List<String> depotStr = depots.stream().map(TGmDepot::getDepotCode).collect(Collectors.toList());
        if (Collections3.isNotEmpty(depotCodes)) {
            depotStr = Collections3.subtract(depotStr, depotCodes);
        }
        List<MbrAuditBonus> auditBonuses = findMbrAuditBonusByAccountId(accountId, null, endTime);
        if (Collections3.isNotEmpty(auditBonuses)) {
            List<String> bonusesStr = auditBonuses.stream()
                    .map(MbrAuditBonus::getDepotCode).collect(Collectors.toList());
            List<String> stringList = Collections3.subtract(depotStr, bonusesStr);
            return stringList;
        }
        return depotStr;
    }

    private RptBetModel getValidBet(MbrAuditAccount audit, List<MbrAuditAccount> audits, int i, String siteCode) {
        String startTime = audit.getTime();
        String endTime = i == audits.size() - 1 ? getCurrentDate(FORMAT_18_DATE_TIME) : audits.get(i + 1).getTime();
        MbrAuditBonus auditBonus = new MbrAuditBonus();
        auditBonus.setAccountId(audit.getAccountId());
        auditBonus.setStartTime(startTime);
        auditBonus.setEndTime(endTime);
        List<MbrAuditBonus> auditBonuses = auditMapper.fundAuditBonusByTime(auditBonus);
        Boolean isRptBetModelOne = Boolean.FALSE;
        if (auditBonuses.size() == 0) {
            isRptBetModelOne = Boolean.TRUE;
        }
        if (Boolean.FALSE.equals(isRptBetModelOne)) {
            long counDrawings = auditBonuses.stream().filter(p ->
                    Constants.EVNumber.one == p.getIsDrawings())
                    .map(MbrAuditBonus::getId).count();
            if (counDrawings == 0) {
                isRptBetModelOne = Boolean.TRUE;
            }
        }
        if (Boolean.TRUE.equals(isRptBetModelOne)) {
            List<String> depotCodes = getDepotCode(audit.getAccountId(), endTime, null);
            return getRptBetModel(siteCode, audit, startTime, endTime, depotCodes);
        }

        List<String> depotStr = auditBonuses.stream().filter(a ->
                a.getIsDrawings() == Constants.EVNumber.zero ||
                        (StringUtils.isNotEmpty(a.getTransferTime())
                                && a.getTransferTime().compareTo(endTime) > 0))
                .map(MbrAuditBonus::getDepotCode).collect(Collectors.toList());
        RptBetModel betModel = new RptBetModel();
        betModel.setValidBet(BigDecimal.ZERO);
        betModel.setPayout(BigDecimal.ZERO);

        List<MbrAuditBonus> auditBonusList = auditBonuses.stream().filter(
                a -> StringUtils.isNotEmpty(a.getTransferTime())
                        && a.getIsDrawings() == Constants.EVNumber.one
                        && a.getTransferTime().compareTo(endTime) < 0)
                .collect(Collectors.toList());
        auditBonusList.sort((r1, r2) -> r1.getTransferTime().compareTo(r2.getTransferTime()));

        String timeFrom = "", timeTo = "";
        int count = auditBonusList.size() + 1;
        for (int j = 0; j < count; j++) {
            String depotCode = StringUtils.EMPTY;
            if (j == 0 && auditBonusList.size() > 0) {
                MbrAuditBonus bonus = auditBonusList.get(j);
                timeFrom = startTime;
                timeTo = bonus.getTransferTime();
                depotCode = bonus.getDepotCode();
                depotStr.add(depotCode);
            }
            if (j > 0 && j < count - 1) {
                MbrAuditBonus bonus = auditBonusList.get(j);
                timeFrom = auditBonusList.get(j - 1).getTransferTime();
                timeTo = bonus.getTransferTime();
                depotCode = bonus.getDepotCode();
                depotStr.add(depotCode);
            }
            if (j == count - 1) {
                timeFrom = auditBonusList.size() == 0
                        ? startTime : auditBonusList.get(auditBonusList.size() - 1).getTransferTime();
                timeTo = endTime;
            }
            List<String> depotCodes = getDepotCode(audit.getAccountId(), endTime, depotStr);
            RptBetModel rptBetModel = getRptBetModel(siteCode, audit, timeFrom, timeTo, depotCodes);
            if (nonNull(rptBetModel)) {
                betModel.setValidBet(betModel.getValidBet().add(
                        nonNull(rptBetModel.getValidBet()) ? rptBetModel.getValidBet() : BigDecimal.ZERO));
                betModel.setPayout(betModel.getPayout().add(
                        nonNull(rptBetModel.getPayout()) ? rptBetModel.getPayout() : BigDecimal.ZERO));
            }
            if (StringUtils.isNotEmpty(depotCode)) {
                depotStr.remove(depotCode);
            }
        }
        return betModel;
    }

    private RptBetModel getRptBetModel(String siteCode, MbrAuditAccount audit, String startTime, String
            endTime, List<String> depotCodes) {
        List<RptBetModel> betModels = analysisService.getValidBet(siteCode,
                Lists.newArrayList(audit.getLoginName().toLowerCase()),
                formatEsDate(startTime), formatEsDate(endTime), null, null, depotCodes);
        if (betModels.size() > 0) {
            return betModels.get(0);
        }
        return null;
    }

    private BigDecimal getBonusRemainValidBet(BounsValidBetDto dto, MbrAuditAccount auditAccount) {
        List<MbrAuditBonus> auditBonuses = dto.getAuditBonuses();
        BigDecimal bigDecimal = auditAccount.getBonusRemainValidBet();
        if (Collections3.isNotEmpty(auditBonuses)) {
            Map<Integer, List<MbrAuditBonus>> auditBonusGroupingBy =
                    auditBonuses.stream().collect(
                            Collectors.groupingBy(
                                    MbrAuditBonus::getDepotId));
            for (Integer depotIdKey : auditBonusGroupingBy.keySet()) {
                List<MbrAuditBonus> auditBonusList = auditBonusGroupingBy.get(depotIdKey);
                Collections.sort(auditBonusList, Comparator.comparing(MbrAuditBonus::getTime));
                if (dto.getDepotIds().contains(auditBonusList.get(0).getDepotId())) {
                    continue;
                }
                if (auditAccount.getTime().compareTo(
                        auditBonusList.get(auditBonusList.size() - 1).getTime()) > 0) {
                    continue;
                }
                long counValid = auditBonusList.stream().filter(p ->
                        Constants.EVNumber.zero == p.getIsValid())
                        .map(MbrAuditBonus::getId).count();
                long couStatusnt = auditBonusList.stream().filter(p ->
                        Constants.EVNumber.zero == p.getStatus())
                        .map(MbrAuditBonus::getId).count();
                long couClean = auditBonusList.stream().filter(p ->
                        Constants.EVNumber.one == p.getIsClean())
                        .map(MbrAuditBonus::getIsClean).count();
                if (counValid == Constants.EVNumber.zero
                        && couStatusnt == Constants.EVNumber.zero
                        && couClean == Constants.EVNumber.zero) {
                    List<Integer> depotIds = dto.getDepotIds();
                    depotIds.add(auditBonusList.get(0).getDepotId());
                    dto.setDepotIds(depotIds);
                    bigDecimal = auditBonusList.get(0).getRemainValidBet().add(bigDecimal);
                }
            }
        }
        return bigDecimal;
    }

    private void castAuditAccout(MbrAuditAccount accountAudit, MbrAuditAccount remainValidBet) {
        BigDecimal addDecimal = accountAudit.getAuditAmount();
        switch (accountAudit.getValidBet().compareTo(addDecimal)) {
            case 0:
                accountAudit.setStatus(Constants.EVNumber.one);
                break;
            case 1:
                accountAudit.setStatus(Constants.EVNumber.one);
                BigDecimal bigDecimal1 = accountAudit.getValidBet().subtract(addDecimal);
                remainValidBet.setRemainValidBet(bigDecimal1.add(remainValidBet.getRemainValidBet()));
                break;
            case -1:
                castRemainValidBet(accountAudit, remainValidBet, addDecimal);
                break;
        }
        accountAudit.setRemainValidBet(remainValidBet.getRemainValidBet());
        auditAccountMapper.updateByPrimaryKeySelective(accountAudit);
    }

    private void castRemainValidBet(MbrAuditAccount accountAudit, MbrAuditAccount remainValidBet, BigDecimal
            bigDecimal) {
        BigDecimal decimal = accountAudit.getValidBet().add(remainValidBet.getRemainValidBet());
        if (decimal.compareTo(bigDecimal) == -1) {
            accountAudit.setStatus(Constants.EVNumber.zero);
            remainValidBet.setRemainValidBet(BigDecimal.ZERO);
            return;
        }
        accountAudit.setStatus(Constants.EVNumber.one);
        remainValidBet.setRemainValidBet(decimal.subtract(bigDecimal));
    }

    private void castDepositBalance(List<MbrAuditAccount> accountAudits) {
        BigDecimal totalBalance = BigDecimal.ZERO;
        Boolean isPayOut = Boolean.FALSE;
        for (int i = 0; i < accountAudits.size(); i++) {
            MbrAuditAccount audit = accountAudits.get(i);
            audit.setDepositAmount(nonNull(audit.getDepositAmount()) ? audit.getDepositAmount() : BigDecimal.ZERO);
            audit.setDepositOutBalance(nonNull(audit.getDepositOutBalance()) ?
                    audit.getDepositOutBalance() : BigDecimal.ZERO);
            BigDecimal depositAmount = audit.getDepositAmount();
            if (nonNull(audit.getDepositBalance()) && audit.getDepositBalance().compareTo(BigDecimal.ZERO) != 0) {
                isPayOut = Boolean.TRUE;
            }
            totalBalance = depositAmount.add(audit.getDepositBalance()).add(totalBalance);
            if (totalBalance.compareTo(audit.getDepositOutBalance()) != 1) {
                audit.setStatus(Constants.EVNumber.one);
                audit.setIsOut(Constants.EVNumber.one);
                audit.setDepositBalance(totalBalance);
                int compareBalance = totalBalance.compareTo(BigDecimal.ZERO);
                if (compareBalance != 1) {
                    audit.setDepositBalance(BigDecimal.ZERO);
                }
            }
            if (totalBalance.compareTo(audit.getDepositOutBalance()) == 1) {
                audit.setDepositBalance(totalBalance);
                audit.setIsOut(Constants.EVNumber.zero);
                totalBalance = BigDecimal.ZERO;
            }
            if (Boolean.FALSE.equals(isPayOut)) {
                totalBalance = BigDecimal.ZERO;
            }
            auditAccountMapper.updateByPrimaryKeySelective(audit);
        }
    }


    private BounsValidBetDto updateAuditBonus(MbrAccount account, Integer depotId, String siteCode) {
        BounsValidBetDto bounsValidBetDto = new BounsValidBetDto();
        List<MbrAuditBonus> auditBonuses = auditBonusValidBet(account, depotId, siteCode);
        Collections.reverse(auditBonuses);
        Map<Integer, Map<Integer, List<MbrAuditBonus>>>
                auditBonusGroupingBy =
                auditBonuses.stream()
                        .collect(Collectors.groupingBy(
                                MbrAuditBonus::getDepotId,
                                Collectors.groupingBy(
                                        MbrAuditBonus::getCatId)));

        for (Integer depotIdKey : auditBonusGroupingBy.keySet()) {
            Map<Integer, List<MbrAuditBonus>> auditBonusMapMap =
                    auditBonusGroupingBy.get(depotIdKey);
            MbrAuditBonus remainValidBet = new MbrAuditBonus();
            remainValidBet.setRemainValidBet(BigDecimal.ZERO);
            for (Integer catIdKey : auditBonusMapMap.keySet()) {
                auditBonusMapMap.get(catIdKey).forEach(as -> {
                    castAuditBonus(as, remainValidBet);
                });
            }
            bounsValidBetDto.setSumValidBet(remainValidBet.getRemainValidBet()
                    .add(nonNull(bounsValidBetDto.getSumValidBet()) ?
                            bounsValidBetDto.getSumValidBet() : BigDecimal.ZERO));
        }
        long count = auditBonuses.stream()
                .filter(auditBonus ->
                        Constants.EVNumber.zero == auditBonus.getStatus())
                .map(MbrAuditBonus::getId).count();
        if (count > 0) casAuditBonusBalance(auditBonuses);
        bounsValidBetDto.setAuditBonuses(auditBonuses);
        return bounsValidBetDto;
    }


    private List<MbrAuditBonus> auditBonusValidBet(MbrAccount account, Integer depotId, String siteCode) {
        List<MbrAuditBonus> auditBonuses = findMbrAuditBonusByAccountId(account.getId(), depotId, null);
        if (Collections3.isNotEmpty(auditBonuses)) {
            Map<Integer, List<MbrAuditBonus>> auditBonusGroupingBy =
                    auditBonuses.stream().collect(
                            Collectors.groupingBy(
                                    MbrAuditBonus::getDepotId));
            for (Integer depotIdKey : auditBonusGroupingBy.keySet()) {
                updateAuditBonusValidBet(auditBonusGroupingBy.get(depotIdKey), siteCode, account);
            }
        }
        return auditBonuses;
    }

    private void castAuditBonus(MbrAuditBonus auditBonus, MbrAuditBonus remainValidBet) {
        BigDecimal addDecimal = auditBonus.getAuditAmount();
        switch (auditBonus.getValidBet().compareTo(addDecimal)) {
            case 0:
                auditBonus.setStatus(Constants.EVNumber.one);
                break;
            case 1:
                auditBonus.setStatus(Constants.EVNumber.one);
                BigDecimal bigDecimal1 = auditBonus.getValidBet().subtract(addDecimal);
                remainValidBet.setRemainValidBet(bigDecimal1.add(remainValidBet.getRemainValidBet()));
                if (auditBonus.getIsValid() == Constants.EVNumber.zero) {
                    remainValidBet.setRemainValidBet(BigDecimal.ZERO);
                }
                break;
            case -1:
                if (auditBonus.getIsValid() == Constants.EVNumber.zero) {
                    remainValidBet.setRemainValidBet(BigDecimal.ZERO);
                }
                castAuditBonusRemainValidBet(auditBonus, remainValidBet, addDecimal);
                break;
        }
        auditBonus.setRemainValidBet(remainValidBet.getRemainValidBet());
        auditBonusMapper.updateByPrimaryKey(auditBonus);
    }

    private void casAuditBonusBalance(List<MbrAuditBonus> auditBonuses) {
        BigDecimal totalBalance = BigDecimal.ZERO;
        for (int i = 0; i < auditBonuses.size(); i++) {
            MbrAuditBonus auditBonus = auditBonuses.get(i);
            auditBonus.setDiscountBalance(nonNull(auditBonus.getPayOut())
                    ? auditBonus.getPayOut() : BigDecimal.ZERO);
            BigDecimal depositAmount = auditBonus.getDepositAmount();
            totalBalance = depositAmount.add(auditBonus.getDiscountBalance()).add(totalBalance);
            if (totalBalance.compareTo(auditBonus.getOutBalance()) != 1) {
                auditBonus.setStatus(Constants.EVNumber.one);
                auditBonus.setIsOut(Constants.EVNumber.one);
                auditBonus.setDiscountBalance(totalBalance);
                int compareBalance = totalBalance.compareTo(BigDecimal.ZERO);
                if (compareBalance != 1) {
                    auditBonus.setDiscountBalance(BigDecimal.ZERO);
                }
            }
            if (totalBalance.compareTo(auditBonus.getOutBalance()) == 1) {
                auditBonus.setDiscountBalance(totalBalance);
                auditBonus.setIsOut(Constants.EVNumber.zero);
                totalBalance = BigDecimal.ZERO;
            }
            auditBonusMapper.updateByPrimaryKey(auditBonus);
        }
    }

    private void castAuditBonusRemainValidBet(MbrAuditBonus auditBonus, MbrAuditBonus remainValidBet, BigDecimal
            bigDecimal) {
        BigDecimal decimal = auditBonus.getValidBet().add(remainValidBet.getRemainValidBet());
        if (decimal.compareTo(bigDecimal) == -1) {
            auditBonus.setStatus(Constants.EVNumber.zero);
            remainValidBet.setRemainValidBet(BigDecimal.ZERO);
            return;
        }
        auditBonus.setStatus(Constants.EVNumber.one);
        remainValidBet.setRemainValidBet(decimal.subtract(bigDecimal));
    }

    private void insertMbrAuditFrauds(List<RptBetTotalModel> rptBetTotalModels, String catName, Long orderNo) {
        rptBetTotalModels.stream().forEach(b -> {
            if (!catName.equals(b.getGameCategory())) {
                MbrAuditFraud auditFraud = new MbrAuditFraud();
                auditFraud.setCatName(b.getGameCategory());
                TGmDepot depot = new TGmDepot();
                depot.setDepotCode(b.getPlatform());
                TGmDepot gmDepot = depotMapper.selectOne(depot);
                auditFraud.setDepotName(gmDepot.getDepotName());
                auditFraud.setOrderNo(orderNo);
                auditFraud.setStartTime(b.getMinTime());
                auditFraud.setEntTime(b.getMaxTime());
                auditFraud.setFraudValidBet(b.getValidBetTotal());
                auditFraud.setPayOut(b.getPayoutTotal());
                auditFraudMapper.insert(auditFraud);
            }
        });
    }


    private void updateAuditBonusValidBet(List<MbrAuditBonus> auditBonuses, String siteCode, MbrAccount account) {
        for (int i = 0; i < auditBonuses.size(); i++) {
            MbrAuditBonus bonus = auditBonuses.get(i);
            TGmCat gmCat = gmCatMapper.selectByPrimaryKey(bonus.getCatId());
            Long orderNo = new SnowFlake().nextId();
            String endTime = i == auditBonuses.size() - 1 ?
                    getCurrentDate(FORMAT_18_DATE_TIME) : auditBonuses.get(i + 1).getTime();
            String startTime = bonus.getTime();

            if (StringUtils.isNotEmpty(bonus.getUpdateAuditTime())
                    && bonus.getUpdateAuditTime().compareTo(endTime) < 0) {
                startTime = bonus.getUpdateAuditTime();
            }
            if (StringUtils.isEmpty(bonus.getDepotCode())) {
                TGmDepot depot = depotMapper.selectByPrimaryKey(bonus.getDepotId());
                bonus.setDepotCode(depot.getDepotCode());
            }
            List<RptBetTotalModel> rptBetTotalModels = analysisService.getGameCategoryReport(
                    siteCode, account.getLoginName(), Lists.newArrayList(bonus.getDepotCode()),
                    formatEsDate(startTime), formatEsDate(endTime));
            if (Collections3.isNotEmpty(rptBetTotalModels)) {
                RptBetTotalModel betTotalModel = getRptBetTotalModel(rptBetTotalModels, gmCat.getCatName());
                bonus.setValidBet(betTotalModel.getValidBetTotal());
                bonus.setPayOut(betTotalModel.getPayoutTotal());
                deleteAuditFraud(bonus.getOrderNo());
                bonus.setOrderNo(orderNo);
                bonus.setIsValid(Constants.EVNumber.one);
                bonus.setStatus(Constants.EVNumber.zero);
                bonus.setIsOut(Constants.EVNumber.zero);
                Boolean isFraud = Boolean.TRUE;
                if (rptBetTotalModels.size() > Constants.EVNumber.one) {
                    isFraud = Boolean.FALSE;
                }
                if (rptBetTotalModels.size() == Constants.EVNumber.one
                        && !rptBetTotalModels.get(0).getGameCategory().equals(gmCat.getCatName())) {
                    isFraud = Boolean.FALSE;
                }
                if (Boolean.FALSE.equals(isFraud)) {
                    bonus.setIsValid(Constants.EVNumber.zero);
                    bonus.setStatus(Constants.EVNumber.zero);
                    bonus.setIsDispose(Constants.EVNumber.zero);
                    insertMbrAuditFrauds(rptBetTotalModels, gmCat.getCatName(), orderNo);
                }
                auditBonusMapper.updateByPrimaryKey(bonus);
            }
        }
    }

    private void deleteAuditFraud(Long orderNo) {
        if (nonNull(orderNo)) {
            MbrAuditFraud auditFraud = new MbrAuditFraud();
            auditFraud.setOrderNo(orderNo);
            auditFraudMapper.delete(auditFraud);
        }
    }

    private RptBetTotalModel getRptBetTotalModel(List<RptBetTotalModel> rptBetTotalModels, String catName) {
        RptBetTotalModel totalModel = new RptBetTotalModel();
        if (Collections3.isNotEmpty(rptBetTotalModels)) {
            rptBetTotalModels.stream().forEach(rptBetTotalModel -> {
                if (catName.equals(rptBetTotalModel.getGameCategory())) {
                    totalModel.setValidBetTotal(rptBetTotalModel.getValidBetTotal().add(totalModel.getValidBetTotal()));
                }
                totalModel.setPayoutTotal(rptBetTotalModel.getPayoutTotal().add(totalModel.getPayoutTotal()));
            });
        }
        return totalModel;
    }

    public List<MbrAuditBonus> findMbrAuditBonusByAccountId(Integer accountId, Integer depotId, String auditTime) {
        MbrAuditBonus auditBonus = new MbrAuditBonus();
        auditBonus.setAccountId(accountId);
        auditBonus.setDepotId(depotId);
        auditBonus.setSort(Boolean.TRUE);
        auditBonus.setIsDrawings(Constants.EVNumber.zero);
        auditBonus.setAuditTime(auditTime);
        return auditMapper.finAuditBonusList(auditBonus);
    }

}
