package com.eveb.saasops.modules.sys.dto;

import com.eveb.saasops.modules.sys.entity.SysRoleEntity;
import com.eveb.saasops.modules.sys.entity.SysRoleMenuEntity;
import lombok.Data;

import java.util.List;

@Data
public class SysRoleEntities {

    private SysRoleEntity sysRole;

    List<SysRoleMenuEntity> sysRoleMenuEntities;
}
