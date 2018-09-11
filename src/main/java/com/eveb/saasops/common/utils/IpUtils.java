package com.eveb.saasops.common.utils;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import nl.bitwalker.useragentutils.Browser;
import nl.bitwalker.useragentutils.OperatingSystem;
import nl.bitwalker.useragentutils.UserAgent;
import nl.bitwalker.useragentutils.Version;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IpUtils {

	// 获取ip地址
	public static String getIp() {
		RequestAttributes ra = RequestContextHolder.getRequestAttributes();
		ServletRequestAttributes sra = (ServletRequestAttributes) ra;
		HttpServletRequest request = sra.getRequest();

		String ip = request.getHeader("x-forwraded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	// 获取User-Agent
	public static String getUAInfo() {
		RequestAttributes ra = RequestContextHolder.getRequestAttributes();
		ServletRequestAttributes sra = (ServletRequestAttributes) ra;
		HttpServletRequest request = sra.getRequest();
		String ua = request.getHeader("User-Agent");
		return ua;
	}

	public static UserAgent getUA() {
		return UserAgent.parseUserAgentString(getUAInfo());
	}

	// 获取当前浏览器
	public static Browser getBrower() {
		Browser browser = getUA().getBrowser();
		return browser;
	}

	// 获取当前浏览器信息详情
	public static String getBrowerName() {
		Version version = getBrower().getVersion(getUAInfo());
		String info =null ;
		if(version != null) {
			info = getBrower().getName() + "/" + version.getVersion();
		}
		return info;
	}

	// 获取当前操作系统
	public static OperatingSystem getOS() {
		OperatingSystem os = getUA().getOperatingSystem();
		return os;
	}

	// 获取当前操作系统名称
	public static String getOsName() {
		return System.getProperty("os.name");
	}

	public  static String  getServerIp(){
		String localip = null;// 本地IP，如果没有配置外网IP则返回它
		String netip = null;// 外网IP
		try {
			Enumeration netInterfaces = NetworkInterface.getNetworkInterfaces();
			InetAddress ip = null;
			boolean finded = false;// 是否找到外网IP
			while (netInterfaces.hasMoreElements() && !finded) {
				NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
				Enumeration address = ni.getInetAddresses();
				while (address.hasMoreElements()) {
					ip = (InetAddress) address.nextElement();
					if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {// 外网IP
						netip = ip.getHostAddress();
						finded = true;
						break;
					} else if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {// 内网IP
						localip = ip.getHostAddress();
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}

		if (netip != null && !"".equals(netip)) {
			return netip;
		} else {
			return localip;
		}
	}
	public static String getUrl(HttpServletRequest request)
	{
		StringBuffer url = request.getRequestURL();
		return url.delete(url.length() - request.getRequestURI().length(), url.length()).append("/").toString();
	}
}
