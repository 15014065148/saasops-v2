package com.eveb.saasops.modules.analysis.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class RptBetModel implements Serializable {

    /**注单唯一编号，平台方获取**/
    private String id;
    /**API前缀**/
    private String apiPrefix;
    /**前缀**/
    private String sitePrefix;
    /**站点名称**/
    private String website;
    /**游戏名称**/
    private String gameName;
    /**游戏类型：真人、老虎机**/
    private String gameType;
    /**游戏平台：PT、AG、BBIN**/
    private String platform;
    /**玩家用户名**/
    private String userName;
    /**投注**/
    private BigDecimal bet;
    /**投注类型**/
    private String betType;
    /**场次**/
    private String roundNo;
    /***桌号**/
    private String tableNo;
    /**局号**/
    private String serialId;
    /**有效投注**/
    private BigDecimal validBet;
    /**派彩**/
    private BigDecimal payout;
    /**奖池投注**/
    private BigDecimal jackpotBet;
    /**奖池赢得**/
    private BigDecimal jackpotPayout;
    /**结果：输、赢**/
    private String result;
    /**状态：已结算、未结算**/
    private String status;
    /**小费**/
    private BigDecimal tip;
    /**投注时间**/
    private Date betTime;
    /**开赛时间**/
    private Date startTime;
    /**派彩时间**/
    private Date payoutTime;
    /**下载时间**/
    private Date downloadTime;
    /**账务时间**/
    private Date orderDate;
    /**'1.行动装置下单：M 1‐1.ios手机：MI1 1‐2.ios平板：MI2 1‐3.Android手机：MA1 1‐4.Android平板：MA2 2.计算机下单：P'**/
    private String origin;
    /**币别**/
    private String currency;
    /**下注前余额 有些平台无此值**/
    private BigDecimal balanceBefore;
    /**派彩后余额 有些平台无此值**/
    private BigDecimal balanceAfter;
    /**游戏详情JSON结果*/
    private String openResultDetail;
    /**游戏详情结果封装*/
    private OpenResultModel openResultModel;
}

