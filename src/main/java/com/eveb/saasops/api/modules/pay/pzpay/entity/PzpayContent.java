package com.eveb.saasops.api.modules.pay.pzpay.entity;

import lombok.Data;

/**
 * 盘子支付响应参数
 * Created by William on 2017/12/7.
 */
@Data
public class PzpayContent {
    /**
     *商户网站唯一订单号。 对应商户网站的订单系统中的唯一订 单号，非盘子支付交易号。需保证在 商户网站中的唯一性。请求时对应的 参数，原样返回。
     */
    private Long out_trade_no;
    /**
     *盘子支付交易号，该交易在盘子支付 的交易流水号。最长 64 位
     */
    private String trade_no;
    /**
     *订单金额，单位分
     */
    private Integer total_fee;
    /**
     *商品名称，是请求时对应的参数，原 样通知回来。 UrlDecode 解码查看。 不参与签
     */
    private String subject;
    /**
     *公用回传参数，注意该参数不会带入 第三方支付平台，而是盘子支付回传 给商户网站，该值不能包含“=”、 “&”等特殊字符。 UrlDecode 解码查看。 不参与签
     */
    private String trade_status;
    /**
     *支付完成时间，指交易在第三方支付 的支付完成时间。 格式为 yyyyMMddHHmmss
     */
    private String extra;
    /**
     *该笔交易创建的时间，指交易在盘子 支付的创建时间。 格式为 yyyyMMddHHmms
     */
    private String create_time;
    /**
     *支付完成时间，指交易在第三方支付 的支付完成时间。 格式为 yyyyMMddHHmmss
     */
    private String pay_time;
    /**
     *错误信息，发生错误时，返回第三方 支付或者盘子支付的错误信息。 UrlDecode 解码查看。 不参与签名

     */
    private String pay_error;
    /**
     *签名，请查看：签名生成方法
     */
    private String sign ;
}
