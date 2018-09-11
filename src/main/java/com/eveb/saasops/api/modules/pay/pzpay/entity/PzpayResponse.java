package com.eveb.saasops.api.modules.pay.pzpay.entity;

import lombok.Data;

/**
 * Created by William on 2017/12/13.
 */
@Data
public class PzpayResponse {
    private Boolean success;
    private String message;
    private Object content;
}
