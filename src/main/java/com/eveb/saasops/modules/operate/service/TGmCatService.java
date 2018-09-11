package com.eveb.saasops.modules.operate.service;

import com.eveb.saasops.common.constants.Constants.Available;
import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.common.utils.ExcelUtil;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.modules.base.service.BaseService;
import org.springframework.util.StringUtils;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import com.eveb.saasops.modules.operate.dao.TGmCatMapper;
import com.eveb.saasops.modules.operate.dao.TGmDepotMapper;
import com.eveb.saasops.modules.operate.dao.TGmDepotcatMapper;
import com.eveb.saasops.modules.operate.entity.TGmCat;
import com.eveb.saasops.modules.operate.entity.TGmDepot;
import com.eveb.saasops.modules.operate.entity.TGmDepotcat;
import com.eveb.saasops.modules.operate.entity.TgmCatLabel;
import com.eveb.saasops.modules.operate.mapper.OperateMapper;

@Service
public class TGmCatService extends BaseService<TGmCatMapper, TGmCat> {

	@Autowired
	private TGmDepotMapper tGmDepotMapper;
	@Autowired
	private TGmDepotcatMapper tGmDepotcatMapper;
	@Autowired
	private TGmCatMapper tGmCatMapper;
	@Autowired
	private OperateMapper operateMapper;
	@Value("${game.cat.excel.path}")
	private String gameExcelPath;

	public PageUtils queryTGmCatList(TGmDepot tGmDepot, Integer pageNo, Integer pageSize, String orderBy) {
		PageHelper.startPage(pageNo, pageSize);
		if (!StringUtils.isEmpty(orderBy)) {
			PageHelper.orderBy(orderBy);
		}

		// 游戏平台
		List<TGmDepot> tGmDepots = tGmDepotMapper.select(tGmDepot);
		// List<TGmDepot> tGmDepots = tGmDepotMapper.getTGmDepotList(tGmDepot);

		// 游戏分类
		Set<TGmCat> tGmCatSet = new HashSet<TGmCat>();
		List<TGmDepotcat> tGmDepotcats = new ArrayList<TGmDepotcat>();
		if (tGmDepots != null) {
			for (int s = 0; s < tGmDepots.size(); s++) {
				TGmDepotcat tGmDepotcat = new TGmDepotcat();
				tGmDepotcat.setDepotId(tGmDepots.get(s).getId());
				List<TGmDepotcat> ts = tGmDepotcatMapper.select(tGmDepotcat);
				if (ts != null) {
					tGmDepotcats.addAll(ts);
				}
			}
		}
		if (tGmDepotcats != null) {
			for (int s = 0; s < tGmDepotcats.size(); s++) {
				TGmCat record = new TGmCat();
				record.setId(tGmDepotcats.get(s).getCatId());
				TGmCat tGmCat = tGmCatMapper.selectOne(record);
				if (tGmCat != null) {
					tGmCatSet.add(tGmCat);
				}
			}
		}
		List<TGmCat> tgcList = new ArrayList<TGmCat>();
		if (tGmCatSet != null) {
			for (TGmCat tGmCat : tGmCatSet) {
				tgcList.add(tGmCat);
			}
		}
		return BeanUtil.toPagedResult(tgcList);

	}

	public void exportExcel(TGmDepot tGmDepot, HttpServletResponse response) {
		String fileName = "游戏列表" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		List<Map<String, Object>> list = Lists.newLinkedList();
		// 游戏平台
		List<TGmDepot> tGmDepots = tGmDepotMapper.selectAll();
		// 游戏分类
		Set<TGmCat> tGmCatSet = new HashSet<TGmCat>();
		List<TGmDepotcat> tGmDepotcats = new ArrayList<TGmDepotcat>();
		if (tGmDepots != null) {
			for (int s = 0; s < tGmDepots.size(); s++) {
				TGmDepotcat tGmDepotcat = new TGmDepotcat();
				tGmDepotcat.setDepotId(tGmDepots.get(s).getId());
				List<TGmDepotcat> ts = tGmDepotcatMapper.select(tGmDepotcat);
				if (ts != null) {
					tGmDepotcats.addAll(ts);
				}
			}
		}

		for (int s = 0; s < tGmDepotcats.size(); s++) {
			TGmCat record = new TGmCat();
			record.setId(tGmDepotcats.get(s).getCatId());
			TGmCat tGmCat = tGmCatMapper.selectOne(record);
			if (tGmCat != null) {
				tGmCatSet.add(tGmCat);
			}
		}

		List<TGmCat> tgcList = new ArrayList<TGmCat>();
		if (tGmCatSet != null) {
			for (TGmCat tGmCat : tGmCatSet) {
				tgcList.add(tGmCat);
			}
		}
		tgcList.stream().forEach(cs -> {
			Map<String, Object> param = new HashMap<>();
			param.put("catName", cs.getCatName());
			param.put("gameCount", cs.getGameCount());
			param.put("tMonthPer", cs.getTMonthPer() == null ? "0" : cs.getTMonthPer());
			param.put("tLastdayPer", cs.getTLastdayPer() == null ? "0" : cs.getTLastdayPer());
			list.add(param);
		});
		Workbook workbook = ExcelUtil.commonExcelExportList("mapList", gameExcelPath, list);
		try {
			ExcelUtil.writeExcel(response, workbook, fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<TGmCat> querySubCat(Integer catId)
	{
		TGmCat tGmCat = new TGmCat();
		tGmCat.setParentId(catId);
		tGmCat.setAvailable(Available.enable);
		if (super.selectCount(tGmCat) > 0) {
			return super.queryListCond(tGmCat);
		} else {
			tGmCat.setParentId(null);
			tGmCat.setId(catId);
			return super.queryListCond(tGmCat);
		}

	}
	
	// FIXME 加入缓存
	public List<TgmCatLabel> queryCatLabelList(Integer depotId) {
		return operateMapper.findCatLabelList(depotId);
	}

	// FIXME 加入缓存
	public List<TGmDepotcat> queryDepotCat(Integer catId,Byte terminal) {
		return operateMapper.findDepotcat(catId,terminal);
	}

}
