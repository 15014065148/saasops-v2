package com.eveb.saasops.sysapi.service;

import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.modules.fund.dao.FundDepositMapper;
import com.eveb.saasops.modules.fund.entity.FundDeposit;
import com.eveb.saasops.modules.fund.service.FundDepositService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class BatchService {

    @Autowired
    private FundDepositMapper fundDepositMapper;

    @Autowired
    private FundDepositService fundDepositService;

    public void paySuccess(Integer depositId) {
        log.info("paySuccess收到BATCH请求，存款ID【" + depositId + "】");
        FundDeposit fundDeposit = fundDepositMapper.selectByPrimaryKey(depositId);
        if (fundDeposit.getStatus() == Constants.EVNumber.two) {
            fundDepositService.updateDepositSucceed(fundDeposit);
            fundDepositMapper.updateByPrimaryKeySelective(fundDeposit);
        }
    }
}
