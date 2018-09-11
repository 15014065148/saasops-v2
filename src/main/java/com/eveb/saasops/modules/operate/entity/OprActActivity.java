package com.eveb.saasops.modules.operate.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "OprActActivity", description = "活动设置")
@Table(name = "opr_act_activity")
public class OprActActivity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "活动模板(对应t_op_acttmpl中的Id)")
    private Integer actTmplId;

    @ApiModelProperty(value = "活动名称")
    private String activityName;

    @ApiModelProperty(value = "活动分类Id 对应opr_act_cat中Id")
    private Integer actCatId;

    @ApiModelProperty(value = "展示开始日期")
    private String showStart;

    @ApiModelProperty(value = "展示结束日期")
    private String showEnd;

    @ApiModelProperty(value = "生效开始日期")
    private String useStart;

    @ApiModelProperty(value = "生效结束日期")
    private String useEnd;

    @ApiModelProperty(value = "0未开始 、1进行中、2已失效")
    private Integer useState;

    @ApiModelProperty(value = "前端展示 1展示 0不展示")
    private Boolean isShow;

    @ApiModelProperty(value = "开启PC端 1开启 0禁用")
    private Boolean enablePc;

    @ApiModelProperty(value = "开启手机端 1开启 0禁用")
    private Boolean enableMb;

    @ApiModelProperty(value = "PC端活动图片")
    private String pcLogoUrl;

    @ApiModelProperty(value = "pc分组文件名")
    private String pcGroupName;

    @ApiModelProperty(value = "pc file Name")
    private String pcRemoteFileName;

    @ApiModelProperty(value = "手机端活动图片")
    private String mbLogoUrl;

    @ApiModelProperty(value = "手机分组文件名")
    private String mbGroupName;

    @ApiModelProperty(value = "手机 file Name")
    private String mbRemoteFileName;

    @ApiModelProperty(value = "活动内容")
    private String content;

    @ApiModelProperty(value = "建立时间")
    private String createTime;

    @ApiModelProperty(value = "createUser")
    private String createUser;

    @ApiModelProperty(value = "modifyUser")
    private String modifyUser;

    @ApiModelProperty(value = "modifyTime")
    private String modifyTime;

    @ApiModelProperty(value = "1开启，0禁用")
    private Byte available;

    @ApiModelProperty(value = "模板名称")
    @Transient
    private String tmplName;

    @ApiModelProperty(value = "活动分类描述")
    @Transient
    private String tmplNameTag;

    @ApiModelProperty(value = "分类名称")
    @Transient
    private String catName;

    @ApiModelProperty(value = "申请未审核数")
    @Transient
    private Integer applyNum;

    @ApiModelProperty(value = "规则字符串")
    @Transient
    private String rule;

    @ApiModelProperty(value = "tmplCode")
    @Transient
    private String tmplCode;

    @ApiModelProperty(value = "路径")
    @Transient
    private String fastDfsUrl;

    @Transient
    private Integer ruleId;
    /*注册送、救援金、有效投注 首存送、存就送  显示 立即存款 显示 立即申请 1
    推荐送、红包、签到 显示  立即领取 3
    返水优惠、活动内容 不显示 0 */

    @ApiModelProperty(value = "前端是否需要显示可能申请活动的按钮(0,1,3)")
    @Transient
    private Byte buttonShow;
    @ApiModelProperty(value = "最小存款金额")
    @Transient
    private BigDecimal amountMin;
    /**
     * 查询使用
     **/
    @Transient
    private String actCatIdList;
    @Transient
    private String actTmplIdList;
    @Transient
    private String useStateList;
}