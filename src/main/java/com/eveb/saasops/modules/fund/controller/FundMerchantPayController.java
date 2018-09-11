package com.eveb.saasops.modules.fund.controller;

import com.eveb.saasops.common.annotation.SysLog;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.modules.base.controller.AbstractController;
import com.eveb.saasops.modules.fund.entity.FundMerchantPay;
import com.eveb.saasops.modules.fund.entity.FundWhiteList;
import com.eveb.saasops.modules.fund.service.MerchantPayService;
import io.swagger.annotations.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;


@RestController
@RequestMapping("/bkapi/fund/deposit")
@Api(description = "代付相关接口")
public class FundMerchantPayController extends AbstractController {

    @Autowired
    private MerchantPayService merchantPayService;

    @Deprecated
    @GetMapping("/findFundWhiteListOne/{id}")
    @RequiresPermissions("member:fundWhiteList:info")
    @ApiOperation(value = "查询单个会员白名单信息", notes = "查询单个会员白名单信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findFundWhiteListOne(@PathVariable("id") Integer accountId) {
        return R.ok().put(merchantPayService.findFundWhiteList(accountId));
    }

    @Deprecated
    @PostMapping("/addFundWhiteList")
    @RequiresPermissions("member:fundWhiteList:add")
    @SysLog(module = "会员列表",methodText = "新增会员白名单")
    @ApiOperation(value = "新增会员白名单", notes = "新增会员白名单")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R addFundWhiteList(@RequestBody FundWhiteList whiteList) {
        whiteList.setCreateUser(getUser().getUsername());
        merchantPayService.addFundWhiteList(whiteList);
        return R.ok();
    }

    @Deprecated
    @GetMapping("/deleteFundWhiteList/{id}")
    @RequiresPermissions("member:fundWhiteList:delete")
    @SysLog(module = "会员列表",methodText = "会员白名单移除")
    @ApiOperation(value = "会员白名单移除", notes = "会员白名单移除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R deleteFundWhiteList(@PathVariable("id") Integer id) {
        merchantPayService.deleteFundWhiteList(id);
        return R.ok();
    }

    @GetMapping("/findFundMerchantPayListPage")
    @RequiresPermissions("merchant:fundMerchantPay:list")
    @ApiOperation(value = "出款管理分页查询", notes = "出款管理分页查询")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findFundMerchantPayListPage(@ModelAttribute FundMerchantPay merchantPay,
                                         @RequestParam("pageNo") @NotNull Integer pageNo,
                                         @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(merchantPayService.findFundMerchantPayList(merchantPay, pageNo, pageSize));
    }

    @PostMapping("/addFundMerchantPay")
    @RequiresPermissions("merchant:fundMerchantPay:save")
    @SysLog(module = "出款管理",methodText = "出款管理新增")
    @ApiOperation(value = "出款管理新增", notes = "出款管理新增")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R addFundMerchantPay(@RequestBody FundMerchantPay merchantPay) {
        merchantPay.setCreateUser(getUser().getUsername());
        merchantPay.setModifyUser(getUser().getUsername());
        merchantPayService.addFundMerchantPay(merchantPay);
        return R.ok();
    }

    @PostMapping("/updateFundMerchantPay")
    @RequiresPermissions("merchant:fundMerchantPay:update")
    @SysLog(module = "出款管理",methodText = "出款管理修改")
    @ApiOperation(value = "出款管理修改", notes = "出款管理修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateFundMerchantPay(@RequestBody FundMerchantPay merchantPay) {
        merchantPay.setModifyUser(getUser().getUsername());
        merchantPayService.updateFundMerchantPay(merchantPay);
        return R.ok();
    }

    @GetMapping("/deleteFundMerchantPay/{id}")
    @RequiresPermissions("merchant:fundMerchantPay:delete")
    @SysLog(module = "出款管理",methodText = "出款管理删除")
    @ApiOperation(value = "出款管理删除", notes = "出款管理删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R deleteFundMerchantPay(@PathVariable("id") Integer id) {
        merchantPayService.deleteFundMerchantPay(id);
        return R.ok();
    }

    @GetMapping("/updateFundMerchantPayAvailable")
    @RequiresPermissions("merchant:fundMerchantPay:update")
    @SysLog(module = "出款管理",methodText = "出款管理修改状态")
    @ApiOperation(value = "出款管理修改状态", notes = "出款管理修改状态")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateFundMerchantPayAvailable(@ModelAttribute FundMerchantPay merchantPay) {
        merchantPay.setModifyUser(getUser().getUsername());
        merchantPayService.updateFundMerchantPayAvailable(merchantPay);
        return R.ok();
    }

    @GetMapping("/findFundMerchantPayOne/{id}")
    @RequiresPermissions("merchant:fundMerchantPay:info")
    @ApiOperation(value = "出款管理查询单个", notes = "出款管理查询单个")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findFundMerchantPayOne(@PathVariable("id") Integer id) {
        return R.ok().put(merchantPayService.findFundMerchantPayOne(id));
    }

    @GetMapping("/findTChannelPayList")
    @ApiOperation(value = "查询所有代付渠道", notes = "查询所有代付渠道")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findTChannelPayList() {
        return R.ok().put(merchantPayService.findTChannelPayList());
    }

}
