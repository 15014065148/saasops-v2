package com.eveb.saasops.modules.agent.entity;

import com.eveb.saasops.modules.base.entity.BaseAuth;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@Table(name = "agy_account")
public class AgentAccount {

    public static final String AGY_TYPE_EXTERNAL = "外部返佣";
    public static final String AGY_TYPE_INTERIOR = "内部统计";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "Id")
    private Integer id;


    @ApiModelProperty(value = "代理账号")
    private String agyAccount;

    @ApiModelProperty(value = "代理密码")
    private String agyPwd;

    @ApiModelProperty(value = "代理类型(0-外部返佣,1-内部统计)")
    private Byte agyType;

    @ApiModelProperty(value = "真实名称")
    private String realName;

    @ApiModelProperty(value = "手机号码")
    private String mobile;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "备注")
    private String memo;

    @ApiModelProperty(value = "1开启，0禁用")
    private Byte available;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "最后一次修改人的账号")
    private String modifyUser;

    @ApiModelProperty(value = "最后一次修改时间")
    private String modifyTime;

    @ApiModelProperty(value = "性别 0 男 1 女 2其他")
    private Integer sex;

    @ApiModelProperty(value = "上级ID 没有上级为0")
    private Integer parentId;

    @ApiModelProperty(value = "IP")
    private String ip;
    
    @ApiModelProperty(value="代理推广代码必须唯一")
    private String spreadCode;
    
    @Transient
    @ApiModelProperty(value = "会员人数")
    public Integer accountNum;

    @Transient
    @ApiModelProperty(value = "总代账户")
    private String agyTopAccount;

    @Transient
    @ApiModelProperty(value = "佣金ID")
    private List<Integer> commonIds;

    @Transient
    private Integer commonId;

    @Transient
    @ApiModelProperty(value = "佣金方案集合")
    private List<Commission> commissions;

    @Transient
    @ApiModelProperty(value = "佣金方案名称")
    private String commnName;

    @Transient
    @ApiModelProperty(value = "开始时间开始")
    private String createTimeFrom;

    @Transient
    @ApiModelProperty(value = "开始时间结束")
    private String createTimeTo;

    @Transient
    @ApiModelProperty(value = "余额")
    private BigDecimal balance;

    @Transient
    @ApiModelProperty(value = "返佣总额")
    private BigDecimal commissionBalance;

    @Transient
    @ApiModelProperty(value = "存款玩家数")
    private Integer playerNumber;

    @Transient
    @ApiModelProperty(value = "存款总额")
    private BigDecimal depositBalance;

    @Transient
    @ApiModelProperty(value = "取款总额")
    private BigDecimal withdrawalBalance;

    @Transient
    private BaseAuth baseAuth;

    @Transient
    private String parentIds;
}