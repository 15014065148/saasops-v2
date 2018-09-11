package com.eveb.saasops.listener;

import lombok.Data;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

/**
 * Created by William on 2018/3/7
 */
@Data
public class BizEvent extends ApplicationEvent {

    private String siteCode;

    /**
     * 会员Id
     */
    private Integer userId ;

    /**
     * 代理Id
     */
    private Integer agencyId;

    private BizEventType eventType;

    private String oldPassword;
    private String newPassword;

    /**
     * 存款金额
     */
    private BigDecimal despoitMoney;
    /**
     * 订单号
     */
    private String orderNum;

    /**
     * 优惠(活动)金额
     */
    private BigDecimal acvitityMoney;
    /**
     * 优惠活动名称
     */
    private String acvitityName;

    /**
     * 提款金额
     */
    private BigDecimal withdrawMoney;

    /**
     * 佣金
     */
    private BigDecimal commssion;

    private String term;

    public BizEvent(Object source, String siteCode, Integer userId, BizEventType eventType) {
        super(source);
        this.eventType = eventType ;
        this.siteCode = siteCode;
        this.userId =userId ;
    }

    public BizEvent(Object source, String siteCode, Integer userId, BizEventType eventType,String oldPassword,String newPassword) {
        super(source);
        this.eventType = eventType ;
        this.siteCode = siteCode;
        this.userId =userId ;
        this.oldPassword=oldPassword;
        this.newPassword = newPassword ;
    }

    public BizEvent(Object source, String siteCode, Integer userId, BizEventType eventType, BigDecimal despoitMoney, String orderNum) {
        super(source);
        this.siteCode = siteCode;
        this.userId = userId;
        this.eventType = eventType;
        this.despoitMoney = despoitMoney;
        this.orderNum = orderNum;
    }
}
