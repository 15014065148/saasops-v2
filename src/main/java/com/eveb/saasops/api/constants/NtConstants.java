package com.eveb.saasops.api.constants;

public class NtConstants {

	public static final String RES_PING = "success";// ping 返回信息
	public static final String DEF_CURRENCY = "CNY";
	public static final String LOGING_SUCCESS = "SUCCESS";
	public static final String NETENT_CAS = "NETENT_CAS";
	public static final String USER_AGENT = "User-Agent";
	public static final String USER_AGENT_VAL = "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)";

	public interface Mod {
		String ping = "ps/ips/ping";// ping
		String alive = "ps/ips/checkSessionAlive";// 检测账号是否在线
		// String register="ps/ips/createPlayerReq";//注册
		String login = "ps/ssw/login";// 登陆
		String gameInfo = "ps/game/GameContainer.action";// 游戏详细信息
		String balance = "omegassw/getBalance";// 会员信息 余额
		String transfer = "ps/ips/transferV2Req";// 转账
		String trfrStatus = "ps/ips/GetTransaction";// 检查会员状态
	}

	public interface JsonKey {
		String brandId = "brandId";
		String brandPwd = "brandPassword";
		String walletUrl = "walletUrl";
	}

}
