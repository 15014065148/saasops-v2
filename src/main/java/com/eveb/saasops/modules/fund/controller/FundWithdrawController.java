package com.eveb.saasops.modules.fund.controller;

import com.eveb.saasops.common.annotation.SysLog;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.common.validator.Assert;
import com.eveb.saasops.listener.BizEvent;
import com.eveb.saasops.modules.base.controller.AbstractController;
import com.eveb.saasops.modules.fund.entity.AccWithdraw;
import com.eveb.saasops.modules.fund.entity.AgyWithdraw;
import com.eveb.saasops.modules.fund.service.FundWithdrawService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@RestController
@RequestMapping("/bkapi/fund/withdraw")
@Api(description = "会员提款,代理提款")
public class FundWithdrawController extends AbstractController {

    @Autowired
    private FundWithdrawService fundWithdrawService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @GetMapping("/accList")
    @RequiresPermissions("fund:accWithdraw:list")
    @ApiOperation(value = "会员提款查询列表", notes = "会员提款查询列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R accList(@ModelAttribute AccWithdraw accWithdraw, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        accWithdraw.setLoginSysUserName(getUser().getUsername());
        return R.ok().putPage(fundWithdrawService.queryAccListPage(accWithdraw, pageNo, pageSize));
    }

    @GetMapping("/accSumDrawingAmount")
    @ApiOperation(value = "会员提款今日取款", notes = "会员提款今日取款")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R accSumDrawingAmount() {
        return R.ok().put("sum", fundWithdrawService.accSumDrawingAmount(getUser().getUsername()));
    }

    @GetMapping("/accInfo/{id}")
    @RequiresPermissions("fund:accWithdraw:info")
    @ApiOperation(value = "会员提款查询(根据ID查询)", notes = "会员提款查询(根据ID查询)")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R accInfo(@PathVariable("id") Integer id) {
        return R.ok().put("accWithdraw", fundWithdrawService.queryAccObject(id));
    }

    @PostMapping("/updateAccStatusFinial")
    @RequiresPermissions("fund:accWithdraw:FinialUpdate")
    @SysLog(module = "会员提款复审-财务", methodText = "会员提款复审-财务")
    @ApiOperation(value = "会员提款修改(审核)状态", notes = "会员提款修改(审核)状态")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAccStatusFinial(@RequestBody AccWithdraw accWithdraw) {
        Assert.isNull(accWithdraw.getId(), "id不能为空");
        Assert.isNull(accWithdraw.getStatus(), "状态不能为空");
        BizEvent bizEvent = new BizEvent(this, CommonUtil.getSiteCode(), null, null);
        fundWithdrawService.updateAccStatus(accWithdraw, getUser().getUsername(), bizEvent);
        if (Objects.nonNull(bizEvent.getEventType())) {
            applicationEventPublisher.publishEvent(bizEvent);
        }
        return R.ok();
    }

    @PostMapping("/updateAccStatus")
    @RequiresPermissions("fund:accWithdraw:update")
    @SysLog(module = "会员提款-初审", methodText = "会员提款审核初审")
    @ApiOperation(value = "会员提款修改(审核)状态初审", notes = "会员提款修改(审核)状态初审")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAccStatus(@RequestBody AccWithdraw accWithdraw) {
        Assert.isNull(accWithdraw.getId(), "id不能为空");
        Assert.isNull(accWithdraw.getStatus(), "状态不能为空");
        BizEvent bizEvent = new BizEvent(this, CommonUtil.getSiteCode(), null, null);
        fundWithdrawService.checkoutStatusByTwo(accWithdraw.getId());
        fundWithdrawService.updateAccStatus(accWithdraw, getUser().getUsername(), bizEvent);
        if (Objects.nonNull(bizEvent.getEventType())) {
            applicationEventPublisher.publishEvent(bizEvent);
        }
        return R.ok();
    }

    @PostMapping("/updateMerchantPayment")
    @SysLog(module = "会员提款", methodText = "更新第三方代付状态")
    @ApiOperation(value = "更新第三方代付状态", notes = "更新第三方代付状态")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateMerchantPayment(@RequestParam("accountId") Integer accountId) {
        fundWithdrawService.updateMerchantPayment(accountId, CommonUtil.getSiteCode());
        return R.ok();
    }

    @PostMapping("/updateAccMemo")
    @RequiresPermissions("fund:accWithdraw:update")
    @SysLog(module = "会员提款", methodText = "会员提款修改备注")
    @ApiOperation(value = "会员提款修改备注", notes = "会员提款修改备注")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAccMemo(@RequestBody AccWithdraw accWithdraw) {
        Assert.isNull(accWithdraw.getId(), "id不能为空");
        fundWithdrawService.updateAccMemo(accWithdraw.getId(), accWithdraw.getMemo(), getUser().getUsername());
        return R.ok();
    }

    @GetMapping("/accExportExecl")
    @RequiresPermissions("fund:accWithdraw:exportExecl")
    @ApiOperation(value = "会员提款查询列表", notes = "会员提款查询列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public void accExportExecl(@ModelAttribute AccWithdraw accWithdraw, HttpServletResponse response) {
        fundWithdrawService.accExportExecl(accWithdraw, response);
    }


    @GetMapping("/agyList")
    @RequiresPermissions("fund:agyWithdraw:list")
    @ApiOperation(value = "代理提款查询列表", notes = "代理提款查询列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R agyList(@ModelAttribute AgyWithdraw agyWithdraw, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, @RequestParam("orderBy") @NotNull String orderBy) {
        return R.ok().putPage(fundWithdrawService.queryAgyListPage(agyWithdraw, pageNo, pageSize, orderBy));
    }

    @GetMapping("/agyInfo/{id}")
    @RequiresPermissions("fund:agyWithdraw:info")
    @ApiOperation(value = "代理提款查询(根据ID查询)", notes = "代理提款查询(根据ID查询)")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R agyInfo(@PathVariable("id") Integer id) {
        return R.ok().put("agyWithdraw", fundWithdrawService.queryAgyObject(id));
    }

    @PostMapping("/updateAgyStatus")
    @RequiresPermissions("fund:agyWithdraw:update")
    @SysLog(module = "代理提款", methodText = "代理提款审核")
    @ApiOperation(value = "代理提款修改状态(审核)", notes = "代理提款修改状态（审核）")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAgyStatus(@RequestBody AgyWithdraw agyWithdraw) {
        Assert.isNull(agyWithdraw.getId(), "id不能为空");
        Assert.isNull(agyWithdraw.getStatus(), "状态不能为空");
        fundWithdrawService.updateAgyStatus(agyWithdraw, getUser().getUsername());
        return R.ok();
    }

    @PostMapping("/updateAgyMemo")
    @RequiresPermissions("fund:agyWithdraw:update")
    @SysLog(module = "代理提款", methodText = "代理提款修改备注")
    @ApiOperation(value = "代理提款修改备注", notes = "代理提款修改备注")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAgyMemo(@RequestBody AgyWithdraw agyWithdraw) {
        Assert.isNull(agyWithdraw.getId(), "id不能为空");
        fundWithdrawService.updateAgyMemo(agyWithdraw.getId(), agyWithdraw.getMemo(), getUser().getUsername());
        return R.ok();
    }

    @GetMapping("/agyExportExecl")
    @RequiresPermissions("fund:accWithdraw:exportExecl")
    @SysLog(module = "代理提款", methodText = "代理提款导出数据")
    @ApiOperation(value = "代理提款导出数据", notes = "代理提款导出数据")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public void agyExportExecl(@ModelAttribute AgyWithdraw agyWithdraw, HttpServletResponse response) {
        fundWithdrawService.agyExportExecl(agyWithdraw, response);
    }

}
