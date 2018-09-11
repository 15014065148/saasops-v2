package com.eveb.saasops.modules.operate.service;

import com.eveb.saasops.modules.base.service.BaseService;
import com.eveb.saasops.modules.operate.dao.TGameLogoMapper;
import com.eveb.saasops.modules.operate.entity.SetGmGame;
import com.eveb.saasops.modules.operate.entity.TGameLogo;
import com.eveb.saasops.modules.operate.mapper.GameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TGameLogoService extends BaseService<TGameLogoMapper, TGameLogo> {
    @Autowired
    private GameMapper gameMapper;

    public int update(TGameLogo tGameLogo) {
        SetGmGame setGmGame = new SetGmGame();
        setGmGame.setDepotId(tGameLogo.getDepotId());
        setGmGame.setMemo(tGameLogo.getMemoDepot());
        setGmGame.setGameLogoId(tGameLogo.getId());
        setGmGame.setSortId(tGameLogo.getSortIdDepot());
        setGmGame.setEnableDepotPc(tGameLogo.getEnableDepotPc());
        setGmGame.setEnableDepotMb(tGameLogo.getEnableDepotMb());
        setGmGame.setEnableDepotApp(tGameLogo.getEnableDepotApp());
        return gameMapper.saveOrUpdataSetGmGame(setGmGame);
    }
}
