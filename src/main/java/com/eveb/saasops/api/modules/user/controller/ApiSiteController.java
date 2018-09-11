package com.eveb.saasops.api.modules.user.controller;

import com.eveb.saasops.api.modules.apisys.service.TCpSiteService;
import com.eveb.saasops.common.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/site")
@Api(value = "api", description = "官网创建站点")
public class ApiSiteController {

    @Autowired
    TCpSiteService tCpSiteService;

    @PostMapping("/createNewSite")
    @ResponseBody
    public R createNewSite(@RequestParam(name = "siteCode") @ApiParam("站点Code 三个字母") String siteCode
            , @RequestParam(name = "url") @ApiParam("域名") String url
            , @RequestParam(name = "siteName") @ApiParam("站点名称") String siteName
            , @RequestParam(name = "startTime") @ApiParam("开始时间 yyyy-MM-dd HH:mm:ss") String startTime
            , @RequestParam(name = "endTime") @ApiParam("结束时间 yyyy-MM-dd HH:mm:ss") String endTime
            , @RequestParam(name = "currency") @ApiParam("币种") String currency
            , @RequestParam(name = "compantId") @ApiParam("公司Id") Integer compantId,@RequestParam(name = "userName") @ApiParam("用户名称") String userName
            , HttpServletRequest request){
        request.setAttribute("schemaName","manage");
        return tCpSiteService.addSiteCode(siteCode,url,siteName,startTime,endTime,currency,compantId,userName);
    }
}
