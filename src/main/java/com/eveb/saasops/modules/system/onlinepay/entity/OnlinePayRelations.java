package com.eveb.saasops.modules.system.onlinepay.entity;

import lombok.Data;

import java.util.List;

/**
 * Created by William on 2018/3/26.
 */
@Data
public class OnlinePayRelations {

    private Integer onlinePayId;

    private List<BankOptions> bankOptions;

    private List<SelectedGroup> selectedGroup;
}
