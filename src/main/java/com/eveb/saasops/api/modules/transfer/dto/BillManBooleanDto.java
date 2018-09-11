package com.eveb.saasops.api.modules.transfer.dto;

import com.eveb.saasops.modules.member.entity.MbrBillManage;
import lombok.Data;

@Data
public class BillManBooleanDto {
    private Boolean isTransfer;
    private MbrBillManage mbrBillManage;
}
