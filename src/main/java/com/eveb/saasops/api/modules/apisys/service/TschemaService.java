package com.eveb.saasops.api.modules.apisys.service;

import com.eveb.saasops.api.modules.apisys.dao.TschemaMapper;
import com.eveb.saasops.api.modules.apisys.entity.Tschema;
import com.eveb.saasops.api.modules.apisys.mapper.ApiSysMapper;
import com.eveb.saasops.modules.base.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TschemaService  extends BaseService<TschemaMapper, Tschema> {

    @Autowired
    TschemaMapper tschemaMapper;
    @Autowired
    ApiSysMapper apiSysMapper;

    public Tschema selectOne(Tschema tschema){
        return tschemaMapper.selectOne(tschema);
    }

    public int selectCount(Tschema tschema){
        return super.selectCount(tschema);
    }

    public int updateTschemaSiteCode(Tschema tschema) {
        return apiSysMapper.updateTschemaSiteCode(tschema);
    }
}
