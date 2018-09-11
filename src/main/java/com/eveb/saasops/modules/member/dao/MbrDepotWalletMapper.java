package com.eveb.saasops.modules.member.dao;

import org.apache.ibatis.annotations.Mapper;

import com.eveb.saasops.modules.base.mapper.MyMapper;
import com.eveb.saasops.modules.member.entity.MbrDepotWallet;

import tk.mybatis.mapper.common.IdsMapper;


@Mapper
public interface MbrDepotWalletMapper extends MyMapper<MbrDepotWallet>,IdsMapper<MbrDepotWallet> {

}
