package com.eveb.saasops.common.constants;

//系统设置常量
public class SystemConstants {
	
	// 网站数据默认查询天数
	public static final String DEFAULT_QUERY_DAYS = "defaultQueryDays";
	// 会员数据查询天数
	public static final String MEMBER_QUERY_DAYS = "memberQueryDays";
	// 管理员密码过期天数
	public static final String PASSWORD_EXPIRE_DAYS = "passwordExpireDays";
	// 站点logo图片
	public static final String LOGO_PATH = "logoPath";
	// 页面Title图片
	public static final String TITLE_PATH = "titlePath";
	// 网站统计代码(PC版)
	public static final String WEBSITE_CODE_PC = "websiteCodePc";
	// 网站统计代码(移动版)
	public static final String WEBSITE_CODE_MB = "websiteCodeMb";	
	// 网站Title
	public static final String WEBSITE_TITLE = "websiteTitle";
	// 网站关键字
	public static final String WEBSITE_KEYWORDS = "websiteKeywords";
	// 网站描述
	public static final String WEBSITE_DESCRIPTION = "websiteDescription";
	// 客服配置代码(PC版)
	public static final String CONFIG_CODE_PC = "configCodePc";
	// 客服配置代码(PC版)
	public static final String CONFIG_CODE_MB = "configCodeMb";
	//自动删除已读站内信天数
	public static final String AUTO_DELETE_DAYS = "autoDeleteDays";
	
	// 邮件发送服务器
	public static final String MAIL_SEND_SERVER = "mailSendServer";
	// 邮件发送端口
	public static final String MAIL_SEND_PORT = "mailSendPort";
	// 邮件发送账号
	public static final String MAIL_SEND_ACCOUNT = "mailSendAccount";
	// 账号密码
	public static final String MAIL_PASSWORD = "mailPassword";
	// 是否使用SSL
	public static final String WETHER_SSL = "wetherSsl";
	// 字符集
	public static final String CHARACTER_SET = "characterSet";
	// 短信网关地址
	public static final String SMS_GETWAY_ADDRESS = "smsGetwayAddress";
	// 短信接口用户名
	public static final String SMS_INTERFACE_NAME = "smsInterfaceName";
	// 短信接口密码
	public static final String SMS_INTERFACE_PASSWORD = "smsInterfacePassword";
	// 短信发送方名称
	public static final String SMS_SEND_NAME = "smsSendName";
	// 短信模板	
	public static final String SMS_TEMPLATE = "smsTemplate";

	// 会员账号        0：无，1：默认，2：必填、默认
	public static final String MEMBER_ACCOUNT = "loginName";
	// 会员登录密码     0：无，1：默认，2：必填、默认
	public static final String MEMBER_LOGIN_PASSWORD = "loginPwd";
	// 会员重复密码    0：无，1：默认，2：必填、默认
	public static final String MEMBER_REPEATED_PASSWORD = "reLoginPwd";
	// 会员验证码      0：无，1：默认，2：必填、默认
	public static final String MEMBER_VERIFICATION_CODE = "captchareg";
	// 会员真实姓名    0：无，1：默认，2：必填、默认
	public static final String MEMBER_REAL_NAME = "realName";
	// 会员手机    0：无，1：默认，2：必填、默认
	public static final String MEMBER_TELPHONE = "mobile";
	// 会员邮箱    0：无，1：默认，2：必填、默认
	public static final String MEMBER_EMAIL = "email";
	// 会员QQ     0：无，1：默认，2：必填、默认
	public static final String MEMBER_QQ = "qq";
	// 会员微信     0：无，1：默认，2：必填、默认
	public static final String MEMBER_WECHAT = "weChat";
	// 会员地址     0：无，1：默认，2：必填、默认
	public static final String MEMBER_ADDRESS = "address";
	
	// 代理账号  0：无，1：默认，2：必填、默认
	public static final String AGENT_ACCOUNT = "agentLoginName";
	// 代理登录密码  0：无，1：默认，2：必填、默认
	public static final String AGENT_LOGIN_PASSWORD = "agentLoginPwd";
	// 代理重复密码   0：无，1：默认，2：必填、默认
	public static final String AGENT_REPEATED_PASSWORD = "agentReLoginPwd";
	// 代理验证码    0：无，1：默认，2：必填、默认
	public static final String AGENT_VERIFICATION_CODE = "agentCaptchareg";
	// 代理真实姓名    0：无，1：默认，2：必填、默认
	public static final String AGENT_REAL_NAME = "agentRealName";
	// 代理手机   0：无，1：默认，2：必填、默认
	public static final String AGENT_TELPHONE = "agentMobile";
	// 代理邮箱   0：无，1：默认，2：必填、默认
	public static final String AGENT_EMAIL = "agentEmail";
	// 代理QQ    0：无，1：默认，2：必填、默认
	public static final String AGENT_QQ = "agentQQ";
	// 代理微信      0：无，1：默认，2：必填、默认
	public static final String AGENT_WECHAT = "agentWechat";
	// 代理地址      0：无，1：默认，2：必填、默认
	public static final String AGENT_ADDRESS = "agentAddress";
			
	// 用户注册是否强制显示网站服务条款    0：否  ，1 是
	public static final String MEMBER_REGISTER_DISPLAY_TERMS_OF_WEBSITE = "memberDisplayTermsOfWebsite";
	
	// 代理注册是否强制显示网站服务条款    0：否  ，1 是
	public static final String AGENT_REGISTER_DISPLAY_TERMS_OF_WEBSITE = "agentDisplayTermsOfWebsite";

	//用户注册网站服务条款
	public static final String MEMBER_SERVICE_TERMS_OF_WEBSITE = "memberServiceTermsOfWebsite";
	
	//代理网站服务条款
	public static final String AGENT_SERVICE_TERMS_OF_WEBSITE = "agentServiceTermsOfWebsite";
	
	// 代理后台查询天数
	public static final String AGENT_QUERY_DAYS = "agentQueryDays";
	// 佣金生成日
	public static final String COMMISSION_GENERATED_DATE = "commissionGeneratedDate";
	// 允许代理绑定银行卡总数
	public static final String TOTAL_OF_AGENT_BINDED_CARDS = "TotalOfAgentBindedCards";
	// 允许代理绑定同银行
	public static final String TOTAL_OF_AGENT_BINDED_SAME_CARDS = "TotalOfAgentBindedSameCards";
	// 允许代理绑定不同名银行卡
	public static final String ALLOW_DIFFERENT_BANK_CARDS = "allowDifferentBankCards";


	public static final String STATION_SETTING = "1";
	
	public static final String MAIL_SETTING = "2";
	
	public static final String SMS_SETTING = "3";
	
	public static final String REGISTER_SETTING = "4";
	
	public static final String KEY_ISVISIBLE="isVisible";
	public static final String KEY_ISREQUIRE="isRequire";
	
	public static final Integer STATUS_DISABLE= 0;
	public static final Integer STATUS_ENABLE = 1;

	// 是否启用自动出款
	public static final String PAY_AUTOMATIC = "payAutomatic";

	// 自动出款单笔最高限额(元)
	public static final String PAY_MONEY = "payMoney";

	// 是否启用免转
	public static final String FREE_WALLETSWITCH = "freeWalletSwitch";
	//站点前缀
	public static final String SITE_CODE ="siteCode";
	//Stoken
	public static final String STOKEN ="SToken";
	//schemaName
	public static final String SCHEMA_NAME ="schemaName";

	public static final String T_CP_SITE_SERVICE ="TCpSiteService";
}
