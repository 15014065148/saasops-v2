package com.eveb.saasops.modules.operate.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Table;

@Setter
@Getter
@ApiModel(value = "t_opt_adv_banner", description = "")
@Table(name = "t_opt_adv_banner")
public class AdvBanner {
    /**
     * 模板Id, 1-12值
      */
    private Integer evebNum;
    /**
     * 展示客户端（0：pc，1：移动，2：移动pc均显示）
     */
    private Integer clientShow;
    /**
     * 广告类型: 1：首页，2：真人，3：电子，4：体育，5：彩票，6： 手机
     */
    private Integer advType;

    /**
     * 图片跳转目标，0：站内；1：站外
     */
    private Integer picTarget;

    /**
     * 活动分类
     */
    private Integer actId;

    /**
     * 活动
     */
    private Integer activityId;

    /**
     * 站外路径
     */
    private String outStation;
    /**
     * 广告路径
     */
    private String picPcPath;

    /**
     * 移动广告路径
     */
    private String picMbPath;
}
