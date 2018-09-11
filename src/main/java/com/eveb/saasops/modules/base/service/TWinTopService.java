package com.eveb.saasops.modules.base.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eveb.saasops.modules.base.dao.TWinTopMapper;
import com.eveb.saasops.modules.base.entity.TWinTop;
import com.eveb.saasops.modules.base.mapper.BaseMapper;

@Service
public class TWinTopService extends BaseService<TWinTopMapper, TWinTop> {

	@Autowired
	BaseMapper baseMapper;

	public List<TWinTop> topWinerList(String startDate, String endDate, Integer rows) {
		return baseMapper.findTopWinList(startDate, endDate, rows);
	}
}
