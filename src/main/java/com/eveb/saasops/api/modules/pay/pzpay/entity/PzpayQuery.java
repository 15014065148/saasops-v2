package com.eveb.saasops.api.modules.pay.pzpay.entity;

import lombok.Data;

/**
 * Created by William on 2017/12/8.
 */
@Data
public class PzpayQuery {

    private String partner_id;
    private String out_trade_no;
    private String trade_no;

    public PzpayQuery() {}

    public PzpayQuery(String partner_id, String out_trade_no, String trade_no) {
        this.partner_id = partner_id;
        this.out_trade_no = out_trade_no;
        this.trade_no = trade_no;
    }
}
