package com.eveb.saasops.modules.operate.service;

import java.util.List;
import java.util.stream.Collectors;

import com.eveb.saasops.common.utils.Collections3;
import com.eveb.saasops.modules.operate.entity.TGameLogo;
import com.eveb.saasops.modules.operate.mapper.GameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eveb.saasops.modules.base.service.BaseService;
import com.eveb.saasops.modules.operate.dao.TGmDepotcatMapper;
import com.eveb.saasops.modules.operate.entity.TGmDepotcat;


@Service
public class TGmDepotcatService extends BaseService<TGmDepotcatMapper, TGmDepotcat> {

    @Autowired
    private GameMapper gameMapper;

    public List<TGmDepotcat> catDepotList(Integer catId) {
        TGameLogo gameLogo = new TGameLogo();
        gameLogo.setCatId(catId);
        List<TGameLogo> list = gameMapper.listtGameLogo(gameLogo);
        if (Collections3.isNotEmpty(list)) {
            return list.stream().map(ls -> {
                TGmDepotcat depotcat = new TGmDepotcat();
                depotcat.setDepotName(ls.getDepotName());
                depotcat.setDepotId(ls.getDepotId());
                depotcat.setCatId(ls.getCatId());
                depotcat.setCatName(ls.getCatName());
                return depotcat;
            }).collect(Collectors.toList());
        }
        return null;
    }
}
