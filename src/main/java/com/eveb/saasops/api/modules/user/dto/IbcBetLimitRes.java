package com.eveb.saasops.api.modules.user.dto;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IbcBetLimitRes {
	private String error_code;
	private String message;
	private ArrayList<IbcBetLimitData> Data;
}
