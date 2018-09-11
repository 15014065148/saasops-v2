package com.eveb.saasops.api.modules.unity.dto;

import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import lombok.Data;

@Data
public class TransferModel{

    private Integer depotId;
    private String depotName;
    private String siteCode;
    private String userName;
    private TGmApi tGmApi;
    private String orderNo;
    private Integer amount;
}
