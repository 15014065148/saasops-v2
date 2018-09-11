package com.eveb.saasops.modules.agent.service;

import com.eveb.saasops.common.exception.R200Exception;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.*;
import com.eveb.saasops.config.MessagesConfig;
import com.eveb.saasops.modules.agent.dao.CommissionMapper;
import com.eveb.saasops.modules.agent.dao.ComnChargeMapper;
import com.eveb.saasops.modules.agent.dao.ComnRateMapper;
import com.eveb.saasops.modules.agent.dao.ComnStageMapper;
import com.eveb.saasops.modules.agent.entity.Commission;
import com.eveb.saasops.modules.agent.entity.ComnCharge;
import com.eveb.saasops.modules.agent.entity.ComnRate;
import com.eveb.saasops.modules.agent.entity.ComnStage;
import com.eveb.saasops.modules.agent.mapper.AgentMapper;
import com.eveb.saasops.modules.base.service.BaseService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Transactional
public class CommissionService extends BaseService<CommissionMapper, Commission> {

    @Autowired
    private AgentMapper agentMapper;
    @Autowired
    private ComnStageMapper comnStageMapper;
    @Autowired
    private ComnChargeMapper comnChargeMapper;
    @Autowired
    private ComnRateMapper comnRateMapper;
    @Autowired
    private CommissionMapper commissionMapper;
    @Autowired
    private MessagesConfig messagesConfig;

    @Value("${agent.commission.excel.path}")
    private String commissionExcelPath;

    public PageUtils findCommissionListPage(Commission record, Integer pageNo, Integer pageSize) {
        record.setBaseAuth(getRowAuth());
        PageHelper.startPage(pageNo, pageSize);
        List<Commission> list = agentMapper.findCommissionListPage(record);
        return BeanUtil.toPagedResult(list);
    }

    public Boolean commissionInsert(Commission commission) {
        commissionMapper.insert(commission);
        commission.getComnStages().stream().forEach(cs -> {
            cs.setCommnId(commission.getId());
            comnStageMapper.insert(cs);
            cs.getComnRates().stream().forEach(cts -> {
                cts.setStageId(cs.getId());
                comnRateMapper.insert(cts);
            });
        });
        return Boolean.TRUE;
    }

    public Boolean commissionUpdate(Commission commission) {
        commissionMapper.updateByPrimaryKeySelective(commission);
        commission.getComnStages().stream().forEach(cs -> {
            comnStageMapper.updateByPrimaryKeySelective(cs);
            cs.getComnRates().stream().forEach(cts -> {
                comnRateMapper.updateByPrimaryKeySelective(cts);
            });
        });
        return Boolean.TRUE;
    }

    public Boolean deleteCommn(Integer id) {
        Commission commission = new Commission();
        commission.setId(id);
        List<Commission> list = agentMapper.findCommissionListPage(commission);
        if (Collections3.isNotEmpty(list)) {
            if (list.get(0).getAccCount() > 0) {
                throw new R200Exception(messagesConfig.getValue("agent.commission.delte"));
            }
            commissionMapper.deleteByPrimaryKey(id);
        }
        return Boolean.TRUE;
    }

    public Commission queryCommnInfo(Integer id) {
        Commission commission = commissionMapper.selectByPrimaryKey(id);
        ComnStage comnStage = new ComnStage();
        comnStage.setCommnId(commission.getId());
        List<ComnStage> comnStages = comnStageMapper.select(comnStage);
        comnStages.stream().forEach(cs -> {
            ComnRate comnRate = new ComnRate();
            comnRate.setStageId(cs.getId());
            cs.setComnRates(comnRateMapper.select(comnRate));
        });
        commission.setComnStages(comnStages);
        return commission;
    }

    public void commissionExportExecl(Commission record, HttpServletResponse response) {
        String fileName = "佣金方案" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xls";
        List<Map<String, Object>> list = Lists.newArrayList();
        agentMapper.findCommissionListPage(record)
                .stream().forEach(cs -> {
            Map<String, Object> paramr = new HashMap<>();
            paramr.put("commnName", cs.getCommnName());
            paramr.put("accCount", cs.getAccCount());
            paramr.put("createUser", cs.getCreateUser());
            paramr.put("createTime", cs.getCreateTime());
            paramr.put("modifyUser", cs.getModifyUser());
            paramr.put("modifyTime", cs.getModifyTime());
            list.add(paramr);
        });
        Workbook workbook = ExcelUtil.commonExcelExportList("mapList", commissionExcelPath, list);
        try {
            ExcelUtil.writeExcel(response, workbook, fileName);
        } catch (IOException e) {
            throw new RRException(e.getMessage());
        }
    }

    public Boolean chargeInsertOrUpdate(ComnCharge record, String userName) {
        if (!StringUtil.isEmpty(record.getId())) {
            record.setModifyUser(userName);
            comnChargeMapper.updateByPrimaryKeySelective(record);
        } else {
            record.setCreateUser(userName);
            record.setCreateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
            comnChargeMapper.insert(record);
        }
        return Boolean.TRUE;
    }

    public List<Commission> findCommissionList(Commission record) {
        return commissionMapper.select(record);
    }

    public int update(Commission record) {
        Commission commission = new Commission();
        commission.setId(record.getId());
        commission.setAvailable(record.getAvailable());
        commission.setModifyUser(record.getModifyUser());
        return commissionMapper.updateByPrimaryKeySelective(record);
    }

    public ComnCharge querychargeRate() {
        List<ComnCharge> comnCharges = comnChargeMapper.selectAll();
        if (Collections3.isNotEmpty(comnCharges)) {
            return comnCharges.get(0);
        }
        return null;
    }
}

