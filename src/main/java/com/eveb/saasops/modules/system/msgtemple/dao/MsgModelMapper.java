package com.eveb.saasops.modules.system.msgtemple.dao;

import com.eveb.saasops.modules.base.mapper.MyMapper;
import com.eveb.saasops.modules.system.msgtemple.entity.MsgModel;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;


@Mapper
@Component
public interface MsgModelMapper extends MyMapper<MsgModel>  {

}
