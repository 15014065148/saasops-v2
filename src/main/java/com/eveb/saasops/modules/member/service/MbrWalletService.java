package com.eveb.saasops.modules.member.service;


import static com.eveb.saasops.common.utils.DateUtil.FORMAT_25_DATE_TIME;
import static com.eveb.saasops.common.utils.DateUtil.getCurrentDate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.eveb.saasops.modules.member.dto.DepotFailDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.utils.BigDecimalMath;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.common.utils.SnowFlake;
import com.eveb.saasops.modules.base.service.BaseService;
import com.eveb.saasops.modules.member.dao.MbrBillDetailMapper;
import com.eveb.saasops.modules.member.dao.MbrWalletMapper;
import com.eveb.saasops.modules.member.dto.BalanceDto;
import com.eveb.saasops.modules.member.entity.MbrAccount;
import com.eveb.saasops.modules.member.entity.MbrBillDetail;
import com.eveb.saasops.modules.member.entity.MbrBillDetail.OpTypeStatus;
import com.eveb.saasops.modules.member.entity.MbrBillManage;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import com.eveb.saasops.modules.member.entity.MbrWallet;
import com.eveb.saasops.modules.member.mapper.MbrMapper;


@Service
@Slf4j
public class MbrWalletService extends BaseService<MbrWalletMapper, MbrWallet> {

    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private MbrBillDetailMapper mbrBillDetailMapper;
    @Autowired
    private MbrDepotWalletService mbrDepotWalletService;
    @Autowired
    private MbrDepotAsyncWalletService mbrDepotAsyncWalletService;

    public MbrWallet queryById(Integer accountId) {
        MbrWallet wallet = new MbrWallet();
        wallet.setAccountId(accountId);
        return queryObjectCond(wallet);
    }

    public List<MbrAccount> listAccName(Integer[] accountIds) {
        return mbrMapper.listAccName(accountIds);
    }

    /**
     * 资金减少
     *
     * @return
     */
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public boolean walletSubtract(MbrWallet mbrWallet, MbrBillDetail mbrBillDetail) {
        int opRecord = mbrMapper.walletSubtract(mbrWallet);
        if (opRecord > 0) {
            MbrWallet entity = new MbrWallet();
            entity.setAccountId(mbrWallet.getAccountId());
            entity = queryObjectCond(entity);
            mbrBillDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
            mbrBillDetail.setOpType(OpTypeStatus.expenditure);
            mbrBillDetail.setAfterBalance(entity.getBalance());
            mbrBillDetail.setBeforeBalance(
                    BigDecimalMath.round(BigDecimalMath.add(entity.getBalance(), mbrBillDetail.getAmount()), 2));
            opRecord = mbrBillDetailMapper.insert(mbrBillDetail);
        }
        return opRecord > 0 ? true : false;
    }

    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public boolean walletAddBatch(MbrBillDetail mbrBillDetail) {
        MbrWallet mbrWallet = new MbrWallet();
        mbrWallet.setBalance(mbrBillDetail.getAmount());
        for (int i = 0; i < mbrBillDetail.getAccountIds().length; i++) {
            mbrWallet.setAccountId(mbrBillDetail.getAccountIds()[i]);
            mbrBillDetail.setOrderNo(mbrBillDetail.getOrderNos()[i]);
            mbrBillDetail.setLoginName(mbrBillDetail.getLoginNames()[i]);
            mbrBillDetail.setAccountId(mbrBillDetail.getAccountIds()[i]);
            walletAdd(mbrWallet, mbrBillDetail);
        }
        return Boolean.TRUE;
    }

    /**
     * 资金add
     *
     * @return
     */
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public Boolean walletAdd(MbrWallet mbrWallet, MbrBillDetail mbrBillDetail) {
        int opRecord = mbrMapper.walletAdd(mbrWallet);
        if (opRecord > 0) {
            MbrWallet entity = new MbrWallet();
            entity.setAccountId(mbrWallet.getAccountId());
            entity = queryObjectCond(entity);
            mbrBillDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
            mbrBillDetail.setOpType(OpTypeStatus.income);
            mbrBillDetail.setAfterBalance(entity.getBalance());
            mbrBillDetail.setBeforeBalance(
                    BigDecimalMath.round(BigDecimalMath.sub(entity.getBalance(), mbrBillDetail.getAmount()), 2));
            if (opRecord > 0)
                mbrBillDetailMapper.insert(mbrBillDetail);
        }
        return opRecord > 0 ? true : false;
    }

    /**
     * 钱包转平台
     */
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public Boolean walletArriveDepot(MbrWallet mbrWallet, MbrBillManage mbrBillManage) {
        if (mbrWallet.getBalance().compareTo(BigDecimal.ONE) == Constants.EVNumber.one ||
                mbrWallet.getBalance().compareTo(BigDecimal.ONE) == Constants.EVNumber.zero) {
            int isSucceed = mbrMapper.walletSubtract(mbrWallet);
            if (isSucceed == 0) return Boolean.FALSE;
        }
        mbrWallet.setBalance(null);
        MbrWallet wallet = queryObjectCond(mbrWallet);
        mbrBillManage.setAfterBalance(wallet.getBalance());
        mbrBillManage.setBeforeBalance(
                BigDecimalMath.round(BigDecimalMath.add(wallet.getBalance(),
                        mbrBillManage.getAmount()), 2));
        return Boolean.TRUE;
    }

    /**
     * 平台转钱包
     */
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public Boolean depotArriveWallet(MbrWallet mbrWallet, MbrBillDetail mbrBillDetail) {
        int isSucceed = mbrMapper.walletAdd(mbrWallet);
        if (isSucceed == 0) return Boolean.FALSE;
        mbrWallet.setBalance(null);
        MbrWallet wallet = queryObjectCond(mbrWallet);
        mbrBillDetail.setAfterBalance(wallet.getBalance());
        mbrBillDetail.setBeforeBalance(
                BigDecimalMath.round(BigDecimalMath.sub(wallet.getBalance(), mbrBillDetail.getAmount()), 2));
        return Boolean.TRUE;
    }

    @Cacheable(cacheNames = ApiConstants.REDIS_DEPOT_ACC_KEY, key = "#siteCode+'_'+#mbrWallet.depotId+'_'+#mbrWallet.accountId")
    public MbrDepotWallet queryObjectCond(MbrDepotWallet mbrWallet, String siteCode) {
        return mbrDepotWalletService.queryObjectCond(mbrWallet);
    }

    public MbrDepotWallet noExistInsert(MbrDepotWallet mbrWallet, String siteCode) {
        MbrDepotWallet queryWallet = new MbrDepotWallet();
        queryWallet.setAccountId(mbrWallet.getAccountId());
        queryWallet.setDepotId(mbrWallet.getDepotId());
        if (null == queryObjectCond(queryWallet, siteCode)) {
            mbrWallet.setBalance(ApiConstants.DEAULT_ZERO_VALUE);
            mbrWallet.setIsTransfer(MbrDepotWallet.IsTransFer.no);
            mbrWallet.setIsLogin(Constants.Available.enable);
            mbrDepotWalletService.save(mbrWallet);
            mbrWallet.setIsBuild(Boolean.TRUE);
            return mbrWallet;
        } else {
            mbrWallet.setIsBuild(Boolean.FALSE);
            return queryWallet;
        }
    }

    public List<MbrDepotWallet> getDepotWallet(MbrDepotWallet walletModel) {
        MbrDepotWallet mbrWallet = new MbrDepotWallet();
        mbrWallet.setAccountId(walletModel.getAccountId());
        mbrWallet.setDepotIds(walletModel.getDepotIds());
        return mbrDepotWalletService.queryCondDepot(mbrWallet);
    }

    public Boolean recoverBalanceNew(MbrDepotWallet walletModel, String ip, Byte transferSource, Boolean isTransferBouns) {
        MbrDepotWallet mbrWallet = new MbrDepotWallet();
        mbrWallet.setAccountId(walletModel.getAccountId());
        mbrWallet.setDepotIds(walletModel.getDepotIds());
        if (!StringUtils.isEmpty(walletModel.getAccountId())) {
            String siteCode = CommonUtil.getSiteCode();
            noExistInsert(walletModel, siteCode);
            List<MbrDepotWallet> list = mbrDepotWalletService.queryCondDepot(mbrWallet);
            List<CompletableFuture<DepotFailDto>> recoverBalanceList = new ArrayList<>();
            list.forEach(e1 -> {
                recoverBalanceList.add(mbrDepotAsyncWalletService.getAsyncRecoverBalance(e1.getDepotId(), e1, siteCode, ip, transferSource, isTransferBouns));
            });
            List<DepotFailDto> failList = recoverBalanceList.stream().map(CompletableFuture::join).filter(e -> e.getFailError() == Boolean.FALSE).collect(Collectors.toList());
            if (failList.size() == 0) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public MbrWallet getBalance(Integer userId) {
        MbrWallet mbrWallet = new MbrWallet();
        mbrWallet.setAccountId(userId);
        return super.queryObjectCond(mbrWallet);

    }

    public MbrBillDetail castWalletAndBillDetail(
            String loginName, int accountId, String financialCode,
            BigDecimal amount, String orderNo, Boolean isSign) {

        MbrBillDetail billDetail = new MbrBillDetail();
        billDetail.setLoginName(loginName);
        billDetail.setAccountId(accountId);
        billDetail.setFinancialCode(financialCode);
        billDetail.setOrderNo(!StringUtils.isEmpty(orderNo) ? orderNo : new SnowFlake().nextId()+"");
        billDetail.setOrderTime(getCurrentDate(FORMAT_25_DATE_TIME));
        billDetail.setDepotId(Constants.SYS_DEPOT_ID);
        billDetail.setAmount(amount);

        MbrWallet mbrWallet = new MbrWallet();
        mbrWallet.setBalance(billDetail.getAmount());
        mbrWallet.setAccountId(billDetail.getAccountId());
        Boolean isSuccess = null;
        if (Boolean.TRUE.equals(isSign)) {
            isSuccess = walletAdd(mbrWallet, billDetail);
        }
        if (Boolean.FALSE.equals(isSign)) {
            isSuccess = walletSubtract(mbrWallet, billDetail);
        }
        if (Boolean.TRUE.equals(isSuccess)) {
            return billDetail;
        }
        return null;
    }

    public void updateTransfer(Integer id) {
        MbrDepotWallet wallet = new MbrDepotWallet();
        wallet.setId(id);
        wallet.setIsTransfer(MbrDepotWallet.IsTransFer.yes);
        mbrDepotWalletService.update(wallet);
    }

    public List<BalanceDto> balancelist(Integer accountId) {
        MbrWallet wallet = getBalance(accountId);
        ArrayList<BalanceDto> list = new ArrayList<BalanceDto>();
        BalanceDto balanceDto = new BalanceDto();
        balanceDto.setBalance(wallet.getBalance());
        balanceDto.setDepotName(Constants.SYSTEM_DEPOT_NAME);
        list.add(balanceDto);
        MbrDepotWallet mbrDepotWallet = new MbrDepotWallet();
        mbrDepotWallet.setAccountId(accountId);
        PageUtils utils = mbrDepotWalletService.queryListPage(mbrDepotWallet, 1, 100, null);
        @SuppressWarnings("unchecked")
        List<MbrDepotWallet> mbrDepotWallets = (List<MbrDepotWallet>) utils.getList();
        mbrDepotWallets.forEach(e -> {
            if (e.getBalance().compareTo(BigDecimal.ZERO) == 1) {
                BalanceDto dto = new BalanceDto();
                dto.setBalance(e.getBalance());
                dto.setDepotName(e.getDepotName());
                list.add(dto);
            }
        });
        return list;
    }
}
