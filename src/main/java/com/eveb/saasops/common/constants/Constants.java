package com.eveb.saasops.common.constants;

import java.math.BigDecimal;

public class Constants {
	
	//项目名称
	public static final String PROJECT_NAME="SaasopsV2";
	//redis 名称空间分隔符
	public static final String REDIS_SPACE_SPACING=":";
	//redis 失效
	public static final String REDIS_CMD_EXPIRED="expired";

    public static final BigDecimal DEAULT_ZERO_VALUE = BigDecimal.ZERO;
    //总代上级默认Id
    public static final int TOP_AGENT_PARENT_ID = 0;
    //暂无
    public static final String SYSTEM_NONE = "NONE";
    //本系统平台depotId
    public static final int SYS_DEPOT_ID = 0;

    public static final String SYSTEM_USER = "系统审核";
    public static final String SYSTEM_PASSUSER = "系统出款";
    
    public static final String SYSTEM_DEPOT_NAME="钱包中心";

    public static final int ONE_HUNDRED = 100;

    public static final String ACCOUNT_CONTACT = "member:mbraccount:contact";

    public interface EVNumber {
        int zero = 0;
        int one = 1;
        int two = 2;
        int three = 3;
        int four = 4;
        int five = 5;
        int six = 6;
        int seven = 7;
    }

    /**
     * 所有记录的的状态
     */
    public interface Available {
        byte disable = 0;
        byte enable = 1;
    }

    public interface TransferType {
        Integer out = 0;//钱包转平台
        Integer into = 1;//平台转钱包
    }

    public interface ChineseAvailable {
        String disable = "禁用";
        String enable = "启用";
    }

    public interface ChineseStatus {
        String succeed = "成功";
        String defeated = "失败";
        String pending = "待处理";
    }

    public interface IsStatus {
        Integer defeated = 0;//失败 or 拒绝
        Integer succeed = 1;//成功 or 通过
        Integer pending = 2;//待处理
        Integer outMoney = 3;//出款中
        Integer four = 4;//待处理
    }

    public interface manageStatus {
        Integer freeze = 0;//冻结
        Integer succeed = 1;//成功
        Integer defeated = 2;//失败
    }
    public interface  sourceType
    {
        String admin="admin";
        String web="web";
    }

    public interface feeWay{
    	Integer fixed = 0;//固定收费
    	Integer percent = 1;//按比例收费    	
    }
    
    public interface status{
    	Integer able = 1;
    	Integer disable = 0;
    }

    public interface Operation_Status{
        String success = "成功";
        String failed = "失败";
    }
    //完整度
    public interface userInfoMeasure{
        Byte userInfoConut=7;
        Byte zero = 0;
        Byte full = 100;
    }
}
