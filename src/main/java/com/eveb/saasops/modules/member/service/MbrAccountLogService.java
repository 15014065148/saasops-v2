package com.eveb.saasops.modules.member.service;

import com.alibaba.fastjson.JSON;
import com.eveb.saasops.api.utils.JsonUtil;
import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.utils.*;
import com.eveb.saasops.modules.agent.dao.AgentAccountMapper;
import com.eveb.saasops.modules.agent.entity.AgentAccount;
import com.eveb.saasops.modules.member.dao.MbrAccountLogMapper;
import com.eveb.saasops.modules.member.dao.MbrAccountMapper;
import com.eveb.saasops.modules.member.dao.MbrGroupMapper;
import com.eveb.saasops.modules.member.dto.AccountLogDto;
import com.eveb.saasops.modules.member.entity.*;
import com.eveb.saasops.modules.member.mapper.MbrMapper;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.eveb.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.eveb.saasops.common.utils.DateUtil.getCurrentDate;
import static com.eveb.saasops.modules.member.entity.MbrAccountLog.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@Transactional
public class MbrAccountLogService {

    @Autowired
    private MbrAccountLogMapper accountLogMapper;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private MbrGroupMapper groupMapper;
    @Autowired
    private AgentAccountMapper agentAccountMapper;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private MbrMapper mbrMapper;

    public void updateAccountInfo(MbrAccount account, MbrAccount mbrAccount, String userName) {
        String beforeChange = "真实姓名:" + account.getRealName()
                + ",联系电话:" + account.getMobile() + ",邮箱:" + account.getEmail()
                + ",QQ:" + account.getQq() + ",微信:" + account.getWeChat();
        String afterChange = "真实姓名:" + (nonNull(mbrAccount.getRealName()) ? mbrAccount.getRealName() : account.getRealName())
                + ",联系电话:" + (nonNull(mbrAccount.getMobile()) ? mbrAccount.getMobile() : account.getMobile())
                + ",邮箱:" + (nonNull(mbrAccount.getEmail()) ? mbrAccount.getEmail() : account.getEmail())
                + ",QQ:" + (nonNull(mbrAccount.getQq()) ? mbrAccount.getQq() : account.getQq())
                + ",微信:" + (nonNull(mbrAccount.getWeChat()) ? mbrAccount.getWeChat() : account.getWeChat());
        addMbrAccountLog(account.getId(), account.getLoginName(), ACCOUNT_INFO,
                beforeChange, afterChange, userName, Constants.EVNumber.two);
    }

    public void updateAccountAvailable(Integer accountId, MbrAccount account, String userName) {
        MbrAccount account1 = accountMapper.selectByPrimaryKey(accountId);
        String beforeChange = account1.getAvailable() == 0 ? "禁用"
                : account1.getAvailable() == 1 ? "开启" : "余额冻结";
        String afterChange = account.getAvailable() == 0 ? "禁用"
                : account.getAvailable() == 1 ? "开启" : "余额冻结";
        addMbrAccountLog(accountId, account1.getLoginName(), ACCOUNT_STATUS,
                beforeChange, afterChange, userName, Constants.EVNumber.two);
    }

    public void addAccountMemo(String userName, MbrMemo mbrMemo) {
        MbrAccount account = accountMapper.selectByPrimaryKey(mbrMemo.getAccountId());
        addMbrAccountLog(mbrMemo.getAccountId(), account.getLoginName(),
                ACCOUNT_MEMO, null, mbrMemo.getMemo(), userName, Constants.EVNumber.two);
    }

    public void updateAccountRest(MbrAccount account, MbrAccount mbrAccount, String userName) {
        MbrGroup group = groupMapper.selectByPrimaryKey(account.getGroupId());
        AgentAccount agentAccount = agentAccountMapper.selectByPrimaryKey(account.getCagencyId());
        String beforeChange = "会员组:" + (nonNull(group) ? group.getGroupName() : "")
                + ",状态:" + (account.getAvailable() == 0 ? "禁用" : account.getAvailable() == 1 ? "开启" : "余额冻结")
                + ",代理:" + (nonNull(agentAccount) ? agentAccount.getAgyAccount() : "");
        MbrGroup group1 = groupMapper.selectByPrimaryKey(mbrAccount.getGroupId());
        AgentAccount agentAccount1 = agentAccountMapper.selectByPrimaryKey(account.getCagencyId());
        String afterChange = "会员组:" + group1.getGroupName()
                + ",状态:" + (mbrAccount.getAvailable() == 0 ? "禁用" : mbrAccount.getAvailable() == 1 ? "开启" : "余额冻结")
                + ",代理:" + (nonNull(agentAccount1) ? agentAccount1.getAgyAccount() : "");
        addMbrAccountLog(account.getId(), account.getLoginName(), ACCOUNT_INFO_OT,
                beforeChange, afterChange, userName, Constants.EVNumber.two);

    }

    public void addAccountBank(MbrBankcard mbrBankcard, String userName, int operatorType) {
        MbrAccount account = accountMapper.selectByPrimaryKey(mbrBankcard.getAccountId());
        addMbrAccountLog(mbrBankcard.getAccountId(), account.getLoginName(),
                ACCOUNT_BANK, null, mbrBankcard.getBankName(),
                isNull(userName) ? account.getLoginName() : userName, operatorType);
    }

    public void updateApiAccountName(MbrAccount mbrAccount, String realName) {
        addMbrAccountLog(mbrAccount.getId(), mbrAccount.getLoginName(),
                ACCOUNT_NAME, mbrAccount.getRealName(), realName,
                mbrAccount.getLoginName(), Constants.EVNumber.one);
    }

    public void updateApiAccountMail(MbrAccount account, String email) {
        MbrAccount mbrAccount = accountMapper.selectByPrimaryKey(account.getId());
        addMbrAccountLog(mbrAccount.getId(), mbrAccount.getLoginName(),
                ACCOUNT_EMAIL, account.getEmail(), email,
                mbrAccount.getLoginName(), Constants.EVNumber.one);
    }

    public void updateApiAccountMobile(MbrAccount account, String mobile) {
        MbrAccount mbrAccount = accountMapper.selectByPrimaryKey(account.getId());
        addMbrAccountLog(account.getId(), mbrAccount.getLoginName(),
                ACCOUNT_MOBILE, account.getMobile(), mobile,
                mbrAccount.getLoginName(), Constants.EVNumber.one);
    }


    private void addMbrAccountLog(Integer accountId, String loginName, String item, String beforeChange,
                                  String afterChange, String userName, int operatorType) {
        AccountLogDto accountLogDto = new AccountLogDto();
        accountLogDto.setStatus(ACCOUNT_SUCCEED);
        accountLogDto.setItem(item);
        accountLogDto.setBeforeChange(beforeChange);
        accountLogDto.setAfterChange(afterChange);
        accountLogDto.setOperatorUser(userName);
        accountLogDto.setOperatorType(operatorType);

        MbrAccountLog mbrAccountLog = new MbrAccountLog();
        mbrAccountLog.setAccountId(accountId);
        mbrAccountLog.setLoginName(loginName);
        mbrAccountLog.setOrderNo(new SnowFlake().nextId());
        mbrAccountLog.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        mbrAccountLog.setContent(JSON.toJSONString(accountLogDto));
        accountLogMapper.insert(mbrAccountLog);
    }

    public PageUtils accountLogList(Integer accountId, Integer pageNo, Integer pageSize, Long userId) {
        PageHelper.startPage(pageNo, pageSize);
        PageHelper.orderBy("createTime DESC");
        MbrAccountLog accountLog = new MbrAccountLog();
        accountLog.setAccountId(accountId);
        List<MbrAccountLog> accountLogs = accountLogMapper.select(accountLog);
        if (Collections3.isNotEmpty(accountLogs)) {
            String perms = mbrMapper.findAccountContact(userId, Constants.ACCOUNT_CONTACT);
            accountLogs.stream().forEach(as -> {
                as.setLogDto(jsonUtil.fromJson(as.getContent(), AccountLogDto.class));
                if (nonNull(as.getLogDto())) {
                    if (ACCOUNT_INFO.equals(as.getLogDto().getItem())) {
                        as.getLogDto().setBeforeChange(getAccountStr(as.getLogDto().getBeforeChange(), perms));
                        as.getLogDto().setAfterChange(getAccountStr(as.getLogDto().getAfterChange(), perms));
                    }
                    if (ACCOUNT_MOBILE.equals(as.getLogDto().getItem())) {
                        as.getLogDto().setBeforeChange(perms.contains("mobile")?as.getLogDto().getBeforeChange():StringUtil.phone(as.getLogDto().getBeforeChange()));
                        as.getLogDto().setAfterChange(perms.contains("mobile")?as.getLogDto().getAfterChange():StringUtil.phone(as.getLogDto().getAfterChange()));
                    }
                    if (ACCOUNT_EMAIL.equals(as.getLogDto().getItem())){
                        as.getLogDto().setBeforeChange(perms.contains("email")?as.getLogDto().getBeforeChange():StringUtil.mail(as.getLogDto().getBeforeChange()));
                        as.getLogDto().setAfterChange(perms.contains("email")?as.getLogDto().getAfterChange():StringUtil.mail(as.getLogDto().getAfterChange()));
                    }
                }
                as.setContent(null);
            });
        }
        return BeanUtil.toPagedResult(accountLogs);
    }

    private String getAccountStr(String str, String perms) {
        if (StringUtil.isNotEmpty(str)) {
            String[] arr = str.replace(",", ":").split(":");
            
            String mobile = StringUtil.phone(arr[3]);
            String email = StringUtil.mail(arr[5]);
            String qq = StringUtil.QQ(arr[7]);
            String wechat = (arr.length == 9 ? "":StringUtil.QQ(arr[9]));
            if(null==perms) {
            	return arr[0] + ":" + arr[1] + "," + arr[2] + ":"
                        + mobile + "," + arr[4] + ":" + email + ","
                        + arr[6] + ":" + qq + "," + arr[8] + ":" + wechat;
            }
            
            if(perms.contains("mobile")) {
            	mobile = arr[3];
            }
            if(perms.contains("email")) {
            	email = arr[5];
            }
            if(perms.contains("qq")) {
            	qq = arr[7];
            }
            if(perms.contains("wechat")) {
            	wechat = (arr.length == 9 ? "":arr[9]);
            }
            
            return arr[0] + ":" + arr[1] + "," + arr[2] + ":"
                    + mobile + "," + arr[4] + ":" + email + ","
                    + arr[6] + ":" + qq + "," + arr[8] + ":" + wechat;
        }
        return StringUtils.EMPTY;
    }
}
