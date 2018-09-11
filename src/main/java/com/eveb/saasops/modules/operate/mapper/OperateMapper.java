package com.eveb.saasops.modules.operate.mapper;

import java.util.List;

import com.eveb.saasops.api.modules.user.dto.ElecGameDto;
import com.eveb.saasops.modules.operate.dto.DepotListDto;
import com.eveb.saasops.modules.operate.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface OperateMapper {

    List<OprNotice> selectNoticeList(OprNotice oprNotice);

    TGmGame selectGameOne(Integer id);

    List<TgmCatLabel> findCatLabelList(@Param("depotId") Integer depotId);

    List<TGmDepotcat> findDepotcat(@Param("catId") Integer catId, @Param("terminal") Byte terminal);

    List<TGmDepotcat> findDepotcatAll();

    List<TGmDepotcat> findCatDepot(@Param("catId") Integer catId);

    List<OprGame> findWebGameList(ElecGameDto elecGameDto);

    List<OprGame> findlabelGameList(ElecGameDto elecGameDto);

    List<DepotListDto> gameAllList();

    List<OprGame> gameAllList1(@Param("pageNumber") int pageNumber);

    List<OprGame> findCatGameList(@Param("depotId") Integer depotId, @Param("catId") Integer catId);

    int updateGmClickNum(@Param("gameId") Integer gameId);

    List<TGmDepot> findelecDepotList(@Param("terminal") Byte terminal);

    public List<TGmCat> queryCatList(TGmDepot tGmDepot);

    List<TGmGame> queryTGmGameList(TGmGame tGmGame);

    List<TGmDepotcat> findcatDepotList(@Param("catId") Integer catId);

    List<OprActActivity> findWebActList();

    List<OprNotice> queryNoticeList(@Param("showType") String showType);

    List<TGmDepot> findDepotBalanceList(@Param("accountId") Integer accountId);

    List<TGmDepot> findDepotList(@Param("accountId") Integer accountId, @Param("terminal") Byte terminal);

}
