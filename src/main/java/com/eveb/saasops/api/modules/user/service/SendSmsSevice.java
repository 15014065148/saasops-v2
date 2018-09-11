package com.eveb.saasops.api.modules.user.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.eveb.saasops.api.modules.user.dto.SmsRes;
import com.eveb.saasops.api.utils.OkHttpUtils;
import com.eveb.saasops.common.exception.RRException;
import com.eveb.saasops.common.utils.StringUtil;
import com.google.gson.Gson;

@Service
public class SendSmsSevice{
	public void sendSms(Map<String, String> params, String url) {
		 String result = OkHttpUtils.postForm(url,params);
		if (StringUtil.isNotEmpty(result)){
			SmsRes smsRes = new Gson().fromJson(result, SmsRes.class);
			if (!smsRes.getCode().equals("2"))
				throw new RRException("短信发送失败!_原因："+smsRes.getMsg());
		}
	}
}
