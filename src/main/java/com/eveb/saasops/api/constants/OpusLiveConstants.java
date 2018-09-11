package com.eveb.saasops.api.constants;

public class OpusLiveConstants {


	public static final String SUC_CODE = "00";

	public interface Mod {
		String createMember = "/CreateMember.API?";// 注册
		String kickUser = "/KickOutMember.API?";// 会员踢线
		String checkIsOnline = "/CheckStatusOnline.API?";//检测会员是否在线
		String balance = "/MemberBalance.API?";// 会员余额
		String debitBalance = "/DebitBalance.API?";// 取款
		String creditBalance = "/CreditBalance.API?";// 存款
		String checkFundTransfer = "/CheckFundTransferStatus.API?";//检测转账
	}

	public interface JosnKey {
		String operatiorId = "Operator_ID";
		String siteCode = "Site_Code";
		String secretKey = "Secret_Key";
		String productCode = "Product_Code";
		String prefix="prefix";
	}
	public interface Memstatus
	{
		String online="online";
		String offline="offline";
		String kickout="kickout";
	}

	/*public interface FundTransfer {
		Integer deposit = 1;//Credit
		Integer withdraw = 0;//Debit
	}*/

	public interface Status {
		String success = "0";
		String failed = "1";
	}

	public interface Languages {
		String english = "en-US";
		String englishIn = "en-IN";
		String chinese = "zh-CN";
		String thai = "th-TH";
		String viet = "vi-VN";
		String japan = "ja-JP";
		String indo = "id-ID";
		String korean = "ko-KR";
		String Khmer = "km-KH";
	}

	public interface Odds {
		Integer indo = 30;
		Integer malay = 31;
		Integer hongkong = 32;
		Integer decimal = 33;
	}

	public interface Currencys {
		String aud = "AUD";
		String eur = "EUR";
		String idr = "IDR";
		String myr = "MYR";
		String rmb = "RMB";
		String thb = "THB";
		String usd = "USD";
		String jpy = "JPY";
		String vnd = "VND";
		String inr = "INR";
		String krw = "KRW";
	}
}
