package com.eveb.saasops.modules.operate.entity;

import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;


@Setter
@Getter
@ApiModel(value = "TOpActtmpl", description = "运营管理-活动模板")
@Table(name = "t_op_acttmpl")
public class TOpActtmpl implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String preferentialCode = "AQ0000001"; //首存送
    public static final String registerCode = "AQ0000002"; //注册送
    public static final String depositSentCode = "AQ0000003";//存就送
    public static final String rescueCode = "AQ0000004";//救援金
    public static final String waterRebatesCode= "AQ0000005";//返水优惠
    public static final String validCode= "AQ0000006";//有效投注
    public static final String recommendBonusCode= "AQ0000007";//推荐送
    public static final String signInCode= "AQ0000008";//签到
    public static final String redPacketCode= "AQ0000009";//红包
    public static final String contentCode= "AQ0000010";//活动内容

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "活动分类编号")
    private String tmplCode;

    @ApiModelProperty(value = "活动分类名称")
    private String tmplName;

    @ApiModelProperty(value = "活动分类描述")
    private String tmplNameTag;

    @ApiModelProperty(value = "活动分类状态　1开启，0禁用")
    private Byte available;

    @ApiModelProperty(value = "活动分类排序号")
    private Integer sortId;

    @ApiModelProperty(value = "备注")
    private String memo;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "最后一次修改人的账号")
    private String modifyUser;

    @ApiModelProperty(value = "最后一次修改时间")
    private String modifyTime;

}