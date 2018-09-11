package com.eveb.saasops.modules.member.entity;

import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.*;


@Setter
@Getter
@ApiModel(value = "MbrAuditAccount", description = "稽核表")
@Table(name = "mbr_audit_account")
public class MbrAuditAccount implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "会员名")
    private String loginName;

    @ApiModelProperty(value = "会员ID")
    private Integer accountId;

    @ApiModelProperty(value = "时间")
    private String time;

    @ApiModelProperty(value = "有效投注额")
    private BigDecimal validBet;

    @ApiModelProperty(value = "存款金额")
    private BigDecimal depositAmount;

    @ApiModelProperty(value = "稽核点流水要求")
    private BigDecimal depositAudit;

    @ApiModelProperty(value = "存款输光余额")
    private BigDecimal depositOutBalance;

    @ApiModelProperty(value = "存款稽核")
    private BigDecimal auditAmount;

    @ApiModelProperty(value = "存款时余额 -派彩")
    private BigDecimal depositBalance;

    @ApiModelProperty(value = "累计剩余有效投注额")
    private BigDecimal remainValidBet;

    @ApiModelProperty(value = "0 不通过 1通过")
    private Integer status;

    @ApiModelProperty(value = "是否提款 0否 1是 2提款中")
    private Integer isDrawings;

    @ApiModelProperty(value = "是输光 0否 1是")
    private Integer isOut;

    @ApiModelProperty(value = "存款，充值ID")
    private Integer depositId;

    @ApiModelProperty(value = "memo")
    private String memo;

    @ApiModelProperty(value = "")
    private String modifyUser;

    @ApiModelProperty(value = "")
    private String modifyTime;

    @ApiModelProperty(value = "派彩")
    private BigDecimal payOut;

    @ApiModelProperty(value = "转账使用存款 方便显示")
    private BigDecimal discardAmount;

    @ApiModelProperty(value = "优惠溢出有效投注")
    private BigDecimal bonusRemainValidBet;

    @Transient
    private Boolean sort;

    @ApiModelProperty(value = "所需流水")
    @Transient
    private BigDecimal waterRequired;
}