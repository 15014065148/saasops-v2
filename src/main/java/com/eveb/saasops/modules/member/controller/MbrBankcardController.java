package com.eveb.saasops.modules.member.controller;

import javax.validation.constraints.NotNull;

import com.eveb.saasops.common.annotation.SysLog;
import com.eveb.saasops.modules.base.controller.AbstractController;
import com.eveb.saasops.modules.base.entity.BaseBank;
import com.eveb.saasops.modules.base.service.BaseBankService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eveb.saasops.common.constants.Constants;
import com.eveb.saasops.common.constants.Constants.Available;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.common.validator.Assert;
import com.eveb.saasops.modules.member.entity.MbrBankcard;
import com.eveb.saasops.modules.member.service.MbrAccountService;
import com.eveb.saasops.modules.member.service.MbrBankcardService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.List;

@RestController
@RequestMapping("/bkapi/member/mbrbankcard")
@Api(value = "MbrBankcard", description = "会员银行卡信息")
public class MbrBankcardController extends AbstractController {

    @Autowired
    private MbrBankcardService mbrBankcardService;
    @Autowired
    MbrAccountService mbrAccountService;
    @Autowired
    private BaseBankService baseBankService;

    @GetMapping("/list")
    @RequiresPermissions("member:mbrbankcard:list")
    @ApiOperation(value = "会员银行卡-列表", notes = "根据当前页及每页笔数显示")
    public R list(@ModelAttribute MbrBankcard mbrBankcard, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("userId") @NotNull Integer userId, @RequestParam("pageSize") @NotNull Integer pageSize, @RequestParam(value = "orderBy", required = false) String orderBy) {
        //不包含删除的银行卡
        mbrBankcard.setIsDel(Available.disable);
        mbrBankcard.setAccountId(userId);
        return R.ok().put("page", mbrBankcardService.queryListPage(mbrBankcard, pageNo, pageSize, orderBy));
    }


    @GetMapping("/info/{id}")
    @RequiresPermissions("member:mbrbankcard:info")
    @ApiOperation(value = "会员银行卡-查看", notes = "根据会员银行卡Id,显示会员银行卡信息")
    public R info(@PathVariable("id") Integer id) {
        MbrBankcard mbrBankcard = mbrBankcardService.queryObject(id);
        return R.ok().put("mbrBankcard", mbrBankcard);
    }


    @PostMapping("/save")
    @RequiresPermissions("member:mbrbankcard:save")
    @ApiOperation(value = "会员银行卡-保存", notes = "保存会员银行卡明细信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    @SysLog(module = "会员模块-银行卡", methodText = "新增银行卡")
    public R save(@RequestBody MbrBankcard mbrBankcard) {
        //会员与银行卡关联表增加id字段进行关联
        if (mbrBankcard.getBankCardId()!=null) {
            BaseBank baseBank = baseBankService.queryObject(mbrBankcard.getBankCardId());
            mbrBankcard.setBankName(baseBank.getBankName());
        } else {
            BaseBank baseBank = new BaseBank();
            baseBank.setBankName(mbrBankcard.getBankName());
            List<BaseBank> baseBanks = baseBankService.select(baseBank);
            mbrBankcard.setBankCardId(baseBanks.get(0).getId());
        }
        verifyBankCard(mbrBankcard);
        return mbrBankcardService.saveBankCard(mbrBankcard, Constants.EVNumber.two, getUser().getUsername());
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @RequiresPermissions("member:mbrbankcard:update")
    @ApiOperation(value = "会员银行卡-更新", notes = "更新会员银行卡明细信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    @SysLog(module = "会员模块-银行卡", methodText = "更新银行卡")
    public R update(@RequestBody MbrBankcard mbrBankcard) {
        Assert.isNull(mbrBankcard.getId(), "记不ID不能为空!");
        verifyBankCard(mbrBankcard);
        return mbrBankcardService.updateBankCard(mbrBankcard, Constants.sourceType.admin);
    }

    /**
     * 会员银行卡禁用启用
     *
     * @return
     */
    @PostMapping("/available")
    @RequiresPermissions("member:mbrbankcard:update")
    @ApiOperation(value = "会员银行卡-更新状态", notes = "状态更新")
    @SysLog(module = "会员模块-银行卡", methodText = "更新银行卡状态")
    public R available(@RequestBody MbrBankcard mbrBankcardDto) {
        MbrBankcard mbrBankcard = new MbrBankcard();
        mbrBankcard.setId(mbrBankcardDto.getId());
        mbrBankcard.setAvailable(mbrBankcardDto.getAvailable());
        mbrBankcardService.update(mbrBankcard);
        return R.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @RequiresPermissions("member:mbrbankcard:delete")
    @ApiOperation(value = "会员银行卡-删除", notes = "根据银行卡Id逻辑删除银行卡")
    @SysLog(module = "会员模块-银行卡", methodText = "删除银行卡")
    public R delete(@RequestBody MbrBankcard mbrBankcard) {
        mbrBankcardService.deleteBatch(mbrBankcard.getIds());
        return R.ok();
    }

    private void verifyBankCard(MbrBankcard mbrBankcard) {
        Assert.isNull(mbrBankcard.getAccountId(), "会员ID不能为空!");
        Assert.isBlank(mbrBankcard.getBankName(), "开户银行不能为空!");
        Assert.isBlank(mbrBankcard.getCardNo(), "开户账号不能为空!");
        Assert.isNumeric(mbrBankcard.getCardNo(), "开户账号只能为数字!");
        Assert.isBankCardNo(mbrBankcard.getCardNo(), "开户账号长度只能为16与19位!", 16, 19);
    }
}
