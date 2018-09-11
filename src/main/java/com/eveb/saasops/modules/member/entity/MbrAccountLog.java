package com.eveb.saasops.modules.member.entity;

import com.eveb.saasops.modules.member.dto.AccountLogDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;


@Setter
@Getter
@ApiModel(value = "MbrAccountLog", description = "OprActRule")
@Table(name = "mbr_account_log")
public class MbrAccountLog implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ACCOUNT_NAME = "真实姓名";
    public static final String ACCOUNT_BANK = "银行卡";
    public static final String ACCOUNT_MOBILE = "手机";
    public static final String ACCOUNT_EMAIL = "邮箱";
    public static final String ACCOUNT_QQ = "QQ";
    public static final String ACCOUNT_WECHAT = "微信";
    public static final String ACCOUNT_GROUP = "会员组";
    public static final String ACCOUNT_AGENT = "代理";
    public static final String ACCOUNT_STATUS = "状态";
    public static final String ACCOUNT_MEMO = "备注";
    public static final String ACCOUNT_INFO = "会员信息修改";
    public static final String ACCOUNT_INFO_OT = "会员其他资料修改";
    public static final String ACCOUNT_SUCCEED = "成功";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "订单号")
    private Long orderNo;

    @ApiModelProperty(value = "会员ID")
    private Integer accountId;

    @ApiModelProperty(value = "会员名")
    private String loginName;

    @ApiModelProperty(value = "createTime")
    private String createTime;

    @ApiModelProperty(value = "内容")
    private String content;

    @Transient
    private AccountLogDto logDto;
}