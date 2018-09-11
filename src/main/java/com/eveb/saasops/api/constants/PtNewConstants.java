package com.eveb.saasops.api.constants;

public class PtNewConstants {

	public static final String ACCESS_TOKE = "x-access-token";// 所有接口需的token关键字
	public static final String LOGIN_TOKEN = "accessToken";// 代理账号返回token关键字
	public static final String DEF_CHASET = "UTF-8";
	public static final String RES_CODE = "Code";
	public static final String RES_SUC_CODE = "200";// 成功CODE
	public static final String RES_SUC_TRF_CODE = "201";// 转账成功

	public interface PostCat {
		String post = "POST";
		String get = "GET";
	}

	public interface Mod {
		String agentLogin = "/v1/login";// 代理账户登陆
		// String refresh = "/v1/login/refresh";// 刷新代理账户Token
		//String gameList = "/v1/players/{playerCode}/games/";// 游戏列表
		String gameInfo = "/v1/players/playerCode/games/";// 游戏详细信息
		String register = "/v1/players";// 建立账号/players
		String info = "/v1/players/";// 会员信息 余额
		String deposit = "/v1/payments/transfers/in";// 转入
		String withdraw = "/v1/payments/transfers/out";// 转出
		String trfrStatus = "/v1/payments/";// 检查会员状态
	}

	public interface JsonKeys {
		String secretKey = "secretKey";// 安全KEY
		String loginurl = "loginurl";// 登陆URL KEY
		String historyurl = "historyurl";// 投注记录URL
	}

	public interface RegiterDefault {
		String country = "CN";
		String currency = "CNY";
		String language = "zh-cn";
		String status = "normal";
		boolean isTest = Boolean.FALSE;
	}

	public interface TransactionInfo {
		String absent = "absent";// 失败
		String processing = "processing";// 处理中
		String committed = "committed";// 已处理
	}
}
