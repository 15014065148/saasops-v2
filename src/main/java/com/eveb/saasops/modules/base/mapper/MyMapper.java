package com.eveb.saasops.modules.base.mapper;

import tk.mybatis.mapper.common.MySqlMapper;

public interface MyMapper<T> extends  MySqlMapper<T>,AuthorityMapper<T> {

}