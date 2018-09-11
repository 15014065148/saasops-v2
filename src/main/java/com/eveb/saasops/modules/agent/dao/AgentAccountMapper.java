package com.eveb.saasops.modules.agent.dao;

import com.eveb.saasops.modules.base.mapper.MyMapper;
import com.eveb.saasops.modules.agent.entity.AgentAccount;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface AgentAccountMapper extends MyMapper<AgentAccount> {

}