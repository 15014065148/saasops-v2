package com.eveb.saasops.modules.sys.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.modules.sys.dao.SysRoleMenuDao;
import com.eveb.saasops.modules.sys.dto.*;
import com.eveb.saasops.modules.sys.entity.SysUserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eveb.saasops.common.utils.Constant;
import com.eveb.saasops.modules.sys.dao.SysMenuDao;
import com.eveb.saasops.modules.sys.dao.SysRoleDao;
import com.eveb.saasops.modules.sys.entity.SysMenuEntity;
import com.eveb.saasops.modules.sys.entity.SysMenuTree;
import com.eveb.saasops.modules.sys.entity.SysRoleMenuEntity;
import com.github.mustachejava.util.InternalArrayList;

import static com.eveb.saasops.common.constants.ColumnAuthConstants.*;

@Service("sysMenuService")
public class SysMenuService {

    List<Integer> times = new InternalArrayList<>();
    @Autowired
    private SysMenuDao sysMenuDao;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysRoleDao sysRoleDao;
    @Autowired
    private SysRoleMenuDao sysRoleMenuDao;


    public List<Long> queryRoleList(Long userId) {
        return sysRoleDao.queryRoleList(userId);
    }


    @Cacheable(value = "SysRoleMenuTree", key = "#siteCode+':'+#roles")
    public List<SysMenuTree> selectTreeByRole(String roles, String siteCode) {
        List<SysMenuTree> sysMenuEntities = sysMenuDao.selectRoleMenuTree(roles);
        List<SysMenuTree> sysMenuTree = new ArrayList<>();
        sysMenuEntities.forEach(sysMenuEntity -> {
            if (sysMenuEntity.getParentId().equals(0L)) {
                sysMenuTree.add(sysMenuEntity);
            }
        });
        getRoleMenuTree(sysMenuTree, sysMenuEntities);
        return sysMenuTree;
    }


    public void getRoleMenuTree(List<SysMenuTree> sysMenuTree, List<SysMenuTree> sysMenuEntities) {
        for (int i = 0; i < sysMenuTree.size(); i++) {
            for (int j = 0; j < sysMenuEntities.size(); j++) {
                if (sysMenuEntities.get(j).getParentId().equals(sysMenuTree.get(i).getMenuId())) {
                    List<SysMenuTree> children = sysMenuTree.get(i).getChildren();
                    if (!children.contains(sysMenuEntities.get(j))) {
                        children.add(sysMenuEntities.get(j));
                        List<SysMenuTree> sysMenuEntitiesCopy = new ArrayList<>(sysMenuEntities);
                        sysMenuEntitiesCopy.remove(i);
                        getRoleMenuTree(children, sysMenuEntitiesCopy);
                    }
                }
            }
        }
    }


    @CacheEvict(value = "SysRoleMenuTree", key = "#siteCode+':'+#roles")
    public void deleteTreeByRole(String roles, String siteCode) {

    }


    public List<SysMenuEntity> queryListParentId(Long parentId, List<Long> menuIdList) {
        List<SysMenuEntity> menuList = queryListParentId(parentId);
        if (menuIdList == null) {
            return menuList;
        }

        List<SysMenuEntity> userMenuList = new ArrayList<>();
        for (SysMenuEntity menu : menuList) {
            if (menuIdList.contains(menu.getMenuId())) {
                userMenuList.add(menu);
            }
        }
        return userMenuList;
    }

    public List<SysMenuEntity> queryListParentId(Long parentId) {
        return sysMenuDao.queryListParentId(parentId);
    }

    public List<SysMenuEntity> queryNotButtonList() {
        return sysMenuDao.queryNotButtonList();
    }

    public List<SysMenuEntity> getUserMenuList(Long userId) {
        // 系统管理员，拥有最高权限
        if (userId == Constant.SUPER_ADMIN) {
            return getAllMenuList(null);
        }

        // 用户菜单列表
        List<Long> menuIdList = sysUserService.queryAllMenuId(userId);
        return getAllMenuList(menuIdList);
    }

    public SysMenuEntity queryObject(Long menuId) {
        return sysMenuDao.queryObject(menuId);
    }

    public List<SysMenuEntity> queryList(Map<String, Object> map) {
        return sysMenuDao.queryList(map);
    }


    public void save(SysMenuEntity menu) {
        sysMenuDao.save(menu);
    }

    public void update(SysMenuEntity menu) {
        sysMenuDao.update(menu);
    }

    @Transactional
    public void deleteBatch(Long[] menuIds) {
        sysMenuDao.deleteBatch(menuIds);
    }

    public List<SysMenuEntity> queryUserList(Long userId) {
        return sysMenuDao.queryUserList(userId);
    }

    /**
     * 获取所有菜单列表
     */
    private List<SysMenuEntity> getAllMenuList(List<Long> menuIdList) {
        // 查询根菜单列表
        List<SysMenuEntity> menuList = queryListParentId(0L, menuIdList);
        // 递归获取子菜单
        getMenuTreeList(menuList, menuIdList);

        return menuList;
    }

    /**
     * 递归
     */
    private List<SysMenuEntity> getMenuTreeList(List<SysMenuEntity> menuList, List<Long> menuIdList) {
        List<SysMenuEntity> subMenuList = new ArrayList<SysMenuEntity>();

        for (SysMenuEntity entity : menuList) {
            if (entity.getType() == Constant.MenuType.CATALOG.getValue()) {// 目录
                entity.setList(getMenuTreeList(queryListParentId(entity.getMenuId(), menuIdList), menuIdList));
            }
            subMenuList.add(entity);
        }
        return subMenuList;
    }

    @CachePut(value = "TreeMenuDto2", key = "'TreeMenu'")
    public List<TreeMenuDto2> queryTreeList() {
        return sysMenuDao.queryMenuList();
    }

    @Cacheable(value = "initRoleMenuTree", key = "#siteCode+':initRoleMenuTree'")
    public List<TreeMenuDto2> initRoleMenuTree(String siteCode) {
        List<SysMenuEntity> menuList = sysMenuDao.queryAllMenu();
        List<TreeMenuDto2> initRoleMenuTree = new LinkedList<>();
        menuList.stream().forEach(menu -> {
            generateTree(menu, initRoleMenuTree, 0L);
        });
        generateChildrenTree(initRoleMenuTree, menuList);
        return initRoleMenuTree;
    }

    @Cacheable(value = "getMenuAuth", key = "#siteCode+':getMenuAuth:'+#menuId.toString()")
    public TreeMenuDto2 getMenuAuth(String siteCode, Long menuId) {
        List<SysMenuEntity> menuList = sysMenuDao.queryAll();
        List<TreeMenuDto2> initRoleMenuTree = new LinkedList<>();
        TreeMenuDto2 treeMenuDto2 = new TreeMenuDto2();
        treeMenuDto2.setChildren(initRoleMenuTree);
        menuList.stream().forEach(menu -> {
            generateTree(menu, initRoleMenuTree, menuId);
            if (menu.getMenuId().equals(menuId)) {
                treeMenuDto2.setId(menuId);
                treeMenuDto2.setLabel(menu.getName());
            }
        });
        times.clear();
        generateAuthTree(initRoleMenuTree, menuList, 1);
        int T = treeMenuDto2.getChildren().size() != 0 ? 1 : 0;
        for (int t : times) {
            if (t > T) {
                T = t;
            }
        }
        treeMenuDto2.setLevel(T);
        return treeMenuDto2;
    }


    public HashSet<SysRoleMenuEntity> getRoleMenuAuth(Long roleId, Long menuId) {
        List<SysRoleMenuEntity> sysRoleMenuEntities = sysRoleDao.getSavedMenuAuth(roleId, menuId);
        List<SysRoleMenuEntity> sysRoleMenuSaved = new ArrayList<>();
        //遍历已保存的所有菜单
        sysRoleMenuEntities.stream().forEach(sysRoleMenuEntity -> {
            //保存的菜单的父id = 最高节点id
            if (sysRoleMenuEntity.getParentId().equals(menuId)) {
                //获取改父菜单下保存的节点
                sysRoleMenuSaved.add(sysRoleMenuEntity);
            }
        });
        HashSet<SysRoleMenuEntity> auth = new HashSet<>(sysRoleMenuSaved); //去除重复
        if (sysRoleMenuSaved.size() != 0) {

            gengerteRoleMenuSaved(auth, sysRoleMenuSaved, sysRoleMenuEntities);
        }
        //去除type = 1
        HashSet<SysRoleMenuEntity> authCopy = new HashSet<>();
        auth.forEach(e -> {
            if (e.getType() != null && e.getType().equals(2)) {
                authCopy.add(e);
            }
        });
        return authCopy;
    }

    /**
     * @param auth                要展现的菜单
     * @param sysRoleMenuSaved    已保存的菜单（父）
     * @param sysRoleMenuEntities //所有保存的菜单数据
     */
    private void gengerteRoleMenuSaved(HashSet<SysRoleMenuEntity> auth, List<SysRoleMenuEntity> sysRoleMenuSaved, List<SysRoleMenuEntity> sysRoleMenuEntities) {
        for (int x = 0; x < sysRoleMenuEntities.size(); x++) {
            for (int i = 0; i < sysRoleMenuSaved.size(); i++) {
                SysRoleMenuEntity saved = sysRoleMenuSaved.get(i);
                if (saved.getMenuId().equals(sysRoleMenuEntities.get(x).getParentId())) {
                    //获取子节点
                    auth.add(sysRoleMenuEntities.get(x));
                    List<SysRoleMenuEntity> sysRoleMenuEntitiesCopy = new ArrayList<>(sysRoleMenuEntities);
                    sysRoleMenuEntitiesCopy.remove(x);
                    List<SysRoleMenuEntity> sysRoleMenuSavedCopy = new ArrayList<>(sysRoleMenuSaved);
                    sysRoleMenuSavedCopy.remove(sysRoleMenuSaved.get(i));
                    sysRoleMenuSavedCopy.add(sysRoleMenuEntities.get(x));
                    gengerteRoleMenuSaved(auth, sysRoleMenuSavedCopy, sysRoleMenuEntitiesCopy);
                }
            }

        }
    }


    private void generateTree(SysMenuEntity menuEntity, List<TreeMenuDto2> initRoleMenuTree, Long parentId) {
        if (menuEntity.getParentId().equals(parentId)) {
            TreeMenuDto2 groupMenu = new TreeMenuDto2(menuEntity.getMenuId(), menuEntity.getName(), null, new LinkedList<TreeMenuDto2>());
            initRoleMenuTree.add(groupMenu);
        }
    }

    private void generateAuthTree(List<TreeMenuDto2> initRoleMenuTree, List<SysMenuEntity> menuList, int i) {
        Integer I = new Integer(i + 1);
        for (int x = 0; x < initRoleMenuTree.size(); x++) {
            TreeMenuDto2 dto = initRoleMenuTree.get(x);
            for (int y = 0; y < menuList.size(); y++) {
                SysMenuEntity menu = menuList.get(y);
                if (menu.getParentId().equals(dto.getId())) {
                    times.add(I);
                    TreeMenuDto2 item = new TreeMenuDto2(menu.getMenuId(), menu.getName(), null, new LinkedList<TreeMenuDto2>());
                    List<TreeMenuDto2> sun = dto.getChildren();
                    if (!sun.contains(item)) {
                        sun.add(item);
                    }
                    List<SysMenuEntity> menuListCopy = new ArrayList<>(menuList);
                    menuListCopy.remove(y);
                    generateAuthTree(sun, menuListCopy, I);
                }
            }
        }

    }

    private void generateChildrenTree(List<TreeMenuDto2> initRoleMenuTree, List<SysMenuEntity> menuList) {
        for (int x = 0; x < initRoleMenuTree.size(); x++) {
            TreeMenuDto2 dto = initRoleMenuTree.get(x);
            for (int y = 0; y < menuList.size(); y++) {
                SysMenuEntity menu = menuList.get(y);
                if (menu.getParentId().equals(dto.getId())) {
                    TreeMenuDto2 item = new TreeMenuDto2(menu.getMenuId(), menu.getName(), null, new LinkedList<TreeMenuDto2>());
                    List<TreeMenuDto2> sun = dto.getChildren();
                    if (!sun.contains(item)) {
                        sun.add(item);
                    }
                    List<SysMenuEntity> menuListCopy = new ArrayList<>(menuList);
                    menuListCopy.remove(y);
                    if (menu.getParentId() == 1) {
                        generateChildrenTree(sun, menuListCopy);
                    }
                }
            }
        }
    }

    public List<TreeMenuDto> queryMenuList(List<Long> menuIdList) {
        List<SysMenuEntity> pMenuList = new ArrayList<>();
        List<SysMenuEntity> cMenuList = new ArrayList<>();
        if (null != menuIdList && 0 != menuIdList.size()) {
            for (Long menuId : menuIdList) {
                SysMenuEntity sysMenuEntity = sysMenuDao.queryObject(menuId);
                if (null != sysMenuEntity) {
                    if (sysMenuEntity.getParentId() == 0L) {
                        pMenuList.add(sysMenuEntity);
                    } else {
                        cMenuList.add(sysMenuEntity);
                    }
                }
            }
        }
        List<TreeMenuDto> treeMenuList = new ArrayList<>();
        if (pMenuList.size() != 0) {
            for (SysMenuEntity pMenu : pMenuList) {
                TreeMenuDto treeMenuDto = new TreeMenuDto();
                treeMenuDto.setId(pMenu.getMenuId());
                treeMenuDto.setLabel(pMenu.getName());
                List<PermissonDto> pdList = new ArrayList<>();
                if (cMenuList.size() != 0) {
                    for (SysMenuEntity cMenu : cMenuList) {
                        if (cMenu.getParentId() == pMenu.getMenuId()) {
                            PermissonDto permissonDto = new PermissonDto();
                            permissonDto.setId(cMenu.getMenuId());
                            permissonDto.setLabel(cMenu.getName());
                            List<PermissionDetailDto> pddList = new ArrayList<>();

                            for (SysMenuEntity dMenu : cMenuList) {
                                if (dMenu.getParentId() == cMenu.getMenuId()) {
                                    if (dMenu.getPerms() != null && (dMenu.getPerms().contains("save")
                                            || dMenu.getPerms().contains("add"))) {
                                        pAdd(pddList, dMenu);
                                    }
                                    if (!(dMenu.getPerms() != null && (dMenu.getPerms().contains("save")
                                            || dMenu.getPerms().contains("add")))) {
                                        pAdd(pddList, dMenu);
                                    }
                                    /*
                                      PermissionDetailDto permissionDetailDto = new PermissionDetailDto();
									  permissionDetailDto.setId(dMenu.getMenuId());
									  permissionDetailDto.setLabel(dMenu.getName());
									  pddList.add(permissionDetailDto);*/
                                }
                            }
                            permissonDto.setChildren(pddList);
                            pdList.add(permissonDto);
                        }
                    }
                }
                treeMenuDto.setChildren(pdList);
                treeMenuList.add(treeMenuDto);
            }
        }
        return treeMenuList;
    }

    private void pAdd(List<PermissionDetailDto> pddList, SysMenuEntity dMenu) {
        PermissionDetailDto permissionDetailDto = new PermissionDetailDto();
        permissionDetailDto.setId(dMenu.getMenuId());
        permissionDetailDto.setLabel(dMenu.getName());
        pddList.add(permissionDetailDto);
    }

    public List<TreeMenuDto2> getChildMenuList(Long menuId) {
        List<TreeMenuDto2> resultList = sysMenuDao.getChildMenuList(menuId);
        return resultList;
    }

    public StatMenuDto getStatMenuDto(SysUserEntity sysUserEntity) {
        StatMenuDto statMenuDto = new StatMenuDto();
        if (sysUserEntity.getRoleId() != Constant.SUPER_ADMIN.intValue()) {
            int depositCount = sysRoleMenuDao.findSysRoleMenuByRoleIdAndMenuId(
                    sysUserEntity.getRoleId(), MEMBER_DEPOSIT_MENU_ID);
            if (depositCount == Constants.EVNumber.zero) {
                statMenuDto.setIsDepositCount(Boolean.FALSE);
            }
            int withdrawFirstCount = sysRoleMenuDao.findSysRoleMenuByRoleIdAndMenuId(
                    sysUserEntity.getRoleId(), MEMBER_WITHDRAW_FIRST_MENU_ID);
            if (withdrawFirstCount == Constants.EVNumber.zero) {
                statMenuDto.setIsWithdrawFirstCount(Boolean.FALSE);
            }
            int withdrawReviewCount = sysRoleMenuDao.findSysRoleMenuByRoleIdAndMenuId(
                    sysUserEntity.getRoleId(), MEMBER_WITHDRAW_REVIEW_MENU_ID);
            if (withdrawReviewCount == Constants.EVNumber.zero) {
                statMenuDto.setIsWithdrawReviewCount(Boolean.FALSE);
            }
            int bonusCount = sysRoleMenuDao.findSysRoleMenuByRoleIdAndMenuId(
                    sysUserEntity.getRoleId(), MEMBER_BOUNS_MENU_ID);
            if (bonusCount == Constants.EVNumber.zero) {
                statMenuDto.setIsBonusCount(Boolean.FALSE);
            }
        }
        return statMenuDto;
    }
}
