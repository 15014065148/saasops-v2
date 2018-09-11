package com.eveb.saasops.modules.operate.dao;

import com.eveb.saasops.modules.operate.entity.AdvBanner;
import com.eveb.saasops.modules.operate.entity.OprAdv;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import com.eveb.saasops.modules.base.mapper.MyMapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface OprAdvMapper extends MyMapper<OprAdv> {

	void deleteByIds(Map<String, Object> map);

	List<OprAdv> queryOprAdvList(OprAdv oprAdv);

	List<OprAdv> queryOprAdvByAvailable(@Param("advType") Integer advType);
	
	OprAdv queryWebOprAdvList(OprAdv oprAdv);

	List<AdvBanner> queryAdvBannerDtoList(AdvBanner advBannerDto);

	void deleteImageById(@Param("id") Integer id);

	int insertAdvInfo(OprAdv oprAdv);

	List<OprAdv> coupletList();

	OprAdv queryOprAdvInfo(@Param("id") Integer id);

	int updateOprAdvAvailable(OprAdv oprAdv);
}
