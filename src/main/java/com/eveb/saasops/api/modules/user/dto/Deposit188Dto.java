package com.eveb.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Deposit188Dto {
	private String loginName;
	private String amount;
	private String referenceNo;

	public String toString() {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		sBuffer.append("<Request Method=\"DepositFund\">");
		sBuffer.append(" <LoginName>" + loginName + "</LoginName>");
		sBuffer.append(" <Amount>" + amount + "</Amount>");
		sBuffer.append(" <ReferenceNo>" + referenceNo + "</ReferenceNo>");
		sBuffer.append(" </Request>");
		return sBuffer.toString();
	}

}
