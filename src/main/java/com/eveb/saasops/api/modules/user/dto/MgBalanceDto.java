package com.eveb.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MgBalanceDto {
	private String timestamp;
	private String apiusername;
	private String apipassword;
	private String token;

	public String toString() {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("<mbrapi-account-call");
		sBuffer.append(" timestamp=\"" + timestamp + "\"");
		sBuffer.append(" apiusername=\"" + apiusername + "\"");
		sBuffer.append(" apipassword=\"" + apipassword + "\"");
		sBuffer.append(" token=\"" + token + "\"");
		sBuffer.append("/>");
		return sBuffer.toString();
	}
}
