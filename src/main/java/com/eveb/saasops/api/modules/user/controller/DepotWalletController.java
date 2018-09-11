package com.eveb.saasops.api.modules.user.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import com.eveb.saasops.api.modules.user.service.AginService;
import com.eveb.saasops.common.exception.RRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eveb.saasops.api.annotation.Login;
import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.modules.apisys.entity.TCpSite;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.apisys.service.TGmApiService;
import com.eveb.saasops.api.modules.user.dto.TransferRequestDto;
import com.eveb.saasops.api.modules.user.service.DepotWalletService;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.common.validator.Assert;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.Objects;


@RestController
@RequestMapping("/api/depotWallet")
@Api(description = "平台服务控制器")
public class DepotWalletController {

    @Autowired
    private DepotWalletService depotWalletService;
    @Autowired
    private TGmApiService gmApiService;
    @Autowired
    private AginService ag;

    @Login
    @GetMapping("/depotBalance")
    @ApiOperation(value = "平台会员余额", notes = "平台会员余额")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R depotBalance(HttpServletRequest request, @RequestParam("depotId") Integer depotId) {
        Assert.isNumeric(depotId, "平台Id不能为空");
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        TGmApi gmApi = gmApiService.queryApiObject(depotId, cpSite.getSiteCode());
        if (Objects.isNull(gmApi)) throw new RRException("无此API线路!");
        return R.ok().put(depotWalletService.findDepotBalance(userId, gmApi)).put("depotId", depotId);
    }

    @Login
    @GetMapping("/transferList")
    @ApiOperation(value = "平台会员转账记录", notes = "平台会员转账记录")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findTransferList(@ModelAttribute TransferRequestDto requestDto, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        requestDto.setAccountId(userId);
        return R.ok().put(depotWalletService.findTransferList(requestDto, pageNo, pageSize));
    }

}
