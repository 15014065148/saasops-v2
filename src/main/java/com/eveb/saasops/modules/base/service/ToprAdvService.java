package com.eveb.saasops.modules.base.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eveb.saasops.modules.base.dao.ToprAdvMapper;
import com.eveb.saasops.modules.base.entity.ToprAdv;
import com.eveb.saasops.modules.base.mapper.BaseMapper;


@Service
public class ToprAdvService extends BaseService<ToprAdvMapper, ToprAdv>{

    @Autowired
    BaseMapper baseMapper;

	
	public List<ToprAdv> queryWebOprAdvList(ToprAdv oprAdv)
	{
		return baseMapper.queryWebOprAdvList(oprAdv);
	}

}
