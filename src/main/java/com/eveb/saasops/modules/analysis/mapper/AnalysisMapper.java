package com.eveb.saasops.modules.analysis.mapper;

import com.eveb.saasops.modules.analysis.entity.*;
import com.eveb.saasops.modules.operate.entity.TGmGame;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface AnalysisMapper {

    List<TGmGame> getGameList(@Param("platFormNames") List<String> platFormNames);

    List<SelectModel> getPlatForm();

    List<SelectModel> getGameType(@Param("platFormId") String platFormId,@Param("parentId")Integer parentId);

    List<TGmGame> getGameCodeByCategory(@Param("platFormName") String platFormName, @Param("cid") String cid,@Param("gameName") String gameName);

    List<TGmGame> getGameCodeByCat(@Param("depotName") String depotName, @Param("catId") String catId,@Param("subCatId") String SubCatId);

    List<Map> getAgentAccount();

    List<RptBetDayModel> getRptBetDay(@Param("parentAgentid") Integer parentAgentid, @Param("agentid") Integer agentid, @Param("groupid") Integer groupid,
                                      @Param("loginName") String loginName, @Param("platform") String platform, @Param("gametype") String gametype,
                                      @Param("betStrTime") String betStrTime, @Param("betEndTime") String betEndTime, @Param("groups") String group, @Param("group_head") String group_head);

    List<FundStatisticsModel> getFundStatistics(@Param("parentAgentid") Integer parentAgentid, @Param("agentid") Integer agentid, @Param("groupid") Integer groupid,
                                                @Param("loginName") String loginName, @Param("platform") String platform, @Param("gametype") String gametype,
                                                @Param("betStrTime") String betStrTime, @Param("betEndTime") String betEndTime);

    FundReportModel getFundReport(@Param("parentAgentid") Integer parentAgentid, @Param("agentid") Integer agentid, @Param("groupid") Integer groupid,
                                  @Param("betStrTime") String betStrTime, @Param("betEndTime") String betEndTime);

    List<String> getApiPrefixBySiteCode(@Param("siteCode") String siteCode);

    List<RptWinLostModel> getRptWinLostList(WinLostReportModel model);

    RptWinLostModel getRptWinLostTotal(WinLostReportModel model);

    List<RptWinLostModel> getRptWinLostGroup(WinLostReportModel model);

    List<RptWinLostModel> getRptWinLostGroupAgent(WinLostReportModel model);

    List<RptWinLostModel> getRptWinLostGroupUser(WinLostReportModel model);

    /***
     * 查询输赢报表总计，根据会员 会员组进行分组
     * @return
     */
    RptWinLostModel getRptWinLostGroupUserTotal(WinLostReportModel model);

    /***
     * 获红利 存款 取款 等交易记录
     * @return
     */
    List<TransactionModel> getTransactionList(WinLostReportModel model);

    /***
     * 根据时间获取红利，按天聚合
     * @return
     */
    List<RptWinLostModel> getBonusReportList(BounsReportQueryModel model);

    /***
     * 根据时间获取红利，按代理聚合
     * @return
     */
    List<RptWinLostModel> getBonusGroupTopAgentReportList(BounsReportQueryModel model);

    List<RptWinLostModel> getBonusGroupAgentReportList(BounsReportQueryModel model);

    List<RptWinLostModel> getBonusGroupUserReportList(BounsReportQueryModel model);

    List<RptWinLostModel> getBonusGroupUserTotal(BounsReportQueryModel model);

    RptWinLostModel getBonusReportListTotal(BounsReportQueryModel model);

    List<TransactionModel> getBonusList(BounsReportQueryModel model);

    List<RptBetTotalModel> getRptBetTotalList(GameReportModel model);

    RptBetTotalModel getRptBetTotals(GameReportModel model);

    List<RptBetTotalModel> getBetDayGroupGameTypeList(GameReportModel model);

    List<RptBetTotalModel> getBetDayGroupTopAgentList(GameReportModel model);

    List<RptBetTotalModel> getBetDayGroupAgentList(GameReportModel model);

    List<RptBetTotalModel> getBetDayGroupUserList(GameReportModel model);

    RptBetTotalModel getBetDayByAgentTotal(GameReportModel model);

    List<RptMemberModel> getRptMemberList(@Param("lmt") Integer limit);

    Integer getRegisterCounts(@Param("date") String date);

    List<WinLostReport> findWinLostList(WinLostReport winLostReport);

    List<WinLostReport> findWinLostListOfTagency(WinLostReport winLostReport);

    List<WinLostReport> findWinLostListByTagencyId(WinLostReport winLostReport);

    List<WinLostReport> findWinLostListByCagencyId(WinLostReport winLostReport);

    List<WinLostReport> findWinLostListByAccountId(WinLostReport winLostReport);

    List<SelectModel> getDepot();

    Integer getValidBetAccountCounts(WinLostReport winLostReport);

    List<SelectModel> getGameCat(String depotId);

    List<SelectModel> getSubGameCat(@Param("depotId") String depotId,@Param("catId") String catId);

    String getDepotName(Integer depotId);

    String getCatName(Integer catId);

    String getGameCatName(@Param("depotCode") String depotCode,@Param("gameCode") String gameCode);

    String getDepotNameToDepotCode(@Param("depotName") String depotName);

    String getDepotCodeToDepotName(@Param("depotCode") String depotCode);

    String getGameTypeByDepotNameAndGameCode(RptBetModel rptBetModel);
}
