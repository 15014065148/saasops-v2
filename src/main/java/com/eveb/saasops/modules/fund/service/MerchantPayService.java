package com.eveb.saasops.modules.fund.service;

import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.exception.R200Exception;
import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.common.utils.Collections3;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.modules.fund.dao.*;
import com.eveb.saasops.modules.fund.entity.*;
import com.eveb.saasops.modules.fund.mapper.FundMapper;
import com.eveb.saasops.modules.member.dao.MbrAccountMapper;
import com.eveb.saasops.modules.member.entity.MbrAccount;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.eveb.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.eveb.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;

@Service
@Transactional
public class MerchantPayService {

    @Autowired
    private FundWhiteListMapper whiteListMapper;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private FundMerchantPayMapper merchantPayMapper;
    @Autowired
    private FundMerchantScopeMapper merchantScopeMapper;
    @Autowired
    private FundMerchantDetailMapper merchantDetailMapper;
    @Autowired
    private TChannelPayMapper channelPayMapper;

    public FundWhiteList findFundWhiteList(Integer accountId) {
        FundWhiteList whiteList = new FundWhiteList();
        whiteList.setAccountId(accountId);
        return whiteListMapper.selectOne(whiteList);
    }

    public void addFundWhiteList(FundWhiteList whiteList) {
        FundWhiteList fundWhiteList = new FundWhiteList();
        fundWhiteList.setAccountId(whiteList.getAccountId());
        FundWhiteList whiteList1 = whiteListMapper.selectOne(fundWhiteList);
        if (isNull(whiteList1)) {
            MbrAccount account = accountMapper.selectByPrimaryKey(whiteList.getAccountId());
            whiteList.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
            whiteList.setLoginName(account.getLoginName());
            whiteListMapper.insert(whiteList);
        }
    }

    public void deleteFundWhiteList(Integer id) {
        whiteListMapper.deleteByPrimaryKey(id);
    }

    public PageUtils findFundMerchantPayList(FundMerchantPay merchantPay, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<FundMerchantPay> list = fundMapper.findFundMerchantPayList(merchantPay);
        return BeanUtil.toPagedResult(list);
    }

    public void addFundMerchantPay(FundMerchantPay merchantPay) {
        FundMerchantPay fundMerchantPay = new FundMerchantPay();
        fundMerchantPay.setAvailable(Constants.EVNumber.one);
        int count = merchantPayMapper.selectCount(fundMerchantPay);
        merchantPay.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        merchantPay.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        merchantPay.setAvailable(count > 0 ? Constants.EVNumber.zero : Constants.EVNumber.one);
        merchantPayMapper.insert(merchantPay);
        if (Collections3.isNotEmpty(merchantPay.getIds())) {
            insertListMerchantScope(merchantPay);
        }
    }

    private void insertListMerchantScope(FundMerchantPay merchantPay) {
        List<FundMerchantScope> scopeList =
                merchantPay.getIds().stream().map(d -> {
                    FundMerchantScope scope = new FundMerchantScope();
                    scope.setGroupId(d);
                    scope.setMerchantId(merchantPay.getId());
                    return scope;
                }).collect(Collectors.toList());
        merchantScopeMapper.insertList(scopeList);
    }

    public void updateFundMerchantPay(FundMerchantPay merchantPay) {
        if (merchantPay.getAvailable() == Constants.EVNumber.one) {
            fundMapper.updateMerchantPayAvailable();
        }
        merchantPay.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        merchantPayMapper.updateByPrimaryKey(merchantPay);
        FundMerchantScope scope = new FundMerchantScope();
        scope.setMerchantId(merchantPay.getId());
        merchantScopeMapper.delete(scope);
        insertListMerchantScope(merchantPay);
    }

    public void deleteFundMerchantPay(Integer id) {
        FundMerchantDetail merchantDetail = new FundMerchantDetail();
        merchantDetail.setMerchantId(id);
        int count = merchantDetailMapper.selectCount(merchantDetail);
        if (count > 0) throw new R200Exception("该商户号自动出款已使用，无法删除!");
        merchantPayMapper.deleteByPrimaryKey(id);
        FundMerchantScope scope = new FundMerchantScope();
        scope.setMerchantId(id);
        merchantScopeMapper.delete(scope);
    }

    public void updateFundMerchantPayAvailable(FundMerchantPay merchantPay) {
        if (merchantPay.getAvailable() == Constants.EVNumber.one) {
            FundMerchantPay merchantPay1 = new FundMerchantPay();
            merchantPay1.setAvailable(Constants.EVNumber.one);
            List<FundMerchantPay> merchantPays = merchantPayMapper.select(merchantPay1);
            if (Collections3.isNotEmpty(merchantPays)) {
                merchantPays.stream().forEach(ms -> {
                    ms.setAvailable(Constants.EVNumber.zero);
                    merchantPayMapper.updateByPrimaryKey(ms);
                });
            }
        }
        FundMerchantPay fundMerchantPay = new FundMerchantPay();
        fundMerchantPay.setAvailable(merchantPay.getAvailable());
        fundMerchantPay.setId(merchantPay.getId());
        fundMerchantPay.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        fundMerchantPay.setModifyUser(merchantPay.getModifyUser());
        merchantPayMapper.updateByPrimaryKeySelective(fundMerchantPay);
    }

    public FundMerchantPay findFundMerchantPayOne(Integer id) {
        FundMerchantPay merchantPay = merchantPayMapper.selectByPrimaryKey(id);
        FundMerchantScope scope = new FundMerchantScope();
        scope.setMerchantId(merchantPay.getId());
        List<FundMerchantScope> scopeList = merchantScopeMapper.select(scope);
        if (Collections3.isNotEmpty(scopeList)) {
            merchantPay.setIds(scopeList.stream().map(
                    st -> st.getGroupId()).collect(Collectors.toList()));
        }
        return merchantPay;
    }

    public List<TChannelPay> findTChannelPayList() {
        TChannelPay channelPay = new TChannelPay();
        channelPay.setAvailable(Constants.EVNumber.one);
        return channelPayMapper.select(channelPay);
    }
}
