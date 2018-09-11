package com.eveb.saasops.api.constants;
/**
 * 
 * @author 
 * 平台BBIN 常用关键字
 */

public class BbinConstants {
	
	// 接口函数专用关键字
	//建立用户
	public static final String BBIN_API_CREATEMEMBER="CreateMember";
	//登陆
	public static final String BBIN_API_LOGIN="Login";
	//登出
	public static final String BBIN_API_LOGOUT="Logout";
	public static final String BBIN_API_CHECKUSRBALANCE="CheckUsrBalance";

	
	public static final String BBIN_API_TRANSFER="Transfer";
	public static final String BBIN_API_CHECKTRANSFER="CheckTransfer";
	public static final String BBIN_API_TRANSFERRECORD="TransferRecord";

	
	public static final String BBIN_API_BETRECORD="BetRecord";
	public static final String BBIN_API_BETRECORDBYMODIFIEDDATE3="BetRecordByModifiedDate3";
	public static final String BBIN_API_LOGIN2="Login2";

	public static final String BBIN_API_PLAYGAME = "PlayGame";
	public static final String BBIN_API_PLAYGAMEBYH5 = "PlayGameByH5";
	public static final String BBIN_API_GETJPHISTORY = "GetJPHistory";
	
	// 返回类型 关键字
	//转账时，API忙
	public static final int BBIN_RSCODE_TFRUNING=10015;
	//新增账号失败
	public static final int BBIN_RSCODE_ACCADDFAIL=21000;
	//账号重复
	public static final int BBIN_RSCODE_ACCADDREPEATED=21001;
	//账号新增成功
	public static final int BBIN_RSCODE_ACCADDSUCC=21100; 
	//使用者未登入
	public static final int BBIN_RSCODE_ACCUNLOGIN=22000;
	//账号不存在
	public static final int BBIN_RSCODE_ACCNOEXIST=22013;
	
	//无权限
	public static final int BBIN_RSCODE_ACCUNAUTH =44002;
	
	//登入成功
	public static final int BBIN_RSCODE_ACCLOGINSCC=99999;
	//系统维护中
	public static final int BBIN_SYS_MAINTENANCE=44444;
	//系统维护中
	public static final int BBIN_GM_MAINTENANCE=44445;
	
	public static final int BBIN_KEY_ERROR=44000;//key驗證錯誤
	
	public static final String BBIN_TRF_FAIL="-1";//转账失败
	
	public static final String BBIN_TRF_SUC="1";//转账成功

	//登出成功
	public static final int BBIN_RSCODE_LOGOUTSCC=22001;
	
	
	public interface loginType
	{
		int login=0;//Login 方式登陆
		int login2=1;//Login2 方式登陆
	}
/*	public interface Transfer
	{
		 String in = "IN";//转入
	     String out = "OUT";//转出
	}*/
}
