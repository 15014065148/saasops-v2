package com.eveb.saasops.modules.sys.service;

import com.eveb.saasops.common.validator.Assert;
import com.eveb.saasops.modules.sys.dto.ColumnAuthTreeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("ColumnAuthProviderServiceImpl")
public class ColumnAuthProviderService{
	
	@Autowired
	private SysColumnAuthService sysColumnAuthServiceImpl;
	
	public List<ColumnAuthTreeDto> getRoleColumnAuth(Integer roleId, Long menuId, Long type) {
		//判断用户是否有权限
		List<ColumnAuthTreeDto> resultList = sysColumnAuthServiceImpl.getRoleColumnAuth(menuId, type, roleId);
		Assert.isNullOrEmpty(resultList, "无权限，请联系管理员");
		return resultList;
	}

	public List<ColumnAuthTreeDto> getAllColumnAuth(Long menuId, Long type) {
		List<ColumnAuthTreeDto> resultList = sysColumnAuthServiceImpl.getColumnAuth(menuId, type);
		return resultList;
	}

	public List<ColumnAuthTreeDto> getRoleAuth(ColumnAuthTreeDto columnAuthTreeDto) {
		List<ColumnAuthTreeDto> resultList = sysColumnAuthServiceImpl.getRoleAuth(columnAuthTreeDto);
		return resultList;
	}
}
