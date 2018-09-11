package com.eveb.saasops.modules.member.mapper;


import java.util.List;
import java.util.Map;

import com.eveb.saasops.modules.member.dto.ItemDto;
import com.eveb.saasops.modules.member.dto.TotalDto;
import com.eveb.saasops.modules.member.entity.*;
import com.eveb.saasops.modules.sys.entity.SysMenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface MbrMapper {


    List<MbrGroup> findGroupList(MbrGroup record);

    List<MbrAccount> findAccountList(MbrAccount record);

    List<MbrAccountOnline> findAccountOnlineList(MbrAccountOnline record);

    List<MbrDepotWallet> findAccountWallet(@Param("depotIds") Integer[] depotIds, @Param("accountId") Integer accountId);

    List<MbrDepotWallet> findDepots(Integer accountId);

    MbrAccount viewAccount(MbrAccount paramAccount);
    
    MbrAccount viewOtherAccount(MbrAccount paramAccount);

    MbrBankcard findBankCardOne(Integer id);

    int walletSubtract(MbrWallet record);

    int walletAdd(MbrWallet record);

    int selectGroupCount(Long[] idArr);

    int updateBankCardBatch(Long[] idArr);

    int updateGroupAvil(MbrGroup group);

    int updateGroupBatch(@Param("idArr") Integer[] idArr, @Param("groupId") Integer groupId);

    int updateStateBatch(@Param("idArr") Integer[] idArr, @Param("available") Byte available);

    int deleteGroupBatch(Long[] idArr);

    int deleteAccountBatch(Long[] idArr);

    List<MbrAccount> listAccName(@Param("accountIds") Integer[] accountIds);

    int deleteMemoBatch(Long[] idArr);


    List<String> getMemberAccountNames(MbrAccount record);

    int insertOrUpdateToken(MbrToken mbrToken);

    int updateBillManageStatus(MbrBillManage mbrBillManage);

    List<MbrBankcard> userBankCard(MbrBankcard bankCard);

    List<MbrAccount> queryMbrList(MbrAccount mbrAccount);

    int countSameBankNum(MbrBankcard bankCard);

    int updateOffline(@Param("loginName") String loginName);

    MbrAccount findMbrAccount(
            @Param("accountId") int accountId,
            @Param("registerStartTime") String registerStartTime,
            @Param("registerStartEnd") String registerStartEnd);

    List<Integer> getAllMbrGroupIds();

    int countGroupMem(@Param("groupId") Integer groupId);

    MbrBillManage findOrder(@Param("minutes") Integer minutes, @Param("orderNo") Long orderNo);

    MbrFundTotal mbrFundsTotal(MbrAccount mbrAccount);

    int updateTempTable();

    int insertBatchTemp(@Param("accountIds") Integer[] accountIds, @Param("uuid") Long uuid);

    List<TotalDto> totalMem(@Param("uuid") Long uuid);

    int delTotalMem(@Param("uuid") Long uuid);

    List<Map> selectRiskControlAudit(MbrAccount mbrAccount);

    List<Map> queryAccountBonusReporList(@Param("accountId") Integer accountId);

    List<MbrBillDetail> queryAccountFundList(@Param("accountId") Integer accountId);

    List<ItemDto> queryAccountAuditInfo(@Param("accountId") Integer accountId,
                                        @Param("keys") String keys,
                                        @Param("item") String item);

    List<MbrMemo> queryAccountMemoList(MbrMemo mbrMemo);

    List<MbrMemo> queryAccountSortMemo(@Param("accountId") Integer accountId);

    String findAccountContact(@Param("userId") Long userId, @Param("perms") String perms);

    int updateBankCardNameByAccId(@Param("accountId") Integer accountId, @Param("realName") String realName);

    List<Map> findHomePageCount(@Param("startday") String startday);

    List<SysMenuEntity> findAccountMenuByRoleId(@Param("roleId") Integer roleId);

    List<MbrCollect> findCollectList(@Param("userId") Long userId, @Param("roleId") Integer roleId);

    int findFreeWalletSwitchStatus(Integer accountId);
}
