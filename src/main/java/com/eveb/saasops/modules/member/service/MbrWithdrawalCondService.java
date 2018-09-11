package com.eveb.saasops.modules.member.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.modules.base.service.BaseService;
import com.eveb.saasops.modules.member.dao.MbrWithdrawalCondMapper;
import com.eveb.saasops.modules.member.entity.MbrAccount;
import com.eveb.saasops.modules.member.entity.MbrWithdrawalCond;
import com.github.pagehelper.PageHelper;

@Service
public class MbrWithdrawalCondService extends BaseService<MbrWithdrawalCondMapper, MbrWithdrawalCond> {
	@Autowired
	MbrAccountService mbrAccountService;

	public PageUtils queryListPage(MbrWithdrawalCond mbrWithdrawalCond, Integer pageNo, Integer pageSize,
			String orderBy) {
		PageHelper.startPage(pageNo, pageSize);
		if (!StringUtils.isEmpty(orderBy))
			PageHelper.orderBy(orderBy);
		List<MbrWithdrawalCond> list = queryListCond(mbrWithdrawalCond);
		return BeanUtil.toPagedResult(list);
	}

	public int selectCountNo(Integer groupId) {
		MbrWithdrawalCond mbrWithdrawalCond = new MbrWithdrawalCond();
		mbrWithdrawalCond.setGroupId(groupId);
		return super.selectCount(mbrWithdrawalCond);
	}

	public MbrWithdrawalCond getMbrWithDrawal(Integer accountId) {
		MbrAccount mbrAccount = mbrAccountService.getAccountInfo(accountId);
		MbrWithdrawalCond mbrDepositCond = new MbrWithdrawalCond();
		mbrDepositCond.setGroupId(mbrAccount.getGroupId());
		return super.queryObjectCond(mbrDepositCond);
	}
}
