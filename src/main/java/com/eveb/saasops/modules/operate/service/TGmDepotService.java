package com.eveb.saasops.modules.operate.service;

import java.util.List;

import com.eveb.saasops.common.utils.StringUtil;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.modules.base.service.BaseService;
import com.eveb.saasops.modules.operate.dao.TGmDepotMapper;
import com.eveb.saasops.modules.operate.entity.TGmDepot;
import com.eveb.saasops.modules.operate.entity.TGmDepotcat;
import com.eveb.saasops.modules.operate.mapper.OperateMapper;

@Service
public class TGmDepotService extends BaseService<TGmDepotMapper, TGmDepot> {
    @Autowired
    private OperateMapper operateMapper;

    public List<TGmDepot> finfTGmDepotList() {
        TGmDepot tGmDepot = new TGmDepot();
        tGmDepot.setAvailable(Constants.Available.enable);
        return super.queryListCond(tGmDepot);
    }

    public List<TGmDepot> finfTGmDepotList(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        TGmDepot tGmDepot = new TGmDepot();
        tGmDepot.setAvailable(Constants.Available.enable);
        return super.queryListCond(tGmDepot);
    }

    public List<TGmDepot> findelecDepotList(Byte terminal) {
        return operateMapper.findelecDepotList(terminal);
    }

    public List<TGmDepotcat> findDepotCatList() {
        return operateMapper.findDepotcatAll();
    }

    public List<TGmDepotcat> findCatDepot(Integer catId) {
        return operateMapper.findCatDepot(catId);
    }

    public List<TGmDepot> findDepotBalanceList(Integer accountId) {
        return operateMapper.findDepotBalanceList(accountId);
    }

    public List<TGmDepot> findDepotList(Integer accountId, Byte terminal) {
        List<TGmDepot> list = operateMapper.findDepotList(accountId,terminal);
        //平台分类名通过/拼接给模板前端使用
        list.forEach(e -> {
            if (StringUtil.isNotEmpty(e.getCatNames())) {
                e.setCatNames(e.getCatNames().replace(",", "/"));
            }
        });
        return list;
    }

}
