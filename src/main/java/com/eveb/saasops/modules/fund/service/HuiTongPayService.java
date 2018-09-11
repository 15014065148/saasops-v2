package com.eveb.saasops.modules.fund.service;

import com.eveb.saasops.api.modules.user.service.OkHttpService;
import com.eveb.saasops.api.utils.JsonUtil;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.pay.Utils;
import com.eveb.saasops.modules.fund.dao.AccWithdrawMapper;
import com.eveb.saasops.modules.fund.dto.*;
import com.eveb.saasops.modules.fund.entity.AccWithdraw;
import com.eveb.saasops.modules.fund.entity.FundMerchantPay;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;

import static com.eveb.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.eveb.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
public class HuiTongPayService {

    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private OkHttpService okHttpService;
    @Autowired
    private AccWithdrawMapper accWithdrawMapper;

   /* private static final String huiTongUrl = "https://api.huitongvip.com/";
    private static final String merchantCode = "11920556";
    private static final String key = "16326f1ae4fc2f165267a3dd33436832";*/

    private static final String remit_html = "remit.html"; //代付请求
    private static final String remit_query_html = "remit_query.html";//代付查询
    private static final String balance_html = "balance.html";//余额查询


    public HTRemitResponseDto remitPay(FundMerchantPay merchantPay, String orderNo, BigDecimal orderAmount,
                                       String bankCode, String accountName, String accountNumber) {
        if (isNull(merchantPay)) return null;
        String url = merchantPay.getUrl() + remit_html;
        HTRemitRequestDto requestDto = new HTRemitRequestDto();
        requestDto.setMerchant_code(merchantPay.getMerchantNo());
        requestDto.setOrder_amount(CommonUtil.adjustScale(orderAmount) + StringUtils.EMPTY);
        requestDto.setTrade_no(orderNo);
        requestDto.setOrder_time(getCurrentDate(FORMAT_18_DATE_TIME));
        requestDto.setBank_code(bankCode);
        requestDto.setAccount_name(accountName);
        requestDto.setAccount_number(accountNumber);
        requestDto.setSign(Utils.getSign(jsonUtil.toStringMap(requestDto), merchantPay.getMerchantKey()));
        try {
            requestDto.setAccount_name(URLEncoder.encode(accountName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = okHttpService.postForm(okHttpService.getPayHttpClient(), url, jsonUtil.toStringMap(requestDto));
        log.info("汇通代付请求返回数据信息【" + result + "】");
        if (StringUtils.isEmpty(result)) return null;
        return jsonUtil.fromJson(result, HTRemitResponseDto.class);
    }

    public HTRemitResponseDto remitQuery(AccWithdraw accWithdraw, FundMerchantPay merchantPay) {
        if (isNull(merchantPay)) return null;
        String url = merchantPay.getUrl() + remit_query_html;
        HTRemitQueryRequestDto requestDto = new HTRemitQueryRequestDto();
        requestDto.setMerchant_code(merchantPay.getMerchantNo());
        requestDto.setNow_date(getCurrentDate(FORMAT_18_DATE_TIME));
        requestDto.setTrade_no(accWithdraw.getTransId());
        requestDto.setSign(Utils.getSign(jsonUtil.toStringMap(requestDto), merchantPay.getMerchantKey()));
        String result = okHttpService.postForm(okHttpService.getPayHttpClient(), url, jsonUtil.toStringMap(requestDto));
        if (nonNull(result)) {
            PZQueryResponseDto responseDto = jsonUtil.fromJson(result, PZQueryResponseDto.class);
            if (Boolean.FALSE.equals(responseDto.getSuccess())) {
                accWithdraw.setMemo(responseDto.getMessage());
                accWithdrawMapper.updateByPrimaryKey(accWithdraw);
            }
            log.info("汇通代付查询返回数据信息【" + result + "】");
            return jsonUtil.fromJson(result, HTRemitResponseDto.class);
        }
        return null;
    }

    public HTBalanceResponseDto balance(FundMerchantPay merchantPay) {
        if (isNull(merchantPay)) return null;
        String url = merchantPay.getUrl() + balance_html;
        HTBalanceRequestDto requestDto = new HTBalanceRequestDto();
        requestDto.setMerchant_code(merchantPay.getMerchantNo());
        requestDto.setQuery_time(getCurrentDate(FORMAT_18_DATE_TIME));
        requestDto.setSign(Utils.getSign(jsonUtil.toStringMap(requestDto), merchantPay.getMerchantKey()));
        try {
            String result = okHttpService.postForm(okHttpService.getPayHttpClient(), url, jsonUtil.toStringMap(requestDto));
            log.info("汇通余额查询返回数据信息【" + result + "】");
            if (StringUtils.isEmpty(result)) return null;
            return jsonUtil.fromJson(result, HTBalanceResponseDto.class);
        } catch (Exception e) {
            log.error("汇通余额查询异常", e);
            return null;
        }
    }

}
