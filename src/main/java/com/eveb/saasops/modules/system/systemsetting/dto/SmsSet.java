package com.eveb.saasops.modules.system.systemsetting.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SmsSet {
	// 短信网关地址
	private String  smsGetwayAddress;
	// 短信接口用户名
	private String  smsInterfaceName;
	// 短信接口密码
	private String  smsInterfacePassword;
	// 短信发送方名称
	private String  smsSendName;
	// 短信模板	
	private String  smsTemplate;
	// 短信测试号码
	private String mobile;

}
