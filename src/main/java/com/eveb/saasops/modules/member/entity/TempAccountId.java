package com.eveb.saasops.modules.member.entity;

import com.eveb.saasops.modules.base.entity.BaseAuth;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Setter
@Getter
@ApiModel(value = "temp_AccountId", description = "")
@Table(name = "temp_AccountId")
public class TempAccountId implements Serializable {
	@ApiModelProperty(value = "会员ID")
	private Integer accountId;
	
	@ApiModelProperty(value = "UUid")
	private Long accUuid;
}