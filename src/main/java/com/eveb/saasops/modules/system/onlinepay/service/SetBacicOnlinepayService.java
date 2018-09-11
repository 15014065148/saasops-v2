package com.eveb.saasops.modules.system.onlinepay.service;

import com.eveb.saasops.common.exception.R200Exception;
import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.ExcelUtil;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.modules.member.entity.MbrGroup;
import com.eveb.saasops.modules.member.service.MbrGroupService;
import com.eveb.saasops.modules.system.onlinepay.dao.SetBacicOnlinepayMapper;
import com.eveb.saasops.modules.system.onlinepay.entity.*;
import com.eveb.saasops.modules.system.onlinepay.mapper.MyOnlinepayMapper;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import io.swagger.models.auth.In;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
public class SetBacicOnlinepayService {

    @Autowired
    private MbrGroupService mbrGroupService;
    @Autowired
    private SetBacicOnlinepayMapper setBacicOnlinepayMapper;
    @Autowired
    private MyOnlinepayMapper myOnlinepayMapper;
    @Autowired
    private SetBasicPaymbrgrouprelationService setBasicPaymbrgrouprelationService;
    @Value("${transfer.online.excel.path}")
    private String payOnlineExcelPath;

    public SetBacicOnlinepay queryObject(Integer id) {
        SetBacicOnlinepay setBacicOnlinepay = setBacicOnlinepayMapper.selectByPrimaryKey(id);
        return payRelation(setBacicOnlinepay);
    }

    public PageUtils queryListPage(SetBacicOnlinepay setBacicOnlinepay, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        List<SetBacicOnlinepay> list = setBacicOnlinepayMapper.selectAll();
        return BeanUtil.toPagedResult(list);
    }

    public List<SetBacicOnlinepay> querySetBacicOnlinepayList() {
        return myOnlinepayMapper.getAllBacicOnlinepay();
    }

    public void save(SetBacicOnlinepay setBacicOnlinepay) {
        setBacicOnlinepayMapper.insert(setBacicOnlinepay);

    }

    public int saveGetGeneratedKeys(SetBacicOnlinepay setBacicOnlinepay) {
        return setBacicOnlinepayMapper.insertUseGeneratedKeys(setBacicOnlinepay);
    }

    public void update(SetBacicOnlinepay setBacicOnlinepay) {
        setBacicOnlinepay.setModifyTime(new Date());
        setBacicOnlinepayMapper.updateByPrimaryKeySelective(setBacicOnlinepay);
    }

    public void delete(Integer id) {
        setBacicOnlinepayMapper.deleteByPrimaryKey(id);
    }

    public void deleteBatch(Integer[] ids) {
        myOnlinepayMapper.deleteBatch(ids);
    }

    /**
     * 关联查询
     *
     * @param setBacicOnlinepay
     * @param pageNo
     * @param pageSize
     * @param orderBy
     * @return
     */
    public PageUtils queryConditions(SetBacicOnlinepay setBacicOnlinepay, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        List<SetBacicOnlinepay> setBacicOnlinepays = myOnlinepayMapper.queryConditions(setBacicOnlinepay);
        for (int i = 0; i < setBacicOnlinepays.size(); i++) {
            SetBacicOnlinepay s = setBacicOnlinepays.get(i);
            payRelation(s);
        }
        return BeanUtil.toPagedResult(setBacicOnlinepays);
    }

    private SetBacicOnlinepay payRelation(SetBacicOnlinepay setBacicOnlinepay) {
        List<OnlinePayRelations> payRelations = myOnlinepayMapper.selectBankAndGroup(setBacicOnlinepay.getId(), setBacicOnlinepay.getMbrGroupType());
        if (payRelations.size() != 0) {
            OnlinePayRelations payRelation = payRelations.get(0);
            List<BankOptions> bankOptions = payRelation.getBankOptions();
            List<SelectedGroup> selectedGroups = new ArrayList<>();
            if (setBacicOnlinepay.getMbrGroupType() == 2) {
                selectedGroups = payRelation.getSelectedGroup();
            } else if (setBacicOnlinepay.getMbrGroupType() == 1) {
                List<MbrGroup> mbrGroups = mbrGroupService.queryList();
                for (MbrGroup mbrGroup : mbrGroups) {
                    SelectedGroup selectedGroup = new SelectedGroup(mbrGroup.getGroupName(), mbrGroup.getId());
                    selectedGroups.add(selectedGroup);
                }
            }
            Integer[] banks = new Integer[bankOptions.size()];
            String mbrGroups = "";
            String selectedGroup = "";
            String bankOption = "";
            int y = 0;
            for (BankOptions BankOption : bankOptions) {
                banks[y] = BankOption.getValue();
                bankOption += BankOption.getText() + ",";
                y++;
            }
            for (SelectedGroup sg : selectedGroups) {
                mbrGroups += sg.getId() + ",";
                selectedGroup += sg.getGroupName() + ",";
            }
            setBacicOnlinepay.setBanks(banks);
            setBacicOnlinepay.setSelectedGroup(selectedGroup.endsWith(",") ? selectedGroup.substring(0, selectedGroup.length() - 1) : selectedGroup);
            setBacicOnlinepay.setBankOptions(bankOption.endsWith(",") ? bankOption.substring(0, bankOption.length() - 1) : bankOption);
            setBacicOnlinepay.setMbrGroups(mbrGroups.endsWith(",") ? mbrGroups.substring(0, mbrGroups.length() - 1) : mbrGroups);
        }
        return setBacicOnlinepay;
    }

    /**
     * 查询支付平台及相关信息，下拉列表使用
     *
     * @return
     */
    public List<Map<String, Object>> queryPayment() {
        String siteCode = CommonUtil.getSiteCode();
        return myOnlinepayMapper.queryPayment(siteCode);
    }

    /**
     * 查询支付平台相关的银行
     *
     * @param param
     * @return
     */
    public List<Map<String, Object>> queryPayBanks(String param) {
        Map<String, Object> params = new HashMap<>();
        params.put("paymentId", param);
        return myOnlinepayMapper.queryPayBanks(params);
    }

    /**
     * 联合插入
     *
     * @param setBacicOnlinepay
     */
    @Transactional
    public void unionSave(SetBacicOnlinepay setBacicOnlinepay) {
        if (setBacicOnlinepay.getMinLimit() < 0 || setBacicOnlinepay.getMaxLimit() < 0) {
            throw new R200Exception("单笔存款最大最小值不能为负数");
        }
        int mbrGroupType = setBacicOnlinepay.getMbrGroupType();
        setBacicOnlinepay.setCreateTime(new Date());
        setBacicOnlinepay.setModifyTime(new Date());
        saveGetGeneratedKeys(setBacicOnlinepay);
        //关联插入会员组关联表
        if (mbrGroupType == 2) {
            insertMbrGroupMulti(setBacicOnlinepay);
        }
    }

    private void insertMbrGroupMulti(SetBacicOnlinepay setBacicOnlinepay) {
        List<SetBasicPaymbrgrouprelation> setBasicPaymbrgrouprelations = new ArrayList<>();
        String[] mbrGs = setBacicOnlinepay.getMbrGroups().split(",");
        for (String mbr : mbrGs) {
            if (!StringUtils.isEmpty(mbr)) {
                SetBasicPaymbrgrouprelation setBasicPaymbrgrouprelation = new SetBasicPaymbrgrouprelation();
                setBasicPaymbrgrouprelation.setMbrGroupId(Integer.parseInt(mbr));
                setBasicPaymbrgrouprelation.setOnlinePayId(setBacicOnlinepay.getId());
                setBasicPaymbrgrouprelations.add(setBasicPaymbrgrouprelation);
            }
        }
        if (setBasicPaymbrgrouprelations.size() != 0) {
            setBasicPaymbrgrouprelationService.multiSave(setBasicPaymbrgrouprelations);
        }
    }

    /**
     * 通过id查找关联的会员组
     *
     * @param id
     * @return
     */
    public List<Map<String, Object>> queryMbrGroupById(String id) {
        return myOnlinepayMapper.queryMbrGroupById(id);
    }

    /**
     * 联合更新
     *
     * @param setBacicOnlinepay
     */
    @Transactional
    public void unionUpdate(SetBacicOnlinepay setBacicOnlinepay) {
        if (setBacicOnlinepay.getMinLimit() < 0 || setBacicOnlinepay.getMaxLimit() < 0) {
            throw new R200Exception("单笔存款最大最小值不能为负数");
        }
        int mbrGroupType = setBacicOnlinepay.getMbrGroupType();
        update(setBacicOnlinepay);
        if (mbrGroupType == 2) {
            //删除会员组关联关系后重新插入关联关系
            SetBasicPaymbrgrouprelation setBasicPaymbrgrouprelation = new SetBasicPaymbrgrouprelation();
            setBasicPaymbrgrouprelation.setOnlinePayId(setBacicOnlinepay.getId());
            setBasicPaymbrgrouprelationService.deleteByonlinePayId(setBasicPaymbrgrouprelation);
            insertMbrGroupMulti(setBacicOnlinepay);
        }
    }

    public void ExportExcel(SetBacicOnlinepay setBacicOnlinepay, HttpServletResponse response) {
        String fileName = "线上支付" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        List<Map<String, Object>> list = Lists.newLinkedList();
        myOnlinepayMapper.queryConditions(setBacicOnlinepay).stream().forEach(
                cs -> {
                    Map<String, Object> param = new HashMap<>();
                    param.put("code", cs.getCode());
                    param.put("name", cs.getName());
                    param.put("merNo", cs.getMerNo());
                    param.put("paymentName", cs.getPaymentName());
                    param.put("mbrGroupNum", cs.getMbrGroupNum() == null ? 0 : cs.getMbrGroupNum());
                    param.put("bankNum", cs.getBankName() == null ? 0 : cs.getBankName());
                    param.put("mbrGroupId", "");
                    param.put("mbrGroupId", "");
                    param.put("mbrGroupId", "");
                    param.put("isEnable", cs.getIsEnable() == 1 ? "启用" : "禁用");
                    list.add(param);
                }
        );
        Workbook workbook = ExcelUtil.commonExcelExportList("mapList", payOnlineExcelPath, list);
        try {
            ExcelUtil.writeExcel(response, workbook, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
