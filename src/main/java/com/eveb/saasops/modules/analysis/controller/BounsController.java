package com.eveb.saasops.modules.analysis.controller;

import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.modules.analysis.entity.BounsReportQueryModel;
import com.eveb.saasops.modules.analysis.service.AnalysisService;
import com.eveb.saasops.modules.base.controller.AbstractController;
import com.eveb.saasops.modules.sys.entity.SysUserEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/bkapi/analysis/bouns")
@Api(value = "Bouns", description = "红利报表")
public class BounsController extends AbstractController {

    @Autowired
    private AnalysisService analysisService;

    /***
     * 查询红利报表
     * @return
     */
    @RequestMapping("/findBonusReportPage")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "红利报表", notes = "全部游戏")
    public R findBonusReportPage(BounsReportQueryModel model) {
        return R.ok().put("page", analysisService.findBonusReportPage(model));
    }

    /***
     * 查询红利报表,聚合代理
     * @return
     */
    @RequestMapping("/findBonusGroupTopAgentReportPage")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "红利报表", notes = "全部游戏")
    public R findBonusGroupTopAgentReportPage(BounsReportQueryModel model) {
        return R.ok().put("page", analysisService.findBonusGroupTopAgentReportPage(model));
    }

    /***
     * 查询红利报表,聚合代理
     * @return
     */
    @RequestMapping("/findBonusGroupAgentReportPage")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "红利报表", notes = "全部游戏")
    public R findBonusGroupAgentReportPage(BounsReportQueryModel model) {
        return R.ok().put("page", analysisService.findBonusGroupAgentReportPage(model));
    }

    /***
     * 查询红利报表,聚合会员 会员组
     * @return
     */
    @RequestMapping("/findBonusGroupUserReportPage")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "红利报表", notes = "全部游戏")
    public R findBonusGroupUserReportPage(BounsReportQueryModel model) {
        return R.ok().put("page", analysisService.findBonusGroupUserReportPage(model));
    }

    /***
     * 查询红利报表,聚合 总代 代理 会员 会员组
     * @return
     */
    @RequestMapping("/findBonusGroupUserTotal")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "红利报表", notes = "全部游戏")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            ,@ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R findBonusGroupUserTotal(BounsReportQueryModel model) {
        /** 获取登录用户的信息 **/
        SysUserEntity sysUserEntity=getUser();
        model.setLoginSysUserName(sysUserEntity.getUsername());
        return R.ok().put("page", analysisService.findBonusGroupUserTotal(model));
    }

    /***
     * 查询红利报表,展示列表
     * @return
     */
    @RequestMapping("/findBonusPage")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "红利报表", notes = "全部游戏")
    public R findBonusPage(BounsReportQueryModel model) {
        return R.ok().put("page", analysisService.findBonusPage(model));
    }
}
