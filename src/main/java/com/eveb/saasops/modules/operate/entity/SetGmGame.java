package com.eveb.saasops.modules.operate.entity;

import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import javax.persistence.Id;
import javax.persistence.Table;


@Setter
@Getter
@ApiModel(value = "SetGmGame", description = "平台中间表")
@Table(name = "set_gm_game")
public class SetGmGame implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @ApiModelProperty(value = "gameLogo ID")
    private Integer gameLogoId;

    @ApiModelProperty(value = "平台ID")
    private Integer depotId;

    @ApiModelProperty(value = "开启PC端 1->开启，0－>禁用")
    private Integer enableDepotPc;

    @ApiModelProperty(value = "开启移动端 1->开启，0－>禁用")
    private Integer enableDepotMb;

    @ApiModelProperty(value = "开启APP 1->开启，0－>禁用")
    private Integer enableDepotApp;

    @ApiModelProperty(value = "排序")
    private Integer sortId;

    @ApiModelProperty(value = "备注")
    private String memo;

}