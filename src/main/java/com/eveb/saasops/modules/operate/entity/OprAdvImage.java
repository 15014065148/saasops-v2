package com.eveb.saasops.modules.operate.entity;

import com.eveb.saasops.modules.operate.dto.InStation;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;


@Setter
@Getter
@ApiModel(value = "opr_adv_image", description = "广告图片")
@Table(name = "opr_adv_image")
public class OprAdvImage implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "广告id")
    private Integer advId;

    @ApiModelProperty(value = "pc图片路径")
    private String pcPath;

    @ApiModelProperty(value = "移动图片路径")
    private String mbPath;

    @ApiModelProperty(value = "图片跳转目标，0：站内；1：站外")
    private Integer picTarget;

    @ApiModelProperty(value = "活动分类")
    private Integer actId;

    @ApiModelProperty(value = "活动")
    private Integer activityId;

    @ApiModelProperty(value = "站外路径")
    private String outStation;
}