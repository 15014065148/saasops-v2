package com.eveb.saasops.api.modules.unity.dto;

import lombok.Data;

@Data
public class PlayGameModel {

    private Integer depotId;
    private String depotName;
    private String siteCode;
    private String userName;
    private String gameId;
    private String gameType;
    private String gamecode;

    /**设备类型：PC、H5、APP**/
    private String origin;
}
