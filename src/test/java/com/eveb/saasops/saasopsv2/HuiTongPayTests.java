package com.eveb.saasops.saasopsv2;

import java.math.BigDecimal;

import com.eveb.saasops.common.utils.SnowFlake;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.eveb.saasops.api.modules.pay.pzpay.entity.PzpayPayParams;
import com.eveb.saasops.api.modules.pay.pzpay.service.OnlinePayService;
import com.eveb.saasops.modules.fund.dao.TOpPayMapper;
import com.eveb.saasops.modules.fund.service.HuiTongPayService;
import com.eveb.saasops.modules.fund.service.PanZiPayService;
import com.eveb.saasops.modules.fund.service.ZhiFuPayService;


@RunWith(SpringRunner.class)
@SpringBootTest
public class HuiTongPayTests {

    @Autowired
    private HuiTongPayService huiTongPayService;
    @Autowired
    private PanZiPayService panZiPayService;
    @Autowired
    private ZhiFuPayService zhiFuPayService;
    @Autowired
    private TOpPayMapper tOpPayMapper;

    @Autowired
    private OnlinePayService onlinePayService;

    @Test
    public void test1() {
        PzpayPayParams pzpayPayParams = new PzpayPayParams();
        pzpayPayParams.setFee(new BigDecimal(200));
        pzpayPayParams.setTerminal(0);
        pzpayPayParams.setPayType(48);
        pzpayPayParams.setAccountId(488);
        pzpayPayParams.setExtra("www.evebdemo.com");
        pzpayPayParams.setIp("127.0.0.1");
        pzpayPayParams.setOutTradeNo(new SnowFlake().nextId() );
        pzpayPayParams.setBankCode("ICBC");
        onlinePayService.optionPayment("ybh",pzpayPayParams);
    }

    @Test
    public void test() throws Exception{
        //System.out.println(JSON.toJSON(huiTongPayService.balance()));
        //System.out.println(JSON.toJSON(huiTongPayService.remitPay(new BigDecimal(0.01),"ICBC","王大爷","6212761911001799970")));
        //{"transid":"11184373842575360","bank_status":"1","sign":"3870ef48388dbea2826a725096ef0d48","is_success":"true","order_id":"2005560404118670","errror_msg":""}
        //System.out.println(JSON.toJSON(huiTongPayService.remitQuery("11184373842575360")));



       // panZiPayService.debitPayment("ABC","李四","334455",new BigDecimal(1));
        //panZiPayService.debitQuery("11639253961277440");
    }

    @Test
    public void test_1() {
//        String order = new SnowFlake().nextId()+"";
//        System.out.println("111111111111=="+order);
//        TOpPay tOpPay = tOpPayMapper.selectByPrimaryKey(9);
//        zhiFuPayService.orderPayment("ep2018042462231921", order,
//                "测试",new BigDecimal(10),0,"bb0e6471547745560079afd920baed0cfe19bc90","test",tOpPay);
    }
}
