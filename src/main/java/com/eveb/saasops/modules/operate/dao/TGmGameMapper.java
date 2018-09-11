package com.eveb.saasops.modules.operate.dao;

import com.eveb.saasops.modules.operate.entity.TGmCat;
import com.eveb.saasops.modules.operate.entity.TGmDepot;
import com.eveb.saasops.modules.operate.entity.TGmGame;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import com.eveb.saasops.modules.base.mapper.MyMapper;


@Mapper
public interface TGmGameMapper extends MyMapper<TGmGame> {

	public List<TGmCat> queryCatList(TGmDepot tGmDepot);

}
