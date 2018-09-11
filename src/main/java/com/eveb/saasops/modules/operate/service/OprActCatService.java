package com.eveb.saasops.modules.operate.service;

import java.util.List;

import com.eveb.saasops.common.exception.R200Exception;
import com.eveb.saasops.modules.operate.dao.OprActActivityMapper;
import com.eveb.saasops.modules.operate.entity.OprActActivity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.modules.base.service.BaseService;
import com.eveb.saasops.modules.operate.dao.OprActCatMapper;
import com.eveb.saasops.modules.operate.entity.OprActCat;
import com.github.pagehelper.PageHelper;


@Service
public class OprActCatService extends BaseService<OprActCatMapper, OprActCat> {

    @Autowired
    private OprActCatMapper oprActCatMapper;
    @Autowired
    private OprActActivityMapper actActivityMapper;


    public PageUtils queryListPage(OprActCat oprActCat, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy))
            PageHelper.orderBy(orderBy);
        List<OprActCat> list = queryListCond(oprActCat);
        return BeanUtil.toPagedResult(list);
    }

    public void delete(Integer id) {
        OprActActivity actActivity = new OprActActivity();
        actActivity.setActCatId(id);
        int count = actActivityMapper.selectCount(actActivity);
        if (count > 0) {
            throw new R200Exception("分类下面有活动，不能删除!");
        }
        oprActCatMapper.deleteByPrimaryKey(id);
    }
}
