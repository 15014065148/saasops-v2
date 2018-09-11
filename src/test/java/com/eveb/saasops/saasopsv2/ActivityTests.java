package com.eveb.saasops.saasopsv2;

import com.eveb.saasops.modules.operate.dto.*;
import com.eveb.saasops.modules.operate.entity.OprActActivity;
import com.eveb.saasops.modules.operate.service.OprActActivityService;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ActivityTests {

    @Autowired
    private OprActActivityService oprActBaseService;
    @Autowired
    private OprActActivityService actActivityService;

    @Test
    public void updateActivityState(){
        actActivityService.updateActivityState();
    }

    @Test
    public void findAccountBonusList() {
        actActivityService.findAccountBonusList(null,null,525,1,3,null);
    }

    @Test
    public void AQ0000001() {
        //DynamicDataSource.setDataSource("187");
        OprActActivity activity = new OprActActivity();
        activity.setActTmplId(1);
        activity.setActivityName("首存送AA");
        activity.setActCatId(5);
        activity.setShowStart("2017-12-24");
        activity.setShowEnd("2017-12-27");
        activity.setUseStart("2017-12-28");
        activity.setUseEnd("2017-12-28");
        activity.setUseState(0);
        activity.setIsShow(true);
        activity.setEnablePc(true);
        activity.setEnableMb(true);
        activity.setContent("测试首存送AA活动啊啊啊啊啊啊啊啊啊啊啊啊");

        JPreferentialDto dto = new JPreferentialDto();
        ActivityScopeDto scopeDto = new ActivityScopeDto();
        scopeDto.setIsAccAll(false);
        scopeDto.setAccIds(Lists.newArrayList(1, 5, 15, 16));
        scopeDto.setIsAgyTopAll(false);
        scopeDto.setAgyTopIds(Lists.newArrayList(20, 21, 22));
        scopeDto.setIsAgyAll(true);
        dto.setScopeDto(scopeDto);
        dto.setScope(2);
        dto.setIsAudit(true);
        dto.setIsBank(true);
        dto.setIsMail(true);
        dto.setIsMobile(true);
        dto.setIsName(true);
        dto.setDepositType(1);

        List<AuditCat> auditCats = Lists.newArrayList();
        AuditCat auditCat = new AuditCat();
        auditCat.setCatId(1);
        auditCat.setIsAll(true);
        auditCats.add(auditCat);
        AuditCat auditCat1 = new AuditCat();
        auditCat1.setCatId(5);
        auditCat1.setIsAll(true);
        auditCats.add(auditCat1);
        AuditCat auditCat2 = new AuditCat();
        auditCat.setCatId(1);
        auditCat2.setIsAll(true);
        auditCats.add(auditCat2);
        AuditCat auditCat3 = new AuditCat();
        auditCat.setCatId(1);
        auditCat3.setIsAll(true);
        auditCats.add(auditCat3);
        dto.setAuditCats(auditCats);


        List<ActivityRuleDto> ruleDtos = Lists.newArrayList();
        ActivityRuleDto activityRuleDto = new ActivityRuleDto();
        activityRuleDto.setAmountMin(new BigDecimal(100));
        activityRuleDto.setAmountMax(new BigDecimal(200));
        activityRuleDto.setDonateType(0);
        activityRuleDto.setDonateAmount(new BigDecimal(10));
        activityRuleDto.setDonateAmountMax(new BigDecimal(300));
        activityRuleDto.setMultipleWater(10.0);
        ruleDtos.add(activityRuleDto);
        ActivityRuleDto ruleDto = new ActivityRuleDto();
        ruleDto.setAmountMin(new BigDecimal(100));
        ruleDto.setAmountMax(new BigDecimal(200));
        ruleDto.setDonateType(1);
        ruleDto.setDonateAmount(new BigDecimal(20));
        ruleDto.setDonateAmountMax(new BigDecimal(300));
        ruleDto.setMultipleWater(10.0);
        ruleDtos.add(ruleDto);
        dto.setActivityRuleDtos(ruleDtos);

        ActivityDto activityDto = new ActivityDto();
        activityDto.setActActivity(activity);
        activityDto.setObject(dto);
        System.out.println(new Gson().toJson(dto));
        oprActBaseService.save(activityDto, "admin", null, null);
    }


    @Test
    public void AQ0000002() {
        //DynamicDataSource.setDataSource("187");
        OprActActivity activity = new OprActActivity();
        activity.setActTmplId(2);
        activity.setActivityName("注册送活动");
        activity.setActCatId(5);
        activity.setShowStart("2017-12-24");
        activity.setShowEnd("2017-12-27");
        activity.setUseStart("2017-12-28");
        activity.setUseEnd("2017-12-28");
        activity.setUseState(0);
        activity.setIsShow(true);
        activity.setEnablePc(true);
        activity.setEnableMb(true);
        activity.setContent("测试首注册送活动啊啊啊啊啊啊啊啊啊啊啊啊");

        JRegisterDto dto = new JRegisterDto();
        ActivityScopeDto scopeDto = new ActivityScopeDto();
        scopeDto.setIsAccAll(false);
        scopeDto.setAccIds(Lists.newArrayList(1, 5, 15, 16));
        scopeDto.setIsAgyTopAll(false);
        scopeDto.setAgyTopIds(Lists.newArrayList(20, 21, 22));
        scopeDto.setIsAgyAll(true);
        dto.setScopeDto(scopeDto);
        dto.setIsAudit(true);
        dto.setIsBank(true);
        dto.setIsMail(true);
        dto.setIsMobile(true);
        dto.setIsName(true);


        List<AuditCat> auditCats = Lists.newArrayList();
        AuditCat auditCat = new AuditCat();
        auditCat.setCatId(1);
        auditCat.setIsAll(true);
        auditCats.add(auditCat);
        AuditCat auditCat1 = new AuditCat();
        auditCat.setCatId(1);
        auditCat1.setIsAll(true);
        auditCats.add(auditCat1);
        AuditCat auditCat2 = new AuditCat();
        auditCat.setCatId(1);
        auditCat2.setIsAll(true);
        auditCats.add(auditCat2);
        AuditCat auditCat3 = new AuditCat();
        auditCat.setCatId(1);
        auditCat3.setIsAll(true);
        auditCats.add(auditCat3);
        dto.setAuditCats(auditCats);

        dto.setRegisterStartTime("2017-03-10 00:00:00");
        dto.setRegisterEndTime("2017-03-10 00:00:00");

        RegisterRuleDto registerRuleDto1 = new RegisterRuleDto();
        registerRuleDto1.setDonateAmount(new BigDecimal(800));
        registerRuleDto1.setMultipleWater(15.0);
        dto.setRuleDto(registerRuleDto1);

        ActivityDto activityDto = new ActivityDto();
        activityDto.setActActivity(activity);
        activityDto.setObject(dto);
        System.out.println(new Gson().toJson(dto));
       /* oprActBaseService.save(activityDto, "admin", null, null);*/
    }


    @Test
    public void AQ0000003() {
        //DynamicDataSource.setDataSource("187");
        OprActActivity activity = new OprActActivity();
        activity.setActTmplId(3);
        activity.setActivityName("存就送活动");
        activity.setActCatId(5);
        activity.setShowStart("2017-12-24");
        activity.setShowEnd("2017-12-27");
        activity.setUseStart("2017-12-28");
        activity.setUseEnd("2017-12-28");
        activity.setUseState(0);
        activity.setIsShow(true);
        activity.setEnablePc(true);
        activity.setEnableMb(true);
        activity.setContent("测试存就送活动啊啊啊啊啊啊啊啊啊啊啊啊");

        JDepositSentDto dto = new JDepositSentDto();
        ActivityScopeDto scopeDto = new ActivityScopeDto();
        scopeDto.setIsAccAll(false);
        scopeDto.setAccIds(Lists.newArrayList(1, 5, 15, 16));
        scopeDto.setIsAgyTopAll(false);
        scopeDto.setAgyTopIds(Lists.newArrayList(20, 21, 22));
        scopeDto.setIsAgyAll(true);
        dto.setScopeDto(scopeDto);
        dto.setIsAudit(true);
        dto.setIsBank(true);
        dto.setIsMail(true);
        dto.setIsMobile(true);
        dto.setIsName(true);


        List<AuditCat> auditCats = Lists.newArrayList();
        AuditCat auditCat = new AuditCat();
        auditCat.setCatId(1);
        auditCat.setIsAll(true);
        auditCats.add(auditCat);
        AuditCat auditCat1 = new AuditCat();
        auditCat.setCatId(1);
        auditCat1.setIsAll(true);
        auditCats.add(auditCat1);
        AuditCat auditCat2 = new AuditCat();
        auditCat.setCatId(1);
        auditCat2.setIsAll(true);
        auditCats.add(auditCat2);
        AuditCat auditCat3 = new AuditCat();
        auditCat.setCatId(1);
        auditCat3.setIsAll(true);
        auditCats.add(auditCat3);
        dto.setAuditCats(auditCats);

        dto.setDrawType(0);
        dto.setDrawNumber(1);
        dto.setFormulaMode(1);

        List<ActivityRuleDto> ruleDtos = Lists.newArrayList();
        ActivityRuleDto activityRuleDto = new ActivityRuleDto();
        activityRuleDto.setAmountMin(new BigDecimal(100));
        activityRuleDto.setAmountMax(new BigDecimal(200));
        activityRuleDto.setDonateType(0);
        activityRuleDto.setDonateAmountMax(new BigDecimal(300));
        activityRuleDto.setMultipleWater(10.0);
        ruleDtos.add(activityRuleDto);
        ActivityRuleDto ruleDto = new ActivityRuleDto();
        ruleDto.setAmountMin(new BigDecimal(100));
        ruleDto.setAmountMax(new BigDecimal(200));
        ruleDto.setDonateType(1);
        ruleDto.setDonateAmount(new BigDecimal(20));
        ruleDto.setDonateAmountMax(new BigDecimal(300));
        ruleDto.setMultipleWater(10.0);
        ruleDtos.add(ruleDto);
        dto.setActivityRuleDtos(ruleDtos);

        ActivityDto activityDto = new ActivityDto();
        activityDto.setActActivity(activity);
        activityDto.setObject(dto);
        System.out.println(new Gson().toJson(dto));
        oprActBaseService.save(activityDto, "admin", null, null);
    }


    @Test
    public void AQ0000004() {
       // DynamicDataSource.setDataSource("187");
        OprActActivity activity = new OprActActivity();
        activity.setActTmplId(4);
        activity.setActivityName("救援金活动");
        activity.setActCatId(5);
        activity.setShowStart("2017-12-24");
        activity.setShowEnd("2017-12-27");
        activity.setUseStart("2017-12-28");
        activity.setUseEnd("2017-12-28");
        activity.setUseState(0);
        activity.setIsShow(true);
        activity.setEnablePc(true);
        activity.setEnableMb(true);
        activity.setContent("测试存救援金啊啊啊啊啊啊啊啊啊啊啊啊");

        JRescueDto dto = new JRescueDto();
        ActivityScopeDto scopeDto = new ActivityScopeDto();
        scopeDto.setIsAccAll(false);
        scopeDto.setAccIds(Lists.newArrayList(1, 5, 15, 16));
        scopeDto.setIsAgyTopAll(false);
        scopeDto.setAgyTopIds(Lists.newArrayList(20, 21, 22));
        scopeDto.setIsAgyAll(true);
        dto.setScopeDto(scopeDto);
        dto.setScope(2);
        dto.setIsAudit(true);
        dto.setIsBank(true);
        dto.setIsMail(true);
        dto.setIsMobile(true);
        dto.setIsName(true);


        List<AuditCat> auditCats = Lists.newArrayList();
        AuditCat auditCat = new AuditCat();
        auditCat.setCatId(1);
        auditCat.setIsAll(true);
        auditCats.add(auditCat);
        AuditCat auditCat1 = new AuditCat();
        auditCat.setCatId(1);
        auditCat1.setIsAll(true);
        auditCats.add(auditCat1);
        AuditCat auditCat2 = new AuditCat();
        auditCat.setCatId(1);
        auditCat2.setIsAll(true);
        auditCats.add(auditCat2);
        AuditCat auditCat3 = new AuditCat();
        auditCat.setCatId(1);
        auditCat3.setIsAll(true);
        auditCats.add(auditCat3);
        dto.setAuditCats(auditCats);

        dto.setDrawType(0);
        dto.setDrawNumber(1);
        dto.setFormulaMode(1);
        dto.setTotalMoney(new BigDecimal(3));

        List<RescueRuleDto> ruleDtos = Lists.newArrayList();
        RescueRuleDto rescueRuleDto = new RescueRuleDto();
        rescueRuleDto.setAmountMin(new BigDecimal(100));
        rescueRuleDto.setAmountMax(new BigDecimal(500));
        rescueRuleDto.setLoseAmountMin(new BigDecimal(50));
        rescueRuleDto.setLoseAmountMax(new BigDecimal(150));
        rescueRuleDto.setDonateType(0);
        rescueRuleDto.setDonateRatio(new BigDecimal(12));
        rescueRuleDto.setDonateAmountMax(new BigDecimal(5000));
        rescueRuleDto.setMultipleWater(10.0);
        ruleDtos.add(rescueRuleDto);

        RescueRuleDto rescueRuleDto1 = new RescueRuleDto();
        rescueRuleDto1.setAmountMin(new BigDecimal(100));
        rescueRuleDto1.setAmountMax(new BigDecimal(500));
        rescueRuleDto1.setLoseAmountMin(new BigDecimal(50));
        rescueRuleDto1.setLoseAmountMax(new BigDecimal(150));
        rescueRuleDto1.setDonateType(1);
        rescueRuleDto1.setDonateAmount(new BigDecimal(100));
        rescueRuleDto1.setDonateAmountMax(new BigDecimal(5000));
        rescueRuleDto1.setMultipleWater(10.0);
        ruleDtos.add(rescueRuleDto1);
        dto.setRuleDtos(ruleDtos);

        ActivityDto activityDto = new ActivityDto();
        activityDto.setActActivity(activity);
        activityDto.setObject(dto);
        System.out.println(new Gson().toJson(dto));
        oprActBaseService.save(activityDto, "admin", null, null);
    }

    @Test
    public void AQ0000005() {
       // DynamicDataSource.setDataSource("test");
        OprActActivity activity = new OprActActivity();
        activity.setActTmplId(5);
        activity.setActivityName("返水优惠活动");
        activity.setActCatId(5);
        activity.setShowStart("2017-12-24");
        activity.setShowEnd("2017-12-27");
        activity.setUseStart("2018-12-28");
        activity.setUseEnd("2019-12-28");
        activity.setUseState(0);
        activity.setIsShow(true);
        activity.setEnablePc(true);
        activity.setEnableMb(true);
        activity.setContent("测试返水优惠活动啊啊啊啊啊啊啊啊啊啊啊啊");

        JWaterRebatesDto dto = new JWaterRebatesDto();
        ActivityScopeDto scopeDto = new ActivityScopeDto();
        scopeDto.setIsAccAll(false);
        scopeDto.setAccIds(Lists.newArrayList(1, 5, 15, 16));
        scopeDto.setIsAgyTopAll(false);
        scopeDto.setAgyTopIds(Lists.newArrayList(20, 21, 22));
        scopeDto.setIsAgyAll(true);
        dto.setScopeDto(scopeDto);
        dto.setScope(2);
        dto.setIsAudit(true);
        dto.setIsBank(true);
        dto.setIsMail(true);
        dto.setIsMobile(true);
        dto.setIsName(true);
        dto.setDrawType(0);
        dto.setDrawNumber(1);

        List<AuditCat> auditCats = Lists.newArrayList();
        AuditCat auditCat = new AuditCat();
        auditCat.setCatId(1);
        auditCat.setIsAll(true);
        auditCats.add(auditCat);
        AuditCat auditCat1 = new AuditCat();
        List<AuditDepot> depots2 = Lists.newArrayList();
        AuditDepot auditDepot2 = new AuditDepot();
        auditDepot2.setDepotId(0);
        auditDepot2.setGames(null);
        depots2.add(auditDepot2);
        auditCat1.setDepots(depots2);
        auditCat1.setCatId(5);
        auditCat1.setIsAll(true);
        auditCats.add(auditCat1);

        AuditCat auditCat2 = new AuditCat();
        List<AuditDepot> depots1 = Lists.newArrayList();
        AuditDepot auditDepot1 = new AuditDepot();
        auditDepot1.setDepotId(0);
        auditDepot1.setGames(null);
        depots1.add(auditDepot1);
        auditCat2.setDepots(depots1);
        auditCat2.setCatId(3);
        auditCat2.setIsAll(true);
        auditCats.add(auditCat2);

        AuditCat auditCat3 = new AuditCat();
        List<AuditDepot> depots = Lists.newArrayList();
        AuditDepot auditDepot = new AuditDepot();
        auditDepot.setDepotId(1);
        auditDepot.setGames(Lists.newArrayList(1, 2, 3, 88));
        depots.add(auditDepot);
        auditCat3.setCatId(12);
        auditCat3.setIsAll(false);
        auditCat3.setDepots(depots);
        auditCats.add(auditCat3);
        dto.setRuleDtos(auditCats);

        List<WaterRebatesRuleListDto> ruleListDtos = Lists.newArrayList();
        WaterRebatesRuleListDto ruleListDto = new WaterRebatesRuleListDto();
        ruleListDto.setValidAmountMin(new BigDecimal(1));
        ruleListDto.setValidAmountMax(new BigDecimal(2000));
        ruleListDto.setDonateRatio(new BigDecimal(10));
        ruleListDtos.add(ruleListDto);
        dto.setRuleListDtos(ruleListDtos);


        ActivityDto activityDto = new ActivityDto();
        activityDto.setActActivity(new Gson().toJson(activity));
        activityDto.setObject(new Gson().toJson(dto));
        System.out.println(new Gson().toJson(dto));
        oprActBaseService.save(activityDto, "admin", null, null);
    }


    @Test
    public void AQ0000006() {
        //DynamicDataSource.setDataSource("187");
        OprActActivity activity = new OprActActivity();
        activity.setActTmplId(6);
        activity.setActivityName("有效投注活动");
        activity.setActCatId(5);
        activity.setShowStart("2017-12-24");
        activity.setShowEnd("2017-12-27");
        activity.setUseStart("2017-12-28");
        activity.setUseEnd("2017-12-28");
        activity.setUseState(0);
        activity.setIsShow(true);
        activity.setEnablePc(true);
        activity.setEnableMb(true);
        activity.setContent("测试有效投注活动啊啊啊啊啊啊啊啊啊啊啊啊");

        JValidDto dto = new JValidDto();
        ActivityScopeDto scopeDto = new ActivityScopeDto();
        scopeDto.setIsAccAll(false);
        scopeDto.setAccIds(Lists.newArrayList(1, 5, 15, 16));
        scopeDto.setIsAgyTopAll(false);
        scopeDto.setAgyTopIds(Lists.newArrayList(20, 21, 22));
        scopeDto.setIsAgyAll(true);
        dto.setScopeDto(scopeDto);
        dto.setScope(2);
        dto.setIsAudit(true);
        dto.setIsBank(true);
        dto.setIsMail(true);
        dto.setIsMobile(true);
        dto.setIsName(true);
        dto.setDrawType(0);
        dto.setDrawNumber(1);
        dto.setFormulaMode(0);

        List<AuditCat> auditCats = Lists.newArrayList();
        AuditCat auditCat = new AuditCat();
        auditCat.setCatId(1);
        auditCat.setIsAll(true);
        auditCats.add(auditCat);
        AuditCat auditCat1 = new AuditCat();
        auditCat.setCatId(1);
        auditCat1.setIsAll(true);
        auditCats.add(auditCat1);
        AuditCat auditCat2 = new AuditCat();
        auditCat.setCatId(1);
        auditCat2.setIsAll(true);
        auditCats.add(auditCat2);
        AuditCat auditCat3 = new AuditCat();
        auditCat.setCatId(1);
        auditCat3.setIsAll(true);
        auditCats.add(auditCat3);
        dto.setAuditCats(auditCats);


        List<WaterRebatesRuleListDto> ruleListDtos = Lists.newArrayList();
        WaterRebatesRuleListDto ruleListDto = new WaterRebatesRuleListDto();
        ruleListDto.setValidAmountMin(new BigDecimal(1000));
        ruleListDto.setValidAmountMax(new BigDecimal(2000));
        ruleListDto.setDonateRatio(new BigDecimal(10));
        ruleListDtos.add(ruleListDto);

        WaterRebatesRuleListDto ruleListDto1 = new WaterRebatesRuleListDto();
        ruleListDto1.setValidAmountMin(new BigDecimal(10100));
        ruleListDto1.setValidAmountMax(new BigDecimal(20010));
        ruleListDto1.setDonateRatio(new BigDecimal(20));
        ruleListDtos.add(ruleListDto1);

        dto.setRuleDtos(ruleListDtos);

        ActivityDto activityDto = new ActivityDto();
        activityDto.setActActivity(activity);
        activityDto.setObject(dto);
        System.out.println(new Gson().toJson(dto));
        oprActBaseService.save(activityDto, "admin", null, null);
    }


    @Test
    public void AQ0000007() {
        //DynamicDataSource.setDataSource("187");
        OprActActivity activity = new OprActActivity();
        activity.setActTmplId(7);
        activity.setActivityName("推荐送活动");
        activity.setActCatId(5);
        activity.setShowStart("2017-12-24");
        activity.setShowEnd("2017-12-27");
        activity.setUseStart("2017-12-28");
        activity.setUseEnd("2017-12-28");
        activity.setUseState(0);
        activity.setIsShow(true);
        activity.setEnablePc(true);
        activity.setEnableMb(true);
        activity.setContent("测试推荐送活动啊啊啊啊啊啊啊啊啊啊啊啊");

        JRecommendDto dto = new JRecommendDto();
        ActivityScopeDto scopeDto = new ActivityScopeDto();
        scopeDto.setIsAccAll(false);
        scopeDto.setAccIds(Lists.newArrayList(1, 5, 15, 16));
        scopeDto.setIsAgyTopAll(false);
        scopeDto.setAgyTopIds(Lists.newArrayList(20, 21, 22));
        scopeDto.setIsAgyAll(true);
        dto.setScopeDto(scopeDto);
        dto.setIsAudit(true);

        RecommendAwardDto awardDto = new RecommendAwardDto();
        awardDto.setIsAward(true);
        awardDto.setAwardType(1);
        awardDto.setAwardBasics(new BigDecimal(500));
        awardDto.setAwardMoney(new BigDecimal(30));
        awardDto.setMultipleWater(8);
        dto.setAward(awardDto);

        RecommendBonusDto bonus = new RecommendBonusDto();
        bonus.setIsBonus(true);
        bonus.setBetMoney(new BigDecimal(2000));
        bonus.setBonusMax(new BigDecimal(500));
        bonus.setMultipleWater(10);
        List<RecommendBonusListDto> bonusListDtos = Lists.newArrayList();
        RecommendBonusListDto recommendBonusListDto = new RecommendBonusListDto();
        recommendBonusListDto.setBetNumber(1);
        recommendBonusListDto.setBonusRatio(new BigDecimal(10));
        bonusListDtos.add(recommendBonusListDto);
        bonus.setBonusListDtos(bonusListDtos);
        dto.setBonus(bonus);

        ActivityDto activityDto = new ActivityDto();
        activityDto.setActActivity(activity);
        activityDto.setObject(dto);
        System.out.println(new Gson().toJson(dto));
        oprActBaseService.save(activityDto, "admin", null, null);
    }


    @Test
    public void AQ0000008() {
        //DynamicDataSource.setDataSource("187");
        OprActActivity activity = new OprActActivity();
        activity.setActTmplId(8);
        activity.setActivityName("签到活动");
        activity.setActCatId(5);
        activity.setShowStart("2017-12-24");
        activity.setShowEnd("2017-12-27");
        activity.setUseStart("2017-12-28");
        activity.setUseEnd("2017-12-28");
        activity.setUseState(0);
        activity.setIsShow(true);
        activity.setEnablePc(true);
        activity.setEnableMb(true);
        activity.setContent("测试签到活动啊啊啊啊啊啊啊啊啊啊啊啊");

        JSignInDto dto = new JSignInDto();
        ActivityScopeDto scopeDto = new ActivityScopeDto();
        scopeDto.setIsAccAll(false);
        scopeDto.setAccIds(Lists.newArrayList(1, 5, 15, 16));
        scopeDto.setIsAgyTopAll(false);
        scopeDto.setAgyTopIds(Lists.newArrayList(20, 21, 22));
        scopeDto.setIsAgyAll(true);
        dto.setScopeDto(scopeDto);
        dto.setIsAudit(true);
        dto.setIsBank(true);
        dto.setIsMail(true);
        dto.setIsMobile(true);
        dto.setIsName(true);
        dto.setSignType(0);
        dto.setSignBenchmark(0);

        List<SignInRuleDto> ruleDtos = Lists.newArrayList();
        SignInRuleDto signInRuleDto = new SignInRuleDto();
        signInRuleDto.setValidAmountMin(new BigDecimal(100));
        signInRuleDto.setDonateAmountMax(new BigDecimal(300));
        signInRuleDto.setDonateType(1);
        signInRuleDto.setDonateAmountMax(new BigDecimal(300));
        signInRuleDto.setMultipleWaterType(0);
        signInRuleDto.setMultipleWater(10.0);
        ruleDtos.add(signInRuleDto);
        dto.setRuleDtos(ruleDtos);

        ActivityDto activityDto = new ActivityDto();
        activityDto.setActActivity(activity);
        activityDto.setObject(dto);
        System.out.println(new Gson().toJson(dto));
        oprActBaseService.save(activityDto, "admin", null, null);
    }

    @Test
    public void AQ0000009() {
        //DynamicDataSource.setDataSource("187");
        OprActActivity activity = new OprActActivity();
        activity.setActTmplId(9);
        activity.setActivityName("红包活动");
        activity.setActCatId(5);
        activity.setShowStart("2017-12-24");
        activity.setShowEnd("2017-12-27");
        activity.setUseStart("2017-12-28");
        activity.setUseEnd("2017-12-28");
        activity.setUseState(0);
        activity.setIsShow(true);
        activity.setEnablePc(true);
        activity.setEnableMb(true);
        activity.setContent("测试红包活动啊啊啊啊啊啊啊啊啊啊啊啊");

        JRedPacketDto dto = new JRedPacketDto();
        ActivityScopeDto scopeDto = new ActivityScopeDto();
        scopeDto.setIsAccAll(false);
        scopeDto.setAccIds(Lists.newArrayList(1, 5, 15, 16));
        scopeDto.setIsAgyTopAll(false);
        scopeDto.setAgyTopIds(Lists.newArrayList(20, 21, 22));
        scopeDto.setIsAgyAll(true);
        dto.setScopeDto(scopeDto);
        dto.setIsAudit(true);


        ActivityDto activityDto = new ActivityDto();
        activityDto.setActActivity(activity);
        activityDto.setObject(dto);
        System.out.println(new Gson().toJson(dto));
        oprActBaseService.save(activityDto, "admin", null, null);
    }
}
