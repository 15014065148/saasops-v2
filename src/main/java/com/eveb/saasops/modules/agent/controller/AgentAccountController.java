package com.eveb.saasops.modules.agent.controller;

import com.eveb.saasops.common.annotation.SysLog;
import com.eveb.saasops.common.utils.DateUtil;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.common.validator.Assert;
import com.eveb.saasops.modules.agent.entity.AgentAccount;
import com.eveb.saasops.modules.agent.service.AgentAccountService;
import com.eveb.saasops.modules.base.controller.AbstractController;
import com.eveb.saasops.modules.sys.service.SysUserAgyaccountrelationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.eveb.saasops.common.constants.Constants.TOP_AGENT_PARENT_ID;
import static com.eveb.saasops.common.utils.CommonUtil.getIpAddress;


@RestController
@RequestMapping("/bkapi/agent/account")
@Api(description = "总代设置,代理设置")
public class AgentAccountController extends AbstractController {

    @Autowired
    private AgentAccountService accountService;
    @Autowired
    private SysUserAgyaccountrelationService sysUserAgyaccountrelationService;

    @PostMapping("/save")
    @RequiresPermissions("agent:account:save")
    @SysLog(module = "总代设置",methodText = "总代设置新增")
    @ApiOperation(value = "总代设置新增", notes = "总代设置新增")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R accountSave(@RequestBody @Valid AgentAccount agentAccount, HttpServletRequest request) {
        agentAccount.setCreateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        agentAccount.setCreateUser(getUser().getUsername());
        agentAccount.setParentId(TOP_AGENT_PARENT_ID);
        agentAccount.setIp(getIpAddress(request));
        accountService.accuntInsert(agentAccount, Boolean.FALSE);
        accountService.addAuthority(agentAccount.getId(),getUserId(),agentAccount.getParentId());
        return R.ok();
    }

    @GetMapping("/list")
    @RequiresPermissions("agent:account:list")
    @ApiOperation(value = "总代设置查询列表", notes = "总代设置查询列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R accountList(@ModelAttribute AgentAccount agentAccount, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        agentAccount.setParentId(TOP_AGENT_PARENT_ID);
        return R.ok().putPage(accountService.findAccountListPage(agentAccount, pageNo, pageSize));
    }

    @GetMapping("/info/{id}")
    @RequiresPermissions("agent:account:info")
    @ApiOperation(value = "总代设置查询(根据ID查询)", notes = "总代设置查询(根据ID查询)")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R accountInfo(@PathVariable("id") Integer id) {
        return R.ok().put("agyAccount", accountService.findAccountInfo(id));
    }


    @PostMapping("/updateAvailable")
    @RequiresPermissions("agent:account:update")
    @SysLog(module = "总代设置,代理设置",methodText = "总代or代理设置禁用/启用")
    @ApiOperation(value = "总代(代理)设置修改禁用/启用", notes = "总代(代理)设置修改禁用/启用")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAvailable(@RequestBody AgentAccount account) {
        Assert.isNull(account.getId(), "id不能为空");
        account.setModifyUser(getUser().getUsername());
        accountService.update(account);
        return R.ok();
    }

    @GetMapping("/exportExecl")
    @RequiresPermissions("agent:account:exportExecl")
    @SysLog(module = "总代设置",methodText = "总代设置数据导出")
    @ApiOperation(value = "总代设置数据导出", notes = "总代设置数据导出")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public void accountExportExecl(@ModelAttribute AgentAccount record, HttpServletResponse response) {
        accountService.accountExportExecl(record, response);
    }

    @GetMapping("/findAccountByName")
    @ApiOperation(value = "总代(代理)设置校验用户名是否存在", notes = "总代(代理)设置校验用户名是否存在")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findAccountByName(@RequestParam("agyAccount") @NotNull String agyAccount) {
        return R.ok().put("name", accountService.findAccountByName(agyAccount));
    }

    @GetMapping("/findTopAccountAll")
    @ApiOperation(value = "查找所有代理", notes = "查找所有代理")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"),@ApiImplicitParam(name = "siteCode", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R findTopAccountAll(@RequestParam(value = "parentId", required = false) Integer parentId) {
        return R.ok().put("accounts", accountService.findTopAccountAll(parentId));
    }

    @GetMapping("/findAllSubAgency")
    @ApiOperation(value = "查找所有子代理", notes = "查找所有子代理")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"),@ApiImplicitParam(name = "siteCode", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R findAllSubAgency() {
        return R.ok().put("accounts", accountService.findAllSubAgency());
    }
    
    @GetMapping("/getAllParentAccount")
    @ApiOperation(value = "获取所有总代", notes = "获取所有总代")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getAllParentAccount() {
        return R.ok().put("accounts", accountService.getAllParentAccount("0"));
    }

    @GetMapping("/findCommissionByAccountId")
    @ApiOperation(value = "查询总代佣金方案", notes = "查询总代佣金方案")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findCommissionByAccountId(@ModelAttribute AgentAccount agentAccount) {
        return R.ok().put("commission", accountService.findCommissionByAccountId(agentAccount));
    }

    @PostMapping("/subordinate/save")
    @RequiresPermissions("agent:subordinate:save")
    @SysLog(module = "代理设置",methodText = "代理设置新增")
    @ApiOperation(value = "代理设置新增", notes = "代理设置新增")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R subordinateSave(@RequestBody @Valid AgentAccount agentAccount, HttpServletRequest request) {
        agentAccount.setCreateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        agentAccount.setCreateUser(getUser().getUsername());
        agentAccount.setIp(getIpAddress(request));
        accountService.accuntInsert(agentAccount, Boolean.TRUE);
        return R.ok();
    }

    @GetMapping("/subordinate/list")
    @ApiOperation(value = "查询代理设置列表", notes = "查询代理设置列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R subordinateList(@ModelAttribute AgentAccount agentAccount, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(accountService.findSubordinateAccListPage(agentAccount, pageNo, pageSize));
    }

    @GetMapping("/subordinate/exportExecl")
    @SysLog(module = "代理设置",methodText = "代理设置导出数据")
    @ApiOperation(value = "代理设置导出数据", notes = "代理设置导出数据")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public void subordinateExportExecl(@ModelAttribute AgentAccount agentAccount, HttpServletResponse response) {
        accountService.subordinateExportExecl(agentAccount, response);
    }

    @GetMapping("/selectAgentTree")
    @ApiOperation(value = "获取代理节点数", notes = "获取代理节点数")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
    ,@ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R selectAgentTree() {
       return R.ok().put("agentTree",accountService.selectLevelsAgentTree());
    }

    @GetMapping("/queryListByUserId")
    @ApiOperation(value = "获取用户对应的代理权限", notes = "获取用户对应的代理权限")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryListByUserId(@RequestParam("userId") Long userId){
       return R.ok().put("data",sysUserAgyaccountrelationService.queryListByUserId(userId));
    }
}
