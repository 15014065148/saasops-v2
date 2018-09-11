package com.eveb.saasops.modules.operate.service;

import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.modules.base.service.BaseService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.eveb.saasops.modules.operate.dao.SetGameMapper;
import com.eveb.saasops.modules.operate.entity.SetGame;

@Service
public class SetGameService extends BaseService<SetGameMapper, SetGame> {
    @Autowired
    private SetGameMapper setGameMapper;

    public PageUtils queryListPage(SetGame setGame, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        return BeanUtil.toPagedResult(setGameMapper.selectAll());
    }

}
