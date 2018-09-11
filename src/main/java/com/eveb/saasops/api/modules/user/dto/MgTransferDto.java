package com.eveb.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MgTransferDto {
	private String timestamp;
	private String apiusername;
	private String apipassword;
	private String token;
	private String product;
	private String operation;
	private String amount;
	private String txId;

	public String toString() {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("<mbrapi-changecredit-call");
		sBuffer.append(" timestamp=\"" + timestamp + "\"");
		sBuffer.append(" apiusername=\"" + apiusername + "\"");
		sBuffer.append(" apipassword=\"" + apipassword + "\"");
		sBuffer.append(" token=\"" + token + "\"");
		sBuffer.append(" product=\"" + product + "\"");
		sBuffer.append(" operation=\"" + operation + "\"");
		sBuffer.append(" amount=\"" + amount + "\"");
		sBuffer.append(" tx-id=\"" + txId + "\"");
		sBuffer.append("/>");
		return sBuffer.toString();
	}
}
