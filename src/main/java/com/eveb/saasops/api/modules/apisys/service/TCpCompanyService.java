package com.eveb.saasops.api.modules.apisys.service;

import com.eveb.saasops.api.config.EvebConfig;
import com.eveb.saasops.api.modules.apisys.dao.TCpCompanyMapper;
import com.eveb.saasops.api.modules.apisys.dto.EvebUrlDto;
import com.eveb.saasops.api.modules.apisys.dto.EveburlRes;
import com.eveb.saasops.api.modules.apisys.entity.TCpCompany;
import com.eveb.saasops.api.modules.user.service.OkHttpService;
import com.eveb.saasops.modules.base.service.BaseService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


@Service
@Slf4j
public class TCpCompanyService extends BaseService<TCpCompanyMapper, TCpCompany> {
    @Autowired
    EvebConfig evebConfig;
    @Autowired
    private OkHttpService okHttpService;

    public Boolean upEvebUrl(String siteCode, String url) {
        String param = getEvebUrlDto(siteCode, url);
        String result = okHttpService.post(evebConfig.getUrl() + param);
        log.debug("EVEB —>模块中心更新URL [提交参数 " + evebConfig.getUrl() + param + " 返回结果 " + result + "]");
        if (!StringUtils.isEmpty(result)) {
            EveburlRes res = new Gson().fromJson(result, EveburlRes.class);
            if (!StringUtils.isEmpty(res.getStatus()) && res.getStatus().equals("200")) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    private String getEvebUrlDto(String siteCode, String url) {
        EvebUrlDto evebUrlDto = new EvebUrlDto();
        evebUrlDto.setSiteCode(siteCode);
        evebUrlDto.setUrl(url);
        return evebUrlDto.toString();
    }
}