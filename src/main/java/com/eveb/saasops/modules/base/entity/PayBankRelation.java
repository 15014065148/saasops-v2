package com.eveb.saasops.modules.base.entity;


import lombok.Data;

import javax.persistence.Table;

/**
 * Created by William on 2018/3/26.
 */
@Data
@Table(name = "t_op_payBankRelation")
public class PayBankRelation {
    private Integer id ;
    private Integer bankId;
    private Integer paymentId;

    public PayBankRelation() {}

    public PayBankRelation( Integer paymentId) {
        this.bankId = bankId;
    }
}
