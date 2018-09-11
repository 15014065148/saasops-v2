package com.eveb.saasops.modules.operate.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Setter
@Getter
@ApiModel(value = "TGameLogo", description = "个性图和LOGO")
@Table(name = "t_game_logo")
public class TGameLogo implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "模块Id(1,电子分类，2真人，3，彩票)")
    private Integer catId;

    @ApiModelProperty(value = "模块对应Id")
    private Integer depotId;

    @ApiModelProperty(value = "PC LOGO")
    private String picUrl;

    @ApiModelProperty(value = "H5  LOGO")
    private String mbPicUrl;

    @ApiModelProperty(value = "APP LOGO")
    private String appPicUrl;

    @ApiModelProperty(value = "PC LOGO")
    private String logoPc;

    @ApiModelProperty(value = "APP LOGO")
    private String logoApp;

    @ApiModelProperty(value = "MB LOGO")
    private String logoMb;

    @ApiModelProperty(value = "详细备注")
    private String memo;

    @ApiModelProperty(value = "排序")
    private Integer sortId;

    @ApiModelProperty(value = "开启PC端 1->开启，0－>禁用")
    private Integer enablePc;

    @ApiModelProperty(value = "开启移动端 1->开启，0－>禁用")
    private Integer enableMb;

    @ApiModelProperty(value = "开启App 1->开启，0－>禁用")
    private Integer enableApp;

    @ApiModelProperty(value = "定义描述")
    private String gameTag;

    @ApiModelProperty(value = "定义标题")
    private String titleTag;

    @Transient
    @ApiModelProperty(value = "批量删除ids")
    private int[] ids;

    @Transient
    @ApiModelProperty(value = "标签")
    private Integer[] labels;

    @Transient
    @ApiModelProperty(value = "分类统计游戏总数")
    private String gameCount;

    @Transient
    @ApiModelProperty(value = "游戏平台名称")
    private String depotName;

    @Transient
    @ApiModelProperty(value = "分类名称")
    private String catName;

    @Transient
    @ApiModelProperty(value = "游戏平台名称 查询使用")
    private String depotIds;

    @Transient
    @ApiModelProperty(value = "开启PC端 1->开启，0－>禁用")
    private Integer enableDepotPc;

    @Transient
    @ApiModelProperty(value = "开启移动端 1->开启，0－>禁用")
    private Integer enableDepotMb;

    @Transient
    @ApiModelProperty(value = "开启APP 1->开启，0－>禁用")
    private Integer enableDepotApp;

    @Transient
    @ApiModelProperty(value = "详细备注")
    private String memoDepot;

    @Transient
    @ApiModelProperty(value = "排序")
    private Integer sortIdDepot;


}