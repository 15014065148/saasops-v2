package com.eveb.saasops.modules.operate.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiModel(value = "JWaterRebatesDto", description = "返水优惠")
public class JWaterRebatesDto {

    @ApiModelProperty(value = "活动范围")
    private ActivityScopeDto scopeDto;

    @ApiModelProperty(value = "有效投注流水范围 0输 1赢 2全部")
    private Integer scope;

    @ApiModelProperty(value = "是否审核 true是 false 否")
    private Boolean isAudit;

    @ApiModelProperty(value = "已填写真实姓名 true是 false 否")
    private Boolean isName;

    @ApiModelProperty(value = "已绑定银行卡 true是 false 否")
    private Boolean isBank;

    @ApiModelProperty(value = "已验证手机 true是 false 否")
    private Boolean isMobile;

    @ApiModelProperty(value = "已验证邮箱 true是 false 否")
    private Boolean isMail;

    @ApiModelProperty(value = "可领取类型 0每日 1每周 2近7日 3自定义")
    private Integer drawType;

    @ApiModelProperty(value = "可领取次数")
    private Integer drawNumber;

    @ApiModelProperty(value = "平台活动规则范围")
    private List<AuditCat> ruleDtos;

    @ApiModelProperty(value = "活动规则")
    private List<WaterRebatesRuleListDto> ruleListDtos;
}
