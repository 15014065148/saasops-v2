package com.eveb.saasops.modules.member.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.modules.base.service.BaseService;
import com.eveb.saasops.modules.member.dao.MbrMemoMapper;
import com.eveb.saasops.modules.member.entity.MbrMemo;
import com.eveb.saasops.modules.member.mapper.MbrMapper;
import com.github.pagehelper.PageHelper;


@Service
public class MbrMemoService extends BaseService<MbrMemoMapper, MbrMemo> {

    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private MbrMemoMapper mbrMemoMapper;


    public List<MbrMemo> queryListPage(MbrMemo mbrMemo) {
        return mbrMapper.queryAccountMemoList(mbrMemo);
    }

    public Map sortList(Integer accountId, Integer roleId, Integer pageNo, Integer pageSize) {
        Map<String, Object> parmar = new HashMap<>();
        List<MbrMemo> memoList = mbrMapper.queryAccountSortMemo(accountId);
        parmar.put("sort", memoList);
        MbrMemo mbrMemo = new MbrMemo();
        mbrMemo.setRoleId(roleId);
        mbrMemo.setAccountId(accountId);
        PageHelper.startPage(pageNo, pageSize);
        List<MbrMemo> mbrMemos = mbrMemoMapper.select(mbrMemo);
        parmar.put("sortPage", BeanUtil.toPagedResult(mbrMemos));
        return parmar;
    }

    public void deleteBatch(Long[] ids) {
        mbrMapper.deleteMemoBatch(ids);
    }

}
