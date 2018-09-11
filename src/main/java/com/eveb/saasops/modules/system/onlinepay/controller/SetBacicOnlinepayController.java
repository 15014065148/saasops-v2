package com.eveb.saasops.modules.system.onlinepay.controller;


import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.modules.base.controller.AbstractController;
import com.eveb.saasops.modules.member.entity.MbrGroup;
import com.eveb.saasops.modules.member.service.MbrGroupService;
import com.eveb.saasops.modules.system.domain.entity.DomainType;
import com.eveb.saasops.modules.system.domain.entity.SystemDomain;
import com.eveb.saasops.modules.system.domain.service.SystemDomainService;
import com.eveb.saasops.modules.system.onlinepay.entity.SetBacicOnlinepay;
import com.eveb.saasops.modules.system.onlinepay.service.SetBacicOnlinepayService;
import io.swagger.annotations.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bkapi/onlinepay/setbaciconlinepay")
@Api(value = "Onlinepay", description = "线上支付")
public class SetBacicOnlinepayController extends AbstractController {
    @Autowired
    private SetBacicOnlinepayService setBacicOnlinepayService;
    @Autowired
    private SystemDomainService systemDomainService;
    @Autowired
    private MbrGroupService mbrGroupService;

    /**
     * 列表
     */
    @GetMapping("/list")
    @RequiresPermissions("onlinepay:setbaciconlinepay:list")
    @ApiOperation(value = "列表", notes = "列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R list(@ModelAttribute SetBacicOnlinepay setBacicOnlinepay, @ApiParam @RequestParam("pageNo") @NotNull Integer pageNo, @ApiParam @RequestParam("pageSize") @NotNull Integer pageSize, @ApiParam String orderBy) {
        return R.ok().put("page", setBacicOnlinepayService.queryListPage(setBacicOnlinepay, pageNo, pageSize, orderBy));
    }

    @GetMapping("/onlinePayList")
    @ApiOperation(value = "线上支付所有数据(不分页)", notes = "线上支付所有数据(不分页)")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"), @ApiImplicitParam(name = "siteCode", value = "siteCode", required = true, dataType = "String", paramType = "header")})
    public R querySetBacicOnlinepayList() {
        return R.ok().put("data", setBacicOnlinepayService.querySetBacicOnlinepayList());
    }

    /**
     * 列表
     */
    @GetMapping("/queryConditions")
    @RequiresPermissions("onlinepay:setbaciconlinepay:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryConditions(@ModelAttribute SetBacicOnlinepay setBacicOnlinepay, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, String orderBy, HttpServletRequest request) {
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        setBacicOnlinepay.setLoginName(loginName);
        return R.ok().put("page", setBacicOnlinepayService.queryConditions(setBacicOnlinepay, pageNo, pageSize, orderBy));
    }


    /**
     * 信息
     */
    @GetMapping("/info/{id}")
    @RequiresPermissions("onlinepay:setbaciconlinepay:info")
    @ApiOperation(value = "信息", notes = "信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R info(@PathVariable("id") Integer id) {
        SetBacicOnlinepay setBacicOnlinepay = setBacicOnlinepayService.queryObject(id);
        if (setBacicOnlinepay.getMbrGroupType() == 1) {
            setBacicOnlinepay.setMbrGroups("");
        }
        return R.ok().put("setBacicOnlinepay", setBacicOnlinepay);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    @RequiresPermissions("onlinepay:setbaciconlinepay:save")
    @ApiOperation(value = "保存", notes = "保存")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R save(@RequestBody SetBacicOnlinepay setBacicOnlinepay) {
        if (StringUtils.isEmpty(setBacicOnlinepay.getMinLimit())) {
            return R.error(2000, "单笔最小限额不为空");
        } else if (setBacicOnlinepay.getMinLimit() < 0) {
            return R.error(2000, "单笔最小限额不能小于0");
        }
        if (StringUtils.isEmpty(setBacicOnlinepay.getMaxLimit())) {
            return R.error(2000, "单笔最大限额不为空");
        } else if (setBacicOnlinepay.getMaxLimit() < setBacicOnlinepay.getMinLimit()) {
            return R.error(2000, "单笔最大限额不能小于单笔最小限额");
        }
        if (StringUtils.isEmpty(setBacicOnlinepay.getMaxLimitDaily())) {
            return R.error(2000, "单日最大限额不为空");
        } else if (setBacicOnlinepay.getMaxLimitDaily() < setBacicOnlinepay.getMaxLimit()) {
            return R.error(2000, "单日最大限额不能小于单笔存款最大值");
        }
        String bankOptions = setBacicOnlinepay.getBankOptions();
        String selectedGroup = setBacicOnlinepay.getSelectedGroup();
        setBacicOnlinepay.setCreateUser(getUser().getUsername());
        setBacicOnlinepayService.unionSave(setBacicOnlinepay);
        setBacicOnlinepay.setBankOptions(bankOptions);
        setBacicOnlinepay.setSelectedGroup(selectedGroup);
        return R.ok().put("data", setBacicOnlinepay);
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @RequiresPermissions("onlinepay:setbaciconlinepay:update")
    @ApiOperation(value = "修改", notes = "修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R update(@RequestBody SetBacicOnlinepay setBacicOnlinepay) {
        setBacicOnlinepay.setModifyUser(getUser().getUsername());
        setBacicOnlinepayService.unionUpdate(setBacicOnlinepay);
        return R.ok();
    }

    @PostMapping("/updateEnable")
    @RequiresPermissions("onlinepay:setbaciconlinepay:update")
    @ApiOperation(value = "修改", notes = "修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateEnable(@RequestBody SetBacicOnlinepay setBacicOnlinepay) {
        setBacicOnlinepayService.update(setBacicOnlinepay);
        return R.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @RequiresPermissions("onlinepay:setbaciconlinepay:delete")
    @ApiOperation(value = "删除", notes = "删除")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R delete(@RequestBody Integer[] ids) {
        setBacicOnlinepayService.deleteBatch(ids);
        return R.ok();
    }

    @GetMapping("/queryPayment")
    @RequiresPermissions("onlinepay:setbaciconlinepay:list")
    @ApiOperation(value = "查询支付平台及相关信息，下拉列表使用", notes = "查询支付平台及相关信息，下拉列表使用")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryPayment() {
        List<Map<String, Object>> list = setBacicOnlinepayService.queryPayment();
        return R.ok().put("list", list);
    }

    @GetMapping("/queryPayBanks")
    @RequiresPermissions("onlinepay:setbaciconlinepay:list")
    @ApiOperation(value = "查询支付平台的银行，下拉列表使用", notes = "查询支付平台的银行，下拉列表使用")
    public R queryPayBanks(@RequestParam String paymentId) {
        List<Map<String, Object>> list = setBacicOnlinepayService.queryPayBanks(paymentId);
        return R.ok().put("list", list);
    }

    @GetMapping("/queryDomainPay")
    @RequiresPermissions("onlinepay:setbaciconlinepay:list")
    @ApiOperation(value = "查询支付的域名", notes = "查询支付的域名")
    public R queryDomainPay() {
        SystemDomain domain = new SystemDomain();
        domain.setDomainType(DomainType.getIndex("支付域名"));
        List<SystemDomain> list = systemDomainService.queryListPage(domain);
        return R.ok().put("list", list);
    }

    @GetMapping("/queryMbrGroups")
    @RequiresPermissions("onlinepay:setbaciconlinepay:list")
    @ApiOperation(value = "查询可用的会员组", notes = "查询可用的会员组")
    public R queryMbrGroups() {
        MbrGroup mbrGroup = new MbrGroup();
        mbrGroup.setAvailable(Constants.Available.enable);
        return R.ok().put("list", mbrGroupService.queryListCond(mbrGroup));
    }

    @GetMapping("/queryMbrGroupById")
    @RequiresPermissions("onlinepay:setbaciconlinepay:list")
    @ApiOperation(value = "查询当前支付方式已配置会员组", notes = "查询当前支付方式已配置会员组")
    public R queryMbrGroupById(@RequestParam String id) {
        return R.ok().put("list", setBacicOnlinepayService.queryMbrGroupById(id));
    }

    /**
     * 列表
     */
    @GetMapping("/ExportExcel")
    @RequiresPermissions("onlinepay:setbaciconlinepay:list")
    public void ExportExcel(SetBacicOnlinepay setBacicOnlinepay, HttpServletResponse response) {
        setBacicOnlinepayService.ExportExcel(setBacicOnlinepay, response);
    }
}
