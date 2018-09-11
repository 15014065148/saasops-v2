package com.eveb.saasops.listener;

import lombok.Getter;

/**
 * Created by William on 2018/3/7.
 */
public enum BizEventType {

    //会员注册成功
    MEMBER_REGISTER_SUCCESS(1),
    //会员升级成功
    MEMBER_LEVELUP_SUCCESS(2),
    //修改会员资料
    UPDATE_MEMBER_INFO(3),
    //强制踢出
    FORCE_LOGOUT(4),
    //会员账户冻结
    MEMBER_ACCOUNT_FREEZE(5),
    //在线支付成功
    ONLINE_PAY_SUCCESS(6),
    //存款审核成功
    DEPOSIT_VERIFY_SUCCESS(7),
    //存款审核失败
    DEPOSIT_VERIFY_FAILED(8),
    //优惠审核成功
    PROMOTE_VERIFY_SUCCESS(9),
    //优惠审核失败
    PROMOTE_VERIFY_FAILED(10),
    //会员返水成功
    MEMBER_COMMISSION_SUCCESS(11),
    //拒绝会员返水
    MEMBER_COMMISSION_REFUSE(12),
    //会员提款初审拒绝
    MEMBER_WITHDRAWAL_PRIMARY_VERIFY_FAILED(13),
    //会员提款复审拒绝
    MEMBER_WITHDRAWAL_REVIEW_VERIFY_FAILED(14),
    //会员取款审核成功
    MEMBER_WITHDRAWAL_REVIEW_VERIFY_SUCCESS(15),
    //拒绝会员取款
    MEMBER_WITHDRAWAL_REFUSE(16),
    //代理注册成功
    AGENCY_REGISTER_SUCCESS(17),
    //代理取款审核成功
    AGENCY_WITHDRAWAL_VERIFY_SUCCESS(18),
    //代理取款审核失败
    AGENCY_WITHDRAWAL_VERIFY_FAILED(19),
    //代理返佣成功
    AGENCY_SALARY_SUCCESS(20),
    //代理账户冻结
    AGENCY_ACCOUNT_FREEZE(21),
    //拒绝代理取款
    AGENCY_WITHDRAWAL_REFUSE(22),
    //拒绝返佣
    AGENCY_SALARY_REFUSE(23);
    //人工存入成功
    //MANUAL_DEPOSIT_SUCCESS(6),
    //人工取出成功
    //MANUAL_WITHDRAWAL_SUCCESS(7),
    //余额冻结
    //BALANCE_ACCOUNT_FREEZE(14),















    @Getter
    private Integer eventCode;

    BizEventType(Integer eventCode) {
        this.eventCode = eventCode;
    }

}
