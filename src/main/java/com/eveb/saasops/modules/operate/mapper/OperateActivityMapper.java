package com.eveb.saasops.modules.operate.mapper;

import com.eveb.saasops.modules.operate.dto.BonusListDto;
import com.eveb.saasops.modules.operate.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OperateActivityMapper {

    List<OprActActivity> findOprActActivityList(OprActActivity activity);

    OprActActivity findOprActActivity(@Param("id") Integer id);

    int findOprActBouns(OprActBonus waterBonus);

    List<TGmGame> findGameList(TGmGame gmGame);

    List<OprActActivity> findWebActList(
            @Param("actCatId") Integer actCatId,
            @Param("groupId") Integer groupId,
            @Param("tagencyId") Integer tagencyId,
            @Param("cagencyId") Integer cagencyId,
            @Param("terminal") Byte terminal);

    List<OprActActivity> findWebDepositActList(
            @Param("groupId") Integer groupId,
            @Param("tagencyId") Integer tagencyId,
            @Param("cagencyId") Integer cagencyId,
            @Param("terminal") Byte terminal);

    List<OprActBonus> findWaterBonusList(OprActBonus waterBonus);

    List<OprActBonus> findAccountBonusList(OprActBonus bonus);

    int findBounsCount(OprActBonus bonus);

    List<OprActActivity> findActivityBySatatus();

    List<OprActActivity> listActivity(@Param("actTmplIds") String actTmplIds);

    List<BonusListDto> findAccountBouns(
            @Param("accountId") Integer accountId,
            @Param("status") Integer status,
            @Param("id") Integer id);

    List<TGmDepot> findDepotByCatId(
            @Param("catId") Integer catId);

    List<OprActBonus> findAccountBounsByStatus(
            @Param("accountId") Integer accountId,
            @Param("id") Integer id);

    int updateBounsState(
            @Param("activityId") Integer activityId,
            @Param("status") Integer status);
}
