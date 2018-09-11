package com.eveb.saasops.modules.base.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.eveb.saasops.modules.base.entity.ToprAdv;
import com.eveb.saasops.modules.base.mapper.MyMapper;


@Mapper
public interface ToprAdvMapper extends MyMapper<ToprAdv> {


	List<ToprAdv> queryWebOprAdvList(ToprAdv oprAdv);

}
