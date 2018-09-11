package com.eveb.saasops.modules.operate.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import com.eveb.saasops.common.utils.*;
import com.eveb.saasops.modules.member.entity.MbrAuditBonus;
import com.eveb.saasops.modules.operate.dto.DepotListDto;
import com.eveb.saasops.modules.operate.entity.*;
import com.eveb.saasops.modules.operate.mapper.GameMapper;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.modules.user.dto.ElecGameDto;
import com.eveb.saasops.modules.base.service.BaseService;
import com.eveb.saasops.modules.operate.dao.TGmGameMapper;
import com.eveb.saasops.modules.operate.mapper.OperateMapper;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;

@Service
public class TGmGameService extends BaseService<TGmGameMapper, TGmGame> {

    @Autowired
    private OperateMapper operateMapper;
    @Value("${game.cat.excel.path}")
    private String gameCatExcelPath;
    @Value("${game.excel.path}")
    private String gameExcelPath;
    @Autowired
    private GameMapper gameMapper;

    public PageUtils queryWebListPage(ElecGameDto elecGameDto, Integer pageNo, Integer pageSize) {
        if (StringUtil.isEmpty(elecGameDto.getSortWay()) || elecGameDto.getSortWay().equals("1")) {
            elecGameDto.setSortWay("desc");
        } else {
            elecGameDto.setSortWay("asc");
        }
        PageHelper.startPage(pageNo, pageSize);
        if (StringUtil.isEmpty(elecGameDto.getShowType()) || elecGameDto.getShowType().equals("1")) {
            return BeanUtil.toPagedResult(operateMapper.findWebGameList(elecGameDto));
        } else {
            return BeanUtil.toPagedResult(operateMapper.findlabelGameList(elecGameDto));
        }
    }

    public Object gameAllList(int pageNumber) {
        List<DepotListDto> depotLists = operateMapper.gameAllList();
        List<OprGame> oprGames = operateMapper.gameAllList1(pageNumber);
        int a = 0;
        int b = pageNumber;
        for (DepotListDto depotListDto : depotLists) {
            depotListDto.setDepotGameList(oprGames.subList(a, b));
            a += pageNumber;
            b += pageNumber;
        }
        return depotLists;

    }

    public PageUtils queryCatGameList(Integer depotId, Integer catId) {
        PageHelper.startPage(1, 100);
        return BeanUtil.toPagedResult(operateMapper.findCatGameList(depotId, catId));
    }

    public TGmGame queryObjectOne(Integer key) {
        return operateMapper.selectGameOne(key);
    }

    @Cacheable(cacheNames = ApiConstants.REDIS_GAME_COMPANY_CACHE_KEY, key = "#siteCode+'_'+#gameId")
    public TGmGame queryObjectOne(Integer gameId, String siteCode) {
        return operateMapper.selectGameOne(gameId);
    }

    @CachePut(cacheNames = ApiConstants.REDIS_GAME_COMPANY_CACHE_KEY, key = "#siteCode+'_'+#gameId")
    public TGmGame updateClickNum(Integer gameId, String siteCode) {
        operateMapper.updateGmClickNum(gameId);
        return queryObject(gameId);
    }

    public Object queryTGmGameList(TGmGame tGmGame, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy)) PageHelper.orderBy(orderBy);
        List<TGmGame> tGmGameList = operateMapper.queryTGmGameList(tGmGame);
        return BeanUtil.toPagedResult(tGmGameList);
    }

    public void exportGameExcel(TGmGame tGmGame, HttpServletResponse response) {
        String fileName = "游戏列表" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        List<Map<String, Object>> list = Lists.newLinkedList();
        List<TGmGame> tGmGameList = operateMapper.queryTGmGameList(tGmGame);
        tGmGameList.stream().forEach(cs -> {
            Map<String, Object> param = new HashMap<>();
            param.put("catName", cs.getCatName());
            param.put("depotName", cs.getDepotName());
            param.put("gameTag", cs.getGameTag());
            param.put("memo", cs.getMemo());
            param.put("enablePc", cs.getEnablePc() == 1 ? "启用" : "禁用");
            param.put("enableMb", cs.getEnableMb() == 1 ? "启用" : "禁用");
            param.put("enableTest", cs.getEnableTest() == 1 ? "启用" : "禁用");
            param.put("monthPer", cs.getMonthPer());
            param.put("lastdayPer", cs.getLastdayPer());
            param.put("available", cs.getAvailable() == 1 ? "启用" : "禁用");
            list.add(param);
        });
        Workbook workbook = ExcelUtil.commonExcelExportList("mapList", gameExcelPath, list);
        try {
            ExcelUtil.writeExcel(response, workbook, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object queryGmCatList(TGmDepot tGmDepot, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy))
            PageHelper.orderBy(orderBy);
        List<TGmCat> gameCatList = operateMapper.queryCatList(tGmDepot);
        return BeanUtil.toPagedResult(gameCatList);
    }

    public void exportGameCatExcel(TGmDepot tGmDepot, HttpServletResponse response) {
        String fileName = "游戏分类列表" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        List<Map<String, Object>> list = Lists.newLinkedList();
        List<TGmCat> gameCatList = operateMapper.queryCatList(tGmDepot);
        gameCatList.stream().forEach(cs -> {
            Map<String, Object> param = new HashMap<>();
            param.put("catName", cs.getCatName());
            param.put("gameCount", cs.getGameCount());
            param.put("tMonthPer", cs.getTMonthPer());
            param.put("tLastdayPer", cs.getTLastdayPer());
            list.add(param);
        });
        Workbook workbook = ExcelUtil.commonExcelExportList("mapList", gameCatExcelPath, list);
        try {
            ExcelUtil.writeExcel(response, workbook, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PageUtils queryListGamePage(TGameLogo tGameLogo, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<TGameLogo> list = gameMapper.listtGameLogo(tGameLogo);
        list.stream().forEach(e -> {
            tGameLogo.setDepotId(e.getDepotId());
            Integer nums = gameMapper.findTGmGameNums(tGameLogo.getDepotId());
            Integer openNums = gameMapper.findTGmGameOpenNums(tGameLogo.getDepotId());
            e.setGameCount(openNums + "/" + nums);
            if (e.getEnablePc() == 0) e.setEnableDepotPc(0);
            if (e.getEnableMb() == 0) e.setEnableDepotMb(0);
            if (e.getEnableApp() == 0) e.setEnableDepotApp(0);
            if (e.getEnablePc() == 1 && Objects.isNull(e.getEnableDepotPc())) e.setEnableDepotPc(1);
            if (e.getEnableMb() == 1 && Objects.isNull(e.getEnableDepotMb())) e.setEnableDepotMb(1);
            if (e.getEnableApp() == 1 && Objects.isNull(e.getEnableDepotApp())) e.setEnableDepotApp(1);
        });
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findGameList(TGmGame tGmGame, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<TGmGame> list = gameMapper.findGameList(tGmGame);
        list.stream().forEach(e -> {
            if (e.getEnableMbTem() == 0) e.setEnableGmaeMb(0);
            if (e.getEnablePcTem() == 0) e.setEnableGmaePc(0);
            if (e.getEnableAppTem() == 0) e.setEnableGmaeApp(0);
            if (e.getEnablePcTem() == 1 && Objects.isNull(e.getEnableGmaePc())) e.setEnableGmaePc(1);
            if (e.getEnableMbTem() == 1 && Objects.isNull(e.getEnableGmaeMb())) e.setEnableGmaeMb(1);
            if (e.getEnableAppTem() == 1 && Objects.isNull(e.getEnableGmaeApp())) e.setEnableGmaeApp(1);
        });
        return BeanUtil.toPagedResult(list);
    }

    public int update(TGmGame tGmGame) {
        SetGame setGame = new SetGame();
        setGame.setDepotId(tGmGame.getDepotId());
        setGame.setGameId(tGmGame.getId());
        setGame.setMemo(tGmGame.getMemoGmae());
        setGame.setPopularity(tGmGame.getPopularityGmae());
        setGame.setEnableGmaePc(tGmGame.getEnableGmaePc());
        setGame.setEnableGmaeMb(tGmGame.getEnableGmaeMb());
        setGame.setEnableGmaeApp(tGmGame.getEnableGmaeApp());
        return gameMapper.saveOrUpdataSetGame(setGame);
    }

}
