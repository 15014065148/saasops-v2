package com.eveb.saasops.saasopsv2;

import com.eveb.saasops.modules.log.service.LogSystemService;
import com.eveb.saasops.modules.member.service.AuditAccountService;
import com.eveb.saasops.modules.operate.service.OprActActivityCastService;
import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;


@RunWith(SpringRunner.class)
@SpringBootTest
public class applyActivityTests {

    @Autowired
    private OprActActivityCastService actActivityCastService;

    @Autowired
    LogSystemService logSystemService;


    @Test
    public void applyActivity() {
        actActivityCastService.applyActivity(438,57,"ybh","",null);
    }

    @Test
    public void queryLog() {
        System.out.println(new Gson().toJson(logSystemService.queryLog(1,10,null)));
    }

    @Test
    public void test(){
        actActivityCastService.accountBonusList(445,null,1,100);
    }


}
