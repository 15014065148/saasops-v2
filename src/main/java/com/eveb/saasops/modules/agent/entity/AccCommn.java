package com.eveb.saasops.modules.agent.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import javax.persistence.Table;

@Setter
@Getter
@Table(name = "agy_acc_commn")
public class AccCommn {
    /**
     * id
     */
    @Id
    private Integer id;

    /**
     * 代理帐号
     */
    private Integer accid;

    /**
     * 佣金方案
     */
    private Integer commnId;
}