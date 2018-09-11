package com.eveb.saasops.modules.analysis.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RptMemberModel {

    /***统计日期**/
    private String startday;
    /***新增会员**/
    private Integer newMbrs;
    /***新增会员并存款**/
    private Integer newDeposits;
    /***活跃会员**/
    private Integer activeMbrs;
    /***总会员**/
    private Integer totalMbrs;
    /***总存款***/
    private BigDecimal deposits=BigDecimal.ZERO;
    /***总提款***/
    private BigDecimal withdraws=BigDecimal.ZERO;
    /***总派彩***/
    private BigDecimal payouts=BigDecimal.ZERO;
    /***总有效投注***/
    private BigDecimal validBets=BigDecimal.ZERO;
}
