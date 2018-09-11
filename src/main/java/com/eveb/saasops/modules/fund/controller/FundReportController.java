package com.eveb.saasops.modules.fund.controller;

import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.modules.transfer.dto.BillRequestDto;
import com.eveb.saasops.api.modules.transfer.service.TransferService;
import com.eveb.saasops.common.annotation.SysLog;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.constants.Constants.Available;
import com.eveb.saasops.common.constants.OrderConstants;
import com.eveb.saasops.common.exception.R200Exception;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.common.validator.Assert;
import com.eveb.saasops.modules.base.controller.AbstractController;
import com.eveb.saasops.modules.fund.entity.FundAudit;
import com.eveb.saasops.modules.fund.service.FundReportService;
import com.eveb.saasops.modules.member.dto.BillRecordDto;
import com.eveb.saasops.modules.member.entity.MbrBillManage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/bkapi/fund/report")
@Api(description = "调整报表,转账报表")
public class FundReportController extends AbstractController {

    @Autowired
    private FundReportService fundReportService;
    @Autowired
    private TransferService transferService;

    @GetMapping("/billList")
    @RequiresPermissions("fund:billReport:list")
    @ApiOperation(value = "转账报表查询列表", notes = "转账报表查询列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R billList(@ModelAttribute MbrBillManage mbrBillManage, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        mbrBillManage.setLoginSysUserName(getUser().getUsername());
        return R.ok().putPage(fundReportService.queryListPage(mbrBillManage, pageNo, pageSize));
    }

    @GetMapping("/billInfo/{id}")
    @RequiresPermissions("fund:billReport:info")
    @ApiOperation(value = "转账报表查询(根据ID)", notes = "转账报表查询(根据ID)")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R billInfo(@PathVariable("id") Integer id) {
        return R.ok().put("info", fundReportService.queryObject(id));
    }

    @PostMapping("/updateBillMemo")
    @RequiresPermissions("fund:billReport:update")
    @SysLog(module = "转账报表", methodText = "转账报表修改备注")
    @ApiOperation(value = "转账报表修改备注", notes = "转账报表修改备注")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateBillMemo(@RequestBody MbrBillManage mbrBillManage) {
        Assert.isNull(mbrBillManage.getId(), "id不能为空");
        mbrBillManage.setModifyUser(getUser().getUsername());
        fundReportService.updateBillMemo(mbrBillManage);
        return R.ok();
    }

    @GetMapping("/billExportExecl")
    @RequiresPermissions("fund:company:exportExecl")
    @SysLog(module = "转账报表", methodText = "导出转账报表数据")
    @ApiOperation(value = "导出转账报表数据", notes = "导出转账报表数据")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public void billReportExportExecl(@ModelAttribute MbrBillManage mbrBillManage, HttpServletResponse response) {
        fundReportService.billReportExportExecl(mbrBillManage, response);
    }

    @PostMapping("/auditSave")
    @RequiresPermissions("fund:audit:save")
    @SysLog(module = "调整报表", methodText = "调整报表新增")
    @ApiOperation(value = "调整报表新增", notes = "调整报表新增")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R auditSave(@RequestBody FundAudit fundAudit) {
        Assert.isNotEmpty(fundAudit.getIds(), "会员不能为空");
        Assert.isBlank(fundAudit.getFinancialCode(), "FinancialCode不能为空");
        if (OrderConstants.FUND_ORDER_CODE_AA.equals(fundAudit.getFinancialCode())) {
            Assert.isNull(fundAudit.getDepositType(), "存款类型不能为空");
            if (fundAudit.getDepositType() == Constants.EVNumber.two && Objects.isNull(fundAudit.getActivityId())) {
                throw new R200Exception("请选择优惠活动");
            }
        }
        Assert.isNumeric(fundAudit.getAmount(), "调整金额只能为数字,并且长度不能大于12位!", 12);
        Assert.isLenght(fundAudit.getMemo(), "备注长度为1-100!", 1, 100);
        if (!StringUtils.isEmpty(fundAudit.getAuditType()) && fundAudit.getAuditType() == Available.enable) {
            Assert.isNumericInterregional(fundAudit.getAuditMultiple(), "稽核倍数只能为1-100!", 1d, 100d);
        }
        fundAudit.setCreateUser(getUser().getUsername());
        fundAudit.setModifyUser(getUser().getUsername());
        fundReportService.auditSave(fundAudit);
        return R.ok();
    }

    @GetMapping("/auditList")
    @RequiresPermissions("fund:audit:list")
    @ApiOperation(value = "调整报表查询列表", notes = "调整报表查询列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R auditList(@ModelAttribute FundAudit fundAudit, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        fundAudit.setLoginSysUserName(getUser().getUsername());
        return R.ok().putPage(fundReportService.queryAuditListPage(fundAudit, pageNo, pageSize));
    }

    @GetMapping("/auditInfo/{id}")
    @RequiresPermissions("fund:audit:info")
    @ApiOperation(value = "调整报表查询(根据ID)", notes = "调整报表查询(根据ID)")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R auditInfo(@PathVariable("id") Integer id) {
        return R.ok().put("info", fundReportService.queryAuditObject(id));
    }

    @PostMapping("/auditUpdateStatus")
    @RequiresPermissions("fund:audit:update")
    @SysLog(module = "调整报表", methodText = "调整报表审核")
    @ApiOperation(value = "调整报表修改状态", notes = "调整报表修改状态")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R auditUpdateStatus(@RequestBody FundAudit fundAudit) {
        Assert.isNull(fundAudit.getId(), "id不能为空");
        Assert.isNull(fundAudit.getStatus(), "状态不能为空");
        fundAudit.setModifyUser(getUser().getUsername());
        fundReportService.auditUpdateStatus(fundAudit, CommonUtil.getSiteCode());
        return R.ok();
    }

    @PostMapping("/auditUpdateMemo")
    @RequiresPermissions("fund:audit:update")
    @SysLog(module = "调整报表", methodText = "调整报表修改备注")
    @ApiOperation(value = "调整报表修改备注", notes = "调整报表修改备注")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R auditUpdateMemo(@RequestBody FundAudit fundAudit) {
        Assert.isNull(fundAudit.getId(), "id不能为空");
        fundReportService.auditUpdateMemo(fundAudit);
        return R.ok();
    }

    @GetMapping("/apiTrfRefresh")
    @RequiresPermissions("fund:audit:update")
    @SysLog(module = "转账报表", methodText = "会员第三方接口订单状态查询")
    @ApiOperation(value = "会员第三方接口订单状态查询", notes = "会员第三方接口订单状态查询")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R apiTrfRefresh(@RequestParam("orderNo") Long orderNo) {
        Assert.isNull(orderNo, "订单号不能为空!");
        return transferService.checkTransfer(orderNo, CommonUtil.getSiteCode());
    }

    @GetMapping("/auditExportExecl")
    @RequiresPermissions("fund:audit:exportExecl")
    @SysLog(module = "调整报表", methodText = "导出调整报表")
    @ApiOperation(value = "导出调整报表", notes = "导出调整报表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public void auditExportExecl(@RequestBody FundAudit fundAudit, HttpServletResponse response) {
        fundReportService.auditExportExecl(fundAudit, response);
    }

    @GetMapping("/queryDepotOrAccountBalance")
    @SysLog(module = "转账报表->新增", methodText = "获取主账户余额及平台余额")
    @ApiOperation(value = "获取主账户余额及平台余额", notes = "获取主账户余额及平台余额")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryDepotOrAccountBalance(@ModelAttribute MbrBillManage mbrBillManage) {
        return R.ok().put("info", fundReportService.queryDepotOrAccountBalance(mbrBillManage, CommonUtil.getSiteCode()));
    }

    @GetMapping("/queryAccountBalance")
    @SysLog(module = "转账报表->新增", methodText = "获取主账户余额")
    @ApiOperation(value = "获取主账户余额", notes = "获取主账户余额")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryAccountBalance(@RequestParam("loginName") String loginName) {
        return R.ok().put("info", fundReportService.queryAccountBalance(loginName));
    }

    @GetMapping("/createOrderNumber")
    @SysLog(module = "转账报表->新增", methodText = "生成转账单号")
    @ApiOperation(value = "生成转账单号", notes = "生成转账单号")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R createOrderNumber() {
        return R.ok().put(fundReportService.createOrderNumber());
    }

    @PostMapping("/save")
    @SysLog(module = "转账报表->新增", methodText = "创建报表")
    @ApiOperation(value = "保存", notes = "保存")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R save(@RequestBody BillRequestDto requestDto, HttpServletRequest request) {
        Assert.isNull(requestDto.getDepotId(), "平台ID不能为空！");
        Assert.isPInt(requestDto.getAmount(), "转账金额只能为正整数");
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        requestDto.setAccountId(userId);
        requestDto.setIp(CommonUtil.getIpAddress(request));
        requestDto.setTransferSource((byte) 2);
        fundReportService.save(requestDto, CommonUtil.getSiteCode());
        return R.ok();
    }

    @GetMapping("/billRecordList")
    @RequiresPermissions("fund:billReport:list")
    @ApiOperation(value = "账变流水查询", notes = "账变流水查询")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R billRecordList(@ModelAttribute BillRecordDto billRecordDto, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(fundReportService.queryBillRecordListPage(billRecordDto, pageNo, pageSize));
    }
}
