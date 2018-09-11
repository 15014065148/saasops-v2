package com.eveb.saasops.modules.base.service;

import java.util.ArrayList;
import java.util.List;

import com.eveb.saasops.modules.base.dao.PayBankRelationMapper;
import com.eveb.saasops.modules.base.entity.PayBankRelation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eveb.saasops.common.constants.Constants.Available;
import com.eveb.saasops.modules.base.dao.BaseBankMapper;
import com.eveb.saasops.modules.base.entity.BaseBank;

@Service
public class BaseBankService {
	@Autowired
	private BaseBankMapper baseBankMapper;

	@Autowired
	private PayBankRelationMapper payBankRelationMapper;

	public BaseBank queryObject(Integer id) {
		return baseBankMapper.selectByPrimaryKey(id);
	}

	public List<BaseBank> selectAll() {
		return baseBankMapper.selectAll();
	}

	public List<BaseBank> select(BaseBank b) {
		return baseBankMapper.select(b);
	}

	public void save(BaseBank baseBank) {
		baseBankMapper.insert(baseBank);
	}

	public void update(BaseBank baseBank) {
		baseBankMapper.updateByPrimaryKeySelective(baseBank);
	}

	public List<BaseBank> selectWd() {
		BaseBank baseBank = new BaseBank();
		baseBank.setWDEnable(Available.enable);
		return baseBankMapper.select(baseBank);
	}

	public List<PayBankRelation> selectPayBankRelation(PayBankRelation p){ return payBankRelationMapper.select(p);}

	public Object getBankList() {	
		List<BaseBank> list = baseBankMapper.selectAll();
		List<BaseBank> bList = new ArrayList<>();
		list.forEach(e->{
			if(e.getBankName().contains("行")) {
				bList.add(e);
			}
		});
		return bList;
	}

	public Object getOnLineList() {
		List<BaseBank> list = baseBankMapper.selectAll();
		List<BaseBank> oList = new ArrayList<>();
		list.forEach(e->{
			if(!e.getBankName().contains("行")) {
				oList.add(e);
			}
		});
		return oList;
	}

}
