package com.eveb.saasops.modules.fund.controller;

import com.eveb.saasops.common.annotation.SysLog;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.validator.Assert;
import com.eveb.saasops.modules.base.controller.AbstractController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.eveb.saasops.modules.fund.entity.FundDeposit;
import com.eveb.saasops.modules.fund.service.FundDepositService;
import com.eveb.saasops.common.utils.R;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/bkapi/fund/deposit")
@Api(description = "线上入款,公司入款")
public class FundDepositController extends AbstractController {

    @Autowired
    private FundDepositService fundDepositService;

    @GetMapping("/list")
    @RequiresPermissions("fund:onLine:list")
    @ApiOperation(value = "线上入款查询列表", notes = "线上入款查询列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            ,@ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R list(@ModelAttribute FundDeposit fundDeposit, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        fundDeposit.setLoginSysUserName(getUser().getUsername());
        fundDeposit.setMark(Constants.EVNumber.zero);
        return R.ok().putPage(fundDepositService.queryListPage(fundDeposit, pageNo, pageSize));
    }

    @GetMapping("/depositList")
    @RequiresPermissions("fund:onLine:list")
    @ApiOperation(value = "会员入款查询列表", notes = "会员入款查询列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            ,@ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R depositList(@ModelAttribute FundDeposit fundDeposit, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(fundDepositService.queryListPage(fundDeposit, pageNo, pageSize));
    }

    @GetMapping("/sumDepositAmount")
    @ApiOperation(value = "线上（公司）入款今日存款", notes = "线上（公司）入款今日存款")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R sumDepositAmount(@ModelAttribute FundDeposit fundDeposit,@RequestParam("make") Integer make) {
        fundDeposit.setMark(make);
        fundDeposit.setLoginSysUserName(getUser().getUsername());
        return R.ok().put("sum", fundDepositService.findSumDepositAmount(fundDeposit));
    }

    @GetMapping("/info/{id}")
    @RequiresPermissions("fund:onLine:info")
    @ApiOperation(value = "线上入款查询(根据ID)", notes = "线上入款查询(根据ID)")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R info(@PathVariable("id") Integer id) {
        FundDeposit fundDeposit = fundDepositService.queryObject(id);
        return R.ok().put("fundDeposit", fundDeposit);
    }

    @PostMapping("/updateStatus")
    @RequiresPermissions("fund:onLine:update")
    @SysLog(module = "线上入款,公司入款",methodText = "线上入款or公司入款审核")
    @ApiOperation(value = "线上入款（公司入款）修改状态(审核)", notes = "线上入款（公司入款）修改状态(审核)")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R onLineUpdate(@RequestBody FundDeposit deposit) {
        Assert.isNull(deposit.getId(), "id不能为空");
        Assert.isNull(deposit.getStatus(), "状态不能为空");
        Assert.isLenght(deposit.getMemo(), "备注长度为1-400!", 1, 400);
        FundDeposit fundDeposit = fundDepositService.updateDeposit(deposit, getUser().getUsername());
        fundDepositService.accountDepositMsg(fundDeposit, CommonUtil.getSiteCode());
        return R.ok();
    }

    @GetMapping("/onLine/exportExecl")
    @ApiOperation(value = "导出线上入款数据", notes = "导出线上入款数据")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public void onLineExportExecl(@ModelAttribute FundDeposit deposit, HttpServletResponse response) {
        deposit.setMark(Constants.EVNumber.zero);
        fundDepositService.depositExportExecl(deposit, Boolean.TRUE, response);
    }

    @PostMapping("updateMemo")
    @RequiresPermissions("fund:onLine:update")
    @SysLog(module = "线上入款,公司入款",methodText = "线上入款or公司入款修改备注")
    @ApiOperation(value = "线上入款（公司入款）修改备注", notes = "线上入款（公司入款）修改备注")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateMemo(@RequestBody @Valid FundDeposit deposit) {
        Assert.isNull(deposit.getId(), "id不能为空");
        Assert.isLenght(deposit.getMemo(), "备注长度为1-400!", 1, 400);
        fundDepositService.updateDepositMemo(deposit, getUser().getUsername());
        return R.ok();
    }

    @GetMapping("/companyList")
    @RequiresPermissions("fund:company:list")
    @ApiOperation(value = "公司入款列表", notes = "公司入款列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R companyList(@ModelAttribute FundDeposit fundDeposit, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        fundDeposit.setMark(Constants.EVNumber.one);
        return R.ok().putPage(fundDepositService.queryListPage(fundDeposit, pageNo, pageSize));
    }

    @GetMapping("/companyInfo/{id}")
    @RequiresPermissions("fund:company:info")
    @ApiOperation(value = "公司入款信息（根据ID）", notes = "公司入款信息（根据ID）")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R companyInfo(@PathVariable("id") Integer id) {
        FundDeposit fundDeposit = fundDepositService.queryObject(id);
        return R.ok().put("fundDeposit", fundDeposit);
    }

    @GetMapping("/company/exportExecl")
    @RequiresPermissions("fund:company:exportExecl")
    @SysLog(module = "公司入款",methodText = "导出公司入款数据")
    @ApiOperation(value = "导出公司入款数据", notes = "导出公司入款数据")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public void companyExportExecl(@ModelAttribute FundDeposit fundDeposit, HttpServletResponse response) {
        fundDeposit.setMark(Constants.EVNumber.one);
        fundDepositService.depositExportExecl(fundDeposit, Boolean.FALSE, response);
    }

    @GetMapping("/listCount")
    @ApiOperation(value = "统计列表", notes = "统计列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R listCount() {
        return R.ok().putPage(fundDepositService.listCount());
    }
}
