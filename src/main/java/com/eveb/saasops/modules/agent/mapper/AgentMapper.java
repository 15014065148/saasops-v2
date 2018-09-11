package com.eveb.saasops.modules.agent.mapper;


import com.eveb.saasops.modules.agent.entity.AgentAccount;
import com.eveb.saasops.modules.agent.entity.AgentTree;
import com.eveb.saasops.modules.agent.entity.Commission;
import com.eveb.saasops.modules.operate.dto.AgyAccDto;
import com.eveb.saasops.modules.operate.dto.AgyAccountDto;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface AgentMapper {

    List<AgentAccount> findAccountListPage(AgentAccount agentAccount);

    //查找帐户是否存在
    int findAccountByName(@Param("agyAccount") String agyAccount);

    List<AgentAccount> findAccountList(AgentAccount agentAccount);
    List<AgentAccount> queryAgyTotal(AgentAccount agentAccount);

    List<AgentAccount> findSubordinateAccListPage(AgentAccount agentAccount);

    List<Commission> findCommissionListPage(Commission commission);

    /**
     * 查询未配置域名的代理账号
     * @return
     */
    List<Map<String,Object>> queryAgyCountNoUrl();

    List<AgentAccount> getAgentAccountAuth(AgentAccount agentAccount);

    List<AgentAccount> queryGenAgt(AgyAccountDto ad);

    List<AgentAccount> queryAgyList(AgyAccountDto ad);

    List<AgentAccount> queryAgentList(AgyAccDto agyAccDto);

    List<AgentAccount> selectGenAgy();

    AgentAccount selectByPrimaryKey(Integer genId);

    List<Integer> getAllLocalAgentAccount();

    List<AgentTree> selectAgentTree();

	List<AgentAccount> findAllSubAgency();
}
