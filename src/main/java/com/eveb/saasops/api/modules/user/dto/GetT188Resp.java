package com.eveb.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetT188Resp {
	private String returnCode;
	private String description;
	private String currencyCode;
	private String balance;
}