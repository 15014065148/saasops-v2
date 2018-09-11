package com.eveb.saasops.api.constants;

import java.math.BigDecimal;

public class PbConstants {
	public static String INIT_VECTOR = "RandomInitVector";
	public interface Headkey{
		String token="token";
		String userCode="userCode";
	}
	public interface Mod {

		String createMember = "/player/create?";// 登陆并创建
		String balance = "/player/info?";// 会员信息余额
		String login = "/player/login?";// 登陆
		String deposit = "/player/deposit?";//存款
		String withdraw = "/player/withdraw?";//取款
		String logOut = "/player/logout?";// 登出
		/*String trfrStatus = "/api/CheckFundTransfer?";// 转账状态
		String preMemBetSet="/api/PrepareMemberBetSetting?";//设置游戏种类
		String conMemBetSet="/api/ConfirmMemberBetSetting?";//确认游戏种类
		String getBetSetLimit="/api/GetBetSettingLimit?";//获取投注最大设置
		String gameInfo = "?lang=cs&g=";// 游戏跳转*/
	}
	public interface Json {
		String agentKey = "agentKey";
		String secretKey = "secretKey";
	}

	public interface Language {
		String en = "en";//English
		String zhCn = "zh-cn";//Simplified Chinese (简体中文)
		String chTw = "zh-tw";// Traditional Chinese (繁體中文)
	}

	public interface Sport {
		String soccer = "soccer";// Soccer
		String tennis = "tennis";// Tennis
		String basketball = "basketball";// Basketball
		String football = "football";// Football
		String baseball = "baseball";// Baseball
		String golf = "golf";// Golf
		String hockey = "hockey";// Hockey
		String volleyball = "volleyball";// Volleyball
		String handball = "handball";// Handball
		String boxing = "boxing";// Boxing
		String snooker = "snooker";// Snooker
		String cycling = "cycling";//cycling
	}
}
