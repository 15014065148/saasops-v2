package com.eveb.saasops.modules.analysis.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.eveb.saasops.ElasticSearchConnection_Read;
import com.eveb.saasops.api.annotation.CacheDuration;
import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.*;
import com.eveb.saasops.common.validator.Assert;
import com.eveb.saasops.modules.analysis.constants.ElasticSearchConstant;
import com.eveb.saasops.modules.analysis.entity.*;
import com.eveb.saasops.modules.analysis.mapper.AnalysisMapper;
import com.eveb.saasops.modules.member.dao.MbrAccountMapper;
import com.eveb.saasops.modules.member.entity.MbrAccount;
import com.eveb.saasops.modules.member.mapper.MbrMapper;
import com.eveb.saasops.modules.operate.entity.TGmGame;
import com.eveb.saasops.modules.sys.dto.ColumnAuthTreeDto;
import com.eveb.saasops.modules.sys.service.ColumnAuthProviderService;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Response;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class AnalysisService {

    @Autowired
    private AnalysisMapper analysisMapper;

    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private MbrAccountMapper mbrAccountMapper;

    @Autowired
    private ColumnAuthProviderService columnAuthProviderService;

    @Autowired
    private ElasticSearchConnection_Read connection;

    private static final SimpleDateFormat defaultsdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    /***
     *提供前端用户查询
     */
    public PageUtils getRptBetListPage(Integer pageNo, Integer pageSize, String siteCode, String loginName,
                                       String platform, Integer gameCatId, String betStrTime, String betEndTime) {
        GameReportQueryModel model = new GameReportQueryModel();
        model.setSiteCode(siteCode);
        model.setLoginName(loginName);
        model.setPlatform(platform);
        model.setBetStrTime(betStrTime);
        model.setBetEndTime(betEndTime);
        model.setGametype(String.valueOf((gameCatId == null) ? "" : gameCatId));
        return getRptBetListPage(pageNo, pageSize, model);
    }

    /***
     *提供前端用户查询
     */
    public Map getRptBetListReport(String siteCode, String loginName,
                                   String platform, Integer gameCatId, String betStrTime, String betEndTime) {
        GameReportQueryModel model = new GameReportQueryModel();
        model.setSiteCode(siteCode);
        model.setLoginName(loginName);
        model.setPlatform(platform);
        model.setBetStrTime(betStrTime);
        model.setBetEndTime(betEndTime);
        model.setGameCatId(gameCatId);
        return getRptBetListReport(model);
    }

    /***
     *提供前端用户查询
     */
    public Map getMbrBetListReport(String siteCode, String loginName,
                                   String platform, String betStrTime, String betEndTime, Integer roleId) {
        GameReportQueryModel model = new GameReportQueryModel();
        model.setSiteCode(siteCode);
        model.setLoginName(loginName);
        model.setPlatform(platform);
        model.setBetStrTime(betStrTime);
        model.setBetEndTime(betEndTime);
        Map map = getRptBetListReport(model);

        //查询用户具备得列权限功能
        List<ColumnAuthTreeDto> menuList = columnAuthProviderService.getRoleColumnAuth(roleId, 724L, 3L);
        Assert.isNullOrEmpty(menuList, "无权限，请联系管理员");

        for (ColumnAuthTreeDto columnAuthTreeDto : menuList) {
            //判断非空，并且不包含列则删除key
            if (!StringUtil.isEmpty(columnAuthTreeDto.getColumnName()) && !map.containsKey(columnAuthTreeDto.getColumnName())) {
                map.remove(columnAuthTreeDto.getColumnName());
            }
        }
        return map;
    }

    /***
     * V2后台调用，北京前端不能调用
     * @param pageNo
     * @param pageSize
     * @return
     */
    public PageUtils getBkRptBetListPage(Integer pageNo, Integer pageSize, GameReportQueryModel model) {
        PageUtils pageUtils = getRptBetListPage(pageNo, pageSize, model);
        List<RptBetModel> list = (List<RptBetModel>) pageUtils.getList();
        if (list.size() != 0) {
            //获取小计
            list.add(getSubtotal(list));
            //获取总计
            list.add(getTotal(model));
        }
        return pageUtils;
    }

    /***
     * 查询所有注单
     * @param pageNo
     * @param pageSize
     * @return
     */
    public PageUtils getRptBetListPage(Integer pageNo, Integer pageSize, GameReportQueryModel model) {
        List<RptBetModel> list = new ArrayList<>();
        try {
            BoolQueryBuilder builder = setEsQuery(model);
            SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
            searchRequestBuilder.addSort(SortBuilders.fieldSort("betTime").order(SortOrder.DESC));
            searchRequestBuilder.setQuery(builder)
                    .setFrom((pageNo - 1) * pageSize)
                    .setSize(pageSize);
            Response response = connection.restClient_Read.performRequest("GET", ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE + "/_search", Collections.singletonMap("_source", "true"), new NStringEntity(searchRequestBuilder.toString(), ContentType.APPLICATION_JSON));
            log.info("查询所有注单:");
            log.info(searchRequestBuilder.toString().replaceAll("\r|\n", "").replace(" ", ""));
            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            JSONArray hits = ((JSONArray) (((Map) map.get("hits")).get("hits")));
            for (Object obj : hits) {
                Map objmap = (Map) obj;
                RptBetModel rptBetModel = JSON.parseObject(objmap.get("_source").toString(), RptBetModel.class);
                getGameTypeByDepotNameAndGameCode(rptBetModel);
                list.add(rptBetModel);
            }
            //前端统一显示depotNamme（转换）
            depotCodeConverDepotNamme(list);
            //注单详情数据封装
            setOpenResultDetail(list);
            //总记录数
            Long total = Long.parseLong(((Map) map.get("hits")).get("total") + "");
            PageUtils page = BeanUtil.toPagedResult(list);
            page.setPageSize(pageSize);
            page.setTotalCount(total);
            //总页数
            page.setTotalPage(BigDecimalMath.ceil(total.intValue(), pageSize));
            //当前页数
            page.setCurrPage(pageNo);
            return page;
        } catch (Exception e) {
            e.printStackTrace();
            log.info(e.getMessage());
            throw new RRException("查询异常!");
        }
    }

    public Map getRptBetListReport(GameReportQueryModel model) {
        Map rsmap = new HashMap();
        try {
            BoolQueryBuilder builder = setEsQuery(model);
            SumAggregationBuilder betaggs = AggregationBuilders.sum("bet").field("bet");
            SumAggregationBuilder validbetaggs = AggregationBuilders.sum("validBet").field("validBet");
            SumAggregationBuilder rewardaggs = AggregationBuilders.sum("payout").field("payout");
            SumAggregationBuilder jackpotBetAggs = AggregationBuilders.sum("jackpotBet").field("jackpotBet");
            SumAggregationBuilder jackpotPayoutAggs = AggregationBuilders.sum("jackpotPayout").field("jackpotPayout");
            SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
            searchRequestBuilder.addAggregation(betaggs);
            searchRequestBuilder.addAggregation(validbetaggs);
            searchRequestBuilder.addAggregation(rewardaggs);
            searchRequestBuilder.addAggregation(jackpotBetAggs);
            searchRequestBuilder.addAggregation(jackpotPayoutAggs);
            searchRequestBuilder.setQuery(builder);

            Response response = connection.restClient_Read.performRequest("GET", ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE + "/_search", Collections.singletonMap("_source", "true"), new NStringEntity(searchRequestBuilder.toString(), ContentType.APPLICATION_JSON));
            log.info(builder.toString());
            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            Integer counts = (Integer) ((Map) map.get("hits")).get("total");
            BigDecimal bet = (BigDecimal) ((Map) ((Map) map.get("aggregations")).get("bet")).get("value");
            BigDecimal reward = (BigDecimal) ((Map) ((Map) map.get("aggregations")).get("payout")).get("value");
            BigDecimal validbet = (BigDecimal) ((Map) ((Map) map.get("aggregations")).get("validBet")).get("value");
            BigDecimal jackpotBet = (BigDecimal) ((Map) ((Map) map.get("aggregations")).get("jackpotBet")).get("value");
            BigDecimal jackpotPayout = (BigDecimal) ((Map) ((Map) map.get("aggregations")).get("jackpotPayout")).get("value");
            rsmap.put("counts", counts);
            //总投注
            rsmap.put("bet", bet.setScale(2, BigDecimal.ROUND_DOWN));
            //总派彩
            rsmap.put("payout", reward.setScale(2, BigDecimal.ROUND_DOWN));
            //有效投注
            rsmap.put("validBet", validbet.setScale(2, BigDecimal.ROUND_DOWN));
            rsmap.put("jackpotBet", jackpotBet.setScale(2, BigDecimal.ROUND_DOWN));
            rsmap.put("jackpotPayout", jackpotPayout.setScale(2, BigDecimal.ROUND_DOWN));
            return rsmap;
        } catch (Exception e) {
            throw new RRException("查询异常!");
        }
    }

    /***
     * 查询财务费用注单，既奖池投注
     * @param pageNo
     * @param pageSize
     * @return
     */
    public PageUtils getJackpotBetListPage(Integer pageNo, Integer pageSize, GameReportQueryModel model) {
        List<RptBetModel> list = new ArrayList<>();
        try {
            BoolQueryBuilder builder = setEsQuery(model);
            SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
            searchRequestBuilder.addSort(SortBuilders.fieldSort("betTime").order(SortOrder.DESC));
            searchRequestBuilder.setQuery(builder)
                    .setFrom((pageNo - 1) * pageSize)
                    .setSize(pageSize);
            Response response = connection.restClient_Read.performRequest("GET", ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE + "/_search", Collections.singletonMap("_source", "true"), new NStringEntity(searchRequestBuilder.toString(), ContentType.APPLICATION_JSON));
            log.info(builder.toString());
            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            JSONArray hits = ((JSONArray) (((Map) map.get("hits")).get("hits")));
            for (Object obj : hits) {
                Map objmap = (Map) obj;
                list.add(JSON.parseObject(objmap.get("_source").toString(), RptBetModel.class));
            }
            //总记录数
            Long total = Long.parseLong(((Map) map.get("hits")).get("total") + "");
            PageUtils page = BeanUtil.toPagedResult(list);
            page.setTotalCount(total);
            //总页数
            page.setTotalPage(BigDecimalMath.ceil(total.intValue(), pageSize));
            //当前页数
            page.setCurrPage(pageNo);
            return page;
        } catch (Exception e) {
            throw new RRException("查询异常!");
        }
    }

    public PageUtils getRptBetDay(Integer pageNo, Integer pageSize, Integer parentAgentid, Integer agentid, Integer groupid,
                                  String loginName, String platform, String gametype, String betStrTime, String betEndTime, String orderBy, String group) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy))
            PageHelper.orderBy(orderBy);
        StringBuffer sb = new StringBuffer();
        if (group != null) {
            String[] gs = group.split(",");
            try {
                for (String groupStr : gs) {
                    sb.append(groupStr.replace("topAgent", "topagt.agyAccount as topAgent").replace("agent", "agt.agyAccount as agent"));
                    sb.append(",");
                }
            } catch (Exception e) {
                throw new RRException("查询异常!");
            }
        }
        List<RptBetDayModel> list = analysisMapper.getRptBetDay(parentAgentid, agentid, groupid, loginName, platform, gametype, betStrTime, betEndTime, group, sb.toString().length() > 0 ? sb.toString().substring(0, sb.length() - 1) : "");
        return BeanUtil.toPagedResult(list);
    }

    public Map getFundStatistics(Integer parentAgentid, Integer agentid, Integer groupid,
                                 String loginName, String platform, String gametype, String betStrTime, String betEndTime) {
        List<FundStatisticsModel> fundlist = analysisMapper.getFundStatistics(parentAgentid, agentid, groupid, loginName, platform, gametype, betStrTime, betEndTime);
        Map resultMap = new HashMap();
        Map legend = new HashMap();
        Map xAxis = new HashMap();
        Map yAxis = new HashMap();
        Map[] maps = new Map[4];
        Map payoutMap = new HashMap();
        Map depositsMap = new HashMap();
        Map withdrawMap = new HashMap();
        Map profitMap = new HashMap();
        BigDecimal[] payouts = new BigDecimal[fundlist.size()];
        BigDecimal[] deposits = new BigDecimal[fundlist.size()];
        BigDecimal[] withdraws = new BigDecimal[fundlist.size()];
        BigDecimal[] profits = new BigDecimal[fundlist.size()];
        String[] xAxisData = new String[fundlist.size()];
        for (int i = 0; i < fundlist.size(); i++) {
            FundStatisticsModel fund = fundlist.get(i);

            xAxisData[i] = fund.getDate();
            payouts[i] = fund.getPayout();
            deposits[i] = fund.getDeposit();
            withdraws[i] = fund.getWithdraw();
            profits[i] = fund.getProfit();

        }
        payoutMap.put("name", "派彩");
        payoutMap.put("data", payouts);
        depositsMap.put("name", "存款");
        depositsMap.put("data", deposits);
        withdrawMap.put("name", "提款");
        withdrawMap.put("data", withdraws);
        profitMap.put("name", "优惠");
        profitMap.put("data", profits);
        maps[0] = payoutMap;
        maps[1] = depositsMap;
        maps[2] = withdrawMap;
        maps[3] = profitMap;
        legend.put("data", new String[]{"派彩", "存款", "提款", "优惠"});
        xAxis.put("data", xAxisData);
        resultMap.put("legend", legend);
        resultMap.put("xAxis", xAxis);
        resultMap.put("yAxis", yAxis);
        resultMap.put("series", maps);
        return resultMap;
    }

    public FundReportModel getFundReport(Integer parentAgentid, Integer agentid, Integer groupid, String betStrTime, String betEndTime) {
        FundReportModel nowFund = analysisMapper.getFundReport(parentAgentid, agentid, groupid, betStrTime, betEndTime);
        FundReportModel oldFund = analysisMapper.getFundReport(parentAgentid, agentid, groupid, lessYear(betStrTime), lessYear(betEndTime));
        nowFund.setPayoutPercent(getPercent(nowFund.getPayout(), oldFund.getPayout()));
        nowFund.setMemberWithdrawPercent(getPercent(nowFund.getMemberWithdraw(), oldFund.getMemberWithdraw()));
        nowFund.setAgyWithdrawPercent(getPercent(nowFund.getAgyWithdraw(), oldFund.getAgyWithdraw()));
        nowFund.setCommissionPercent(getPercent(nowFund.getCommission(), oldFund.getCommission()));
        nowFund.setDiscountPercent(getPercent(nowFund.getDiscount(), oldFund.getDiscount()));
        nowFund.setProfitPercent(getPercent(nowFund.getProfit(), oldFund.getProfit()));
        return nowFund;
    }


    /***
     * 生成es查询组合
     * @return
     * @throws Exception
     */
    private BoolQueryBuilder setEsQuery(GameReportQueryModel model) throws Exception {
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        /**传入查询的前缀**/
        builder.must(QueryBuilders.termsQuery("sitePrefix", toLowerCase(analysisMapper.getApiPrefixBySiteCode(model.getSiteCode()))));
        if (model.getParentAgentid() != null || model.getAgentid() != null || model.getGroupid() != null) {
            MbrAccount mbr = new MbrAccount();
            mbr.setTagencyId(model.getParentAgentid());
            mbr.setCagencyId(model.getAgentid());
            mbr.setGroupId(model.getGroupid());
            //获取符合代理线和会员组的会员名称
            List<String> usernameList = mbrMapper.getMemberAccountNames(mbr);
            builder.must(QueryBuilders.termsQuery("userName", toLowerCase(usernameList)));
        }
        if (model.getLoginName() != null && !model.getLoginName().equals("")) {
            builder.must(QueryBuilders.termsQuery("userName", model.getLoginName().toLowerCase()));
        }
        if (model.getPlatform() != null && !model.getPlatform().equals("")) {
            builder.must(QueryBuilders.matchPhraseQuery("platform", analysisMapper.getDepotNameToDepotCode(model.getPlatform())));
        }
        if ((model.getPlatform() != null && !model.getPlatform().equals("")) || (model.getGametype() != null && !model.getGametype().equals("")) || (model.getGamename() != null && !model.getGamename().equals(""))) {
            builder.must(createGameQuery(model));
        }
        if (model.getOrigin() != null && !model.getOrigin().equals("")) {
            builder.must(QueryBuilders.matchPhraseQuery("origin", model.getOrigin()));
        }
        /**注单ID**/
        if (model.getBetid() != null && !model.getBetid().equals("")) {
            builder.must(QueryBuilders.queryStringQuery(model.getBetid().toString()).defaultField("id"));
        }
        /**状态**/
        if (model.getStatus() != null && !model.getStatus().equals("")) {
            builder.must(QueryBuilders.queryStringQuery(model.getStatus()).defaultField("status"));
        }
        /**结果：输、赢**/
        if (model.getResult() != null && !model.getResult().equals("")) {
            builder.must(QueryBuilders.termsQuery("result", model.getResult()));
        }
        /**彩金下注**/
        if (model.getGtJpBet() != null) {
            builder.must(QueryBuilders.rangeQuery("jackpotBet").gte(model.getGtJpBet()));
        }
        if (model.getLtJpBet() != null) {
            builder.must(QueryBuilders.rangeQuery("jackpotBet").lte(model.getLtJpBet()));
        }
        /**彩金中奖**/
        if (model.getGtJpReward() != null) {
            builder.must(QueryBuilders.rangeQuery("jackpotPayout").gte(model.getGtJpReward()));
        }
        if (model.getLtJpReward() != null) {
            builder.must(QueryBuilders.rangeQuery("jackpotPayout").lte(model.getLtJpReward()));
        }
        /**下注**/
        if (model.getGtBet() != null) {
            builder.must(QueryBuilders.rangeQuery("bet").gte(model.getGtBet()));
        }
        /**下注**/
        if (model.getLtBet() != null) {
            builder.must(QueryBuilders.rangeQuery("bet").lte(model.getLtBet()));
        }
        /**有效下注**/
        if (model.getGtValidBet() != null) {
            builder.must(QueryBuilders.rangeQuery("validBet").gte(model.getGtValidBet()));
        }
        /**有效下注**/
        if (model.getLtValidBet() != null) {
            builder.must(QueryBuilders.rangeQuery("validBet").lte(model.getLtValidBet()));
        }
        /**派彩**/
        if (model.getGtReward() != null) {
            builder.must(QueryBuilders.rangeQuery("payout").gte(model.getGtReward()));
        }
        /**派彩**/
        if (model.getLtReward() != null) {
            builder.must(QueryBuilders.rangeQuery("payout").lte(model.getLtReward()));
        }
        /**大于开始时间**/
        if (model.getBetStrTime() != null) {
            builder.must(QueryBuilders.rangeQuery("betTime").gte(sdf.format(defaultsdf.parse(model.getBetStrTime()))));
        }
        /**小于结束时间**/
        if (model.getBetEndTime() != null) {
            builder.must(QueryBuilders.rangeQuery("betTime").lte(sdf.format(defaultsdf.parse(model.getBetEndTime()))));
        }
        if (model.getTableNo() != null && !model.getTableNo().equals("")) {
            builder.must(QueryBuilders.termsQuery("tableNo", model.getTableNo().toLowerCase()));
        }
        if (model.getSerialId() != null && !model.getSerialId().equals("")) {
            builder.must(QueryBuilders.termsQuery("serialId", model.getSerialId().toLowerCase()));
        }
        return builder;
    }

    /***
     * 输赢报表会员查询（精确到时/分/秒）生成es查询组合
     * @return
     * @throws Exception
     */
    private BoolQueryBuilder setEsQuery(WinLostEsQueryModel model) throws Exception {
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        List<String> list = new ArrayList<>();
        if (StringUtil.isNotEmpty(model.getAccountId())) {
            list.add(mbrAccountMapper.selectByPrimaryKey(Integer.parseInt(model.getAccountId())).getLoginName());
            /**传入查询的会员名**/
            builder.must(QueryBuilders.termsQuery("userName", toLowerCase(list)));
        }
        /**传入查询的前缀**/
        builder.must(QueryBuilders.termsQuery("sitePrefix", toLowerCase(analysisMapper.getApiPrefixBySiteCode(model.getSiteCode()))));

        /**大于开始时间**/
        if (model.getStartTime() != null && !model.getStartTime().equals("")) {
            builder.must(QueryBuilders.rangeQuery("betTime").gte(sdf.format(defaultsdf.parse(model.getStartTime()))));
        }
        /**小于结束时间**/
        if (model.getEndTime() != null && !model.getEndTime().equals("")) {
            builder.must(QueryBuilders.rangeQuery("betTime").lte(sdf.format(defaultsdf.parse(model.getEndTime()))));
        }
        if ((model.getDepotId() != null && !model.getDepotId().equals("")) || (model.getCatId() != null && !model.getCatId().equals("")) || (model.getSubCatId() != null && !model.getSubCatId().equals(""))) {
            builder.must(createGameQuery(model));
        }
        if (model.getCatId() != null && !model.getCatId().equals("")) {
            builder.must(QueryBuilders.matchPhraseQuery("gameType", analysisMapper.getDepotName(Integer.parseInt(model.getCatId()))));
        }
        if (model.getSubCatId() != null && !model.getCatId().equals("")) {
            builder.must(QueryBuilders.matchPhraseQuery("platform", model.getSubCatId()));
        }
        return builder;
    }

    private BoolQueryBuilder createGameQuery(GameReportQueryModel model) {
        BoolQueryBuilder gameQuery = QueryBuilders.boolQuery();
        //根据游戏分类查询该分类下的游戏
        List<TGmGame> games = analysisMapper.getGameCodeByCategory(model.getPlatform(), model.getGametype(), model.getGamename());
        Map depotMap = new HashMap();
        List<String> depotList = new ArrayList();
        for (TGmGame gmGame : games) {
            depotMap.put(gmGame.getDepotName(), null);
        }
        Iterator iterator = depotMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Object key = entry.getKey();
            depotList.add(key.toString());
        }
        for (String depot : depotList) {
            BoolQueryBuilder gameQueryList = new BoolQueryBuilder();
            for (TGmGame game : games) {
                if (game.getDepotName().equals(depot)) {
                    gameQueryList.should(QueryBuilders.queryStringQuery(game.getGameCode().toLowerCase()).defaultField("gameType"));
                }
            }
            BoolQueryBuilder depotQuery = QueryBuilders.boolQuery().must(QueryBuilders.queryStringQuery(depot.toLowerCase().toLowerCase()).defaultField("platform")).must(gameQueryList);
            gameQuery.should(depotQuery);
        }
        return gameQuery;
    }

    private BoolQueryBuilder createGameQuery(WinLostEsQueryModel model) {
        BoolQueryBuilder gameQuery = QueryBuilders.boolQuery();
        //根据游戏分类查询该分类下的游戏
        List<TGmGame> games = analysisMapper.getGameCodeByCat((StringUtil.isNotEmpty(model.getDepotId())) ? analysisMapper.getDepotName(Integer.parseInt(model.getDepotId())) : "", model.getCatId(), model.getSubCatId());
        Map depotMap = new HashMap();
        List<String> depotList = new ArrayList();
        for (TGmGame gmGame : games) {
            depotMap.put(gmGame.getDepotName(), null);
        }
        Iterator iterator = depotMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Object key = entry.getKey();
            depotList.add(key.toString());
        }
        for (String depot : depotList) {
            BoolQueryBuilder gameQueryList = new BoolQueryBuilder();
            for (TGmGame game : games) {
                if (game.getDepotName().equals(depot)) {
                    gameQueryList.should(QueryBuilders.queryStringQuery(game.getGameCode().toLowerCase()).defaultField("gameType"));
                }
            }
            BoolQueryBuilder depotQuery = QueryBuilders.boolQuery().must(QueryBuilders.queryStringQuery(depot.toLowerCase().toLowerCase()).defaultField("platform")).must(gameQueryList);
            gameQuery.should(depotQuery);
        }
        return gameQuery;
    }

    /****
     * 根据游戏平台及游戏类型获取有效投注
     * @param gamelist
     * @param startTime
     * @param endTime
     * @return
     */
    public List<RptBetModel> getValidBet(String sitePrefix, List<String> userlist, String startTime,
                                         String endTime, List<TGmGame> gamelist, String status, List<String> platForms) {
        List<RptBetModel> rslist = new ArrayList<>();
        TermsAggregationBuilder agg = AggregationBuilders.terms("userName").field("userName").
                subAggregation(AggregationBuilders.sum("validBet").field("validBet"))
                .subAggregation(AggregationBuilders.sum("payout").field("payout"));
        agg.size(ElasticSearchConstant.SEARCH_COUNT);
        BoolQueryBuilder query = QueryBuilders.boolQuery();//查询组合
        query.must(QueryBuilders.rangeQuery("betTime").gte(startTime).lt(endTime));
        query.must(QueryBuilders.termsQuery("sitePrefix", toLowerCase(analysisMapper.getApiPrefixBySiteCode(sitePrefix))));

        if (Collections3.isNotEmpty(userlist)) {
            query.must(QueryBuilders.termsQuery("userName", toLowerCase(userlist)));
        }
        if (StringUtil.isNotEmpty(status)) {
            query.must(QueryBuilders.termsQuery("status", status));
        }
        BoolQueryBuilder pfbuilder = QueryBuilders.boolQuery();
        platForms.forEach(pf -> {
            pfbuilder.should(QueryBuilders.boolQuery().must(QueryBuilders.termsQuery("platform", pf.toLowerCase())));
        });
        query.must(pfbuilder);
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        if (Collections3.isNotEmpty(gamelist)) {
            gamelist.stream().forEach(gmGame -> {
                builder.should(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termsQuery("platform", gmGame.getDepotName().toLowerCase()))
                        .must(QueryBuilders.termsQuery("gameType", gmGame.getGameCode().toLowerCase())));
            });
        }
        query.must(builder);
        SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
        searchRequestBuilder.setQuery(query);
        searchRequestBuilder.addAggregation(agg);
        String str = searchRequestBuilder.toString();
        log.info("稽核获取有效投注额请求参数【" + str + "】");
        try {
            Response response = connection.restClient_Read.performRequest("GET", ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE + "/_search",
                    Collections.singletonMap("_source", "true"), new NStringEntity(str, ContentType.APPLICATION_JSON));
            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            for (Object obj : (JSONArray) ((Map) ((Map) map.get("aggregations")).get("userName")).get("buckets")) {
                Map rs = new HashMap();
                Map objmap = (Map) obj;
                RptBetModel rb = new RptBetModel();
                rb.setUserName(objmap.get("key").toString());
                rb.setValidBet(((BigDecimal) ((Map) objmap.get("validBet")).get("value"))
                        .setScale(2, BigDecimal.ROUND_DOWN));
                rb.setPayout(((BigDecimal) ((Map) objmap.get("payout")).get("value"))
                        .setScale(2, BigDecimal.ROUND_DOWN));
                rslist.add(rb);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        log.info("稽核获取有效投注额【" + JSON.toJSON(rslist) + "】");
        return rslist;
    }

    /****
     * 根据游戏平台、游戏集合获取总输赢
     * @param startTime
     * @param endTime
     * @return
     */
    public List<Map> getUserWinLoss(String sitePrefix, List<String> userlist, String startTime, String endTime) {
        List<Map> rslist = new ArrayList<>();
        /** 统计派彩和奖池派彩**/
        TermsAggregationBuilder agg = AggregationBuilders.terms("userName").field("userName").
                subAggregation(AggregationBuilders.sum("payout").field("payout"))
                .subAggregation(AggregationBuilders.sum("jackpotPayout").field("jackpotPayout"))
                .subAggregation(AggregationBuilders.sum("bet").field("bet"));
        agg.size(ElasticSearchConstant.SEARCH_COUNT);
        //查询组合
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        if (Objects.nonNull(startTime)) {
            query.must(QueryBuilders.rangeQuery("orderDate").gte(startTime).lt(endTime));
        }
        query.must(QueryBuilders.termsQuery("sitePrefix", toLowerCase(analysisMapper.getApiPrefixBySiteCode(sitePrefix))));
        query.must(QueryBuilders.termsQuery("userName", toLowerCase(userlist)));
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        query.must(builder);
        SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
        searchRequestBuilder.setQuery(query);
        searchRequestBuilder.addAggregation(agg);
        String str = searchRequestBuilder.toString();
        try {
            Response response = connection.restClient_Read.performRequest("GET", ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE + "/_search",
                    Collections.singletonMap("_source", "true"), new NStringEntity(str, ContentType.APPLICATION_JSON));
            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            for (Object obj : (JSONArray) ((Map) ((Map) map.get("aggregations")).get("userName")).get("buckets")) {
                Map rs = new HashMap();
                Map objmap = (Map) obj;
                rs.put(objmap.get("key"), ((BigDecimal) ((Map) objmap.get("payout")).get("value")).add(((BigDecimal) ((Map) objmap.get("jackpotPayout")).get("value")))
                        .setScale(2, BigDecimal.ROUND_DOWN));
                rslist.add(rs);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return rslist;
    }


    /****
     * 根据游戏平台、分类、游戏集合获取总输赢 投注开始时间和结束时间
     * @param startTime
     * @param endTime
     * @return
     */
    public List<RptBetTotalModel> getGameCategoryReport(String sitePrefix, String userName, List<String> platforms, String startTime, String endTime) {
        List<RptBetTotalModel> rslist = new ArrayList<>();
        Map<String, Map> pfMap = initGameTree(analysisMapper.getGameList(platforms));

        TermsAggregationBuilder agg = AggregationBuilders.terms("platform").field("platform")
                .subAggregation(AggregationBuilders.sum("payout").field("payout"))
                .subAggregation(AggregationBuilders.sum("validBet").field("validBet"))
                .subAggregation(AggregationBuilders.sum("bet").field("bet"))
                .subAggregation(AggregationBuilders.max("maxTime").field("betTime"))
                .subAggregation(AggregationBuilders.min("minTime").field("betTime"));
        agg.size(ElasticSearchConstant.SEARCH_COUNT);

        pfMap.entrySet().forEach(pf -> {
            String platform = pf.getKey();
            Map<String, List> cateMap = pf.getValue();
            cateMap.entrySet().forEach(cate -> {
                //查询组合
                BoolQueryBuilder query = QueryBuilders.boolQuery();
                if (Objects.nonNull(startTime)) {
                    query.must(QueryBuilders.rangeQuery("betTime").gte(startTime).lt(endTime));
                }
                query.must(QueryBuilders.termsQuery("sitePrefix", toLowerCase(analysisMapper.getApiPrefixBySiteCode(sitePrefix))));
                query.must(QueryBuilders.termsQuery("userName", userName.toLowerCase()));
                BoolQueryBuilder builder = QueryBuilders.boolQuery();
                List<TGmGame> games = cate.getValue();
                games.forEach(game -> {
                    builder.should(QueryBuilders.boolQuery()
                            .must(QueryBuilders.termsQuery("platform", platform.toLowerCase()))
                            .must(QueryBuilders.termsQuery("gameType", game.getGameCode().toLowerCase())));
                });
                query.must(builder);
                SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
                searchRequestBuilder.setQuery(query);
                searchRequestBuilder.addAggregation(agg);
                String str = searchRequestBuilder.toString();
                try {
                    Response response = connection.restClient_Read.performRequest("GET", ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE + "/_search",
                            Collections.singletonMap("_source", "true"), new NStringEntity(str, ContentType.APPLICATION_JSON));
                    Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
                    for (Object obj : (JSONArray) ((Map) ((Map) map.get("aggregations")).get("platform")).get("buckets")) {
                        Map objmap = (Map) obj;
                        RptBetTotalModel betTotal = new RptBetTotalModel();
                        betTotal.setPlatform(platform);
                        betTotal.setGameCategory(cate.getKey());
                        betTotal.setBetTotal(((BigDecimal) ((Map) objmap.get("bet")).get("value"))
                                .setScale(2, BigDecimal.ROUND_DOWN));
                        betTotal.setValidBetTotal(((BigDecimal) ((Map) objmap.get("validBet")).get("value"))
                                .setScale(2, BigDecimal.ROUND_DOWN));
                        betTotal.setPayoutTotal(((BigDecimal) ((Map) objmap.get("payout")).get("value"))
                                .setScale(2, BigDecimal.ROUND_DOWN));
                        betTotal.setMinTime(defaultsdf.format(sdf.parse(((Map) objmap.get("minTime")).get("value_as_string").toString())));
                        betTotal.setMaxTime(defaultsdf.format(sdf.parse(((Map) objmap.get("minTime")).get("value_as_string").toString())));
                        rslist.add(betTotal);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                log.info("稽核获取有效投注额【" + JSON.toJSON(rslist) + "】");
            });
        });
        return rslist;
    }

    /***
     * 获取每个平台最新注单的时间
     * @param sitePrefix
     * @param userName
     * @return
     */
    public List<RptBetTotalModel> getPlatformMaxTime(String sitePrefix, String userName) {
        List<RptBetTotalModel> rslist = new ArrayList<>();
        TermsAggregationBuilder agg = AggregationBuilders.terms("platform").field("platform")
                .subAggregation(AggregationBuilders.max("maxTime").field("betTime"));
        agg.size(ElasticSearchConstant.SEARCH_COUNT);
        //查询组合
        BoolQueryBuilder query = QueryBuilders.boolQuery();

        query.must(QueryBuilders.termsQuery("sitePrefix", toLowerCase(analysisMapper.getApiPrefixBySiteCode(sitePrefix))));
        if (Objects.nonNull(userName)) {
            query.must(QueryBuilders.termsQuery("userName", userName.toLowerCase()));
        }
        SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
        searchRequestBuilder.setQuery(query);
        searchRequestBuilder.addAggregation(agg);
        String str = searchRequestBuilder.toString();
        log.info("获取每个平台最新注单的时间【" + str + "】");
        try {
            Response response = connection.restClient_Read.performRequest("GET", ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE + "/_search",
                    Collections.singletonMap("_source", "true"), new NStringEntity(str, ContentType.APPLICATION_JSON));
            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            for (Object obj : (JSONArray) ((Map) ((Map) map.get("aggregations")).get("platform")).get("buckets")) {
                Map objmap = (Map) obj;
                RptBetTotalModel betTotal = new RptBetTotalModel();
                betTotal.setPlatform(objmap.get("key").toString());
                betTotal.setMaxTime(defaultsdf.format(sdf.parse(((Map) objmap.get("maxTime")).get("value_as_string").toString())));
                rslist.add(betTotal);
            }
            return rslist;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }


    public List<SelectModel> getPlatForm() {
        return analysisMapper.getPlatForm();
    }

    public List<SelectModel> getGameType(String platFormId, Integer parentId) {
        return analysisMapper.getGameType(platFormId, parentId);
    }

    /***
     * 获取最后一条投注时间
     */
    public String getBetLastDate(String sitePrefix) throws Exception {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(QueryBuilders.termsQuery("sitePrefix", toLowerCase(analysisMapper.getApiPrefixBySiteCode(sitePrefix))));
        SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
        searchRequestBuilder.addSort(SortBuilders.fieldSort("betTime").order(SortOrder.DESC));
        searchRequestBuilder.setQuery(query)
                .setSize(1);
        Response response = connection.restClient_Read.performRequest("GET", ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE + "/_search", Collections.singletonMap("_source", "true"), new NStringEntity(searchRequestBuilder.toString(), ContentType.APPLICATION_JSON));
        log.info(searchRequestBuilder.toString());
        Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
        JSONArray hits = ((JSONArray) (((Map) map.get("hits")).get("hits")));
        for (Object obj : hits) {
            Map objmap = (Map) obj;
            objmap = JSON.parseObject(objmap.get("_source").toString());
            Date betLastDate = sdf.parse(objmap.get("betTime").toString());
            String betLastDateStr = defaultsdf.format(betLastDate);
            return betLastDateStr;
        }
        return null;
    }

    public static Map<String, Map> initGameTree(List<TGmGame> gmGames) {
        Map<String, Map> pfMap = new HashMap<>();
        gmGames.forEach(pf -> {
            pfMap.put(pf.getDepotName(), null);
        });

        pfMap.keySet().forEach(key -> {
            Map cate = new HashMap();
            gmGames.forEach(g -> {
                if (key.equals(g.getDepotName())) {
                    cate.put(g.getCatName(), null);
                }
            });
            pfMap.put(key, cate);
        });

        pfMap.entrySet().forEach(entry -> {
            Map cateMap = entry.getValue();
            cateMap.keySet().forEach(cate -> {
                List<TGmGame> games = new ArrayList<>();
                gmGames.forEach(g -> {
                    if (cate.equals(g.getCatName()) && entry.getKey().equals(g.getDepotName())) {
                        games.add(g);
                    }
                });
                cateMap.put(cate, games);
            });
            pfMap.put(entry.getKey(), cateMap);
        });
        return pfMap;
    }


    /***
     * 查询输赢
     * @return
     */
    public PageUtils findRptWinLostPage(WinLostReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptWinLostModel> list = analysisMapper.getRptWinLostList(model);
        /**小计**/
        list.add(totalWinLost(list));
        list.add(analysisMapper.getRptWinLostTotal(model));
        return BeanUtil.toPagedResult(list);
    }

    /***
     * 查询输赢，根据总代 代理 会员组进行分组
     * @return
     */
    public PageUtils findRptWinLostGroupPage(WinLostReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptWinLostModel> list = analysisMapper.getRptWinLostGroup(model);
        /**小计**/
        list.add(totalWinLost(list));
        list.add(analysisMapper.getRptWinLostTotal(model));
        return BeanUtil.toPagedResult(list);
    }

    /***
     * 查询输赢，根据 代理 进行分组
     * @return
     */
    public PageUtils findWinLostGroupAgentReportPage(WinLostReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptWinLostModel> list = analysisMapper.getRptWinLostGroupAgent(model);
        /**小计**/
        list.add(totalWinLost(list));
        list.add(analysisMapper.getRptWinLostTotal(model));
        return BeanUtil.toPagedResult(list);
    }

    /***
     * 查询输赢，根据会员 会员组进行分组
     * @return
     */
    public PageUtils findRptWinLostGroupUserPage(WinLostReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptWinLostModel> list = analysisMapper.getRptWinLostGroupUser(model);
        /**小计**/
        list.add(totalWinLost(list));
        list.add(analysisMapper.getRptWinLostGroupUserTotal(model));
        return BeanUtil.toPagedResult(list);
    }


    /***
     * 查询会员 存款 提款 红利数据
     * @return
     */
    public PageUtils findTransactionPage(WinLostReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        /** 设置账号显示规则：
         1、如果是邮箱，则显示前二位及.com。
         2、如果是电话号码，则隐藏中间四位。
         3、如果是微信收款，则显示后三位字符。
         4、如果是银行，则显示银行名称代码加银行帐号后四位。
         */
        List<TransactionModel> list = analysisMapper.getTransactionList(model);
        for (int i = 0; i < list.size(); i++) {
            /** 获取账号值 */
            String accountName = list.get(i).getAccountName();
            /** 获取入款类别 */
            String mark = list.get(i).getMark();
            /** 银行代码 */
            String bankCode = list.get(i).getBankcode();
            if (accountName == null || StringUtils.isEmpty(accountName)) ;
//                else if (mark.equals("1")){
            /** 判断是否为邮箱 */
            else if (VerificationUtil.isEmail(accountName)) {
                String emailNumber = accountName.replaceAll(accountName.substring(2, accountName.lastIndexOf(".")), "******");
                list.get(i).setAccountName(emailNumber);
            }
            /** 判断是否为手机号 */
            else if (VerificationUtil.isPhone(accountName)) {
                String phoneNumber = accountName.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
                list.get(i).setAccountName(phoneNumber);
            } else if (!StringUtils.isEmpty(bankCode)) {
                /** 获取银行卡号后四位 */
                String bankNumber = accountName.substring(accountName.length() - 4);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(bankCode);
                stringBuilder.append(bankNumber);
                list.get(i).setAccountName(stringBuilder.toString());
            } else if (VerificationUtil.hasChineseByRange(accountName)) {
                list.get(i).setAccountName(accountName);
            } else {
                String wxNumber = accountName.substring(accountName.length() - 3);
                list.get(i).setAccountName(wxNumber);
            }
        }
//        }
        return BeanUtil.toPagedResult(list);
    }


    public PageUtils findBonusReportPage(BounsReportQueryModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptWinLostModel> list = analysisMapper.getBonusReportList(model);
        list.add(totalWinLost(list));
        list.add(analysisMapper.getBonusReportListTotal(model));
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findBonusGroupTopAgentReportPage(BounsReportQueryModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptWinLostModel> list = analysisMapper.getBonusGroupTopAgentReportList(model);
        list.add(totalWinLost(list));
        list.add(analysisMapper.getBonusReportListTotal(model));
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findBonusGroupAgentReportPage(BounsReportQueryModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptWinLostModel> list = analysisMapper.getBonusGroupAgentReportList(model);
        list.add(totalWinLost(list));
        list.add(analysisMapper.getBonusReportListTotal(model));
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findBonusGroupUserReportPage(BounsReportQueryModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptWinLostModel> list = analysisMapper.getBonusGroupUserReportList(model);
        list.add(totalWinLost(list));
        list.add(analysisMapper.getBonusReportListTotal(model));
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findBonusGroupUserTotal(BounsReportQueryModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptWinLostModel> list = analysisMapper.getBonusGroupUserTotal(model);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findBonusPage(BounsReportQueryModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<TransactionModel> list = analysisMapper.getBonusList(model);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findRptBetTotalPage(GameReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptBetTotalModel> list = analysisMapper.getRptBetTotalList(model);
        list.add(totalBetDay(list));
        list.add(analysisMapper.getRptBetTotals(model));
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findRptBetTotalList(GameReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptBetTotalModel> list = analysisMapper.getRptBetTotalList(model);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findBetDayGroupGameTypePage(GameReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptBetTotalModel> list = analysisMapper.getBetDayGroupGameTypeList(model);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findBetDayGroupTopAgentPage(GameReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptBetTotalModel> list = analysisMapper.getBetDayGroupTopAgentList(model);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findBetDayGroupAgentPage(GameReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptBetTotalModel> list = analysisMapper.getBetDayGroupAgentList(model);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findBetDayGroupUserPage(GameReportModel model) {
        PageHelper.startPage(model.getPageNo(), model.getPageSize());
        List<RptBetTotalModel> list = analysisMapper.getBetDayGroupUserList(model);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findBetDayByAgentPage(GameReportModel model) {
        return BeanUtil.toPagedResult(Collections.singletonList(analysisMapper.getBetDayByAgentTotal(model)));
    }

    /***
     * 小计
     * @param list
     * @return
     */
    public RptWinLostModel totalWinLost(List<RptWinLostModel> list) {
        Integer betCounts = 0;
        Integer depositCounts = 0;
        Integer depositTimes = 0;
        Integer withdrawTimes = 0;
        Integer profitTimes = 0;
        Integer profitCounts = 0;
        BigDecimal deposits = BigDecimal.ZERO;
        BigDecimal withdraws = BigDecimal.ZERO;
        BigDecimal earnings = BigDecimal.ZERO;
        BigDecimal profits = BigDecimal.ZERO;
        RptWinLostModel twl = new RptWinLostModel();
        for (RptWinLostModel total : list) {
            betCounts += total.getBetCounts();
            depositCounts += total.getDepositCounts();
            depositTimes += total.getDepositTimes();
            withdrawTimes += total.getWithdrawTimes();
            profitTimes += total.getProfitTimes();
            deposits = deposits.add(total.getDeposits());
            withdraws = withdraws.add(total.getWithdraws());
            earnings = earnings.add(total.getEarnings());
            profits = profits.add(total.getProfits());
            profitCounts += total.getProfitCounts();
        }
        twl.setStartday("小计");
        twl.setBetCounts(betCounts);
        twl.setDepositCounts(depositCounts);
        twl.setDepositTimes(depositTimes);
        twl.setWithdrawTimes(withdrawTimes);
        twl.setProfitTimes(profitTimes);
        twl.setDeposits(deposits);
        twl.setWithdraws(withdraws);
        twl.setEarnings(earnings);
        twl.setProfits(profits);
        twl.setProfitCounts(profitCounts);
        return twl;
    }

    public RptBetTotalModel totalBetDay(List<RptBetTotalModel> list) {
        RptBetTotalModel rsObj = new RptBetTotalModel();
        Integer times = 0;
        /***总投注额***/
        BigDecimal betTotal = BigDecimal.ZERO;
        /***总有效投注额***/
        BigDecimal validBetTotal = BigDecimal.ZERO;
        /***总派彩额***/
        BigDecimal payoutTotal = BigDecimal.ZERO;
        /***累积投注***/
        BigDecimal jackpotBetTotal = BigDecimal.ZERO;
        /***累积派彩***/
        BigDecimal jackpotPayoutTotal = BigDecimal.ZERO;
        for (RptBetTotalModel bet : list) {
            times += bet.getTimes();
            betTotal = betTotal.add(bet.getBetTotal());
            validBetTotal = validBetTotal.add(bet.getValidBetTotal());
            payoutTotal = payoutTotal.add(bet.getPayoutTotal());
            jackpotBetTotal = jackpotBetTotal.add(bet.getJackpotBetTotal());
            jackpotPayoutTotal = jackpotPayoutTotal.add(bet.getJackpotPayoutTotal());
        }
        rsObj.setStartday("小计");
        rsObj.setTimes(times);
        rsObj.setBetTotal(betTotal);
        rsObj.setValidBetTotal(validBetTotal);
        rsObj.setPayoutTotal(payoutTotal);
        rsObj.setJackpotBetTotal(jackpotBetTotal);
        rsObj.setJackpotPayoutTotal(jackpotPayoutTotal);
        if (validBetTotal.compareTo(BigDecimal.ZERO) == 0) {
            rsObj.setWinRate(BigDecimal.ZERO.floatValue());
        } else {
            rsObj.setWinRate(payoutTotal.divide(validBetTotal, 4).multiply(new BigDecimal(100)).floatValue());
        }
        rsObj.setJackpotWinTotal(jackpotBetTotal.subtract(jackpotPayoutTotal));
        return rsObj;
    }

    public List<Map> getAgentAccount() {
        List<Map> list = analysisMapper.getAgentAccount();
        return list;
    }

    /**
     * 百分比= (now - old / old)*100
     *
     * @param now
     * @param old
     * @return
     */
    private BigDecimal getPercent(BigDecimal now, BigDecimal old) {
        BigDecimal hundred = new BigDecimal(100);
        return (old.compareTo(new BigDecimal("0")) > 0 ? now.subtract(old).divide(old) : now).multiply(hundred);
    }

    private String lessYear(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String str = "2017-12-07";
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sdf.parse(str));
        } catch (ParseException e) {
            return str;
        }
        calendar.add(Calendar.YEAR, -1);
        return sdf.format(calendar.getTime());
    }

    public List toLowerCase(List list) {
        List newList = new ArrayList();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            newList.add(String.valueOf(it.next()).toLowerCase());
        }
        return newList;
    }

    public List<RptMemberModel> getRptMemberList() {
        //查询条数
        Integer limit = 7;
        List<RptMemberModel> list = analysisMapper.getRptMemberList(limit);
        List<RptMemberModel> rs = new ArrayList<>();
        list.stream().forEach(rm -> {
            rm.setTotalMbrs(analysisMapper.getRegisterCounts(rm.getStartday()));
            rs.add(rm);
        });
        return rs;
    }

    /***
     * 查询输赢，根据游戏类别
     * @return
     */
    public List<WinLostReport> findWinLostList(WinLostReport winLostReport) {
        List<WinLostReport> list = analysisMapper.findWinLostList(winLostReport);
        /**小计**/
        list.add(winLostTotal(list));
        return list;
    }

    /***
     * 查询输赢，根据总代
     * @return
     */
    public PageUtils findWinLostListOfTagency(WinLostReport winLostReport) {
        PageHelper.startPage(winLostReport.getPageNo(), winLostReport.getPageSize());
        List<WinLostReport> list = analysisMapper.findWinLostListOfTagency(winLostReport);
        /**小计**/
        list.add(winLostTotal(list));
        return BeanUtil.toPagedResult(list);
    }

    /***
     * 查询输赢，根据总代->代理
     * @return
     */
    public PageUtils findWinLostListByTagencyId(WinLostReport winLostReport) {
        PageHelper.startPage(winLostReport.getPageNo(), winLostReport.getPageSize());
        List<WinLostReport> list = analysisMapper.findWinLostListByTagencyId(winLostReport);
        /**小计**/
        list.add(winLostTotal(list));
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findWinLostListByCagencyId(WinLostReport winLostReport) {
        PageHelper.startPage(winLostReport.getPageNo(), winLostReport.getPageSize());
        List<WinLostReport> list = analysisMapper.findWinLostListByCagencyId(winLostReport);
        /**小计**/
        list.add(winLostTotal(list));
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils findWinLostListByAccountId(WinLostEsQueryModel model) {
        List<RptBetModel> rptBetModels = new ArrayList<>();
        try {
            BoolQueryBuilder builder = setEsQuery(model);
            SearchRequestBuilder searchRequestBuilder = connection.client.prepareSearch("report");
            searchRequestBuilder.addSort(SortBuilders.fieldSort("betTime").order(SortOrder.DESC));
            searchRequestBuilder.setQuery(builder)
                    .setFrom((model.getPageNo() - 1) * model.getPageSize())
                    .setSize(model.getPageSize());
            Response response = connection.restClient_Read.performRequest("GET", ElasticSearchConstant.REPORT_INDEX + "/" + ElasticSearchConstant.REPORT_TYPE + "/_search", Collections.singletonMap("_source", "true"), new NStringEntity(searchRequestBuilder.toString(), ContentType.APPLICATION_JSON));
            log.info(builder.toString());
            Map map = (Map) JSON.parse(EntityUtils.toString(response.getEntity()));
            JSONArray hits = ((JSONArray) (((Map) map.get("hits")).get("hits")));
            for (Object obj : hits) {
                Map objmap = (Map) obj;
                rptBetModels.add(JSON.parseObject(objmap.get("_source").toString(), RptBetModel.class));
            }
            List<WinLostReport> list = getWinLostReportList(rptBetModels);
            log.info(builder.toString());
            /**小计**/
            list.add(winLostTotal(list));
            //总记录数
            Long total = Long.parseLong(((Map) map.get("hits")).get("total") + "");
            PageUtils page = BeanUtil.toPagedResult(list);
            page.setTotalCount(total);
            //总页数
            page.setTotalPage(BigDecimalMath.ceil(total.intValue(), model.getPageSize()));
            //当前页数
            page.setCurrPage(model.getPageNo());
            return page;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RRException("查询异常!");
        }
    }

    public WinLostReport winLostTotal(List<WinLostReport> list) {
        WinLostReport winLostReport = new WinLostReport();
        Long total = 0L;
        BigDecimal betTotal = BigDecimal.ZERO;
        BigDecimal validbetTotal = BigDecimal.ZERO;
        BigDecimal payoutTotal = BigDecimal.ZERO;
        BigDecimal winLostRatio = BigDecimal.ZERO;
        for (WinLostReport w : list) {
            if (w.getTotal() != null && w.getTotal() != 0) {
                total += w.getTotal();
            }
            if (w.getBetTotal() != null && !w.getBetTotal().equals(BigDecimal.ZERO)) {
                betTotal = betTotal.add(w.getBetTotal());
            }
            if (w.getValidbetTotal() != null && !w.getValidbetTotal().equals(BigDecimal.ZERO)) {
                validbetTotal = validbetTotal.add(w.getValidbetTotal());
            }
            if (w.getPayoutTotal() != null && !w.getPayoutTotal().equals(BigDecimal.ZERO)) {
                payoutTotal = payoutTotal.add(w.getPayoutTotal());
            }
            if (w.getWinLostRatio() != null && !w.getWinLostRatio().equals(BigDecimal.ZERO)) {
                winLostRatio = winLostRatio.add(w.getWinLostRatio());
            }
        }
        winLostReport.setLevel("总计");
        winLostReport.setTotal(total);
        winLostReport.setBetTotal(betTotal);
        winLostReport.setValidbetTotal(validbetTotal);
        winLostReport.setPayoutTotal(payoutTotal);
        winLostReport.setWinLostRatio(winLostRatio);
        return winLostReport;
    }

    public List<SelectModel> getDepot() {
        return analysisMapper.getDepot();
    }

    public Integer getValidBetAccountCounts(WinLostReport winLostReport) {
        return analysisMapper.getValidBetAccountCounts(winLostReport);
    }

    public List<SelectModel> getGameCat(String depotId) {
        return analysisMapper.getGameCat(depotId);
    }

    public List<SelectModel> getSubGameCat(String depotId, String catId) {
        return analysisMapper.getSubGameCat(depotId, catId);
    }

    public List<WinLostReport> getWinLostReportList(List<RptBetModel> rptBetModels) {
        List<WinLostReport> winLostReports = new ArrayList<>();
        for (RptBetModel rptBetModel : rptBetModels) {
            WinLostReport winLostReport = new WinLostReport();
            winLostReport.setLoginName(rptBetModel.getUserName());
            winLostReport.setDepositName(rptBetModel.getPlatform());
            winLostReport.setCatName(getGameCatName(rptBetModel.getPlatform(), rptBetModel.getGameType()));
            winLostReport.setGameName(rptBetModel.getGameName());
            winLostReport.setBetTotal(((rptBetModel.getBet() == null) ? BigDecimal.ZERO : rptBetModel.getBet()).setScale(2, BigDecimal.ROUND_HALF_UP));
            winLostReport.setValidbetTotal(((rptBetModel.getValidBet() == null) ? BigDecimal.ZERO : rptBetModel.getValidBet()).setScale(2, BigDecimal.ROUND_HALF_UP));
            winLostReport.setPayoutTotal(((rptBetModel.getPayout() == null) ? BigDecimal.ZERO : rptBetModel.getPayout()).setScale(2, BigDecimal.ROUND_HALF_UP));
            winLostReport.setGameTimes("1");
            winLostReport.setWinLostRatio((rptBetModel.getValidBet() != null && !rptBetModel.getValidBet().equals(BigDecimal.ZERO)) ? (rptBetModel.getPayout().divide(rptBetModel.getValidBet(), 2, BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal(100)) : BigDecimal.ZERO);
            winLostReports.add(winLostReport);
        }
        return winLostReports;
    }

    @Cacheable(cacheNames = ApiConstants.REDIS_WINLOST_CATCH, key = "#depotCode+'_'+#gameCode")
    public String getGameCatName(String depotCode, String gameCode) {
        return analysisMapper.getGameCatName(depotCode, gameCode);
    }

    private void setOpenResultDetail(List<RptBetModel> list) {
        list.forEach(e -> {
            try {
                e.setOpenResultModel(JSON.parseObject(e.getOpenResultDetail(), OpenResultModel.class));
            } catch (Exception ee) {
                e.setOpenResultModel(null);
            }
        });
    }

    public void depotCodeConverDepotNamme(List<RptBetModel> list) {
        list.forEach(e -> {
            try {
                e.setPlatform(conver(e.getPlatform()));
            } catch (Exception ee) {
                e.setOpenResultModel(null);
            }
        });
    }

    @Cacheable(cacheNames = ApiConstants.DEPOTCODE_CONVERTO_DEPOTNAME_CACHE, key = "#DepotCode")
    @CacheDuration(duration = 24 * 60 * 60)
    public String conver(String DepotCode) {
        return analysisMapper.getDepotCodeToDepotName(DepotCode);
    }

    public void getGameTypeByDepotNameAndGameCode(RptBetModel rptBetModel) {
        rptBetModel.setGameType((StringUtil.isNotEmpty(analysisMapper.getGameTypeByDepotNameAndGameCode(rptBetModel)) ? analysisMapper.getGameTypeByDepotNameAndGameCode(rptBetModel) : ""));
    }

    /**
     * 返回小计
     */
    private RptBetModel getSubtotal(List<RptBetModel> list) {
        RptBetModel subTotal = new RptBetModel();
        BigDecimal betTotal = BigDecimal.ZERO;
        BigDecimal validBetTotal = BigDecimal.ZERO;
        BigDecimal payoutTotal = BigDecimal.ZERO;
        for (RptBetModel r : list) {
            if (r.getBet() != null) {
                betTotal = betTotal.add(r.getBet());
            }
            if (r.getValidBet() != null) {
                validBetTotal = validBetTotal.add(r.getValidBet());
            }
            if (r.getPayout() != null) {
                payoutTotal = payoutTotal.add(r.getPayout());
            }
        }
        subTotal.setGameName("小计");
        subTotal.setBet(betTotal);
        subTotal.setValidBet(validBetTotal);
        subTotal.setPayout(payoutTotal);
        return subTotal;
    }

    /**
     * 返回总计
     */
    private RptBetModel getTotal(GameReportQueryModel model) {
        RptBetModel total = new RptBetModel();
        Map map = getRptBetListReport(model);
        total.setGameName("总计");
        total.setBet((BigDecimal) map.get("bet"));
        total.setValidBet((BigDecimal) map.get("validBet"));
        total.setPayout((BigDecimal) map.get("payout"));
        return total;
    }
}
