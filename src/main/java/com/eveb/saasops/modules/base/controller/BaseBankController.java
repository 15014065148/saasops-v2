package com.eveb.saasops.modules.base.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.modules.base.service.BaseBankService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/bkapi/base/baseBank")
@Api(value = "BaseBank", description = "银行基本信息记录")
public class BaseBankController {
	@Autowired
	private BaseBankService baseBankService;
	
    @RequestMapping("/list")
    @ApiOperation(value="银行信息-列表", notes="显示所有银行基本信息")
    public R list() {
        return R.ok().put("banks",baseBankService.selectAll());
    }
    
    @RequestMapping("/bList")
    @ApiOperation(value="银行信息-列表", notes="显示银行基本信息")
    public R bankList() {
        return R.ok().put("bList",baseBankService.getBankList());
    }
    
    @RequestMapping("/oList")
    @ApiOperation(value="银行信息-列表", notes="显示第三方支付基本信息")
    public R onLineList() {
        return R.ok().put("oList",baseBankService.getOnLineList());
    }
    
}
