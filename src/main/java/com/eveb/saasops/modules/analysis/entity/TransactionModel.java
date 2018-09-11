package com.eveb.saasops.modules.analysis.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class TransactionModel {

    /**类型**/
    private String transactionName;
    private String orderNo;
    /**时间**/
    private Date createTime;
    /***帐号**/
    private String accountName;
    /***金额**/
    private BigDecimal amount;
    /***备注***/
    private String memo;
    /***审核人**/
    private String auditUser;
    /***方式**/
    private String transactionType;
    /***活动字段***/
    /**活动名称*/
    private String activityName;
    /**活动分类*/
    private String catName;
    /**申请时间*/
    private Date applicationTime;
    /**红利金额**/
    private BigDecimal bonusAmount;
    /**银行代码*/
    private String bankcode;
    /**入款类别 0：线上 1：公司*/
    private String mark;
    /**实际出款*/
    private Double withdraw;
}
