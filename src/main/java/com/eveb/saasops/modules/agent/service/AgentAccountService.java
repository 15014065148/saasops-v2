package com.eveb.saasops.modules.agent.service;

import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.exception.R200Exception;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.ExcelUtil;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.config.MessagesConfig;
import com.eveb.saasops.modules.agent.dao.AccCommnMapper;
import com.eveb.saasops.modules.agent.dao.AgentAccountMapper;
import com.eveb.saasops.modules.agent.entity.AccCommn;
import com.eveb.saasops.modules.agent.entity.AgentAccount;
import com.eveb.saasops.modules.agent.entity.AgentTree;
import com.eveb.saasops.modules.agent.entity.Commission;
import com.eveb.saasops.modules.agent.mapper.AgentMapper;
import com.eveb.saasops.modules.base.entity.BaseAuth;
import com.eveb.saasops.modules.base.service.BaseService;
import com.eveb.saasops.modules.sys.dao.SysUserAgyaccountrelationMapper;
import com.eveb.saasops.modules.sys.entity.SysUserAgyaccountrelation;
import com.eveb.saasops.modules.sys.service.SysUserService;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
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

import static com.eveb.saasops.common.constants.Constants.TOP_AGENT_PARENT_ID;
import static java.util.Objects.nonNull;

@Service
@Transactional
@Slf4j
public class AgentAccountService extends BaseService<AgentAccountMapper, AgentAccount>{

    @Autowired
    private AccCommnMapper accCommnMapper;
    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private AgentMapper agentMapper;
    @Value("${agent.account.excel.path}")
    private String accountExcelPath;
    @Value("${agent.subordinate.excel.path}")
    private String subordinateExcelPath;
    @Autowired
    private MessagesConfig messagesConfig;
    @Autowired
    private SysUserAgyaccountrelationMapper agyaccountrelationMapper;
    @Autowired
    private SysUserService sysUserService;

    public List<Integer> getAllLocalAgentAccount(){
        return agentMapper.getAllLocalAgentAccount() ;
    }

    public PageUtils findAccountListPage(AgentAccount record, Integer pageNo, Integer pageSize) {
        record.setBaseAuth(getRowAuth());
        PageHelper.startPage(pageNo, pageSize);
        List<AgentAccount> list = agentMapper.findAccountListPage(record);
        list.forEach(lt -> lt.setAgyPwd(null));
        return BeanUtil.toPagedResult(list);
    }

    public AgentAccount findAccountInfo(Integer id) {
        AgentAccount agentAccount = new AgentAccount();
        agentAccount.setId(id);
        return Optional.ofNullable(
                agentMapper.findAccountListPage(agentAccount).stream()
                        .findAny()).get().orElse(null);
    }

    public Boolean accuntInsert(AgentAccount record, Boolean commonIdSign) {
        int count = agentMapper.findAccountByName(record.getAgyAccount());
        if (count > 0) {
            throw new R200Exception(messagesConfig.getValue("agent.account.exist"));
        }
        record.setAvailable(Constants.Available.enable);
        super.save(record);
        if (commonIdSign) {
            accCommnInsert(record.getId(), record.getCommonId());
        } else {
            record.getCommonIds().stream()
                    .forEach(id ->
                            accCommnInsert(record.getId(), id));
        }
        return Boolean.TRUE;
    }

    private void accCommnInsert(Integer accId, Integer commnId) {
        AccCommn accCommn = new AccCommn();
        accCommn.setAccid(accId);
        accCommn.setCommnId(commnId);
        accCommnMapper.insert(accCommn);
    }

    public void accountExportExecl(AgentAccount record, HttpServletResponse response) {
        String fileName = "总代设置" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xls";
        List<AgentAccount> accounts = agentMapper.findAccountListPage(record);
        List<Map<String, Object>> list = Lists.newArrayList();
        accounts.stream().forEach(account -> {
            Map<String, Object> paramr = new HashMap<>();
            paramr.put("agyAccount", account.getAgyAccount());
            paramr.put("realName", account.getRealName());
            paramr.put("createTime", account.getCreateTime());
            paramr.put("accountNum", account.getAccountNum());
            paramr.put("commissionsName", getCommissionsNames(account.getCommissions()));
            if (nonNull(account.getAvailable())) {
                paramr.put("available", account.getAvailable() == Constants.Available.enable
                        ? Constants.ChineseAvailable.enable : Constants.ChineseAvailable.disable);
            }
            if (nonNull(account.getAgyType())) {
                paramr.put("agyType", account.getAgyType() == Constants.Available.disable
                        ? AgentAccount.AGY_TYPE_EXTERNAL : AgentAccount.AGY_TYPE_INTERIOR);
            }
        });
        Workbook workbook = ExcelUtil.commonExcelExportList("mapList", accountExcelPath, list);
        try {
            ExcelUtil.writeExcel(response, workbook, fileName);
        } catch (IOException e) {
            throw new RRException(e.getMessage());
        }
    }

    private String getCommissionsNames(List<Commission> commissions) {
        StringBuffer buffer = new StringBuffer();
        commissions.stream().forEach(cs -> {
            buffer.append(cs.getCommnName() + " ");
        });
        return buffer.toString();
    }

    public int findAccountByName(String agyAccount) {
        return agentMapper.findAccountByName(agyAccount);
    }

    public List<AgentAccount> findTopAccountAll(Integer parentId) {
    	parentId =(parentId==null?0:parentId);
        AgentAccount agentAccount = new AgentAccount();
        agentAccount.setAvailable(Constants.Available.enable);
        agentAccount.setParentId(parentId);
        if (org.springframework.util.StringUtils.isEmpty(parentId)) {
            agentAccount.setParentId(TOP_AGENT_PARENT_ID);
        }
        BaseAuth baseAuth=getRowAuth();
        log.info("BaseAuth 所有参数-----------{}",baseAuth.toString());
        agentAccount.setBaseAuth(baseAuth);
        if(parentId == 0 ) {
            //此处代理账号为总代,获取所有地区代理上对于的总代
            return agentMapper.queryAgyTotal(agentAccount);
        }
        return agentMapper.findAccountList(agentAccount);
    }

    public List<AgentAccount> getAllParentAccount(String parentIds) {
        AgentAccount agentAccount = new AgentAccount();
        agentAccount.setAvailable(Constants.Available.enable);
        agentAccount.setParentIds(parentIds);
        //此处代理账号为总代,获取所有地区代理上对于的总代
        return agentMapper.getAgentAccountAuth(agentAccount);
    }
    public List<AgentAccount> getAllParentAccount(String parentIds,String enable) {
        AgentAccount agentAccount = new AgentAccount();
        agentAccount.setParentIds(parentIds);
        //此处代理账号为总代,获取所有地区代理上对于的总代
        return agentMapper.getAgentAccountAuth(agentAccount);
    }

    public List<Commission> findCommissionByAccountId(AgentAccount record) {
        List<AgentAccount> accounts = agentMapper.findAccountListPage(record);
        List<Commission> commissions = accounts.get(0).getCommissions();
        return commissions;
    }
    public PageUtils findSubordinateAccListPage(AgentAccount record, Integer pageNo, Integer pageSize) {
        record.setBaseAuth(getRowAuth());
        PageHelper.startPage(pageNo, pageSize);
        List<AgentAccount> list = agentMapper.findSubordinateAccListPage(record);
        return BeanUtil.toPagedResult(list);
    }

    public int update(AgentAccount account) {
        AgentAccount agentAccount = queryObject(account.getId());
        agentAccount.setAvailable(account.getAvailable());
        agentAccount.setModifyUser(account.getModifyUser());
        return agentAccountMapper.updateByPrimaryKey(agentAccount);
    }




    public void subordinateExportExecl(AgentAccount record, HttpServletResponse response) {
        String fileName = "代理设置" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xls";
        List<AgentAccount> accounts = agentMapper.findSubordinateAccListPage(record);
        List<Map<String, Object>> list = Lists.newArrayList();
        accounts.stream().forEach(account -> {
            Map<String, Object> paramr = new HashMap<>();
            paramr.put("agyAccount", account.getAgyAccount());
            paramr.put("realName", account.getRealName());
            paramr.put("createTime", account.getCreateTime());
            paramr.put("accountNum", account.getAccountNum());
            paramr.put("agyTopAccount", account.getAgyTopAccount());
            paramr.put("commissionsName", account.getCommnName());
            paramr.put("available", account.getAvailable() == Constants.Available.enable
                    ? Constants.ChineseAvailable.enable : Constants.ChineseAvailable.disable);
            list.add(paramr);
        });
        Workbook workbook = ExcelUtil.commonExcelExportList("mapList", subordinateExcelPath, list);
        try {
            ExcelUtil.writeExcel(response, workbook, fileName);
        } catch (IOException e) {
            throw new RRException(e.getMessage());
        }
    }

    /**
     * 查询未配置域名的代理账号
     *
     * @return
     */
    public List<Map<String, Object>> queryAgyCountNoUrl() {
        return agentMapper.queryAgyCountNoUrl();
    }

    /**
     * 添加代理权限
     * @param agentId
     * @param userId
     */
    public void addAuthority(Integer agentId,Long userId,int parentId) {
        if (parentId == 0) {
            //总代直接添加
            SysUserAgyaccountrelation agyaccountrelation = new SysUserAgyaccountrelation(userId, agentId, 0);
            agyaccountrelationMapper.insert(agyaccountrelation);
            sysUserService.deleteAuthorityCache(userId, CommonUtil.getSiteCode());
        }
        return;
    }

    public List<AgentTree> selectAgentTree(){
        return agentMapper.selectAgentTree();
    }

    public List<AgentTree> selectLevelsAgentTree(){
        List<AgentAccount> agentAccounts=agentAccountMapper.selectAll();
        return getAgentTreeTop(agentAccounts);
    }

    private List<AgentTree> getAgentTreeTop(List<AgentAccount> agentAccounts){
        List<AgentTree> top = new LinkedList<>();
        for (AgentAccount agentAccount : agentAccounts) {
            if(agentAccount.getParentId() == 0){
                AgentTree agentTree = new AgentTree(agentAccount.getId(),agentAccount.getAgyAccount(),agentAccount.getParentId(),null,false);
                getAgentTreeChildren(agentAccounts,agentTree);
                top.add(agentTree);
            }
        }
        return top;
    }

    private AgentTree getAgentTreeChildren(List<AgentAccount> agentAccounts,AgentTree treeTop){
        List<AgentTree> children = new LinkedList<>();
        for (AgentAccount agentAccount:agentAccounts) {
            if(agentAccount.getParentId() == treeTop.getId()){
                AgentTree child = new AgentTree(agentAccount.getId(),agentAccount.getAgyAccount(),agentAccount.getParentId(),null,false);
                getAgentTreeChildren(agentAccounts,child);
                children.add(child);
            }
        }
        treeTop.setChildren(children);
        return treeTop;
    }

	public List<AgentAccount> findAllSubAgency() {
		return agentMapper.findAllSubAgency();
	}
}

