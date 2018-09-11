package com.eveb.saasops.modules.agent.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Setter
@Getter
@Table(name = "agy_commn_rate")
public class ComnRate {
    /**
     * id
     */
    @Id
    private Integer id;

    /**
     * 平台id
     */
    private Integer depotId;

    /**
     * 游戏分类ID
     */
    private Integer catId;

    /**
     * 返佣金比例
     */
    private BigDecimal commnRate;

    /**
     * stageid
     */
    private Integer stageId;


}