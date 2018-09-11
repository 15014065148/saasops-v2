package com.eveb.saasops.modules.operate.service;

import com.eveb.saasops.common.constants.AdvConstant;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.exception.R200Exception;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.*;
import com.eveb.saasops.modules.base.service.BaseService;
import com.eveb.saasops.modules.operate.dao.OprAdvImageMapper;
import com.eveb.saasops.modules.operate.dao.OprAdvMapper;
import com.eveb.saasops.modules.operate.entity.AdvBanner;
import com.eveb.saasops.modules.operate.entity.OprAdv;
import com.eveb.saasops.modules.operate.entity.OprAdvImage;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class OprAdvService extends BaseService<OprAdvMapper, OprAdv> {

    @Autowired
    private OprAdvMapper oprAdvMapper;

    @Autowired
    private OprAdvImageMapper oprAdvImageMapper;

    @Value("${opr.adv.excel.path}")
    private String advExcelPath;

    @Autowired
    private QiNiuYunUtil qiNiuYunUtil;

    @Transactional
    public void save(OprAdv oprAdv, String username) {
        OprAdv queryParam = new OprAdv();
        queryParam.setAdvType(oprAdv.getAdvType());
        queryParam.setAdvTypeChild(oprAdv.getAdvTypeChild());
        List<Integer> availables = new ArrayList<>();
        availables.add(1);
        queryParam.setAvailables(availables);
        List<OprAdv> queryOprAdvList = oprAdvMapper.queryOprAdvList(queryParam);
        if (queryOprAdvList != null && queryOprAdvList.size() >= Constants.EVNumber.one) {
            if((1 == queryParam.getAdvType() || 2==queryParam.getAdvType())){
                throw new R200Exception("同一类型不能超过1条数据！");
            }
        }

        oprAdv.setCreater(username);
        oprAdv.setCreateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        if (StringUtils.isEmpty(oprAdv.getAdvType()) || oprAdv.getAdvType() != Constants.EVNumber.zero) {//非轮播清空子类型
            oprAdv.setAdvTypeChild(Constants.EVNumber.zero);
        }
        oprAdvMapper.insertAdvInfo(oprAdv);
        List<OprAdvImage> imageList = oprAdv.getImageList();
        if(!Collections3.isEmpty(imageList)) {
            for (int i = 0; i < imageList.size(); i++) {
                OprAdvImage oprAdvImage = imageList.get(i);
                oprAdvImage.setAdvId(oprAdv.getId());
                //getSingleImageUrl(oprAdvImage.getUploadFile(), oprAdvImage);
                picTarget(oprAdvImage);
                String outStation = oprAdvImage.getOutStation();
                if (null != outStation && !(outStation.contains("http://") || outStation.contains("https://"))) {
                    outStation = "http://" + outStation;
                }
                oprAdvImageMapper.insert(oprAdvImage);
            }
        }
    }

    @Transactional
    public void update(OprAdv oprAdv, String username) {
        oprAdv.setUpdater(username);
        oprAdv.setUpdateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        if (StringUtils.isEmpty(oprAdv.getAdvType()) || oprAdv.getAdvType() != Constants.EVNumber.zero) {//非轮播清空子类型
            oprAdv.setAdvTypeChild(Constants.EVNumber.zero);
        }
        oprAdvMapper.updateByPrimaryKeySelective(oprAdv);
        oprAdvMapper.deleteImageById(oprAdv.getId());

        List<OprAdvImage> imageList = oprAdv.getImageList();
        if(!Collections3.isEmpty(imageList)) {
            for (int i = 0; i < imageList.size(); i++) {
                OprAdvImage oprAdvImage = imageList.get(i);
                if (oprAdvImage.getPicTarget() == AdvConstant.TARGET_IN) {
                    oprAdvImage.setOutStation(null);
                } else if (oprAdvImage.getPicTarget() == AdvConstant.TARGET_OUT) {
                    oprAdvImage.setActId(null);
                    oprAdvImage.setActivityId(null);
                }
                String outStation = oprAdvImage.getOutStation();
                if (null != outStation && !(outStation.contains("http://") || outStation.contains("https://"))) {
                    outStation = "http://" + outStation;
                }
                //getSingleImageUrl(oprAdvImage.getUploadFile(), oprAdvImage);
                picTarget(oprAdvImage);
                oprAdvImageMapper.insert(oprAdvImage);
            }
        }
    }

    public void picTarget(OprAdvImage oprAdvImage) {
        if (oprAdvImage.getPicTarget() == AdvConstant.TARGET_IN) {
            if (null == oprAdvImage.getActId()) {
                throw new R200Exception("活动分类不可为空");
            }
        }
        if (oprAdvImage.getPicTarget() == AdvConstant.TARGET_OUT) {
            if (null == oprAdvImage.getOutStation()) {
                throw new R200Exception("站外路径不可为空");
            }
        }
    }

    public void deleteBatch(OprAdv oprAdv) {

        Map<String, Object> map = new HashMap<>();
        if (null != oprAdv.getIds()) {
            map.put("ids", oprAdv.getIds());
        }
        oprAdvMapper.deleteByIds(map);
    }

    public PageUtils queryListPage(OprAdv oprAdv, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy))
            PageHelper.orderBy(orderBy);
        List<OprAdv> list = oprAdvMapper.queryOprAdvList(oprAdv);
        return BeanUtil.toPagedResult(list);
    }

    public String getSingleImageUrl(MultipartFile uploadFile) {
        String fileName = null;
        if (Objects.nonNull(uploadFile)) {
            try {
                String prefix = uploadFile.getOriginalFilename()
                        .substring(uploadFile.getOriginalFilename().indexOf("."));
                byte[] fileBuff = IOUtils.toByteArray(uploadFile.getInputStream());
                fileName = qiNiuYunUtil.uploadFile(fileBuff, UUID.randomUUID().toString() + prefix);
            } catch (Exception e) {
                throw new RRException(e.getMessage());
            }
        }
        return fileName;
    }

    public void accountExportExcel(OprAdv oprAdv, HttpServletResponse response) {
        String fileName = "广告管理" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ".xls";
        // OprAdv oprAdv = new Gson().fromJson(oprAdv,OprAdv.class);
        List<OprAdv> advList = oprAdvMapper.queryOprAdvList(oprAdv);
        List<Map<String, Object>> list = Lists.newArrayList();
        advList.stream().forEach(adv -> {
            Map<String, Object> paramr = new HashMap<>();
            paramr.put("title", adv.getTitle());
            paramr.put("advType", getAdvType(adv.getAdvType(), adv.getAdvTypeChild()));
            paramr.put("advClient", getClient(adv.getClientShow()));
            paramr.put("advTypeChildNum", adv.getAdvTypeChildNum());
            paramr.put("validity", adv.getUseStart() + "~" + adv.getUseEnd());
            paramr.put("creater", adv.getCreater());
            paramr.put("createTime", adv.getCreateTime());
        });
        Workbook workbook = ExcelUtil.commonExcelExportList("mapList", advExcelPath, list);
        try {
            ExcelUtil.writeExcel(response, workbook, fileName);
        } catch (IOException e) {
            throw new RRException(e.getMessage());
        }
    }

    public String getAdvType(Integer type, Integer typeChild) {
        String str = new String();
        if (type == AdvConstant.ADV_ACROUSEL) {
            str = "轮播图";
            switch (typeChild) {
                case Constants.EVNumber.one:
                    str = str + ">首页";
                    break;
                case Constants.EVNumber.two:
                    str = str + ">真人";
                    break;
                case Constants.EVNumber.three:
                    str = str + ">电子";
                    break;
                case Constants.EVNumber.four:
                    str = str + ">体育";
                    break;
                case Constants.EVNumber.five:
                    str = str + ">彩票";
                    break;

            }
        } else if (type == AdvConstant.ADV_LEFT) {
            str = "左对联";
        } else if (type == AdvConstant.ADV_RIGHT) {
            str = "右对联";
        } else if (type == AdvConstant.ADV_POPUP) {
            str = "弹窗";
        }
        return str;
    }

    public String getClient(Integer client) {
        String str = new String();
        if (client == AdvConstant.CLIENT_PC) {
            str = "PC端";
        } else if (client == AdvConstant.CLIENT_MB) {
            str = "移动端";
        } else if (client == AdvConstant.CLIENT_PC_MB) {
            str = "PC端、移动端";
        }
        return str;
    }

    public void enableOprAdv(OprAdv oprAdv) {
        if(1==oprAdv.getAvailable()){
            List<OprAdv> queryOprAdvList = oprAdvMapper.queryOprAdvByAvailable(oprAdv.getAdvType());
            if (queryOprAdvList != null && queryOprAdvList.size() >= Constants.EVNumber.one) {
                if((1 == oprAdv.getAdvType() || 2==oprAdv.getAdvType())){
                    throw new R200Exception("同一类型不能超过1条数据！");
                }
            }
        }
        oprAdvMapper.updateOprAdvAvailable(oprAdv);
    }

    public OprAdv queryOprAdvInfo(Integer id) {
        OprAdv oprAdv = oprAdvMapper.queryOprAdvInfo(id);
        return oprAdv;
    }

    /**
     * 获取是否设置图片，无则查询模板信息
     *
     * @param bannerDto
     * @return
     */
    public List<AdvBanner> queryWebOprAdvOrBannerList(AdvBanner bannerDto) {
        List<AdvBanner> resultList = new ArrayList<AdvBanner>();
        OprAdv oprAdv = new OprAdv();
        oprAdv.setAdvTypeChild(bannerDto.getAdvType());
        oprAdv.setClientShow(bannerDto.getClientShow());
        oprAdv.setAdvType(0);  //轮播图
        OprAdv oprAdvObj = oprAdvMapper.queryWebOprAdvList(oprAdv);
        if (oprAdvObj != null) {
            List<OprAdvImage> imageList = oprAdvObj.getImageList();
            for (OprAdvImage imageItem : imageList) {
                AdvBanner banner = new AdvBanner();
                banner.setClientShow(oprAdvObj.getClientShow());
                banner.setPicTarget(imageItem.getPicTarget());
                banner.setActId(imageItem.getActId());
                banner.setActivityId(imageItem.getActivityId());
                if (imageItem.getOutStation() != null && imageItem.getOutStation().length() > Constants.EVNumber.seven) {//校是否输入连接：http://
                    banner.setOutStation(imageItem.getOutStation());
                }
                banner.setPicPcPath(imageItem.getPcPath());
                banner.setPicMbPath(imageItem.getMbPath());
                resultList.add(banner);
            }
        } else {
            resultList = oprAdvMapper.queryAdvBannerDtoList(bannerDto);
        }
        return resultList;
    }

    public List<OprAdv> coupletList() {
        List<OprAdv> resultList = oprAdvMapper.coupletList();
        return resultList;
    }
}
