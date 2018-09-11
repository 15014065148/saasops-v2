package com.eveb.saasops.api.modules.user.dto;

import org.springframework.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MgTokenDto {
	
	private String j_username;
	private String j_password;
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		if (!StringUtils.isEmpty(j_username))
			buffer.append("&j_username=").append(j_username);
		if (!StringUtils.isEmpty(j_password))
			buffer.append("&j_password=").append(j_password);
			buffer.replace(0,1,"?");
		return buffer.toString();
	}
}
