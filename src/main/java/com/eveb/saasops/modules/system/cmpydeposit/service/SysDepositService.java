package com.eveb.saasops.modules.system.cmpydeposit.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import com.eveb.saasops.api.utils.HttpsRequestUtil;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.modules.sys.entity.SysUserEntity;
import com.google.common.collect.Lists;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.constants.Constants.Available;
import com.eveb.saasops.common.constants.SystemConstants;
import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.modules.member.dao.MbrGroupMapper;
import com.eveb.saasops.modules.member.entity.MbrDepositCond;
import com.eveb.saasops.modules.member.entity.MbrGroup;
import com.eveb.saasops.modules.member.entity.MbrWithdrawalCond.FeeWayVal;
import com.eveb.saasops.modules.member.service.MbrAccountService;
import com.eveb.saasops.modules.member.service.MbrDepositCondService;
import com.eveb.saasops.modules.system.cmpydeposit.dao.SysDepMbrMapper;
import com.eveb.saasops.modules.system.cmpydeposit.dao.SysDepositMapper;
import com.eveb.saasops.modules.system.cmpydeposit.entity.SysDepMbr;
import com.eveb.saasops.modules.system.cmpydeposit.entity.SysDeposit;
import com.eveb.saasops.modules.system.mapper.SetBasicSysMapper;
import com.github.pagehelper.PageHelper;

import javax.servlet.http.HttpServletRequest;

import static java.util.Objects.nonNull;

@Service
public class SysDepositService {
    @Autowired
    private SysDepositMapper sysDepositMapper;
    @Autowired
    private SysDepMbrMapper sysDepMbrMapper;
    @Autowired
    private SetBasicSysMapper setBasicSysMapper;
    @Autowired
    MbrAccountService mbrAccountService;
    @Autowired
    MbrDepositCondService mbrDepositCondService;
    @Autowired
    MbrGroupMapper mbrGroupMapper;

    @Value("${deposit.plot.excel.path}")
    private String depositExcelPath;

    public SysDeposit queryObject(Integer id) {
        SysDeposit sysDeposit = new SysDeposit();
        sysDeposit = sysDepositMapper.selectByPrimaryKey(id);
        SysDepMbr record = new SysDepMbr();
        record.setDepositId(id);
        List<SysDepMbr> sdmList = new ArrayList<>();
        sdmList = sysDepMbrMapper.select(record);
        List<Integer> grpIds = new ArrayList<>();
        List<MbrGroup> checkedGroups = new ArrayList<>();
        for (SysDepMbr dm : sdmList) {
            Integer grpId = dm.getMemGroId();
            grpIds.add(grpId);
            checkedGroups.add(mbrGroupMapper.selectByPrimaryKey(grpId));
        }
        sysDeposit.setMbrGrdIds(grpIds);
        sysDeposit.setCheckedGroup(checkedGroups);
        return sysDeposit;
    }

    public List<SysDeposit> queryGpCondList(Integer groupId) {
        return setBasicSysMapper.querySetBasicSysdeposit(groupId);
    }

    public SysDeposit queryObjectAndGroup(Integer bankId, Integer groupId) {
        return setBasicSysMapper.setBasicSysdepositOne(bankId, groupId);
    }

    public List<SysDeposit> queryAccCondList(HttpServletRequest request) {
        //获取设备来源
        String dev = request.getHeader("dev");
        Byte devSource = HttpsRequestUtil.getHeaderOfDev(dev);
        List<SysDeposit> amountList = setBasicSysMapper.querydepositAmount(devSource);
        List<SysDeposit> deposits = Lists.newArrayList();
        setSysDeposits(amountList, deposits);
        deposits.forEach(e -> {
            if (e.getBankType() == 0)
                e.setDepositType(e.getBankName());
        });
        return deposits;
    }

    private void setSysDeposits(List<SysDeposit> amountList, List<SysDeposit> deposits) {
        for (int i = 0; i < amountList.size(); i++) {
            SysDeposit sysDeposit = amountList.get(i);
            sysDeposit.setDayMaxAmt(nonNull(sysDeposit.getDayMaxAmt())
                    ? sysDeposit.getDayMaxAmt() : BigDecimal.ZERO);
            sysDeposit.setDepositAmount(nonNull(sysDeposit.getDepositAmount())
                    ? sysDeposit.getDepositAmount() : BigDecimal.ZERO);
            if (sysDeposit.getDepositAmount().compareTo(sysDeposit.getDayMaxAmt()) == -1
                    && deposits.size() == 0) {
                deposits.add(sysDeposit);
                continue;
            }
            List<Integer> bankTypes = deposits.stream().map(
                    ds -> ds.getBankType()).collect(Collectors.toList());
            if (bankTypes.contains(Constants.EVNumber.one) && sysDeposit.getBankType() == Constants.EVNumber.zero &&
                    sysDeposit.getDepositAmount().compareTo(sysDeposit.getDayMaxAmt()) == -1) {
                deposits.add(sysDeposit);
                break;
            }
            if (bankTypes.contains(Constants.EVNumber.zero) && sysDeposit.getBankType() == Constants.EVNumber.one &&
                    sysDeposit.getDepositAmount().compareTo(sysDeposit.getDayMaxAmt()) == -1) {
                deposits.add(sysDeposit);
                break;
            }
        }
    }


    public PageUtils queryListPage(SysDeposit sysDeposit, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<SysDeposit> list;
        if (sysDeposit.getDTypes() != null) {
            sysDeposit.setBankNameList(sysDeposit.getDTypes().stream().filter(x -> x.contains("行")).collect(Collectors.toList()));
            sysDeposit.setDepositTypeList(sysDeposit.getDTypes().stream().filter(x -> !x.contains("行")).collect(Collectors.toList()));
        }
        list = sysDepositMapper.querySysDepositList(sysDeposit);
        list.forEach(e -> {
            if (e.getBankType() == 0)
                e.setDepositType(e.getBankName());
            if (e.getBankType() == 1)
                e.setDepositType(e.getBankName());
            SysDepMbr sdm = new SysDepMbr();
            sdm.setDepositId(e.getId());
            List<SysDepMbr> sdmList = sysDepMbrMapper.select(sdm);
            List<Integer> ids = new ArrayList<>();
            if (sdmList != null && sdmList.size() != 0) {
                for (SysDepMbr sd : sdmList) {
                    ids.add(sd.getMemGroId());
                }
            }
            e.setMbrGrdIds(ids);
        });
        return BeanUtil.toPagedResult(list);
    }

    public List<SysDeposit> querySysDepositList() {
        List<SysDeposit> list = new ArrayList<>();
        SysDeposit record = new SysDeposit();
        record.setStatus(SystemConstants.STATUS_ENABLE);
        list = sysDepositMapper.select(record);
        list.forEach(e -> {
            e.setPayName(e.getBankName());
            if (e.getBankType() == 0)
                e.setDepositType(e.getBankName());
        });
        return list;
    }

    public void save(SysDeposit sysDeposit) {
        List<SysDeposit> listBank = new ArrayList<SysDeposit>();
        List<SysDeposit> listpayment = new ArrayList<SysDeposit>();
        if (sysDeposit.getFeeWay() == Constants.feeWay.fixed) {
            sysDeposit.setFeeFixed(null);
        } else {
            sysDeposit.setFeeScale(null);
            sysDeposit.setFeeTop(null);
        }
        sysDeposit.setCreateTime(new Date());
        SysUserEntity user = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
        sysDeposit.setCreateUser(user.getUsername());
        String code = CommonUtil.getRandomCode();
        sysDeposit.setCode(code);
        sysDeposit.setStatus(Constants.status.able);
        /** 获取公司入款信息*/
        getListBank(listBank, listpayment);
        List<Integer> listBankSort = listBank.stream().map(ls -> ls.getSortId()).collect(Collectors.toList());
        List<Integer> listpaymentSort = listpayment.stream().map(ls -> ls.getSortId()).collect(Collectors.toList());

        if (sysDeposit.getBankType() == 0) {
            if (listBankSort.contains(sysDeposit.getSortId())) {
                throw new RRException("银行转账排序号已存在，请重新输入排序号！");
            } else {
                insertBank(sysDeposit);
            }
        } else {
            if (listpaymentSort.contains(sysDeposit.getSortId())) {
                throw new RRException("第三方支付排序号已存在，请重新输入排序号！");
            } else {
                insertBank(sysDeposit);
            }
        }
    }

    private void insertBank(SysDeposit sysDeposit) {
        sysDepositMapper.insert(sysDeposit);
        sysDeposit.setCreateTime(null);
        SysDeposit sd = sysDepositMapper.selectOne(sysDeposit);
        List<Integer> mbrGrdIds = sysDeposit.getMbrGrdIds();
        if (mbrGrdIds != null) {
            for (Integer mbrGrdId : mbrGrdIds) {
                SysDepMbr sdm = new SysDepMbr();
                sdm.setDepositId(sd.getId());
                sdm.setMemGroId(mbrGrdId);
                sysDepMbrMapper.insert(sdm);
            }
        }
    }

    public void update(SysDeposit sysDeposit) {
        List<SysDeposit> listBank = new ArrayList<SysDeposit>();
        List<SysDeposit> listpayment = new ArrayList<SysDeposit>();
        SysDepMbr record = new SysDepMbr();
        record.setDepositId(sysDeposit.getId());
        sysDepMbrMapper.delete(record);
        List<Integer> mbrGrdIds = sysDeposit.getMbrGrdIds();
        if (mbrGrdIds != null) {
            for (Integer mbrGrdId : mbrGrdIds) {
                SysDepMbr sdm = new SysDepMbr();
                sdm.setDepositId(sysDeposit.getId());
                sdm.setMemGroId(mbrGrdId);
                sysDepMbrMapper.insert(sdm);
            }
        }
        SysUserEntity user = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
        sysDeposit.setModifyUser(user.getUsername());
        sysDeposit.setModifyTime(new Date());
        getListBank(listBank, listpayment);

        SysDeposit deposit = sysDepositMapper.selectByPrimaryKey(sysDeposit.getId());
        List<Integer> listBankSort = listBank.stream().filter(ls ->
                !deposit.getSortId().equals(sysDeposit.getSortId())).map(SysDeposit::getSortId)
                .collect(Collectors.toList());
        List<Integer> listpaymentSort = listpayment.stream().filter(ls ->
                !deposit.getSortId().equals(sysDeposit.getSortId())).map(SysDeposit::getSortId)
                .collect(Collectors.toList());
        if (sysDeposit.getBankType() == 0) {
            if (listBankSort.contains(sysDeposit.getSortId())) {
                throw new RRException("银行转账排序号已存在，请重新输入排序号！");
            } else {
                sysDepositMapper.updateByPrimaryKeySelective(sysDeposit);
            }
        } else {
            if (listpaymentSort.contains(sysDeposit.getSortId())) {
                throw new RRException("第三方支付排序号已存在，请重新输入排序号！");
            } else {
                sysDepositMapper.updateByPrimaryKeySelective(sysDeposit);
            }
        }
    }

    private void getListBank(List<SysDeposit> listBank, List<SysDeposit> listpayment) {
        /** 获取公司入款信息*/
        SysDeposit sysDeposits = new SysDeposit();
        List<SysDeposit> list = sysDepositMapper.querySysDepositList(sysDeposits);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getBankType() == 0) {
                listBank.add(list.get(i));
            } else {
                listpayment.add(list.get(i));
            }
        }
    }

    public void updateByPrimaryKey(SysDeposit sysDeposit) {
        if (sysDeposit.getFeeWay() == Constants.feeWay.fixed) {
            sysDeposit.setFeeFixed(null);
        } else {
            sysDeposit.setFeeScale(null);
            sysDeposit.setFeeTop(null);
        }
        sysDepositMapper.updateByPrimaryKey(sysDeposit);
    }

    public void updateStatus(SysDeposit sysDeposit) {

        sysDepositMapper.updateByPrimaryKeySelective(sysDeposit);
    }

    public void delete(Integer id) {
        SysDepMbr record = new SysDepMbr();
        record.setDepositId(id);
        sysDepMbrMapper.delete(record);
        sysDepositMapper.deleteByPrimaryKey(id);
    }

	/*public void depositExportExcel(SysDeposit sysDeposit, HttpServletResponse response) {
		String fileName = "入款设置" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		List<Map<String, Object>> list = Lists.newLinkedList();
		sysDepositMapper.select(sysDeposit).stream().forEach(sd -> {
			SysDepMbr sdm = new SysDepMbr();
			sdm.setDepositId(sd.getId());
			List<SysDepMbr> sdmList = sysDepMbrMapper.select(sdm);
			Map<String, Object> param = new HashMap<>();
			param.put("code", sd.getCode());
			param.put("bankAccount", sd.getBankAccount());
			param.put("cardNumber", sd.getCardNumber());
			param.put("depositType", sd.getDepositType());
			param.put("realName", sd.getRealName());
			param.put("mbrGrpNum", sdmList.size());
			param.put("depCnt", "0");
			param.put("totalAmt", "0");
			param.put("status", sd.getStatus() == 1 ? "启用" : "禁用");
			list.add(param);
		});
		Workbook workbook = ExcelUtil.commonExcelExportList("mapList", depositExcelPath, list);
		try {
			ExcelUtil.writeExcel(response, workbook, fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

/*	public BigDecimal getHandlingCharge(Integer bankId, BigDecimal fee,Integer accountId) {
		SysDeposit sysDeposit = queryObjectAndGroup(bankId,null);
		MbrDepositCond mbrDepositCond=mbrDepositCondService.getMbrDeposit(accountId);
		return getHandlingCharge(sysDeposit,fee,mbrDepositCond);
	}*/

    /**
     * 线上入款手续费
     *
     * @param sysDeposit
     * @param fee
     * @param mbrDepositCond
     * @return
     */
    public BigDecimal getOnlineHandlingCharge(SysDeposit sysDeposit, BigDecimal fee, MbrDepositCond mbrDepositCond) {
        //当会员组不返回手续费并且是银行卡的时候手续费为 0 其它要计算返回手续费或存款的手续费
        if (mbrDepositCond.getFeeEnable() == Available.disable && sysDeposit.getBankType() == 0) {
            return BigDecimal.ZERO;
        } else {
            if (sysDeposit.getFeeWay() == FeeWayVal.fixed)
                return sysDeposit.getFeeFixed();
            else if (sysDeposit.getFeeWay() == FeeWayVal.scale) {
                BigDecimal temp = fee.multiply(sysDeposit.getFeeScale()).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_DOWN);
                return temp.compareTo(sysDeposit.getFeeTop()) == 1 ? sysDeposit.getFeeTop() : temp;
            }
            return BigDecimal.ZERO;
        }
    }

}
