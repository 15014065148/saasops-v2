package com.eveb.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Balance188Dto {
	private String loginName;
	public String toString() {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		sBuffer.append("<Request Method=\"GetMemberBalance\">");
		sBuffer.append(" <LoginName>"+loginName+"</LoginName>");
		sBuffer.append(" </Request>");
		return sBuffer.toString();
	}


}
