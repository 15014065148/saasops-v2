package com.eveb.saasops.modules.operate.controller;

import javax.validation.constraints.NotNull;

import com.eveb.saasops.common.annotation.SysLog;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.validator.Assert;
import com.eveb.saasops.modules.base.controller.AbstractController;
import com.eveb.saasops.modules.operate.dto.ActBonusAuditDto;
import com.eveb.saasops.modules.operate.dto.ActivityDto;
import com.eveb.saasops.modules.operate.entity.OprActBonus;
import io.swagger.annotations.*;
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
import org.springframework.web.multipart.MultipartFile;

import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.modules.operate.entity.OprActActivity;
import com.eveb.saasops.modules.operate.service.OprActActivityService;


@RestController
@RequestMapping("/bkapi/operate/activity")
@Api(description = "运营管理-活动设置")
public class OprActActivityController extends AbstractController {

    @Autowired
    private OprActActivityService oprActBaseService;

    @GetMapping("/listAll")
    @ApiOperation(value = "查询所有活动", notes = "查询所有活动")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R listAll(@RequestParam(value = "actCatId", required = false) Integer actCatId) {
        return R.ok().put(oprActBaseService.queryListAll(actCatId));
    }

    @GetMapping("/activityList")
    @ApiOperation(value = "查询所有活动包括失效的", notes = "查询所有活动包括失效的")
    public R activityList(String actTmplIds) {
        return R.ok().put(oprActBaseService.activityList(actTmplIds));
    }

    @GetMapping("/list")
    @RequiresPermissions("operate:activity:list")
    @ApiOperation(value = "查询列表", notes = "查询列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R list(@ModelAttribute OprActActivity activity, @RequestParam("pageNo") @NotNull Integer pageNo,
                  @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(oprActBaseService.queryListPage(activity, pageNo, pageSize));
    }

    @GetMapping("/info/{id}")
    @RequiresPermissions("operate:activity:info")
    @ApiOperation(value = "信息", notes = "信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R info(@PathVariable("id") Integer id) {
        return R.ok().put(oprActBaseService.queryObject(id));
    }

    @PostMapping("/save")
    @RequiresPermissions("operate:activity:save")
    @SysLog(module = "活动设置", methodText = "新增加活动")
    @ApiOperation(value = "保存", notes = "保存")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R saveActivtiy(@ModelAttribute ActivityDto activityDto, @RequestParam(value = "uploadPcFile", required = false) MultipartFile uploadPcFile,
                          @RequestParam(value = "uploadMbFile", required = false) MultipartFile uploadMbFile) {
        Assert.isNull(activityDto.getActActivity(), "不能为空");
        Assert.isNull(activityDto.getObject(), "不能为空");
        oprActBaseService.save(activityDto, getUser().getUsername(), uploadPcFile, uploadMbFile);
        return R.ok();
    }

    @PostMapping("/update")
    @RequiresPermissions("operate:activity:update")
    @SysLog(module = "活动设置", methodText = "活动编辑")
    @ApiOperation(value = "修改", notes = "修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateActivity(@ModelAttribute ActivityDto activityDto, @RequestParam(value = "uploadPcFile", required = false) MultipartFile uploadPcFile,
                            @RequestParam(value = "uploadMbFile", required = false) MultipartFile uploadMbFile) {
        Assert.isNull(activityDto.getActActivity(), "不能为空");
        Assert.isNull(activityDto.getObject(), "不能为空");
        oprActBaseService.updateActivity(activityDto, uploadPcFile, uploadMbFile, getUser().getUsername());
        return R.ok();
    }

    @PostMapping("/updateAvailable")
    @RequiresPermissions("operate:activity:update")
    @SysLog(module = "活动设置", methodText = "活动状态修改启用/禁用")
    @ApiOperation(value = "修改", notes = "修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAvailable(@RequestBody OprActActivity activity) {
        Assert.isNull(activity.getAvailable(), "状态不能为空");
        Assert.isNull(activity.getId(), "id不能为空");
        oprActBaseService.updateAvailable(activity, getUser().getUsername());
        return R.ok();
    }

    @GetMapping("/delete")
    @RequiresPermissions("operate:activity:delete")
    @SysLog(module = "活动设置", methodText = "活动删除")
    @ApiOperation(value = "活动删除", notes = "活动删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R deleteActivity(@RequestParam("id") Integer id) {
        Assert.isNull(id, "id不能为空");
        oprActBaseService.deleteActivity(id);
        return R.ok();
    }

    @GetMapping("/activityAuditList")
    @RequiresPermissions("operate:activity:activityAuditList")
    @ApiOperation(value = "活动审核集合", notes = "活动审核集合")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R activityAuditList(@ModelAttribute OprActBonus oprActBonus, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(oprActBaseService.activityAuditList(oprActBonus, pageNo, pageSize));
    }

    @PostMapping("/activityAudit")
    @RequiresPermissions("operate:activity:activityAudit")
    @SysLog(module = "活动设置", methodText = "活动审核")
    @ApiOperation(value = "活动审核", notes = "活动审核")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R activityAudit(@RequestBody ActBonusAuditDto bonusAuditDto) {
        Assert.isNull(bonusAuditDto.getBonuses(), "不能为空");
        Assert.isNull(bonusAuditDto.getStatus(), "状态不能为空");
        Assert.isNull(bonusAuditDto.getTmplCode(), "TmplCode状态不能为空");
        oprActBaseService.activityAudit(bonusAuditDto, getUser().getUsername());
        oprActBaseService.activityAuditMsg(bonusAuditDto, CommonUtil.getSiteCode());
        return R.ok();
    }
}
