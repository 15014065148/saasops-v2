package com.eveb.saasops.api.constants;

import java.math.BigDecimal;

public class IbcConstants {
	public static String ERROR_CODE="0";//成功
	public static String GAME_161="161";//游戏161
	//使用者未登入
	public static String ERROR_CODE_ACCUNLOGIN="24203";

	public interface Mod {
		String createMember = "/api/CreateMember?";// 创建账号
		String balance = "/api/CheckUserBalance?";// 会员信息余额
		String login = "/api/Login?";// 登陆
		String logOut = "/api/KickUser?";// 登出
		String deposit = "/api/FundTransfer?";//存款
		String withdraw = "/api/FundTransfer?";//取款
		String trfrStatus = "/api/CheckFundTransfer?";// 转账状态
		String preMemBetSet="/api/PrepareMemberBetSetting?";//设置游戏种类
		String conMemBetSet="/api/ConfirmMemberBetSetting?";//确认游戏种类
		String getBetSetLimit="/api/GetBetSettingLimit?";//获取投注最大设置
		String gameInfo = "?lang=cs&g=";// 游戏跳转
	}

	public interface Transfer {
		BigDecimal MaxTransfer = new BigDecimal(1000000.00);
		BigDecimal MinTransfer = new BigDecimal(1.00);
		Integer toIn=1;//转出到第三方
		Integer toOut=0;//转出到平台
	}

	public interface Odds { 
		String euro = "1";//欧洲盘
		String hk = "2";//香港盘
		String indo = "3";//印尼盘
		String malay = "4";//马来盘
		String us = "5";//美国盘
	}
	public interface Currencies {
		Integer SGD=1;//  新加坡美元
		Integer RMB=13;//  中国人民币
	}
	public interface Json
	{
		String gameids="gameids";
	}
	

}
