package com.eveb.saasops.modules.base.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Table(name="t_bs_bank")
@Getter
@Setter
public class BaseBank {
    /**
     * 
     * 表 : base_bank
     * 对应字段 : id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 
     * 表 : base_bank
     * 对应字段 : bankName
     */
    @Column
    private String bankName;

    /**
     * 
     * 表 : base_bank
     * 对应字段 : bankCode
     */
    @Column
    private String bankCode;

    /**
     * 
     * 表 : base_bank
     * 对应字段 : bankLog
     */
    @Column
    private String bankLog;
    /**
     * 是否支持取款(1,是，0否)
     */
    @Column
    private Byte wDEnable;



}