package com.eveb.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MgCreationDto {
	private String crId;
	private String crType;
	private String neId;
	private String neType;
	private String tarType;
	private String username;
	private String name;
	private String password;
	private String confirmPassword;
	private String currency;
	private String language;
	private String email;
	private String mobile;
	private Casino casino;
	private Poker poker;
}
