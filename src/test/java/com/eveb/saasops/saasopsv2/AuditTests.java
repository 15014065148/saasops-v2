package com.eveb.saasops.saasopsv2;

import com.eveb.saasops.api.modules.user.service.RedisService;
import com.eveb.saasops.common.constants.RedisConstants;
import com.eveb.saasops.modules.member.dao.MbrAccountMapper;
import com.eveb.saasops.modules.member.entity.MbrAccount;
import com.eveb.saasops.modules.member.service.AuditAccountService;
import com.eveb.saasops.modules.member.service.AuditCastService;
import com.eveb.saasops.modules.operate.dao.OprActBonusMapper;
import com.eveb.saasops.modules.operate.entity.OprActBonus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Objects;


@RunWith(SpringRunner.class)
@SpringBootTest
public class AuditTests {

    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private AuditCastService auditCastService;
    @Autowired
    private OprActBonusMapper actBonusMapper;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private RedisService redisService;

    @Test
    @Rollback(false)
    public void test12() {
        //auditCastService.doingCronAuditAccount("ybh", 488);
        auditAccountService.auditDetail(2);
    }

    @Test
    public void test132222(){
        auditAccountService.getAccountBonus(1,12,772,new BigDecimal(101),508);
    }


    @Test
    public void test1() {
        auditAccountService.isBounsOut(8, 1);
    }

    @Test
    public void getDepotAuditDto() {
        auditAccountService.getDepotAuditDto(8, 1);
    }


    @Test
    public void accountUseBonus() {
        OprActBonus actBonus = actBonusMapper.selectByPrimaryKey(637);
        MbrAccount account = accountMapper.selectByPrimaryKey(431);
        actBonus.setBonusAmount(new BigDecimal(8));
        actBonus.setDiscountAudit(2);
        auditAccountService.accountUseBonus(actBonus, account, new BigDecimal(80), 222, 5, 14);
    }

    @Test
    public void test999() {
        auditAccountService.auditCharge(233, new BigDecimal(3), "2434343434343434343", "127.0.0.1", "ybh", "rock");
    }


    @Test
    public void test00(){
        String transferInCont = RedisConstants.SEESION_TRANSFERIN + 1 + 12;
        if (Objects.nonNull(redisService.getRedisValus(transferInCont))){
            System.out.println("11111");
        }
        redisService.setRedisValue(transferInCont,1);
        redisService.del(transferInCont);
    }
}
