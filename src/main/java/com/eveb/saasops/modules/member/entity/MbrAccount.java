package com.eveb.saasops.modules.member.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.eveb.saasops.modules.base.entity.BaseAuth;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ApiModel(value = "MbrAccount", description = "")
@Table(name = "mbr_account")
@ToString
public class MbrAccount implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;
    //批量操作的时候
    @Transient
    private Integer Ids[];
    @ApiModelProperty(value = "会员账号")
    private String loginName;
    //@JsonIgnore
    @ApiModelProperty(value = "会员密码")
    private String loginPwd;
    //@JsonIgnore
    @ApiModelProperty(value = "资金密码")
    private String securePwd;

    @ApiModelProperty(value = "会员组")
    private Integer groupId;

    @ApiModelProperty(value = "总代 top 冗余字段")
    private Integer tagencyId;

    @ApiModelProperty(value = "直属代理 direct")
    private Integer cagencyId;

    @ApiModelProperty(value = "真实名称")
    private String realName;

    @ApiModelProperty(value = "联系电话号码")
    private String mobile;

    @ApiModelProperty(value = "备注")
    private String memo;

    @ApiModelProperty(value = "是否黑名称(1,是，0否)")
    private Byte isLock;

    @ApiModelProperty(value = "1开启，0禁用,2余额冻结")
    private Byte available;

    @ApiModelProperty(value = "最后登录时间")
    private String loginTime;

    @Transient
    @ApiModelProperty(value = "最后登录ip")
    private String loginIp;

    @ApiModelProperty(value = "")
    private String registerTime;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "1在线，0离线")
    private Byte isOnline;

    @ApiModelProperty(value = "是否验证手机(1验证，0未验证)")
    private Byte isVerifyMoblie;

    @ApiModelProperty(value = "是否验证邮箱(1验证，0未验证)")
    private Byte isVerifyEmail;

    @ApiModelProperty(value = "手机否接收消息(1允许，0不允许)")
    private Byte isAllowMsg;

    @ApiModelProperty(value = "邮箱否接收消息(1允许，0不允许)")
    private Byte isAllowEmail;

    @ApiModelProperty(value = "QQ号码")
    private String qq;

    @ApiModelProperty(value = "微信号码")
    private String weChat;

    @ApiModelProperty(value = "地区ID")
    private Integer areaId;

    @ApiModelProperty(value = "")
    private String address;

    @ApiModelProperty(value = "免转钱包开关 :0 关  1 开")
    private Integer freeWalletSwitch;

    @JsonIgnore
    @ApiModelProperty(value = "加密专用")
    private String salt;
    /**
     * 注册IP
     */
    @Transient
    private String registerIp;
    /**
     * 注册来源url
     */
    @Transient
    private String registerUrl;
    /**
     * 登陆来源 登陆来源(0 PC,3 H5)
     */
    private Byte loginSource;
    /**
     * 注册来源 注册来源(0 PC,3 H5)
     */
    @Transient
    private Byte registerSource;
    /**
     * 钱包余额
     */
    @Transient
    private BigDecimal balance;
    /**
     * 总金额
     */
    @Transient
    private BigDecimal totalBalance;
    /**
     * 总存款
     */
    @Transient
    private BigDecimal totalDeposit;
    /**
     * 总取款
     */
    @Transient
    private BigDecimal totalWithdrawal;
    /**
     * 代理账号
     */
    @Transient
    private String agyAccount;

    /**
     * 总代账号
     */
    @Transient
    private String tagyAccount;
    /**
     * 最后登录时间结束 表 : mbr_account 对应字段 : loginTimeEnd
     */
    @Transient
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String loginTimeEnd;

    /**
     * 注册时间结束 表 : mbr_account 对应字段 : registerTimeEnd
     */

    @Transient
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String registerTimeEnd;
    /**
     * 会员组名称
     */
    @Transient
    private String groupName;

    @Transient
    private BaseAuth baseAuth;
    @Transient
    private Set<String> columnSets;

    //会员组ids
    @Transient
    private List<Integer> groupIds;
    //总代组ids
    @Transient
    private List<Integer> tagencyIds;
    //代理组ids
    @Transient
    private List<Integer> cagencyIds;

    @ApiModelProperty(value = "代理推广代码,可选")
    @Transient
    private String spreadCode;
    @ApiModelProperty(value = "验证码")
    @Transient
    private String captchareg;
    @ApiModelProperty(value = "站点前缀")

    @Transient
    private String websiteTitle;

    @ApiModelProperty(value = "站点名称")
    @Transient
    private String siteFore;

    @ApiModelProperty(value = "首存时间")
    @Transient
    private String depositTime;

    // 0禁用,1开启 ,2余额冻结
    public interface Status {
        int DISABLED = 0;
        int VALID = 1;
        int LOCKED = 2;
    }

    @Transient
    @ApiModelProperty(value = "注册来源：0 PC，3 H5")
    private String registerSourceList;

    @Transient
    @ApiModelProperty(value = "登陆来源：0 PC，3 H5")
    private String loginSourceList;

    @Transient
    @ApiModelProperty(value = "总代 查询使用")
    private String tagencyIdList;

    @Transient
    @ApiModelProperty(value = "直属代理 查询使用")
    private String cagencyIdList;

    @Transient
    @ApiModelProperty(value = "会员组 查询使用")
    private List<String> groupIdList;

    @Transient
    @ApiModelProperty(value = "状态 查询使用")
    private String availableList;

    @Transient
    @ApiModelProperty(value = "1在线，0离线 查询使用")
    private String isOnlineList;
}