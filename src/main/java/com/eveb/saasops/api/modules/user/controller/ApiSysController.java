package com.eveb.saasops.api.modules.user.controller;

import com.eveb.saasops.api.annotation.Login;
import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.modules.apisys.entity.TCpSite;
import com.eveb.saasops.api.modules.apisys.service.TCpSiteService;
import com.eveb.saasops.api.modules.transfer.dto.BillRequestDto;
import com.eveb.saasops.api.modules.user.dto.ElecGameDto;
import com.eveb.saasops.api.modules.user.dto.TCpSiteDto;
import com.eveb.saasops.api.modules.user.service.ApiSysService;
import com.eveb.saasops.api.modules.user.service.PtService;
import com.eveb.saasops.api.utils.HttpsRequestUtil;
import com.eveb.saasops.common.constants.AdvConstant;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.constants.Constants.Available;
import com.eveb.saasops.common.constants.SystemConstants;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.AESUtil;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.DateUtil;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.common.validator.Assert;
import com.eveb.saasops.modules.analysis.service.AnalysisService;
import com.eveb.saasops.modules.base.entity.BaseArea;
import com.eveb.saasops.modules.base.entity.ToprAdv;
import com.eveb.saasops.modules.base.service.BaseAreaService;
import com.eveb.saasops.modules.base.service.BaseBankService;
import com.eveb.saasops.modules.base.service.TWinTopService;
import com.eveb.saasops.modules.base.service.ToprAdvService;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.operate.entity.AdvBanner;
import com.eveb.saasops.modules.operate.entity.OprActCat;
import com.eveb.saasops.modules.operate.service.*;
import com.eveb.saasops.modules.system.systemsetting.dto.StationSet;
import com.eveb.saasops.modules.system.systemsetting.dto.SysWebTerms;
import com.eveb.saasops.modules.system.systemsetting.entity.SysSetting;
import com.eveb.saasops.modules.system.systemsetting.service.SysSettingService;
import io.swagger.annotations.*;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * 游戏分类，与系统信息相关的都放这里
 */
@RestController
@RequestMapping("/api/sys")
@Api(value = "api/sys", description = "系统基本信息服服务接口")
public class ApiSysController {


    @Autowired
    SysSettingService sysSettingService;
    @Autowired
    private TGmDepotService tGmDepotService;
    @Autowired
    private TGmCatService tGmCatService;
    @Autowired
    private TGmGameService tGmGameService;
    @Autowired
    private ApiSysService apiSysService;
    @Autowired
    private BaseBankService baseBankService;
    @Autowired
    private BaseAreaService baseAreaService;
    @Autowired
    private OprNoticeService oprNoticeService;
    @Autowired
    private PtService ptService;
    @Autowired
    private TWinTopService tWinTopService;
    @Autowired
    private OprActActivityService oprActActivityService;
    @Autowired
    private OprActCatService oprActCatService;

    @Autowired
    private OprAdvService oprAdvService;

    @Autowired
    private ToprAdvService toprAdvService;

    @Autowired
    private TCpSiteService tCpSiteService;
    @Autowired
    private AnalysisService analysisService;

    @GetMapping(value = "/getSiteCode")
    @ApiOperation(value = "获取前缀", notes = "")
    public R getSitePre(@RequestParam("url") String url) {
        TCpSite tCpSite = tCpSiteService.queryOneCond(CommonUtil.getDomainForUrl(url));
        try {
            return R.ok().put("SToken", AESUtil.encrypt(tCpSite.getSiteCode()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @GetMapping(value = "/findSiteCode")
    @ApiOperation(value = "提供给官网", notes = "提供给官网")
    public R findSiteCode(@RequestParam("url") String url) {
        TCpSite tCpSite = tCpSiteService.queryOneCond(CommonUtil.getDomainForUrl(url));
        return R.ok().put("siteCode", tCpSite.getSiteCode());
    }


    @Login
    @GetMapping("/transit")
    @ApiOperation(value = "游戏接口跳转", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R transit(@ModelAttribute BillRequestDto requestDto, HttpServletRequest request, HttpServletResponse response) {
        Assert.isNull(requestDto.getGameId(), "游戏Id不能为空!");
        // FIXME 等其它再放到SERIVCE中去
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        if (StringUtils.isEmpty(requestDto.getTerminal())) {
            requestDto.setTerminal(ApiConstants.Terminal.pc);
        }
        MbrDepotWallet wallet = new MbrDepotWallet();
        wallet.setAccountId(userId);
        wallet.setLoginName(loginName);
        requestDto.setAccountId(userId);
        requestDto.setIp(CommonUtil.getIpAddress(request));
        String dev = request.getHeader("dev");
        Byte transferSource = HttpsRequestUtil.getHeaderOfDev(dev);
        requestDto.setTransferSource(transferSource);
        return apiSysService.transit(cpSite, userId, loginName, wallet, requestDto);
    }

    @GetMapping("/protocol")
    @ApiOperation(value = "会员注册,平台协议", notes = "display:是否强制显示网站服务条款，1是，0否;serviceTerms:网站服务条款内容")
    public R protocol(HttpServletRequest request) {
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        SysWebTerms bb = sysSettingService.getMbrSysWebTerms(cpSite.getSiteCode());
        return R.ok().put("protocol", bb);
    }

    @GetMapping("/depotList")
    @ApiOperation(value = "平台信息列表", notes = "平台信息列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R depotList(HttpServletRequest request, @RequestParam(value = "terminal", required = false) Byte terminal) {
        if (StringUtils.isEmpty(terminal)) terminal = ApiConstants.Terminal.pc;
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put("tGmDepots", tGmDepotService.findDepotList(userId, terminal));
    }

    @GetMapping("/catLabelList")
    @ApiOperation(value = "电子类别与标签", notes = "电子类别与标签")
    public R catList(@RequestParam("depotId") Integer depotId) {
        return R.ok().put("categorys", tGmCatService.queryCatLabelList(depotId));
    }

    @GetMapping("/catDepotList")
    @ApiOperation(value = "根据游戏类别查询那些平台有此类游戏()", notes = "1,体育3,真人12,彩票")
    public R catDepotList(@RequestParam("catId") Integer catId, @RequestParam(value = "terminal", required = false) Byte terminal) {
        return R.ok().put("catDepots", tGmCatService.queryDepotCat(catId, terminal));
    }

    @GetMapping("/gameList")
    @ApiOperation(value = "游戏列表", notes = "游戏列表")
    public R gamelist(@ModelAttribute ElecGameDto elecGameDto, @RequestParam("pageNo") @NotNull Integer pageNo,
                      @RequestParam("pageSize") @NotNull Integer pageSize) {
        if (StringUtils.isEmpty(elecGameDto.getTerminal())) {
            elecGameDto.setTerminal(ApiConstants.Terminal.pc);
        }
        return R.ok().put("page", tGmGameService.queryWebListPage(elecGameDto, pageNo, pageSize));
    }

    @GetMapping("/gameAllList")
    @ApiOperation(value = "最热门游戏列表", notes = "最热门游戏列表")
    public R gameAllList(@RequestParam("pageNumber") @NotNull int pageNumber) {
        return R.ok().put("page", tGmGameService.gameAllList(pageNumber));
    }

    @GetMapping("/catList")
    @ApiOperation(value = "彩票类别下彩票列表", notes = "彩票列表")
    public R lotteryList(@RequestParam("depotId") @NotNull Integer depotId) {
        Integer catId = new Integer(12);
        return R.ok().put("page", tGmGameService.queryCatGameList(depotId, catId));
    }

    @GetMapping("/elecDepotList")
    @ApiOperation(value = "电子平台信息列表", notes = "电子平台信息列表")
    public R elecDepotList(@RequestParam(value = "terminal", required = false) Byte terminal) {
        if (StringUtils.isEmpty(terminal)) terminal = ApiConstants.Terminal.pc;
        return R.ok().put("tGmDepots", tGmDepotService.findelecDepotList(terminal));
    }

    @GetMapping("/banks")
    @ApiOperation(value = "取款银行列表", notes = "取款银行列表")
    public R getBanks() {
        return R.ok().put("banks", baseBankService.selectWd());
    }

    @GetMapping("/provs")
    @ApiOperation(value = "地址省份", notes = "地址省份")
    public R getProvs() {
        BaseArea sysBaseArea = new BaseArea();
        return R.ok().put("provs", baseAreaService.findArea(sysBaseArea));
    }

    @GetMapping("/citys")
    @ApiOperation(value = "地址城市", notes = "地址城市")
    public R getCitys(@RequestParam("prov") String prov) {
        BaseArea sysBaseArea = new BaseArea();
        sysBaseArea.setProv(prov);
        return R.ok().put("citys", baseAreaService.findArea(sysBaseArea));
    }

    @GetMapping("/noticeList")
    @ApiOperation(value = "系统公告", notes = "系统公告")
    public R list(@RequestParam("pageNo") @NotNull Integer pageNo,
                  @RequestParam("pageSize") @NotNull Integer pageSize,
                  @ApiParam(value = "公告类型,0：公告(跑马灯),1：广播(弹窗)") @RequestParam(value = "showType", required = false) String showType) {
        return R.ok().putPage(oprNoticeService.queryNoticeListPage(showType, pageNo, pageSize, ""));
    }

    @GetMapping("/getJackPot")
    @ApiOperation(value = "系统奖池", notes = "系统奖池")
    public R getJackPot() {
        return ptService.getJackPot();
    }

    @GetMapping("/topWinerList")
    @ApiOperation(value = "中奖排行榜", notes = "中奖排行榜")
    public R getTopWiner(String startDate, String endDate, Integer rows) {
        return R.ok().put("winers", tWinTopService.topWinerList(startDate, endDate, rows));
    }

    @GetMapping("/ActivityList")
    @ApiOperation(value = "活动记录", notes = "活动记录")
    public R ActivityList(@RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, @RequestParam("actCatId") @NotNull Integer actCatId, @ApiParam("默认为PC ,PC 0,手机为 1") @RequestParam(value = "terminal", required = false) Byte terminal, HttpServletRequest request) {
        return R.ok().put("page", oprActActivityService.webActivityList(pageNo, pageSize, actCatId, null, terminal));
    }

    @GetMapping("/ActivityCatList")
    @ApiOperation(value = "查询活动分类", notes = "查询所有分类")
    public R listAll() {
        OprActCat actCat = new OprActCat();
        actCat.setAvailable(Available.enable);
        return R.ok().putPage(oprActCatService.queryListCond(actCat));
    }

    @GetMapping("/indexadvList")
    @ApiOperation(value = "首页活广告", notes = "首页活广告")
    public R indexadvList(@ApiParam("广告类型: 1：首页，2：真人，3：电子，4：体育，5：彩票，6： 手机") @RequestParam(value = "advType", required = false) Integer advType,
                          @ApiParam("模板Id, 1-12值") @RequestParam(value = "evebNum", required = false) Integer evebNum,
                          @ApiParam("默认为PC ,PC 0,手机为 1") @RequestParam(value = "terminal", required = false) Byte terminal) {
        AdvBanner oprAdv = new AdvBanner();
        oprAdv.setAdvType(advType);
        oprAdv.setEvebNum(evebNum);
        if (!StringUtils.isEmpty(terminal) && terminal == ApiConstants.Terminal.mobile) {
            oprAdv.setClientShow(AdvConstant.CLIENT_MB);
        } else {
            oprAdv.setClientShow(AdvConstant.CLIENT_PC);
        }
        return R.ok().putPage(oprAdvService.queryWebOprAdvOrBannerList(oprAdv));
    }

    @GetMapping("/coupletList")
    @ApiOperation(value = "对联列表", notes = "对联列表")
    public R coupletList() {
        return R.ok().putPage(oprAdvService.coupletList());
    }

    @GetMapping("/secondadvList")
    @ApiOperation(value = "二级页面活广告", notes = "二级页面活广告")
    public R secondadvList(@ApiParam("游戏分类:1真人，2电子，3，彩票，4手机") @RequestParam(value = "gameCat", required = false) Byte gameCat, @ApiParam("平台Id号") @RequestParam(value = "depotId", required = false) Integer depotId, @ApiParam("默认为PC ,PC 0,手机为 1") @RequestParam(value = "terminal", required = false) Byte terminal) {
        ToprAdv oprAdv = new ToprAdv();
        oprAdv.setAdvType(AdvConstant.ADV_ACROUSEL);
        if (!StringUtils.isEmpty(terminal) && terminal == ApiConstants.Terminal.mobile) {
            oprAdv.setClientShow(AdvConstant.CLIENT_MB);
        } else {
            oprAdv.setClientShow(AdvConstant.CLIENT_PC);
        }
        oprAdv.setGameCat(gameCat);
        oprAdv.setDepotId(depotId);
        return R.ok().putPage(toprAdvService.queryWebOprAdvList(oprAdv));
    }

    @GetMapping("/getSerUrl")
    @ApiOperation(value = "返回客服链接", notes = "返回客服链接")
    public R getSerUrl(@ApiParam("默认为PC ,PC 0,手机为 1") @RequestParam(value = "terminal", required = false) Byte terminal) {
        return R.ok().put(sysSettingService.getCustomerSerUrl(terminal));
    }

    @PostMapping("/applySite")
    @ApiOperation(value = "站点测试转正式站点", notes = "站点测试转正式站点")
    public R applySite(@RequestBody TCpSiteDto tCpSiteDto, HttpServletRequest request) {
        Assert.isSiteCode(tCpSiteDto.getSiteCode(), "站点代码不能为空,长度不能大于4位!", 1, 4);
        Assert.isLenght(tCpSiteDto.getSiteName(), "站点名称不能为空，长度不能大于16!", 1, 16);
        Assert.isLenght(tCpSiteDto.getSiteUrl(), "站点URL不能为空!", 1, 100);
        Assert.isBlank(tCpSiteDto.getStartDate(), "站点开始时间不能为空!");
        Assert.isBlank(tCpSiteDto.getEndDate(), "站点结束时间不能为空!");
        Assert.isLenght(tCpSiteDto.getMemo(), "备注最大长度为100!", 0, 100);
        Assert.isLenght(tCpSiteDto.getCompanyUser(), "商户账户不能为空,长度最大为16位!", 1, 16);
        TCpSite tCpSite = new TCpSite();
        try {
            BeanUtils.copyProperties(tCpSite, tCpSiteDto);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RRException("内部数据异常!");
        }
        tCpSite.setAvailable(Available.enable);
        tCpSite.setCurrency(ApiConstants.CURRENCY_TYPE_RMB);
        tCpSite.setIsapi(Available.disable);
        tCpSite.setSchemaName(tCpSite.getSiteCode());
        tCpSite.setCreateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        request.setAttribute("schemaName", "manage");
        tCpSiteService.saveCpSite(tCpSite);
        return R.ok();
    }

    /**
     * 前端获取用户查询基本信息查询天数及范围
     */
    @RequestMapping(value = "/queryConfigDaysAndScope", method = RequestMethod.GET)
    @ApiOperation(value = "queryConfigDaysAndScope", notes = "queryRegisterSet")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryConfigDaysAndScope() {
        StationSet stationSet = sysSettingService.queryConfigDaysAndScope();
        if (Objects.isNull(stationSet)) throw new RRException("系统配置异常，请联系管理员!");
        return R.ok().put(stationSet);
    }

    /**
     * 投注记录查询游戏分类的联动下拉列表
     *
     * @return
     */
    @GetMapping("/findGameCatList")
    @ApiOperation(value = "投注记录查询游戏分类的联动下拉列表", notes = "投注记录查询游戏分类的联动下拉列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findGameCatList(@RequestParam(value = "platFormId", required = false) String platFormId) {
        if (platFormId != null && !platFormId.trim().equals("")) {
            return R.ok().put("page", analysisService.getGameType(platFormId, 0));
        }
        return R.ok();
    }


    @GetMapping("/findFreeWalletSwitch")
    @ApiOperation(value = "查询免转开关", notes = "查询免转开关")
    public R findFreeWalletSwitch() {
        SysSetting setting = sysSettingService.getSysSetting(SystemConstants.FREE_WALLETSWITCH);
        setting.setSysvalue("1");
        return R.ok().put(setting);
    }

}
