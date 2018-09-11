package com.eveb.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class IbcTransferRes {
	private String error_code;
	private String message;
	private IbcTransferDateRes Data;
}
