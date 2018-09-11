package com.eveb.saasops.modules.system.agencydomain.service;

import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.ExcelUtil;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.modules.system.agencydomain.dao.SystemAgencyUrlMapper;
import com.eveb.saasops.modules.system.agencydomain.entity.SystemAgencyUrl;
import com.eveb.saasops.modules.system.agencydomain.mapper.MySystemAgencyUrlMapper;
import com.eveb.saasops.modules.system.domain.entity.StateType;
import com.eveb.saasops.modules.system.domain.service.SystemDomainService;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
public class SystemAgencyUrlService {
    private static Logger log = Logger.getLogger(SystemDomainService.class);
    @Autowired
    private SystemAgencyUrlMapper systemAgencyUrlMapper;
    @Autowired
    private MySystemAgencyUrlMapper mySystemAgencyUrlMapper;
    @Value("${domain.agency.excel.path}")
    private String systemAgencyUrlPath;

    public SystemAgencyUrl queryObject(Integer agencyId) {
        return systemAgencyUrlMapper.selectByPrimaryKey(agencyId);
    }

    public PageUtils queryListPage(SystemAgencyUrl systemAgencyUrl, Integer pageNo, Integer pageSize,String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if(!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }else {
            PageHelper.orderBy("modifyTime Desc");
        }
        List<SystemAgencyUrl> list = systemAgencyUrlMapper.selectAll();
        return BeanUtil.toPagedResult(list);
    }

    /**
     * 插入，包括批量和单插入
     * @param systemAgencyUrl
     */
    public void save(SystemAgencyUrl systemAgencyUrl) throws CloneNotSupportedException {
        String urls = CommonUtil.remKong(systemAgencyUrl.getUrl());
        if(!StringUtils.isEmpty(urls)) {
            if (urls.indexOf(",") == 0) {
                systemAgencyUrlMapper.insert(systemAgencyUrl);
            } else {
                String[] urlArr = urls.split(",");
                List<SystemAgencyUrl> systemAgencyUrls = new ArrayList<>();
                for (String url : urlArr) {
                    if (!StringUtils.isEmpty(url)) {
                        SystemAgencyUrl sau = new SystemAgencyUrl();
                        try {
                            sau = (SystemAgencyUrl) systemAgencyUrl.clone();
                            sau.setUrl(url);
                        } catch (CloneNotSupportedException e) {
                            log.error("克隆异常");
                            e.printStackTrace();
                            throw e;
                        }
                        systemAgencyUrls.add(sau);
                    }
                }
                mySystemAgencyUrlMapper.multiInsert(systemAgencyUrls);
            }
        }
    }

    public void update(SystemAgencyUrl systemAgencyUrl) {
        systemAgencyUrl.setModifyTime(new Date());
        systemAgencyUrlMapper.updateByPrimaryKeySelective(systemAgencyUrl);
    }

    public void delete(Integer agencyId) {
            systemAgencyUrlMapper.deleteByPrimaryKey(agencyId);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(String ids) {
        mySystemAgencyUrlMapper.multiDelete(ids);
    }

    /**
     * 关联查询
     * @param systemAgencyUrl
     * @param pageNo
     * @param pageSize
     * @param orderBy
     * @return
     */
    public PageUtils queryConditions(SystemAgencyUrl systemAgencyUrl, Integer pageNo, Integer pageSize,String orderBy){
        PageHelper.startPage(pageNo, pageSize);
        if(!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }else {
            PageHelper.orderBy("modifyTime Desc");
        }
        List<SystemAgencyUrl> list =mySystemAgencyUrlMapper.queryConditions(systemAgencyUrl);
        return BeanUtil.toPagedResult(list);
    }

    public void exportExcel(SystemAgencyUrl systemAgencyUrl, HttpServletResponse response) {
        String fileName = "代理域名" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        List<Map<String, Object>> list = Lists.newLinkedList();
        mySystemAgencyUrlMapper.queryConditions(systemAgencyUrl).stream().forEach(
                cs->{
                    Map<String, Object> param = new HashMap<>();
                    param.put("agyAccount", cs.getAgyAccount());
                    param.put("name", cs.getName());
                    Integer sum=cs.getSum();
                    param.put("sum", sum==null?0:sum);
                    param.put("url", cs.getUrl());
                    param.put("bind", cs.getBind()==1?"绑定":"未绑定");
                    param.put("state", StateType.getName(cs.getState()));
                    list.add(param);
                }
        );
        Workbook workbook = ExcelUtil.commonExcelExportList("mapList", systemAgencyUrlPath, list);
        try {
            ExcelUtil.writeExcel(response, workbook, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
