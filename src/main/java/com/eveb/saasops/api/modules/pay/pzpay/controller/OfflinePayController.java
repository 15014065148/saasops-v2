package com.eveb.saasops.api.modules.pay.pzpay.controller;

import javax.servlet.http.HttpServletRequest;

import com.eveb.saasops.api.utils.HttpsRequestUtil;
import com.eveb.saasops.modules.operate.service.OprActActivityCastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eveb.saasops.api.annotation.Login;
import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.modules.pay.pzpay.entity.PzpayPayParams;
import com.eveb.saasops.api.modules.pay.pzpay.service.OfflinePayService;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.common.utils.SnowFlake;
import com.eveb.saasops.common.validator.Assert;
import com.eveb.saasops.modules.member.entity.MbrDepositCond;
import com.eveb.saasops.modules.member.service.MbrDepositCondService;
import com.eveb.saasops.modules.system.cmpydeposit.entity.SysDeposit;
import com.eveb.saasops.modules.system.cmpydeposit.service.SysDepositService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/OfflinePay")
@Api(value = "OfflinePay", description = "线下支付")
@Slf4j
public class OfflinePayController {

    @Autowired
    private SysDepositService sysDepositService;
    @Autowired
    private OfflinePayService offlinePayService;
    @Autowired
    MbrDepositCondService mbrDepositCondService;
    @Autowired
    OprActActivityCastService oprActActivityCastService;

    @GetMapping("/bankList")
    @ApiOperation(value = "公司入款可入款的银行", notes = "公司入款可入款的银行")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "dev", value = "PC/H5", required = true, dataType = "String", paramType = "header")})
    @Login
    public R bankList(HttpServletRequest request) {
        return R.ok().put("bankCards", sysDepositService.queryAccCondList(request));
    }

    @PostMapping("/applyPay")
    @ApiOperation(value = "公司入款支付申请", notes = "公司入款支付申请")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R applyPay(@RequestBody PzpayPayParams pzpayPayParams, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        Assert.isNull(pzpayPayParams.getBankCardId(), "入款银行卡不能为空!");
        Assert.isNull(pzpayPayParams.getFee(), "入款金额不能为空!");
        pzpayPayParams.setAccountId(userId);
        pzpayPayParams.setIp(CommonUtil.getIpAddress(request));
        //增加设备来源
        String dev=request.getHeader("dev");
        Byte devSource=HttpsRequestUtil.getHeaderOfDev(dev);
        MbrDepositCond mbrDepositCond = mbrDepositCondService.getMbrDeposit(userId);
        SysDeposit sysDeposit = offlinePayService.offlinePayVerify(pzpayPayParams, mbrDepositCond);
        sysDeposit.setDevSource(devSource+"");
        //如果有勾选活动，需要验证活动
        if (!StringUtils.isEmpty(pzpayPayParams.getActivityId())) {
            oprActActivityCastService.checkoutApplyActivity(userId, pzpayPayParams.getActivityId(), pzpayPayParams.getFee(), Boolean.TRUE);
        }
        Long outTradeNo = new SnowFlake().nextId();
        log.debug("outTradeNo: {}", outTradeNo);
        pzpayPayParams.setOutTradeNo(outTradeNo);
        //保存入款信息
        return R.ok().put("info", offlinePayService.saveFundDespoit(pzpayPayParams, sysDeposit, mbrDepositCond));
    }
}
