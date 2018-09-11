package com.eveb.saasops.modules.operate.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Transient;

@Getter
@Setter
@ApiModel(value = "前台游戏类别显示", description = "")
public class OprGame {
    @ApiModelProperty(value = "游戏Id")
    private Integer id;
    @ApiModelProperty(value = "平台Id")
    private Integer depotId;
    @ApiModelProperty(value = "游戏名称")
    private String gameName;
    @ApiModelProperty(value = "游戏图片url链接")
    private String logo;
    @ApiModelProperty(value = "游戏图片url链接")
    private String logo2;
    @ApiModelProperty(value = "人气值")
    private Integer clickNum;
    @ApiModelProperty(value = "推荐度")
    private Integer recRating;
    @ApiModelProperty(value = "平台名称")
    private String depotName;
    @ApiModelProperty(value = "游戏类型")
    private String catName;
    @ApiModelProperty(value = "奖金池1是，0否")
    private String enablePool;
    @ApiModelProperty(value = "试玩1是，0否")
    private String enableTest;
    @ApiModelProperty(value = "好评度")
    private Integer goodNum;
    @ApiModelProperty(value = "开启PC端 1->开启，0－>禁用")
    private Byte enablePc;
    @ApiModelProperty(value = "开启移动端 1->开启，0－>禁用")
    private Byte enableMb;
    @ApiModelProperty(value = "开启App 1->开启，0－>禁用")
    private Byte enableApp;
}
