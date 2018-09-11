package com.eveb.saasops.modules.log.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.modules.base.service.BaseService;
import com.eveb.saasops.modules.log.dao.LogMbrregisterMapper;
import com.eveb.saasops.modules.log.entity.LogMbrRegister;
import com.eveb.saasops.modules.member.service.IpService;
import com.github.pagehelper.PageHelper;

@Service
public class LogMbrregisterService extends BaseService<LogMbrregisterMapper, LogMbrRegister> {

	@Autowired
	IpService ipService;

	public PageUtils queryListPage(LogMbrRegister logMbrregister, Integer pageNo, Integer pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		List<LogMbrRegister> list = queryListCond(logMbrregister);
		return BeanUtil.toPagedResult(list);
	}

	public LogMbrRegister selectOne(LogMbrRegister logMbrregister) {
		LogMbrRegister log = queryObjectCond(logMbrregister);
		if (StringUtils.isEmpty(log.getRegArea())) {
			log.setRegArea(ipService.getIpArea(log.getRegisterIp()));
		}

		return log;
	}

}
