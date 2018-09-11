package com.eveb.saasops.modules.operate.entity;

import java.io.Serializable;

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
@ApiModel(value = "根据游戏类别找出对应那些平台有这个类别的游戏", description = "")
@Table(name = "t_gm_depotcat")
public class TGmDepotcat implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "平台Id")
    private Integer depotId;

    @ApiModelProperty(value = "类别Id")
    private Integer catId;

    @ApiModelProperty(value = "类别名称")
    @Transient
    private String catName;

    @ApiModelProperty(value = "平台名称")
    @Transient
    private String depotName;

    @ApiModelProperty(value = "游戏表logo")
    @Transient
    private String logo2;

    @ApiModelProperty(value = "游戏表个性图")
    @Transient
    private String logo;

    @ApiModelProperty(value = "PC个性图")
    @Transient
    private String picUrl;

    @ApiModelProperty(value = "PC logo")
    @Transient
    private String logoPc;

    @ApiModelProperty(value = "手机个性图")
    @Transient
    private String mbPicUrl;

    @ApiModelProperty(value = "手机 logo")
    @Transient
    private String logoMb;

    @ApiModelProperty(value = "APP个性图")
    @Transient
    private String appPicUrl;

    @ApiModelProperty(value = "APP logo")
    @Transient
    private String logoApp;

    @ApiModelProperty(value = "游戏文字说明")
    @Transient
    private String gameTag;

    @ApiModelProperty(value = "排序号")
    @Transient
    private String sortId;

    @ApiModelProperty(value = "体育未有登陆 URL链接")
    @Transient
    private String pcUrlTag;
}