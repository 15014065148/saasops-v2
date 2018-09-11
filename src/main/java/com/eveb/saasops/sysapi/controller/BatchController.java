package com.eveb.saasops.sysapi.controller;

import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.apisys.mapper.ApiSysMapper;
import com.eveb.saasops.api.modules.apisys.service.TGmApiService;
import com.eveb.saasops.api.modules.user.service.DepotWalletService;
import com.eveb.saasops.api.modules.user.service.RedisService;
import com.eveb.saasops.common.utils.AESUtil;
import com.eveb.saasops.common.utils.Collections3;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.common.validator.Assert;
import com.eveb.saasops.common.constants.RedisConstants;
import com.eveb.saasops.listener.BizEvent;
import com.eveb.saasops.listener.BizEventType;
import com.eveb.saasops.modules.member.service.AuditCastService;
import com.eveb.saasops.modules.operate.service.OprActActivityService;
import com.eveb.saasops.modules.operate.service.OprRecMbrService;
import com.eveb.saasops.modules.system.onlinepay.service.SetBacicOnlinepayService;
import com.eveb.saasops.modules.fund.service.FundWithdrawService;
import com.eveb.saasops.sysapi.service.BatchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Objects.isNull;

@Slf4j
@RestController
@RequestMapping("/sysapi")
@Api(value = "提供给batch项目的服务", description = "提供给batch项目的服务")
public class BatchController {

    @Autowired
    private DepotWalletService depotWalletService;
    @Autowired
    private TGmApiService gmApiService;
    @Autowired
    private BatchService batchService;
    @Autowired
    private ApiSysMapper apiSysMapper;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private FundWithdrawService withdrawService;
    @Autowired
    private SetBacicOnlinepayService setBacicOnlinepayService;
    @Autowired
    private OprRecMbrService oprRecMbrService;
    @Autowired
    private AuditCastService auditCastService;
    @Autowired
    private OprActActivityService actActivityService;
    @Autowired
    private RedisService redisService;


    @RequestMapping("/depotBalance")
    @ApiOperation(value = "平台会员余额", notes = "平台会员余额")
    public R depotBalance(@RequestParam("siteCode") String siteCode, @RequestParam("depotId") Integer depotId, @RequestParam("accountId") Integer accountId) {
        Assert.isNull(depotId, "平台Id不能为空");
        Assert.isNull(accountId, "会员Id不能为空");
        Assert.isBlank(siteCode, "siteCode不能为空");
        String cpSiteCode = apiSysMapper.getCpSiteCode(AESUtil.decrypt(siteCode));
        TGmApi gmApi = gmApiService.queryApiObject(depotId, cpSiteCode);
        return R.ok().put(depotWalletService.queryDepotBalance(accountId, gmApi));
    }

    @PostMapping("/paySuccess")
    @ApiOperation(value = "平台会员支付成功", notes = "平台会员支付成功")
    public R paySuccess(@RequestParam("ids") List<Integer> ids) {
        Assert.isNull(ids, "ids不能为空");
        ids.forEach(is -> batchService.paySuccess(is));
        return R.ok();
    }

    @PostMapping("/onlinePayInfo")
    @ApiOperation(value = "获取支付方式", notes = "获取支付方式")
    public R onlinePayInfo(@RequestParam("id") Integer id) {
        Assert.isNull(id, "id不能为空");
        return R.ok().put(setBacicOnlinepayService.queryObject(id));
    }

    @RequestMapping("/validBetMsg")
    @ApiOperation(value = "会员返水消息通知", notes = "会员返水消息通知")
    public void validBetMsg(@RequestParam("siteCode") String siteCode, @RequestParam("accountId") Integer accountId,
                            @RequestParam("acvitityMoney") BigDecimal acvitityMoney) {
        BizEvent bizEvent = new BizEvent(this, AESUtil.decrypt(siteCode), accountId, BizEventType.MEMBER_COMMISSION_SUCCESS);
        bizEvent.setAcvitityMoney(CommonUtil.adjustScale(acvitityMoney));
        applicationEventPublisher.publishEvent(bizEvent);
    }

    @RequestMapping("/updateMerchantPay")
    @ApiOperation(value = "代付状态更新", notes = "代付状态更新")
    public void updateMerchantPay(@RequestParam("siteCode") String siteCode) {
        log.info("收到BATCH代付状态更新通知【" + AESUtil.decrypt(siteCode) + "】");
        withdrawService.updateMerchantPayment(null, AESUtil.decrypt(siteCode));
    }

    @RequestMapping("/updateOprRecMbr")
    @ApiOperation(value = "已读过期站内消息更新", notes = "代付状态更新")
    public void updateOprRecMbr(@RequestParam("siteCode") String siteCode) {
        log.info("收到BATCH已读过期站内消息更新通知【" + AESUtil.decrypt(siteCode) + "】");
        oprRecMbrService.deleteOprRecMbr();
    }

    @RequestMapping("/auditAccount")
    @ApiOperation(value = "计算会员稽核", notes = "计算会员稽核")
    public void auditAccount(@RequestParam("siteCode") String siteCode) {
        log.info("收到BATCH开始计算会员稽核【" + AESUtil.decrypt(siteCode) + "】");
        String siteCodes = AESUtil.decrypt(siteCode);
        List<Integer> ids = auditCastService.findAuditAccountIds(null);
        if (Collections3.isNotEmpty(ids)) {
            ids.stream().forEach(id -> {
                String key = RedisConstants.AUDIT_ACCOUNT + siteCodes + id;
                Object object = redisService.getRedisValus(key);
                if (isNull(object)) {
                    try {
                        redisService.setRedisValue(key, id);
                        auditCastService.doingCronAuditAccount(siteCodes, id);
                    } finally {
                        redisService.del(key);
                    }
                }
            });
        }
    }

    @RequestMapping("/updateActivityState")
    @ApiOperation(value = "更新活动状态，优惠券状态", notes = "更新活动状态，优惠券状态")
    public void updateActivityState(@RequestParam("siteCode") String siteCode) {
        log.info("收到BATCH开始更新活动状态，优惠券状态【" + AESUtil.decrypt(siteCode) + "】");
        actActivityService.updateActivityState();
    }
}