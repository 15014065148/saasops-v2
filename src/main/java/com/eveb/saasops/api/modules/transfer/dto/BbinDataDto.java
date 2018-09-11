package com.eveb.saasops.api.modules.transfer.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BbinDataDto {
	@ApiModelProperty(value="狀態(1:成功；-1:處理中或失敗 )")
    private Integer Code;
    @ApiModelProperty(value="消息")
    private String Message;
    @ApiModelProperty(value="轉帳序號")
    private Long TransID;
    @ApiModelProperty(value="轉帳型態(入/出)")
    private String TransType;
    @ApiModelProperty(value="狀態(1:成功；-1:處理中或失敗 )")
    private String Status;
}
