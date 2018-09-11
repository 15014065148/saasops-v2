package com.eveb.saasops.modules.operate.dao;

import com.eveb.saasops.modules.operate.entity.OprRecMbr;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import com.eveb.saasops.modules.base.mapper.MyMapper;

@Mapper
public interface OprRecMbrMapper extends MyMapper<OprRecMbr> {
	
	//站内信列表
    List<OprRecMbr> queryMbrMesList(OprRecMbr oprRecMbr);
	
	List<OprRecMbr> queryAgyMesList(OprRecMbr oprRecMbr);

    List<OprRecMbr> queryAllList(OprRecMbr oprRecMbr);

    void deleteOprRecMbr();
}
