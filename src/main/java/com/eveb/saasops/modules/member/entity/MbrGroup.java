package com.eveb.saasops.modules.member.entity;

import com.eveb.saasops.modules.base.entity.BaseAuth;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@ApiModel(value = "MbrGroup", description = "")
@Table(name = "mbr_group")
public class MbrGroup implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @Transient
    @ApiModelProperty(value = "会员ID,List")
    private Long ids[];

    @ApiModelProperty(value = "会员组名")
    private String groupName;

	/*@ApiModelProperty(value = "自动升级条件-总存款")
	private BigDecimal totalDeposit;

	@ApiModelProperty(value = "自动升级条件-总有效投注")
	private BigDecimal totalBet;

	@ApiModelProperty(value = "升级条件(0,总存款，1总投注)")
	private Byte expandWay;*/

    @ApiModelProperty(value = "备注")
    private String memo;

    @ApiModelProperty(value = "1开启，0禁用")
    private Byte available;

    @ApiModelProperty(value = "0不是默认，1默认")
    @Transient
    private Byte isDef;

    // 当前组会员数
    @Transient
    private Integer mbrNums;

    // 会司入款最低限额
    @Transient
    private BigDecimal lowQuota;

    // 公司入款最高限额
    @Transient
    private BigDecimal topQuota;

    // 存款手续费
    @Transient
    private String depositFee;

    // 存款限免时间
    @Transient
    private Integer feeHours;

    // 存款限免次数
    @Transient
    private Integer feeTimes;

    // 每日取款限额
    @Transient
    private BigDecimal withDrawalQuota;

    // 单笔取款最低限额
    @Transient
    private BigDecimal wLowQuota;

    // 单笔取款最高限额
    @Transient
    private BigDecimal wTopQuota;

    // 取款手续费
    @Transient
    private String withdrawalFee;

    // 单笔取款手续费 时限/小时
    @Transient
    private Integer wfeeHours;

    // 单笔取款限免次数
    @Transient
    private Integer wfeeTimes;

    // 存款详细设置Id
    @Transient
    private Integer did;

    // 取款详细设置Id
    @Transient
    private Integer wid;
    //是否完成(1完成，0未完成)
    @Transient
    private Integer groupDone;
    // 组Id
    @Transient
    private Integer groupId;
    @Transient
    private String groupIds;
    @Transient
    private BaseAuth baseAuth;


    @Transient
    @ApiModelProperty(value = "会员组 查询使用")
    private String groupIdList;

    @Transient
    @ApiModelProperty(value = "1开启，0禁用 查询使用")
    private String availableList;


}