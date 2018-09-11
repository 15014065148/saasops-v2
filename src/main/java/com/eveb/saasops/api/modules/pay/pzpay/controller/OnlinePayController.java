package com.eveb.saasops.api.modules.pay.pzpay.controller;

import com.eveb.saasops.api.annotation.Login;
import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.modules.pay.pzpay.dto.DepositListDto;
import com.eveb.saasops.api.modules.pay.pzpay.entity.PzpayPayParams;
import com.eveb.saasops.api.modules.pay.pzpay.service.OnlinePayService;
import com.eveb.saasops.api.utils.HttpsRequestUtil;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.constants.SystemConstants;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.common.utils.SnowFlake;
import com.eveb.saasops.common.validator.Assert;
import com.eveb.saasops.modules.fund.dto.ZhiFuRequestDto;
import com.eveb.saasops.modules.fund.dto.ZhiFuResponseDto;
import com.eveb.saasops.modules.fund.entity.FundDeposit;
import com.eveb.saasops.modules.fund.entity.TOpPay;
import com.eveb.saasops.modules.fund.service.FundDepositService;
import com.eveb.saasops.modules.fund.service.ZhiFuPayService;
import com.eveb.saasops.modules.operate.service.OprActActivityCastService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.isNull;

/**
 * Created by William on 2017/12/6.
 */
@RestController
@RequestMapping("/api/OnlinePay/pzPay")
@Api(value = "OnlinePay", description = "在线充值，支付")
@Slf4j
public class OnlinePayController {

    @Autowired
    private OnlinePayService onlinePayService;
    @Autowired
    private FundDepositService fundDepositService;
    @Autowired
    OprActActivityCastService oprActActivityCastService;
    @Autowired
    private ZhiFuPayService zhiFuPayService;


    @Login
    @GetMapping("/payUrl")
    @ApiOperation(value = "线上支付，充值", notes = "线上支付，充值")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R getPayUrl(@ModelAttribute PzpayPayParams pzpayPayParams, HttpServletRequest request) {
        Long outTradeNo = new SnowFlake().nextId();
        log.debug("outTradeNo: {}", outTradeNo);
        //6212262102013209178
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        pzpayPayParams.setAccountId(userId);
        boolean payPermit = onlinePayService.judgeSaveConditions(pzpayPayParams.getFee(), pzpayPayParams.getPayType());
        if (!payPermit) {
            return R.error(2000, "非正常操作").put("2000", "用户充值金额不在允许范围");
        }
        //如果有勾选活动，需要验证活动
        if (!StringUtils.isEmpty(pzpayPayParams.getActivityId())) {
            oprActActivityCastService.checkoutApplyActivity(userId, pzpayPayParams.getActivityId(), pzpayPayParams.getFee(), Boolean.TRUE);
        }
        pzpayPayParams.setOutTradeNo(outTradeNo);
        pzpayPayParams.setIp(CommonUtil.getIpAddress(request));
        //获取入款的客户端来源
        String dev = request.getHeader("dev");
        pzpayPayParams.setFundSource(HttpsRequestUtil.getHeaderOfDev(dev));
        Map<String, Object> paramr = onlinePayService.optionPayment(CommonUtil.getSiteCode(), pzpayPayParams);
        if (Boolean.FALSE.equals(paramr.get("status"))) {
            return R.error(2000, paramr.get("message").toString());
        }
        return R.ok().put("res", paramr);
    }

    @Login
    @GetMapping("/payResult")
    @ApiOperation(value = "获取充值结果，通过订单号,并更新用户钱包", notes = "获取充值结果，通过订单号,并更新用户钱包")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"), @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R getPayResult(HttpServletRequest request) {
        FundDeposit fundDeposit = new FundDeposit();
        fundDeposit.setAccountId((Integer) request.getAttribute(ApiConstants.USER_ID));
        fundDeposit.setStatus(2);
        List<FundDeposit> fundDeposits = fundDepositService.selectList(fundDeposit);
        onlinePayService.getPayResult(fundDeposits, CommonUtil.getSiteCode());
        return R.ok();
    }


    @Login
    @GetMapping("/getPzpayPictureUrl/{accountId}")
    @ApiOperation(value = "获取支付类型相对的支付方式及图片路径 ", notes = "获取支付类型相对的支付方式及图片路径")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "dev", value = "dev", required = true, dataType = "byte", paramType = "header")})
    public R getPzpayPictureUrl(@ModelAttribute TOpPay tOpPay, HttpServletRequest request) {
        return R.ok().put("res", onlinePayService.getPzpayPictureUrl(tOpPay, request));
    }

    @Login
    @GetMapping("/getFundDepositList")
    @ApiOperation(value = "会员充值的记录线上入款 ", notes = "会员充值的记录线上入款")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getFundDepositOnLine(@ModelAttribute DepositListDto fundDeposit,
                                  @RequestParam("pageNo") @NotNull Integer pageNo,
                                  @RequestParam("pageSize") @NotNull Integer pageSize,
                                  @ApiParam("排序字段") @RequestParam(value = "orderBy", required = false) String orderBy,
                                  HttpServletRequest request) {
        Assert.isNull(fundDeposit.getMark(), "充值类型不能为空!");
        if (fundDeposit.getMark() != Constants.EVNumber.zero
                && fundDeposit.getMark() != Constants.EVNumber.one
                && fundDeposit.getMark() != Constants.EVNumber.two
                && fundDeposit.getMark() != Constants.EVNumber.three) {
            throw new RRException("充值类型选择错误!");
        }
        fundDeposit.setAccountId((Integer) request.getAttribute(ApiConstants.USER_ID));
        R r = R.ok().put("res", fundDepositService.queryListPage(fundDeposit, pageNo, pageSize, orderBy));
        Double taltal = fundDepositService.findDepositSum(fundDeposit);
        r.put("totalCount", isNull(taltal) ? 0.00d : taltal);
        return r;
    }

    @Deprecated
    @Login
    @GetMapping("/getMbFundDepositList")
    @ApiOperation(value = "会员充值的记录线上入款手机端 ", notes = "会员充值的记录线上入款手机端")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getMbFundDepositList(@ModelAttribute DepositListDto fundDeposit, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, @ApiParam("排序字段") @RequestParam(value = "orderBy", required = false) String orderBy, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        fundDeposit.setAccountId(userId);
        return R.ok().put("res", fundDepositService.queryDepositAndOtherListPage(fundDeposit, pageNo, pageSize, orderBy));
    }


    @PostMapping("/zhiFuCallback")
    public ZhiFuResponseDto zhiFuCallback(@ModelAttribute ZhiFuRequestDto requestDto, HttpServletRequest request) {
        request.setAttribute(SystemConstants.SCHEMA_NAME, requestDto.getMemo());
        return zhiFuPayService.zhiFuCallback(requestDto);
    }
}
