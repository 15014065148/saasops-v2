package com.eveb.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MgLaunchGmDto {
	private String timestamp;
	private String apiusername;
	private String apipassword;
	private String token;
	private String language;
	private String gameId;
	private String bankingUrl;
	private String lobbyUrl;
	private String logoutRedirectUrl;
	private String demoMode;
	private String titanium;
	
	public String toString() {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("<mbrapi-launchurl-call");
		sBuffer.append(" timestamp=\"" + timestamp + "\"");
		sBuffer.append(" apiusername=\"" + apiusername + "\"");
		sBuffer.append(" apipassword=\"" + apipassword + "\"");
		
		sBuffer.append(" token=\""+token+"\"");
		sBuffer.append(" language=\"" + language + "\"");
		sBuffer.append(" gameId=\"" + gameId + "\"");
		sBuffer.append(" bankingUrl=\"" + bankingUrl + "\"");
		sBuffer.append(" lobbyUrl=\"" + lobbyUrl + "\"");
		
		sBuffer.append(" logoutRedirectUrl=\"" + logoutRedirectUrl + "\"");
		sBuffer.append(" demoMode=\"" + demoMode + "\"");
		sBuffer.append(" titanium=\"" + titanium + "\"");
		sBuffer.append("/>");
		return sBuffer.toString();
	}
}
