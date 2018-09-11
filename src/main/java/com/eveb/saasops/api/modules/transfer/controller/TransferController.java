package com.eveb.saasops.api.modules.transfer.controller;

import javax.servlet.http.HttpServletRequest;

import com.eveb.saasops.api.modules.transfer.dto.TransferAmoutDto;
import com.eveb.saasops.api.modules.user.service.RedisService;
import com.eveb.saasops.common.utils.Collections3;
import com.eveb.saasops.common.constants.RedisConstants;
import com.eveb.saasops.modules.member.dto.AuditBonusDto;
import com.eveb.saasops.modules.member.dto.DepotFailDto;
import com.eveb.saasops.modules.member.service.MbrDepotAsyncWalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eveb.saasops.api.annotation.Login;
import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.modules.apisys.entity.TCpSite;
import com.eveb.saasops.api.modules.transfer.dto.BillRequestDto;
import com.eveb.saasops.api.modules.transfer.service.TransferService;
import com.eveb.saasops.api.utils.HttpsRequestUtil;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.common.validator.Assert;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.entity.MbrWallet;
import com.eveb.saasops.modules.member.service.MbrWalletService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pay")
@Api(description = "会员转账")
public class TransferController {

    @Autowired
    private TransferService transferService;
    @Autowired
    private MbrWalletService mbrWalletService;
    @Autowired
    private MbrDepotAsyncWalletService mbrDepotAsyncWalletService;
    @Autowired
    private RedisService redisService;

    @Login
    @PostMapping("/transferIn")
    @ApiOperation(value = "会员转账->转入", notes = "会员转账->转入")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R TransferIn(@RequestBody BillRequestDto requestDto, HttpServletRequest request) {
        Assert.isPInt(requestDto.getAmount(), "转账金额只能为正整数");
        Assert.isNull(requestDto.getDepotId(), "平台ID不能为空");
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        requestDto.setAccountId(userId);
        requestDto.setLoginName(loginName);
        requestDto.setIp(CommonUtil.getIpAddress(request));
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        getTransSource(requestDto, request);
        String transferInCont = RedisConstants.SEESION_TRANSFERIN + requestDto.getDepotId()
                + requestDto.getAccountId() + "_" + cpSite.getSiteCode();
        if (Objects.nonNull(redisService.getRedisValus(transferInCont))) return R.error();
        redisService.setRedisValue(transferInCont, requestDto.getDepotId());
        TransferAmoutDto amoutDto;
        try {
            amoutDto = transferService.TransferIn(requestDto, cpSite.getSiteCode());
        } finally {
            redisService.del(transferInCont);
        }
        return R.ok().put(amoutDto);
    }

    @Deprecated
    @Login
    @GetMapping("/isTransferAmount")
    @ApiOperation(value = "是否转出平台余额", notes = "是否转出平台余额")
    public R TransferIn(@RequestParam("depotId") Integer depotId, HttpServletRequest request) {
        Assert.isNull(depotId, "平台ID不能为空");
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        return R.ok().put(transferService.isTransferAmount(depotId, userId, loginName, CommonUtil.getSiteCode()));
    }

    @Login
    @PostMapping("/transferOut")
    @ApiOperation(value = "会员转账->转出", notes = "会员转账->转出")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R TransferOut(@RequestBody BillRequestDto requestDto, HttpServletRequest request) {
        Assert.isPInt(requestDto.getAmount(), "转账金额只能为正整数");
        Assert.isNull(requestDto.getDepotId(), "平台ID不能为空");
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        requestDto.setAccountId(userId);
        requestDto.setLoginName(loginName);
        requestDto.setIp(CommonUtil.getIpAddress(request));
        getTransSource(requestDto, request);
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        AuditBonusDto auditBonusDto = transferService.TransferOut(requestDto, cpSite.getSiteCode());
        if (Boolean.FALSE.equals(auditBonusDto.getIsSucceed()) || Boolean.FALSE.equals(auditBonusDto.getIsFraud())) {
            return R.error().put(auditBonusDto);
        }
        return R.ok().put(auditBonusDto);
    }

    @Login
    @GetMapping("/checkTransfer")
    @ApiOperation(value = "会员转账单查询", notes = "会员转账单查询")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R checkTransfer(@RequestParam("orderNo") Long orderNo, HttpServletRequest request) {
        Assert.isNull(orderNo, "订单号不能为空");
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        return transferService.checkTransfer(orderNo, cpSite.getSiteCode());
    }

    @GetMapping("/recoverBalance")
    @ApiOperation(value = "会员第三方账号余额回收", notes = "会员第三方账号余额回收")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R recoverBalance(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        MbrDepotWallet wallet = new MbrDepotWallet();
        wallet.setAccountId(userId);
        wallet.setLoginName(loginName);
        String ip = CommonUtil.getIpAddress(request);
        //获取客户端来源
        String dev = request.getHeader("dev");
        Byte transferSource = HttpsRequestUtil.getHeaderOfDev(dev);
        List<MbrDepotWallet> depotWallets = mbrWalletService.getDepotWallet(wallet);
        if (Collections3.isEmpty(depotWallets)) {
            return R.ok("没有每三方账号需要回收余额!");
        }
        List<CompletableFuture<DepotFailDto>> recoverBalanceList = new ArrayList<>();
        depotWallets.forEach(e1 -> {
            recoverBalanceList.add(mbrDepotAsyncWalletService.getAsyncRecoverBalance(e1.getDepotId(), e1, CommonUtil.getSiteCode(), ip, transferSource, Boolean.TRUE));
        });
        List<DepotFailDto> failList = recoverBalanceList.stream().map(CompletableFuture::join).filter(e -> e.getFailError() == Boolean.FALSE).collect(Collectors.toList());
        if (failList.size() > 0) {
            return R.halfError(failList.stream().map(e -> String.valueOf(e.getDepotId())).collect(Collectors.joining(",")),
                    failList.stream().map(e -> String.valueOf(e.getIsSign())).collect(Collectors.joining(",")));
        }
        return R.ok("回收第三方账户余额成功!");
    }

    @PostMapping("/checkBalance")
    @ApiOperation(value = "平台会员余额查询", notes = "平台会员余额查询")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R balance(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        MbrWallet mbrWallet = mbrWalletService.getBalance(userId);
        return R.ok().put("balance", mbrWallet.getBalance());
    }

    /**
     * 获取转账的客户端来源
     *
     * @param requestDto
     * @param request
     */
    private void getTransSource(@RequestBody BillRequestDto requestDto, HttpServletRequest request) {
        String dev = request.getHeader("dev");
        Byte transferSource = HttpsRequestUtil.getHeaderOfDev(dev);
        requestDto.setTransferSource(transferSource);
    }
}
