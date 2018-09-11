package com.eveb.saasops.modules.agent.controller;

import com.eveb.saasops.common.annotation.SysLog;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.common.validator.Assert;
import com.eveb.saasops.modules.base.controller.AbstractController;
import com.eveb.saasops.modules.agent.entity.Commission;
import com.eveb.saasops.modules.agent.entity.ComnCharge;
import com.eveb.saasops.modules.agent.service.CommissionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.List;


@RestController
@RequestMapping("/bkapi/agent/commn")
@Api(description = "佣金方案")
public class CommissionController extends AbstractController {

    @Autowired
    private CommissionService commissionService;

    @GetMapping("findCommissionAll")
    @ApiOperation(value = "查找所有的佣金方案", notes = "查找所有的佣金方案")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findCommissionAll() {
        return R.ok().put("commissions", commissionService.findCommissionList(new Commission()));
    }

    @PostMapping("/save")
    @RequiresPermissions("agent:commission:save")
    @SysLog(module = "佣金方案",methodText = "佣金方案新增")
    @ApiOperation(value = "佣金方案新增", notes = "佣金方案新增")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R commissionInsert(@RequestBody Commission commissions) {
        commissionService.commissionInsert(commissions);
        return R.ok();
    }

    @PostMapping("/update")
    @RequiresPermissions("agent:commission:update")
    @SysLog(module = "佣金方案",methodText = "佣金方案修改")
    @ApiOperation(value = "佣金方案修改", notes = "佣金方案修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R commissionUpdate(@RequestBody Commission commissions) {
        commissionService.commissionUpdate(commissions);
        return R.ok();
    }

    @GetMapping("/list")
    @RequiresPermissions("agent:commission:list")
    @ApiOperation(value = "佣金方案列表", notes = "佣金方案列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R commissionPagingData(@ModelAttribute Commission record, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put("page", commissionService.findCommissionListPage(record, pageNo, pageSize));
    }

    @PostMapping("/updateAvailable")
    @RequiresPermissions("agent:commission:update")
    @SysLog(module = "佣金方案",methodText = "佣金方案状态修改")
    @ApiOperation(value = "佣金方案状态修改", notes = "佣金方案状态修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAvailable(@RequestBody Commission record) {
        Assert.isNull(record.getId(), "id不能为空");
        Assert.isNull(record.getAvailable(), "状态不能为空");
        record.setModifyUser(getUser().getUsername());
        commissionService.update(record);
        return R.ok();
    }

    @PostMapping("/delete")
    @RequiresPermissions("agent:commission:delete")
    @SysLog(module = "佣金方案",methodText = "佣金方案删除")
    @ApiOperation(value = "佣金方案删除", notes = "佣金方案删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R deleteCommn(@RequestBody Commission record) {
        Assert.isNull(record.getId(), "id不能为空");
        commissionService.deleteCommn(record.getId());
        return R.ok();
    }

    @GetMapping("/info/{id}")
    @RequiresPermissions("agent:commission:info")
    @ApiOperation(value = "佣金方案查询(根据ID查询)", notes = "佣金方案查询(根据ID查询)")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R commnInfo(@PathVariable("id") Integer id) {
        return R.ok().put("commn", commissionService.queryCommnInfo(id));
    }

    @GetMapping("/exportExecl")
    @RequiresPermissions("agent:commission:exportExecl")
    @SysLog(module = "佣金方案",methodText = "导出佣金方案excel数据")
    @ApiOperation(value = "导出佣金方案excel数据", notes = "导出佣金方案excel数据")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public void commissionExportExecl(@ModelAttribute Commission record, HttpServletResponse response) {
        commissionService.commissionExportExecl(record, response);
    }

    /*@PostMapping("/charge/save")
    @RequiresPermissions("agent:chargeRate:all")
    @SysLog(module = "佣金方案",methodText = 费用分摊比例新增)
    @ApiOperation(value = "费用分摊比例新增", notes = "费用分摊比例新增")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R chargeInsert(@RequestBody ComnCharge record) {
        commissionService.chargeInsertOrUpdate(record, getUser().getUsername());
        return R.ok();
    }

    @PostMapping("/charge/update")
    @RequiresPermissions("agent:chargeRate:all")
    @SysLog(module = "佣金方案",methodText = "费用分摊比例修改")
    @ApiOperation(value = "费用分摊比例修改", notes = "费用分摊比例修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R chargeUpdate(@RequestBody ComnCharge record) {
        commissionService.chargeInsertOrUpdate(record, getUser().getUsername());
        return R.ok();
    }

    @GetMapping("/charge/info")
    @RequiresPermissions("agent:chargeRate:all")
    @ApiOperation(value = "费用分摊比例查询", notes = "费用分摊比例查询")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R chargeInfo() {
        return R.ok().put(commissionService.querychargeRate());
    }*/

}
