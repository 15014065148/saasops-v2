package com.eveb.saasops.api.modules.transfer.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TransferRequestDto {

    private String website;         //網站名稱
    private String username;        //會員帳號
    private String uppername;       //上層帳號
    private Long remitno;           //轉帳序號(唯一值)，可用貴公司轉帳紀錄的流水號，以避免重覆轉帳< 請用int(19)( 1~9223372036854775806)來做設定 >，別名transid
    private String action;          //IN(轉入額度) OUT(轉出額度)
    private String key;             //轉帳額度(正整數)
    private Integer remit;             //轉帳額度(正整數)

    private Long transid;

    private String transtype;//IN轉入;OUT轉出
    private String date_start;//開始日期
    private String date_end;//結束日期
    private String start_hhmmss;//開始時間
    private String end_hhmmss;//結束時間

    private Integer page;
    private Integer pagelimit;
}
