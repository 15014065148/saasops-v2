package com.eveb.saasops.api.modules.pay.pzpay.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by William on 2017/12/27.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnlinePayPicture {

    private String name;

    private Integer paymentId;

    private Integer payType;
    private Integer minLimit;
    private Integer maxLimit;
    private Integer urlMethod;
    private String mBankLogo;

    private List<PayPictureData> payData;

}
