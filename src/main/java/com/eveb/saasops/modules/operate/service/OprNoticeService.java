package com.eveb.saasops.modules.operate.service;

import java.util.List;
import java.util.Objects;

import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eveb.saasops.common.constants.GroupByConstants;
import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.modules.base.service.BaseService;
import com.eveb.saasops.modules.operate.dao.OprNoticeMapper;
import com.eveb.saasops.modules.operate.entity.OprNotice;
import com.eveb.saasops.modules.operate.mapper.OperateMapper;
import com.github.pagehelper.PageHelper;

@Service
public class OprNoticeService extends BaseService<OprNoticeMapper, OprNotice> {

	@Autowired
	private OperateMapper operateMapper;
	@Autowired
	private OprNoticeMapper oprNoticeMapper;

	public PageUtils queryListPage(OprNotice oprNotice, Integer pageNo, Integer pageSize, String orderBy) {
		PageHelper.startPage(pageNo, pageSize);
		orderBy=GroupByConstants.getOrderBy(GroupByConstants.noticeMod, orderBy);
			PageHelper.orderBy(orderBy);
		List<OprNotice> list = operateMapper.selectNoticeList(oprNotice);
		return BeanUtil.toPagedResult(list);
	}
	

	public Object queryNoticeListPage(String showType,Integer pageNo, Integer pageSize, String orderBy) {
		PageHelper.startPage(pageNo, pageSize);
		orderBy=GroupByConstants.getOrderBy(GroupByConstants.noticeMod, orderBy);
			PageHelper.orderBy(orderBy);
		List<OprNotice> list = operateMapper.queryNoticeList(showType);
		return BeanUtil.toPagedResult(list);
		

	}

	public void deleteBatch(Integer[] ids) {
		if (Objects.nonNull(ids)) {
			String idStr = StringUtil.join(",", ids);
			oprNoticeMapper.deleteByIds(idStr);
		}
	}

}
