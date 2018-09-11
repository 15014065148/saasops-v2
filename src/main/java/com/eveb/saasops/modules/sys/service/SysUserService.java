package com.eveb.saasops.modules.sys.service;

import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.modules.agent.entity.AgentAccount;
import com.eveb.saasops.modules.agent.service.AgentAccountService;
import com.eveb.saasops.modules.member.entity.MbrGroup;
import com.eveb.saasops.modules.member.service.MbrGroupService;
import com.eveb.saasops.modules.sys.dao.SysUserAgyaccountrelationMapper;
import com.eveb.saasops.modules.sys.dao.SysUserDao;
import com.eveb.saasops.modules.sys.dao.SysUserMbrgrouprelationMapper;
import com.eveb.saasops.modules.sys.entity.Authority;
import com.eveb.saasops.modules.sys.entity.SysUserAgyaccountrelation;
import com.eveb.saasops.modules.sys.entity.SysUserEntity;
import com.eveb.saasops.modules.sys.entity.SysUserEntity.ErrorCode;
import com.eveb.saasops.modules.sys.entity.SysUserMbrgrouprelation;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 系统用户
 */
@Slf4j
@Service("sysUserService")
public class SysUserService{

    @Autowired  //①  注入上下文
    private ApplicationContext context;
    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private SysUserRoleService sysUserRoleService;
    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private SysUserAgyaccountrelationService sysUserAgyaccountrelationService;
    @Autowired
    private SysUserMbrgrouprelationService sysUserMbrgrouprelationService;
    @Autowired
    private MbrGroupService mbrGroupService;
    @Autowired
    private AgentAccountService agentAccountService;
    @Autowired
    private SysUserMbrgrouprelationMapper sysUserMbrgrouprelationMapper;
    @Autowired
    private SysUserAgyaccountrelationMapper sysUserAgyaccountrelationMapper;

    @Resource(name = "stringRedisTemplate_1")
    private RedisTemplate redisTemplate;

    @Value("${redis.cache.sysUser}")
    private String sys_user;


    protected SysUserEntity getUser() {
        return (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
    }

    public List<String> queryAllPerms(Long userId) {
        return sysUserDao.queryAllPerms(userId);
    }

    public List<Long> queryAllMenuId(Long userId) {
        return sysUserDao.queryAllMenuId(userId);
    }

    public SysUserEntity queryByUserName(String username) {
        return sysUserDao.queryByUserName(username);
    }

    public SysUserEntity queryObject(Long userId) {

        return sysUserDao.queryObject(userId);
    }


    public SysUserEntity queryUserEntityOne(Long userId) {
        SysUserEntity userEntity = new SysUserEntity();
        userEntity.setUserId(userId);
        List<SysUserEntity> sysUserEntities = sysUserDao.queryConditions(userEntity);
        SysUserEntity sysUserEntity = sysUserEntities != null ? sysUserEntities.get(0) : null;
        SysUserMbrgrouprelation mbrgrouprelation = new SysUserMbrgrouprelation();
        mbrgrouprelation.setUserId(sysUserEntity.getUserId());
        sysUserEntity.setMbrGroups(sysUserMbrgrouprelationMapper.select(mbrgrouprelation));
        SysUserAgyaccountrelation userAgyaccountrelation = new SysUserAgyaccountrelation();
        userAgyaccountrelation.setUserId(sysUserEntity.getUserId());
        sysUserEntity.setAgyAccounts(sysUserAgyaccountrelationMapper.select(userAgyaccountrelation));
        return sysUserEntity;
    }

    public void setMbrAuthTotal(SysUserEntity user) {
        List<MbrGroup> groups = mbrGroupService.queryList();
        List<SysUserMbrgrouprelation> mbrGroups = user.getMbrGroups();
        mbrGroups.clear();
        groups.stream().forEach(group -> {
            SysUserMbrgrouprelation sysUserMbrgrouprelation = new SysUserMbrgrouprelation();
            sysUserMbrgrouprelation.setUserId(user.getUserId());
            sysUserMbrgrouprelation.setMbrGroupId(group.getId());
            mbrGroups.add(sysUserMbrgrouprelation);
        });
    }

    public void setAgyAuthTotal(SysUserEntity user) {
        List<AgentAccount> agentAccounts = agentAccountService.queryList();
        List<SysUserAgyaccountrelation> sysUserAgyaccountrelations = user.getAgyAccounts();
        sysUserAgyaccountrelations.clear();
        agentAccounts.stream().forEach(agentAccount -> {
            SysUserAgyaccountrelation sysUserAgyaccountrelation = new SysUserAgyaccountrelation();
            sysUserAgyaccountrelation.setAgyAccountId(agentAccount.getId());
            sysUserAgyaccountrelation.setDisabled(true);
            sysUserAgyaccountrelation.setAgyAccountType(agentAccount.getParentId().equals(0) ? 0 : 1);
            sysUserAgyaccountrelations.add(sysUserAgyaccountrelation);
        });
    }

    public PageUtils queryList(SysUserEntity userEntity) {
        PageHelper.startPage(userEntity.getPageNo(), userEntity.getPageSize());
        if (!StringUtils.isEmpty(userEntity.getOrder()))
            PageHelper.orderBy(userEntity.getOrder());
        List<SysUserEntity> list = sysUserDao.queryList(userEntity);
        PageUtils p = BeanUtil.toPagedResult(list);
        return p;
    }

    /*@Override
    public int queryTotal(Map<String, Object> map) {
        return sysUserDao.queryTotal(map);
    }*/

    @Transactional
    public void save(SysUserEntity user) {
        user.setCreateTime(new Date());
        //sha256加密
        String salt = RandomStringUtils.randomAlphanumeric(20);
        user.setPassword(new Sha256Hash(user.getPassword(), salt).toHex());
        user.setSecurepwd(new Sha256Hash(user.getSecurepwd(), salt).toHex());
        user.setSalt(salt);
        sysUserDao.save(user);
        //检查角色是否越权
        //checkRole(user);
        //保存用户与角色关系
        //SysUserEntity sue = sysUserDao.queryByUserName(user.getUsername());
        if (user.getRoleIdList() != null) {
            sysUserRoleService.saveOrUpdate(user.getUserId(), user.getRoleIdList());
        }
        //更新用户数据权限
        SysUserService sysUserService = context.getBean(SysUserService.class);
        sysUserService.saveDataAuth(user, CommonUtil.getSiteCode());
    }

    @Transactional
    public int update(SysUserEntity user) {
        user.setPassword(null);
        user.setSecurepwd(null);
        sysUserDao.update(user);
        // 检查角色是否越权
        checkRole(user);
        // 保存用户与角色关系
        sysUserRoleService.saveOrUpdate(user.getUserId(), user.getRoleIdList());
        //更新用户数据权限
        SysUserService sysUserService = context.getBean(SysUserService.class);
        sysUserService.updateDataAuth(user, CommonUtil.getSiteCode());
        return 0;
    }

    /**
     * 删除缓存
     *
     * @param userId
     */
    @Transactional
    public void deleteCatchBatch(Long userId) {
        sysUserDao.deleteSysUser(userId);
        redisTemplate.delete(sys_user + CommonUtil.getSiteCode() + ":" + userId);
    }

    /**
     * 修改密码
     */
    public int updatePassword(Long userId, String password, String newPassword) {
        SysUserEntity dbUserEntity = sysUserDao.queryObject(userId);
        //String newShaPwd = new Sha256Hash(newPassword, dbUserEntity.getSalt()).toHex();
        if (newPassword.equals(dbUserEntity.getPassword())) {
            return ErrorCode.code_01;
        }
        if (!StringUtils.isEmpty(dbUserEntity.getSecurepwd()) && newPassword.equals(dbUserEntity.getSecurepwd())) {
            return ErrorCode.code_02;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("password", password);
        map.put("newPassword", newPassword);
        return sysUserDao.updatePassword(map);
    }

    /**
     * 修改安全密码
     */
    public int updateSecPassword(Long userId, String securepwd, String newPassword) {
        SysUserEntity dbUserEntity = sysUserDao.queryObject(userId);
        //String newShaPwd = new Sha256Hash(newPassword, dbUserEntity.getSalt()).toHex();
        if (newPassword.equals(dbUserEntity.getSecurepwd())) {
            return ErrorCode.code_03;
        }
        if (!StringUtils.isEmpty(dbUserEntity.getPassword()) && newPassword.equals(dbUserEntity.getPassword())) {
            return ErrorCode.code_04;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("securepwd", securepwd);
        map.put("newPassword", newPassword);
        return sysUserDao.updateSecPassword(map);
    }

    public PageUtils queryConditions(SysUserEntity userEntity) {
        PageHelper.startPage(userEntity.getPageNo(), userEntity.getPageSize());
        if (!StringUtils.isEmpty(userEntity.getOrder()))
            PageHelper.orderBy(userEntity.getOrder());
        List<SysUserEntity> list = sysUserDao.queryConditions(userEntity);

        list.stream().forEach(sysUserEntity -> {
            if (sysUserEntity.getUserAgyAccountAuth() == 1) {
                setAgyAuthTotal(sysUserEntity);
            }
            if (sysUserEntity.getUserMbrGroupAuth() == 1) {
                setMbrAuthTotal(sysUserEntity);
            }
        });
        PageUtils p = BeanUtil.toPagedResult(list);
        return p;
    }

    @Cacheable(value = "authority", key = "#siteCode+':'+#userId.toString()")
    public Authority getUserAuth(Long userId, String siteCode) {
        List<SysUserAgyaccountrelation> agyaccountrelations = sysUserDao.getAuthAgy(userId);
        List<SysUserMbrgrouprelation> mbrgrouprelations = sysUserDao.getAuthMbr(userId);
        String agyAthIds_local = "";
        String agyAthIds_total = "";
        for (SysUserAgyaccountrelation agy : agyaccountrelations) {
            agy.setUserId(userId);
            if (agy.getAgyAccountType() != null) {
                if (agy.getAgyAccountType() == 0) {
                    agyAthIds_total += agy.getAgyAccountId() + ",";
                } else {
                    agyAthIds_local += agy.getAgyAccountId() + ",";
                }
            }
        }
        //2 会员组
        String mbrAuth = "";
        for (SysUserMbrgrouprelation mbrgrouprelation : mbrgrouprelations) {
            mbrgrouprelation.setUserId(userId);
            mbrAuth += mbrgrouprelation.getMbrGroupId() + ",";
        }
        //把当前用户的权限信息放入redis缓存
        Map<String, Object> rowAuthority = new HashMap<>();
        rowAuthority.put("agyAthIds_total", !agyAthIds_total.equals("") ? agyAthIds_total.substring(0, agyAthIds_total.length() - 1) : "");
        rowAuthority.put("agyAthIds_local", !agyAthIds_local.equals("") ? agyAthIds_local.substring(0, agyAthIds_local.length() - 1) : "");
        rowAuthority.put("mbrAuth", !mbrAuth.equals("") ? mbrAuth.substring(0, mbrAuth.length() - 1) : "");
        SysUserEntity user = sysUserDao.queryObject(userId);
        rowAuthority.put("mbrAuthType", user.getUserMbrGroupAuth());
        rowAuthority.put("agyAuthType", user.getUserAgyAccountAuth());
        return new Authority(String.valueOf(userId), rowAuthority);
    }

    /**
     * 检查角色是否越权
     */
    private void checkRole(SysUserEntity user) {
        //如果不是超级管理员，则需要判断用户的角色是否自己创建
        if (getUser().getRoleId() == 1) {
            return;
        }

        //查询用户创建的角色列表
        List<Long> roleIdList = sysRoleService.queryRoleIdList(user.getRoleName());

        //判断是否越权
        if (!roleIdList.containsAll(user.getRoleIdList())) {
            throw new RRException("新增用户所选角色，不是本人创建");
        }
    }


    /**
     * 保存用户和数据权限的关联关系
     *
     * @param user
     */
    @Transactional
    @CachePut(value = "authority", key = "#siteCode+':'+#user.getUserId().toString()")
    public Authority saveDataAuth(SysUserEntity user, String siteCode) {
        //1 代理关系
        Long userId = user.getUserId();

        String agyAthIds_local = "";
        String agyAthIds_total = "";
        List<SysUserAgyaccountrelation> agyaccountrelations = user.getAgyAccounts();
        if (agyaccountrelations.size() > 0) {
            for (int i = 0; i < agyaccountrelations.size(); i++) {
                SysUserAgyaccountrelation agy = agyaccountrelations.get(i);
                agy.setUserId(userId);
                agyaccountrelations.get(i).setUserId(userId);
                if (agy.getAgyAccountType() == 0) {
                    agyAthIds_total += agy.getAgyAccountId() + ",";
                } else {
                    agyAthIds_local += agy.getAgyAccountId() + ",";
                }
            }
            sysUserAgyaccountrelationService.saveList(agyaccountrelations);
        }
        //2 会员组
        String mbrAuth = "";
        List<SysUserMbrgrouprelation> mbrgrouprelations = user.getMbrGroups();
        if (mbrgrouprelations.size() > 0) {
            for (SysUserMbrgrouprelation mbrgrouprelation : mbrgrouprelations) {
                mbrgrouprelation.setUserId(userId);
                mbrAuth += mbrgrouprelation.getMbrGroupId() + ",";
            }
            sysUserMbrgrouprelationService.saveList(mbrgrouprelations);
        }
        //把当前用户的权限信息放入redis缓存
        Map<String, Object> rowAuthority = new HashMap<>();
        rowAuthority.put("agyAthIds_total", !agyAthIds_total.equals("") ? agyAthIds_total.substring(0, agyAthIds_total.length() - 1) : "");
        rowAuthority.put("agyAthIds_local", !agyAthIds_local.equals("") ? agyAthIds_local.substring(0, agyAthIds_local.length() - 1) : "");
        rowAuthority.put("mbrAuth", !mbrAuth.equals("") ? mbrAuth.substring(0, mbrAuth.length() - 1) : "");
        rowAuthority.put("mbrAuthType", user.getUserMbrGroupAuth());
        rowAuthority.put("agyAuthType", user.getUserAgyAccountAuth());
        return new Authority(String.valueOf(userId), rowAuthority);
    }

    @CacheEvict(value = "authority", key = "#siteCode+':'+#userId", allEntries = true)
    public void deleteAuthorityCache(Long userId, String siteCode) {
    }

    public void updateEnable(SysUserEntity user) {
        sysUserDao.updateEnable(user);
    }

    /**
     * 更新用户数据权限
     *
     * @param user
     * @return
     */
    @Transactional
    @CachePut(value = "authority", key = "#siteCode+':'+#user.getUserId().toString()")
    public Authority updateDataAuth(SysUserEntity user, String siteCode) {
        sysUserDao.deleteAuthority(user.getUserId());
        //1 代理关系
       /* Long userId = user.getUserId();
        List<SysUserAgyaccountrelation> agyaccountrelations =new ArrayList<>();
        if (user.getUserAgyAccountAuth() == 2 ) { //等于1 的时候全部总代,在读取的时候做处理
            agyaccountrelations = user.getAgyAccounts();
            if(agyaccountrelations.size() > 0) {
                for (SysUserAgyaccountrelation agy : agyaccountrelations) {
                    agy.setUserId(userId);
                }
                sysUserAgyaccountrelationService.saveList(agyaccountrelations);
            }
        }
        List<SysUserMbrgrouprelation> mbrgrouprelations =new ArrayList<>();
        if(user.getUserMbrGroupAuth() == 2 && mbrgrouprelations.size() > 0) {
            mbrgrouprelations = user.getMbrGroups();
            if(mbrgrouprelations.size() > 0 ) {
                //2 会员组
                for (SysUserMbrgrouprelation mbrgrouprelation : mbrgrouprelations) {
                    mbrgrouprelation.setUserId(userId);
                }
                sysUserMbrgrouprelationService.saveList(mbrgrouprelations);
            }
        }
        //把当前用户的权限信息放入redis缓存
        Map<String, Object> rowAuthority = new HashMap<>();
        rowAuthority.put("mbrAuthType", user.getUserMbrGroupAuth());
        rowAuthority.put("agyAuthType", user.getUserAgyAccountAuth());
        rowAuthority.put("agyAuth", agyaccountrelations);
        rowAuthority.put("mbrAuth", mbrgrouprelations);
        return new Authority(String.valueOf(userId), rowAuthority);*/
        //更新用户数据权限
        SysUserService sysUserService = context.getBean(SysUserService.class);
        return sysUserService.saveDataAuth(user, CommonUtil.getSiteCode());
    }

    public int authsecPwd(Long userId, String securepwd) {
        SysUserEntity dbUserEntity = sysUserDao.queryObject(userId);
        String newSecurePwd = new Sha256Hash(securepwd, dbUserEntity.getSalt()).toHex();
        if (!newSecurePwd.equals(dbUserEntity.getSecurepwd()))
            return ErrorCode.code_06;
        else
            return ErrorCode.code_07;
    }
}
