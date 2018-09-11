package com.eveb.saasops.modules.member.service;

import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.apisys.service.TGmApiService;
import com.eveb.saasops.api.modules.transfer.dto.BillRequestDto;
import com.eveb.saasops.api.modules.transfer.service.TransferService;
import com.eveb.saasops.api.modules.user.dto.UserBalanceResponseDto;
import com.eveb.saasops.api.modules.user.service.DepotWalletService;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.utils.BigDecimalMath;
import com.eveb.saasops.config.ThreadLocalCache;
import com.eveb.saasops.modules.member.dto.AuditBonusDto;
import com.eveb.saasops.modules.member.dto.DepotFailDto;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class MbrDepotAsyncWalletService {

    @Autowired
    private TGmApiService gmApiService;
    @Autowired
    private DepotWalletService depotWalletService;
    @Autowired
    private TransferService transferService;

    @Async
    public CompletableFuture<UserBalanceResponseDto> getAsyncBalance(Integer depotId, Integer userId, String siteCode) {
        ThreadLocalCache.setSiteCodeAsny(siteCode);
        TGmApi gmApi = gmApiService.queryApiObject(depotId, siteCode);
        UserBalanceResponseDto balanceDto = null;
        try {
            balanceDto = depotWalletService.queryDepotBalance(userId, gmApi);
        } catch (Exception ex) {
        }
        if (balanceDto == null) {
            balanceDto = new UserBalanceResponseDto();
            balanceDto.setBalance(new BigDecimal("0"));
        }
        balanceDto.setDepotId(depotId);
        return CompletableFuture.completedFuture(balanceDto);
    }

    @Async
    public CompletableFuture<DepotFailDto> getAsyncRecoverBalance(Integer depotId, MbrDepotWallet wallet, String siteCode, String ip, Byte transferSource, Boolean isTransferBouns) {
        DepotFailDto depotFailDto = new DepotFailDto();
        depotFailDto.setDepotId(depotId);
        depotFailDto.setFailError(Boolean.TRUE);
        try {
            ThreadLocalCache.setSiteCodeAsny(siteCode);
            TGmApi gmApi = gmApiService.queryApiObject(depotId, siteCode);
            //TODO 查询余额
            UserBalanceResponseDto balanceDto = depotWalletService.queryDepotBalance(wallet.getAccountId(), gmApi);
            balanceDto.setBalance(BigDecimalMath.formatDownRounding(balanceDto.getBalance()));
            if (balanceDto.getBalance().doubleValue() > 0) {
                BillRequestDto requestDto = getBillRequestDto(balanceDto.getBalance(), depotId, wallet, ip);
                requestDto.setTransferSource(transferSource);
                //TODO 转账
                if (Boolean.FALSE.equals(isTransferBouns)) {
                    requestDto.setIsTransferBouns(isTransferBouns);
                }
                AuditBonusDto auditBonusDto = transferService.TransferOut(requestDto, siteCode);
                if (Boolean.FALSE.equals(auditBonusDto.getIsSucceed()) || Boolean.FALSE.equals(auditBonusDto.getIsFraud())) {
                    depotFailDto.setIsSign(Constants.EVNumber.one);
                    depotFailDto.setFailError(false);
                }
            }
        } catch (Exception e) {
            depotFailDto.setIsSign(Constants.EVNumber.zero);
            depotFailDto.setFailError(false);
        }
        return CompletableFuture.completedFuture(depotFailDto);
    }

    public BillRequestDto getBillRequestDto(BigDecimal balance, Integer depotId, MbrDepotWallet mbrWallet, String ip) {
        BillRequestDto requestDto = new BillRequestDto();
        requestDto.setAmount(balance);
        requestDto.setDepotId(depotId);
        requestDto.setAccountId(mbrWallet.getAccountId());
        requestDto.setLoginName(mbrWallet.getLoginName());
        requestDto.setIp(ip);
        return requestDto;
    }
}
