package com.eveb.saasops.api.modules.pay.pzpay.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public enum PzpayPayType {
    /**
     * 支付宝支付
     */
    ALiPay(2, "QR_ALI"),
    /**
     * 微信支付
     */
    WeiXing(3, "QR_WX"),
    /**
     * 信用卡支付
     */
    CreditCard(4, "BANK"),
    /**
     * 借记卡支付
     */
    BankCard(5, "BANK"),
    /**
     * QQ支付
     */
    QQPay(6, "QR_QQ"),
    /**
     * 银联支付
     */
    UnionPay(7, "QR_UNION"),
    /**
     * 京东支付
     */
    JdPay(8, "QR_JD"),
    /**
     * 直付支付宝
     **/
    ZhiFu(9, "ZHIFU");


    @Getter
    private Integer payType;
    @Getter
    private String payCode;

    // 普通方法
    public static String getName(int payType) {
        for (PzpayPayType pay : PzpayPayType.values()) {
            if (pay.getPayType() == payType) {
                return pay.getPayCode();
            }
        }
        return null;
    }

}