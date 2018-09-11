package com.eveb.saasops.api.modules.pay.pzpay.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OnlinePayDto implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty(value = "id")
    private Integer id;

    //账户名称
    @ApiModelProperty(value = "账户名称")
    private String name;
    
    //支付编码
    @ApiModelProperty(value = "支付编码")
    private String code;
    
    //支付编码
    @ApiModelProperty(value = "支付平台Code")
    private String payCode;
    
    //支付平台Id
    @ApiModelProperty(value = "支付平台Id")
    private Integer paymentId;
    
    @ApiModelProperty(value = "商户号")
    private  String merNo;
    
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
    private Integer sort ;
    
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
    
    @ApiModelProperty(value = "支付种类")
    private String payClass;
    
    @ApiModelProperty(value = "支付方式")
    private String payWay;
    
    /*@ApiModelProperty(value = "是否为二维码 0 不是 ,1 是")
    private Integer isQR;*/
    @ApiModelProperty(value = "是否为二维码 0 不是 ,1 是")
    private Integer urlMethod;

    @ApiModelProperty(value = "支持会员组数")
    private Integer mbrGroupNum;
    
    @ApiModelProperty(value = "支付平台名称")
    private String paymentName;
    
    @ApiModelProperty(value = "支持银行数")
    private Integer bankNum;
    
    @ApiModelProperty(value = "支持银行")
    private String bankName;
    
    @ApiModelProperty(value = "是否启用 1启用 2禁言 查询使用")
    private String isEnables;
    
    @ApiModelProperty(value = "插入会员组关联关系")
    private String mbrGroups;
    
    @ApiModelProperty(value = "支持银行集合")
    private Integer[] banks;
    
    private String bankOptions;
    private String selectedGroup;
    private BigDecimal depositAmount;
    private BigDecimal depositAmountDaily;
    private Integer payCount;
}
