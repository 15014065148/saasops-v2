package com.eveb.saasops.modules.member.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.eveb.saasops.modules.base.service.BaseService;
import com.eveb.saasops.modules.member.dao.MbrBillManageMapper;
import com.eveb.saasops.modules.member.entity.MbrBillManage;
import com.eveb.saasops.modules.member.mapper.MbrMapper;

@Service
public class MbrBillManageService extends BaseService<MbrBillManageMapper, MbrBillManage> {
	@Autowired
	private MbrMapper mbrMapper;
	public MbrBillManage queryOrderNo(Long orderNo)
	{
		return mbrMapper.findOrder(6, orderNo);
	}
	
	public int updateStatus(MbrBillManage mbrBillManage)
	{
		return mbrMapper.updateBillManageStatus(mbrBillManage);
	}
}
