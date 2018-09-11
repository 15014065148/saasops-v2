package com.eveb.saasops.api.modules.pay.pzpay.entity;

import lombok.Data;

/**
 * Created by William on 2017/12/27.
 */
@Data
public class PayPictureData {
    private Integer id ;
    private String bankName;
    private String bankCode;
    private String bankLog;
    private Integer paymentId;
}
