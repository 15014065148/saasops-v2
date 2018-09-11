package com.eveb.saasops.modules.operate.service;

import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.modules.base.service.BaseService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.eveb.saasops.modules.operate.dao.SetGmGameMapper;
import com.eveb.saasops.modules.operate.entity.SetGmGame;

@Service
public class SetGmGameService extends BaseService<SetGmGameMapper, SetGmGame> {
    @Autowired
    private SetGmGameMapper setGmGameMapper;

    public PageUtils queryListPage(SetGmGame setGmGame, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        return BeanUtil.toPagedResult(setGmGameMapper.selectAll());
    }

}
