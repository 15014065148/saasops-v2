package com.eveb.saasops.api.constants;

/**
 * 
 * @author 平台EV 常用关键字
 */
public class EvConstants {

	// 接口函数专用关键字
	public static final String EV_FUN_CHECKORCREATEGAMEACCOUT = "lg";//检测并创建游戏账号
	public static final String EV_FUN_PREPARETRANSFERCREDIT="tc";//预备转账
	public static final String EV_FUN_TRANSFERCREDITCONFIRM="tcc";//转账确认
	public static final String EV_FUN_GETBALANCE="gb";//余额查找
	public static final String EV_FUN_QUERYORDERSTATUS="qos";//状态查询
	public static final String EV_FUN_LOGOUT="Logout";//登出
	public static final String EV_M_DOBUSINESS = "doBusiness.do?";
	public static final String EV_M_FORWARDGAME = "forwardGame.do?";
	public static final String EV_USER_AGENT_VAL="WEB_LIB_GI_";
	public static final String EV_USER_AGENT="User-Agent";
	public static final String EV_FUN_SUCC_KEY = "0";
	public static final String EV_DM="api.mallggshop.com";
	//public static final String EV_SEQUENCE="none";
	
	public static final String EV_DESCODE_KEY="desCode";
	
	public interface CreateAccoutParam
	{
		String limit="1,1,1,1,1,1,1,1,1,1,1,1,1,1";//1代表桌台使能，0代表不能 14个数字
		String limitvideo="1";//固定值
		String limitroulette="1";//固定值
	}

	public interface Actype {
		int trueAccount = 1;// 真钱账户
		int testAccount = 0;// 试玩账号
	}

	public interface OddTpye {
		// 人民币下注范围
		String OddA = "A";// 20-50000
		String OddB = "B";// 50-5000
		String OddC = "C";// 20-10000
		String OddD = "D";// 200-20000
		String OddE = "E";// 300-30000
		String OddF = "F";// 400-40000
		String OddG = "G";// 500-50000
		String OddH = "H";// 1000-100000
		String OddI = "I";// 2000-200000
	}
	
	public interface GameType{
		Integer hall_0=0;//	大厅 
		Integer hall_1=1;//	旗舰厅 (AGQ 厅) 
		Integer hall_2=2;//	国际厅 (AGIN 厅) 
		Integer hall_3=3;//	多台 (自选多台) 
		Integer hall_4=4;//	包桌 (VIP 包桌)

	}

	public interface CurTpye {
		// 币种
		String curCny = "RMB";// 人民币
	}

	public interface RetCode {
		int succ_code = 0;// 成功
		int fail_code = 1;// 失败 , 订单未处理状态
		int inv_code = 2;// 无效
	}

	public interface LangCode {
		// 语言
		int cn_code = 1;// zh-cn (简体中文)
		int tw_code = 2;// zh-tw (䌓体中文）
		int us_code = 3;// en-us(英语)
		int jp_code = 4;// euc-jp(日语) 4
	}

	/**
	 * 预备转账 返回结果
	 */
	public interface PPreTrf{
		int fail=0; //失败
		int suc=1; //成功
	}
	public interface Res
	{
		String succ="0";//成功
		String fail="1";//失败，订单处理中
		String net_err="network_error";//网络问题导致资料遗失
		String error="error";//转账错误, 参看 msg 错误信息描述
	}
}
