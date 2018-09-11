package com.eveb.saasops.modules.sys.service;

import com.eveb.saasops.modules.sys.dao.SysColumnAuthDao;
import com.eveb.saasops.modules.sys.dto.ColumnAuthTreeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("sysColumnAuthService")
public class SysColumnAuthService{

    @Autowired
    private SysColumnAuthDao sysColumnAuthDao;
    
    /**
     * 获取所有列权限列表
     */
    public List<ColumnAuthTreeDto> getColumnAuth(Long menuId, Long type) {
        List<ColumnAuthTreeDto> columnAuthList = sysColumnAuthDao.getColumnAuth(menuId, type);
        return columnAuthList;
    }
    
    /**
     * 获取所有列权限列表
     */
    public List<ColumnAuthTreeDto> getRoleColumnAuth(Long menuId, Long type, Integer roleId) {
        List<ColumnAuthTreeDto> columnAuthList = sysColumnAuthDao.getRoleColumnAuth(menuId, type, roleId);
        return columnAuthList;
    }
    
    /**
     * 获取所有列权限列表
     */
    public List<ColumnAuthTreeDto> getRoleAuth(ColumnAuthTreeDto columnAuthTreeDto) {
        List<ColumnAuthTreeDto> columnAuthList = sysColumnAuthDao.getRoleAuth(columnAuthTreeDto);
        return columnAuthList;
    }

}
