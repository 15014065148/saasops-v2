package com.eveb.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MgLoginDto {
	private String timestamp;
	private String apiusername;
	private String apipassword;
	private String username;
	private String password;
	private String ipaddress;
	private String partnerId;
	private String currencyCode;

	public String toString() {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("<mbrapi-login-call");
		sBuffer.append(" timestamp=\"" + timestamp + "\"");
		sBuffer.append(" apiusername=\"" + apiusername + "\"");
		sBuffer.append(" apipassword=\"" + apipassword + "\"");
		sBuffer.append(" username=\"" + username + "\"");
		sBuffer.append(" password=\"" + password + "\"");
		sBuffer.append(" ipaddress=\"" + ipaddress + "\"");
		sBuffer.append(" partnerId=\"" + partnerId + "\"");
		sBuffer.append(" currencyCode=\"" + currencyCode + "\"");
		sBuffer.append("/>");
		return sBuffer.toString();
	}
}
