package com.eveb.saasops.modules.base.mapper;

import java.util.List;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.eveb.saasops.modules.base.entity.BaseArea;
import com.eveb.saasops.modules.base.entity.TWinTop;
import com.eveb.saasops.modules.base.entity.ToprAdv;

@Mapper
public interface BaseMapper {
	List<BaseArea> findBaseArea(BaseArea baseArea);
	
	List<TWinTop> findTopWinList(@Param("startDate")String startDate,@Param("endDate") String endDate,@Param("rows") Integer rows);
	
	List<ToprAdv> queryWebOprAdvList(ToprAdv oprAdv);
}
