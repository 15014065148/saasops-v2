package com.eveb.saasops.modules.system.onlinepay.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Setter
@Getter
@ApiModel(value = "SetBacicOnlinepay", description = "")
@Table(name = "set_bacic_onlinePay")
public class SetBacicOnlinepay implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    //账户名称
    @ApiModelProperty(value = "账户名称")
    private String name;
    //支付编码
    @ApiModelProperty(value = "支付编码")
    private String code;
    //支付平台Id
    @ApiModelProperty(value = "支付平台Id")
    private Integer paymentId;
    @ApiModelProperty(value = "商户号")
    private String merNo;
    //是否启用 1启用 2禁言
    @ApiModelProperty(value = "是否启用 1启用 2禁言")
    private Integer isEnable;
    //创建者
    @ApiModelProperty(value = "创建者")
    private String createUser;
    //创建时间
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
    //更新人
    @ApiModelProperty(value = "更新人")
    private String modifyUser;
    //最后一次更新时间
    @ApiModelProperty(value = "最后一次更新时间")
    private Date modifyTime;
    @ApiModelProperty(value = "账户描述")
    private String description;
    @ApiModelProperty(value = "密钥")
    private String password;
    @ApiModelProperty(value = "排序号")
    private Integer sort;
    @ApiModelProperty(value = "单笔存款最小值")
    private Integer minLimit;
    @ApiModelProperty(value = "单笔存款最大值")
    private Integer maxLimit;
    @ApiModelProperty(value = "单日最大限额")
    private Integer maxLimitDaily;
    @ApiModelProperty(value = "会员组关联方式 1：全部 2：自定义")
    private Integer mbrGroupType;
    @ApiModelProperty(value = "关联支付域名id")
    private Integer domainId;
    @ApiModelProperty(value = "设备来源;PC:0,H5:3")
    private String devSource;
    @ApiModelProperty(value = "支付种类")
    @Transient
    private String payClass;
    @ApiModelProperty(value = "支付方式")
    @Transient
    private String payWay;
    //@ApiModelProperty(value = "是否为二维码 0 不是 ,1 是")
    //private Integer isQR;
    //@ApiModelProperty(value = "是否为二维码 0 不是 ,1 是")
    //private Integer urlMethod;

    @Transient
    @ApiModelProperty(value = "支持会员组数")
    private Integer mbrGroupNum;
    @Transient
    @ApiModelProperty(value = "支付平台名称")
    private String paymentName;
    @Transient
    @ApiModelProperty(value = "支持银行数")
    private Integer bankNum;
    @Transient
    @ApiModelProperty(value = "支持银行")
    private String bankName;
    @Transient
    @ApiModelProperty(value = "是否启用 1启用 2禁言 查询使用")
    private String isEnables;
    @Transient
    @ApiModelProperty(value = "插入会员组关联关系")
    private String mbrGroups;
    @Transient
    @ApiModelProperty(value = "支持银行集合")
    private Integer[] banks;
    @Transient
    private String bankOptions;
    @Transient
    private String selectedGroup;
    @Transient
    private BigDecimal depositAmount;
    @Transient
    private BigDecimal depositAmountDaily;
    @Transient
    private Integer payCount;
    @Transient
    @ApiModelProperty(value = "支付渠道")
    private String payName;
    @Transient
    @ApiModelProperty(value = "是否启用 1启用 2禁言 查询使用")
    private List<String> isEnabless;
    @Transient
    @ApiModelProperty(value = "会员名")
    private String loginName;
}