package com.eveb.saasops.modules.system.cmpydeposit.entity;

import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.math.BigDecimal;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.eveb.saasops.modules.member.entity.MbrGroup;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

@Setter
@Getter
@ApiModel(value = "SysDeposit", description = "")
@Table(name = "set_basic_sys_deposit")
public class SysDeposit implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "编号")
    private String code;

    @ApiModelProperty(value = "状态: 0:禁用，1：启用")
    private Integer status;

    @ApiModelProperty(value = "账户类型: 0:银行账户，1：第三方支付")
    private Integer bankType;

    @ApiModelProperty(value = "开户账号")
    private String bankAccount;

    @ApiModelProperty(value = "开户姓名")
    private String realName;

    @ApiModelProperty(value = "存款渠道")
    private String depositType;

    @ApiModelProperty(value = "开户银行")
    private String bankName;

    @ApiModelProperty(value = "开户支行")
    private String bankBranch;

    @ApiModelProperty(value = "手续费上限金额CNY")
    private BigDecimal feeTop;

    @ApiModelProperty(value = "手续费 按比例收费")
    private BigDecimal feeScale;

    @ApiModelProperty(value = "固定收费")
    private BigDecimal feeFixed;

    @ApiModelProperty(value = "收费的方式(0-按比例收费 1固定收费)")
    private Integer feeWay;

    @ApiModelProperty(value = "每日最大存款金额")
    private BigDecimal dayMaxAmt;

    @Transient
    @ApiModelProperty(value = "单日已存款金额")
    private BigDecimal dayDepAmt;

    @ApiModelProperty(value = "会员组id")
    @Transient
    private Integer mbrGrpId;

    @ApiModelProperty(value = "所属会员组id集合")
    @Transient
    private List<Integer> mbrGrdIds;

    @ApiModelProperty(value = "会员组数量")
    @Transient
    private Integer mbrGrdNum;

    @ApiModelProperty(value = "银行图片")
    @Transient
    private String bankLog;

    @ApiModelProperty(value = "排序号")
    private Integer sortId;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新人")
    private String modifyUser;

    @ApiModelProperty(value = "更新时间")
    private Date modifyTime;

    @ApiModelProperty(value = "设备来源；PC:0 H5:3 ")
    private String devSource;

    @ApiModelProperty(value = "渠道")
    @Transient
    private String dType;

    @ApiModelProperty(value = "单日已存款次数")
    @Transient
    private Integer depositTimes;

    @ApiModelProperty(value = "组ids")
    @Transient
    private List<Integer> grpIds;

    @ApiModelProperty(value = "选中会员组")
    @Transient
    private List<MbrGroup> checkedGroup;

    @Transient
    @ApiModelProperty(value = "最低限额 CNY")
    private BigDecimal lowQuota;

    @Transient
    @ApiModelProperty(value = "最高限额 CNY")
    private BigDecimal topQuota;

    @Transient
    @ApiModelProperty(value = "合计入款")
    private BigDecimal depositAmount;
    @Transient
    @ApiModelProperty(value = "账户复选框")
    private List<Integer> bankTypes;
    @Transient
    @ApiModelProperty(value = "存款渠道")
    private List<String> depositTypeList;

    @Transient
    @ApiModelProperty(value = "开户银行")
    private List<String> bankNameList;

    @Transient
    @ApiModelProperty(value = "渠道复选框")
    private List<String> dTypes;
    @Transient
    @ApiModelProperty(value = "支付渠道")
    private String payName;

    //前端拼接使用
    @Transient
    @ApiModelProperty(value = "前端拼接使用")
    private String paymentName="";

}