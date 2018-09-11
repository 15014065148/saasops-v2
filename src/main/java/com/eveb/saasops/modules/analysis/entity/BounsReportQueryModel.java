package com.eveb.saasops.modules.analysis.entity;

import lombok.Data;

import javax.persistence.Transient;

@Data
public class BounsReportQueryModel {

    /***针对前端传入的时间进行处理***/
    private static final String strTiming = " 00:00:00";
    private static final String endTiming = " 23:59:59";

    private Integer pageNo;
    private Integer pageSize;
    /**
     * 总代理
     */
    private Integer parentAgentid;
    /**
     * 代理账号
     */
    private Integer agentid;
    /**
     * 会员组
     */
    private Integer groupid;
    /**
     * 会员名
     */
    private String userName;
    private String userId;
    /**
     * 活动ID
     */
    private Integer activityId;
    private Integer catId;
    private Integer origin;
    /*** 1 PC端 ，2 手机端***/
    private Integer enablePc;
    private Integer enableMb;
    private String betStrTime;
    private String betEndTime;
    private String loginSysUserName;

    //查询使用
    @Transient
    private String parentAgentidList;
    @Transient
    private String agentidList;
    @Transient
    private String groupidList;
    @Transient
    private String activityIdList;
    //活动名称/客户端
    @Transient
    private String catIdList;
    @Transient
    private String originList;

    public Integer getEnablePc() {
        return origin != null && origin == 1 ? 1 : 0;
    }

    public Integer getEnableMb() {
        return origin != null && origin == 2 ? 1 : 0;
    }

    public String getBetStrTime() {
        if (betStrTime == null || betStrTime.isEmpty()) {
            return null;
        }
        return betStrTime + strTiming;
    }

    public String getBetEndTime() {
        if (betEndTime == null || betEndTime.isEmpty()) {
            return null;
        }
        return betEndTime + endTiming;
    }
}
