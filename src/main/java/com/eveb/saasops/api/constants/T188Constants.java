package com.eveb.saasops.api.constants;

public class T188Constants {

	public static String REG_DEF_TIME = "GMT-04:00";// 格林
	public static String SUC_CODE = "000";// 成功CODE

	public interface Mod {
		String register = "/RegisterMember";// 注册
		String balance = "/GetMemberBalance";// 会员信息 余额
		String deposit = "/DepositFund";// 转入
		String withdraw = "/WithdrawFund";// 转出
		String transferStatus = "/GetTransferStatus";// 转出

	}

	public interface SaopModName {
		String GetTicket = "GetTicket";
		String Balance = "Balance";
	}

	public interface JsonKey {
		String url = "url";
		String guid = "guid";
		String brandId = "BrandId";
		String pid = "pid";
	}

	public interface Odds {
		String euro = "1";
		String hk = "2";
		String malay = "3";
		String indo = "4";
	}

	public interface Currencies {
		String dzd = "DZD"; // Algeria Dinar
		String eek = "EEK"; // Estonia Kroon
		String eur = "EUR"; // Euro Dollar
		String gbp = "GBP"; // British Pound
		String hkd = "HKD"; // Hong Kong Dollar
		String idr = "IDR"; // Indonesia Ruppiah
		String krw = "KRW"; // South Korean Won
		String myr = "MYR"; // Malaysia Ringgit
		String php = "PHP"; // Philippines Peso
		String rmb = "RMB"; // Renminbi (Chinese Yuan)
		String sgd = "SGD"; // Singapore Dollar
		String thb = "THB"; // Thai Baht
		String twd = "TWD"; // Taiwan Dollar
		String usd = "USD"; // US Dollar
		String vnd = "VND"; // Vietnam Dong
		String cny = "CNY";
	}

	public interface Language {
		String eng = "EBG";// 英文
		String chs = "CHS";// 中文 简
		String cht = "CHT";// 中文 繁
	}

	public interface Status {
		String active = "501";// 激活
		String inactive = "502";// 非激活
		String Suspend = "504";// 挂起
	}

}
