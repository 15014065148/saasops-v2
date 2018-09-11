package com.eveb.saasops.modules.fund.entity;

import com.eveb.saasops.modules.base.entity.BaseAuth;
import com.eveb.saasops.modules.member.entity.MbrBankcard;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;


@Setter
@Getter
@ApiModel(value = "FundAccWithdraw", description = "FundAccWithdraw")
@Table(name = "fund_acc_withdraw")
public class AccWithdraw implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "订单前缀")
    private String orderPrefix;

    @ApiModelProperty(value = "会员Id")
    private Integer accountId;

    @ApiModelProperty(value = "提款状态(0 拒绝 1 通过 2待处理 3 出款中 4自动出款人工审核 5自动出款中)")
    private Integer status;

    @ApiModelProperty(value = "0 手动出款 1自动出款 3处理中")
    private Integer type;

    @ApiModelProperty(value = "提款金额")
    private BigDecimal drawingAmount;

    @ApiModelProperty(value = "转账手续费")
    private BigDecimal handlingCharge;

    @ApiModelProperty(value = "行政扣款")
    private BigDecimal cutAmount;

    @ApiModelProperty(value = "扣除优惠")
    private BigDecimal discountAmount;

    @ApiModelProperty(value = "实际出款")
    private BigDecimal actualArrival;

    @ApiModelProperty(value = "审核人")
    private String auditUser;

    @ApiModelProperty(value = "审核时间")
    private String auditTime;

    @ApiModelProperty(value = "出款人")
    private String passUser;

    @ApiModelProperty(value = "出款时间")
    private String passTime;

    @ApiModelProperty(value = "ip")
    private String ip;

    @ApiModelProperty(value = "")
    private String memo;

    @ApiModelProperty(value = "")
    private String createUser;

    @ApiModelProperty(value = "")
    private String createTime;

    @ApiModelProperty(value = "")
    private String modifyUser;

    @ApiModelProperty(value = "")
    private String modifyTime;

    @ApiModelProperty(value = "转账记录ID")
    private Integer billDetailId;

    @ApiModelProperty(value = "取款申请来源：0 PC，3 H5")
    private Byte withdrawSource;
    
    @Transient
    @ApiModelProperty(value = "出款完成之后会员姓名")
    private String realName;
    
    @ApiModelProperty(value = "会员提款银行卡信息(bankcardId)")
    private Integer bankCardId;

    @Transient
    @ApiModelProperty(value = "会员组ID")
    private Integer groupId;

    @Transient
    @ApiModelProperty(value = "会员组名字")
    private String groupName;

    @Transient
    @ApiModelProperty(value = "会员名")
    private String loginName;

    @Transient
    @ApiModelProperty(value = "直属代理")
    private String agyAccount;

    @Transient
    @ApiModelProperty(value = "总代理")
    private String topAgyAccount;

    @Transient
    private Integer agyTopAccountId;

    @Transient
    private Integer agyAccountId;

    @Transient
    @ApiModelProperty(value = "申请时间开始")
    private String createTimeFrom;

    @Transient
    @ApiModelProperty(value = "申请时间结束")
    private String createTimeTo;

    @Transient
    @ApiModelProperty(value = "提款信息")
    private MbrBankcard mbrBankcard;

    @Transient
    @ApiModelProperty(value = "提款次数")
    private Integer withdrawCount;

    @Transient
    @ApiModelProperty(value = "登录用户的名字")
    private String loginSysUserName;

    @Transient
    @ApiModelProperty(value = "提款银行")
    private String bankName;

    @Transient
    @ApiModelProperty(value = "分行信息地址")
    private String address;

    @Transient
    private Integer notStatus;

    @Transient
    private BaseAuth baseAuth;

    @Transient
    private Integer merchantId;

    @Transient
    private String transId;

    @Transient
    private String orderId;

    @Transient
    private Integer merchantDetailId;

    public interface Status {
        byte rejective=0;//拒绝
        byte suc = 1;//取款成功
        byte apply = 2;//待处理
        byte process = 3;//出款中
    }
    
    @Transient
    private List<Integer> topAgyAccounts;
    
    @Transient
    private List<Integer> agyAccountIds;
    
    @Transient
    private List<Integer> groupIds;
    
    @Transient
    private List<Integer> statuss;
    
    @Transient
    @ApiModelProperty(value = "取款申请来源：0 PC，3 H5")
    private String withdrawSourceList;
}