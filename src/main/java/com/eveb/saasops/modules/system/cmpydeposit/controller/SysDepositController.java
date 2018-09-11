package com.eveb.saasops.modules.system.cmpydeposit.controller;


import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import com.eveb.saasops.common.annotation.SysLog;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.modules.system.cmpydeposit.entity.SysDeposit;
import com.eveb.saasops.modules.system.cmpydeposit.service.SysDepositService;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/bkapi/setting/sysdeposit")
@Api(value = "SysDeposit", description = "")
public class SysDepositController {
    @Autowired
    private SysDepositService sysDepositService;
    
    @GetMapping("/list")
    @RequiresPermissions("setting:sysdeposit:list")
    @ApiOperation(value="列表", notes="列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R list(@ModelAttribute SysDeposit sysDeposit, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put("page", sysDepositService.queryListPage(sysDeposit,pageNo,pageSize));  
    }

    @SysLog(module = "入款管理",methodText = "查询列表")
    @GetMapping("/sysDepositList")
    @RequiresPermissions("setting:sysdeposit:list")
    @ApiOperation(value="公司入款设置列表（不分页）", notes="公司入款设置列表（不分页）")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R querySysDepositList() {
        return R.ok().put("data", sysDepositService.querySysDepositList());
    }


    @GetMapping("/info/{id}")
    @RequiresPermissions("setting:sysdeposit:info")
    @ApiOperation(value="信息", notes="信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R info(@PathVariable("id") Integer id) {
        SysDeposit sysDeposit =sysDepositService.queryObject(id);
        return R.ok().put("sysDeposit", sysDeposit);
    }
    
    @SysLog(module = "入款设置",methodText = "保存入款设置")
    @PostMapping("/save")
    @RequiresPermissions("setting:sysdeposit:save")
    @ApiOperation(value="保存", notes="保存")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R save(@RequestBody SysDeposit sysDeposit) {
            sysDepositService.save(sysDeposit);
        return R.ok();
    }

    @SysLog(module = "入款设置",methodText = "更新入款设置")
    @PostMapping("/update")
    @RequiresPermissions("setting:sysdeposit:update")
    @ApiOperation(value="修改", notes="修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R update(@RequestBody SysDeposit sysDeposit) { 	
    	sysDepositService.update(sysDeposit);
        return R.ok();
    }
    
    @SysLog(module = "入款设置",methodText = "更新入款设置是否启用")
    @PostMapping("/updateStatus")
    @RequiresPermissions("setting:sysdeposit:update")
    @ApiOperation(value="修改", notes="修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateStatus(@RequestBody SysDeposit sysDeposit) { 		
       sysDepositService.updateStatus(sysDeposit); 
       return R.ok();
    }
    
    @PostMapping("/deleteById/{id}")
    @RequiresPermissions("setting:sysdeposit:delete")
    @ApiOperation(value="删除", notes="删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R deleteById(@PathVariable(name="id",required = true) Integer id) {
            sysDepositService.delete(id);
        return R.ok();
    }
    

/*    *//**
     * 导出报表
     * @param response
     *//*
    @GetMapping("/exportExcel")
    @RequiresPermissions("setting:sysdeposit:export")
    @ApiOperation(value="导出报表", notes="导出报表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public void domainExportExcel(SysDeposit sysDeposit, HttpServletResponse response){
    	sysDepositService.depositExportExcel(sysDeposit,response);
    }*/
    
}
