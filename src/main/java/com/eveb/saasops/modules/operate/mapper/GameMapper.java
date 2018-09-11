package com.eveb.saasops.modules.operate.mapper;

import com.eveb.saasops.modules.operate.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GameMapper {
    /**
     * 根据条件查询游戏平台列表
     *
     * @param tGmDepot
     * @return
     */
    List<TGmDepot> listTGmDepot(TGmDepot tGmDepot);

    /**
     * 保存游戏开关
     *
     * @param setGame
     * @return
     */
    int saveOrUpdataSetGame(SetGame setGame);

    /**
     * 保存游戏平台开关
     *
     * @param setGmGame
     * @return
     */
    int saveOrUpdataSetGmGame(SetGmGame setGmGame);

    /**
     * 根据条件查询游戏分類
     *
     * @return
     */
    List<TGmCat> findGameType();

    /**
     * 根据条件查询子分类
     *
     * @return
     */
    List<TGmCat> findSubCat();

    /**
     * 根据条件查询游戏分类列表
     *
     * @param tGmCat
     * @return
     */
    List<TGmCat> listTGmCat(TGmCat tGmCat);

    /**
     * 根据条件查询游戏列表及统计
     *
     * @param tGmGame
     * @return
     */
    List<TGmGame> listTGmGame(TGmGame tGmGame);

    /**
     * 根据条件查询游戏列表(由于多个分类，故多连了几张表)
     *
     * @param tGameLogo
     * @return
     */
    List<TGameLogo> listtGameLogo(TGameLogo tGameLogo);

    /**
     * 根据条件查询游戏列表
     *
     * @param tGmGame
     * @return
     */
    List<TGmGame> findGameList(TGmGame tGmGame);

    /**
     * @return
     */
    List<TGameLogo> listInfo();

    /**
     * 根据条件查询游戏平台
     *
     * @return
     */
    List<TGmDepot> findGameDepot();

    /**
     * 查询分类下各个平台的游戏总数
     *
     * @param depotId
     * @return
     */
    Integer findTGmGameNums(@Param("depotId") Integer depotId);

    /**
     * 查询分类下各个平台打开游戏总数
     *
     * @param depotId
     * @return
     */
    Integer findTGmGameOpenNums(@Param("depotId") Integer depotId);
}
