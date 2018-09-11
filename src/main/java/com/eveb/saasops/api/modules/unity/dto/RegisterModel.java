package com.eveb.saasops.api.modules.unity.dto;

import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import lombok.Data;

@Data
public class RegisterModel {

    private Integer depotId;
    private String depotName;
    private String password;
    private String siteCode;
    private String userName;
    private TGmApi tGmApi;
}
