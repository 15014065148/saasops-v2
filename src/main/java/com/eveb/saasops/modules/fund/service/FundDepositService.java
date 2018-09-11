package com.eveb.saasops.modules.fund.service;

import com.eveb.saasops.api.modules.pay.pzpay.dto.DepositListDto;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.*;
import com.eveb.saasops.config.MessagesConfig;
import com.eveb.saasops.listener.BizEvent;
import com.eveb.saasops.listener.BizEventType;
import com.eveb.saasops.modules.fund.entity.QuickFunction;
import com.eveb.saasops.modules.fund.mapper.FundMapper;
import com.eveb.saasops.modules.member.dao.MbrAuditAccountMapper;
import com.eveb.saasops.modules.member.dao.MbrAccountMapper;
import com.eveb.saasops.modules.member.entity.MbrAccount;
import com.eveb.saasops.modules.member.entity.MbrAuditAccount;
import com.eveb.saasops.modules.member.entity.MbrBillDetail;
import com.eveb.saasops.modules.member.service.AuditAccountService;
import com.eveb.saasops.modules.member.service.MbrWalletService;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.eveb.saasops.modules.fund.dao.FundDepositMapper;
import com.eveb.saasops.modules.fund.entity.FundDeposit;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;

import static com.eveb.saasops.common.constants.Constants.SYSTEM_USER;
import static com.eveb.saasops.common.utils.DateUtil.FORMAT_10_DATE;
import static com.eveb.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.eveb.saasops.common.utils.DateUtil.getCurrentDate;

@Service
@Transactional
public class FundDepositService {

    @Autowired
    private FundDepositMapper fundDepositMapper;
    @Autowired
    private MbrWalletService walletService;
    @Autowired
    private MbrAuditAccountMapper accountAuditMapper;
    @Autowired
    private AuditAccountService accountAuditService;
    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private MessagesConfig messagesConfig;
    @Value("${fund.onLine.excel.path}")
    private String onLineExcelPath;
    @Value("${fund.company.excel.path}")
    private String companyExcelPath;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public FundDeposit queryObject(Integer id) {
        FundDeposit fundDeposit = new FundDeposit();
        fundDeposit.setId(id);
        Optional<FundDeposit> optional = Optional.ofNullable(
                fundMapper.findDepositList(fundDeposit)
                        .stream().findAny()).get();
        if (optional.isPresent()) {
            FundDeposit deposit = optional.get();
            FundDeposit sit = new FundDeposit();
            sit.setMark(deposit.getMark());
            sit.setStatus(Constants.IsStatus.succeed);
            sit.setAccountId(deposit.getAccountId());
            int depositCount = fundMapper.findDepositCount(sit);
            deposit.setDepositCount(depositCount);
            return deposit;
        }
        return null;
    }

    public List<FundDeposit> selectList(FundDeposit fundDeposit) {
        return fundDepositMapper.select(fundDeposit);
    }

    public PageUtils queryListPage(FundDeposit fundDeposit, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<FundDeposit> list = fundMapper.findDepositList(fundDeposit);
        //收款渠道拼接返回给前端直接使用
        list.forEach(e -> {
            if (StringUtil.isEmpty(e.getOnlinePayName())) {
                e.setPayType(e.getRealName() + "-" + e.getDepositType());
            }
            if (StringUtil.isEmpty(e.getDepositType())) {
                e.setPayType(e.getOnlinePayName() + "-" + e.getPaymentName());
            }
        });
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils queryListPage(DepositListDto fundDeposit, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        List<FundDeposit> list;
        if (fundDeposit.getMark() == Constants.EVNumber.zero) {
            list = fundMapper.findDepositListApiOfOnline(fundDeposit);
        } else if (fundDeposit.getMark() == Constants.EVNumber.one) {
            list = fundMapper.findDepositListApiCompany(fundDeposit);
        } else if (fundDeposit.getMark() == Constants.EVNumber.two) {
            list = fundMapper.findDepositListApiOther(fundDeposit);
        } else {
            list = fundMapper.findDepositAndOtherList(fundDeposit);
        }
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils queryDepositAndOtherListPage(DepositListDto fundDeposit, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        List<FundDeposit> list = fundMapper.findDepositAndOtherList(fundDeposit);
        return BeanUtil.toPagedResult(list);
    }


    public Double findDepositSum(DepositListDto fundDeposit) {
        if (fundDeposit.getMark() == Constants.EVNumber.zero
                || fundDeposit.getMark() == Constants.EVNumber.one) {
            return fundMapper.findDepositSum(fundDeposit);
        }
        if (fundDeposit.getMark() == Constants.EVNumber.two) {
            return fundMapper.findDepositSumOther(fundDeposit);
        }
        return fundMapper.findDepositSumAndAudit(fundDeposit);
    }

    public Double findSumDepositAmount(FundDeposit fundDeposit) {
        fundDeposit.setStatus(Constants.IsStatus.succeed);
        fundDeposit.setCreateTime(getCurrentDate(FORMAT_10_DATE));
        return fundMapper.findSumDepositAmount(fundDeposit);
    }

    public FundDeposit updateDeposit(FundDeposit fundDeposit, String userName) {
        FundDeposit deposit = checkoutFund(fundDeposit);
        if (Constants.IsStatus.succeed.equals(fundDeposit.getStatus())) {
            updateDepositSucceed(deposit);
        }
        deposit.setStatus(fundDeposit.getStatus());
        deposit.setAuditUser(userName);
        deposit.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        deposit.setMemo(fundDeposit.getMemo());
        deposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        deposit.setModifyUser(userName);
        fundDepositMapper.updateByPrimaryKey(deposit);
        return deposit;
    }

    public void accountDepositMsg(FundDeposit fundDeposit, String siteCode) {
        BizEvent bizEvent = new BizEvent(this, siteCode, fundDeposit.getAccountId(), null);
        if (Constants.IsStatus.succeed.equals(fundDeposit.getStatus())) {
            bizEvent.setEventType(BizEventType.DEPOSIT_VERIFY_SUCCESS);
        }
        if (Constants.IsStatus.defeated.equals(fundDeposit.getStatus())) {
            bizEvent.setEventType(BizEventType.DEPOSIT_VERIFY_FAILED);
        }
        bizEvent.setDespoitMoney(fundDeposit.getDepositAmount());
        applicationEventPublisher.publishEvent(bizEvent);
    }

    public void updateDepositSucceed(FundDeposit fundDeposit) {
        MbrAuditAccount accountAudit = new MbrAuditAccount();
        accountAudit.setDepositId(fundDeposit.getId());
        MbrAuditAccount audit = accountAuditMapper.selectOne(accountAudit);
        if (Objects.isNull(audit)) {
            accountAuditService.insertAccountAudit(
                    fundDeposit.getAccountId(), fundDeposit.getDepositAmount(), fundDeposit.getId(), null);
        }
        MbrAccount account = accountMapper.selectByPrimaryKey(fundDeposit.getAccountId());
        MbrBillDetail billDetail = walletService.castWalletAndBillDetail(account.getLoginName(),
                fundDeposit.getAccountId(), fundDeposit.getOrderPrefix(),
                fundDeposit.getActualArrival(), fundDeposit.getOrderNo(), Boolean.TRUE);
        fundDeposit.setBillDetailId(billDetail.getId());
        fundDeposit.setStatus(Constants.IsStatus.succeed);
        fundDeposit.setAuditUser(SYSTEM_USER);
        fundDeposit.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        fundDeposit.setIsPayment(Boolean.TRUE);
    }

    private FundDeposit checkoutFund(FundDeposit fundDeposit) {
        FundDeposit deposit = fundDepositMapper.selectByPrimaryKey(fundDeposit.getId());
        if (deposit.getStatus() != Constants.IsStatus.pending) {
            throw new RRException(messagesConfig.getValue("saasops.illegal.request"));
        }
        if (fundDeposit.getStatus() == Constants.IsStatus.defeated && Boolean.TRUE.equals(deposit.getIsPayment())) {
            throw new RRException("订单已支付成功，不能处理为失败！");
        }
        return deposit;
    }

    public void updateDepositMemo(FundDeposit fundDeposit, String userName) {
        FundDeposit deposit = fundDepositMapper.selectByPrimaryKey(fundDeposit.getId());
        deposit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        deposit.setModifyUser(userName);
        deposit.setMemo(fundDeposit.getMemo());
        fundDepositMapper.updateByPrimaryKey(deposit);
    }

    public void depositExportExecl(FundDeposit fundDeposit, Boolean isOnLine, HttpServletResponse response) {
        String fileName = isOnLine == true ? "线上入款" : "公司入款" +
                "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xls";
        List<FundDeposit> fundDeposits = fundMapper.findDepositList(fundDeposit);
        List<Map<String, Object>> list = Lists.newArrayList();
        fundDeposits.stream().forEach(deposit -> {
            Map<String, Object> paramr = new HashMap<>();
            paramr.put("orderNo", deposit.getOrderPrefix() + deposit.getOrderNo());
            paramr.put("loginName", deposit.getLoginName());
            paramr.put("groupName", deposit.getGroupName());
            paramr.put("depositAmount", deposit.getDepositAmount());
            paramr.put("ip", deposit.getIp());
            paramr.put("status", deposit.getStatus() == 0
                    ? Constants.ChineseStatus.defeated : deposit.getStatus() == 1
                    ? Constants.ChineseStatus.succeed : Constants.ChineseStatus.pending);
            list.add(paramr);
        });
        Workbook workbook = ExcelUtil.commonExcelExportList("mapList",
                isOnLine == true ? onLineExcelPath : companyExcelPath, list);
        try {
            ExcelUtil.writeExcel(response, workbook, fileName);
        } catch (IOException e) {
            throw new RRException(e.getMessage());
        }
    }

    public BigDecimal totalCompanyDeposit(Integer companyId) {
        return fundMapper.sumCompanyDeposit(companyId);
    }

    public List<QuickFunction> listCount() {
        return fundMapper.listCount();
    }
}
