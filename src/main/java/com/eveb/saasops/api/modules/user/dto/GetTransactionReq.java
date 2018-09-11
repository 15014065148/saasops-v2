package com.eveb.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetTransactionReq {
	private String brandId;
	private String brandPassword;
	private String transactionId;

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\"?>");
		sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
		sb.append("xmlns:sec=\"http://secondarywallet.connect.omega.com/\">");
		sb.append("<soapenv:Header/>");
		sb.append("<soapenv:Body>");
		sb.append("<sec:checkTransactionReq>");
		sb.append("<brandId>" + getBrandId() + "</brandId>");
		sb.append("<brandPassword>" + getBrandPassword() + "</brandPassword>");
		sb.append("<platformTranId>" + getTransactionId() + "</platformTranId>");
		sb.append("</sec:checkTransactionReq>");
		sb.append("</soapenv:Body>");
		sb.append("</soapenv:Evelope>");
		return sb.toString();
	}
}