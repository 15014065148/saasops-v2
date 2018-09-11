package com.eveb.saasops.modules.fund.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;


@Setter
@Getter
@ApiModel(value = "AgyBillDetail", description = "AgyBillDetail")
@Table(name = "agy_bill_detail")
public class AgyBillDetail implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "产生交易记录order")
    private Long orderNo;

    @ApiModelProperty(value = "代理登陆名称")
    private String loginName;

    @ApiModelProperty(value = "代理ID")
    private Integer accountId;

    @ApiModelProperty(value = "财务类别代码")
    private String financialCode;

    @ApiModelProperty(value = "操作金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "操作后余额")
    private BigDecimal afterBalance;

    @ApiModelProperty(value = "操作前的余额")
    private BigDecimal beforeBalance;

    @ApiModelProperty(value = "操作类型，0 支出1 收入")
    private Byte opType;

    @ApiModelProperty(value = "生成订单时间")
    private String orderTime;

    @ApiModelProperty(value = "备注")
    private String memo;

}