package com.eveb.saasops.modules.member.service;

import static com.eveb.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.eveb.saasops.common.utils.DateUtil.getCurrentDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.eveb.saasops.common.constants.Constants.Available;
import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.common.utils.StringUtil;
import com.eveb.saasops.common.validator.Assert;
import com.eveb.saasops.modules.base.service.BaseService;
import com.eveb.saasops.modules.member.dao.MbrBankcardMapper;
import com.eveb.saasops.modules.member.entity.MbrAccount;
import com.eveb.saasops.modules.member.entity.MbrBankcard;
import com.eveb.saasops.modules.member.mapper.MbrMapper;
import com.github.pagehelper.PageHelper;

@Service
public class MbrBankcardService extends BaseService<MbrBankcardMapper, MbrBankcard> {
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    MbrAccountService mbrAccountService;
    @Autowired
	private MbrAccountLogService accountLogService;

    public PageUtils queryListPage(MbrBankcard mbrBankcard, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy))
            PageHelper.orderBy(orderBy);
        List<MbrBankcard> list = queryListCond(mbrBankcard);
        return BeanUtil.toPagedResult(list);
    }

    public MbrBankcard findMemberCardOne(Integer id) {
        return mbrMapper.findBankCardOne(id);
    }

    public void deleteBatch(Long[] idArr) {
        mbrMapper.updateBankCardBatch(idArr);
    }

    public int countBankNo(Integer accountId) {
        MbrBankcard mbrBankcard = new MbrBankcard();
        mbrBankcard.setAccountId(accountId);
        mbrBankcard.setIsDel(Available.disable);
        mbrBankcard.setAvailable(Available.enable);
        return super.selectCount(mbrBankcard);
    }

	public List<MbrBankcard> ListCondBankCard(Integer accountId) {
		MbrBankcard mbrBankcard = new MbrBankcard();
		mbrBankcard.setAccountId(accountId);
		mbrBankcard.setIsDel(Available.disable);
		mbrBankcard.setAvailable(Available.enable);
		List<MbrBankcard> bankcards = mbrMapper.userBankCard(mbrBankcard);
		bankcards.forEach(e -> {
			e.setCardNo(StringUtil.bankNo(e.getCardNo()));
		});
		return bankcards;
	}

	public R saveBankCard(MbrBankcard mbrBankcard, Integer operatorType, String userName)
	{
		mbrBankcard.setId(null);
		MbrAccount mbrAccount = mbrAccountService.getAccountInfo(mbrBankcard.getAccountId());
		mbrBankcard.setRealName(mbrAccount.getRealName());
		Assert.isBlank(mbrBankcard.getRealName(), "开户姓名不能为空!");
/*		if (type.equals(Constants.sourceType.web))
			Assert.isBlank(mbrAccount.getMobile(), "为了您的资金安全,请先绑定手机号!");*/
		mbrBankcard.setIsDel(Available.disable);
		if (StringUtils.isEmpty(mbrBankcard.getAvailable()))
			mbrBankcard.setAvailable(Available.enable);
		mbrBankcard.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
		MbrBankcard card = new MbrBankcard();
		card.setCardNo(mbrBankcard.getCardNo());
		card.setIsDel(Available.disable);
		if (super.selectCount(card) == 0) {
			card = new MbrBankcard();
			card.setAccountId(mbrBankcard.getAccountId());
			card.setIsDel(Available.disable);
			if (super.selectCount(card) < 4) {
				card = new MbrBankcard();
				card.setBankName(mbrBankcard.getBankName());
				card.setAccountId(mbrBankcard.getAccountId());
				card.setIsDel(Available.disable);
				if (super.selectCount(card) == 0) {
					super.save(mbrBankcard);
					accountLogService.addAccountBank(mbrBankcard, userName, operatorType);
					return R.ok();
				} else {
					return R.error("同行只允许绑定1张银行卡!");
				}
			} else {
				return R.error("每个会员最多只能绑定4张银行卡!");
			}
		} else {
			return R.error("此银行卡号已经绑定!");
		}
	}

	public R updateBankCard(MbrBankcard mbrBankcard,String type)
	{
		mbrBankcard.setIsDel(null);
		MbrAccount mbrAccount = mbrAccountService.getAccountInfo(mbrBankcard.getAccountId());
		mbrBankcard.setRealName(mbrAccount.getRealName());
		Assert.isBlank(mbrBankcard.getRealName(), "开户姓名不能为空!");
		//Assert.isBlank(mbrAccount.getMobile(),"为了您的资金安全,请先绑定手机号!");
		if (StringUtils.isEmpty(mbrBankcard.getAvailable()))
			mbrBankcard.setAvailable(Available.enable);
		MbrBankcard card = new MbrBankcard();
		card.setCardNo(mbrBankcard.getCardNo());
		card.setIsDel(Available.disable);
		card.setId(mbrBankcard.getId());
		if (mbrMapper.countSameBankNum(card)==0) {
				card = new MbrBankcard();
				card.setBankName(mbrBankcard.getBankName());
				card.setIsDel(Available.disable);
				card.setAccountId(mbrBankcard.getAccountId());
				card.setId(mbrBankcard.getId());
				if (mbrMapper.countSameBankNum(card) == 0) {
					super.update(mbrBankcard);
					return R.ok();
				} else {
					return R.error("同行只允许绑定1张银行卡!");
				}
		} else {
			return R.error("此银行卡号已经绑定!");
		}
	}
	
	public void updateBankCardNameByAccId(int accountId, String realName) {
		mbrMapper.updateBankCardNameByAccId(accountId, realName);
	}
}
