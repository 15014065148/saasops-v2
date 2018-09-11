package com.eveb.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsRes {
	private String code;// 是否成功 2为成功
	private String msg; // 反回文字信息
	private String smsid;// 成功反回消息Id
}
