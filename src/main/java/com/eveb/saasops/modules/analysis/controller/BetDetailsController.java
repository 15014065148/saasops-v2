package com.eveb.saasops.modules.analysis.controller;

import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.modules.analysis.entity.FundReportModel;
import com.eveb.saasops.modules.analysis.entity.GameReportQueryModel;
import com.eveb.saasops.modules.analysis.service.AnalysisService;
import com.eveb.saasops.modules.base.controller.AbstractController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.*;

@RestController
@RequestMapping("/bkapi/analysis/betDetails")
@Api(value = "Analysis",description = "经营分析")
public class BetDetailsController extends AbstractController {

    @Autowired
    private AnalysisService analysisService;

    /**
     * 跳转到全部游戏注单页面
     *
     * @return
     */
    @GetMapping("betDetailsList")
    public String goBetDetailsList()
    {
        return "/analysis/betDetailsList";
    }

    /**
     * 查询投注明细统计
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping("/finalBetDetailsAll")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "投注记录", notes = "全部游戏")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R betDetailsData(@RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize,GameReportQueryModel model) {
        model.setSiteCode(CommonUtil.getSiteCode());
        return  R.ok().put("page",analysisService.getBkRptBetListPage(pageNo, pageSize,model)).put("total",analysisService.getRptBetListReport(model));
    }

    /**
     * 查询日统计
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping("/findRptBetDay")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "经营报表", notes = "经营报表")
    public R findRptBetDay(@RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, @RequestParam(value="parentAgentid", required = false)Integer parentAgentid,
                           @RequestParam(value="agentid", required = false)Integer agentid,@RequestParam(value="groupid", required = false)Integer groupid,@RequestParam(value="loginName", required = false)String loginName,
                           @RequestParam(value="platform", required = false)String platform,@RequestParam(value="gametype", required = false)String gametype,
                           @RequestParam(value="betStrTime", required = false)String betStrTime,@RequestParam(value="betEndTime", required = false)String betEndTime,@RequestParam(value="orderBy",required=false) String orderBy,
                           @RequestParam(value="group",required=false) String group) {
        Map resultMap=new HashMap<>();
        FundReportModel report=analysisService.getFundReport(parentAgentid,agentid,groupid,betStrTime,betEndTime);
        Map fundMap=analysisService.getFundStatistics(parentAgentid, agentid, groupid, loginName,
                platform,gametype,betStrTime,betEndTime);
        PageUtils page=analysisService.getRptBetDay(pageNo, pageSize, parentAgentid, agentid, groupid, loginName,
                platform,gametype,betStrTime,betEndTime,orderBy,group);
        resultMap.put("profit",report);
        resultMap.put("option",fundMap);
        resultMap.put("page",page);
        return  R.ok().put("data",resultMap);
    }

    /**
     * 查询彩金下注
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping("/findJackpotBet")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "彩金下注", notes = "全部游戏")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findJackpotBetDetails(@RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, GameReportQueryModel model) {
        model.setSiteCode(CommonUtil.getSiteCode());
        return  R.ok().put("page",analysisService.getJackpotBetListPage(pageNo, pageSize,model));
    }

    /**
     * 查询彩金中奖
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping("/findJackpotRewardBet")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "彩金中奖", notes = "全部游戏")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findJackpotRewardBetDetails(@RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, GameReportQueryModel model) {
        model.setSiteCode(CommonUtil.getSiteCode());
        return  R.ok().put("page",analysisService.getJackpotBetListPage(pageNo, pageSize, model));
    }

    /**
     * 查询游戏代码
     *
     * @return
     */
    @GetMapping("/finalGameCode")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "游戏代码", notes = "游戏代码")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R finalGameCodByType(@RequestParam("codetype")String platFormId) {
        if(platFormId!=null&&!platFormId.trim().equals("")) {
            return R.ok().put("page", analysisService.getGameType(platFormId,0));
        }
        return  R.ok().put("page",analysisService.getPlatForm());
    }

    /**
     * 查询所有代理账号
     *
     * @return
     */
    @GetMapping("/finalAgentAccount")
    @RequiresPermissions("analysis:betDetails:finalBetDetailsAll")
    @ApiOperation(value = "代理账号", notes = "代理账号")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R finalAgentAccount() {
        return  R.ok().put("page",analysisService.getAgentAccount());
    }


}
