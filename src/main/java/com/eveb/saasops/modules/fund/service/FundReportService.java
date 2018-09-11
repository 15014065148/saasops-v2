package com.eveb.saasops.modules.fund.service;

import com.eveb.saasops.api.modules.apisys.entity.TGmApi;
import com.eveb.saasops.api.modules.apisys.service.TGmApiService;
import com.eveb.saasops.api.modules.transfer.dto.BillRequestDto;
import com.eveb.saasops.api.modules.transfer.service.TransferService;
import com.eveb.saasops.api.modules.user.service.DepotWalletService;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.constants.OrderConstants;
import com.eveb.saasops.common.exception.R200Exception;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.*;
import com.eveb.saasops.common.validator.Assert;
import com.eveb.saasops.config.MessagesConfig;
import com.eveb.saasops.modules.base.service.BaseService;
import com.eveb.saasops.modules.fund.dao.FundAuditMapper;
import com.eveb.saasops.modules.fund.entity.FundAudit;
import com.eveb.saasops.modules.fund.mapper.FundMapper;
import com.eveb.saasops.modules.member.dao.MbrAccountMapper;
import com.eveb.saasops.modules.member.dao.MbrBillManageMapper;
import com.eveb.saasops.modules.member.dto.BillRecordDto;
import com.eveb.saasops.modules.member.entity.*;
import com.eveb.saasops.modules.member.service.AuditAccountService;
import com.eveb.saasops.modules.member.service.MbrWalletService;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.eveb.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.eveb.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.nonNull;

@Service
@Transactional
public class FundReportService extends BaseService<MbrBillManageMapper, MbrBillManage> {

    @Autowired
    private FundMapper fundMapper;
    @Autowired
    private FundAuditMapper fundAuditMapper;
    @Autowired
    private MbrBillManageMapper mbrBillManageMapper;
    @Autowired
    private MbrWalletService walletService;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private MessagesConfig messagesConfig;
    @Value("${fund.billReport.excel.path}")
    private String billReportExcelPath;
    @Value("${fund.audit.excel.path}")
    private String auditReportExcelPath;
    @Value("${fund.auditTopAgy.excel.path}")
    private String auditTopAgyExcelPath;
    @Value("${fund.auditAgy.excel.path}")
    private String auditAgyExcelPath;
    @Autowired
    private MbrWalletService mbrWalletService;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private DepotWalletService depotWalletService;
    @Autowired
    private TGmApiService gmApiService;
    @Autowired
    private TransferService transferService;


    public PageUtils queryListPage(MbrBillManage mbrBillManage, Integer pageNo, Integer pageSize) {
        mbrBillManage.setBaseAuth(getRowAuth());
        PageHelper.startPage(pageNo, pageSize);
        List<MbrBillManage> list = fundMapper.findMbrBillManageList(mbrBillManage);
        return BeanUtil.toPagedResult(list);
    }

    @Override
    public MbrBillManage queryObject(Integer id) {
        MbrBillManage billReport = new MbrBillManage();
        billReport.setId(id);
        return Optional.ofNullable(
                fundMapper.findMbrBillManageList(billReport)
                        .stream().findAny()).get().orElse(null);
    }

    public void updateBillMemo(MbrBillManage mbrBillManage) {
        MbrBillManage report = mbrBillManageMapper.selectByPrimaryKey(mbrBillManage.getId());
        report.setMemo(mbrBillManage.getMemo());
        report.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        report.setModifyUser(mbrBillManage.getModifyUser());
        mbrBillManageMapper.updateByPrimaryKey(report);
    }

    public void billReportExportExecl(MbrBillManage mbrBillManage, HttpServletResponse response) {
        String fileName = "转账报表" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xls";
        List<MbrBillManage> billReports = fundMapper.findMbrBillManageList(mbrBillManage);
        List<Map<String, Object>> list = Lists.newArrayList();
        billReports.stream().forEach(rs -> {
            Map<String, Object> paramr = new HashMap<>();
            paramr.put("depotNo", rs.getOrderNo());
            paramr.put("agyAccount", rs.getAgyAccount());
            paramr.put("topAgyAccount", rs.getTopAgyAccount());
            paramr.put("loginName", rs.getLoginName());
            paramr.put("realName", rs.getRealName());
            paramr.put("depotName", rs.getDepotName());
            list.add(paramr);
        });
        Workbook workbook = ExcelUtil.commonExcelExportList("mapList", billReportExcelPath, list);
        try {
            ExcelUtil.writeExcel(response, workbook, fileName);
        } catch (IOException e) {
            throw new RRException(e.getMessage());
        }
    }

    public void auditSave(FundAudit audit) {
        List<FundAudit> fundAudits = audit.getIds()
                .stream().map(id ->
                        castFundAudit(audit, id))
                .collect(Collectors.toList());
        fundAuditMapper.insertList(fundAudits);
    }

    private FundAudit castFundAudit(FundAudit audit, Integer id) {
        MbrAccount account = accountMapper.selectByPrimaryKey(id);
        if (nonNull(account)) {
            FundAudit fundAudit = new FundAudit();
            fundAudit.setAccountId(id);
            fundAudit.setActivityId(audit.getActivityId());
            fundAudit.setAmount(audit.getAmount());
            fundAudit.setDepositType(audit.getDepositType());
            fundAudit.setAuditType(audit.getAuditType());
            fundAudit.setAuditMultiple(audit.getAuditMultiple());
            fundAudit.setIsClear(audit.getIsClear());
            fundAudit.setMemo(audit.getMemo());
            fundAudit.setFinancialCode(audit.getFinancialCode());
            fundAudit.setLoginName(account.getLoginName());
            fundAudit.setStatus(Constants.IsStatus.pending);
            fundAudit.setOrderNo(new SnowFlake().nextId()+"");
            fundAudit.setOrderPrefix(OrderConstants.FUND_ORDER_AUDIT);
            fundAudit.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
            fundAudit.setCreateUser(audit.getCreateUser());
            if (OrderConstants.FUND_ORDER_CODE_AM.equals(audit.getFinancialCode())) {
                MbrBillDetail mbrBillDetail = mbrWalletService.castWalletAndBillDetail(account.getLoginName(),
                        account.getId(), audit.getFinancialCode(), audit.getAmount(), audit.getOrderNo(), Boolean.FALSE);
                if (Objects.isNull(mbrBillDetail)) {
                    throw new R200Exception("人工减少会员" + account.getLoginName() + ",余额不足");
                }
                fundAudit.setBillDetailId(mbrBillDetail.getId());
            }
            return fundAudit;
        }
        return null;
    }

    public PageUtils queryAuditListPage(FundAudit audit, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<FundAudit> list = fundMapper.findFundAuditList(audit);
        return BeanUtil.toPagedResult(list);
    }

    public FundAudit queryAuditObject(Integer id) {
        FundAudit fundAudit = new FundAudit();
        fundAudit.setId(id);
        return Optional.ofNullable(
                fundMapper.findFundAuditList(fundAudit).stream()
                        .findAny()).get().orElse(null);
    }

    public void auditUpdateStatus(FundAudit fundAudit, String siteCode) {
        FundAudit audit = fundAuditMapper.selectByPrimaryKey(fundAudit.getId());
        if (audit.getStatus() != Constants.IsStatus.pending) {
            throw new R200Exception(messagesConfig.getValue("saasops.illegal.request"));
        }
        if (nonNull(audit.getAuditId())) {
            throw new R200Exception("请勿重复请求");
        }
        audit.setStatus(fundAudit.getStatus());
        audit.setMemo(fundAudit.getMemo());
        audit.setAuditUser(fundAudit.getModifyUser());
        audit.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        audit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        audit.setModifyUser(fundAudit.getModifyUser());
        if (fundAudit.getStatus() == Constants.EVNumber.one) {
            artificialIncrease(audit, siteCode);
        }
        if (fundAudit.getStatus() == Constants.EVNumber.zero) {
            auditTurnDown(audit);
        }
        fundAuditMapper.updateByPrimaryKey(audit);
    }

    private void auditTurnDown(FundAudit audit) {
        if (OrderConstants.FUND_ORDER_CODE_AM.equals(audit.getFinancialCode())) {
            walletService.castWalletAndBillDetail(audit.getLoginName(),
                    audit.getAccountId(), audit.getFinancialCode(), audit.getAmount(),
                    audit.getOrderNo(), Boolean.TRUE);
        }
    }

    private void artificialIncrease(FundAudit fundAudit, String siteCode) {
        if (OrderConstants.FUND_ORDER_CODE_AA.equals(fundAudit.getFinancialCode())) {

            if (fundAudit.getAuditType() == Constants.EVNumber.one) {
                MbrAuditAccount auditAccount = auditAccountService.insertAccountAudit(
                        fundAudit.getAccountId(), fundAudit.getAmount(), null, fundAudit.getAuditMultiple());
                fundAudit.setAuditId(auditAccount.getId());
            }

            MbrBillDetail mbrBillDetail = walletService.castWalletAndBillDetail(fundAudit.getLoginName(),
                    fundAudit.getAccountId(), fundAudit.getFinancialCode(), fundAudit.getAmount(),
                    fundAudit.getOrderNo(), Boolean.TRUE);
            fundAudit.setBillDetailId(mbrBillDetail.getId());

        }
        if (OrderConstants.FUND_ORDER_CODE_AM.equals(fundAudit.getFinancialCode())) {
            if (nonNull(fundAudit.getIsClear()) && fundAudit.getIsClear() == Constants.EVNumber.one) {
                auditAccountService.clearAccountAudit(fundAudit.getLoginName(),
                        fundAudit.getModifyUser(), siteCode, "人工清除稽核点");
            }
        }
    }

    public void auditUpdateMemo(FundAudit fundAudit) {
        FundAudit audit = fundAuditMapper.selectByPrimaryKey(fundAudit.getId());
        audit.setMemo(fundAudit.getMemo());
        audit.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        audit.setModifyUser(fundAudit.getLoginName());
        fundAuditMapper.updateByPrimaryKey(audit);
    }

    public void auditExportExecl(FundAudit fundAudit, HttpServletResponse response) {
        String fileName = "调整报表" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xls";
        List<FundAudit> fundAudits = fundMapper.findFundAuditList(fundAudit);
        List<Map<String, Object>> list = Lists.newArrayList();
        fundAudits.stream().forEach(fs -> {
            Map<String, Object> paramr = new HashMap<>();
            paramr.put("orderNo", fs.getOrderPrefix() + fs.getOrderNo());
            paramr.put("agyAccount", fs.getAgyAccount());
            paramr.put("topAgyAccount", fs.getTopAgyAccount());
            paramr.put("loginName", fs.getLoginName());
            paramr.put("amount", fs.getAmount());
            paramr.put("auditUser", fs.getAuditUser());
            list.add(paramr);
        });
        Workbook workbook = ExcelUtil.commonExcelExportList("mapList", billReportExcelPath, list);
        try {
            ExcelUtil.writeExcel(response, workbook, fileName);
        } catch (IOException e) {
            throw new RRException(e.getMessage());
        }
    }

    public long createOrderNumber() {
        return new SnowFlake().nextId();
    }

    public MbrWallet queryAccountBalance(String loginName) {
        return fundMapper.findAccountBalance(loginName);
    }

    public MbrWallet queryDepotOrAccountBalance(MbrBillManage mbrBillManage, String siteCode) {
        TGmApi gmApi = gmApiService.queryApiObject(mbrBillManage.getDepotId(), siteCode);
        Assert.isNull(mbrBillManage.getLoginName(), "会员名不存在，请重新输入！");
        MbrWallet mbrWallet = queryAccountBalance(mbrBillManage.getLoginName());
        mbrWallet.setDepotBeforeBalance(depotWalletService.queryDepotBalance(mbrWallet.getId(), gmApi).getBalance());
        return mbrWallet;
    }

    public int save(BillRequestDto requestDto, String siteCode) {
        MbrWallet mbrWallet = queryAccountBalance(requestDto.getLoginName());
        requestDto.setAccountId(mbrWallet.getId());
        requestDto.setTransferSource((byte) 0);
        if (requestDto.getOpType() == 0) {
            transferService.TransferIn(requestDto, siteCode);
        } else {
            transferService.TransferOut(requestDto, siteCode);
        }
        return 1;
    }

    public PageUtils queryBillRecordListPage(BillRecordDto billRecordDto, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<BillRecordDto> list = fundMapper.findBillRecordList(billRecordDto);
        return BeanUtil.toPagedResult(list);
    }
}
