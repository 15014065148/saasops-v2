package com.eveb.saasops.modules.system.systemsetting.controller;


import java.util.ArrayList;
import java.util.List;

import com.eveb.saasops.common.utils.CommonUtil;
import com.eveb.saasops.modules.system.systemsetting.dto.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.eveb.saasops.common.annotation.SysLog;
import com.eveb.saasops.common.constants.SystemConstants;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.modules.system.systemsetting.entity.SysSetting;
import com.eveb.saasops.modules.system.systemsetting.service.SysSettingService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/bkapi/setting/syssetting")
public class SysSettingController {
  
	@Autowired
    private SysSettingService sysSettingService;

    
    @RequestMapping(value = "/saveWebTerms", method = RequestMethod.POST)
    @RequiresPermissions("setting:syssetting:save") 
    @ApiOperation(value="saveOrUpdate", notes="saveOrUpdate")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R saveOrUpdateWebTerms(@RequestParam(value="keyArray[]") List<String> keyArray,@RequestParam(value="valueArray[]") List<String> valueArray) { 	
    	//FIXME  站点前缀 asdf 是错误的，需要加上
    	SysWebTerms swt =sysSettingService.getMbrSysWebTerms("asdf");
    	List<SysSetting> ssList = new ArrayList<SysSetting>();
		for (int s = 0; s < keyArray.size(); s++) {
			SysSetting ss = new SysSetting();
			String key = keyArray.get(s);
			if((key.equals(SystemConstants.MEMBER_REGISTER_DISPLAY_TERMS_OF_WEBSITE))||(key.equals(SystemConstants.AGENT_REGISTER_DISPLAY_TERMS_OF_WEBSITE))) {
				ss.setSyskey(keyArray.get(s));
				ss.setSysvalue(valueArray.get(s));				
			}else {
				ss.setSyskey(keyArray.get(s));
				ss.setWebsiteTerms(valueArray.get(s));
			}
			ssList.add(ss);
		}   	
    	sysSettingService.modifyOrUpdate(ssList);
    	return R.ok();
    }
    
    /**
     * 获取注册设置
     */
    @SysLog(module = "系统设置",methodText = "保存入款设置")
    @RequestMapping(value = "/queryRegisterSet", method = RequestMethod.GET)
//    @RequiresPermissions("setting:syssetting:info") 
    @ApiOperation(value="queryRegisterSet", notes="queryRegisterSet")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryRegisterSet() { 	
    	RegisterSet registerSet = sysSettingService.queryRegisterSet();
    	return R.ok().put(registerSet);
    }  
    
    /**
     * 获取站点设置
     */
    @RequestMapping(value = "/queryStationSet", method = RequestMethod.GET)
    //@RequiresPermissions("setting:syssetting:info")
    @ApiOperation(value="queryStationSet", notes="queryRegisterSet")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryStationSet() { 	
    	StationSet stationSet = sysSettingService.queryStationSet();
    	return R.ok().put(stationSet);
    }  
    
    
    @RequestMapping(value = "/queryStationInfo", method = RequestMethod.GET)
    //@RequiresPermissions("setting:syssetting:info")
    @ApiOperation(value="queryStationSet", notes="queryRegisterSet")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryStationInfo() { 	
    	StationSet stationSet = sysSettingService.queryStationSet();
    	return R.ok().put(stationSet.getWebsiteTitle());
    }  
    
    /**
     * 获取邮件设置
     */
    @RequestMapping(value = "/queryMailSet", method = RequestMethod.GET)
    @RequiresPermissions("setting:syssetting:info") 
    @ApiOperation(value="queryMailSet", notes="queryRegisterSet")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryMailSet() { 	
    	MailSet mailSet = sysSettingService.queryMaliSet();
    	return R.ok().put(mailSet);
    }  
    
    /**
     * 获取短信设置
     */
    @RequestMapping(value = "/querySmsSet", method = RequestMethod.GET)
    @RequiresPermissions("setting:syssetting:info") 
    @ApiOperation(value="querySmsSet", notes="querySmsSet")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R querySmsSet() { 	
    	SmsSet smSet = sysSettingService.querySmsSet();
    	return R.ok().put(smSet);
    }  
    
    /**
     * 获取短信设置
     */
    @RequestMapping(value = "/queryWebTerms", method = RequestMethod.GET)
    @RequiresPermissions("setting:syssetting:info") 
    @ApiOperation(value="queryWebTerms", notes="queryWebTerms")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryWebTerms() { 	
    	WebTerms webTerms = sysSettingService.queryWebTerms();
    	return R.ok().put(webTerms);
    }  
    
    //站点设置
    @SysLog(module = "系统设置",methodText = "保存或更新站点设置")
    @RequestMapping(value = "/modifyStationSet", method = RequestMethod.POST)
    @RequiresPermissions("setting:syssetting:save") 
    @ApiOperation(value="saveOrUpdate", notes="saveOrUpdate")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R modifyStationSet(@RequestParam String stationSet,@RequestParam(value = "logoPicFile", required = false) MultipartFile logoPicFile, @RequestParam(value = "titlePicFile", required = false) MultipartFile titlePicFile) {
    	sysSettingService.saveStationSet(StringEscapeUtils.unescapeHtml4(stationSet));
    	sysSettingService.modifyPic(titlePicFile, logoPicFile);
    	return R.ok();
    } 
    //邮件设置
    @SysLog(module = "系统设置",methodText = "保存或更新邮件设置")
    @RequestMapping(value = "/modifyMailSet", method = RequestMethod.POST)
    @RequiresPermissions("setting:syssetting:save") 
    @ApiOperation(value="saveOrUpdate", notes="saveOrUpdate")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R modifyMailSet(@RequestBody MailSet mailSet) { 	
    	sysSettingService.saveMailSet(mailSet);
    	return R.ok();
    } 
    //短信设置
    @SysLog(module = "系统设置",methodText = "保存或更新短信设置")
    @RequestMapping(value = "/modifySmsSet", method = RequestMethod.POST)
    @RequiresPermissions("setting:syssetting:save") 
    @ApiOperation(value="saveOrUpdate", notes="saveOrUpdate")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R modifySmsSet(@RequestBody SmsSet smsSet) { 	
    	sysSettingService.saveSmsSet(smsSet);
    	return R.ok();
    } 
   
    //注册设置
    @SysLog(module = "系统设置",methodText = "保存或更新注册设置")
    @RequestMapping(value = "/modifyRegisterSet", method = RequestMethod.POST)
    @RequiresPermissions("setting:syssetting:save") 
    @ApiOperation(value="saveOrUpdate", notes="saveOrUpdate")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R modifyRegisterSet(@RequestBody RegisterSet registerSet) { 	
    	sysSettingService.saveRegSet(registerSet);
    	return R.ok();
    } 
    //用户注册条款
    @SysLog(module = "系统设置",methodText = "保存或更新用户注册条款") 
    @RequestMapping(value = "/modifyWebTerms", method = RequestMethod.POST)
    @RequiresPermissions("setting:syssetting:save") 
    @ApiOperation(value="saveOrUpdate", notes="saveOrUpdate")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R modifyWebTerms(@RequestBody WebTerms webTerms) { 	
    	sysSettingService.saveWebTerms(webTerms, CommonUtil.getSiteCode());
    	return R.ok();
    }
    
    //测试接收短信
    @SysLog(module = "系统设置",methodText = "测试接收短信")
    @RequestMapping(value = "/testReceiveSms", method = RequestMethod.GET)
    @RequiresPermissions("setting:syssetting:save") 
    @ApiOperation(value="saveOrUpdate", notes="saveOrUpdate")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R tryReceiveSms(@ModelAttribute SmsSet smsSet) { 	
    	String code = sysSettingService.testReceiveSms(smsSet);
    	return R.ok().put(code);
    } 
    
    //测试接收邮件
    @SysLog(module = "系统设置",methodText = "测试接收邮件")
    @RequestMapping(value = "/testReceiveMail", method = RequestMethod.GET)
    @RequiresPermissions("setting:syssetting:save") 
    @ApiOperation(value="saveOrUpdate", notes="saveOrUpdate")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R tryReceiveMail(@ModelAttribute MailSet mailSet) { 	
    	String content = sysSettingService.testReceiveMail(mailSet);
    	 if (StringUtils.isEmpty(content)) throw new RRException("发送邮件失败!");
    	return R.ok().put(content);
    }


    @SysLog(module = "系统设置", methodText = "保存或更新其它设置（出款）")
    @RequestMapping(value = "/modifyPaySet", method = RequestMethod.POST)
    @RequiresPermissions("setting:syssetting:save")
    @ApiOperation(value = "modifyPaySet", notes = "modifyPaySet")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R modifyPaySet(@RequestBody PaySet paySet) {
        sysSettingService.modifyPaySet(paySet);
        return R.ok();
    }

    @GetMapping(value = "/queryPaySet")
    @RequiresPermissions("setting:syssetting:info")
    @ApiOperation(value = "查询其他设置", notes = "查询其他设置")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryPaySet() {
        return R.ok().put(sysSettingService.queryPaySet());
    }
}
