package com.eveb.saasops.api.modules.user.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.springframework.util.StringUtils;

import com.eveb.saasops.api.utils.CipherText;
import com.eveb.saasops.common.exception.RRException;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "EV数据传送", description = "数据传送")
public class EvDataDto {
	@ApiModelProperty(value = "代理编码")
	private String cagent;
	@ApiModelProperty(value = "游戏账号长度不能大于20")
	private String loginname;
	@ApiModelProperty(value = "方法名称简写 lg:检测并创建账号")
	private String method;
	@ApiModelProperty(value = "actype{1真钱，0试玩}")
	private Integer actype;
	@ApiModelProperty(value = "账号密码长度不能小于20")
	private String password;
	@ApiModelProperty(value = "盘口 默认A")
	private String oddtype;
	@ApiModelProperty(value = "盘口 人民币 CNY")
	private String cur;
	@ApiModelProperty(value = "回转域名")
	private String dm;
	@ApiModelProperty(value = "序列号")
	private String sid;
	@ApiModelProperty(value = "语言")
	private Integer lang;
	@ApiModelProperty(value = "游戏代码")
	private Integer gameType;
	@ApiModelProperty(value = "转账类型(IN:从网站账号转款到游戏账号,OUT:從遊戲账號转款到網站賬號)")
	private String type;
	@ApiModelProperty(value = "转账金额，别名又叫额度")
	private Double credit;
	@ApiModelProperty(value = "cagent+序列")
	private String billno;
	@ApiModelProperty(value = "状态 值 = 1 代表调用‘预备转账成功， 值 = 0 失败 ")
	private Integer flag;
	@ApiModelProperty(value = "桌台号")
	private String limit;
	@ApiModelProperty(value = "桌台号")
	private String limitvideo;
	@ApiModelProperty(value = "桌台号")
	private String limitroulette;
	@ApiModelProperty(value = "时间")
	private String time;
	@ApiModelProperty(value = "token")
	private String token;
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		if (!StringUtils.isEmpty(cagent))
			buffer.append("cagent=").append(cagent).append("&");

		if (!StringUtils.isEmpty(loginname))
			buffer.append("loginname=").append(loginname).append("&");

		if (!StringUtils.isEmpty(method))
			buffer.append("method=").append(method).append("&");

		if (!StringUtils.isEmpty(actype))
			buffer.append("actype=").append(actype).append("&");

		if (!StringUtils.isEmpty(password))
			buffer.append("password=").append(password).append("&");

		if (!StringUtils.isEmpty(dm))
			buffer.append("dm=").append(dm).append("&");
		else
			buffer.append("dm=").append("&");

		if (!StringUtils.isEmpty(sid))
			buffer.append("sid=").append(sid).append("&");
		else {
			buffer.append("sid=").append("&");
		}
		if (!StringUtils.isEmpty(token))
			buffer.append("token=").append(token).append("&");
		else {
			buffer.append("token=").append("&");
		}
		
		if (!StringUtils.isEmpty(lang))
			buffer.append("lang=").append(lang).append("&");

		if (!StringUtils.isEmpty(gameType))
			buffer.append("gametype=").append(gameType).append("&");

		if (!StringUtils.isEmpty(oddtype))
			buffer.append("oddtype=").append(oddtype).append("&");
		if (!StringUtils.isEmpty(cur))
			buffer.append("cur=").append(cur).append("&");

		if (!StringUtils.isEmpty(type))
			buffer.append("type=").append(type).append("&");

		if (!StringUtils.isEmpty(credit))
			buffer.append("credit=").append(credit).append("&");

		if (!StringUtils.isEmpty(billno))
			buffer.append("billno=").append(billno).append("&");

		if (!StringUtils.isEmpty(flag))
			buffer.append("flag=").append(flag).append("&");

		if (!StringUtils.isEmpty(limit))
			buffer.append("limit=").append(limit).append("&");

		if (!StringUtils.isEmpty(limitvideo))
			buffer.append("limitvideo=").append(limitvideo).append("&");

		if (!StringUtils.isEmpty(limitroulette))
			buffer.append("limitroulette=").append(limitroulette).append("&");

		if (!StringUtils.isEmpty(time))
			buffer.append("time=").append(time).append("&");

		if (buffer.length() > 0)
			buffer.setLength(buffer.length() - 1);
		return buffer.toString();
	}

	public String getEncrypt(String key) {
		try {
			return CipherText.hexEncrypt(toString(), key);
		} catch (Exception e) {
			throw new RRException("EV加密数据异常");
		}
	}

	public List<BasicNameValuePair> getParams() {
		List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
		list.add(new BasicNameValuePair("cagent", getString(cagent)));
		list.add(new BasicNameValuePair("loginname", getString(loginname)));
		list.add(new BasicNameValuePair("password", getString(password)));
		list.add(new BasicNameValuePair("dm", getString(dm)));
		list.add(new BasicNameValuePair("lang", getString(lang)));
		list.add(new BasicNameValuePair("oddtype", getString(oddtype)));
		list.add(new BasicNameValuePair("gameType", getString(gameType)));
		list.add(new BasicNameValuePair("sid", getString(sid)));
		list.add(new BasicNameValuePair("token", getString(token)));
		list.add(new BasicNameValuePair("time", getString(time)));
		return list;
	}

	private String getString(Object str) {
		return str == null ? "" : String.valueOf(str);
	}

	public interface Mh5 {
		String isMobile = "y";// 手机端
		String isPC = "n";// 桌面端
	}
	
	/*
	 * public static void main(String args[]) { AGDataDto aa=new AGDataDto();
	 * aa.setActype("1231"); aa.setCur("asdfsada111");
	 * System.out.println(aa.toString());
	 * System.out.println("cagent=XXXXXX/\\\\/loginname=ptest98/\\\\/method=ice"); }
	 */
}
