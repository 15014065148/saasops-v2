package com.eveb.saasops.modules.agent.entity;

import com.eveb.saasops.modules.base.entity.BaseAuth;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@Table(name = "agy_commission")
public class Commission {
    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 佣金方案名称
     */
    private String commnName;

    /**
     * 基础有效投注额 Base Valid Bet
     */
    private BigDecimal basevalBet;

    /**
     * 1开启，0禁用
     */
    private Byte available;

    /**
     * 佣金计算基准 类型  1盈利总额 0 有效投注总额
     */
    private Integer commnType;

    /**
     * 备注
     */
    private String memo;

    private String createUser;
    private String createTime;
    private String modifyUser;
    private String modifyTime;

    /**
     * 使用代理数
     */
    @Transient
    private Integer accCount;

    /**
     * 阶段
     */
    @Transient
    private List<ComnStage> comnStages;
    @Transient
    private BaseAuth baseAuth;

}