package com.eveb.saasops.api.modules.user.dto;

import java.util.Base64;
import com.eveb.saasops.common.exception.RRException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PngBase64Code {
	private String userName;
	private String passWd;

	public String toString() {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("Basic ");
		try {
			sBuffer.append(Base64.getEncoder().encodeToString((userName + ":" + passWd).getBytes("UTF-8")));
		} catch (Exception e) {
			throw new RRException("账密加密BASE64出错!");
		}
		return sBuffer.toString();
	}
}
