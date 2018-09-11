/*package com.eveb.saasops.api.modules.pay.pzpay.entity;

import lombok.Data;

import java.math.BigDecimal;

*//**
 * 盘子支付
 * Created by William on 2017/12/6.
 *//*
@Data
public class Pzpay {
    *//**
     * 商户ID，盘子支付提供
     *//*
    private String partner_id;
    *//**
     *收款账号id，可在商户系统找到
     *//*
    private Long bank_id;
    *//**
     *银行代码，如果填写了该参数，则支付会跳转对应的网上银行。 详见：银行代码 可空
     *//*
    private String bank_code;
    *//**
     *支付方式。 取值：BANK表示网银，QR_WX表示微信扫码，QR_ALI表示支付宝扫码
     *//*
    private String pay_type;
    *//**
     *合作商户网站唯一订单号
     *//*
    private String out_trade_no;
    *//**
     *订单金额，单位分，仅支持人民币
     *//*
    private BigDecimal total_fee;
    *//**
     *商品名称。 不参与签名
     *//*
    private String subject;
    *//**
     *异步通知url
     *//*
    private String notify_url;
    *//**
     *同步返回url
     *//*
    private String return_url;
    *//**
     *用户在创建交易时，该用户当前所使用机器的IP。扫码时必填
     *//*
    private String client_ip;
    *//**
     *公用回传参数，注意该参数不会带入第三方支付平台，而是盘子支付回传给商户网站，该值不能包含“=”、 “&”等特殊字符。 不参与签名
     *//*
    private String extra;

    public Pzpay() {}

    public Pzpay(String partner_id, Long bank_id) {
        this.partner_id = partner_id;
        this.bank_id = bank_id;
    }
    public Pzpay(String partner_id, Long bank_id, int payType, String notify_url, String return_url, String out_trade_no, BigDecimal total_fee, String bank_code) {
        this.partner_id = partner_id;
        this.bank_id = bank_id;
        this.pay_type = PzpayPayType.getName(payType);
        this.return_url=return_url;
        this.notify_url=notify_url;
        this.out_trade_no=out_trade_no;
        this.total_fee=total_fee;
        this.bank_code=bank_code;
    }
}
*/