package com.eveb.saasops.modules.agent.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@Table(name = "agy_commn_stage")
public class ComnStage {
    /**
     * id
     */
    @Id
    private Integer id;

    /**
     * 阶段
     */
    private Integer stage;

    /**
     * 盈利总额 or 有效投注总额
     */
    private BigDecimal amount;

    /**
     * 有效投注人数 Valid People Number
     */
    private Integer valpeopNum;

    /**
     * Commission ID
     */
    private Integer commnId;

    /**
     *
     */
    @Transient
    private List<ComnRate> comnRates;

}