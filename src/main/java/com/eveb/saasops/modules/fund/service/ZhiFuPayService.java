package com.eveb.saasops.modules.fund.service;

import com.alibaba.fastjson.JSON;
import com.eveb.saasops.api.modules.pay.pzpay.dto.OnlinePayDto;
import com.eveb.saasops.api.modules.pay.pzpay.entity.PzpayPayParams;
import com.eveb.saasops.api.modules.user.service.OkHttpService;
import com.eveb.saasops.api.utils.JsonUtil;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.utils.pay.ZhiFuPay;
import com.eveb.saasops.modules.fund.dao.FundDepositMapper;
import com.eveb.saasops.modules.fund.dao.TOpPayMapper;
import com.eveb.saasops.modules.fund.dto.*;
import com.eveb.saasops.modules.fund.entity.FundDeposit;
import com.eveb.saasops.modules.fund.entity.TOpPay;
import com.eveb.saasops.modules.fund.mapper.FundMapper;
import com.eveb.saasops.modules.system.onlinepay.dao.SetBacicOnlinepayMapper;
import com.eveb.saasops.modules.system.onlinepay.entity.SetBacicOnlinepay;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.eveb.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME2;
import static com.eveb.saasops.common.utils.DateUtil.getCurrentDate;

@Slf4j
@Service
public class ZhiFuPayService {

    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private OkHttpService okHttpService;
    @Autowired
    private FundDepositMapper fundDepositMapper;
    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private TOpPayMapper tOpPayMapper;
    @Autowired
    private FundDepositService fundDepositService;
    @Autowired
    private SetBacicOnlinepayMapper setBacicOnlinepayMapper;

    private static final String method = "easypay.trade.pay";
    private static final String platform = "alipay";
    private static final String callback_method = "api/OnlinePay/pzPay/zhiFuCallback";
//    private static final String url = "https://g99.liangdiming.com/gateway.php";
//    private static final String secret = "bb0e6471547745560079afd920baed0cfe19bc90";

    public Map<String, Object> zhiFuPay(PzpayPayParams pzpayPayParams, OnlinePayDto onlinePayDto, String siteCode) {
        Map<String, Object> paramr = new HashMap<>();
        pzpayPayParams.setTerminal(Objects.nonNull(pzpayPayParams.getTerminal())
                && pzpayPayParams.getTerminal() == Constants.EVNumber.one ?
                Constants.EVNumber.one : Constants.EVNumber.zero);
        TOpPay tOpPay = tOpPayMapper.selectByPrimaryKey(onlinePayDto.getPaymentId());
        ZhiFuTradeResponseDto responseDto = orderPayment(onlinePayDto.getMerNo(),
                pzpayPayParams.getOutTradeNo().toString(), onlinePayDto.getDescription(),
                pzpayPayParams.getFee(), pzpayPayParams.getTerminal(), onlinePayDto.getPassword(), siteCode, tOpPay);
        if (Objects.isNull(responseDto) || "FAIL".equals(responseDto.getStatus())) {
            FundDeposit fundDeposit = new FundDeposit();
            fundDeposit.setId(pzpayPayParams.getDepositId());
            fundDeposit.setStatus(Constants.EVNumber.zero);
            fundDeposit.setMemo("直付下单失败");
            fundDepositMapper.updateByPrimaryKeySelective(fundDeposit);
            paramr.put("status", Boolean.FALSE);
            paramr.put("message", Objects.nonNull(responseDto) ? responseDto.getMessage() : "直付下单失败");
            return paramr;
        }
        paramr.put("isQR",Boolean.FALSE);
        paramr.put("url", responseDto.getPage_url());
        return paramr;
    }

    public ZhiFuTradeResponseDto orderPayment(String key, String orderNo, String title, BigDecimal totalFee,
                                              Integer terminal, String secret, String memo, TOpPay tOpPay) {
        ZhiFuTradeRequestDto requestDto = new ZhiFuTradeRequestDto();
        requestDto.setKey(key);
        requestDto.setMethod(method);
        requestDto.setTrade_no(orderNo);
        requestDto.setTitle(title);
        requestDto.setMoney(totalFee.toString());
        requestDto.setPlatform(platform);
        requestDto.setMobile(Constants.EVNumber.one == terminal ? "Y" : "N");
        requestDto.setTimestamp(getCurrentDate(FORMAT_18_DATE_TIME2));
        requestDto.setNotify(tOpPay.getCallbackUrl() + callback_method);
        requestDto.setMemo(memo);
        String sign = ZhiFuPay.getSign(jsonUtil.toStringMap(requestDto), secret);
        requestDto.setSecret(secret);
        requestDto.setSign(sign);
        String result = okHttpService.postForm(okHttpService.getPayHttpClient(),
                tOpPay.getTPayUrl(), jsonUtil.toStringMap(requestDto));
        log.info("直付请求返回数据信息【" + result + "】");
        if (StringUtils.isEmpty(result)) return null;
        return jsonUtil.fromJson(result, ZhiFuTradeResponseDto.class);
    }

    public ZhiFuResponseDto zhiFuCallback(ZhiFuRequestDto requestDto) {
        log.info("直付回调返回数据信息【" + JSON.toJSONString(requestDto) + "】");
        ZhiFuResponseDto responseDto = new ZhiFuResponseDto();
        if (Objects.nonNull(requestDto)) {
            FundDeposit fundDeposit = new FundDeposit();
            fundDeposit.setOrderNo(requestDto.getTrade_no());
            FundDeposit deposit = fundMapper.updateFundDepositLock(fundDeposit);
            if (Objects.nonNull(deposit) && deposit.getStatus() == Constants.EVNumber.two) {
                SetBacicOnlinepay setBacicOnlinepay = setBacicOnlinepayMapper.selectByPrimaryKey(deposit.getOnlinePayId());
                String mySign = ZhiFuPay.getSign(jsonUtil.toStringMap(requestDto), setBacicOnlinepay.getPassword());
                if (mySign.equals(requestDto.getSign())) {
                    fundDepositService.updateDepositSucceed(deposit);
                    fundDepositMapper.updateByPrimaryKeySelective(deposit);
                    responseDto.setStatus("SUCCESS");
                    responseDto.setMessage("成功");
                } else {
                    responseDto.setStatus("FAIL");
                    responseDto.setMessage("加密不对称");
                }
                responseDto.setKey(requestDto.getKey());
                responseDto.setTrade_no(requestDto.getTrade_no());
                responseDto.setSn(requestDto.getSn());
                String sign = ZhiFuPay.getSign(jsonUtil.toStringMap(responseDto), setBacicOnlinepay.getPassword());
                responseDto.setSign(sign);
                return responseDto;
            }
        }
        return null;
    }

}
