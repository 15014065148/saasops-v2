package com.eveb.saasops.modules.member.service;

import static com.eveb.saasops.common.utils.DateUtil.FORMAT_10_DATE;
import static com.eveb.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.eveb.saasops.common.utils.DateUtil.getCurrentDate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import com.eveb.saasops.config.ThreadLocalCache;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.eveb.saasops.api.config.ApiConfig;
import com.eveb.saasops.api.constants.ApiConstants;
import com.eveb.saasops.api.modules.user.dto.PwdDto;
import com.eveb.saasops.api.modules.user.service.ApiUserService;
import com.eveb.saasops.common.constants.ColumnAuthConstants;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.constants.Constants.Available;
import com.eveb.saasops.common.constants.GroupByConstants;
import com.eveb.saasops.common.exception.R200Exception;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.common.utils.Collections3;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.DateUtil;
import com.eveb.saasops.common.utils.IpUtils;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.common.utils.SnowFlake;
import com.eveb.saasops.common.utils.StringUtil;
import com.eveb.saasops.common.validator.Assert;
import com.eveb.saasops.common.validator.ValidRegUtils;
import com.eveb.saasops.listener.BizEvent;
import com.eveb.saasops.listener.BizEventType;
import com.eveb.saasops.modules.agent.dao.AgentAccountMapper;
import com.eveb.saasops.modules.agent.entity.AgentAccount;
import com.eveb.saasops.modules.base.service.BaseService;
import com.eveb.saasops.modules.fund.entity.AccWithdraw;
import com.eveb.saasops.modules.fund.entity.FundDeposit;
import com.eveb.saasops.modules.fund.service.FundDepositService;
import com.eveb.saasops.modules.fund.service.FundReportService;
import com.eveb.saasops.modules.fund.service.FundWithdrawService;
import com.eveb.saasops.modules.log.dao.LogMbrregisterMapper;
import com.eveb.saasops.modules.log.entity.LogMbrRegister;
import com.eveb.saasops.modules.log.entity.LogMbrRegister.RegIpValue;
import com.eveb.saasops.modules.log.service.LogMbrloginService;
import com.eveb.saasops.modules.member.dao.MbrAccountMapper;
import com.eveb.saasops.modules.member.dao.MbrWalletMapper;
import com.eveb.saasops.modules.member.dao.TempAccountMapper;
import com.eveb.saasops.modules.member.dto.ItemDto;
import com.eveb.saasops.modules.member.dto.TotalDto;
import com.eveb.saasops.modules.member.entity.MbrAccount;
import com.eveb.saasops.modules.member.entity.MbrAccountOnline;
import com.eveb.saasops.modules.member.entity.MbrBankcard;
import com.eveb.saasops.modules.member.entity.MbrBillDetail;
import com.eveb.saasops.modules.member.entity.MbrBillManage;
import com.eveb.saasops.modules.member.entity.MbrFundTotal;
import com.eveb.saasops.modules.member.entity.MbrWallet;
import com.eveb.saasops.modules.member.entity.TempAccountId;
import com.eveb.saasops.modules.member.mapper.MbrMapper;
import com.eveb.saasops.modules.operate.service.OprActActivityService;
import com.eveb.saasops.modules.sys.dto.ColumnAuthTreeDto;
import com.eveb.saasops.modules.sys.service.ColumnAuthProviderService;
import com.eveb.saasops.modules.system.systemsetting.entity.SysSetting;
import com.github.pagehelper.PageHelper;

@Service
public class MbrAccountService extends BaseService<MbrAccountMapper, MbrAccount> {

    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private MbrWalletMapper mbrWalletMapper;
    @Autowired
    private LogMbrregisterMapper logMbrregisterMapper;
    @Autowired
    private ApiUserService apiUserService;
    @Autowired
    private MbrBankcardService mbrBankcardService;
    @Autowired
    private IpService ipService;
    @Autowired
    private ApiConfig apiConfig;
    @Autowired
    private LogMbrloginService logLoginService;
    @Autowired
    private TempAccountMapper tempAccountMapper;
    @Autowired
    private OprActActivityService actActivityService;
    @Autowired
    private FundWithdrawService withdrawService;
    @Autowired
    private FundDepositService depositService;
    @Autowired
    private FundReportService reportService;
    @Autowired
    private MbrAccountLogService accountLogService;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Value("${api.regDeaultCagencyId}")
    private int cagencyId;
    @Autowired
    private ColumnAuthProviderService columnAuthProviderService;
    @Autowired
    private LogMbrloginService logMbrloginService;


    /**
     * @param mbrAccount
     * @param pageNo
     * @param pageSize
     * @param orderBy
     * @return
     */
    @Transactional
    public PageUtils queryListPage(MbrAccount mbrAccount, Integer pageNo, Integer pageSize, String orderBy, Integer roleId) {
        List<ColumnAuthTreeDto> menuList = columnAuthProviderService.getRoleColumnAuth(roleId, ColumnAuthConstants.MEMBER_LIST_MENU_ID, ColumnAuthConstants.COLUMN_MENU_TYPE_FOUR);
        Assert.isNullOrEmpty(menuList, "无权限，请联系管理员");

        Set<String> columnSets = new HashSet<String>();
        for (ColumnAuthTreeDto columnAuthTreeDto : menuList) {
            columnSets.add(columnAuthTreeDto.getColumnName());
            if ("cagencyId".equals(columnAuthTreeDto.getColumnName())) {
                columnSets.add("agyAccount");
            }
            if ("groupId".equals(columnAuthTreeDto.getColumnName())) {
                columnSets.add("groupName");
            }
        }
        columnSets.add("id");
        mbrAccount.setColumnSets(columnSets);

        mbrAccount.setBaseAuth(getRowAuth());
        PageHelper.startPage(pageNo, pageSize);
        orderBy = GroupByConstants.getOrderBy(GroupByConstants.accountMod, orderBy);
        PageHelper.orderBy(orderBy);

        List<MbrAccount> list = mbrMapper.findAccountList(mbrAccount);
        List<TempAccountId> accounts = new ArrayList<TempAccountId>();
        Long uuid = new SnowFlake().nextId();
        list.forEach(e -> {
            TempAccountId tempAccountId = new TempAccountId();
            tempAccountId.setAccountId(e.getId());
            tempAccountId.setAccUuid(uuid);
            accounts.add(tempAccountId);
        });
        if (list.size() > 0) {
            tempAccountMapper.insertList(accounts);
            List<TotalDto> listTdeposit = mbrMapper.totalMem(uuid);
            mbrMapper.delTotalMem(uuid);
            listTdeposit.forEach(e -> {
                list.forEach(acce -> {
                    if (acce.getId().equals(e.getAccountId())) {
                        acce.setTotalDeposit(e.getTotalDeposit());
                        acce.setTotalWithdrawal(e.getTotalWithdrawal());
                        acce.setTotalBalance(e.getTotalBalance());
                    }
                });
            });
        }
        PageUtils p = BeanUtil.toPagedResult(list);
        return p;
    }


    public List<ColumnAuthTreeDto> querySeachCondition(Integer roleId, Long typeId) {
        //获取用户列权限   id为4查询搜索栏显示字段，为5查询列表显示字段。
        List<ColumnAuthTreeDto> resultList = columnAuthProviderService.getRoleColumnAuth(roleId, ColumnAuthConstants.MEMBER_LIST_MENU_ID, typeId);
        return resultList;
    }


    public List<ColumnAuthTreeDto> columnFrameList(Integer roleId) {
        List<ColumnAuthTreeDto> allList = columnAuthProviderService.getAllColumnAuth(ColumnAuthConstants.MEMBER_LIST_MENU_ID, ColumnAuthConstants.COLUMN_MENU_TYPE_THREE);
        Set<Long> set = new HashSet<>();
        getMenuChildIds(set, allList);
        ColumnAuthTreeDto paramDto = new ColumnAuthTreeDto();
        paramDto.setParamList(set);
        paramDto.setMenuId(ColumnAuthConstants.MEMBER_LIST_MENU_ID);
        paramDto.setRoleId(roleId);
        List<ColumnAuthTreeDto> resultList = columnAuthProviderService.getRoleAuth(paramDto);
        return resultList;
    }

    private void getMenuChildIds(Set<Long> set, List<ColumnAuthTreeDto> list) {
        if (Collections3.isNotEmpty(list)) {
            for (ColumnAuthTreeDto columnTreeDto : list) {
                set.add(columnTreeDto.getMenuId());
                getMenuChildIds(set, columnTreeDto.getChildList());
            }
        }
    }

    // 根据用户Id 查询
    public MbrAccount getAccountInfo(Integer userId) {
        MbrAccount info = new MbrAccount();
        info.setId(userId);
        return queryObjectCond(info);
    }

    // 根据用户名 查询
    public MbrAccount getAccountInfo(String loginName) {
        MbrAccount info = new MbrAccount();
        info.setLoginName(loginName);
        return queryObjectCond(info);

    }

    public int getAccountNum(String loginName) {
        MbrAccount info = new MbrAccount();
        info.setLoginName(loginName);
        return selectCount(info);
    }

    /**
     * 返回在线会员
     *
     * @param mbrAccountOnline
     * @param pageNo
     * @param pageSize
     * @param orderBy
     * @return
     */
    public PageUtils queryListOnlinePage(MbrAccountOnline mbrAccountOnline, Integer pageNo, Integer pageSize,
                                         String orderBy) {
        mbrAccountOnline.setBaseAuth(getRowAuth());
        PageHelper.startPage(pageNo, pageSize);
        orderBy = GroupByConstants.getOrderBy(GroupByConstants.onlineAccountMod, orderBy);
        PageHelper.orderBy(orderBy);
        List<MbrAccountOnline> list = mbrMapper.findAccountOnlineList(mbrAccountOnline);
        list.forEach(e1 -> {
            e1.setOnlineTimeStr(StringUtil.formatOnlineTime(e1.getOnlineTime()));
        });
        return BeanUtil.toPagedResult(list);
    }

    /**
     * 后端注册
     *
     * @param mbrAccount
     * @param request
     */
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public boolean adminSave(MbrAccount mbrAccount, HttpServletRequest request) {
        AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(cagencyId);
        mbrAccount.setCagencyId(cagencyId);
        mbrAccount.setTagencyId(agentAccount.getParentId());
        return save(mbrAccount, request, RegIpValue.adminManage);
    }


    @Transactional
    public MbrAccount webSave(MbrAccount mbrAccount) {
        if (!StringUtils.isEmpty(mbrAccount.getSpreadCode())) {
            AgentAccount agent = new AgentAccount();
            agent.setSpreadCode(mbrAccount.getSpreadCode());
            AgentAccount agentAccount = agentAccountMapper.selectOne(agent);
            if (Objects.nonNull(agentAccount)) {
                mbrAccount.setCagencyId(agentAccount.getId());
                mbrAccount.setTagencyId(agentAccount.getParentId());
            }
        }
        mbrAccount.setIsOnline(Available.enable);
        String salt = RandomStringUtils.randomAlphanumeric(20);
        mbrAccount.setLoginPwd(new Sha256Hash(mbrAccount.getLoginPwd(), salt).toHex());
        mbrAccount.setSalt(salt);
        mbrAccount.setRegisterTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        mbrAccount.setLoginTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        super.save(mbrAccount);
        mbrWalletMapper.insert(getMbrWallet(mbrAccount));
        return mbrAccount;
    }

    public void asyncLogoInfo(MbrAccount mbrAccount, HttpServletRequest request, String siteCode, Boolean isInsert) {
        CompletableFuture.runAsync(() -> {
            ThreadLocalCache.setSiteCodeAsny(siteCode);
            if (Boolean.TRUE.equals(isInsert)) {
                logMbrregisterMapper.insert(getlogRegister(mbrAccount, request, mbrAccount.getLoginSource()));
            }
            //设置之前未有登出的会员登出时间
            logMbrloginService.setLoginOffTime(mbrAccount.getLoginName());
            logMbrloginService.saveLoginLog(mbrAccount, request);
            setMemLineState(mbrAccount.getId(), Available.enable);
        });
    }

    public void setMemLineState(Integer userId, Byte lineState) {
        MbrAccount account = new MbrAccount();
        account.setId(userId);
        account.setIsOnline(lineState);
        if (Available.enable == lineState)
            account.setLoginTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        update(account);
    }

    /**
     * @param mbrAccount
     * @param request
     * @return
     */
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public boolean save(MbrAccount mbrAccount, HttpServletRequest request, Byte source) {
        // 保证区域代理与总代上下级关系正常
        String salt = RandomStringUtils.randomAlphanumeric(20);
        mbrAccount.setLoginPwd(new Sha256Hash(mbrAccount.getLoginPwd(), salt).toHex());
        mbrAccount.setSalt(salt);
        mbrAccount.setRegisterTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        mbrAccount.setLoginTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        if (super.save(mbrAccount) > 0) {
            mbrWalletMapper.insert(getMbrWallet(mbrAccount));
            logMbrregisterMapper.insert(getlogRegister(mbrAccount, request, source));
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public void updateAccountRest(MbrAccount mbrAccount, BizEvent bizEvent, String userName) {
        MbrAccount account = getAccountInfo(mbrAccount.getId());
        if (account.getTagencyId() == apiConfig.getTestAgentId()
                && mbrAccount.getTagencyId() != apiConfig.getTestAgentId()) {
            throw new RRException("测试组代理下的会员不能移到其它代理下!");
        }
        if (account.getAvailable() != mbrAccount.getAvailable()) {
            if (mbrAccount.getAvailable().compareTo(new Byte("0")) == 0) {
                bizEvent.setEventType(BizEventType.MEMBER_ACCOUNT_FREEZE);
            }
            if (mbrAccount.getAvailable().compareTo(new Byte("2")) == 0) {
                bizEvent.setEventType(BizEventType.MEMBER_WITHDRAWAL_REFUSE);
            }
        }
        accountLogService.updateAccountRest(account, mbrAccount, userName);
        // 保证区域代理与总代上下级关系正常
        AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(mbrAccount.getCagencyId());
        account.setCagencyId(mbrAccount.getCagencyId());
        account.setTagencyId(agentAccount.getParentId());
        account.setGroupId(mbrAccount.getGroupId());
        account.setAvailable(mbrAccount.getAvailable());
        accountMapper.updateByPrimaryKeySelective(account);
        if (mbrAccount.getAvailable() == Constants.EVNumber.zero) {
            kickLine(mbrAccount.getId());
        }
    }

    public LinkedHashMap<String, Object> webUserInfo(String loginName) {
        LinkedHashMap<String, Object> userInfo = new LinkedHashMap<String, Object>();
        MbrWallet wallet = new MbrWallet();
        MbrAccount mbrAccount = new MbrAccount();
        mbrAccount.setLoginName(loginName);
        wallet.setLoginName(loginName);
        wallet = mbrWalletMapper.selectOne(wallet);
        mbrAccount = queryObjectCond(mbrAccount);
        //计算完整度realName,mobile,email,weChat,securePwd,qq,bankCard
        Byte userInfoMeasure = getUserInfoMeasure(mbrAccount);
        userInfo.put("userId", mbrAccount.getId());
        userInfo.put("loginName", mbrAccount.getLoginName());
        userInfo.put("realName", mbrAccount.getRealName());
        userInfo.put("registerTime", mbrAccount.getRegisterTime());
        userInfo.put("balance", wallet.getBalance());
        userInfo.put("available", mbrAccount.getAvailable());
        userInfo.put("mobile", mbrAccount.getMobile());
        userInfo.put("email", mbrAccount.getEmail());
        userInfo.put("weChat", mbrAccount.getWeChat());
        userInfo.put("loginPwd", mbrAccount.getLoginPwd());
        userInfo.put("securePwd", mbrAccount.getSecurePwd());
        userInfo.put("qq", mbrAccount.getQq());
        userInfo.put("userInfoMeasure", userInfoMeasure);
        if (StringUtils.isEmpty(mbrAccount.getFreeWalletSwitch()))
            mbrAccount.setFreeWalletSwitch(Constants.EVNumber.one);
        userInfo.put("freeWalletSwitch", mbrAccount.getFreeWalletSwitch());
        return userInfo;
    }

    public LinkedHashMap<String, Object> webFindPwdUserInfo(String loginName) {
        LinkedHashMap<String, Object> userInfo = new LinkedHashMap<String, Object>();
        MbrAccount mbrAccount = new MbrAccount();
        mbrAccount.setLoginName(loginName);
        mbrAccount = queryObjectCond(mbrAccount);
        userInfo.put("loginName", mbrAccount.getLoginName());
        userInfo.put("email", StringUtil.mail(mbrAccount.getEmail()));
        userInfo.put("EmailValidateStatus", mbrAccount.getIsVerifyEmail());
        userInfo.put("phone", StringUtil.phone(mbrAccount.getMobile()));
        userInfo.put("PhoneValidateStatus", mbrAccount.getIsVerifyMoblie());
        return userInfo;
    }

    public void kickLine(Integer accountId) {
        String siteCode = CommonUtil.getSiteCode();
        MbrAccount mbrAccount = getAccountInfo(accountId);
        apiUserService.rmLoginTokenCache(siteCode, mbrAccount.getLoginName());
        updateOffline(mbrAccount.getLoginName());
    }

    public MbrAccount viewAccount(Integer roleId, Long userId, Integer id) {
        List<ColumnAuthTreeDto> menuList = columnAuthProviderService.getRoleColumnAuth(roleId, ColumnAuthConstants.MEMBER_DATA_MENU_ID, ColumnAuthConstants.COLUMN_MENU_TYPE_THREE);
        Assert.isNullOrEmpty(menuList, "无权限，请联系管理员");

        MbrAccount paramAccount = new MbrAccount();
        Set<String> columnSets = new HashSet<String>();
        for (ColumnAuthTreeDto columnAuthTreeDto : menuList) {
            if (null != columnAuthTreeDto.getColumnName() && !"".equals(columnAuthTreeDto.getColumnName())) {
                columnSets.add(columnAuthTreeDto.getColumnName());
            }
        }
        columnSets.add("id");
        paramAccount.setColumnSets(columnSets);
        paramAccount.setId(id);

        MbrAccount mbrAccount = mbrMapper.viewAccount(paramAccount);
        String perms = mbrMapper.findAccountContact(userId, Constants.ACCOUNT_CONTACT);
        if (null != perms) {
            if (!perms.contains("email") && org.apache.commons.lang.StringUtils.isNotEmpty(mbrAccount.getEmail())) {
                mbrAccount.setEmail(StringUtil.mail(mbrAccount.getEmail()));
            }
            if (!perms.contains("mobile") && org.apache.commons.lang.StringUtils.isNotEmpty(mbrAccount.getMobile())) {
                mbrAccount.setMobile(StringUtil.phone(mbrAccount.getMobile()));
            }
            if (!perms.contains("qq") && org.apache.commons.lang.StringUtils.isNotEmpty(mbrAccount.getQq())) {
                mbrAccount.setQq(StringUtil.QQ(mbrAccount.getQq()));
            }
            if (!perms.contains("wechat") && org.apache.commons.lang.StringUtils.isNotEmpty(mbrAccount.getWeChat())) {
                mbrAccount.setWeChat(StringUtil.QQ(mbrAccount.getWeChat()));
            }
        } else {
            mbrAccount.setEmail(StringUtil.mail(mbrAccount.getEmail()));
            mbrAccount.setMobile(StringUtil.phone(mbrAccount.getMobile()));
            mbrAccount.setQq(StringUtil.QQ(mbrAccount.getQq()));
            mbrAccount.setWeChat(StringUtil.QQ(mbrAccount.getWeChat()));
        }

        MbrAccount mbrAccountWallet = viewOtherAccount(roleId, userId, id);//查询余额
        if (mbrAccountWallet != null) {
            mbrAccount.setBalance(mbrAccountWallet.getTotalBalance());
        }

        return mbrAccount;
    }

    public MbrAccount viewOtherAccount(Integer roleId, Long userId, Integer id) {
        List<ColumnAuthTreeDto> menuList = columnAuthProviderService.getRoleColumnAuth(roleId, ColumnAuthConstants.MEMBER_OTHER_DATA_MENU_ID, ColumnAuthConstants.COLUMN_MENU_TYPE_THREE);
        Assert.isNullOrEmpty(menuList, "无权限，请联系管理员");

        MbrAccount paramAccount = new MbrAccount();
        Set<String> columnSets = new HashSet<String>();
        for (ColumnAuthTreeDto columnAuthTreeDto : menuList) {
            if (null != columnAuthTreeDto.getColumnName() && !"".equals(columnAuthTreeDto.getColumnName())) {
                columnSets.add(columnAuthTreeDto.getColumnName());
            }
        }
        columnSets.add("id");
        paramAccount.setColumnSets(columnSets);
        paramAccount.setId(id);

        MbrAccount mbrAccount = mbrMapper.viewOtherAccount(paramAccount);
        String perms = mbrMapper.findAccountContact(userId, Constants.ACCOUNT_CONTACT);
        if (null != perms) {
            if (!perms.contains("email") && org.apache.commons.lang.StringUtils.isNotEmpty(mbrAccount.getEmail())) {
                mbrAccount.setEmail(StringUtil.mail(mbrAccount.getEmail()));
            }
            if (!perms.contains("mobile") && org.apache.commons.lang.StringUtils.isNotEmpty(mbrAccount.getMobile())) {
                mbrAccount.setMobile(StringUtil.phone(mbrAccount.getMobile()));
            }
            if (!perms.contains("qq") && org.apache.commons.lang.StringUtils.isNotEmpty(mbrAccount.getQq())) {
                mbrAccount.setQq(StringUtil.QQ(mbrAccount.getQq()));
            }
            if (!perms.contains("wechat") && org.apache.commons.lang.StringUtils.isNotEmpty(mbrAccount.getWeChat())) {
                mbrAccount.setWeChat(StringUtil.QQ(mbrAccount.getWeChat()));
            }
        } else {
            mbrAccount.setEmail(StringUtil.mail(mbrAccount.getEmail()));
            mbrAccount.setMobile(StringUtil.phone(mbrAccount.getMobile()));
            mbrAccount.setQq(StringUtil.QQ(mbrAccount.getQq()));
            mbrAccount.setWeChat(StringUtil.QQ(mbrAccount.getWeChat()));
        }
        return mbrAccount;
    }

    public int updateGroupBatch(Integer[] id, Integer groupId) {
        return mbrMapper.updateGroupBatch(id, groupId);
    }

    public MbrAccount updateAvailable(Integer id, Byte available, String userName) {
        MbrAccount mbrAccount = new MbrAccount();
        mbrAccount.setId(id);
        mbrAccount.setAvailable(available);
        accountLogService.updateAccountAvailable(id, mbrAccount, userName);
        super.update(mbrAccount);
        if (available == Constants.EVNumber.zero) {
            kickLine(mbrAccount.getId());
        }
        return getAccountInfo(id);
    }

    @CachePut(cacheNames = ApiConstants.REDIS_USER_CACHE_KEY, key = "#siteCode+''+#id")
    public MbrAccount updateRealName(Integer id, String realName, String siteCode) {
        MbrAccount mbrAccount = getAccountInfo(id);
        if (StringUtils.isEmpty(mbrAccount.getRealName())) {
            MbrAccount mbrAccount1 = new MbrAccount();
            mbrAccount1.setId(id);
            mbrAccount1.setRealName(realName);
            accountLogService.updateApiAccountName(mbrAccount, realName);
            super.update(mbrAccount1);
            mbrAccount.setRealName(realName);
        }
        return mbrAccount;
    }

    public void updatePwd(Integer id, PwdDto pwdDto) {
        MbrAccount mbrAccount = getAccountInfo(id);
        String lastPwdHash = new Sha256Hash(pwdDto.getLastPwd(), mbrAccount.getSalt()).toHex();
        if (!lastPwdHash.equals(mbrAccount.getLoginPwd())) {
            throw new RRException("旧密码错误,不能修改密码!");
        }
        String newPwdHash = new Sha256Hash(pwdDto.getPwd(), mbrAccount.getSalt()).toHex();
        if (newPwdHash.equals(mbrAccount.getLoginPwd())) {
            throw new RRException("被修改的密码不能与旧密码相同!");
        }
        if (newPwdHash.equals(mbrAccount.getSecurePwd())) {
            throw new RRException("被修改的密码不能与资金密码相同!");
        }
        mbrAccount = new MbrAccount();
        mbrAccount.setId(id);
        mbrAccount.setLoginPwd(newPwdHash);
        super.update(mbrAccount);
    }

    public void updateScPwd(Integer id, PwdDto pwdDto) {
        MbrAccount mbrAccount = getAccountInfo(id);
        String lastPwdHash = new Sha256Hash(pwdDto.getLastPwd(), mbrAccount.getSalt()).toHex();
        if (!lastPwdHash.equals(mbrAccount.getSecurePwd())) {
            throw new RRException("旧密码错误,不能修改密码!");
        }
        String newPwdHash = new Sha256Hash(pwdDto.getPwd(), mbrAccount.getSalt()).toHex();
        if (newPwdHash.equals(mbrAccount.getSecurePwd())) {
            throw new RRException("被修改的密码不能与旧密码相同!");
        }
        if (newPwdHash.equals(mbrAccount.getLoginPwd())) {
            throw new RRException("被修改的密码不能与登陆相同!");
        }
        mbrAccount = new MbrAccount();
        mbrAccount.setId(id);
        mbrAccount.setSecurePwd(newPwdHash);
        super.update(mbrAccount);
    }

    public boolean updateOrCheckScPwd(Integer id, String pwd) {
        MbrAccount mbrAccount = getAccountInfo(id);
        if (mbrAccount.getAvailable() == MbrAccount.Status.LOCKED)
            throw new RRException("余额已冻结,不能申请取款!");
        String newPwdHash = new Sha256Hash(pwd, mbrAccount.getSalt()).toHex();
        if (StringUtils.isEmpty(mbrAccount.getSecurePwd())) {
            if (newPwdHash.equals(mbrAccount.getLoginPwd()))
                throw new RRException("资金密码不能与登陆密码相同!");
            mbrAccount = new MbrAccount();
            mbrAccount.setId(id);
            mbrAccount.setSecurePwd(newPwdHash);
            super.update(mbrAccount);
            return Boolean.TRUE;
        } else if (mbrAccount.getSecurePwd().equals(newPwdHash)) {
            return Boolean.TRUE;
        } else {
            throw new RRException("资金密码错误!");
        }
    }

    public int updateMail(Integer id, String email) {
        MbrAccount mbrAccount = new MbrAccount();
        mbrAccount.setId(id);
        mbrAccount.setEmail(email);
        mbrAccount.setIsAllowEmail(Available.enable);
        mbrAccount.setIsVerifyEmail(Available.enable);
        accountLogService.updateApiAccountMail(mbrAccount, email);
        return super.update(mbrAccount);
    }

    public int updateMobile(Integer id, String mobile) {
        MbrAccount mbrAccount = new MbrAccount();
        mbrAccount.setId(id);
        mbrAccount.setMobile(mobile);
        mbrAccount.setIsVerifyMoblie(Available.enable);
        mbrAccount.setIsAllowMsg(Available.enable);
        accountLogService.updateApiAccountMobile(mbrAccount, mobile);
        return super.update(mbrAccount);
    }

    //@Cacheable(cacheNames = ApiConstants.REDIS_USER_CACHE_KEY, key = "#siteCode+''+#id")
    //TODO 去掉缓存 不然拿到的信息不准确  导致余额冻结了还可以操作资金
    public MbrAccount queryObject(Integer id, String siteCode) {
        return super.queryObject(id);
    }

    private MbrWallet getMbrWallet(MbrAccount mbrAccount) {
        MbrWallet wallet = new MbrWallet();
        wallet.setLoginName(mbrAccount.getLoginName());
        wallet.setBalance(Constants.DEAULT_ZERO_VALUE);
        wallet.setAccountId(mbrAccount.getId());
        return wallet;
    }

    private LogMbrRegister getlogRegister(MbrAccount mbrAccount, HttpServletRequest request, Byte source) {
        LogMbrRegister logRegister = new LogMbrRegister();
        logRegister.setRegisterIp(CommonUtil.getIpAddress(request));
        logRegister.setRegisterSource(source);
        logRegister.setRegisterUrl(IpUtils.getUrl(request));
        logRegister.setLoginName(mbrAccount.getLoginName());
        logRegister.setAccountId(mbrAccount.getId());
        logRegister.setRegisterTime(getCurrentDate(FORMAT_18_DATE_TIME));
        logRegister.setRegArea(ipService.getIpArea(logRegister.getRegisterIp()));
        return logRegister;
    }

    public LinkedHashMap<String, Object> webUserVfyInfo(Integer userId) {
        LinkedHashMap<String, Object> userInfo = new LinkedHashMap<String, Object>();
        MbrAccount mbrAccount = getAccountInfo(userId);
        userInfo.put("loginPwdVfy", true);
        userInfo.put("scPwdVfy", !StringUtils.isEmpty(mbrAccount.getSecurePwd()));
        userInfo.put("emailVfy", mbrAccount.getIsVerifyEmail() == 1);
        userInfo.put("phoneVfy", mbrAccount.getIsVerifyMoblie() == 1);
        userInfo.put("bankCardNo", mbrBankcardService.countBankNo(userId));
        userInfo.put("email", StringUtil.mail(mbrAccount.getEmail()));
        userInfo.put("phone", StringUtil.phone(mbrAccount.getMobile()));
        return userInfo;
    }

    public void updateMbrAccount(MbrAccount account, String userName) {
        ValidRegUtils.validRealName(account, SysSetting.SysValueConst.require);
        ValidRegUtils.validQQ(account, SysSetting.SysValueConst.visible);
        ValidRegUtils.validWeChat(account, SysSetting.SysValueConst.visible);
        MbrAccount oldAccount = getAccountInfo(account.getId());
        if (StringUtil.isNotEmpty(account.getEmail())) {
            ValidRegUtils.validEmail(account, SysSetting.SysValueConst.visible);
            account.setIsVerifyEmail(Available.disable);
            account.setIsAllowEmail(Available.disable);
        }
        if (StringUtil.isNotEmpty(account.getMobile())) {
            ValidRegUtils.validPhone(account, SysSetting.SysValueConst.visible);
            account.setIsVerifyMoblie(Available.disable);
            account.setIsAllowMsg(Available.disable);
        }
        // 修改会员基本信息
        super.update(account);

        // 同步修改银行卡用户名
        if (null == oldAccount) {
            // 添加会员信息修改日志
            accountLogService.updateAccountInfo(oldAccount, account, userName);
            return;
        }
        if (null != account.getRealName()) {
            mbrBankcardService.updateBankCardNameByAccId(account.getId(), account.getRealName());
        }
    }

    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public int updateOffline(String loginName) {
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(loginName)) {
            int resu = mbrMapper.updateOffline(loginName);
            if (resu > 0) {
                logLoginService.setLoginOffTime(loginName);
            }
            return resu;
        }
        return Constants.EVNumber.zero;
    }

    public MbrFundTotal findMbrTotal(Integer accountId, Integer roleId) {
        //查询用户具备得列权限功能
        List<ColumnAuthTreeDto> menuList = columnAuthProviderService.getRoleColumnAuth(roleId, ColumnAuthConstants.MEMBER_ASSET_DATA_MENU_ID, ColumnAuthConstants.COLUMN_MENU_TYPE_THREE);
        Assert.isNullOrEmpty(menuList, "无权限，请联系管理员");

        MbrAccount mbrAccount = new MbrAccount();
        Set<String> columnSets = new HashSet<String>();
        for (ColumnAuthTreeDto columnAuthTreeDto : menuList) {
            if ("bet".equals(columnAuthTreeDto.getColumnName()) || "payout".equals(columnAuthTreeDto.getColumnName()) || "validBet".equals(columnAuthTreeDto.getColumnName())) {
                continue;
            }
            if (!StringUtil.isEmpty(columnAuthTreeDto.getColumnName())) {
                columnSets.add(columnAuthTreeDto.getColumnName());
            }
        }
        mbrAccount.setId(accountId);
        mbrAccount.setColumnSets(columnSets);
        return mbrMapper.mbrFundsTotal(mbrAccount);
    }

    public List<Map> queryAccountAuditList(Integer accountId) {
        List<ColumnAuthTreeDto> menuList = columnAuthProviderService.getRoleColumnAuth(getUser().getRoleId(), ColumnAuthConstants.MEMBER_RISK_MENU_ID, ColumnAuthConstants.COLUMN_MENU_TYPE_THREE);
        Assert.isNullOrEmpty(menuList, "无权限，请联系管理员");

        MbrAccount mbrAccount = new MbrAccount();
        Set<String> columnSets = new HashSet<String>();
        for (ColumnAuthTreeDto columnAuthTreeDto : menuList) {
            columnSets.add(columnAuthTreeDto.getColumnName());
        }
        mbrAccount.setId(accountId);
        mbrAccount.setColumnSets(columnSets);
        return mbrMapper.selectRiskControlAudit(mbrAccount);
    }

    public PageUtils queryAccountAuditInfo(Integer accountId, String keys, String item, Integer pageNo,
                                           Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<ItemDto> list = mbrMapper.queryAccountAuditInfo(accountId, keys, item);
        return BeanUtil.toPagedResult(list);
    }

    public List<Map> queryAccountBonusReporList(Integer accountId) {
        return mbrMapper.queryAccountBonusReporList(accountId);
    }

    public PageUtils bonusList(Integer accountId, Integer pageNo, Integer pageSize) {
        return actActivityService.findAccountBonusList(null, null, accountId, pageNo, pageSize, Constants.EVNumber.one);
    }

    public PageUtils withdrawList(Integer accountId, Integer pageNo, Integer pageSize) {
        AccWithdraw accWithdraw = new AccWithdraw();
        accWithdraw.setAccountId(accountId);
        return withdrawService.queryAccListPage(accWithdraw, pageNo, pageSize);
    }

    public PageUtils depositList(Integer accountId, Integer pageNo, Integer pageSize) {
        FundDeposit deposit = new FundDeposit();
        deposit.setAccountId(accountId);
        return depositService.queryListPage(deposit, pageNo, pageSize);
    }

    public PageUtils manageList(Integer accountId, Integer pageNo, Integer pageSize) {
        MbrBillManage mbrBillManage = new MbrBillManage();
        mbrBillManage.setAccountId(accountId);
        return reportService.queryListPage(mbrBillManage, pageNo, pageSize);
    }

    public PageUtils fundList(Integer accountId, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<MbrBillDetail> list = mbrMapper.queryAccountFundList(accountId);
        return BeanUtil.toPagedResult(list);
    }

    public PageUtils accountLogList(Integer accountId, Integer pageNo, Integer pageSize, Long userId) {
        return accountLogService.accountLogList(accountId, pageNo, pageSize, userId);
    }

    public MbrAccount findAccountByName(String loginName) {
        MbrAccount account = new MbrAccount();
        account.setLoginName(loginName);
        MbrAccount mbrAccount = accountMapper.selectOne(account);
        account.setId(mbrAccount.getId());
        return account;
    }

    public List<Map> findHomePageCount() {
        String startday = getCurrentDate(FORMAT_10_DATE);
        return mbrMapper.findHomePageCount(startday);
    }

    public void saveQQOrWeChat(MbrAccount mbrAccount) {
        accountMapper.updateByPrimaryKeySelective(mbrAccount);
    }

    //计算完整度 realName,mobile,email,weChat,securePwd,qq,bankCard
    private Byte getUserInfoMeasure(MbrAccount mbrAccount) {
        List<MbrBankcard> mbrBankcardsList = mbrBankcardService.ListCondBankCard(mbrAccount.getId());

        byte userInfoMeasure = 0;
        //真实姓名
        if (StringUtil.isNotEmpty(mbrAccount.getRealName())) {
            userInfoMeasure++;
        }
        //手机号码
        if (StringUtil.isNotEmpty(mbrAccount.getMobile())) {
            userInfoMeasure++;
        }
        //邮箱
        if (StringUtil.isNotEmpty(mbrAccount.getEmail())) {
            userInfoMeasure++;
        }
        //微信号
        if (StringUtil.isNotEmpty(mbrAccount.getWeChat())) {
            userInfoMeasure++;
        }
        //securePwd
        if (StringUtil.isNotEmpty(mbrAccount.getSecurePwd())) {
            userInfoMeasure++;
        }
        //qq
        if (StringUtil.isNotEmpty(mbrAccount.getQq())) {
            userInfoMeasure++;
        }
        //bankCard
        if (mbrBankcardsList.size() != 0) {
            userInfoMeasure++;
        }
        if (userInfoMeasure == 0) {
            return Constants.userInfoMeasure.zero;
        }
        if (userInfoMeasure == 7) {
            return Constants.userInfoMeasure.full;
        }
        BigDecimal full = new BigDecimal(Constants.userInfoMeasure.full);
        Byte result = ((full.divide(new BigDecimal(Constants.userInfoMeasure.userInfoConut), 3, RoundingMode.HALF_UP).multiply(new BigDecimal(userInfoMeasure))).setScale(0, BigDecimal.ROUND_HALF_UP)).byteValue();
        return result;
    }

    public void setSecurePwdOfFirst(MbrAccount mbrAccount) {
        MbrAccount account = accountMapper.selectByPrimaryKey(mbrAccount.getId());
        if (StringUtil.isNotEmpty(account.getSecurePwd()))
            throw new R200Exception("资金密码已设置，请通过修改方式进行修改");
        String newPwd = mbrAccount.getSecurePwd();
        if (StringUtil.isEmpty(newPwd))
            throw new R200Exception("资金密码不能为空");
        String newPwdHash = new Sha256Hash(newPwd, account.getSalt()).toHex();
        if (newPwdHash.equals(account.getLoginPwd()))
            throw new RRException("资金密码不能与登陆密码相同!");
        account.setSecurePwd(newPwdHash);
        accountMapper.updateByPrimaryKeySelective(account);
    }

    //单独返回免转开关状态
    public int findFreeWalletSwitchStatus(Integer accountId) {
        return mbrMapper.findFreeWalletSwitchStatus(accountId);
    }
}
