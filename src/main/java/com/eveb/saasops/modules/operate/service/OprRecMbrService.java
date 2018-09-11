package com.eveb.saasops.modules.operate.service;

import static com.eveb.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;

import static com.eveb.saasops.common.utils.DateUtil.getCurrentDate;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.eveb.saasops.common.constants.OprConstants;
import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.modules.agent.entity.AgentAccount;
import com.eveb.saasops.modules.agent.mapper.AgentMapper;
import com.eveb.saasops.modules.base.service.BaseService;
import com.eveb.saasops.modules.log.dao.OperationLogMapper;
import com.eveb.saasops.modules.member.dao.MbrAccountMapper;
import com.eveb.saasops.modules.member.entity.MbrAccount;
import com.eveb.saasops.modules.member.mapper.MbrMapper;
import com.eveb.saasops.modules.operate.dao.OprMesMapper;
import com.eveb.saasops.modules.operate.dao.OprRecMapper;
import com.eveb.saasops.modules.operate.dao.OprRecMbrMapper;
import com.eveb.saasops.modules.operate.dto.AgyAccDto;
import com.eveb.saasops.modules.operate.entity.OprMes;
import com.eveb.saasops.modules.operate.entity.OprRec;
import com.eveb.saasops.modules.operate.entity.OprRecMbr;
import com.eveb.saasops.modules.sys.entity.SysUserEntity;
import com.github.pagehelper.PageHelper;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
public class OprRecMbrService extends BaseService<OprRecMbrMapper, OprRecMbr> {

    @Autowired
    private OprRecMbrMapper oprRecMbrMapper;
    @Autowired
    private OprRecMapper oprRecMapper;
    @Autowired
    private OprMesMapper oprMesMapper;
    @Value("${opr.plot.excel.path}")
    private String oprExcelPath;
    @Autowired
    OperationLogMapper operationLogMapper;
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private MbrAccountMapper MbrAccountMapper;
    @Autowired
    private AgentMapper agentMapper;

    // 获取用户未读站内信数量
    public Integer getUnreadMsgCount(String loginName) {
        // 获取当前用户
        // SysUserEntity user = (SysUserEntity)
        // SecurityUtils.getSubject().getPrincipal();
        OprRecMbr record = new OprRecMbr();
        record.setMbrName(loginName);
        record.setIsRead(OprConstants.UN_READ);
        Integer count = oprRecMbrMapper.queryMbrMesList(record).size();
        return count;
    }

	/*
	public void exportExcel(OprRecMbr oprRecMbr, HttpServletResponse response) {
		String fileName = "站内信列表" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		List<Map<String, Object>> list = Lists.newLinkedList();

		Set<OprRecMbr> ormSet = new HashSet<>();
		List<OprRecMbr> oprRecMbrList = oprRecMbrMapper.queryOrmList(oprRecMbr);
		List<OprRecMbr> ormList = new ArrayList<>();
		for (OprRecMbr orm : oprRecMbrList) {
			if (ormSet.add(orm)) {
				if (orm.getMbrId() == null) {
					orm.setIsRead(OprConstants.UN_READ);
				}
				if (orm.getGroupId() != null) {
					orm.setRecType(OprConstants.OPR_MBR);
				} else {
					orm.setRecType(OprConstants.OPR_AGY);
				}
				ormList.add(orm);
			}
		}
		ormList.stream().forEach(cs -> {
			Map<String, Object> param = new HashMap<>();
			param.put("mbrName", cs.getMbrName());
			param.put("title", cs.getTitle());
			param.put("context", cs.getContext());
			param.put("recType", cs.getRecType() == OprConstants.OPR_MBR ? "会员" : "代理");
			param.put("sendTime", cs.getCreateTime());
			param.put("sender", cs.getSender());
			param.put("isRead", cs.getIsRead() == OprConstants.UN_READ ? "未读" : "已读");
			param.put("readDate", cs.getReadDate());
			list.add(param);
		});
		Workbook workbook = ExcelUtil.commonExcelExportList("mapList", oprExcelPath, list);
		try {
			ExcelUtil.writeExcel(response, workbook, fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

    @Transactional(propagation = Propagation.REQUIRED)
    public void modifyOrm(List<OprRecMbr> ormList) {
        if (ormList != null && ormList.size() != 0) {
            for (int s = 0; s < ormList.size(); s++) {
                OprRecMbr orm = ormList.get(s);
                deleteRecord(orm);
            }
        }
    }


    // 批量设置已读
    @Transactional(propagation = Propagation.REQUIRED)
    public void readBatch(List<OprRecMbr> ormList) {
        if (ormList != null) {
            for (int s = 0; s < ormList.size(); s++) {
                OprRecMbr orm = ormList.get(s);
                OprRecMbr record = new OprRecMbr();
                record.setMsgId(orm.getMsgId());
                record.setMbrId(orm.getMbrId());
//TODO	
                OprRecMbr opr = oprRecMbrMapper.selectOne(record);
                record.setIsRead(OprConstants.READED);
                record.setReadDate(getCurrentDate(FORMAT_18_DATE_TIME));
                if (null == opr) {
                    oprRecMbrMapper.insertSelective(record);
                } else {
                    record.setId(opr.getId());
                    oprRecMbrMapper.updateByPrimaryKeySelective(record);
                }

            }
        }
    }

    // 单条阅读
    @Transactional(propagation = Propagation.REQUIRED)
    public void readMsg(OprRecMbr oprRecMbr) {
        if (oprRecMbr != null) {
            OprRecMbr record = new OprRecMbr();
            record.setMsgId(oprRecMbr.getMsgId());

            record.setMbrId(oprRecMbr.getMbrId());
//TODO			
            OprRecMbr opr = oprRecMbrMapper.selectOne(record);
            record.setIsRead(OprConstants.READED);
            record.setReadDate(getCurrentDate(FORMAT_18_DATE_TIME));
            if (null == opr) {
                oprRecMbrMapper.insertSelective(record);
            }
        }
    }

    public Object queryMbrList(MbrAccount mbrAccount, String string) {

        if (mbrAccount.getGroupIds() == null || mbrAccount.getGroupIds().size() == 0) {
            mbrAccount.setGroupIds(null);
        }
        if (mbrAccount.getCagencyIds() == null || mbrAccount.getCagencyIds().size() == 0) {
            mbrAccount.setCagencyIds(null);
        }
        if (mbrAccount.getTagencyIds() == null || mbrAccount.getTagencyIds().size() == 0) {
            mbrAccount.setTagencyIds(null);
        }
        List<MbrAccount> mbrList = mbrMapper.queryMbrList(mbrAccount);
        return BeanUtil.toPagedResult(mbrList);
    }

    public Object queryAgentList(AgyAccDto agyAccDto, String auth) {
        List<AgentAccount> list = new ArrayList<>();

        if (agyAccDto.getIsAllGen() != null && agyAccDto.getIsAllGen() == true) {
            AgentAccount record = new AgentAccount();
            record.setParentId(0);
            // List<AgentAccount> genAgyList = agentAccountMapper.select(record);
            List<AgentAccount> genAgyList = agentMapper.selectGenAgy();
            return BeanUtil.toPagedResult(genAgyList);
        }

        if (agyAccDto.getIsAllAgt() != null && agyAccDto.getIsAllAgt() == true) {
            if (null != agyAccDto.getGenIds()) {
                List<AgentAccount> genAgyList = new ArrayList<>();
                for (Integer genId : agyAccDto.getGenIds()) {
                    AgentAccount agy = agentMapper.selectByPrimaryKey(genId);
                    genAgyList.add(agy);
                }
                return BeanUtil.toPagedResult(genAgyList);
            }
        } else if (agyAccDto.getIsAllAgt() != null && agyAccDto.getIsAllAgt() == false) {
            if (null != agyAccDto.getAgtIds()) {
                List<AgentAccount> agyList = new ArrayList<>();
                for (Integer agyId : agyAccDto.getAgtIds()) {
                    AgentAccount agy = agentMapper.selectByPrimaryKey(agyId);
                    agyList.add(agy);
                }
                return BeanUtil.toPagedResult(agyList);
            }
        }
        if (null != agyAccDto.getLoginName()) {
            list = agentMapper.queryAgentList(agyAccDto);
            BeanUtil.toPagedResult(list);
        } else {
            list = agentMapper.queryAgentList(agyAccDto);
            BeanUtil.toPagedResult(list);
        }
        return BeanUtil.toPagedResult(list);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void saveOprRecMbr(OprRecMbr oprRecMbr) {
        // 获取当前用户
        SysUserEntity user = (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
        // 存储站内信内容
        OprMes oprMes = new OprMes();
        oprMes.setSender(user.getUsername());
        oprMes.setTitle(oprRecMbr.getTitle());
        oprMes.setContext(oprRecMbr.getContext());
        oprMes.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        oprMesMapper.insert(oprMes);
        OprMes om = oprMesMapper.selectOne(oprMes);
        // 站内信发送会员和代理
        send(oprRecMbr, om);
    }

    /**
     * 站内信发送
     *
     * @param oprRecMbr
     * @param sender
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void sendInMail(OprRecMbr oprRecMbr, String sender) {
        // 存储站内信内容
        OprMes oprMes = new OprMes();
        oprMes.setSender(sender);
        oprMes.setTitle(oprRecMbr.getTitle());
        oprMes.setContext(oprRecMbr.getContext());
        oprMes.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        oprMesMapper.insert(oprMes);
        OprMes om = oprMesMapper.selectOne(oprMes);
        send(oprRecMbr, om);
    }

    private void send(OprRecMbr oprRecMbr, OprMes om) {
        // 站内信发送会员
        if (null != oprRecMbr.getMbrList()) {
            List<OprRec> orList = new ArrayList<OprRec>();
            for (MbrAccount acc : oprRecMbr.getMbrList()) {
                if (null == acc.getId()) {
                    MbrAccount record = new MbrAccount();
                    record.setLoginName(acc.getLoginName());
                    List<MbrAccount> mList = MbrAccountMapper.select(record);
                    if (null != mList) {
                        MbrAccount mbrAccount = mList.get(0);
                        insertMesToMbrs(om, mbrAccount, orList);
                    }
                } else {
                    insertMesToMbrs(om, acc, orList);
                }
            }
            oprRecMapper.insertList(orList);
        }
        // 站内信发送代理
        if (null != oprRecMbr.getAgyList()) {
            List<OprRec> orList = new ArrayList<OprRec>();
            for (AgentAccount agent : oprRecMbr.getAgyList()) {
                if (null == agent.getId()) {
                    AgyAccDto agyAccDto = new AgyAccDto();
                    agyAccDto.setLoginName(agent.getAgyAccount());
                    List<AgentAccount> aList = agentMapper.queryAgentList(agyAccDto);
                    if (null != aList) {
                        AgentAccount aa = aList.get(0);
                        if (aa.getAgyAccount().equals(agent.getAgyAccount())) {
                            insertMesToAgents(om, aa, orList);
                        }
                    }
                } else {
                    insertMesToAgents(om, agent, orList);
                }
            }
            oprRecMapper.insertList(orList);
        }
    }


    private void insertMesToAgents(OprMes om, AgentAccount agent, List<OprRec> orList) {
        OprRec oprRec = new OprRec();
        oprRec.setMsgId(om.getId());
        if (agent.getParentId() == 0) {
            oprRec.setGenAgtId(agent.getId());
        } else {
            oprRec.setAgtId(agent.getId());
        }
        orList.add(oprRec);
    }

    private void insertMesToMbrs(OprMes om, MbrAccount mbrAccount, List<OprRec> orList) {
        OprRec oprRec = new OprRec();
        oprRec.setMsgId(om.getId());
        oprRec.setMbrId(mbrAccount.getId());
        orList.add(oprRec);
    }

    /*public PageUtils queryListPage(OprRecMbr oprRecMbr, Integer pageNo, Integer pageSize, String orderBy, String auth) {
        BaseAuth baseAuth = getRowAuth();
        String groups = baseAuth.getGroupIds();
        String agyaccounts = baseAuth.getAgyAccountIds();
        BaseAuth baseAuthNew = new BaseAuth();

        if(oprRecMbr.getMbrTypes() == null){
            baseAuthNew = baseAuth;
        }
        if (oprRecMbr.getMbrTypes().toString().contains("1")) {
            baseAuthNew.setGroupIds(groups);
        }
        if (oprRecMbr.getMbrTypes().toString().contains("2")) {
            baseAuthNew.setAgyAccountIds(agyaccounts);
        }
        if (!StringUtils.isEmpty(baseAuthNew))
            oprRecMbr.setBaseAuth(baseAuthNew);
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        List<OprRecMbr> ormList;

         if (oprRecMbr.getMbrType().equals(OprConstants.OPR_AGY)) {
            setIsReadsNorY(oprRecMbr);
            oprRecMbr.setBaseAuth(baseAuthNew);
            ormList = oprRecMbrMapper.queryAgyMesList(oprRecMbr);
        } else if (oprRecMbr.getMbrType().equals(OprConstants.OPR_MBR)) {
            oprRecMbr.setBaseAuth(baseAuthNew);
            setIsReadsNorY(oprRecMbr);
            ormList = oprRecMbrMapper.queryMbrMesList(oprRecMbr);
        } else {
            setIsReadsNorY(oprRecMbr);
            oprRecMbr.setBaseAuth(baseAuthNew);
            ormList = oprRecMbrMapper.queryAllList(oprRecMbr);
        }
        PageUtils p = BeanUtil.toPagedResult(ormList);
        ormList.sort((h1, h2) -> h2.getCreateTime().compareTo(h1.getCreateTime()));
        Date now = new Date();
        StationSet stationSet = ssService.queryStationSet();
        List<OprRecMbr> bList = new ArrayList<>();
        for (OprRecMbr orm : ormList) {
            if (orm.getReadDate() != null && orm.getReadDate() != "") {
                Integer autoDeleteDays = stationSet.getAutoDeleteDays();
                String readDate = orm.getReadDate();
                readDate = readDate.substring(0, readDate.indexOf("."));
                Date rDate = DateUtil.parse(readDate, DateUtil.FORMAT_18_DATE_TIME);
                Date expireDate = DateUtil.getDateAfter(rDate, autoDeleteDays);
                if (now.getTime() > expireDate.getTime()) {
                    deleteRecord(orm);
                } else {
                    bList.add(orm);
                }
            } else {
                bList.add(orm);
            }
        }
        p.setList(bList);
        return p;
    }*/

    public PageUtils queryListPage(OprRecMbr oprRecMbr, Integer pageNo, Integer pageSize, String orderBy, String auth) {
        List<OprRecMbr> ormList = new LinkedList<>();
        PageHelper.startPage(pageNo,pageSize);
        if(oprRecMbr.getMbrTypes() != null && oprRecMbr.getMbrTypes().size() !=0) {
            String types = oprRecMbr.getMbrTypes().toString().replace("[","").replace("]","");
                switch (types){
                    case "1" :
                        ormList=oprRecMbrMapper.queryMbrMesList(oprRecMbr);
                        break;
                    case "2" :
                        ormList.addAll(oprRecMbrMapper.queryAgyMesList(oprRecMbr));
                        break;
                    default :
                        ormList.addAll(oprRecMbrMapper.queryAllList(oprRecMbr));
                }

        }else {
            ormList = oprRecMbrMapper.queryAllList(oprRecMbr);
        }
       return BeanUtil.toPagedResult(ormList);
    }

    private void setIsReadsNorY(OprRecMbr oprRecMbr) {
        oprRecMbr.setIsReadsN(oprRecMbr.getIsReads().stream().filter(x -> x == 0).collect(Collectors.toList()));
        System.out.println(oprRecMbr.getIsReadsN());
        oprRecMbr.setIsReadsY(oprRecMbr.getIsReads().stream().filter(x -> x != 0).collect(Collectors.toList()));
        System.out.println(oprRecMbr.getIsReadsY());
    }

    public void deleteRecord(OprRecMbr orm) {
        OprRecMbr record = new OprRecMbr();
        record.setMsgId(orm.getMsgId());
//TODO  
        record.setMbrId(orm.getMbrId());
        OprRecMbr opr = oprRecMbrMapper.selectOne(record);
        record.setIsRead(OprConstants.DELETED);
        record.setReadDate(getCurrentDate(FORMAT_18_DATE_TIME));
        if (null == opr) {
            oprRecMbrMapper.insertSelective(record);
        } else {
            record.setId(opr.getId());
            oprRecMbrMapper.updateByPrimaryKeySelective(record);
        }
    }

    /**
     * 删除站内过期信息
     */
    public void deleteOprRecMbr(){
        oprRecMbrMapper.deleteOprRecMbr();
    }
}
