package com.eveb.saasops.modules.fund.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import com.eveb.saasops.common.utils.StringUtil;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "FundDeposit", description = "FundDeposit")
@Table(name = "fund_deposit")
public class FundDeposit implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "id")
	@NotNull
	private Integer id;

	@ApiModelProperty(value = "会员ID(mbr_account）")
	private Integer accountId;

	@ApiModelProperty(value = "公司入款设置ID(set_basic_sys_deposit 主键)")
	private Integer companyPayId;

	@ApiModelProperty(value = "收款账户（线上支付ID）")
	private Integer onlinePayId;

	@ApiModelProperty(value = "0 线上入款 ,1 公司入款")
	private Integer mark;

	@ApiModelProperty(value = "0 失败 1 成功 2待处理")
	private Integer status;

	@ApiModelProperty(value = "0 未支付 1 支付")
	private Boolean isPayment;

	@ApiModelProperty(value = "存款金额")
	private BigDecimal depositAmount;

	@ApiModelProperty(value = "存款人姓名")
	private String depositUser;

	@Transient
	@ApiModelProperty(value = "优惠金额")
	private BigDecimal discountAmount;

	@ApiModelProperty(value = "手续费")
	private BigDecimal handlingCharge;

	@ApiModelProperty(value = "实际到账")
	private BigDecimal actualArrival;

	@ApiModelProperty(value = "审核人")
	private String auditUser;

	@ApiModelProperty(value = "审核时间")
	private String auditTime;

	@ApiModelProperty(value = "ip")
	private String ip;

	@ApiModelProperty(value = "活动ID")
	private Integer activityId;

	@ApiModelProperty(value = "备注")
	private String memo;

	@ApiModelProperty(value = "")
	private String createUser;

	@ApiModelProperty(value = "")
	private String createTime;

	@ApiModelProperty(value = "")
	private String modifyUser;

	@ApiModelProperty(value = "")
	private String modifyTime;

	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "订单号")
	private String orderNo;

	@ApiModelProperty(value = "订单前缀")
	private String orderPrefix;

	@ApiModelProperty(value = "转账记录ID")
	private Integer billDetailId;

	@ApiModelProperty(value = "存款附言")
	private String depositPostscript;

	@ApiModelProperty(value = "手续费还返默认(为1 扣代理 ，为0 手续费已处理)")
	private Byte handingback;
	
	@ApiModelProperty(value = "存款来源：0 PC，3 H5")
	private Byte fundSource;
	
	@Transient
	@ApiModelProperty(value = "会员名")
	private String loginName;

	@Transient
	private String groupName;

	@Transient
	private String agyAccount;

	@Transient
	private Integer agyTopAccountId;

	@Transient
	private Integer agyAccountId;

	@Transient
	private Integer depositId;

	@Transient
	@ApiModelProperty(value = "开始时间 yyyy-MM-dd HH:mm:ss")
	private String createTimeFrom;

	@Transient
	@ApiModelProperty(value = "结束时间 yyyy-MM-dd HH:mm:ss")
	private String createTimeTo;

	@Transient
	@ApiModelProperty(value = "会员组ID")
	private Integer groupId;

	@Transient
	@ApiModelProperty(value = "支付次数")
	private int depositCount;

	@Transient
	@ApiModelProperty(value = "线上支付ID名称")
	private String onlinePayName;

	@Transient
	@ApiModelProperty(value = "公司入款类型")
	private String depositType;

	@Transient
	@ApiModelProperty(value = "是否提款 0否 1是")
	private Boolean isDrawings;

	@Transient
	@ApiModelProperty(value = "真实姓名")
	private String realName;

	@Transient
	private int isSign;

	@Transient
	@ApiModelProperty(value = "失败 成功  待处理")
	private String statusStr;

	@Transient
	@ApiModelProperty(value = "审核开始时间 yyyy-MM-dd HH:mm:ss")
	private String auditTimeFrom;

	@Transient
	@ApiModelProperty(value = "审核结束时间 yyyy-MM-dd HH:mm:ss")
	private String auditTimeTo;

	@Transient
	@ApiModelProperty(value = "登录用户的名字")
	private String loginSysUserName;

	public interface Mark {
		int onlinePay = 0;// 0 线上入款
		int offlinePay = 1;// 1 公司入款
	}

	public interface Status {
		int fail = 0;// 0 失败
		int suc = 1;// 1 成功
		int apply = 2;// 2待处理
	}

	public interface PaymentStatus {
		boolean unPay = false;
		boolean pay = true;
	}

	@Transient
	@ApiModelProperty(value = "总代 查询接口使用")
	private List<Integer> agyTopAccountIds;

	@Transient
	@ApiModelProperty(value = "会员ID(mbr_account） 查询接口使用")
	private List<Integer> accountIds;

	@Transient
	@ApiModelProperty(value = "存款渠道集合  查询接口使用")
	private List<Integer> companyPayIds;

	@Transient
	@ApiModelProperty(value = "代理方式集合  查询接口使用")
	private List<Integer> agyAccountIds;

	@Transient
	@ApiModelProperty(value = "会员组 查询接口使用")
	private List<Integer> groupIds;

	@Transient
	@ApiModelProperty(value = "公司入款类型 查询接口使用")
	private List<String> depositTypes;

	@Transient
	@ApiModelProperty(value = "状态 查询接口使用")
	private String statuss;

	@Transient
	@ApiModelProperty(value = "收款账户（线上支付ID） 查询接口使用")
	private List<Integer> onlinePayIds;

	@Transient
	@ApiModelProperty(value = "存款来源：0 PC，3 H5")
	private String fundSourceList;

	@Transient
	@ApiModelProperty(value = "支付机构：盘子支付。。。")
	private String paymentName;

	//收款渠道查询使用
	@Transient
	@ApiModelProperty(value = "0 线上入款 ,1 公司入款")
	private String markStr;

	//订单号查询使用
	@Transient
	@ApiModelProperty(value = "订单号模糊查询使用")
	private String orderNoStr;

	//收款渠道
	@Transient
	@ApiModelProperty(value = "收款渠道,给前端使用")
	private String payType;

    //充值记录类型返回
    @Transient
    @ApiModelProperty(value = "收款渠道,给前端使用")
    private String depositTypeName;

}