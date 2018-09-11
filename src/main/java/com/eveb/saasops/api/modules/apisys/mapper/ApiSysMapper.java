package com.eveb.saasops.api.modules.apisys.mapper;


import com.eveb.saasops.api.modules.apisys.entity.*;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApiSysMapper {

	TCpSite findCpSiteOne(@Param("siteUrl") String siteUrl);

	List<TCpSite> findCpSite();

	TGmApi findGmApiOne(@Param("depotId") Integer depotId, @Param("siteCode") String siteCode);

	Integer updateSchema(@Param("id") Integer id, @Param("siteCode") String siteCode);

	Tschema selectTschemaOne();
	
    List<SsysConfig> listSysConfig(@Param("groups") String groups);

    String getCpSiteCode(@Param("siteCode") String siteCode);

	TCpSite getCpSiteBySiteCode(@Param("siteCode") String siteCode);

    int updateTschemaSiteCode(Tschema tschema);


    int insertApiPrefix(TGmApiprefix tGmApiprefix);

	List<TGmApiprefix> selectApiPrefixByModel();
}
