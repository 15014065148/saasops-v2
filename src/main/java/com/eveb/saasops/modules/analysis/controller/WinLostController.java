package com.eveb.saasops.modules.analysis.controller;

import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.modules.analysis.entity.WinLostEsQueryModel;
import com.eveb.saasops.modules.analysis.entity.WinLostReport;
import com.eveb.saasops.modules.analysis.entity.WinLostReportModel;
import com.eveb.saasops.modules.analysis.service.AnalysisService;
import com.eveb.saasops.modules.base.controller.AbstractController;
import com.eveb.saasops.modules.sys.entity.SysUserEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/bkapi/analysis/winlost")
@Api(value = "WinLost", description = "输赢报表")
public class WinLostController extends AbstractController {
    @Autowired
    private AnalysisService analysisService;

    /***
     * 查询输赢报表
     * @return
     */
    @RequestMapping("/findWinLostReportPage")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "输赢报表", notes = "全部游戏")
    public R findWinLostReportPage(WinLostReportModel model) {
        return R.ok().put("page", analysisService.findRptWinLostPage(model));
    }

    /***
     * 查询输赢报表，根据总代 代理 会员组进行分组
     * @return
     */
    @RequestMapping("/findWinLostGroupReportPage")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "输赢报表", notes = "全部游戏")
    public R findWinLostGroupReportPage(WinLostReportModel model) {
        return R.ok().put("page", analysisService.findRptWinLostGroupPage(model));
    }

    /***
     * 查询输赢报表，根据 代理 进行分组
     * @return
     */
    @RequestMapping("/findWinLostGroupAgentReportPage")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "输赢报表", notes = "全部游戏")
    public R findWinLostGroupAgentReportPage(WinLostReportModel model) {
        return R.ok().put("page", analysisService.findWinLostGroupAgentReportPage(model));
    }

    /***
     * 查询输赢报表，根据会员 会员组进行分组
     * @return
     */
    @RequestMapping("/findWinLostGroupUserReportPage")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "输赢报表", notes = "全部游戏")
    public R findWinLostGroupUserReportPage(WinLostReportModel model) {
        /** 获取登录用户的信息 **/
        SysUserEntity sysUserEntity=getUser();
        model.setLoginSysUserName(sysUserEntity.getUsername());
        return R.ok().put("page", analysisService.findRptWinLostGroupUserPage(model));
    }

    /***
     * 查询会员 存款 提款 红利数据
     * @return
     */
    @RequestMapping("/findTransactionPage")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "输赢报表", notes = "全部游戏")
    public R findTransactionPage(WinLostReportModel model) {
        return R.ok().put("page", analysisService.findTransactionPage(model));
    }

    /***
     * 查询最近七天会员数据
     * @return
     */
    @RequestMapping("/mamberReport")
    @ApiOperation(value = "最近七天会员数据", notes = "全部游戏")
    public R mamberReport() {
        return R.ok().put("page", analysisService.getRptMemberList());
    }

    /***
     * 查询输赢报表
     * @return
     */
    @GetMapping("/findWinLostList")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "输赢报表", notes = "输赢报表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findWinLostList(@ModelAttribute WinLostReport winLostReport) {
        return R.ok().put("page", analysisService.findWinLostList(winLostReport));
    }
    /***
     * 根据总代显示输赢报表
     * @return
     */
    @GetMapping("/findWinLostListOfTagency")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "根据总代显示输赢报表", notes = "根据总代显示输赢报表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findWinLostListOfTagency(@ModelAttribute WinLostReport winLostReport) {
        return R.ok().put("page", analysisService.findWinLostListOfTagency(winLostReport));
    }
    /***
     * 根据总代查询显示输赢报表
     * @return
     */
    @GetMapping("/findWinLostListByTagencyId")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "根据总代查询显示输赢报表", notes = "根据总代查询显示输赢报表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findWinLostListByTagencyId(@ModelAttribute WinLostReport winLostReport) {
        return R.ok().put("page", analysisService.findWinLostListByTagencyId(winLostReport));
    }
    /***
     * 根据代理查询显示输赢报表
     * @return
     */
    @GetMapping("/findWinLostListByCagencyId")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "根据代理查询显示输赢报表", notes = "根据代理查询显示输赢报表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findWinLostListByCagencyId(@ModelAttribute WinLostReport winLostReport) {
        return R.ok().put("page", analysisService.findWinLostListByCagencyId(winLostReport));
    }
    /***
     * 根据会员查询显示输赢报表
     * @return
     */
    @GetMapping("/findWinLostListByAccountId")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "根据会员查询显示输赢报表", notes = "根据会员查询显示输赢报表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findWinLostListByAccountId(@ModelAttribute WinLostEsQueryModel model) {
       model.setSiteCode(CommonUtil.getSiteCode());
        return R.ok().put("page", analysisService.findWinLostListByAccountId(model));
    }
    /**
     * 查询平台列表
     *
     * @return
     */
    @GetMapping("/findDepotList")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "查询平台下拉列表", notes = "查询平台下拉列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findDepotList() {
        return  R.ok().put("data",analysisService.getDepot());
    }
    /**
     * 查询游戏类型列表
     *
     * @return
     */
    @GetMapping("/findGameList")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "查询游戏类型下拉列表", notes = "查询游戏类型下拉列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findGameList(@RequestParam(value = "depotId",required = false)String depotId) {
        return R.ok().put("data", analysisService.getGameCat(depotId));
    }
    /**
     * 查询游戏类型子类列表
     *
     * @return
     */
    @GetMapping("/findSubGameList")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "查询游戏类型子类下拉列表", notes = "查询游戏类型子类下拉列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findSubGameList(@RequestParam(value = "depotId",required = false)String depotId,@RequestParam(value = "catId",required = false)String catId) {
        return R.ok().put("data", analysisService.getSubGameCat(depotId,catId));
    }
    /**
     * 查询有效投注人数
     *
     * @return
     */
    @GetMapping("/getValidBetAccountCounts")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "查询有效投注人数", notes = "查询有效投注人数")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getValidBetAccountCounts(@ModelAttribute WinLostReport winLostReport) {
        Map<String,Integer> map=new HashMap<>();
        map.put("validBetAccountCounts",analysisService.getValidBetAccountCounts(winLostReport));
        return  R.ok().put(map);
    }
    /**
     * 查询输赢报表最后一次更新时间
     *
     * @return
     */
    @GetMapping("/getBetLastDate")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "查询输赢报表最后一次更新时间", notes = "查询输赢报表最后一次更新时间")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getBetLastDate() {
        String siteCode=CommonUtil.getSiteCode();
        Map<String,String> map=new HashMap<>();
        String betLastDateStr;
        try {
            betLastDateStr=analysisService.getBetLastDate(siteCode);
        } catch (Exception e) {
            throw new RRException("查询异常!");
        }
        map.put("betLastDate",betLastDateStr);
        return  R.ok().put(map);
    }
}
