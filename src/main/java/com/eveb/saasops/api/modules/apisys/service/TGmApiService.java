package com.eveb.saasops.api.modules.apisys.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.modules.apisys.dao.TGmApiMapper;
import com.eveb.saasops.api.modules.apisys.dto.ProxyProperty;
import com.eveb.saasops.api.modules.apisys.entity.SsysConfig;
import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.apisys.mapper.ApiSysMapper;
import com.eveb.saasops.modules.base.service.BaseService;
import com.google.gson.Gson;


@Service
public class TGmApiService extends BaseService<TGmApiMapper, TGmApi>{
	
	@Autowired
	ApiSysMapper apiSysMapper;

	@SuppressWarnings("unchecked")
	@Cacheable(cacheNames=ApiConstants.REDIS_GAME_API_CACHE_KEY, key="#siteCode+'_'+#depotId")
	public TGmApi queryApiObject(Integer depotId,String siteCode)
	{
		//FIXME
		TGmApi gmApi = apiSysMapper.findGmApiOne(depotId, siteCode);
		if (Objects.nonNull(gmApi) && !StringUtils.isEmpty(gmApi.getSecureCode())) {
			gmApi.setSecureCodes((Map<String, String>) JSON.parse(gmApi.getSecureCode()));
		}
		return gmApi;
	}
	@Cacheable(cacheNames = ApiConstants.REDIS_PROXY_CATCH)
	public List<SsysConfig> queryList(String groupApi)
	{
		List<SsysConfig> apiSystemList=apiSysMapper.listSysConfig(groupApi);
		apiSystemList.forEach(e->{e.setProxyProperty(new Gson().fromJson(e.getValues(), ProxyProperty.class));});
		return apiSystemList;
	}
}
