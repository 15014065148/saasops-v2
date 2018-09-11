package com.eveb.saasops.api.constants;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.entity.ContentType;


public class MgConstants {
	public static String TOKEN_KEY = "X-Api-Auth";
	public static String TOKEN_RES_KEY="token";
	public static String RS_SUCCESS="success";
	public static String URL_ID="{id}";
	public static String RES_SUC_CODE="0";
	

	public static Map<String, String> getHead() {
		Map<String, String> head = new HashMap<String, String>();
		head.put("X-Requested-With", "X-Api-Client");
		head.put("X-Api-Call", "X-Api-Client");
		head.put("Content-Type", "application/json");
		return head;
	}
	public static Map<String, String> getHeadXml() {
		Map<String, String> head = new HashMap<String, String>();
		//head.put("X-Requested-With", "X-Api-Client");
		//head.put("X-Api-Call", "X-Api-Client");
		head.put("Content-Type", ContentType.APPLICATION_XML.toString());
		return head;
	}

	public interface Mod {
		String token = "lps/j_spring_security_check";// 代理账号TOKEN
		String createMember = "lps/secure/network/{id}/downline";// 创建账号
		String login = "member-api-web/member-api";// 登陆
		String gameInfo = "member-api-web/member-api";// 游戏详细信息
		String balance = "member-api-web/member-api";// 会员信息 余额
		String transfer = "member-api-web/member-api";// 转账
		//String trfrStatus = "ps/ips/GetTransaction";// 检查会员状态
	}

	public interface Json {
		String crId = "crId";
		String neId = "neId";
		String crType="crType";
		String apiAdmin = "apiAdmin";
		String apiPwd = "apiPassword";
		String partnerId = "partnerId";
		String currency = "currency";
		String neType = "neType";
		String reportCrId = "reportCrId";
		String j_username = "j_username";
		String j_password = "j_password";
		String jackpotUrl = "jackpotUrl";
		String DownloadStep = "DownloadStep";
	}

	public interface MgCreationVal {
		String currency = "CNY";
		String language = "en";
		String tarType = "m";
	}

	public interface LoginResKey {
		String casinoId = "casinoId";
		String status = "status";
		String timestamp = "timestamp";
		String token = "token";
	}

	public interface LaunchResKey {
		String status = "status";
		String timestamp = "timestamp";
		String token = "token";
		String launchUrl = "launchUrl";
	}

	public interface DetailsResKey {
		String status = "status";
		String timestamp = "timestamp";
		String token = "token";
		String network_entity_key = "network-entity-key";
		String currency = "currency";
		String timezone = "timezone";
		String code = "code";
		String poker_alias = "poker-alias";
		String main_user_key = "main-user-key";
		String username = "username";
		String name = "name";
		String language = "language";
		String product = "product";
		String credit_balance = "credit-balance";
		String cash_balance = "cash-balance";
		String cash_soft = "cash-soft";
		String cash_hard = "cash-hard";
		String account_wallet = "account-wallet";
	}

	public interface TransferResKey {
		String status = "status";
		String timestamp = "timestamp";
		String token = "token";
	}

	public interface TransferParam {
		String deposit = "topup";
		String withdraw = "withdraw";
	}
	public interface Product
	{
		String poker="poker";//扑克
		String casino="casino";//堵场  
	}
}
