package com.eveb.saasops.modules.member.service;

import java.util.List;

import com.eveb.saasops.modules.sys.dao.SysUserMbrgrouprelationMapper;
import com.eveb.saasops.modules.sys.entity.SysUserMbrgrouprelation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.eveb.saasops.common.constants.Constants.Available;
import com.eveb.saasops.common.constants.GroupByConstants;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.modules.base.service.BaseService;
import com.eveb.saasops.modules.member.dao.MbrGroupMapper;
import com.eveb.saasops.modules.member.entity.MbrGroup;
import com.eveb.saasops.modules.member.mapper.MbrMapper;
import com.github.pagehelper.PageHelper;


@Service
public class MbrGroupService extends BaseService<MbrGroupMapper, MbrGroup> {
    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private MbrDepositCondService mbrDepositCondService;
    @Autowired
    private MbrWithdrawalCondService mbrWithdrawalCondService;
    @Autowired
    private SysUserMbrgrouprelationMapper mbrgrouprelationMapper;

    public PageUtils queryListPage(MbrGroup mbrGroup, Integer pageNo, Integer pageSize, String orderBy, Long userId) {
        mbrGroup.setBaseAuth(getRowAuth());
        PageHelper.startPage(pageNo, pageSize);
        orderBy = GroupByConstants.getOrderBy(GroupByConstants.groupMod, orderBy);
        PageHelper.orderBy(orderBy);
        List<MbrGroup> list = mbrMapper.findGroupList(mbrGroup);
        return BeanUtil.toPagedResult(list);
    }

    public List<Integer> getAllMbrGroupIds() {
        return mbrMapper.getAllMbrGroupIds();
    }

    public void deleteBatch(Long[] idArr) {
        if (mbrMapper.selectGroupCount(idArr) == 0)
            mbrMapper.deleteGroupBatch(idArr);
        SysUserMbrgrouprelation mbrgrouprelation = new SysUserMbrgrouprelation();
        mbrgrouprelation.setMbrGroupId(idArr[0].intValue());
        mbrgrouprelationMapper.delete(mbrgrouprelation);
    }

    public int updateGroupAvil(Integer id, Byte available) {
        int mod = 0;
        MbrGroup mbrGroup = new MbrGroup();
        mbrGroup.setId(id);
        mbrGroup.setAvailable(available);
        mbrGroup.setIsDef(Available.disable);
        if (mbrGroup.getAvailable() == Available.enable) {
            int deposit = mbrDepositCondService.selectCountNo(mbrGroup.getId());
            int withDrawl = mbrWithdrawalCondService.selectCountNo(mbrGroup.getId());
            if (deposit > 0 && withDrawl > 0) {
                checkMember(id, available);
                mod = mbrMapper.updateGroupAvil(mbrGroup);
            }/* else {
				throw new RRException("不能修改状态!");
			}*/
        } else {
            checkMember(id, available);
            mod = mbrMapper.updateGroupAvil(mbrGroup);
        }
        return mod;
    }

    public int save(MbrGroup group) {
        if (super.selectCount(group) > 0)
            throw new RRException("会员组名称已存在,请不要增加相同名称会员组!");
        return super.save(group);
    }

    public void checkMember(Integer groupId, Byte available) {
        if (available == Available.disable && (mbrMapper.countGroupMem(groupId) > 0)) {
            throw new RRException("请移出该会员组下会员后,方可使用禁用功能!");
        }
    }
}
