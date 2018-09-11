package com.eveb.saasops.api.modules.user.dto;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IbcRes {
	private String error_code;
	private String message;
	private String sessionToken;
	private ArrayList<IbcBalanceRes> Data;
}
