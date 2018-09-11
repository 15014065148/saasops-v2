package com.eveb.saasops.api.modules.apisys.service;

import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.modules.apisys.dao.TCpSiteMapper;
import com.eveb.saasops.api.modules.apisys.dao.TcpSiteurlMapper;
import com.eveb.saasops.api.modules.apisys.entity.TCpSite;
import com.eveb.saasops.api.modules.apisys.entity.TGmApiprefix;
import com.eveb.saasops.api.modules.apisys.entity.TcpSiteurl;
import com.eveb.saasops.api.modules.apisys.entity.Tschema;
import com.eveb.saasops.api.modules.apisys.mapper.ApiSysMapper;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.DateUtil;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.modules.base.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TCpSiteService extends BaseService<TCpSiteMapper, TCpSite> {

	/**<schemaName,siteCode>*/
	public static Map<String,String> schemaName = new ConcurrentHashMap<>();

	/**<siteCode,schemaName>*/
	public static Map<String,String> siteCode = new ConcurrentHashMap<>();

	@Autowired
	ApiSysMapper apiSysMapper;
	@Autowired
	TcpSiteurlMapper tcpSiteurlMapper;
    @Autowired
    TschemaService tschemaService;

	public TCpSite queryOneCond(String url) {
		return apiSysMapper.findCpSiteOne(url);
	}

	@Cacheable(cacheNames = ApiConstants.REDIS_GAME_SITECODE_CACHE_KEY, key = "#siteCode")
	public TCpSite queryPreOneCond(String siteCode) {
		TCpSite cpSite = new TCpSite();
		cpSite.setSiteCode(siteCode);
		return apiSysMapper.getCpSiteBySiteCode(siteCode);
	}


	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public void saveCpSite(TCpSite tCpSite) {
		Tschema tschema = apiSysMapper.selectTschemaOne();
		if (StringUtils.isEmpty(tschema))
			throw new RRException("没有可用的资料库,请管理员新增资料库!");
		if (apiSysMapper.updateSchema(tschema.getId(), tCpSite.getSiteCode()) > 0) {
			super.save(tCpSite);
			tcpSiteurlMapper.insert(getTcpSiteurl(tCpSite));
		} else {
			throw new RRException("更新资料库失败，请稍后再试!");
		}

	}

	@Cacheable(cacheNames = ApiConstants.SITE_CODE, key = "#SchemaName")
	public String getSiteCode(String SchemaName){
		TCpSite cpSite = new TCpSite();
		cpSite.setSchemaName(SchemaName);
		cpSite =super.queryObjectCond(cpSite);
		return cpSite.getSiteCode();
	}


	/**
	 * 初始化SchemaName 和SiteCode 的对应关系 key=schemaName , value =siteCode
	 * 初始化SiteCode 和SchemaName 的对应关系 key=siteCode , value =schemaName
	 * @return
	 */
	@Bean
	public Map<String,String> initSchemaName(){
		List<TCpSite> tCpSites=apiSysMapper.findCpSite();
		schemaName.clear();
		siteCode.clear();
		for (TCpSite tCpSite: tCpSites) {
			schemaName.put(tCpSite.getSchemaName(),tCpSite.getSiteCode());
			siteCode.put(tCpSite.getSiteCode(),tCpSite.getSchemaName());
		}
		return schemaName;
	}


	private TcpSiteurl getTcpSiteurl(TCpSite tCpSite) {
		TcpSiteurl tcpSiteurl = new TcpSiteurl();
		tcpSiteurl.setSiteId(tCpSite.getId());
		tcpSiteurl.setSiteCode(tCpSite.getSiteCode());
		tcpSiteurl.setSiteUrl(tCpSite.getSiteUrl());
		return tcpSiteurl;
	}

    /**
     * 添加SiteCode
     * @param siteCode
     * @param url  xxx.xxx.com
     * @return
     */
    @Transactional
	public R addSiteCode(String siteCode, String url,String siteName
            ,String startTime,String endTime
            ,String currency,Integer compantId,String userName){
        if(siteCode.length() > 3 ){
            return R.ok().put("2000","站点前缀过长");
        }
        Tschema tschema = apiSysMapper.selectTschemaOne();
        if(tschema == null ){
            return R.ok().put("2000","已无预备站点，请联系客服");
        }
        tschema.setSiteCode(siteCode);
        tschema.setIsUsed(new Byte("1"));
        int i =tschemaService.updateTschemaSiteCode(tschema);
        if(i == 0){
            return R.ok().put("2000","站点前缀已被使用");
        }
        /*插入sitecode 和 schemaName 的对应关系*/
        TCpSite tCpSite =new TCpSite();
        tCpSite.setSiteCode(siteCode);
        tCpSite.setSchemaName(tschema.getSimpleName());
        tCpSite.setIsapi(new Byte("0"));
        tCpSite.setAvailable(new Byte("1"));
        String currentDate=DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME);
        tCpSite.setCreateTime(currentDate);
        tCpSite.setMemo(siteName);
        tCpSite.setCurrency(currency);
        tCpSite.setSiteName(siteName);
        tCpSite.setCreateUser(userName);
        tCpSite.setModifyTime(currentDate);
        tCpSite.setStartDate(startTime);
        tCpSite.setEndDate(endTime);
        tCpSite.setCompanyId(compantId);
        super.save(tCpSite);
        /*插入siteCode 和域名的对应关系*/
        TcpSiteurl tcpSiteurl =new TcpSiteurl();
        tcpSiteurl.setSiteCode(siteCode);
        tcpSiteurl.setSiteUrl(url);
        tcpSiteurl.setSiteId(tCpSite.getId());
        tcpSiteurlMapper.insert(tcpSiteurl);
        /*插入apiPrefix 和siteCode 的对应关系*/
        List<TGmApiprefix> models=apiSysMapper.selectApiPrefixByModel();
        models.forEach(model -> {
            apiSysMapper.insertApiPrefix(new TGmApiprefix(model.getApiId(),model.getPrefix().replace("model",siteCode),tCpSite.getId(),new Byte("1"),userName,userName));
        });
        this.schemaName.put(tCpSite.getSchemaName(),tCpSite.getSiteCode());
        return R.ok().put("域名设置成功");
    }
}
