package com.eveb.saasops.api.modules.unity.service;

import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.apisys.service.TGmApiService;
import com.eveb.saasops.api.modules.unity.dto.LoginModel;
import com.eveb.saasops.api.modules.unity.dto.PlayGameModel;
import com.eveb.saasops.api.modules.unity.dto.RegisterModel;
import com.eveb.saasops.api.modules.unity.dto.TransferModel;
import com.eveb.saasops.api.modules.user.service.OkHttpService;
import com.eveb.saasops.common.exception.R200Exception;
import com.eveb.saasops.common.validator.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class GameDepotService {

    @Autowired
    public OkHttpService okHttpService;

    @Autowired
    private TGmApiService apiService;

    public String createMember(RegisterModel registerModel){
        log.error("siteCode===="+registerModel.getSiteCode());
        TGmApi api = apiService.queryApiObject(registerModel.getDepotId(), registerModel.getSiteCode());
        log.error("api apiUrl========="+api.getApiUrl());
        log.error("api info======="+api.toString());
        Assert.isNull(api, "api线路不存在");
        StringBuffer url = new StringBuffer();
        url.append(api.getApiUrl());
        url.append(api.getDepotCode());
        url.append("/createMember");

        Map<String, String> map = new HashMap();
        map.put("siteCode", registerModel.getSiteCode());
        map.put("userName", registerModel.getUserName());

        log.error("请求url==="+url.toString());
        String resultString;
        try {
            resultString = okHttpService.postJson(url.toString(), map);
            log.error("创建会员返回值========"+resultString);
        } catch (Exception e) {
            log.error("调用接口异常");
            throw new R200Exception("调用接口异常");
        }
        return resultString;
    }


    public String login(LoginModel loginModel){
        TGmApi api = apiService.queryApiObject(loginModel.getDepotId(), loginModel.getSiteCode());
        Assert.isNull(api, "api线路不存在");
        StringBuffer url = new StringBuffer();
        url.append(api.getApiUrl());
        url.append(api.getDepotCode());
        url.append("/login");

        Map<String, String> map = new HashMap();
        map.put("siteCode", loginModel.getSiteCode());
        map.put("userName", loginModel.getUserName());

        String resultString;
        try {
            resultString = okHttpService.postJson(url.toString(), map);
            log.error("会员登录返回值========"+resultString);
        } catch (Exception e) {
            log.error("调用接口异常");
            throw new R200Exception("调用接口异常");
        }
        return resultString;
    }

    public String logout(LoginModel loginModel){
        TGmApi api = apiService.queryApiObject(loginModel.getDepotId(), loginModel.getSiteCode());
        Assert.isNull(api, "api线路不存在");
        StringBuffer url = new StringBuffer();
        url.append(api.getApiUrl());
        url.append(api.getDepotCode());
        url.append("/logout");

        Map<String, String> map = new HashMap();
        map.put("siteCode", loginModel.getSiteCode());
        map.put("userName", loginModel.getUserName());

        String resultString;
        try {
            resultString = okHttpService.postJson(url.toString(), map);
            log.error("会员登出返回值========"+resultString);
        } catch (Exception e) {
            log.error("调用接口异常");
            throw new R200Exception("调用接口异常");
        }
        return resultString;
    }


    public String deposit(TransferModel transferModel){
        TGmApi api = apiService.queryApiObject(transferModel.getDepotId(), transferModel.getSiteCode());
        Assert.isNull(api, "api线路不存在");
        StringBuffer url = new StringBuffer();
        url.append(api.getApiUrl());
        url.append(api.getDepotCode());
        url.append("/deposit");

        Map<String, String> map = new HashMap();
        map.put("siteCode", transferModel.getSiteCode());
        map.put("userName", transferModel.getUserName());
        map.put("orderNo", transferModel.getOrderNo());
        map.put("amount", transferModel.getAmount().toString());

        String resultString;
        try {
            resultString = okHttpService.postJson(url.toString(), map);
            log.error("存款返回值========"+resultString);
        } catch (Exception e) {
            log.error("调用接口异常");
            throw new R200Exception("调用接口异常");
        }
        return resultString;
    }


    public String withdrawal(TransferModel transferModel){
        TGmApi api = apiService.queryApiObject(transferModel.getDepotId(), transferModel.getSiteCode());
        Assert.isNull(api, "api线路不存在");
        StringBuffer url = new StringBuffer();
        url.append(api.getApiUrl());
        url.append(api.getDepotCode());
        url.append("/withdrawal");

        Map<String, String> map = new HashMap();
        map.put("siteCode", transferModel.getSiteCode());
        map.put("userName", transferModel.getUserName());
        map.put("orderNo", transferModel.getOrderNo());
        map.put("amount", transferModel.getAmount().toString());

        String resultString;
        try {
            resultString = okHttpService.postJson(url.toString(), map);
            log.error("取款返回值========"+resultString);
        } catch (Exception e) {
            log.error("调用接口异常");
            throw new R200Exception("调用接口异常");
        }
        return resultString;
    }



    public String queryBalance(LoginModel loginModel){
        TGmApi api = apiService.queryApiObject(loginModel.getDepotId(), loginModel.getSiteCode());
        Assert.isNull(api, "api线路不存在");
        StringBuffer url = new StringBuffer();
        url.append(api.getApiUrl());
        url.append(api.getDepotCode());
        url.append("/queryBalance");

        Map<String, String> map = new HashMap();
        map.put("siteCode", loginModel.getSiteCode());
        map.put("userName", loginModel.getUserName());

        String resultString;
        try {
            resultString = okHttpService.postJson(url.toString(), map);
            log.error("查询用户余额返回值========"+resultString);
        } catch (Exception e) {
            log.error("调用接口异常");
            throw new R200Exception("调用接口异常");
        }
        return resultString;
    }


    public String checkTransfer(TransferModel transferModel){
        TGmApi api = apiService.queryApiObject(transferModel.getDepotId(), transferModel.getSiteCode());
        Assert.isNull(api, "api线路不存在");
        StringBuffer url = new StringBuffer();
        url.append(api.getApiUrl());
        url.append(api.getDepotCode());
        url.append("/checkTransfer");

        Map<String, String> map = new HashMap();
        map.put("siteCode", transferModel.getSiteCode());
        map.put("userName", transferModel.getUserName());
        map.put("orderNo", transferModel.getOrderNo());

        String resultString;
        try {
            resultString = okHttpService.postJson(url.toString(), map);
            log.error("确认转账返回值========"+resultString);
        } catch (Exception e) {
            log.error("调用接口异常");
            throw new R200Exception("调用接口异常");
        }
        return resultString;
    }


    public String playGame(PlayGameModel playGameModel){
        TGmApi api = apiService.queryApiObject(playGameModel.getDepotId(), playGameModel.getSiteCode());
        Assert.isNull(api, "api线路不存在");
        StringBuffer url = new StringBuffer();
        url.append(api.getApiUrl());
        url.append(api.getDepotCode());
        url.append("/playGame");

        Map<String, String> map = new HashMap();
        map.put("siteCode", playGameModel.getSiteCode());
        map.put("userName", playGameModel.getUserName());
        map.put("gameType", playGameModel.getGameType());
        map.put("gameId", playGameModel.getGameId());
        map.put("gameCode", playGameModel.getGamecode());

        String resultString;
        try {
            resultString = okHttpService.postJson(url.toString(), map);
            log.error("玩游戏返回值========"+resultString);
        } catch (Exception e) {
            log.error("调用接口异常");
            throw new R200Exception("调用接口异常");
        }
        return resultString;
    }


    public String openHall(PlayGameModel playGameModel){
        TGmApi api = apiService.queryApiObject(playGameModel.getDepotId(), playGameModel.getSiteCode());
        Assert.isNull(api, "api线路不存在");
        StringBuffer url = new StringBuffer();
        url.append(api.getApiUrl());
        url.append(api.getDepotCode());
        url.append("/openHall");

        Map<String, String> map = new HashMap();
        map.put("siteCode", playGameModel.getSiteCode());
        map.put("userName", playGameModel.getUserName());

        String resultString;
        try {
            resultString = okHttpService.postJson(url.toString(), map);
            log.error("打开大厅返回值========"+resultString);
        } catch (Exception e) {
            log.error("调用接口异常");
            throw new R200Exception("调用接口异常");
        }
        return resultString;
    }


    public String tryPlayGame(PlayGameModel playGameModel){
        TGmApi api = apiService.queryApiObject(playGameModel.getDepotId(), playGameModel.getSiteCode());
        Assert.isNull(api, "api线路不存在");
        StringBuffer url = new StringBuffer();
        url.append(api.getApiUrl());
        url.append(api.getDepotCode());
        url.append("/tryPlayGame");

        Map<String, String> map = new HashMap();
        map.put("gameType", playGameModel.getGameType());

        String resultString;
        try {
            resultString = okHttpService.postJson(url.toString(), map);
            log.error("试玩游戏返回值========"+resultString);
        } catch (Exception e) {
            log.error("调用接口异常");
            throw new R200Exception("调用接口异常");
        }
        return resultString;
    }

}
