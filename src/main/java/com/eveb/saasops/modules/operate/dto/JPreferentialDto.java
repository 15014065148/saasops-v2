package com.eveb.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ApiModel(value = "JPreferentialDto", description = "首存送")
public class JPreferentialDto {

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

    @ApiModelProperty(value = "流水范围")
    private List<AuditCat> auditCats;

    @ApiModelProperty(value = "首存类型 0常规 1每日首存 2每周首存")
    private Integer depositType;

    @ApiModelProperty(value = "活动规则")
    private List<ActivityRuleDto> activityRuleDtos;

    @ApiModelProperty(value = "申请金额")
    private BigDecimal appliedAmount;
}
