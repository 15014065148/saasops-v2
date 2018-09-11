package com.eveb.saasops.modules.system.onlinepay.entity;

import lombok.Data;

/**
 * Created by William on 2018/3/26.
 */
@Data
public class SelectedGroup {

    private String groupName;
    private Integer id ;
    public SelectedGroup() { }
    public SelectedGroup(String groupName, Integer id) {
        this.groupName = groupName;
        this.id = id;
    }
}
