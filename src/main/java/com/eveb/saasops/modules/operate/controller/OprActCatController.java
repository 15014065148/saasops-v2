package com.eveb.saasops.modules.operate.controller;

import javax.validation.constraints.NotNull;

import com.eveb.saasops.common.annotation.SysLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eveb.saasops.common.constants.Constants.Available;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.modules.operate.entity.OprActCat;
import com.eveb.saasops.modules.operate.service.OprActCatService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/bkapi/operate/opractcat")
@Api(description = "活动分类")
public class OprActCatController {

    @Autowired
    private OprActCatService oprActCatService;

    @GetMapping("/listAll")
    @ApiOperation(value = "查询所有", notes = "查询所有")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R listAll() {
        OprActCat actCat = new OprActCat();
        actCat.setAvailable(Available.enable);
        return R.ok().putPage(oprActCatService.queryListCond(actCat));
    }

    @GetMapping("/list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R list(@ModelAttribute OprActCat oprActCat, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, @RequestParam(value = "orderBy", required = false) String orderBy) {
        return R.ok().putPage(oprActCatService.queryListPage(oprActCat, pageNo, pageSize, orderBy));
    }


    @PostMapping("/save")
    @RequiresPermissions("operate:activity:save")
    @SysLog(module = "活动设置",methodText = "活动分类新增")
    @ApiOperation(value = "保存", notes = "保存")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R save(@RequestBody OprActCat oprActCat) {
        oprActCat.setAvailable(Available.enable);
        oprActCatService.save(oprActCat);
        return R.ok();
    }

    @PostMapping("/update")
    @RequiresPermissions("operate:activity:update")
    @SysLog(module = "活动设置",methodText = "活动分类修改状态")
    @ApiOperation(value = "修改状态", notes = "修改状态")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R update(@RequestBody OprActCat oprActCat) {
        oprActCatService.update(oprActCat);
        return R.ok();
    }

    @PostMapping("/delete")
    @RequiresPermissions("operate:activity:delete")
    @SysLog(module = "活动设置",methodText = "活动分类删除")
    @ApiOperation(value = "删除", notes = "删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R delete(@RequestBody OprActCat oprActCat) {
        oprActCatService.delete(oprActCat.getId());
        return R.ok();
    }
}
