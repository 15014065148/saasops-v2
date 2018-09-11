package com.eveb.saasops.modules.agent.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Id;
import javax.persistence.Table;

@Setter
@Getter
@Table(name = "agy_commn_charge")
public class ComnCharge {
    /**
     *id
     */
    @Id
    private Integer id;

    /**
     * 手续费分摊比例->存款(百分比)
     */
    private Integer depositPercent;

    /**
     * 手续费分摊比例->取款(百分比)
     */
    private Integer withdrawPercent;

    /**
     * 手续费分摊比例->返水(百分比) 返水：rakeback
     */
    private Integer rakebackPercent;

    /**
     * 手续费分摊比例->优惠(百分比)
     */
    private Integer discountPercent;

    /**
     * 手续费分摊比例->其他(百分比)
     */
    private Integer otherPercent;
    private String createUser;
    private String createTime;
    private String modifyUser;
    private String modifyTime;

}